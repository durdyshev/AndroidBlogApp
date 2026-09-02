package com.aura.dating.core.navigation

sealed class Screen(val route: String) {
    // Onboarding & Auth
    data object Splash : Screen("splash")
    data object Welcome : Screen("welcome")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Verification : Screen("verification/{email}") {
        fun createRoute(email: String) = "verification/$email"
    }
    data object ForgotPassword : Screen("forgot_password")
    data object LocationPermission : Screen("location_permission")

    // Profile Setup
    data object CreateProfile : Screen("create_profile")
    data object AddPhotos : Screen("add_photos")
    data object SelectInterests : Screen("select_interests")
    data object DatingPreferences : Screen("dating_preferences")

    // Main App
    data object Main : Screen("main")
    data object UserProfileDetail : Screen("user_profile/{userId}") {
        fun createRoute(userId: String) = "user_profile/$userId"
    }
    data object Conversation : Screen("conversation/{conversationId}?matchName={matchName}&photoUrl={photoUrl}") {
        fun createRoute(conversationId: String, matchName: String = "Chat", photoUrl: String? = null): String {
            val encodedName = android.net.Uri.encode(matchName.ifBlank { "Chat" })
            val encodedPhoto = if (!photoUrl.isNullOrBlank()) android.net.Uri.encode(photoUrl) else ""
            return "conversation/$conversationId?matchName=$encodedName&photoUrl=$encodedPhoto"
        }
    }
    data object NearbyMap : Screen("nearby_map")
    data object Notifications : Screen("notifications")
    data object LocationSearch : Screen("location_search")
    data object SearchResults : Screen("search_results/{title}/{subtitle}") {
        fun createRoute(title: String, subtitle: String) =
            "search_results/${android.net.Uri.encode(title)}/${android.net.Uri.encode(subtitle)}"
    }

    // Profile Edit & Settings
    data object EditProfile : Screen("edit_profile")
    data object EditPhotos : Screen("edit_photos")
    data object EditInterests : Screen("edit_interests")
    data object Settings : Screen("settings")
    data object Privacy : Screen("privacy")
    data object NotificationSettings : Screen("notification_settings")
    data object BlockedUsers : Screen("blocked_users")
    data object AccountSettings : Screen("account_settings")
}
