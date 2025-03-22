package com.engineerfred.finalyearproject.ui.nav

import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.engineerfred.finalyearproject.domain.model.LiteModel
import com.engineerfred.finalyearproject.ui.screen.camera.CameraScreen
import com.engineerfred.finalyearproject.ui.screen.home.HomeScreen
import com.engineerfred.finalyearproject.ui.screen.onBoarding.OnBoardingScreen
import com.engineerfred.finalyearproject.ui.screen.username.UsernameScreen
import com.engineerfred.finalyearproject.ui.theme.DarkGrayBlue
import com.engineerfred.finalyearproject.ui.theme.LightTeal

@Composable
fun AppGraph(
    modifier: Modifier = Modifier,
    onOnBoardCompleted: () -> Unit,
    isOnBoardCompleted: Boolean,
    username: String?,
    detectionModel: LiteModel?,
    onModelSelected: (LiteModel) -> Unit,
    navController: NavHostController = rememberNavController()
) {

    val startDestination = if ( isOnBoardCompleted.not() ) {
        Routes.OnBoardScreen.destination
    } else {
        if ( username.isNullOrEmpty() ) {
            Routes.UsernameScreen.destination
        } else {
            Routes.HomeScreen.destination
        }
    }

    NavHost(
        modifier = modifier.fillMaxSize().background(
            brush = Brush.linearGradient(
                colors = listOf( DarkGrayBlue, LightTeal )
            )
        ),
        startDestination = startDestination,
        navController = navController
    ) {

        composable(
            route = Routes.HomeScreen.destination,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                )
            }
        ) { navBackStackEntry ->
            val capturedImageUrl = navBackStackEntry.savedStateHandle.get<String>("captured_img_url")
            HomeScreen(
                capturedImageUrl = capturedImageUrl,
                onCaptureImage = {
                    navController.navigate(Routes.CameraScreen.destination){
                        launchSingleTop = true
                    }
                },
                detectionModel = detectionModel,
                onModelSelected = onModelSelected
            )
        }

        composable(
            route = Routes.OnBoardScreen.destination
        ) {
            OnBoardingScreen(
                onNavigateToHome = {
                    onOnBoardCompleted()
                    navController.navigate(Routes.UsernameScreen.destination){
                        launchSingleTop = true
                        popUpTo(0)
                    }
                }
            )
        }

        composable(
            route = Routes.CameraScreen.destination,
        ) {
            CameraScreen(
                onCaptureComplete = {
                    val decodedImageUrl = Uri.decode(it)
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("captured_img_url", decodedImageUrl)
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.UsernameScreen.destination,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                )
            }
        ) {
            UsernameScreen(
                onUsernameSaved = {
                    navController.navigate(Routes.HomeScreen.destination){
                        launchSingleTop = true
                        popUpTo(0)
                    }
                }
            )
        }
    }
}