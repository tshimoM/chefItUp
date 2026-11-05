package com.chefitup.app.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chefitup.app.R
import com.chefitup.app.presentation.components.BrandCubeLoader
import com.chefitup.app.presentation.theme.Charcoal
import com.chefitup.app.presentation.theme.ChefMotion
import com.chefitup.app.presentation.theme.EmberGold
import com.chefitup.app.presentation.theme.FlameDeep
import com.chefitup.app.presentation.theme.FlameOrange
import com.chefitup.app.presentation.theme.FlameSoft
import kotlinx.coroutines.delay

/**
 * Dedicated loader page shown before the rest of the app.
 * Brand logo + 3D cube loader on a flame/charcoal canvas that matches the logo.
 */
@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val destination by viewModel.destination.collectAsStateWithLifecycle()

    val contentAlpha = remember { Animatable(0f) }
    val contentScale = remember { Animatable(0.92f) }
    val exitAlpha = remember { Animatable(1f) }
    var minTimeElapsed by remember { mutableStateOf(false) }

    val pulse = rememberInfiniteTransition(label = "loader_glow")
    val glow by pulse.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    val loadingPulse by pulse.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loading_text"
    )

    LaunchedEffect(Unit) {
        contentAlpha.animateTo(1f, tween(ChefMotion.Medium, easing = EaseOutCubic))
        contentScale.animateTo(1f, tween(ChefMotion.Slow, easing = EaseOutCubic))
        // Keep the loader page visible long enough to read the brand + cube.
        delay(2600)
        minTimeElapsed = true
    }

    LaunchedEffect(destination, minTimeElapsed) {
        if (!minTimeElapsed) return@LaunchedEffect
        when (destination) {
            SplashDestination.Loading -> Unit
            SplashDestination.Onboarding,
            SplashDestination.Login,
            SplashDestination.Home -> {
                exitAlpha.animateTo(0f, tween(ChefMotion.Medium))
                when (destination) {
                    SplashDestination.Onboarding -> onNavigateToOnboarding()
                    SplashDestination.Login -> onNavigateToLogin()
                    SplashDestination.Home -> onNavigateToHome()
                    SplashDestination.Loading -> Unit
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(exitAlpha.value)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Charcoal,
                        FlameDeep.copy(alpha = 0.92f),
                        FlameOrange.copy(alpha = 0.88f),
                        EmberGold.copy(alpha = 0.55f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Soft brand wash behind logo + cube
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            FlameSoft.copy(alpha = glow),
                            EmberGold.copy(alpha = glow * 0.45f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(contentScale.value)
                .alpha(contentAlpha.value)
        ) {
            Box(
                modifier = Modifier
                    .size(118.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.22f),
                                Color.White.copy(alpha = 0.06f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_chef_logo),
                    contentDescription = stringResource(R.string.cd_app_logo),
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = stringResource(R.string.splash_brand),
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.splash_tagline),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.88f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            BrandCubeLoader(size = 76.dp)

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = stringResource(R.string.splash_loading),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = loadingPulse),
                textAlign = TextAlign.Center
            )
        }
    }
}
