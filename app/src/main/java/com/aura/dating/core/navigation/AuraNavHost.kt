package com.aura.dating.core.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aura.dating.feature.auth.ui.ForgotPasswordScreen
import com.aura.dating.feature.auth.ui.LoginScreen
import com.aura.dating.feature.auth.ui.RegisterScreen
import com.aura.dating.feature.auth.ui.VerificationScreen
import com.aura.dating.feature.chat.ui.ConversationScreen
import com.aura.dating.feature.discover.ui.NearbyMapScreen
import com.aura.dating.feature.home.ui.MainScreen
import com.aura.dating.feature.notifications.ui.NotificationsScreen
import com.aura.dating.feature.onboarding.ui.LocationPermissionScreen
import com.aura.dating.feature.onboarding.ui.SplashScreen
import com.aura.dating.feature.onboarding.ui.WelcomeScreen
import com.aura.dating.feature.profile.ui.EditInterestsScreen
import com.aura.dating.feature.profile.ui.EditPhotosScreen
import com.aura.dating.feature.profile.ui.EditProfileScreen
import com.aura.dating.feature.profile.ui.UserProfileDetailScreen
import com.aura.dating.feature.profile.viewmodel.ProfileViewModel
import com.aura.dating.feature.profile_creation.ui.AddPhotosScreen
import com.aura.dating.feature.profile_creation.ui.CreateProfileScreen
import com.aura.dating.feature.profile_creation.ui.DatingPreferencesScreen
import com.aura.dating.feature.profile_creation.ui.SelectInterestsScreen
import com.aura.dating.feature.profile_creation.viewmodel.CreateProfileViewModel
import com.aura.dating.feature.settings.ui.AccountSettingsScreen
import com.aura.dating.feature.settings.ui.BlockedUsersScreen
import com.aura.dating.feature.settings.ui.NotificationSettingsScreen
import com.aura.dating.feature.settings.ui.PrivacyScreen
import com.aura.dating.feature.settings.ui.SettingsScreen

@Composable
fun AuraNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier,
        enterTransition = { slideInHorizontally(initialOffsetX = { 400 }) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -400 }) + fadeOut() },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -400 }) + fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { 400 }) + fadeOut() }
    ) {
        // Splash & Onboarding
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToWelcome = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLocationPermission = {
                    navController.navigate(Screen.LocationPermission.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToCreateProfile = {
                    navController.navigate(Screen.CreateProfile.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.LocationPermission.route) {
            LocationPermissionScreen(
                onPermissionGranted = {
                    navController.navigate(Screen.CreateProfile.route) {
                        popUpTo(Screen.LocationPermission.route) { inclusive = true }
                    }
                }
            )
        }

        // Auth
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreateProfile = {
                    navController.navigate(Screen.CreateProfile.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onNavigateToVerification = { email ->
                    navController.navigate(Screen.Verification.createRoute(email))
                },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) }
            )
        }

        composable(
            route = Screen.Verification.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            VerificationScreen(
                email = email,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreateProfile = {
                    navController.navigate(Screen.CreateProfile.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(onNavigateBack = { navController.popBackStack() })
        }

        // Profile Setup Flow (Shared ViewModel in navigation graph)
        composable(Screen.CreateProfile.route) { backStackEntry ->
            val viewModel: CreateProfileViewModel = hiltViewModel(backStackEntry)
            CreateProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddPhotos = { navController.navigate(Screen.AddPhotos.route) },
                viewModel = viewModel
            )
        }

        composable(Screen.AddPhotos.route) { backStackEntry ->
            val parentEntry = androidx.compose.runtime.remember(backStackEntry) {
                try { navController.getBackStackEntry(Screen.CreateProfile.route) } catch (_: Exception) { backStackEntry }
            }
            val viewModel: CreateProfileViewModel = hiltViewModel(parentEntry)
            AddPhotosScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSelectInterests = { navController.navigate(Screen.SelectInterests.route) },
                viewModel = viewModel
            )
        }

        composable(Screen.SelectInterests.route) { backStackEntry ->
            val parentEntry = androidx.compose.runtime.remember(backStackEntry) {
                try { navController.getBackStackEntry(Screen.CreateProfile.route) } catch (_: Exception) { backStackEntry }
            }
            val viewModel: CreateProfileViewModel = hiltViewModel(parentEntry)
            SelectInterestsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.CreateProfile.route) { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }

        composable(Screen.DatingPreferences.route) { backStackEntry ->
            val parentEntry = androidx.compose.runtime.remember(backStackEntry) {
                try { navController.getBackStackEntry(Screen.CreateProfile.route) } catch (_: Exception) { backStackEntry }
            }
            val viewModel: CreateProfileViewModel = hiltViewModel(parentEntry)
            DatingPreferencesScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.CreateProfile.route) { inclusive = true }
                    }
                },
                viewModel = viewModel
            )
        }

        // Main App Scaffold
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToUserProfile = { userId ->
                    navController.navigate(Screen.UserProfileDetail.createRoute(userId))
                },
                onNavigateToNearbyMap = { navController.navigate(Screen.NearbyMap.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToConversation = { convId, name, photoUrl ->
                    navController.navigate(Screen.Conversation.createRoute(convId, name, photoUrl))
                },
                onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onNavigateToEditPhotos = { navController.navigate(Screen.EditPhotos.route) },
                onNavigateToEditInterests = { navController.navigate(Screen.EditInterests.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        // Detail Screens
        composable(
            route = Screen.UserProfileDetail.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            UserProfileDetailScreen(
                userId = userId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Conversation.route,
            arguments = listOf(
                navArgument("conversationId") { type = NavType.StringType },
                navArgument("matchName") { type = NavType.StringType },
                navArgument("photoUrl") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) {
            ConversationScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUserProfile = { userId ->
                    navController.navigate(Screen.UserProfileDetail.createRoute(userId))
                }
            )
        }

        composable(Screen.NearbyMap.route) {
            NearbyMapScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUserProfile = { userId ->
                    navController.navigate(Screen.UserProfileDetail.createRoute(userId))
                }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(onNavigateBack = { navController.popBackStack() })
        }

        // Profile Edit Screens
        composable(Screen.EditProfile.route) {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            EditProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = profileViewModel
            )
        }

        composable(Screen.EditPhotos.route) {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            EditPhotosScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = profileViewModel
            )
        }

        composable(Screen.EditInterests.route) {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            EditInterestsScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = profileViewModel
            )
        }

        // Settings Suite
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPrivacy = { navController.navigate(Screen.Privacy.route) },
                onNavigateToNotificationSettings = { navController.navigate(Screen.NotificationSettings.route) },
                onNavigateToBlockedUsers = { navController.navigate(Screen.BlockedUsers.route) },
                onNavigateToAccount = { navController.navigate(Screen.AccountSettings.route) },
                onNavigateToWelcome = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Privacy.route) {
            PrivacyScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.NotificationSettings.route) {
            NotificationSettingsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.BlockedUsers.route) {
            BlockedUsersScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.AccountSettings.route) {
            AccountSettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToWelcome = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
