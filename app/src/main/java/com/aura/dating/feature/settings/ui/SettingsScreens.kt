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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aura.dating.R
import com.aura.dating.core.designsystem.components.AuraTopBar
import com.aura.dating.core.designsystem.components.Avatar
import com.aura.dating.core.designsystem.components.ConfirmationDialog
import com.aura.dating.core.designsystem.components.EmptyState
import com.aura.dating.core.designsystem.components.SecondaryButton
import com.aura.dating.core.designsystem.theme.AuraRose
import com.aura.dating.core.localization.AppLanguage
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
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showLanguagePicker by remember { mutableStateOf(false) }

    val currentLanguage = AppLanguage.fromCode(uiState.selectedLanguageCode)

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
                title = stringResource(R.string.settings),
                showBackButton = true,
                onBackClick = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.Spacing20)
            ) {
                SettingsNavigationItem(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.app_language),
                    subtitle = "${currentLanguage.flagEmoji} ${currentLanguage.nativeName}",
                    onClick = { showLanguagePicker = true }
                )

                SettingsNavigationItem(
                    icon = Icons.Default.Notifications,
                    title = stringResource(R.string.push_notifications),
                    subtitle = stringResource(R.string.notifications_subtitle),
                    onClick = onNavigateToNotificationSettings
                )

                SettingsNavigationItem(
                    icon = Icons.Default.Lock,
                    title = stringResource(R.string.privacy),
                    subtitle = stringResource(R.string.privacy_subtitle),
                    onClick = onNavigateToPrivacy
                )

                SettingsNavigationItem(
                    icon = Icons.Default.Block,
                    title = stringResource(R.string.blocked_users),
                    subtitle = stringResource(R.string.blocked_users_subtitle),
                    onClick = onNavigateToBlockedUsers
                )

                SettingsNavigationItem(
                    icon = Icons.Default.Person,
                    title = stringResource(R.string.account),
                    subtitle = stringResource(R.string.account_subtitle),
                    onClick = onNavigateToAccount
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing32))

                SecondaryButton(
                    text = stringResource(R.string.logout),
                    onClick = { showLogoutDialog = true },
                    textColor = Color.White
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing24))

                Text(
                    text = stringResource(R.string.app_version_info),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }

        if (showLanguagePicker) {
            LanguageSelectionBottomSheet(
                selectedCode = uiState.selectedLanguageCode,
                onLanguageSelected = { code ->
                    viewModel.setLanguage(code)
                },
                onDismiss = { showLanguagePicker = false }
            )
        }

        if (showLogoutDialog) {
            ConfirmationDialog(
                title = stringResource(R.string.logout),
                message = stringResource(R.string.logout_confirmation),
                confirmText = stringResource(R.string.logout),
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
                title = stringResource(R.string.notifications),
                showBackButton = true,
                onBackClick = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.Spacing24, vertical = Dimens.Spacing16)
            ) {
                SettingToggleRow(
                    title = stringResource(R.string.new_matches),
                    subtitle = stringResource(R.string.new_matches_notification_desc),
                    checked = uiState.newMatchesPush,
                    onCheckedChange = viewModel::toggleNewMatchesPush
                )

                SettingToggleRow(
                    title = stringResource(R.string.messages),
                    subtitle = stringResource(R.string.messages_notification_desc),
                    checked = uiState.messagesPush,
                    onCheckedChange = viewModel::toggleMessagesPush
                )

                SettingToggleRow(
                    title = stringResource(R.string.new_likes),
                    subtitle = stringResource(R.string.likes_notification_desc),
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
                title = stringResource(R.string.privacy_and_visibility),
                showBackButton = true,
                onBackClick = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.Spacing24, vertical = Dimens.Spacing16)
            ) {
                SettingToggleRow(
                    title = stringResource(R.string.show_online_status),
                    subtitle = stringResource(R.string.show_online_status_desc),
                    checked = uiState.showOnlineStatus,
                    onCheckedChange = viewModel::toggleShowOnline
                )

                SettingToggleRow(
                    title = stringResource(R.string.show_approximate_distance),
                    subtitle = stringResource(R.string.show_approximate_distance_desc),
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
                title = stringResource(R.string.blocked_users),
                showBackButton = true,
                onBackClick = onNavigateBack
            )

            if (uiState.blockedUsers.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.no_blocked_users),
                    description = stringResource(R.string.no_blocked_users_desc),
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
                                text = stringResource(R.string.unblock),
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is SettingsEvent.NavigateToWelcome -> onNavigateToWelcome()
                is SettingsEvent.ShowToast -> {
                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_LONG).show()
                }
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
                title = stringResource(R.string.account),
                showBackButton = true,
                onBackClick = onNavigateBack
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.Spacing24, vertical = Dimens.Spacing16)
            ) {
                if (uiState.userEmail.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.account_information),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(Dimens.Spacing12))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(Dimens.RadiusMedium),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.Spacing16, vertical = Dimens.Spacing16),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = stringResource(R.string.email),
                                tint = AuraRose,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(Dimens.Spacing16))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.email_address),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = uiState.userEmail,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimens.Spacing32))
                }

                Text(
                    text = stringResource(R.string.account_actions),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing16))

                Text(
                    text = stringResource(R.string.delete_account_explanation),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(Dimens.Spacing32))

                SecondaryButton(
                    text = if (uiState.isLoading) stringResource(R.string.deleting_account) else stringResource(R.string.delete_account),
                    onClick = { if (!uiState.isLoading) showDeleteDialog = true },
                    textColor = PassColor,
                    borderColor = PassColor
                )

                if (uiState.errorMessage != null) {
                    Spacer(modifier = Modifier.height(Dimens.Spacing16))
                    Text(
                        text = uiState.errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = PassColor
                    )
                }
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(color = AuraRose)
            }
        }

        if (showDeleteDialog) {
            ConfirmationDialog(
                title = stringResource(R.string.delete_account_dialog_title),
                message = stringResource(R.string.delete_account_dialog_desc),
                confirmText = stringResource(R.string.delete_permanently),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionBottomSheet(
    selectedCode: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = Dimens.RadiusLarge, topEnd = Dimens.RadiusLarge)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.Spacing24, vertical = Dimens.Spacing16)
                .padding(bottom = Dimens.Spacing32)
        ) {
            Text(
                text = stringResource(R.string.select_language),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(Dimens.Spacing16))

            AppLanguage.entries.forEach { lang ->
                val isSelected = (lang.code == selectedCode)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.RadiusMedium))
                        .clickable {
                            onLanguageSelected(lang.code)
                            onDismiss()
                        }
                        .padding(vertical = Dimens.Spacing12, horizontal = Dimens.Spacing8),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = lang.flagEmoji,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(Dimens.Spacing16))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = lang.nativeName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) AuraRose else Color.White
                        )
                        if (lang != AppLanguage.SYSTEM && lang.displayName != lang.nativeName) {
                            Text(
                                text = lang.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    RadioButton(
                        selected = isSelected,
                        onClick = {
                            onLanguageSelected(lang.code)
                            onDismiss()
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = AuraRose,
                            unselectedColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            }
        }
    }
}
