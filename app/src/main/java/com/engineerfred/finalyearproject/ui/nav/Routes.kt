package com.engineerfred.finalyearproject.ui.nav

sealed class Routes( val destination: String ) {
    data object onBoardScreen: Routes("onboard")
    data object HomeScreen: Routes("home")
    data object CameraScreen: Routes("camera")
}