package com.aura.dating.domain.notifications.model

import com.aura.dating.core.notifications.NotificationType
import kotlinx.serialization.Serializable

@Serializable
data class NotificationItem(
    val id: String,
    val userId: String,
    val actorId: String? = null,
    val type: NotificationType,
    val title: String,
    val body: String,
    val isRead: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)
