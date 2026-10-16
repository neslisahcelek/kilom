package app.kilo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kilo.ui.BackupNotice
import app.kilo.ui.theme.KiloTheme
import dev.chrisbanes.haze.HazeState
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.writeString
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import app.kilo.ui.HealthSyncNotice
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.backup_close
import kilo.composeapp.generated.resources.backup_export_csv
import kilo.composeapp.generated.resources.backup_export_json
import kilo.composeapp.generated.resources.backup_import
import kilo.composeapp.generated.resources.backup_import_empty
import kilo.composeapp.generated.resources.backup_import_success
import kilo.composeapp.generated.resources.backup_subtitle
import kilo.composeapp.generated.resources.backup_title
import kilo.composeapp.generated.resources.health_auto_sync_subtitle
import kilo.composeapp.generated.resources.health_auto_sync_title
import kilo.composeapp.generated.resources.health_import_button
import kilo.composeapp.generated.resources.health_import_loading
import kilo.composeapp.generated.resources.health_section_title_android
import kilo.composeapp.generated.resources.health_section_title_ios
import kilo.composeapp.generated.resources.health_sync_all_button
import kilo.composeapp.generated.resources.health_sync_all_loading
import kilo.composeapp.generated.resources.health_sync_denied
import kilo.composeapp.generated.resources.health_sync_success
import kilo.composeapp.generated.resources.reminder_enable_subtitle
import kilo.composeapp.generated.resources.reminder_enable_title
import kilo.composeapp.generated.resources.reminder_section_title
import kilo.composeapp.generated.resources.reminder_time_title
import kilo.composeapp.generated.resources.cycle_settings_enable
import kilo.composeapp.generated.resources.cycle_settings_enable_subtitle
import kilo.composeapp.generated.resources.cycle_settings_title
import kilo.composeapp.generated.resources.cycle_show_card_subtitle
import kilo.composeapp.generated.resources.cycle_show_card_title
import kilo.composeapp.generated.resources.hevy_active_status_desc
import kilo.composeapp.generated.resources.hevy_benefit_cycle
import kilo.composeapp.generated.resources.hevy_benefit_doms
import kilo.composeapp.generated.resources.hevy_benefit_strength
import kilo.composeapp.generated.resources.hevy_clear_button
import kilo.composeapp.generated.resources.hevy_how_to_export_hint
import kilo.composeapp.generated.resources.hevy_import_button
import kilo.composeapp.generated.resources.hevy_import_empty_error
import kilo.composeapp.generated.resources.hevy_import_success_format
import kilo.composeapp.generated.resources.hevy_imported_count_format
import kilo.composeapp.generated.resources.hevy_open_analytics_cta
import kilo.composeapp.generated.resources.hevy_section_desc
import kilo.composeapp.generated.resources.hevy_section_title
import kilo.composeapp.generated.resources.hevy_why_title
import io.github.vinceglb.filekit.dialogs.FileKitType
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

object PickerType {
    fun File(extensions: List<String>) = FileKitType.File(extensions = extensions)
}

