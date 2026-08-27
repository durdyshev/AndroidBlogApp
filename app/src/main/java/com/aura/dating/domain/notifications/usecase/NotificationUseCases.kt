package com.aura.dating.domain.notifications.usecase

import com.aura.dating.core.common.result.Result
import com.aura.dating.domain.notifications.model.NotificationItem
import com.aura.dating.domain.notifications.repository.NotificationRepository
import javax.inject.Inject

class SyncDeviceTokenUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(token: String): Result<Unit> {
        if (token.isBlank()) return Result.Success(Unit)
        return notificationRepository.syncDeviceToken(token)
    }
}

class GetNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(): Result<List<NotificationItem>> {
        return notificationRepository.getNotifications()
    }
}
