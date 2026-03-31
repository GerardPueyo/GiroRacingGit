package com.example.giroracing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.giroracing.ui.screens.GiroRacingScreen
import com.example.giroracing.viewmodel.GiroRacingViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: GiroRacingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GiroRacingScreen(viewModel)
        }
    }
}