package com.aura.dating.feature.discover.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aura.dating.core.designsystem.components.PrimaryButton
import com.aura.dating.core.designsystem.components.SecondaryButton
import com.aura.dating.core.designsystem.theme.AuraPrimaryGradient
import com.aura.dating.core.designsystem.theme.AuraRose
import com.aura.dating.core.designsystem.theme.DarkBackground
import com.aura.dating.core.designsystem.theme.Dimens
import com.aura.dating.domain.matching.model.SwipeResult
import com.aura.dating.domain.profile.model.UserProfile

@Composable
fun MatchCelebrationDialog(
    swipeResult: SwipeResult,
    myProfile: UserProfile?,
    onSendMessageClick: (matchId: String, name: String, photoUrl: String?) -> Unit,
    onKeepBrowsingClick: () -> Unit
) {
    val scale = remember { Animatable(0.5f) }
    val partner = swipeResult.matchedUser

    LaunchedEffect(Unit) {
        scale.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
    }

    Dialog(
        onDismissRequest = onKeepBrowsingClick,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground.copy(alpha = 0.95f))
                .padding(Dimens.Spacing24),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale.value)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = AuraRose,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing16))

                Text(
                    text = "It's a Match!",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        brush = AuraPrimaryGradient
                    )
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing8))

                Text(
                    text = "You and ${partner?.displayName ?: "someone"} liked each other",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing32))

                // Overlapping Avatars
                Box(
                    modifier = Modifier.height(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // My Photo (Left)
                    Box(
                        modifier = Modifier
                            .padding(end = 60.dp)
                            .size(110.dp)
                            .clip(CircleShape)
                            .border(3.dp, AuraRose, CircleShape)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(myProfile?.photos?.firstOrNull()?.photoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "My photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Partner Photo (Right)
                    Box(
                        modifier = Modifier
                            .padding(start = 60.dp)
                            .size(110.dp)
                            .clip(CircleShape)
                            .border(3.dp, AuraRose, CircleShape)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(partner?.photos?.firstOrNull()?.photoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "${partner?.displayName}'s photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.Spacing48))

                PrimaryButton(
                    text = "Say Hello to ${partner?.displayName ?: "Match"}",
                    onClick = {
                        if (swipeResult.matchId != null) {
                            onSendMessageClick(
                                swipeResult.matchId,
                                partner?.displayName ?: "Match",
                                partner?.photos?.firstOrNull()?.photoUrl
                            )
                        } else {
                            onKeepBrowsingClick()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing16))

                SecondaryButton(
                    text = "Keep Browsing",
                    onClick = onKeepBrowsingClick,
                    textColor = Color.White
                )
            }
        }
    }
}
