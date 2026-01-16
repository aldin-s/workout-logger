package com.example.workouttracker.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.workouttracker.R
import com.example.workouttracker.ui.components.StatCard
import com.example.workouttracker.ui.theme.RepsTheme
import java.text.NumberFormat
import java.util.Locale

/**
 * Stats Screen - Zeigt Workout-Statistiken an.
 * Erster Compose-Screen (Phase 1 der Migration).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.stats_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is StatsUiState.Loading -> LoadingContent(modifier = Modifier.padding(padding))
            is StatsUiState.Success -> StatsContent(state = state, modifier = Modifier.padding(padding))
            is StatsUiState.Error -> ErrorContent(message = state.message, modifier = Modifier.padding(padding))
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun StatsContent(
    state: StatsUiState.Success,
    modifier: Modifier = Modifier
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Workouts diese Woche
        item {
            StatCard(
                title = stringResource(R.string.stats_weekly_workouts),
                value = state.weeklyWorkouts.toString(),
                icon = Icons.Default.Refresh
            )
        }
        
        // Gesamtanzahl Workouts
        item {
            StatCard(
                title = stringResource(R.string.stats_total_workouts),
                value = state.totalWorkouts.toString(),
                icon = Icons.Default.DateRange
            )
        }
        
        // Gesamtgewicht
        item {
            StatCard(
                title = stringResource(R.string.stats_total_weight),
                value = "${numberFormat.format(state.totalWeight)} kg",
                icon = Icons.Default.Favorite
            )
        }
        
        // Gesamtanzahl Sets
        item {
            StatCard(
                title = stringResource(R.string.stats_total_sets),
                value = state.totalSets.toString(),
                icon = Icons.AutoMirrored.Filled.List
            )
        }
        
        // Häufigste Übung
        state.favoriteExercise?.let { exercise ->
            item {
                StatCard(
                    title = stringResource(R.string.stats_favorite_exercise),
                    value = exercise,
                    icon = Icons.Default.Star
                )
            }
        }
        
        // Letztes Workout
        state.lastWorkoutDate?.let { date ->
            item {
                StatCard(
                    title = stringResource(R.string.stats_last_workout),
                    value = date,
                    icon = Icons.Default.Check
                )
            }
        }
        
        // Spacer am Ende
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.stats_error),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, name = "Stats Screen - Success")
@Composable
private fun StatsScreenPreview() {
    RepsTheme(darkTheme = true, dynamicColor = false) {
        StatsContent(
            state = StatsUiState.Success(
                totalWorkouts = 42,
                weeklyWorkouts = 5,
                totalWeight = 12500.0,
                totalSets = 156,
                favoriteExercise = "Kreuzheben",
                lastWorkoutDate = "14.01.2026"
            )
        )
    }
}

@Preview(showBackground = true, name = "Stats Screen - Empty")
@Composable
private fun StatsScreenEmptyPreview() {
    RepsTheme(darkTheme = true, dynamicColor = false) {
        StatsContent(
            state = StatsUiState.Success(
                totalWorkouts = 0,
                weeklyWorkouts = 0,
                totalWeight = 0.0,
                totalSets = 0,
                favoriteExercise = null,
                lastWorkoutDate = null
            )
        )
    }
}
