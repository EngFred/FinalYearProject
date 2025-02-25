package com.engineerfred.finalyearproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.engineerfred.finalyearproject.ui.DetectorScreen
import com.engineerfred.finalyearproject.ui.theme.FinalYearProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinalYearProjectTheme {
                //checkModelOutputShape(this, "best.tflite")
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DetectorScreen(
                        modifier = Modifier.padding(innerPadding),
                        applicationContext
                    )
                }
            }
        }
    }
}