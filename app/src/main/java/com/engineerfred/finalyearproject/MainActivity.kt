package com.engineerfred.finalyearproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.engineerfred.finalyearproject.data.local.PrefsStore
import com.engineerfred.finalyearproject.ui.nav.AppGraph
import com.engineerfred.finalyearproject.ui.theme.DarkGrayBlue
import com.engineerfred.finalyearproject.ui.theme.FinalYearProjectTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var prefsStore: PrefsStore

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinalYearProjectTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = DarkGrayBlue,
                    contentColor = Color.White
                ) { innerPadding ->
                    AppGraph(
                        onOnBoardCompleted = {
                            prefsStore.setOnboardingCompleted(true)
                        },
                        isOnBoardCompleted = prefsStore.isOnboardingCompleted(),
                        detectionModel = prefsStore.getSelectedModel(),
                        onModelSelected = {
                            prefsStore.setSelectedModel(it)
                        },
                        username = prefsStore.getUsername(),
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}