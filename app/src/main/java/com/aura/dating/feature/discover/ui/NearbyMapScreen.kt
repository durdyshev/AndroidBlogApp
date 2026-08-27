package com.aura.dating.feature.discover.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aura.dating.core.designsystem.components.AuraTopBar
import com.aura.dating.core.designsystem.components.Avatar
import com.aura.dating.core.designsystem.theme.AuraPrimaryGradient
import com.aura.dating.core.designsystem.theme.AuraRose
import com.aura.dating.core.designsystem.theme.DarkBackground
import com.aura.dating.core.designsystem.theme.Dimens
import com.aura.dating.feature.discover.viewmodel.DiscoverViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NearbyMapScreen(
    onNavigateBack: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    viewModel: DiscoverViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = Dimens.Spacing32)
        ) {
            AuraTopBar(
                title = "Nearby Radar",
                showBackButton = true,
                onBackClick = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.Spacing24),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Active Profiles in Your Area",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Coordinates are obfuscated to protect user privacy.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Radar Circle Container
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                // Radar Rings
                Box(
                    modifier = Modifier
                        .size(320.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                )

                // Center (You)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AuraPrimaryGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "You are here",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Surrounding nearby candidates placed geometrically on radar
                uiState.candidates.take(8).forEachIndexed { index, candidate ->
                    val angle = (index * (360.0 / 8.0) * (Math.PI / 180.0))
                    val radiusDp = 50 + (index % 3) * 45
                    val offsetX = (radiusDp * cos(angle)).toInt().dp
                    val offsetY = (radiusDp * sin(angle)).toInt().dp

                    Box(
                        modifier = Modifier
                            .offset(x = offsetX, y = offsetY)
                            .clickable { onNavigateToUserProfile(candidate.id) }
                    ) {
                        Avatar(
                            imageUrl = candidate.photos.firstOrNull()?.photoUrl,
                            name = candidate.displayName,
                            size = 40.dp,
                            isOnline = candidate.isOnline,
                            showBorderGradient = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1.2f))
        }
    }
}
