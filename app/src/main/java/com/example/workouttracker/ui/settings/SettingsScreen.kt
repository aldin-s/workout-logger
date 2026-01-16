package com.example.workouttracker.ui.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import com.example.workouttracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsState,
    onNavigateBack: () -> Unit,
    onVibrationEnabledChange: (Boolean) -> Unit,
    onVibrationDurationChange: (VibrationDuration) -> Unit,
    onSoundEnabledChange: (Boolean) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onPauseTimeChange: (PauseTime) -> Unit,
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
                Toast.makeText(context, context.getString(R.string.import_success, result.count), Toast.LENGTH_LONG).show()
                onClearImportResult()
            }
            is ImportResult.Error -> {
                Toast.makeText(context, R.string.import_error, Toast.LENGTH_SHORT).show()
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
                
                // Keep Screen On
                SwitchSettingItem(
                    title = stringResource(R.string.keep_screen_on),
                    checked = state.keepScreenOn,
                    onCheckedChange = onKeepScreenOnChange
                )
                
                // Default Pause Time
                ClickableSettingItem(
                    title = stringResource(R.string.default_pause_time),
                    value = stringResource(R.string.pause_time_seconds, state.defaultPauseTime),
                    onClick = { showPauseTimeDialog = true }
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
        PauseTimeDialog(
            currentPauseTime = PauseTime.fromSeconds(state.defaultPauseTime),
            onPauseTimeSelected = {
                onPauseTimeChange(it)
                showPauseTimeDialog = false
            },
            onDismiss = { showPauseTimeDialog = false }
        )
    }
    
    if (showLanguageDialog) {
        LanguageDialog(
            currentLanguage = state.language,
            onLanguageSelected = { 
                onLanguageChange(it)
                showLanguageDialog = false
                Toast.makeText(context, R.string.restart_app_message, Toast.LENGTH_SHORT).show()
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
            onCheckedChange = onCheckedChange
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
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
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
private fun PauseTimeDialog(
    currentPauseTime: PauseTime,
    onPauseTimeSelected: (PauseTime) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.default_pause_time)) },
        text = {
            Column {
                PauseTime.entries.forEach { pauseTime ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPauseTimeSelected(pauseTime) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = pauseTime == currentPauseTime,
                            onClick = { onPauseTimeSelected(pauseTime) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${pauseTime.seconds}s")
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
                    text = stringResource(R.string.delete),
                    color = MaterialTheme.colorScheme.error
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
