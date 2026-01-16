package com.example.workouttracker.ui.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.R
import com.example.workouttracker.ui.theme.RepsTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    historyItems: List<HistoryItem>,
    onNavigateBack: () -> Unit,
    onSessionLongClick: (WorkoutSession) -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.workout_history),
                        style = MaterialTheme.typography.titleMedium,
                        letterSpacing = 1.sp
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (historyItems.isEmpty()) {
            EmptyHistoryState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            HistoryList(
                items = historyItems,
                onSessionLongClick = onSessionLongClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

@Composable
private fun EmptyHistoryState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📋",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.empty_history_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.empty_history_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryList(
    items: List<HistoryItem>,
    onSessionLongClick: (WorkoutSession) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = items,
            key = { item ->
                when (item) {
                    is HistoryItem.DateHeader -> "header_${item.dateLabel}"
                    is HistoryItem.WorkoutItem -> "workout_${item.session.startTime.time}"
                }
            }
        ) { item ->
            when (item) {
                is HistoryItem.DateHeader -> DateHeaderItem(dateLabel = item.dateLabel)
                is HistoryItem.WorkoutItem -> WorkoutHistoryItem(
                    session = item.session,
                    onLongClick = { onSessionLongClick(item.session) }
                )
            }
        }
        
        // Bottom spacing
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun DateHeaderItem(dateLabel: String) {
    Text(
        text = dateLabel,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WorkoutHistoryItem(
    session: WorkoutSession,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Exercise name
            Text(
                text = session.exerciseName.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Workout details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatWorkoutDetails(session),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = formatTimeRange(session.startTime, session.endTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatWorkoutDetails(session: WorkoutSession): String {
    return String.format(
        Locale.getDefault(),
        "%.1f kg × %d Wdh · %d Sätze",
        session.weight,
        session.reps,
        session.totalSets
    )
}

private fun formatTimeRange(startTime: Date, endTime: Date): String {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val start = timeFormat.format(startTime)
    val end = timeFormat.format(endTime)
    
    return if (start == end) start else "$start - $end"
}

// Previews

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun HistoryScreenEmptyPreview() {
    RepsTheme(darkTheme = true, dynamicColor = false) {
        HistoryScreen(
            historyItems = emptyList(),
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun HistoryScreenWithDataPreview() {
    val sampleData = listOf(
        HistoryItem.DateHeader("HEUTE"),
        HistoryItem.WorkoutItem(
            WorkoutSession(
                exerciseName = "Kreuzheben",
                weight = 100.0,
                reps = 5,
                totalSets = 3,
                startTime = Date(),
                endTime = Date(),
                sets = emptyList()
            )
        ),
        HistoryItem.WorkoutItem(
            WorkoutSession(
                exerciseName = "Bankdrücken",
                weight = 80.0,
                reps = 8,
                totalSets = 4,
                startTime = Date(),
                endTime = Date(),
                sets = emptyList()
            )
        ),
        HistoryItem.DateHeader("GESTERN"),
        HistoryItem.WorkoutItem(
            WorkoutSession(
                exerciseName = "Kniebeugen",
                weight = 120.0,
                reps = 5,
                totalSets = 5,
                startTime = Date(),
                endTime = Date(),
                sets = emptyList()
            )
        )
    )
    
    RepsTheme(darkTheme = true, dynamicColor = false) {
        HistoryScreen(
            historyItems = sampleData,
            onNavigateBack = {}
        )
    }
}
