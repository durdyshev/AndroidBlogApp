package com.aura.dating

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.aura.dating.core.designsystem.theme.AuraTheme
import com.aura.dating.core.designsystem.theme.DarkBackground
import com.aura.dating.core.navigation.AuraNavHost
import com.aura.dating.core.navigation.Screen
import com.aura.dating.core.notifications.AuraNotificationService
import com.aura.dating.core.notifications.GlobalNotificationManager
import com.aura.dating.core.notifications.NotificationType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var globalNotificationManager: GlobalNotificationManager

    private val pendingRoute = MutableStateFlow<String?>(null)

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        parseNotificationIntent(intent)

        requestNotificationPermission()
        globalNotificationManager.startListening()
        AuraNotificationService.start(this)

        setContent {
            AuraTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    val navController = rememberNavController()
                    val targetRoute by pendingRoute.collectAsState()

                    LaunchedEffect(targetRoute) {
                        targetRoute?.let { route ->
                            try {
                                val currentRoute = navController.currentBackStackEntry?.destination?.route
                                Log.d("MainActivity", "Pending route: $route, currentRoute: $currentRoute")
                                if (currentRoute != null && currentRoute != Screen.Splash.route) {
                                    navController.navigate(route)
                                    pendingRoute.value = null
                                }
                            } catch (e: Exception) {
                                Log.e("MainActivity", "Navigation error: ${e.message}")
                                pendingRoute.value = null
                            }
                        }
                    }

                    AuraNavHost(
                        navController = navController,
                        onMainReady = {
                            pendingRoute.value?.let { route ->
                                try {
                                    Log.d("MainActivity", "Navigating to pending route from onMainReady: $route")
                                    navController.navigate(route)
                                } catch (e: Exception) {
                                    Log.e("MainActivity", "onMainReady navigation error: ${e.message}")
                                } finally {
                                    pendingRoute.value = null
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        parseNotificationIntent(intent)
    }

    private fun parseNotificationIntent(intent: Intent?) {
        if (intent == null) return
        val notifType = intent.getStringExtra("notification_type") ?: return

        when (notifType) {
            NotificationType.NEW_MESSAGE.name -> {
                val conversationId = intent.getStringExtra("conversation_id")
                    ?: intent.getStringExtra("sender_id")
                val senderName = intent.getStringExtra("sender_name")
                    ?: intent.getStringExtra("title")
                    ?: "Chat"
                val photoUrl = intent.getStringExtra("photo_url")

                if (!conversationId.isNullOrBlank()) {
                    val route = Screen.Conversation.createRoute(
                        conversationId = conversationId,
                        matchName = senderName,
                        photoUrl = photoUrl
                    )
                    pendingRoute.value = route
                }
            }
            NotificationType.NEW_MATCH.name,
            NotificationType.NEW_LIKE.name,
            NotificationType.SUPER_LIKE.name -> {
                val actorId = intent.getStringExtra("actor_id")
                if (!actorId.isNullOrBlank()) {
                    pendingRoute.value = Screen.UserProfileDetail.createRoute(actorId)
                } else {
                    pendingRoute.value = Screen.Notifications.route
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
