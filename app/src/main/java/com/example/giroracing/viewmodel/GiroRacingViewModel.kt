package com.example.giroracing.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
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

    var currentSpeed by mutableFloatStateOf(0f) // Empieza en 0 km/h
        private set

    // Cambiamos a mutableStateOf para que Compose detecte el cambio y refresque la UI
    var isAccelerating by mutableStateOf(false)
        private set
    var isBraking by mutableStateOf(false)
        private set

    val obstacles = mutableStateListOf<Obstacle>()

    private val maxSpeed = 50f           // 500 km/h
    private val accelerationRate = 0.35f
    private val decelerationRate = 0.05f // Fricción natural (lenta)
    private val brakeForce = 0.8f       // Freno (rápido)
    
    private var lastSpawnTime = 0L

    fun updateOffset(tiltX: Float) {
        if (isGameOver) return
        // Solo giramos si el coche se está moviendo
        val sensitivity = if (currentSpeed > 0) 18f else 0f
        carXOffsetPx -= (tiltX * sensitivity)
    }

    fun limitOffset(minX: Float, maxX: Float) {
        carXOffsetPx = carXOffsetPx.coerceIn(minX, maxX)
    }

    fun updateAcceleration(active: Boolean) {
        isAccelerating = active
    }

    fun updateBraking(active: Boolean) {
        isBraking = active
    }

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

    fun updateGame(
        canvasHeight: Float,
        roadWidth: Float,
        carWidth: Float,
        carHeight: Float,
        carY: Float,
        centerX: Float
    ) {
        if (isGameOver) return

        // Lógica de Velocidad
        if (isBraking) {
            currentSpeed = (currentSpeed - brakeForce).coerceAtLeast(0f)
        } else if (isAccelerating) {
            currentSpeed = (currentSpeed + accelerationRate).coerceAtMost(maxSpeed)
        } else {
            // Perder velocidad lentamente (fricción natural)
            currentSpeed = (currentSpeed - decelerationRate).coerceAtLeast(0f)
        }

        // Puntuación exponencial basada en velocidad
        score += (currentSpeed * currentSpeed / 50f).toLong()

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
        // Solo spawneamos obstáculos si nos movemos a cierta velocidad
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
