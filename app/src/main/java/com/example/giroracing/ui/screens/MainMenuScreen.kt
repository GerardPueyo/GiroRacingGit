package com.example.giroracing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.giroracing.R

/**
 * The landing screen when you open the app. 
 * Provides options to start the game or visit the shop.
 */
@Composable
fun MainMenuScreen(onPlayClick: () -> Unit, onShopClick: () -> Unit) {
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF4CAF50), Color(0xFF1B5E20))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Game title
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                val titleText = stringResource(id = R.string.app_name).uppercase()
                Text(
                    text = titleText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF003300), // Shadow color
                    modifier = Modifier.offset(2.dp, 2.dp),
                    textAlign = TextAlign.Center,
                    softWrap = false
                )
                Text(
                    text = titleText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    softWrap = false
                )
            }
            
            Spacer(modifier = Modifier.height(64.dp))
            
            // Play button to start racing
            GameButton(
                text = stringResource(id = R.string.play),
                containerColor = Color(0xFFE10600),
                contentColor = Color.White,
                onClick = onPlayClick,
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Shop button to customize the car
            GameButton(
                text = stringResource(id = R.string.shop),
                containerColor = Color(0xFFFFD600),
                contentColor = Color(0xFF424242),
                onClick = onShopClick,
                fontSize = 22.sp
            )
        }
    }
}

/**
 * A custom reusable button with a stylized racing look.
 */
@Composable
fun GameButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    fontSize: TextUnit
) {
    Box(
        modifier = Modifier
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .widthIn(min = 200.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .padding(bottom = 6.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(containerColor)
                .border(4.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(horizontal = 32.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.uppercase(),
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
