package com.aura.dating.domain.notifications.repository

import com.aura.dating.core.common.result.Result
import com.aura.dating.domain.notifications.model.NotificationItem

interface NotificationRepository {
    suspend fun syncDeviceToken(token: String): Result<Unit>
    suspend fun getNotifications(): Result<List<NotificationItem>>
    suspend fun markAsRead(notificationId: String): Result<Unit>
}
