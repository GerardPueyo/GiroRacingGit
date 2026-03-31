package com.example.giroracing.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.giroracing.sensors.AccelerometerHandler
import com.example.giroracing.viewmodel.GiroRacingViewModel

@Composable
fun GiroRacingScreen(viewModel: GiroRacingViewModel) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val accelerometerHandler = AccelerometerHandler(context)
        val listener = accelerometerHandler.start { tiltX ->
            viewModel.updateOffset(tiltX)
        }
        
        onDispose {
            accelerometerHandler.stop(listener)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // 1. Dibujar el entorno (Césped)
        drawRect(color = Color(0xFF2D5A27))

        // 2. Definir la Carretera (70% del ancho de la pantalla)
        val roadWidth = canvasWidth * 0.7f
        val roadLeft = (canvasWidth - roadWidth) / 2
        val roadRight = roadLeft + roadWidth

        drawRect(
            color = Color(0xFF444444),
            topLeft = Offset(roadLeft, 0f),
            size = Size(roadWidth, canvasHeight)
        )

        // 3. EL COCHE Y SUS LÍMITES
        val carWidth = 45.dp.toPx()
        val carHeight = 80.dp.toPx()

        val centerX = (canvasWidth - carWidth) / 2
        val minX = roadLeft - centerX
        val maxX = roadRight - centerX - carWidth

        viewModel.limitOffset(minX, maxX)

        val currentCarX = centerX + viewModel.carXOffsetPx
        val carY = canvasHeight - carHeight - 100.dp.toPx()

        // Dibujo del coche
        drawRect(
            color = Color.Red,
            topLeft = Offset(currentCarX, carY),
            size = Size(carWidth, carHeight)
        )

        // Detalle (Parabrisas)
        drawRect(
            color = Color(0xFF81D4FA),
            topLeft = Offset(currentCarX + 5.dp.toPx(), carY + 10.dp.toPx()),
            size = Size(carWidth - 10.dp.toPx(), 20.dp.toPx())
        )
    }
}