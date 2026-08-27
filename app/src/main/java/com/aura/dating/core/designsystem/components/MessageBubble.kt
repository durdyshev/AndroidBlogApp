package com.aura.dating.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aura.dating.core.common.utils.DateTimeUtils
import com.aura.dating.core.designsystem.theme.AuraBlue
import com.aura.dating.core.designsystem.theme.AuraPrimaryGradient
import com.aura.dating.core.designsystem.theme.Dimens

enum class MessageBubbleType {
    TEXT, IMAGE, SYSTEM
}

enum class MessageDeliveryStatus {
    SENDING, SENT, DELIVERED, READ, FAILED
}

@Composable
fun MessageBubble(
    content: String,
    timestampMillis: Long,
    isFromMe: Boolean,
    messageType: MessageBubbleType = MessageBubbleType.TEXT,
    mediaUrl: String? = null,
    status: MessageDeliveryStatus = MessageDeliveryStatus.SENT,
    onImageClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (messageType == MessageBubbleType.SYSTEM) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = Dimens.Spacing8),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(Dimens.RadiusPill))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(horizontal = Dimens.Spacing16, vertical = Dimens.Spacing6)
            ) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    val bubbleShape = if (isFromMe) {
        RoundedCornerShape(
            topStart = Dimens.RadiusLarge,
            topEnd = Dimens.RadiusLarge,
            bottomStart = Dimens.RadiusLarge,
            bottomEnd = Dimens.RadiusSmall
        )
    } else {
        RoundedCornerShape(
            topStart = Dimens.RadiusLarge,
            topEnd = Dimens.RadiusLarge,
            bottomStart = Dimens.RadiusSmall,
            bottomEnd = Dimens.RadiusLarge
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = if (isFromMe) Dimens.Spacing48 else Dimens.Spacing8,
                end = if (isFromMe) Dimens.Spacing8 else Dimens.Spacing48,
                top = Dimens.Spacing4,
                bottom = Dimens.Spacing4
            ),
        horizontalArrangement = if (isFromMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(bubbleShape)
                .then(
                    if (isFromMe) {
                        Modifier.background(AuraPrimaryGradient)
                    } else {
                        Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                    }
                )
                .padding(Dimens.Spacing12)
        ) {
            Column {
                if (messageType == MessageBubbleType.IMAGE && !mediaUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(mediaUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Shared image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp, max = 220.dp)
                            .clip(RoundedCornerShape(Dimens.RadiusMedium))
                            .then(
                                if (onImageClick != null) {
                                    Modifier.clickable { onImageClick(mediaUrl) }
                                } else Modifier
                            )
                    )
                    if (content.isNotBlank()) {
                        Spacer(modifier = Modifier.height(Dimens.Spacing8))
                    }
                }

                if (content.isNotBlank()) {
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isFromMe) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.Spacing4))

                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = DateTimeUtils.formatMessageTime(timestampMillis),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = if (isFromMe) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )

                    if (isFromMe) {
                        Spacer(modifier = Modifier.width(Dimens.Spacing4))
                        when (status) {
                            MessageDeliveryStatus.SENDING -> {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(Color.White.copy(alpha = 0.5f))
                                )
                            }
                            MessageDeliveryStatus.SENT -> {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Sent",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            MessageDeliveryStatus.DELIVERED -> {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Delivered",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            MessageDeliveryStatus.READ -> {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = "Read",
                                    tint = AuraBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            MessageDeliveryStatus.FAILED -> {
                                Text(
                                    text = "!",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
