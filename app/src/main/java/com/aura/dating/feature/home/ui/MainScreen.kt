package com.aura.dating.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.aura.dating.core.designsystem.components.AuraBottomNavigation
import com.aura.dating.core.designsystem.components.AuraTab
import com.aura.dating.core.designsystem.theme.DarkBackground
import com.aura.dating.feature.discover.ui.DiscoverScreen
import com.aura.dating.feature.matches.ui.MatchesScreen
import com.aura.dating.feature.profile.ui.ProfileScreen
import com.aura.dating.feature.profile.viewmodel.ProfileViewModel

@Composable
fun MainScreen(
    onNavigateToUserProfile: (String) -> Unit,
    onNavigateToNearbyMap: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToConversation: (String, String, String?) -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToEditPhotos: () -> Unit,
    onNavigateToEditInterests: () -> Unit,
    onNavigateToSettings: () -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val profileUiState by profileViewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(AuraTab.DISCOVER) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Tab Content
        when (selectedTab) {
            AuraTab.DISCOVER -> {
                DiscoverScreen(
                    onNavigateToUserProfile = onNavigateToUserProfile,
                    onNavigateToNearbyMap = onNavigateToNearbyMap,
                    onNavigateToNotifications = onNavigateToNotifications,
                    onNavigateToConversation = onNavigateToConversation,
                    myProfile = profileUiState.myProfile
                )
            }
            AuraTab.MATCHES -> {
                MatchesScreen(
                    onNavigateToConversation = onNavigateToConversation,
                    onNavigateToUserProfile = onNavigateToUserProfile
                )
            }
            AuraTab.PROFILE -> {
                ProfileScreen(
                    onNavigateToEditProfile = onNavigateToEditProfile,
                    onNavigateToEditPhotos = onNavigateToEditPhotos,
                    onNavigateToEditInterests = onNavigateToEditInterests,
                    onNavigateToSettings = onNavigateToSettings,
                    viewModel = profileViewModel
                )
            }
        }

        // Floating Bottom Navigation
        AuraBottomNavigation(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
