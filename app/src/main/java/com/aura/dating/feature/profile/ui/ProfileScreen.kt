package com.aura.dating.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Interests
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aura.dating.core.common.utils.DateTimeUtils
import com.aura.dating.core.designsystem.components.AuraTopBar
import com.aura.dating.core.designsystem.components.Avatar
import com.aura.dating.core.designsystem.components.InterestChip
import com.aura.dating.core.designsystem.theme.AuraBlue
import com.aura.dating.core.designsystem.theme.AuraRose
import com.aura.dating.core.designsystem.theme.DarkBackground
import com.aura.dating.core.designsystem.theme.Dimens
import com.aura.dating.feature.profile.viewmodel.ProfileViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    onNavigateToEditProfile: () -> Unit,
    onNavigateToEditPhotos: () -> Unit,
    onNavigateToEditInterests: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: ProfileViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val profile = uiState.myProfile

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            AuraTopBar(
                title = "My Profile",
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = Dimens.Spacing24)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(Dimens.Spacing16))

                // Avatar with Edit badge
                Box(contentAlignment = Alignment.BottomEnd) {
                    Avatar(
                        imageUrl = profile?.photos?.firstOrNull()?.photoUrl,
                        name = profile?.displayName ?: "Aura",
                        size = Dimens.AvatarSizeHero,
                        showBorderGradient = true
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AuraRose)
                            .clickable(onClick = onNavigateToEditPhotos)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Edit Photos",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.Spacing16))

                // Name & Age
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${profile?.displayName ?: "User"}, ${profile?.let { DateTimeUtils.calculateAge(it.birthDateMillis) } ?: 24}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(Dimens.Spacing6))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified Profile",
                        tint = AuraBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Location info
                val locationText = when {
                    !profile?.cityName.isNullOrBlank() && !profile.countryName.isNullOrBlank() -> {
                        val city = profile.cityName.trim()
                        val country = profile.countryName.trim()
                        "$city, $country"
                    }
                    !profile?.cityName.isNullOrBlank() -> profile.cityName.trim()
                    !profile?.regionName.isNullOrBlank() -> profile.regionName.trim()
                    !profile?.countryName.isNullOrBlank() -> profile.countryName.trim()
                    else -> null
                }

                if (!locationText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(Dimens.Spacing6))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(Dimens.Spacing4))
                        Text(
                            text = locationText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.Spacing24))

                // Quick Action Cards (Edit Profile, Photos, Interests)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing12)
                ) {
                    ProfileActionButton(
                        icon = Icons.Default.Edit,
                        label = "Edit Info",
                        onClick = onNavigateToEditProfile,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileActionButton(
                        icon = Icons.Default.AddPhotoAlternate,
                        label = "Photos (${profile?.photos?.size ?: 0})",
                        onClick = onNavigateToEditPhotos,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileActionButton(
                        icon = Icons.Default.Interests,
                        label = "Interests",
                        onClick = onNavigateToEditInterests,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.Spacing24))

                // Bio Section
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(Dimens.RadiusMedium)
                ) {
                    Column(modifier = Modifier.padding(Dimens.Spacing16)) {
                        Text(
                            text = "About Me",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(Dimens.Spacing8))
                        Text(
                            text = if (profile?.bio.isNullOrBlank()) "No bio written yet. Tap 'Edit Info' to introduce yourself." else profile.bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.Spacing16))

                // Interests Section
                if (!profile?.interests.isNullOrEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(Dimens.RadiusMedium)
                    ) {
                        Column(modifier = Modifier.padding(Dimens.Spacing16)) {
                            Text(
                                text = "My Interests",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(Dimens.Spacing12))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing8),
                                verticalArrangement = Arrangement.spacedBy(Dimens.Spacing8)
                            ) {
                                profile.interests.forEach { interest ->
                                    InterestChip(
                                        name = interest.name,
                                        icon = interest.icon,
                                        isSelected = true
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.Spacing32))
            }
        }
    }
}

@Composable
fun ProfileActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(Dimens.RadiusMedium))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(Dimens.RadiusMedium)
    ) {
        Column(
            modifier = Modifier.padding(vertical = Dimens.Spacing16, horizontal = Dimens.Spacing8),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = AuraRose,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(Dimens.Spacing8))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
