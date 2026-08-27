package com.aura.dating.feature.settings.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aura.dating.core.designsystem.components.AuraTopBar
import com.aura.dating.core.designsystem.components.Avatar
import com.aura.dating.core.designsystem.components.ConfirmationDialog
import com.aura.dating.core.designsystem.components.EmptyState
import com.aura.dating.core.designsystem.components.SecondaryButton
import com.aura.dating.core.designsystem.theme.AuraRose
import com.aura.dating.core.designsystem.theme.DarkBackground
import com.aura.dating.core.designsystem.theme.Dimens
import com.aura.dating.core.designsystem.theme.PassColor
import com.aura.dating.feature.settings.viewmodel.SettingsEvent
import com.aura.dating.feature.settings.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToBlockedUsers: () -> Unit,
    onNavigateToAccount: () -> Unit,
    onNavigateToWelcome: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            if (event is SettingsEvent.NavigateToWelcome) {
                onNavigateToWelcome()
            }
        }
    }

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
                title = "Settings",
                showBackButton = true,
                onBackClick = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.Spacing20)
            ) {
                SettingsNavigationItem(
                    icon = Icons.Default.Notifications,
                    title = "Push Notifications",
                    subtitle = "Likes, matches, and chat alerts",
                    onClick = onNavigateToNotificationSettings
                )

                SettingsNavigationItem(
                    icon = Icons.Default.Lock,
                    title = "Privacy & Visibility",
                    subtitle = "Online status, approximate distance",
                    onClick = onNavigateToPrivacy
                )

                SettingsNavigationItem(
                    icon = Icons.Default.Block,
                    title = "Blocked Users",
                    subtitle = "Manage blocked profiles",
                    onClick = onNavigateToBlockedUsers
                )

                SettingsNavigationItem(
                    icon = Icons.Default.Person,
                    title = "Account",
                    subtitle = "Session and account removal",
                    onClick = onNavigateToAccount
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing32))

                SecondaryButton(
                    text = "Sign Out",
                    onClick = { showLogoutDialog = true },
                    textColor = Color.White
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing24))

                Text(
                    text = "Aura Dating & Social Discovery v1.0.0 (2026)",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        if (showLogoutDialog) {
            ConfirmationDialog(
                title = "Sign Out?",
                message = "Are you sure you want to sign out of your Aura account?",
                confirmText = "Sign Out",
                onConfirm = {
                    showLogoutDialog = false
                    viewModel.logout()
                },
                onDismiss = { showLogoutDialog = false }
            )
        }
    }
}

@Composable
fun SettingsNavigationItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.Spacing6)
            .clip(RoundedCornerShape(Dimens.RadiusMedium))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(Dimens.RadiusMedium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Spacing16),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AuraRose,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(Dimens.Spacing16))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AuraTopBar(
                title = "Notifications",
                showBackButton = true,
                onBackClick = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.Spacing24, vertical = Dimens.Spacing16)
            ) {
                SettingToggleRow(
                    title = "New Matches",
                    subtitle = "Notify when someone likes you back",
                    checked = uiState.newMatchesPush,
                    onCheckedChange = viewModel::toggleNewMatchesPush
                )

                SettingToggleRow(
                    title = "Messages",
                    subtitle = "Notify when you receive a new chat message",
                    checked = uiState.messagesPush,
                    onCheckedChange = viewModel::toggleMessagesPush
                )

                SettingToggleRow(
                    title = "New Likes",
                    subtitle = "Notify when you receive likes or super likes",
                    checked = uiState.likesPush,
                    onCheckedChange = viewModel::toggleLikesPush
                )
            }
        }
    }
}

@Composable
fun PrivacyScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AuraTopBar(
                title = "Privacy & Visibility",
                showBackButton = true,
                onBackClick = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.Spacing24, vertical = Dimens.Spacing16)
            ) {
                SettingToggleRow(
                    title = "Show Online Status",
                    subtitle = "Let matches see when you are active",
                    checked = uiState.showOnlineStatus,
                    onCheckedChange = viewModel::toggleShowOnline
                )

                SettingToggleRow(
                    title = "Show Approximate Distance",
                    subtitle = "Display approximate distance (e.g. 2.4 km away)",
                    checked = uiState.showDistance,
                    onCheckedChange = viewModel::toggleShowDistance
                )
            }
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimens.Spacing12),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AuraRose
            )
        )
    }
}

@Composable
fun BlockedUsersScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AuraTopBar(
                title = "Blocked Users",
                showBackButton = true,
                onBackClick = onNavigateBack
            )

            if (uiState.blockedUsers.isEmpty()) {
                EmptyState(
                    title = "No Blocked Users",
                    description = "You haven't blocked anyone yet.",
                    icon = Icons.Default.Block
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimens.Spacing24)
                ) {
                    items(uiState.blockedUsers, key = { it.id }) { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Dimens.Spacing12),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Avatar(
                                    imageUrl = user.photoUrl,
                                    name = user.displayName,
                                    size = 48.dp
                                )
                                Spacer(modifier = Modifier.width(Dimens.Spacing16))
                                Text(
                                    text = user.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White
                                )
                            }

                            SecondaryButton(
                                text = "Unblock",
                                onClick = { viewModel.unblockUser(user.blockedUserId) },
                                modifier = Modifier.width(100.dp)
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}

@Composable
fun AccountSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWelcome: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            if (event is SettingsEvent.NavigateToWelcome) {
                onNavigateToWelcome()
            }
        }
    }

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
                title = "Account",
                showBackButton = true,
                onBackClick = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.Spacing24, vertical = Dimens.Spacing16)
            ) {
                Text(
                    text = "Account Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing16))

                Text(
                    text = "Deleting your account will permanently remove your photos, conversations, matches, and profile from Aura. This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing32))

                SecondaryButton(
                    text = "Delete Account",
                    onClick = { showDeleteDialog = true },
                    textColor = PassColor,
                    borderColor = PassColor
                )
            }
        }

        if (showDeleteDialog) {
            ConfirmationDialog(
                title = "Permanently Delete Account?",
                message = "Are you absolutely sure? All your matches, conversations, and profile details will be permanently removed.",
                confirmText = "Delete Permanently",
                isDestructive = true,
                onConfirm = {
                    showDeleteDialog = false
                    viewModel.deleteAccount()
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
    }
}
