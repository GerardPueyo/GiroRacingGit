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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

data class Obstacle(val xOffset: Float, var y: Float, val color: Color)

class GiroRacingViewModel : ViewModel() {
    var carXOffsetPx by mutableFloatStateOf(0f)
        private set

    var roadOffset by mutableFloatStateOf(0f)
        private set

    var isGameOver by mutableStateOf(false)
        private set

    var score by mutableLongStateOf(0L)
        private set

    var isNitroActive by mutableStateOf(false)
        private set

    var nitroLevel by mutableFloatStateOf(1f) // 0 to 1
        private set

    var currentSpeed by mutableFloatStateOf(0f)
        private set

    private var isAccelerating = false
    private var isBraking = false

    val obstacles = mutableStateListOf<Obstacle>()

    private val maxNormalSpeed = 15f
    private val nitroSpeed = 28f
    private val acceleration = 0.2f
    private val deceleration = 0.1f
    private val brakeForce = 0.5f
    
    private var lastSpawnTime = 0L

    fun updateOffset(tiltX: Float) {
        if (isGameOver) return
        val sensitivity = if (isNitroActive) 20f else 15f
        carXOffsetPx -= (tiltX * sensitivity)
    }

    fun limitOffset(minX: Float, maxX: Float) {
        carXOffsetPx = carXOffsetPx.coerceIn(minX, maxX)
    }

    fun setAccelerating(active: Boolean) {
        isAccelerating = active
    }

    fun setBraking(active: Boolean) {
        isBraking = active
    }

    fun activateNitro() {
        if (isGameOver || isNitroActive || nitroLevel < 0.2f) return
        
        viewModelScope.launch {
            isNitroActive = true
            while (nitroLevel > 0f && isNitroActive) {
                delay(100)
                nitroLevel -= 0.05f
            }
            isNitroActive = false
        }
    }

    fun resetGame() {
        carXOffsetPx = 0f
        obstacles.clear()
        isGameOver = false
        score = 0
        nitroLevel = 1f
        isNitroActive = false
        currentSpeed = 0f
        isAccelerating = false
        isBraking = false
        lastSpawnTime = System.currentTimeMillis()
    }

    fun updateGame(
        canvasHeight: Float,
        roadWidth: Float,
        carWidth: Float,
        carHeight: Float,
        carY: Float,
        centerX: Float
    ) {
        if (isGameOver) return

        // Speed Logic
        val targetMaxSpeed = if (isNitroActive) nitroSpeed else maxNormalSpeed
        
        if (isBraking) {
            currentSpeed = (currentSpeed - brakeForce).coerceAtLeast(0f)
        } else if (isAccelerating) {
            currentSpeed = (currentSpeed + acceleration).coerceAtMost(targetMaxSpeed)
        } else {
            // Natural friction
            currentSpeed = (currentSpeed - deceleration).coerceAtLeast(0f)
        }

        // Score based on speed
        score += (currentSpeed / 5f).toLong()

        if (!isNitroActive && nitroLevel < 1f) {
            nitroLevel += 0.001f
        }

        roadOffset = (roadOffset + currentSpeed) % 100f

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

        val currentTime = System.currentTimeMillis()
        val spawnInterval = if (isNitroActive) 800 else 1500
        if (currentTime - lastSpawnTime > spawnInterval && obstacles.size < 5 && currentSpeed > 2f) {
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