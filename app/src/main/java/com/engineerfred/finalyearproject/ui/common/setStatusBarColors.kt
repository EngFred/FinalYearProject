package com.engineerfred.finalyearproject.ui.common

import android.view.View
import android.view.Window
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.engineerfred.finalyearproject.ui.theme.DarkGrayBlue
import com.engineerfred.finalyearproject.ui.theme.LightTeal

fun setStatusBarColors(
    window: Window,
    view: View,
    statusBarColor: Color = DarkGrayBlue,
    navigationBarColor: Color = LightTeal
) {
    window.statusBarColor = statusBarColor.toArgb()
    window.navigationBarColor = navigationBarColor.toArgb()
    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
    WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
}