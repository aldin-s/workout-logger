package com.example.workouttracker.ui.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workouttracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsState,
    onNavigateBack: () -> Unit,
    onVibrationEnabledChange: (Boolean) -> Unit,
    onVibrationDurationChange: (VibrationDuration) -> Unit,
    onSoundEnabledChange: (Boolean) -> Unit,
    onSoundSelectionClick: () -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onPauseTimeChange: (Int) -> Unit,
    onDefaultSetsChange: (Int) -> Unit,
    onDefaultRepsChange: (Int) -> Unit,
    onLanguageChange: (String) -> Unit,
    onExportCsv: () -> Unit,
    onExportJson: () -> Unit,
    onImportJson: (Uri) -> Unit,
    onDeleteAllData: () -> Unit,
    onClearExportResult: () -> Unit,
    onClearImportResult: () -> Unit,
    onClearDeleteResult: () -> Unit,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    
    // Dialogs state
    var showVibrationDurationDialog by remember { mutableStateOf(false) }
    var showPauseTimeDialog by remember { mutableStateOf(false) }
    var showSetsDialog by remember { mutableStateOf(false) }
    var showRepsDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showExportFormatDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    
    // File picker
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { onImportJson(it) }
    }
    
    // Handle export result
    LaunchedEffect(state.exportResult) {
        when (val result = state.exportResult) {
            is ExportResult.Success -> {
                context.startActivity(Intent.createChooser(result.intent, context.getString(R.string.export_history)))
                Toast.makeText(context, R.string.export_success, Toast.LENGTH_SHORT).show()
                onClearExportResult()
            }
            is ExportResult.Error -> {
                Toast.makeText(context, R.string.export_error, Toast.LENGTH_SHORT).show()
                onClearExportResult()
            }
            null -> {}
        }
    }
    
    // Handle import result
    LaunchedEffect(state.importResult) {
        when (val result = state.importResult) {
            is ImportResult.Success -> {
                val summary = result.summary
                val message = if (summary.exercisesImported > 0) {
                    context.getString(
                        R.string.import_success_with_exercises,
                        summary.workoutsImported,
                        summary.exercisesImported,
                        summary.workoutsSkipped
                    )
                } else {
                    context.getString(
                        R.string.import_success,
                        summary.workoutsImported,
                        summary.workoutsSkipped
                    )
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                onClearImportResult()
            }
            is ImportResult.Error -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                onClearImportResult()
            }
            null -> {}
        }
    }
    
    // Handle delete result
    LaunchedEffect(state.deleteResult) {
        when (state.deleteResult) {
            is DeleteResult.Success -> {
                Toast.makeText(context, R.string.delete_success, Toast.LENGTH_SHORT).show()
                onClearDeleteResult()
                onFinish()
            }
            is DeleteResult.Error -> {
                Toast.makeText(context, R.string.error_generic, Toast.LENGTH_SHORT).show()
                onClearDeleteResult()
            }
            null -> {}
        }
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.settings),
                        fontWeight = FontWeight.Bold
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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
        ) {
            // Timer Section
            // Timer Section (Feedback settings)
            SettingsSection(title = stringResource(R.string.settings_timer_section)) {
                // Vibration Toggle
                SwitchSettingItem(
                    title = stringResource(R.string.vibration),
                    checked = state.vibrationEnabled,
                    onCheckedChange = onVibrationEnabledChange
                )
                
                // Vibration Duration (only visible when vibration enabled)
                if (state.vibrationEnabled) {
                    ClickableSettingItem(
                        title = stringResource(R.string.vibration_duration),
                        value = stringResource(VibrationDuration.fromMs(state.vibrationDuration).labelResId),
                        onClick = { showVibrationDurationDialog = true }
                    )
                }
                
                // Sound Toggle
                SwitchSettingItem(
                    title = stringResource(R.string.sound),
                    checked = state.soundEnabled,
                    onCheckedChange = onSoundEnabledChange
                )
                
                // Sound Selection (only visible when sound enabled)
                if (state.soundEnabled) {
                    ClickableSettingItem(
                        title = stringResource(R.string.sound_selection),
                        value = state.soundName,
                        onClick = onSoundSelectionClick
                    )
                }
                
                // Keep Screen On
                SwitchSettingItem(
                    title = stringResource(R.string.keep_screen_on),
                    checked = state.keepScreenOn,
                    onCheckedChange = onKeepScreenOnChange
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Workout Section (Default values)
            SettingsSection(title = stringResource(R.string.settings_workout_section)) {
                // Default Pause Time
                ClickableSettingItem(
                    title = stringResource(R.string.default_pause_time),
                    value = stringResource(R.string.pause_time_seconds, state.defaultPauseTime),
                    onClick = { showPauseTimeDialog = true }
                )
                
                // Default Sets
                ClickableSettingItem(
                    title = stringResource(R.string.default_sets),
                    value = stringResource(R.string.default_sets_value, state.defaultSets),
                    onClick = { showSetsDialog = true }
                )
                
                // Default Reps
                ClickableSettingItem(
                    title = stringResource(R.string.default_reps),
                    value = stringResource(R.string.default_reps_value, state.defaultReps),
                    onClick = { showRepsDialog = true }
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Language Section
            SettingsSection(title = stringResource(R.string.settings_language_section)) {
                ClickableSettingItem(
                    title = stringResource(R.string.language),
                    value = if (state.language == "de") {
                        stringResource(R.string.language_german)
                    } else {
                        stringResource(R.string.language_english)
                    },
                    onClick = { showLanguageDialog = true }
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Data Section
            SettingsSection(title = stringResource(R.string.settings_data_section)) {
                // Export
                ClickableSettingItem(
                    title = stringResource(R.string.export_history),
                    subtitle = stringResource(R.string.export_history_subtitle),
                    onClick = { showExportFormatDialog = true },
                    isLoading = state.isExporting
                )
                
                // Import
                ClickableSettingItem(
                    title = stringResource(R.string.import_history),
                    subtitle = stringResource(R.string.import_history_subtitle),
                    onClick = { importFileLauncher.launch(arrayOf("application/json")) },
                    isLoading = state.isImporting
                )
                
                // Delete All
                ClickableSettingItem(
                    title = stringResource(R.string.delete_all_data),
                    subtitle = stringResource(R.string.delete_all_data_subtitle),
                    onClick = { showDeleteConfirmDialog = true },
                    isDestructive = true,
                    isLoading = state.isDeleting
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // About Section
            SettingsSection(title = stringResource(R.string.settings_about)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.app_version_label),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = state.appVersion,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Dialogs
    if (showVibrationDurationDialog) {
        VibrationDurationDialog(
            currentDuration = VibrationDuration.fromMs(state.vibrationDuration),
            onDurationSelected = { 
                onVibrationDurationChange(it)
                showVibrationDurationDialog = false
            },
            onDismiss = { showVibrationDurationDialog = false }
        )
    }
    
    if (showPauseTimeDialog) {
        NumberInputDialog(
            title = stringResource(R.string.default_pause_time),
            currentValue = state.defaultPauseTime,
            minValue = 1,
            maxValue = 600,
            onValueConfirmed = {
                onPauseTimeChange(it)
                showPauseTimeDialog = false
            },
            onDismiss = { showPauseTimeDialog = false }
        )
    }
    
    if (showSetsDialog) {
        NumberInputDialog(
            title = stringResource(R.string.default_sets),
            currentValue = state.defaultSets,
            minValue = 1,
            maxValue = 99,
            onValueConfirmed = {
                onDefaultSetsChange(it)
                showSetsDialog = false
            },
            onDismiss = { showSetsDialog = false }
        )
    }
    
    if (showRepsDialog) {
        NumberInputDialog(
            title = stringResource(R.string.default_reps),
            currentValue = state.defaultReps,
            minValue = 1,
            maxValue = 999,
            onValueConfirmed = {
                onDefaultRepsChange(it)
                showRepsDialog = false
            },
            onDismiss = { showRepsDialog = false }
        )
    }
    
    if (showLanguageDialog) {
        LanguageDialog(
            currentLanguage = state.language,
            onLanguageSelected = { 
                onLanguageChange(it)
                showLanguageDialog = false
                // No restart needed - Per-App Language API handles it automatically
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
    
    if (showExportFormatDialog) {
        ExportFormatDialog(
            onCsvSelected = {
                showExportFormatDialog = false
                onExportCsv()
            },
            onJsonSelected = {
                showExportFormatDialog = false
                onExportJson()
            },
            onDismiss = { showExportFormatDialog = false }
        )
    }
    
    if (showDeleteConfirmDialog) {
        DeleteConfirmDialog(
            onConfirm = {
                showDeleteConfirmDialog = false
                onDeleteAllData()
            },
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
private fun SwitchSettingItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                // Checked state (ON)
                checkedThumbColor = MaterialTheme.colorScheme.surface,
                checkedTrackColor = MaterialTheme.colorScheme.onSurface,
                checkedBorderColor = MaterialTheme.colorScheme.onSurface,
                checkedIconColor = MaterialTheme.colorScheme.onSurface,
                // Unchecked state (OFF)
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                uncheckedTrackColor = MaterialTheme.colorScheme.surface,
                uncheckedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                uncheckedIconColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

@Composable
private fun ClickableSettingItem(
    title: String,
    value: String? = null,
    subtitle: String? = null,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
    isLoading: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading) { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                // Brutalist: No red, use dimmed text for destructive actions
                color = if (isDestructive) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VibrationDurationDialog(
    currentDuration: VibrationDuration,
    onDurationSelected: (VibrationDuration) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.vibration_duration)) },
        text = {
            Column {
                VibrationDuration.entries.forEach { duration ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDurationSelected(duration) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = duration == currentDuration,
                            onClick = { onDurationSelected(duration) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(duration.labelResId))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun LanguageDialog(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val languages = listOf(
        "de" to stringResource(R.string.language_german),
        "en" to stringResource(R.string.language_english)
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.language)) },
        text = {
            Column {
                languages.forEach { (code, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(code) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = code == currentLanguage,
                            onClick = { onLanguageSelected(code) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(name)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun ExportFormatDialog(
    onCsvSelected: () -> Unit,
    onJsonSelected: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.export_format_title)) },
        text = {
            Column {
                TextButton(
                    onClick = onCsvSelected,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.export_csv),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                TextButton(
                    onClick = onJsonSelected,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.export_json),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun DeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_confirm_title)) },
        text = { Text(stringResource(R.string.delete_confirm_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.delete).uppercase(),
                    // Brutalist: No red, use regular onSurface with emphasis via uppercase
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

/**
 * Reusable number picker dialog for selecting integers from a range.
 * Uses radio buttons for consistent UI with other settings dialogs.
 */
@Composable
private fun NumberInputDialog(
    title: String,
    currentValue: Int,
    minValue: Int,
    maxValue: Int,
    onValueConfirmed: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var textValue by remember { mutableStateOf(currentValue.toString()) }
    var isError by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = textValue,
                onValueChange = { newValue ->
                    // Only allow digits
                    if (newValue.all { it.isDigit() }) {
                        textValue = newValue
                        val intValue = newValue.toIntOrNull()
                        isError = intValue == null || intValue < minValue || intValue > maxValue
                    }
                },
                isError = isError,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val intValue = textValue.toIntOrNull()
                    if (intValue != null && intValue in minValue..maxValue) {
                        onValueConfirmed(intValue)
                    }
                },
                enabled = !isError && textValue.isNotEmpty()
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
