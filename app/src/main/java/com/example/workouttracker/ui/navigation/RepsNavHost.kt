package com.example.workouttracker.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

/**
 * Zentraler Navigation Host für die REPS App.
 * 
 * Wird schrittweise mit echten Screens befüllt:
 * - Phase 2: Settings
 * - Phase 3: History
 * - Phase 4: Main
 * - Phase 5: WorkoutInput
 * - Phase 6: Timer
 */
@Composable
fun RepsNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.MAIN
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Phase 4: Main/Dashboard Screen
        composable(Routes.MAIN) {
            // TODO: MainScreen implementieren
            PlaceholderScreen(
                title = "REPS",
                onNavigateBack = { /* Main hat kein Back */ }
            )
        }
        
        // Phase 2: Settings Screen
        composable(Routes.SETTINGS) {
            // TODO: SettingsScreen implementieren
            PlaceholderScreen(
                title = "Einstellungen",
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Phase 3: History Screen
        composable(Routes.HISTORY) {
            // TODO: HistoryScreen implementieren
            PlaceholderScreen(
                title = "Verlauf",
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Phase 4: Main/Dashboard Screen
        composable(Routes.MAIN) {
            // TODO: MainScreen implementieren
            PlaceholderScreen(
                title = "REPS",
                onNavigateBack = { /* Main hat kein Back */ }
            )
        }
        
        // Phase 5: WorkoutInput Screen
        composable(Routes.WORKOUT_INPUT) {
            // TODO: WorkoutInputScreen implementieren
            PlaceholderScreen(
                title = "Neues Workout",
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Phase 6: Timer Screen mit Argumenten
        composable(
            route = Routes.TIMER,
            arguments = listOf(
                navArgument("exerciseId") { type = NavType.LongType },
                navArgument("sets") { type = NavType.IntType },
                navArgument("isTimeBased") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getLong("exerciseId") ?: 0L
            val sets = backStackEntry.arguments?.getInt("sets") ?: 3
            val isTimeBased = backStackEntry.arguments?.getBoolean("isTimeBased") ?: false
            
            // TODO: TimerScreen implementieren
            PlaceholderScreen(
                title = "Timer (Übung: $exerciseId, Sets: $sets, Zeit: $isTimeBased)",
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Temporärer Platzhalter-Screen für noch nicht implementierte Screens.
 */
@Composable
private fun PlaceholderScreen(
    title: String,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "TODO: $title implementieren")
    }
}
