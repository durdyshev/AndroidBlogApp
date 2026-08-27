package com.aura.dating.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.aura.dating.core.designsystem.theme.AuraPrimaryGradient
import com.aura.dating.core.designsystem.theme.AuraRose
import com.aura.dating.core.designsystem.theme.Dimens

enum class AuraTab(val title: String, val icon: ImageVector) {
    DISCOVER("Discover", Icons.Default.AutoAwesome),
    MATCHES("Matches", Icons.Default.Favorite),
    CHATS("Chats", Icons.Default.ChatBubble),
    PROFILE("Profile", Icons.Default.Person)
}

@Composable
fun AuraBottomNavigation(
    selectedTab: AuraTab,
    onTabSelected: (AuraTab) -> Unit,
    modifier: Modifier = Modifier,
    unreadMessagesCount: Int = 0,
    newMatchesCount: Int = 0
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Dimens.Spacing20, vertical = Dimens.Spacing12)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(Dimens.RadiusExtraLarge),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            ),
        shape = RoundedCornerShape(Dimens.RadiusExtraLarge),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.BottomBarHeight)
                .padding(horizontal = Dimens.Spacing12),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AuraTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1f,
                    label = "TabScale"
                )
                val iconColor by animateColorAsState(
                    targetValue = if (isSelected) AuraRose else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    label = "TabIconColor"
                )

                val interactionSource = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .scale(scale)
                        .clip(RoundedCornerShape(Dimens.RadiusMedium))
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onTabSelected(tab) }
                        )
                        .padding(horizontal = Dimens.Spacing12, vertical = Dimens.Spacing8),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val badgeCount = when (tab) {
                            AuraTab.MATCHES -> newMatchesCount
                            AuraTab.CHATS -> unreadMessagesCount
                            else -> 0
                        }

                        if (badgeCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = AuraRose,
                                        contentColor = Color.White
                                    ) {
                                        Text(text = if (badgeCount > 99) "99+" else badgeCount.toString())
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    tint = iconColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = iconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .padding(top = Dimens.Spacing4)
                                    .size(width = 16.dp, height = 3.dp)
                                    .clip(CircleShape)
                                    .background(AuraPrimaryGradient)
                            )
                        }
                    }
                }
            }
        }
    }
}
