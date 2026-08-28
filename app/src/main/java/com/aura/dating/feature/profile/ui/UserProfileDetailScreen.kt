package com.aura.dating.feature.profile.ui

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aura.dating.core.common.utils.DateTimeUtils
import com.aura.dating.core.common.utils.DistanceUtils
import com.aura.dating.core.designsystem.components.AuraTopBar
import com.aura.dating.core.designsystem.components.Avatar
import com.aura.dating.core.designsystem.components.ConfirmationDialog
import com.aura.dating.core.designsystem.components.ErrorState
import com.aura.dating.core.designsystem.components.InterestChip
import com.aura.dating.core.designsystem.theme.AuraBlue
import com.aura.dating.core.designsystem.theme.AuraRose
import com.aura.dating.core.designsystem.theme.DarkBackground
import com.aura.dating.core.designsystem.theme.Dimens
import com.aura.dating.core.designsystem.theme.PassColor
import com.aura.dating.feature.profile.viewmodel.ProfileEvent
import com.aura.dating.feature.profile.viewmodel.ProfileViewModel
import com.aura.dating.feature.settings.ui.ReportUserSheet

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserProfileDetailScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onNavigateToConversation: ((String, String, String?) -> Unit)? = null,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val candidate = uiState.selectedUserProfile

    var showMenu by remember { mutableStateOf(false) }
    var showBlockDialog by remember { mutableStateOf(false) }
    var showReportSheet by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.loadUserProfile(userId)
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            if (event is ProfileEvent.NavigateBack) {
                onNavigateBack()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        if (uiState.isLoading && candidate == null) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = AuraRose
            )
        } else if (candidate == null) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                AuraTopBar(
                    title = "Profile",
                    showBackButton = true,
                    onBackClick = onNavigateBack
                )
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorState(
                        message = uiState.errorMessage ?: "Could not load user profile",
                        onRetry = { viewModel.loadUserProfile(userId) }
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                val photoUrl = candidate.photos.firstOrNull()?.photoUrl
                if (!photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(photoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Avatar(
                            imageUrl = null,
                            name = candidate.displayName,
                            size = 120.dp
                        )
                    }
                }

                AuraTopBar(
                    title = "",
                    showBackButton = true,
                    onBackClick = onNavigateBack,
                    actions = {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Report Profile") },
                                onClick = {
                                    showMenu = false
                                    showReportSheet = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Block User", color = PassColor) },
                                onClick = {
                                    showMenu = false
                                    showBlockDialog = true
                                }
                            )
                        }
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.Spacing24)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${candidate?.displayName ?: "User"}, ${candidate?.let { DateTimeUtils.calculateAge(it.birthDateMillis) } ?: 25}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(Dimens.Spacing8))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = AuraBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.Spacing6))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(Dimens.Spacing4))
                    Text(
                        text = DistanceUtils.formatDistance(candidate?.distanceKm),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }

                if (!candidate?.bio.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(Dimens.Spacing20))
                    Text(
                        text = "About",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(Dimens.Spacing8))
                    Text(
                        text = candidate?.bio ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                if (!candidate?.interests.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(Dimens.Spacing20))
                    Text(
                        text = "Interests",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(Dimens.Spacing12))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing8),
                        verticalArrangement = Arrangement.spacedBy(Dimens.Spacing8)
                    ) {
                        candidate?.interests?.forEach { interest ->
                            InterestChip(
                                name = interest.name,
                                icon = interest.icon,
                                isSelected = true
                            )
                        }
                    }
                }

                if (onNavigateToConversation != null && candidate != null) {
                    Spacer(modifier = Modifier.height(Dimens.Spacing24))
                    Button(
                        onClick = {
                            onNavigateToConversation(
                                candidate.id,
                                candidate.displayName,
                                candidate.photos.firstOrNull()?.photoUrl
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(Dimens.RadiusPill),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AuraRose
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(Dimens.Spacing8))
                        Text(
                            text = "Send Message",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.Spacing32))
            }
        }
    }

        if (showBlockDialog) {
            ConfirmationDialog(
                title = "Block ${candidate?.displayName}?",
                message = "They will not be able to view your profile or contact you.",
                confirmText = "Block",
                isDestructive = true,
                onConfirm = {
                    showBlockDialog = false
                    candidate?.let { viewModel.blockUser(it.id, it.displayName, it.photos.firstOrNull()?.photoUrl) }
                },
                onDismiss = { showBlockDialog = false }
            )
        }

        if (showReportSheet) {
            ReportUserSheet(
                reportedUserName = candidate?.displayName ?: "User",
                onSubmitReport = { reason, details ->
                    candidate?.let { viewModel.reportUser(it.id, reason, details) }
                },
                onDismiss = { showReportSheet = false }
            )
        }
    }
}
