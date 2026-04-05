package com.example.giroracing.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.giroracing.R
import com.example.giroracing.sensors.AccelerometerHandler
import com.example.giroracing.viewmodel.CarModel
import com.example.giroracing.viewmodel.GiroRacingViewModel

/**
 * The main game screen where the racing happens.
 * It handles the drawing loop, sensor inputs, and game UI.
 */
@Composable
fun GiroRacingScreen(viewModel: GiroRacingViewModel, onBackToMenu: () -> Unit) {
    val context = LocalContext.current
    val density = LocalDensity.current
    
    val scoreText = stringResource(id = R.string.score, viewModel.score)
    val speedText = stringResource(id = R.string.speed, (viewModel.currentSpeed * 10).toInt())
    val brakeText = stringResource(id = R.string.freno)
    val gasText = stringResource(id = R.string.gas)

    var canvasSize by remember { mutableStateOf(Size.Zero) }

    // Start the game update loop when the screen is active and the canvas is ready
    LaunchedEffect(viewModel.isGameOver, canvasSize) {
        if (!viewModel.isGameOver && canvasSize.width > 0) {
            while (true) {
                withFrameMillis {
                    val roadWidth = canvasSize.width * 0.7f
                    val carWidth = with(density) { 50.dp.toPx() }
                    val carHeight = with(density) { 100.dp.toPx() }
                    val centerX = (canvasSize.width - carWidth) / 2
                    val carY = (canvasSize.height / 2) - (carHeight / 2)
                    
                    viewModel.updateGame(
                        canvasHeight = canvasSize.height,
                        roadWidth = roadWidth,
                        carWidth = carWidth,
                        carHeight = carHeight,
                        carY = carY,
                        centerX = centerX
                    )
                }
            }
        }
    }

    // Set up the accelerometer to handle steering
    DisposableEffect(Unit) {
        val accelerometerHandler = AccelerometerHandler(context)
        var listener: android.hardware.SensorEventListener? = null
        
        if (accelerometerHandler.isAvailable()) {
            listener = accelerometerHandler.start { tiltX ->
                viewModel.updateOffset(tiltX)
            }
        }
        
        onDispose { 
            listener?.let { accelerometerHandler.stop(it) }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .onGloballyPositioned { coordinates ->
            canvasSize = Size(coordinates.size.width.toFloat(), coordinates.size.height.toFloat())
        }
    ) {
        // Main drawing area for the road, car, and obstacles
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            var accActive = false
                            var brakeActive = false
                            
                            // Detect touch on the bottom pedals
                            event.changes.forEach { change ->
                                if (change.pressed) {
                                    val x = change.position.x
                                    val y = change.position.y
                                    if (y > size.height * 0.7f) {
                                        if (x > size.width * 0.5f) accActive = true
                                        else brakeActive = true
                                    }
                                }
                            }
                            viewModel.updateAcceleration(accActive)
                            viewModel.updateBraking(brakeActive)
                        }
                    }
                }
        ) {
            if (size.width == 0f) return@Canvas
            
            val canvasWidth = size.width
            val canvasHeight = size.height
            val roadWidth = canvasWidth * 0.7f
            
            val carWidth = 50.dp.toPx()
            val carHeight = 100.dp.toPx()
            
            val centerX = (canvasWidth - carWidth) / 2
            val carY = (canvasHeight / 2) - (carHeight / 2)

            // Background grass
            drawRect(color = Color(0xFF2D5A27))

            // The road
            val roadLeft = (canvasWidth - roadWidth) / 2
            drawRect(
                color = Color(0xFF333333),
                topLeft = Offset(roadLeft, 0f),
                size = Size(roadWidth, canvasHeight)
            )

            // Animated lane dashes
            val dashLength = 40.dp.toPx()
            val totalLineLength = dashLength + 40.dp.toPx()
            val offsetTransition = (viewModel.roadOffset / 100f) * totalLineLength
            var currentLineY = -totalLineLength + offsetTransition
            while (currentLineY < canvasHeight) {
                drawRect(Color.White, Offset(canvasWidth / 2 - 2.dp.toPx(), currentLineY), Size(4.dp.toPx(), dashLength))
                currentLineY += totalLineLength
            }

            // Drawing obstacles
            viewModel.obstacles.forEach { obstacle ->
                val obsX = centerX + obstacle.xOffset
                drawRect(obstacle.color, Offset(obsX, obstacle.y), Size(carWidth * 0.8f, carHeight * 0.8f))
                drawRect(Color(0xFF81D4FA), Offset(obsX + 5.dp.toPx(), obstacle.y + (carHeight * 0.8f) - 25.dp.toPx()), Size((carWidth * 0.8f) - 10.dp.toPx(), 15.dp.toPx()))
            }

            // Handle car movement limits and draw the car
            val minX = roadLeft - centerX
            val maxX = (roadLeft + roadWidth) - centerX - carWidth
            viewModel.limitOffset(minX, maxX)
            val currentCarX = centerX + viewModel.carXOffsetPx
            
            val mainColor = if (viewModel.isGameOver) Color.Gray else viewModel.carColor
            if (viewModel.carModel == CarModel.F1) {
                drawF1(currentCarX, carY, carWidth, carHeight, mainColor)
            } else {
                drawSedan(currentCarX, carY, carWidth, carHeight, mainColor)
            }

            // UI Pedals
            val pedalAreaY = canvasHeight * 0.82f
            val pedalWidth = 90.dp.toPx()
            val pedalHeight = 130.dp.toPx()

            drawRect(
                color = Color.Red.copy(alpha = 0.8f), 
                topLeft = Offset(30.dp.toPx(), pedalAreaY), 
                size = Size(pedalWidth, pedalHeight)
            )
            drawContextTextSmall(brakeText, 35.dp.toPx(), pedalAreaY + pedalHeight/2 + 10f)

            drawRect(
                color = Color.Green.copy(alpha = 0.8f), 
                topLeft = Offset(canvasWidth - pedalWidth - 30.dp.toPx(), pedalAreaY), 
                size = Size(pedalWidth, pedalHeight)
            )
            drawContextTextSmall(gasText, canvasWidth - pedalWidth - 15.dp.toPx(), pedalAreaY + pedalHeight/2 + 10f)

            // HUD
            drawContextText(scoreText, 50f, 150f, size = 80f)
            drawContextText(speedText, 50f, 250f, size = 60f)
        }

        // Show game over dialog when player crashes
        if (viewModel.isGameOver) {
            GameOverDialog(
                score = viewModel.score,
                onRetry = { viewModel.resetGame() },
                onMenu = onBackToMenu
            )
        }
    }
}

