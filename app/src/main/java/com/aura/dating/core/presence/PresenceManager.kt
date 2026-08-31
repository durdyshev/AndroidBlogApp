package com.aura.dating.core.presence

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.aura.dating.core.security.TokenStorage
import com.aura.dating.domain.profile.repository.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresenceManager @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val tokenStorage: TokenStorage
) : Application.ActivityLifecycleCallbacks {

    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var heartbeatJob: Job? = null
    private var activeActivityCount = 0

    fun initialize(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityStarted(activity: Activity) {
        activeActivityCount++
        if (activeActivityCount == 1) {
            setOnlineStatus(true)
            startHeartbeat()
        }
    }

    override fun onActivityStopped(activity: Activity) {
        activeActivityCount = (activeActivityCount - 1).coerceAtLeast(0)
        if (activeActivityCount == 0) {
            stopHeartbeat()
            setOnlineStatus(false)
        }
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (isActive) {
                delay(45000)
                if (activeActivityCount > 0) {
                    setOnlineStatus(true)
                }
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    fun setOnlineStatus(isOnline: Boolean) {
        scope.launch {
            try {
                val userId = tokenStorage.getUserId()
                if (!userId.isNullOrBlank()) {
                    profileRepository.updateOnlineStatus(isOnline)
                    Log.d("PresenceManager", "Presence updated: isOnline=$isOnline for user=$userId")
                }
            } catch (e: Exception) {
                Log.e("PresenceManager", "Failed to update presence: ${e.message}")
            }
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
