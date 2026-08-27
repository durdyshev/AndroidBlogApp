package com.aura.dating.data.notifications.repository

import com.aura.dating.core.common.result.AppError
import com.aura.dating.core.common.result.Result
import com.aura.dating.core.security.TokenStorage
import com.aura.dating.data.notifications.remote.NotificationRemoteDataSource
import com.aura.dating.domain.notifications.model.NotificationItem
import com.aura.dating.domain.notifications.repository.NotificationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val remoteDataSource: NotificationRemoteDataSource,
    private val tokenStorage: TokenStorage
) : NotificationRepository {

    override suspend fun syncDeviceToken(token: String): Result<Unit> {
        val userId = tokenStorage.getUserId()
            ?: return Result.Error(AppError.Unauthorized())

        return remoteDataSource.registerDeviceToken(userId, token)
    }

    override suspend fun getNotifications(): Result<List<NotificationItem>> {
        val userId = tokenStorage.getUserId()
            ?: return Result.Error(AppError.Unauthorized())

        return remoteDataSource.getNotifications(userId)
    }

    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        return remoteDataSource.markAsRead(notificationId)
    }
}