/**
 * Dialog shown when the player loses, displaying the score and options to restart.
 */
@Composable
fun GameOverDialog(score: Long, onRetry: () -> Unit, onMenu: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .shadow(16.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF4CAF50), Color(0xFF1B5E20))))
                .border(4.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.game_over).uppercase(),
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(id = R.string.score, score),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD600),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            GameButtonDialog(
                text = stringResource(id = R.string.retry),
                containerColor = Color(0xFFE10600),
                contentColor = Color.White,
                onClick = onRetry
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            GameButtonDialog(
                text = stringResource(id = R.string.menu),
                containerColor = Color(0xFF757575),
                contentColor = Color.White,
                onClick = onMenu
            )
        }
    }
}

@Composable
fun GameButtonDialog(
    text: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .widthIn(min = 180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .padding(bottom = 5.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(containerColor)
                .border(3.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                .padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.uppercase(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Draws the Formula 1 style car on the canvas.
 */
fun DrawScope.drawF1(x: Float, y: Float, w: Float, h: Float, carColor: Color) {
    val tireColor = Color(0xFF1A1A1A)
    val bodyBlack = Color(0xFF111111)
    
    drawRect(carColor, Offset(x, y + h * 0.05f), Size(w, h * 0.06f))
    val wheelW = w * 0.28f
    val wheelH = h * 0.16f
    drawRect(tireColor, Offset(x + w * 0.02f, y + h * 0.12f), Size(wheelW, wheelH))
    drawRect(tireColor, Offset(x + w * 0.70f, y + h * 0.12f), Size(wheelW, wheelH))
    val noseW = w * 0.22f
    drawRect(carColor, Offset(x + (w - noseW) / 2, y + h * 0.11f), Size(noseW, h * 0.35f))
    val bodyW = w * 0.65f
    val bodyX = x + (w - bodyW) / 2
    drawRect(carColor, Offset(bodyX, y + h * 0.4f), Size(bodyW, h * 0.38f))
    val cockpitW = w * 0.25f
    drawRect(bodyBlack, Offset(x + (w - cockpitW) / 2, y + h * 0.48f), Size(cockpitW, h * 0.18f))
    drawCircle(Color.White, radius = w * 0.06f, center = Offset(x + w / 2, y + h * 0.55f))
    drawRect(tireColor, Offset(x - w * 0.02f, y + h * 0.68f), Size(wheelW * 1.1f, wheelH * 1.2f))
    drawRect(tireColor, Offset(x + w * 0.74f, y + h * 0.68f), Size(wheelW * 1.1f, wheelH * 1.2f))
    drawRect(carColor, Offset(x + w * 0.1f, y + h * 0.85f), Size(w * 0.8f, h * 0.08f))
}

/**
 * Draws the Sedan style car on the canvas.
 */
fun DrawScope.drawSedan(x: Float, y: Float, w: Float, h: Float, carColor: Color) {
    val tireColor = Color(0xFF1A1A1A)
    val windowColor = Color(0xFF81D4FA)

    val wheelW = w * 0.2f
    val wheelH = h * 0.15f
    drawRect(tireColor, Offset(x - 2.dp.toPx(), y + h * 0.15f), Size(wheelW, wheelH))
    drawRect(tireColor, Offset(x + w - wheelW + 2.dp.toPx(), y + h * 0.15f), Size(wheelW, wheelH))
    drawRect(tireColor, Offset(x - 2.dp.toPx(), y + h * 0.7f), Size(wheelW, wheelH))
    drawRect(tireColor, Offset(x + w - wheelW + 2.dp.toPx(), y + h * 0.7f), Size(wheelW, wheelH))

    drawRect(carColor, Offset(x, y + h * 0.1f), Size(w, h * 0.8f))
    

    drawRect(windowColor, Offset(x + w * 0.15f, y + h * 0.3f), Size(w * 0.7f, h * 0.35f))

    drawRect(Color.Black.copy(alpha = 0.2f), Offset(x + w * 0.2f, y + h * 0.25f), Size(w * 0.6f, 2.dp.toPx()))
}

private fun DrawScope.drawContextText(text: String, x: Float, y: Float, size: Float = 50f) {
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = size
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    drawContext.canvas.nativeCanvas.drawText(text, x, y, paint)
}

private fun DrawScope.drawContextTextSmall(text: String, x: Float, y: Float) {
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 35f
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    drawContext.canvas.nativeCanvas.drawText(text, x, y, paint)
}
