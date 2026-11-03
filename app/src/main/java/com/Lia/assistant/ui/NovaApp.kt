package com.Lia.assistant.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.forge.ForgeController
import com.Lia.assistant.ui.components.LiaBottomNavigation
import com.Lia.assistant.ui.screens.about.AboutScreen
import com.Lia.assistant.ui.screens.chat.ChatScreen
import com.Lia.assistant.ui.screens.debug.DebugScreen
import com.Lia.assistant.ui.screens.forge.ForgeScreen
import com.Lia.assistant.ui.screens.history.HistoryScreen
import com.Lia.assistant.ui.screens.home.HomeScreen
import com.Lia.assistant.ui.screens.onboarding.OnboardingScreen
import com.Lia.assistant.ui.screens.orb.OrbStyleScreen
import com.Lia.assistant.ui.screens.permissions.PermissionsScreen
import com.Lia.assistant.ui.screens.personality.PersonalityScreen
import com.Lia.assistant.ui.screens.privacy.PrivacyScreen
import com.Lia.assistant.ui.screens.profile.ProfileScreen
import com.Lia.assistant.ui.screens.quickactions.QuickActionsScreen
import com.Lia.assistant.ui.screens.settings.SettingsScreen
import com.Lia.assistant.ui.screens.voice.VoiceScreen
import com.Lia.assistant.ui.theme.LocalBottomBarInset
import com.Lia.assistant.ui.theme.nightSky

@Composable
fun NovaApp(appState: NovaAppState) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    val pendingForgeOpen by ForgeController.pendingOpen.collectAsState()

    LaunchedEffect(pendingForgeOpen) {
        if (pendingForgeOpen) {
            ForgeController.consumePendingOpen()
            navController.navigate("forge") {
                launchSingleTop = true
            }
        }
    }

    val bottomBarRoutes = listOf("home", "chat", "history", "settings")
    val showBottomBar = currentRoute in bottomBarRoutes

    CompositionLocalProvider(
        LocalBottomBarInset provides PaddingValues(bottom = if (showBottomBar) 90.dp else 0.dp)
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    LiaBottomNavigation(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onVoiceClick = {
                            navController.navigate("voice") {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .nightSky()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = if (appState.hasOnboarded) "home" else "onboarding",
                    enterTransition = { fadeIn() },
                    exitTransition = { fadeOut() }
                ) {
                    composable("onboarding") {
                        OnboardingScreen(
                            appState = appState,
                            onComplete = {
                                navController.navigate("home") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("home") {
                        HomeScreen(
                            appState = appState,
                            onOpenVoice = { navController.navigate("voice") },
                            onOpenChat = { navController.navigate("chat") },
                            onOpenProfile = { navController.navigate("profile") },
                            onOpenQuickActions = { navController.navigate("quick_actions") },
                            onOpenForgePrompt = { prompt ->
                                ForgeController.start(appState.let { navController.context }, prompt)
                            }
                        )
                    }

                    composable("chat") {
                        ChatScreen(
                            appState = appState,
                            onBack = { navController.popBackStack() },
                            onOpenForge = { _ ->
                                navController.navigate("forge")
                            }
                        )
                    }

                    composable("voice") {
                        VoiceScreen(
                            appState = appState,
                            onClose = { navController.popBackStack() },
                            onOpenSettings = { navController.navigate("settings") }
                        )
                    }

                    composable("history") {
                        HistoryScreen(
                            appState = appState,
                            onOpenChat = { navController.navigate("chat") }
                        )
                    }

                    composable("settings") {
                        SettingsScreen(
                            appState = appState,
                            navController = navController
                        )
                    }

                    composable("quick_actions") {
                        QuickActionsScreen(
                            onBack = { navController.popBackStack() },
                            onOpenForge = { navController.navigate("forge") }
                        )
                    }

                    composable("profile") {
                        ProfileScreen(
                            appState = appState,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("personality") {
                        PersonalityScreen(
                            appState = appState,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("orb_style") {
                        OrbStyleScreen(
                            appState = appState,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("permissions") {
                        PermissionsScreen(onBack = { navController.popBackStack() })
                    }

                    composable("privacy") {
                        PrivacyScreen(onBack = { navController.popBackStack() })
                    }

                    composable("about") {
                        AboutScreen(onBack = { navController.popBackStack() })
                    }

                    composable("debug") {
                        DebugScreen(
                            appState = appState,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("forge") {
                        ForgeScreen(onBack = { navController.popBackStack() })
                    }

                    // Flavor-specific routes (e.g. accessibility disclosure on direct)
                    registerFlavorRoutes(this, navController)
                }
            }
        }
    }
}
