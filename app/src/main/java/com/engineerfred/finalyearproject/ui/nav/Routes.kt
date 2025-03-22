package com.engineerfred.finalyearproject.ui.nav

sealed class Routes( val destination: String ) {
    data object OnBoardScreen: Routes("onboard")
    data object HomeScreen: Routes("home")
    data object CameraScreen: Routes("camera")
    data object UsernameScreen: Routes("username")

}