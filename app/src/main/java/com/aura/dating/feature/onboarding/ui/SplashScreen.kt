package com.aura.dating.feature.onboarding.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aura.dating.core.designsystem.theme.AuraPrimaryGradient
import com.aura.dating.core.designsystem.theme.AuraRose
import com.aura.dating.core.designsystem.theme.DarkBackground
import com.aura.dating.core.designsystem.theme.Dimens
import com.aura.dating.feature.onboarding.viewmodel.OnboardingViewModel
import com.aura.dating.feature.onboarding.viewmodel.SplashNavigationState

@Composable
fun SplashScreen(
    onNavigateToWelcome: () -> Unit,
    onNavigateToLocationPermission: () -> Unit,
    onNavigateToCreateProfile: () -> Unit,
    onNavigateToMain: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val navigationState by viewModel.navigationState.collectAsState()
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = tween(700))
        alpha.animateTo(1f, animationSpec = tween(700))
        viewModel.checkInitialDestination()
    }

    LaunchedEffect(navigationState) {
        when (navigationState) {
            SplashNavigationState.NavigateToWelcome -> onNavigateToWelcome()
            SplashNavigationState.NavigateToLocationPermission -> onNavigateToLocationPermission()
            SplashNavigationState.NavigateToCreateProfile -> onNavigateToCreateProfile()
            SplashNavigationState.NavigateToMain -> onNavigateToMain()
            SplashNavigationState.Loading -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = AuraRose,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(Dimens.Spacing16))
            Text(
                text = "aura",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    brush = AuraPrimaryGradient
                )
            )
            Spacer(modifier = Modifier.height(Dimens.Spacing8))
            Text(
                text = "Real connections, nearby.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}
