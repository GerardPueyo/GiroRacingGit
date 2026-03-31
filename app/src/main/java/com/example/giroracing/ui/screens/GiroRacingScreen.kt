package com.example.giroracing.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.giroracing.sensors.AccelerometerHandler
import com.example.giroracing.viewmodel.GiroRacingViewModel

@Composable
fun GiroRacingScreen(viewModel: GiroRacingViewModel) {
    val context = LocalContext.current

    LaunchedEffect(viewModel.isGameOver) {
        if (!viewModel.isGameOver) {
            while (true) {
                withFrameMillis { }
            }
        }
    }

    LaunchedEffect(viewModel.isGameOver) {
        if (viewModel.isGameOver) {
            Toast.makeText(context, "¡FIN DEL JUEGO! Score: ${viewModel.score}", Toast.LENGTH_SHORT).show()
            kotlinx.coroutines.delay(1500)
            viewModel.resetGame()
        }
    }

    DisposableEffect(Unit) {
        val accelerometerHandler = AccelerometerHandler(context)
        val listener = accelerometerHandler.start { tiltX ->
            viewModel.updateOffset(tiltX)
        }
        onDispose { accelerometerHandler.stop(listener) }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        var accActive = false
                        var brakeActive = false
                        
                        event.changes.forEach { change ->
                            if (change.pressed) {
                                val x = change.position.x
                                val y = change.position.y
                                if (y > size.height * 0.75f) { // Zona de pedales un poco más amplia
                                    if (x > size.width * 0.5f) accActive = true
                                    else brakeActive = true
                                }
                            }
                        }
                        viewModel.setAccelerating(accActive)
                        viewModel.setBraking(brakeActive)
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(onTap = { offset ->
                    if (offset.y < size.height * 0.75f) {
                        viewModel.activateNitro()
                    }
                })
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val roadWidth = canvasWidth * 0.7f
        
        val carWidth = 50.dp.toPx()
        val carHeight = 100.dp.toPx()
        
        val centerX = (canvasWidth - carWidth) / 2
        
        // POSICIÓN DEL COCHE: A media pantalla (50% de la altura)
        val carY = (canvasHeight / 2) - (carHeight / 2)

        viewModel.updateGame(canvasHeight, roadWidth, carWidth, carHeight, carY, centerX)

        // 1. Entorno
        drawRect(color = Color(0xFF2D5A27))

        // 2. Carretera
        val roadLeft = (canvasWidth - roadWidth) / 2
        drawRect(
            color = Color(0xFF333333),
            topLeft = Offset(roadLeft, 0f),
            size = Size(roadWidth, canvasHeight)
        )

        // Líneas
        val dashLength = 40.dp.toPx()
        val totalLineLength = dashLength + 40.dp.toPx()
        val offsetTransition = (viewModel.roadOffset / 100f) * totalLineLength
        var currentLineY = -totalLineLength + offsetTransition
        while (currentLineY < canvasHeight) {
            drawRect(Color.White, Offset(canvasWidth / 2 - 2.dp.toPx(), currentLineY), Size(4.dp.toPx(), dashLength))
            currentLineY += totalLineLength
        }

        // 3. OBSTÁCULOS
        viewModel.obstacles.forEach { obstacle ->
            val obsX = centerX + obstacle.xOffset
            drawRect(obstacle.color, Offset(obsX, obstacle.y), Size(carWidth * 0.8f, carHeight * 0.8f))
            drawRect(Color(0xFF81D4FA), Offset(obsX + 5.dp.toPx(), obstacle.y + (carHeight * 0.8f) - 25.dp.toPx()), Size((carWidth * 0.8f) - 10.dp.toPx(), 15.dp.toPx()))
        }

        // 4. JUGADOR (Ferrari)
        val minX = roadLeft - centerX
        val maxX = (roadLeft + roadWidth) - centerX - carWidth
        viewModel.limitOffset(minX, maxX)
        val currentCarX = centerX + viewModel.carXOffsetPx
        
        drawF1Ferrari(currentCarX, carY, carWidth, carHeight, viewModel.isGameOver, viewModel.isNitroActive)

        // 5. PEDALES (Abajo en el césped)
        val pedalAreaY = canvasHeight * 0.82f
        val pedalWidth = 90.dp.toPx()
        val pedalHeight = 130.dp.toPx()
        
        // Freno (Izquierda)
        drawRect(Color.Red.copy(alpha = 0.7f), Offset(30.dp.toPx(), pedalAreaY), Size(pedalWidth, pedalHeight))
        drawContextTextSmall("FRENO", 35.dp.toPx(), pedalAreaY + pedalHeight/2)

        // Gas (Derecha)
        drawRect(Color.Green.copy(alpha = 0.7f), Offset(canvasWidth - pedalWidth - 30.dp.toPx(), pedalAreaY), Size(pedalWidth, pedalHeight))
        drawContextTextSmall("GAS", canvasWidth - pedalWidth - 25.dp.toPx(), pedalAreaY + pedalHeight/2)

        // 6. UI
        drawContextText("Score: ${viewModel.score}", 50f, 100f)
        drawContextText("Speed: ${(viewModel.currentSpeed * 10).toInt()} km/h", 50f, 180f)
        
        // Barra de Nitro
        val nitroBarWidth = 150.dp.toPx()
        val nitroBarHeight = 15.dp.toPx()
        val nitroBarX = canvasWidth - nitroBarWidth - 30.dp.toPx()
        val nitroBarY = 60.dp.toPx()
        drawRect(Color.Black.copy(alpha = 0.5f), Offset(nitroBarX - 4f, nitroBarY - 4f), Size(nitroBarWidth + 8f, nitroBarHeight + 8f))
        drawRect(if (viewModel.isNitroActive) Color.Cyan else Color(0xFFFFEB3B), Offset(nitroBarX, nitroBarY), Size(nitroBarWidth * viewModel.nitroLevel, nitroBarHeight))
    }
}

