package com.example.klarity.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.klarity.presentation.screen.editor.EditorScreen
import com.example.klarity.presentation.screen.home.HomeScreen
import com.example.klarity.presentation.theme.KlarityMotion

/**
 * Main navigation host for the app with Material 3 motion tokens.
 *
 * Features:
 * - Type-safe navigation with sealed Screen classes
 * - M3 motion curves for smooth transitions
 * - Lifecycle-aware navigation state
 * - Predictive back gesture support
 */
@Composable
fun KlarityNavigation(
    navController: NavHostController = rememberNavController()
) {
    // Observe current navigation state for lifecycle awareness
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavHost(
        navController = navController,
        startDestination = Screen.Home,
        // Material 3 motion - Emphasized motion for navigation transitions
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = KlarityMotion.emphasizedEnter()
            ) + fadeIn(animationSpec = KlarityMotion.emphasizedEnter())
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = KlarityMotion.emphasizedExit()
            ) + fadeOut(animationSpec = KlarityMotion.emphasizedExit())
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = KlarityMotion.emphasizedEnter()
            ) + fadeIn(animationSpec = KlarityMotion.emphasizedEnter())
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = KlarityMotion.emphasizedExit()
            ) + fadeOut(animationSpec = KlarityMotion.emphasizedExit())
        }
    ) {
        composable<Screen.Home> {
            // All editing happens in the main screen - no navigation needed
            HomeScreen()
        }

        composable<Screen.Editor> { backStackEntry ->
            val editor: Screen.Editor = backStackEntry.toRoute()
            EditorScreen(
                noteId = editor.noteId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // TODO: Implement Settings screen
        // composable<Screen.Settings> {
        //     SettingsScreen(
        //         onBack = { navController.popBackStack() }
        //     )
        // }
    }
}
