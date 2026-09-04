package com.aura.dating.feature.discover.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aura.dating.core.common.utils.DistanceUtils
import com.aura.dating.core.designsystem.components.AuraTopBar
import com.aura.dating.core.designsystem.components.EmptyState
import com.aura.dating.core.designsystem.components.ErrorState
import com.aura.dating.core.designsystem.components.FilterSheet
import com.aura.dating.core.designsystem.components.LoadingSkeletonCard
import com.aura.dating.core.designsystem.components.ProfileCard
import com.aura.dating.core.designsystem.components.SwipeCard
import com.aura.dating.core.designsystem.components.SwipeDirection
import com.aura.dating.core.designsystem.theme.AuraBlue
import com.aura.dating.core.designsystem.theme.AuraPrimaryGradient
import com.aura.dating.core.designsystem.theme.AuraSuperLikeGradient
import com.aura.dating.core.designsystem.theme.DarkBackground
import com.aura.dating.core.designsystem.theme.Dimens
import com.aura.dating.core.designsystem.theme.LikeColor
import com.aura.dating.core.designsystem.theme.PassColor
import com.aura.dating.domain.matching.model.SwipeActionType
import com.aura.dating.domain.profile.model.UserProfile
import com.aura.dating.feature.discover.viewmodel.DiscoverViewModel

import androidx.compose.material.icons.filled.Notifications

@Composable
fun DiscoverScreen(
    onNavigateToUserProfile: (String) -> Unit,
    onNavigateToNearbyMap: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToLocationSearch: () -> Unit,
    onNavigateToConversation: (String, String, String?) -> Unit,
    myProfile: UserProfile?,
    viewModel: DiscoverViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp) // Space for bottom navigation
        ) {
            // Header Bar
            AuraTopBar(
                title = "",
                showBrandedLogo = true,
                actions = {
                    IconButton(
                        onClick = onNavigateToNotifications,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = onNavigateToNearbyMap,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Nearby Radar",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { showFilterSheet = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filters",
                            tint = Color.White
                        )
                    }
                }
            )

            // Discovery Mode Segment Switch (📍 Near Me / 🌍 Search)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.Spacing20, vertical = Dimens.Spacing4)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 📍 Near Me (Active)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(AuraPrimaryGradient)
                        .padding(vertical = Dimens.Spacing8),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📍 Near Me",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // 🌍 Search (Clickable)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .clickable(onClick = onNavigateToLocationSearch)
                        .padding(vertical = Dimens.Spacing8),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🌍 Search",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // Main Discovery Card Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = Dimens.Spacing16, vertical = Dimens.Spacing8),
                contentAlignment = Alignment.Center
            ) {
                when {
                    uiState.isLoading && uiState.candidates.isEmpty() -> {
                        LoadingSkeletonCard()
                    }

                    uiState.errorMessage != null && uiState.candidates.isEmpty() -> {
                        ErrorState(
                            message = uiState.errorMessage ?: "Failed to load candidates",
                            onRetry = { viewModel.updateLocationAndFetchCandidates(forceRefresh = true) }
                        )
                    }

                    uiState.candidates.isEmpty() -> {
                        EmptyState(
                            title = "No More Profiles Nearby",
                            description = "You've viewed all nearby profiles matching your filters. Expand your search distance or age range to meet more people.",
                            actionButtonText = "Adjust Filters",
                            onActionClick = { showFilterSheet = true }
                        )
                    }

                    else -> {
                        // Display stacked cards (top 2 candidates for fluid animation)
                        val topCandidate = uiState.candidates.first()
                        val nextCandidate = uiState.candidates.getOrNull(1)

                        if (nextCandidate != null) {
                            ProfileCard(
                                name = nextCandidate.displayName,
                                age = nextCandidate.age,
                                photoUrls = nextCandidate.photos.map { it.photoUrl },
                                distanceText = DistanceUtils.formatDistance(nextCandidate.distanceKm, uiState.showDistance),
                                bio = nextCandidate.bio,
                                interests = nextCandidate.interests.map { it.name },
                                isOnline = nextCandidate.isOnline,
                                onInfoClick = { onNavigateToUserProfile(nextCandidate.id) },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 10.dp)
                            )
                        }

                        SwipeCard(
                            key = topCandidate.id,
                            onSwiped = { direction ->
                                val action = when (direction) {
                                    SwipeDirection.RIGHT -> SwipeActionType.LIKE
                                    SwipeDirection.LEFT -> SwipeActionType.PASS
                                    SwipeDirection.UP -> SwipeActionType.SUPER_LIKE
                                }
                                viewModel.onSwipe(topCandidate.id, action)
                            },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            ProfileCard(
                                name = topCandidate.displayName,
                                age = topCandidate.age,
                                photoUrls = topCandidate.photos.map { it.photoUrl },
                                distanceText = DistanceUtils.formatDistance(topCandidate.distanceKm, uiState.showDistance),
                                bio = topCandidate.bio,
                                interests = topCandidate.interests.map { it.name },
                                isOnline = topCandidate.isOnline,
                                onInfoClick = { onNavigateToUserProfile(topCandidate.id) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // Bottom Action Buttons (Pass, SuperLike, Like)
            if (uiState.candidates.isNotEmpty()) {
                val current = uiState.candidates.first()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimens.Spacing32, vertical = Dimens.Spacing8),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pass Button
                    Surface(
                        modifier = Modifier
                            .size(60.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .clickable { viewModel.onSwipe(current.id, SwipeActionType.PASS) },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Pass",
                                tint = PassColor,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    // Super Like Button
                    Surface(
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .clickable { viewModel.onSwipe(current.id, SwipeActionType.SUPER_LIKE) },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Super Like",
                                tint = AuraBlue,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    // Like Button
                    Surface(
                        modifier = Modifier
                            .size(60.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .clickable { viewModel.onSwipe(current.id, SwipeActionType.LIKE) },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Like",
                                tint = LikeColor,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            }
        }

        // Filter Sheet
        if (showFilterSheet) {
            FilterSheet(
                minAge = uiState.filter.minAge,
                maxAge = uiState.filter.maxAge,
                maxDistanceKm = uiState.filter.maxDistanceKm,
                selectedGender = uiState.filter.genderPreference,
                onlyOnline = uiState.filter.showOnlyOnline,
                onApply = { minAge, maxAge, maxDist, gender, online ->
                    viewModel.applyFilters(minAge, maxAge, maxDist, gender, online)
                },
                onDismiss = { showFilterSheet = false }
            )
        }

        // Match Celebration Dialog
        uiState.currentMatchCelebration?.let { match ->
            MatchCelebrationDialog(
                swipeResult = match,
                myProfile = myProfile,
                onSendMessageClick = { matchId, name, photoUrl ->
                    viewModel.dismissMatchCelebration()
                    onNavigateToConversation(matchId, name, photoUrl)
                },
                onKeepBrowsingClick = {
                    viewModel.dismissMatchCelebration()
                }
            )
        }
    }
}