private fun DrawScope.drawF1Ferrari(x: Float, y: Float, w: Float, h: Float, isGameOver: Boolean, isNitro: Boolean) {
    val ferrariRed = if (isGameOver) Color.Gray else Color(0xFFE10600)
    val tireColor = Color(0xFF1A1A1A)
    val bodyBlack = Color(0xFF111111)
    
    drawRect(ferrariRed, Offset(x, y + h * 0.05f), Size(w, h * 0.06f))
    val wheelW = w * 0.28f
    val wheelH = h * 0.16f
    drawRect(bodyBlack, Offset(x + w * 0.15f, y + h * 0.18f), Size(w * 0.7f, 2.dp.toPx()))
    drawRect(tireColor, Offset(x + w * 0.02f, y + h * 0.12f), Size(wheelW, wheelH))
    drawRect(tireColor, Offset(x + w * 0.70f, y + h * 0.12f), Size(wheelW, wheelH))
    val noseW = w * 0.22f
    drawRect(ferrariRed, Offset(x + (w - noseW) / 2, y + h * 0.11f), Size(noseW, h * 0.35f))
    val bodyW = w * 0.65f
    val bodyX = x + (w - bodyW) / 2
    drawRect(ferrariRed, Offset(bodyX, y + h * 0.4f), Size(bodyW, h * 0.38f))
    val cockpitW = w * 0.25f
    drawRect(bodyBlack, Offset(x + (w - cockpitW) / 2, y + h * 0.48f), Size(cockpitW, h * 0.18f))
    drawCircle(Color.White, radius = w * 0.06f, center = Offset(x + w / 2, y + h * 0.55f))
    drawRect(tireColor, Offset(x - w * 0.02f, y + h * 0.68f), Size(wheelW * 1.1f, wheelH * 1.2f))
    drawRect(tireColor, Offset(x + w * 0.74f, y + h * 0.68f), Size(wheelW * 1.1f, wheelH * 1.2f))
    drawRect(ferrariRed, Offset(x + w * 0.1f, y + h * 0.85f), Size(w * 0.8f, h * 0.08f))

    if (isNitro) {
        drawRect(Color.Cyan, Offset(x + w * 0.4f, y + h * 0.93f), Size(w * 0.2f, 40f))
    }
}

private fun DrawScope.drawContextText(text: String, x: Float, y: Float) {
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 50f
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    drawContext.canvas.nativeCanvas.drawText(text, x, y, paint)
}

private fun DrawScope.drawContextTextSmall(text: String, x: Float, y: Float) {
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 30f
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    drawContext.canvas.nativeCanvas.drawText(text, x, y, paint)
}