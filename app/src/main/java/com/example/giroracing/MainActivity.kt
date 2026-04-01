package com.example.giroracing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.giroracing.ui.screens.GiroRacingScreen
import com.example.giroracing.ui.screens.MainMenuScreen
import com.example.giroracing.viewmodel.GiroRacingViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: GiroRacingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            
            NavHost(navController = navController, startDestination = "menu") {
                composable("menu") {
                    MainMenuScreen(onPlayClick = {
                        viewModel.resetGame()
                        navController.navigate("game")
                    })
                }
                composable("game") {
                    GiroRacingScreen(
                        viewModel = viewModel,
                        onBackToMenu = {
                            navController.navigate("menu") {
                                popUpTo("menu") { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}