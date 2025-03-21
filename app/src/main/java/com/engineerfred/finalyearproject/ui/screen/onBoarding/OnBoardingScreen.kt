package com.engineerfred.finalyearproject.ui.screen.onBoarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.engineerfred.finalyearproject.R
import com.engineerfred.finalyearproject.ui.screen.onBoarding.components.OnboardingPage
import com.engineerfred.finalyearproject.ui.screen.onBoarding.components.OnboardingPageUI
import com.engineerfred.finalyearproject.ui.screen.onBoarding.components.PageIndicator
import kotlinx.coroutines.launch

@Composable
fun OnBoardingScreen(
    onNavigateToHome: () -> Unit
) {

    val coroutineScope = rememberCoroutineScope()

    val pages = listOf(
        OnboardingPage(
            title = "Welcome to FracDetect",
            description = "Your AI-powered assistant for detecting bone fractures from X-ray images.",
            imageRes = R.drawable.dataset_cover
        ),
        OnboardingPage(
            title = "How It Works",
            description = "Select or capture an image, choose a detection model, and view results.",
            imageRes = R.drawable.dataset_cover
        ),
        OnboardingPage(
            title = "AI Predictions Are Not Always Perfect",
            description = "This app provides AI-assisted analysis but does not replace professional medical diagnosis.",
            imageRes = R.drawable.ic_warning
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            modifier = Modifier.weight(1f),
            state = pagerState
        ) { page ->
            OnboardingPageUI(page = pages[page])
        }
        PageIndicator(pagerState.currentPage, pages.size)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(36.dp),
            horizontalArrangement = if( pagerState.currentPage < pages.size - 1 ) Arrangement.SpaceBetween else Arrangement.Center,
        ) {
            if (pagerState.currentPage < pages.size - 1) {
                Button(
                    onClick = {
                    onNavigateToHome()
                }) {
                    Text(
                        "Skip",
                        color = Color.White,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(2f, 2f),
                                blurRadius = 4f
                            ),
                        )
                    )
                }
            }
            Button(
                modifier = if ( pagerState.currentPage < pages.size - 1 ) Modifier.wrapContentSize() else Modifier.fillMaxWidth(.85f),
                onClick = {
                if (pagerState.currentPage == pages.size - 1) {
                    onNavigateToHome()
                } else {
                    coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            }) {
                Text(if (pagerState.currentPage == pages.size - 1) "Got It!" else "Next", color = Color.White,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.5f),
                            offset = Offset(2f, 2f),
                            blurRadius = 4f
                        ),
                    )
                )
            }
        }
    }
}