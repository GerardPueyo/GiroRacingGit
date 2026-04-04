package com.example.giroracing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.giroracing.data.UserPreferencesRepository
import com.example.giroracing.ui.screens.GiroRacingScreen
import com.example.giroracing.ui.screens.MainMenuScreen
import com.example.giroracing.ui.screens.ShopScreen
import com.example.giroracing.ui.theme.GiroRacingTheme
import com.example.giroracing.viewmodel.GiroRacingViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val repository = UserPreferencesRepository(applicationContext)
        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GiroRacingViewModel(repository) as T
            }
        }

        setContent {
            GiroRacingTheme {
                val viewModel: GiroRacingViewModel = viewModel(factory = viewModelFactory)
                val navController = rememberNavController()
                
                NavHost(navController = navController, startDestination = "menu") {
                    composable("menu") {
                        MainMenuScreen(
                            onPlayClick = {
                                viewModel.resetGame()
                                navController.navigate("game")
                            },
                            onShopClick = {
                                navController.navigate("shop")
                            }
                        )
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
                    composable("shop") {
                        ShopScreen(
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onSkinSelect = { color, model ->
                                viewModel.setCarSkin(color, model)
                            }
                        )
                    }
                }
            }
        }
    }
}