@Composable
fun BackupSheet(
    isOpen: Boolean,
    notice: BackupNotice?,
    isHealthSyncSupported: Boolean = false,
    healthProvider: app.kilo.platform.HealthProvider = app.kilo.platform.HealthProvider.NONE,
    healthSyncEnabled: Boolean = false,
    isSyncingHealth: Boolean = false,
    isImportingHealth: Boolean = false,
    healthSyncNotice: HealthSyncNotice? = null,
    onToggleHealthSync: (Boolean) -> Unit = {},
    onSyncAllToHealth: () -> Unit = {},
    onImportFromHealth: () -> Unit = {},
    reminderSupported: Boolean = false,
    reminderEnabled: Boolean = false,
    reminderMinutes: Int = 480,
    onToggleReminder: (Boolean) -> Unit = {},
    cycleEnabled: Boolean = false,
    isCycleCardVisible: Boolean = true,
    userGender: app.kilo.data.UserGender = app.kilo.data.UserGender.UNSPECIFIED,
    onToggleCycle: (Boolean) -> Unit = {},
    onToggleCycleCardVisible: (Boolean) -> Unit = {},
    onSelectUserGender: (app.kilo.data.UserGender) -> Unit = {},
    onOpenCycleSetup: () -> Unit = {},
    workoutCount: Int = 0,
    workoutImportNotice: BackupNotice? = null,
    onImportHevyCsv: (String) -> Unit = {},
    onClearWorkouts: () -> Unit = {},
    onOpenCorrelation: () -> Unit = {},
    onExportCsv: () -> String,
    onExportJson: () -> String,
    onImport: (String) -> Unit,
    onDismiss: () -> Unit,
    hazeState: HazeState? = null,
    modifier: Modifier = Modifier,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type
    val scope = rememberCoroutineScope()

    var pendingExportType by remember { mutableStateOf<String?>(null) }

    val fileSaver = rememberFileSaverLauncher(dialogSettings = FileKitDialogSettings()) { platformFile: PlatformFile? ->
        if (platformFile != null) {
            val type = pendingExportType
            scope.launch {
                val data = if (type == "json") onExportJson() else onExportCsv()
                try {
                    platformFile.writeString(data)
                } catch (_: Exception) {}
            }
        }
        pendingExportType = null
    }

    val filePicker = rememberFilePickerLauncher { platformFile: PlatformFile? ->
        if (platformFile != null) {
            scope.launch {
                try {
                    val text = platformFile.readString()
                    if (text.isNotBlank()) {
                        onImport(text)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    val hevyPicker = rememberFilePickerLauncher(
        type = PickerType.File(extensions = listOf("csv")),
    ) { platformFile: PlatformFile? ->
        if (platformFile != null) {
            scope.launch {
                try {
                    val text = platformFile.readString()
                    if (text.isNotBlank()) {
                        onImportHevyCsv(text)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(colors.scrim)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.BottomCenter,
        ) {
            AnimatedVisibility(
                visible = isOpen,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 28.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                        ),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    hazeState = hazeState,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .navigationBarsPadding()
                            .padding(horizontal = 24.dp)
                            .padding(top = 16.dp, bottom = 44.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Drag pill
                        Box(
                            modifier = Modifier
                                .size(width = 36.dp, height = 4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(colors.glassBorder),
                        )

                        Spacer(Modifier.height(16.dp))

                        // Title
                        Text(
                            text = stringResource(Res.string.backup_title),
                            style = typography.title,
                            color = colors.textPrimary,
                        )

                        Spacer(Modifier.height(4.dp))

                        // Subtitle
                        Text(
                            text = stringResource(Res.string.backup_subtitle),
                            style = typography.caption,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )

                        // Status Notice Banner
                        if (notice != null) {
                            Spacer(Modifier.height(16.dp))
                            val bannerBg = if (notice.isSuccess) colors.negative.copy(alpha = 0.15f) else colors.positive.copy(alpha = 0.15f)
                            val bannerBorder = if (notice.isSuccess) colors.negative else colors.positive
                            val bannerText = if (notice.isSuccess) {
                                stringResource(Res.string.backup_import_success, notice.count)
                            } else {
                                stringResource(Res.string.backup_import_empty)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bannerBg)
                                    .border(1.dp, bannerBorder.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = bannerText,
                                    style = typography.subhead.copy(fontWeight = FontWeight.Medium),
                                    color = bannerBorder,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // Action button: Export CSV
                        BackupActionButton(
                            title = stringResource(Res.string.backup_export_csv),
                            iconLabel = "CSV",
                            onClick = {
                                pendingExportType = "csv"
                                fileSaver.launch(suggestedName = "kilom_backup", defaultExtension = "csv")
                            },
                        )

                        Spacer(Modifier.height(10.dp))

                        // Action button: Export JSON
                        BackupActionButton(
                            title = stringResource(Res.string.backup_export_json),
                            iconLabel = "JSON",
                            onClick = {
                                pendingExportType = "json"
                                fileSaver.launch(suggestedName = "kilom_backup", defaultExtension = "json")
                            },
                        )

                        Spacer(Modifier.height(10.dp))

                        // Action button: Import from File
                        BackupActionButton(
                            title = stringResource(Res.string.backup_import),
                            iconLabel = "↑",
                            isAccent = true,
                            onClick = {
                                filePicker.launch()
                            },
                        )

                        // Hevy Resistance Training Section
                        Spacer(Modifier.height(18.dp))

                        Text(
                            text = stringResource(Res.string.hevy_section_title),
                            style = typography.subhead.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 2.dp),
                        )

                        Text(
                            text = stringResource(Res.string.hevy_section_desc),
                            style = typography.caption,
                            color = colors.textSecondary,
                            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 10.dp),
                        )

                        // "Why Import Workout Data?" Explanation Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.glassTint)
                                .border(0.5.dp, colors.glassBorder, RoundedCornerShape(16.dp))
                                .padding(14.dp),
                        ) {
                            Column {
                                Text(
                                    text = stringResource(Res.string.hevy_why_title),
                                    style = typography.caption.copy(fontWeight = FontWeight.Bold, fontSize = 12.5.sp),
                                    color = colors.accent,
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    text = stringResource(Res.string.hevy_benefit_doms),
                                    style = typography.caption.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                                    color = colors.textSecondary,
                                )

                                Spacer(Modifier.height(5.dp))

                                Text(
                                    text = stringResource(Res.string.hevy_benefit_cycle),
                                    style = typography.caption.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                                    color = colors.textSecondary,
                                )

                                Spacer(Modifier.height(5.dp))

                                Text(
                                    text = stringResource(Res.string.hevy_benefit_strength),
                                    style = typography.caption.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                                    color = colors.textSecondary,
                                )

                                Spacer(Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.accent.copy(alpha = 0.08f))
                                        .border(0.5.dp, colors.accent.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                ) {
                                    Text(
                                        text = stringResource(Res.string.hevy_how_to_export_hint),
                                        style = typography.caption.copy(fontSize = 11.sp, lineHeight = 15.5.sp),
                                        color = colors.textSecondary,
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Workout Import Status Notice Banner (if user just uploaded a CSV)
                        if (workoutImportNotice != null) {
                            val wBg = if (workoutImportNotice.isSuccess) {
                                colors.negative.copy(alpha = 0.15f)
                            } else {
                                colors.positive.copy(alpha = 0.15f)
                            }
                            val wBorder = if (workoutImportNotice.isSuccess) colors.negative else colors.positive
                            val wText = if (workoutImportNotice.isSuccess) {
                                stringResource(Res.string.hevy_import_success_format, workoutImportNotice.count)
                            } else {
                                stringResource(Res.string.hevy_import_empty_error)
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(wBg)
                                    .border(1.dp, wBorder.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = wText,
                                    style = typography.caption.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.5.sp),
                                    color = wBorder,
                                    textAlign = TextAlign.Center,
                                )
                            }

                            Spacer(Modifier.height(10.dp))
                        }

                        // Action button: Import Hevy CSV
                        BackupActionButton(
                            title = stringResource(Res.string.hevy_import_button),
                            iconLabel = "🏋️",
                            isAccent = workoutCount == 0,
                            onClick = {
                                hevyPicker.launch()
                            },
                        )

                        if (workoutCount > 0) {
                            Spacer(Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.accent.copy(alpha = 0.08f))
                                    .border(0.5.dp, colors.accent.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                                    .padding(14.dp),
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            text = stringResource(Res.string.hevy_imported_count_format, workoutCount),
                                            style = typography.caption.copy(fontWeight = FontWeight.Bold, fontSize = 12.5.sp),
                                            color = colors.accent,
                                        )

                                        Text(
                                            text = stringResource(Res.string.hevy_clear_button),
                                            style = typography.caption.copy(fontSize = 11.5.sp),
                                            fontWeight = FontWeight.SemiBold,
                                            color = colors.danger,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(colors.danger.copy(alpha = 0.10f))
                                                .border(0.5.dp, colors.danger.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                                .clickable(role = Role.Button, onClick = onClearWorkouts)
                                                .padding(horizontal = 10.dp, vertical = 5.dp),
                                        )
                                    }

                                    Spacer(Modifier.height(6.dp))

                                    Text(
                                        text = stringResource(Res.string.hevy_active_status_desc),
                                        style = typography.caption.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                                        color = colors.textSecondary,
                                    )

                                    Spacer(Modifier.height(10.dp))

                                    // Direct redirection button to open Correlation & Strength Analytics
                                    BackupActionButton(
                                        title = stringResource(Res.string.hevy_open_analytics_cta),
                                        iconLabel = "📊",
                                        isAccent = true,
                                        onClick = onOpenCorrelation,
                                    )
                                }
                            }
                        }

                        // Optional Health Sync Section (Apple Health / Health Connect)
                        if (isHealthSyncSupported) {
                            Spacer(Modifier.height(18.dp))

                            val healthTitle = if (healthProvider == app.kilo.platform.HealthProvider.APPLE_HEALTH) {
                                stringResource(Res.string.health_section_title_ios)
                            } else {
                                stringResource(Res.string.health_section_title_android)
                            }

                            Text(
                                text = healthTitle,
                                style = typography.subhead.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary,
                                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 8.dp),
                            )

                            // Health Sync Notice Banner
                            if (healthSyncNotice != null) {
                                val isSuccess = healthSyncNotice.isSuccess
                                val hBg = if (isSuccess) colors.negative.copy(alpha = 0.15f) else colors.positive.copy(alpha = 0.15f)
                                val hBorder = if (isSuccess) colors.negative else colors.positive
                                val hText = if (isSuccess) {
                                    stringResource(Res.string.health_sync_success, healthSyncNotice.count)
                                } else {
                                    stringResource(Res.string.health_sync_denied)
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(hBg)
                                        .border(1.dp, hBorder.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = hText,
                                        style = typography.subhead.copy(fontWeight = FontWeight.Medium),
                                        color = hBorder,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                                Spacer(Modifier.height(10.dp))
                            }

                            // Health Auto-Sync Toggle Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.glassTint)
                                    .border(0.5.dp, colors.glassBorder, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                        Text(
                                            text = stringResource(Res.string.health_auto_sync_title),
                                            style = typography.body.copy(fontWeight = FontWeight.SemiBold),
                                            color = colors.textPrimary,
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = stringResource(Res.string.health_auto_sync_subtitle),
                                            style = typography.caption,
                                            color = colors.textSecondary,
                                        )
                                    }
                                    Switch(
                                        checked = healthSyncEnabled,
                                        onCheckedChange = onToggleHealthSync,
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = colors.onAccent,
                                            checkedTrackColor = colors.accent,
                                            uncheckedThumbColor = colors.textSecondary,
                                            uncheckedTrackColor = colors.glassTint,
                                        ),
                                    )
                                }
                            }

                            Spacer(Modifier.height(10.dp))

                            // Import from Health Button
                            BackupActionButton(
                                title = if (isImportingHealth) stringResource(Res.string.health_import_loading) else stringResource(Res.string.health_import_button),
                                iconLabel = "📥",
                                onClick = onImportFromHealth,
                            )

                            Spacer(Modifier.height(10.dp))

                            // Sync All to Health Button
                            BackupActionButton(
                                title = if (isSyncingHealth) stringResource(Res.string.health_sync_all_loading) else stringResource(Res.string.health_sync_all_button),
                                iconLabel = "♥",
                                onClick = onSyncAllToHealth,
                            )
                        }

                        // Daily Reminder Section
                        if (reminderSupported) {
                            Spacer(Modifier.height(18.dp))

                            Text(
                                text = stringResource(Res.string.reminder_section_title),
                                style = typography.subhead.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary,
                                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 8.dp),
                            )

                            // Reminder Toggle Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.glassTint)
                                    .border(0.5.dp, colors.glassBorder, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                        Text(
                                            text = stringResource(Res.string.reminder_enable_title),
                                            style = typography.body.copy(fontWeight = FontWeight.SemiBold),
                                            color = colors.textPrimary,
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        val hour = reminderMinutes / 60
                                        val min = reminderMinutes % 60
                                        val timeStr = "${hour.toString().padStart(2, '0')}:${min.toString().padStart(2, '0')}"
                                        Text(
                                            text = if (reminderEnabled) "${stringResource(Res.string.reminder_time_title)}: $timeStr" else stringResource(Res.string.reminder_enable_subtitle),
                                            style = typography.caption,
                                            color = colors.textSecondary,
                                        )
                                    }
                                    Switch(
                                        checked = reminderEnabled,
                                        onCheckedChange = onToggleReminder,
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = colors.onAccent,
                                            checkedTrackColor = colors.accent,
                                            uncheckedThumbColor = colors.textSecondary,
                                            uncheckedTrackColor = colors.glassTint,
                                        ),
                                    )
                                }
                            }
                        }

                        // Cycle & Nutrition Guide Section
                        Spacer(Modifier.height(18.dp))

                        Text(
                            text = stringResource(Res.string.cycle_settings_title),
                            style = typography.subhead.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 8.dp),
                        )

                        // Show/Hide Cycle Card on Dashboard Toggle
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(colors.glassTint)
                                .border(0.5.dp, colors.glassBorder, RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                    Text(
                                        text = stringResource(Res.string.cycle_show_card_title),
                                        style = typography.body.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.textPrimary,
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = stringResource(Res.string.cycle_show_card_subtitle),
                                        style = typography.caption,
                                        color = colors.textSecondary,
                                    )
                                }
                                Switch(
                                    checked = isCycleCardVisible,
                                    onCheckedChange = onToggleCycleCardVisible,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = colors.onAccent,
                                        checkedTrackColor = colors.accent,
                                        uncheckedThumbColor = colors.textSecondary,
                                        uncheckedTrackColor = colors.glassTint,
                                    ),
                                )
                            }
                        }

                        if (isCycleCardVisible) {
                            Spacer(Modifier.height(10.dp))

                            // Cycle Toggle Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.glassTint)
                                    .border(0.5.dp, colors.glassBorder, RoundedCornerShape(16.dp))
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                        Text(
                                            text = stringResource(Res.string.cycle_settings_enable),
                                            style = typography.body.copy(fontWeight = FontWeight.SemiBold),
                                            color = colors.textPrimary,
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = stringResource(Res.string.cycle_settings_enable_subtitle),
                                            style = typography.caption,
                                            color = colors.textSecondary,
                                        )
                                    }
                                    Switch(
                                        checked = cycleEnabled,
                                        onCheckedChange = { checked ->
                                            if (checked) {
                                                onOpenCycleSetup()
                                            } else {
                                                onToggleCycle(false)
                                            }
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = colors.onAccent,
                                            checkedTrackColor = colors.accent,
                                            uncheckedThumbColor = colors.textSecondary,
                                            uncheckedTrackColor = colors.glassTint,
                                        ),
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // Close button
                        Text(
                            text = stringResource(Res.string.backup_close),
                            style = typography.body.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textSecondary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(role = Role.Button, onClick = onDismiss)
                                .padding(horizontal = 24.dp, vertical = 10.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BackupActionButton(
    title: String,
    iconLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAccent: Boolean = false,
) {
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    val bg = if (isAccent) colors.accent.copy(alpha = 0.16f) else colors.glassTint
    val border = if (isAccent) colors.accent.copy(alpha = 0.4f) else colors.glassBorder

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(0.5.dp, border, RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isAccent) colors.accent.copy(alpha = 0.25f) else colors.glassBorder.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = iconLabel,
                    style = typography.caption.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                    color = if (isAccent) colors.accent else colors.textPrimary,
                )
            }

            Spacer(Modifier.width(14.dp))

            Text(
                text = title,
                style = typography.body.copy(fontWeight = FontWeight.Medium),
                color = colors.textPrimary,
            )
        }

        Text(
            text = "›",
            style = typography.title.copy(fontWeight = FontWeight.Light),
            color = colors.textSecondary,
        )
    }
}
