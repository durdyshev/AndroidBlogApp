package com.aura.dating.domain.moderation.repository

import com.aura.dating.core.common.result.Result
import com.aura.dating.domain.moderation.model.BlockedUser
import com.aura.dating.domain.moderation.model.ReportRequest
import kotlinx.coroutines.flow.Flow

interface ModerationRepository {
    val blockedUsersFlow: Flow<List<BlockedUser>>
    suspend fun blockUser(blockedUserId: String, displayName: String, photoUrl: String?): Result<Unit>
    suspend fun unblockUser(blockedUserId: String): Result<Unit>
    suspend fun getBlockedUsers(forceRefresh: Boolean = false): Result<List<BlockedUser>>
    suspend fun reportUser(request: ReportRequest): Result<Unit>
    suspend fun deleteAccount(): Result<Unit>
}
