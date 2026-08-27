package com.aura.dating.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aura.dating.core.designsystem.theme.AuraBlue
import com.aura.dating.core.designsystem.theme.AuraDarkCardGradient
import com.aura.dating.core.designsystem.theme.Dimens
import com.aura.dating.core.designsystem.theme.OnlineColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileCard(
    name: String,
    age: Int,
    photoUrls: List<String>,
    distanceText: String,
    bio: String?,
    interests: List<String>,
    isOnline: Boolean = false,
    isVerified: Boolean = true,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPhotoIndex by remember(photoUrls) { mutableIntStateOf(0) }
    val safePhotos = photoUrls.ifEmpty { listOf("") }

    Surface(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(Dimens.RadiusExtraLarge),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Photo display
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(safePhotos.getOrNull(currentPhotoIndex))
                    .crossfade(true)
                    .build(),
                contentDescription = "$name's photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Touch regions for browsing photos
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (currentPhotoIndex > 0) currentPhotoIndex--
                        }
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (currentPhotoIndex < safePhotos.lastIndex) currentPhotoIndex++
                        }
                )
            }

            // Top photo progress indicator bars
            if (safePhotos.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.Spacing16, vertical = Dimens.Spacing12),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing6)
                ) {
                    safePhotos.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == currentPhotoIndex) Color.White
                                    else Color.White.copy(alpha = 0.35f)
                                )
                        )
                    }
                }
            }

            // Bottom Gradient Overlay & Info
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(AuraDarkCardGradient)
                    .padding(Dimens.Spacing20)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$name, $age",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            if (isVerified) {
                                Spacer(modifier = Modifier.width(Dimens.Spacing6))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = AuraBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            if (isOnline) {
                                Spacer(modifier = Modifier.width(Dimens.Spacing8))
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(OnlineColor, CircleShape)
                                )
                            }
                        }

                        IconButton(
                            onClick = onInfoClick,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "View Profile Info",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimens.Spacing4))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(Dimens.Spacing4))
                        Text(
                            text = distanceText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    if (!bio.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(Dimens.Spacing8))
                        Text(
                            text = bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 2
                        )
                    }

                    if (interests.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(Dimens.Spacing10))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing6),
                            verticalArrangement = Arrangement.spacedBy(Dimens.Spacing6)
                        ) {
                            interests.take(4).forEach { interest ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(Dimens.RadiusPill))
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .padding(
                                            horizontal = Dimens.Spacing10,
                                            vertical = Dimens.Spacing4
                                        )
                                ) {
                                    Text(
                                        text = interest,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
