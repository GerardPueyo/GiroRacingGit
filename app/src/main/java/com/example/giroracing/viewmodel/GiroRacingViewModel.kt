package com.example.giroracing.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.giroracing.data.UserPreferencesRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Simple data class for obstacles on the road.
 */
data class Obstacle(val xOffset: Float, var y: Float, val color: Color)

/**
 * Available car models in the game.
 */
enum class CarModel {
    F1, SEDAN
}

/**
 * Handles all the game logic, state, and car physics.
 * Communicates with the repository to persist selected car skins.
 */
class GiroRacingViewModel(private val repository: UserPreferencesRepository) : ViewModel() {
    
    // Game state variables
    var carXOffsetPx by mutableFloatStateOf(0f)
        private set

    var roadOffset by mutableFloatStateOf(0f)
        private set

    var isGameOver by mutableStateOf(false)
        private set

    var score by mutableLongStateOf(0L)
        private set

    var currentSpeed by mutableFloatStateOf(0f)
        private set

    var isAccelerating by mutableStateOf(false)
        private set
    var isBraking by mutableStateOf(false)
        private set

    var carColor by mutableStateOf(Color(0xFFE10600))
        private set
        
    var carModel by mutableStateOf(CarModel.F1)
        private set

    val obstacles = mutableStateListOf<Obstacle>()

    // Constants for car movement and game feel
    private val maxSpeed = 50f
    private val accelerationRate = 0.35f
    private val decelerationRate = 0.05f
    private val brakeForce = 0.8f
    
    private var lastSpawnTime = 0L

    init {
        // Load saved car preferences when the ViewModel is created
        viewModelScope.launch {
            repository.carColor.collectLatest { color ->
                carColor = color
            }
        }
        viewModelScope.launch {
            repository.carModel.collectLatest { model ->
                carModel = model
            }
        }
    }

    /**
     * Updates car position based on accelerometer tilt.
     */
    fun updateOffset(tiltX: Float) {
        if (isGameOver) return
        val sensitivity = if (currentSpeed > 0) 18f else 0f
        carXOffsetPx -= (tiltX * sensitivity)
    }

    /**
     * Keeps the car within the road boundaries.
     */
    fun limitOffset(minX: Float, maxX: Float) {
        carXOffsetPx = carXOffsetPx.coerceIn(minX, maxX)
    }

    fun updateAcceleration(active: Boolean) {
        isAccelerating = active
    }

    fun updateBraking(active: Boolean) {
        isBraking = active
    }

    /**
     * Updates the car's appearance and saves it to data store.
     */
    fun setCarSkin(color: Color, model: CarModel) {
        carColor = color
        carModel = model
        viewModelScope.launch {
            repository.saveCarSkin(color, model)
        }
    }

    /**
     * Resets game variables for a new round.
     */
    fun resetGame() {
        carXOffsetPx = 0f
        obstacles.clear()
        isGameOver = false
        score = 0
        currentSpeed = 0f
        isAccelerating = false
        isBraking = false
        lastSpawnTime = System.currentTimeMillis()
    }

    /**
     * Main game loop function. Updates movement, checks for collisions, and spawns obstacles.
     */
    fun updateGame(
        canvasHeight: Float,
        roadWidth: Float,
        carWidth: Float,
        carHeight: Float,
        carY: Float,
        centerX: Float
    ) {
        if (isGameOver) return

        // Speed physics
        if (isBraking) {
            currentSpeed = (currentSpeed - brakeForce).coerceAtLeast(0f)
        } else if (isAccelerating) {
            currentSpeed = (currentSpeed + accelerationRate).coerceAtMost(maxSpeed)
        } else {
            currentSpeed = (currentSpeed - decelerationRate).coerceAtLeast(0f)
        }

        // Score based on speed
        score += (currentSpeed * currentSpeed / 50f).toLong()
        roadOffset = (roadOffset + currentSpeed) % 100f

        // Obstacle movement and collision detection
        val iterator = obstacles.iterator()
        while (iterator.hasNext()) {
            val obstacle = iterator.next()
            obstacle.y += currentSpeed

            val currentCarX = centerX + carXOffsetPx
            val obstacleX = centerX + obstacle.xOffset
            
            if (checkCollision(
                    currentCarX, carY, carWidth, carHeight,
                    obstacleX, obstacle.y, carWidth * 0.8f, carHeight * 0.8f
                )
            ) {
                isGameOver = true
            }

            if (obstacle.y > canvasHeight) {
                iterator.remove()
            }
        }

        // Randomly spawn new obstacles based on current speed
        val currentTime = System.currentTimeMillis()
        if (currentSpeed > 5f) {
            val spawnInterval = (1500 * (15f / currentSpeed)).toLong().coerceAtLeast(350L)
            if (currentTime - lastSpawnTime > spawnInterval && obstacles.size < 8) {
                val margin = 20f
                val roadLeft = -roadWidth / 2 + margin
                val roadRight = roadWidth / 2 - carWidth - margin
                val randomX = Random.nextFloat() * (roadRight - roadLeft) + roadLeft
                
                obstacles.add(
                    Obstacle(
                        randomX,
                        -carHeight - 100f,
                        Color(Random.nextFloat(), Random.nextFloat(), Random.nextFloat(), 1f)
                    )
                )
                lastSpawnTime = currentTime
            }
        }
    }

    /**
     * Checks if the car's bounding box overlaps with an obstacle's bounding box.
     */
    private fun checkCollision(
        px: Float, py: Float, pw: Float, ph: Float,
        ox: Float, oy: Float, ow: Float, oh: Float
    ): Boolean {
        val pLeft = px + pw * 0.15f
        val pRight = px + pw * 0.85f
        val pTop = py + ph * 0.05f
        val pBottom = py + ph * 0.95f

        val oLeft = ox
        val oRight = ox + ow
        val oTop = oy
        val oBottom = oy + oh

        return pLeft < oRight && pRight > oLeft && pTop < oBottom && pBottom > oTop
    }
}
