package com.aura.dating.feature.discover.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aura.dating.core.common.utils.DistanceUtils
import com.aura.dating.core.designsystem.components.AuraTopBar
import com.aura.dating.core.designsystem.components.EmptyState
import com.aura.dating.core.designsystem.components.ErrorState
import com.aura.dating.core.designsystem.components.LoadingSkeletonCard
import com.aura.dating.core.designsystem.components.ProfileCard
import com.aura.dating.core.designsystem.theme.AuraBlue
import com.aura.dating.core.designsystem.theme.AuraRose
import com.aura.dating.core.designsystem.theme.DarkBackground
import com.aura.dating.core.designsystem.theme.Dimens
import com.aura.dating.core.designsystem.theme.LikeColor
import com.aura.dating.core.designsystem.theme.PassColor
import com.aura.dating.domain.discovery.model.DiscoveryCandidate
import com.aura.dating.domain.matching.model.SwipeActionType
import com.aura.dating.domain.profile.model.UserProfile
import com.aura.dating.feature.discover.viewmodel.LocationSearchViewModel

@Composable
fun SearchResultsScreen(
    title: String,
    subtitle: String,
    onNavigateBack: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    onNavigateToConversation: (String, String, String?) -> Unit,
    onAdjustFilters: () -> Unit,
    myProfile: UserProfile?,
    viewModel: LocationSearchViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Pagination trigger
    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = uiState.results.size
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleIndex >= totalItems - 3 && uiState.hasMore && !uiState.isPaginating
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadNextPage()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            AuraTopBar(
                title = title,
                showBackButton = true,
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = onAdjustFilters,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Adjust Filters",
                            tint = Color.White
                        )
                    }
                }
            )

            // Subtitle banner with filter summary & results count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.Spacing20, vertical = Dimens.Spacing4),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
                if (uiState.results.isNotEmpty()) {
                    Text(
                        text = "${uiState.results.size}${if (uiState.hasMore) "+" else ""} people",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AuraRose
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.Spacing8))

            // Main Content Area
            when {
                uiState.isSearching && uiState.results.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Dimens.Spacing16),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingSkeletonCard()
                    }
                }

                uiState.errorMessage != null && uiState.results.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ErrorState(
                            message = uiState.errorMessage ?: "Couldn't load people. Please try again.",
                            onRetry = { viewModel.executeSearch(isNewSearch = true) }
                        )
                    }
                }

                uiState.results.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            title = "No People Found",
                            description = "Try expanding your location, age range, or gender preferences.",
                            actionButtonText = "Change Filters",
                            onActionClick = onAdjustFilters
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = Dimens.Spacing16, vertical = Dimens.Spacing8),
                        verticalArrangement = Arrangement.spacedBy(Dimens.Spacing20),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(
                            items = uiState.results,
                            key = { _, candidate -> candidate.id }
                        ) { _, candidate ->
                            SearchResultCandidateCard(
                                candidate = candidate,
                                onProfileClick = { onNavigateToUserProfile(candidate.id) },
                                onPassClick = { viewModel.swipeCandidate(candidate, SwipeActionType.PASS) },
                                onLikeClick = { viewModel.swipeCandidate(candidate, SwipeActionType.LIKE) },
                                onSuperLikeClick = { viewModel.swipeCandidate(candidate, SwipeActionType.SUPER_LIKE) }
                            )
                        }

                        if (uiState.isPaginating) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Dimens.Spacing16),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = AuraRose,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Mutual Match Celebration Dialog
        uiState.matchResult?.let { match ->
            if (match.isMatch && match.matchedUser != null) {
                MatchCelebrationDialog(
                    swipeResult = match,
                    myProfile = myProfile,
                    onSendMessageClick = { matchId, name, photoUrl ->
                        viewModel.clearMatchDialog()
                        onNavigateToConversation(matchId, name, photoUrl)
                    },
                    onKeepBrowsingClick = {
                        viewModel.clearMatchDialog()
                    }
                )
            }
        }
    }
}

@Composable
private fun SearchResultCandidateCard(
    candidate: DiscoveryCandidate,
    onProfileClick: () -> Unit,
    onPassClick: () -> Unit,
    onLikeClick: () -> Unit,
    onSuperLikeClick: () -> Unit
) {
    val locationLabel = when {
        !candidate.cityName.isNullOrBlank() && !candidate.regionName.isNullOrBlank() -> "${candidate.cityName}, ${candidate.regionName}"
        !candidate.cityName.isNullOrBlank() -> candidate.cityName
        !candidate.regionName.isNullOrBlank() -> candidate.regionName
        !candidate.countryName.isNullOrBlank() -> candidate.countryName
        else -> DistanceUtils.formatDistance(candidate.distanceKm)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(440.dp)
        ) {
            ProfileCard(
                name = candidate.displayName,
                age = candidate.age,
                photoUrls = candidate.photos.map { it.photoUrl },
                distanceText = locationLabel,
                bio = candidate.bio,
                interests = candidate.interests.map { it.name },
                isOnline = candidate.isOnline,
                onInfoClick = onProfileClick,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(Dimens.Spacing12))

        // Action Buttons Row (Pass, SuperLike, Like)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.Spacing24),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pass Button
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onPassClick),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Pass",
                        tint = PassColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Super Like Button
            Surface(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onSuperLikeClick),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Super Like",
                        tint = AuraBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Like Button
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onLikeClick),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Like",
                        tint = LikeColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
