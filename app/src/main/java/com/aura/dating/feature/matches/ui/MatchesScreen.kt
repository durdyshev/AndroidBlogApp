package com.aura.dating.feature.matches.ui

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Badge
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aura.dating.core.common.utils.DateTimeUtils
import com.aura.dating.core.designsystem.components.AuraTopBar
import com.aura.dating.core.designsystem.components.Avatar
import com.aura.dating.core.designsystem.components.EmptyState
import com.aura.dating.core.designsystem.components.LoadingSkeletonItem
import com.aura.dating.core.designsystem.theme.AuraRose
import com.aura.dating.core.designsystem.theme.DarkBackground
import com.aura.dating.core.designsystem.theme.Dimens
import com.aura.dating.feature.matches.viewmodel.MatchesViewModel

@Composable
fun MatchesScreen(
    onNavigateToConversation: (conversationId: String, name: String, photoUrl: String?) -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    viewModel: MatchesViewModel = hiltViewModel()
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
                .padding(bottom = 80.dp)
        ) {
            AuraTopBar(
                title = "Matches & Messages",
                showBrandedLogo = false
            )

            if (uiState.isLoading && uiState.matches.isEmpty() && uiState.conversations.isEmpty()) {
                Column(modifier = Modifier.padding(horizontal = Dimens.Spacing24)) {
                    repeat(5) { LoadingSkeletonItem() }
                }
            } else if (uiState.matches.isEmpty() && uiState.conversations.isEmpty()) {
                EmptyState(
                    title = "No Matches Yet",
                    description = "When you and someone else both like each other, they'll appear here. Keep discovering!",
                    icon = Icons.Default.Favorite
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // New Matches Horizontal Bar
                    if (uiState.matches.isNotEmpty()) {
                        item {
                            Column(modifier = Modifier.padding(vertical = Dimens.Spacing8)) {
                                Text(
                                    text = "New Matches (${uiState.matches.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = Dimens.Spacing24)
                                )
                                Spacer(modifier = Modifier.height(Dimens.Spacing12))
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = Dimens.Spacing20),
                                    horizontalArrangement = Arrangement.spacedBy(Dimens.Spacing16)
                                ) {
                                    items(uiState.matches, key = { it.id }) { match ->
                                        val existingConv = uiState.conversations.find { it.matchId == match.id || it.participantUserId == match.matchedUserId }
                                        val convId = existingConv?.id ?: match.id
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .clickable {
                                                    onNavigateToConversation(
                                                        convId,
                                                        match.matchedUserName,
                                                        match.matchedUserPhotoUrl
                                                    )
                                                }
                                                .padding(vertical = Dimens.Spacing4)
                                        ) {
                                            Avatar(
                                                imageUrl = match.matchedUserPhotoUrl,
                                                name = match.matchedUserName,
                                                size = 64.dp,
                                                showBorderGradient = true
                                            )
                                            Spacer(modifier = Modifier.height(Dimens.Spacing6))
                                            Text(
                                                text = match.matchedUserName,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.White,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(Dimens.Spacing12))
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    modifier = Modifier.padding(horizontal = Dimens.Spacing24)
                                )
                            }
                        }
                    }

                    // Conversations Header
                    item {
                        Text(
                            text = "Conversations",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = Dimens.Spacing24, vertical = Dimens.Spacing12)
                        )
                    }

                    // Conversations List
                    if (uiState.conversations.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimens.Spacing32),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Tap on a match above to start chatting!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        items(uiState.conversations, key = { it.id }) { conversation ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onNavigateToConversation(
                                            conversation.id,
                                            conversation.participantName,
                                            conversation.participantPhotoUrl
                                        )
                                    }
                                    .padding(horizontal = Dimens.Spacing24, vertical = Dimens.Spacing12),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Avatar(
                                    imageUrl = conversation.participantPhotoUrl,
                                    name = conversation.participantName,
                                    size = 56.dp,
                                    isOnline = conversation.isParticipantOnline
                                )

                                Spacer(modifier = Modifier.width(Dimens.Spacing16))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = conversation.participantName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = DateTimeUtils.formatRelativeTime(conversation.lastMessageAtMillis),
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = Color.White.copy(alpha = 0.6f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(Dimens.Spacing4))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = conversation.lastMessageText ?: "Say hi to your new match!",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (conversation.unreadCount > 0) Color.White else Color.White.copy(alpha = 0.6f),
                                            fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )

                                        if (conversation.unreadCount > 0) {
                                            Badge(
                                                containerColor = AuraRose,
                                                contentColor = Color.White,
                                                modifier = Modifier.padding(start = Dimens.Spacing8)
                                            ) {
                                                Text(text = conversation.unreadCount.toString())
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
