package com.Lia.assistant.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder

fun registerFlavorRoutes(navGraphBuilder: NavGraphBuilder, navController: NavController) {
    // No extra routes for Google Play build
}

@Composable
fun AccessibilitySettingsRow(navController: NavController) {
    // No accessibility row in Google Play build
}
