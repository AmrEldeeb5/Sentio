package com.example.klarity.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.klarity.presentation.screen.editor.EditorScreen
import com.example.klarity.presentation.screen.home.HomeScreen

/**
 * Main navigation host for the app.
 */
@Composable
fun KlarityNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home
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
    }
}
