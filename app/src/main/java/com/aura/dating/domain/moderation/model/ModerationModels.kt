package com.aura.dating.domain.moderation.model

import kotlinx.serialization.Serializable

enum class ReportReason(val displayName: String) {
    SPAM("Spam or Advertising"),
    FAKE_PROFILE("Fake Profile or Impersonation"),
    HARASSMENT("Harassment or Bullying"),
    INAPPROPRIATE_CONTENT("Inappropriate Photos or Bio"),
    SCAM("Scam or Financial Fraud"),
    OTHER("Other Concern")
}

@Serializable
data class ReportRequest(
    val reportedUserId: String,
    val reason: ReportReason,
    val details: String? = null
)

@Serializable
data class BlockedUser(
    val id: String,
    val blockedUserId: String,
    val displayName: String,
    val photoUrl: String?,
    val blockedAtMillis: Long = System.currentTimeMillis()
)
