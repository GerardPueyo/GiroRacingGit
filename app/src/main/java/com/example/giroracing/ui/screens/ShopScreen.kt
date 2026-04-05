package com.example.giroracing.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.giroracing.R
import com.example.giroracing.viewmodel.CarModel

/**
 * Data model for a car skin in the shop.
 */
data class CarSkin(val name: String, val price: String, val color: Color, val model: CarModel)

/**
 * The shop screen where players can select different car models and colors.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(onBackClick: () -> Unit, onSkinSelect: (Color, CarModel) -> Unit) {
    val ownedText = stringResource(id = R.string.owned)
    
    // List of available car skins
    val skins = listOf(
        CarSkin("Ferrari Red", ownedText, Color(0xFFE10600), CarModel.F1),
        CarSkin("McLaren Orange", "$1.99", Color(0xFFFF8000), CarModel.F1),
        CarSkin("Mercedes Silver", "$2.49", Color(0xFFC0C0C0), CarModel.F1),
        CarSkin("Classic Sedan", "$0.99", Color(0xFF1E88E5), CarModel.SEDAN),
        CarSkin("Sport Sedan", "$1.49", Color(0xFF43A047), CarModel.SEDAN)
    )

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF4CAF50), Color(0xFF1B5E20))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(id = R.string.shop).uppercase(), 
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B5E20),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(paddingValues)
        ) {
            // Scrollable list of shop items
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(skins) { skin ->
                    ShopItem(skin, onSkinSelect)
                }
            }
        }
    }
}

/**
 * A single item card in the shop showing the car preview and buy/select button.
 */
@Composable
fun ShopItem(skin: CarSkin, onSkinSelect: (Color, CarModel) -> Unit) {
    val ownedText = stringResource(id = R.string.owned)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .border(3.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Mini preview of the car skin
                Box(
                    modifier = Modifier
                        .size(70.dp, 90.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1B5E20))
                        .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        if (skin.model == CarModel.F1) {
                            drawF1Preview(skin.color)
                        } else {
                            drawSedanPreview(skin.color)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = skin.name,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (skin.model == CarModel.F1) stringResource(id = R.string.formula1) else stringResource(id = R.string.sedan),
                        color = Color(0xFFFFD600),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            GameButtonSmall(
                text = if (skin.price == ownedText) stringResource(id = R.string.select) 
                       else stringResource(id = R.string.buy, skin.price),
                containerColor = if (skin.price == ownedText) Color(0xFF757575) else Color(0xFFE10600),
                onClick = { onSkinSelect(skin.color, skin.model) }
            )
        }
    }
}

@Composable
fun GameButtonSmall(
    text: String,
    containerColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .padding(bottom = 4.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(containerColor)
                .border(2.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text.uppercase(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

/**
 * Simplified drawing of the F1 car for the shop preview.
 */
private fun DrawScope.drawF1Preview(carColor: Color) {
    val w = size.width
    val h = size.height
    val tireColor = Color(0xFF1A1A1A)
    val bodyBlack = Color(0xFF111111)
    
    drawRect(carColor, Offset(0f, h * 0.05f), Size(w, h * 0.06f))
    drawRect(tireColor, Offset(w * 0.02f, h * 0.12f), Size(w * 0.28f, h * 0.16f))
    drawRect(tireColor, Offset(w * 0.70f, h * 0.12f), Size(w * 0.28f, h * 0.16f))
    drawRect(carColor, Offset(w * 0.39f, h * 0.11f), Size(w * 0.22f, h * 0.35f))
    drawRect(carColor, Offset(w * 0.175f, h * 0.4f), Size(w * 0.65f, h * 0.38f))
    drawRect(bodyBlack, Offset(w * 0.375f, h * 0.48f), Size(w * 0.25f, h * 0.18f))
    drawRect(tireColor, Offset(-w * 0.02f, h * 0.68f), Size(w * 0.308f, h * 0.192f))
    drawRect(tireColor, Offset(w * 0.74f, h * 0.68f), Size(w * 0.308f, h * 0.192f))
    drawRect(carColor, Offset(w * 0.1f, h * 0.85f), Size(w * 0.8f, h * 0.08f))
}

/**
 * Simplified drawing of the Sedan car for the shop preview.
 */
private fun DrawScope.drawSedanPreview(carColor: Color) {
    val w = size.width
    val h = size.height
    val windowColor = Color(0xFF81D4FA)
    val tireColor = Color(0xFF1A1A1A)
    
    drawRect(tireColor, Offset(0f, h * 0.15f), Size(w * 0.2f, h * 0.15f))
    drawRect(tireColor, Offset(w * 0.8f, h * 0.15f), Size(w * 0.2f, h * 0.15f))
    drawRect(tireColor, Offset(0f, h * 0.7f), Size(w * 0.2f, h * 0.15f))
    drawRect(tireColor, Offset(w * 0.8f, h * 0.7f), Size(w * 0.2f, h * 0.15f))
    
    drawRect(carColor, Offset(w * 0.1f, h * 0.1f), Size(w * 0.8f, h * 0.8f))
    drawRect(windowColor, Offset(w * 0.2f, h * 0.3f), Size(w * 0.6f, h * 0.4f))
    drawRect(Color.Black.copy(alpha = 0.2f), Offset(w * 0.2f, h * 0.25f), Size(w * 0.6f, 2.dp.toPx()))
}
