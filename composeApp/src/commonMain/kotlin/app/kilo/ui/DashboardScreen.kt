package app.kilo.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.font.FontWeight
import app.kilo.platform.rememberPhotoPicker
import app.kilo.platform.rememberShareSheet
import app.kilo.ui.components.ActionBar
import app.kilo.ui.components.BackupSheet
import app.kilo.ui.components.CycleCard
import app.kilo.ui.components.CycleSheet
import app.kilo.ui.components.EntrySheet
import app.kilo.ui.components.GlassCard
import app.kilo.ui.components.GoalCard
import app.kilo.ui.components.GoalSheet
import app.kilo.ui.components.HealthOnboardingSheet
import app.kilo.ui.components.HeroCard
import app.kilo.ui.components.HistoryRow
import app.kilo.ui.components.IosDatePicker
import kotlinx.datetime.Instant
import kotlinx.datetime.toLocalDateTime
import app.kilo.ui.components.ShareCardSheet
import app.kilo.ui.components.StreakCard
import app.kilo.ui.components.SummaryCard
import app.kilo.ui.components.TrendChart
import app.kilo.ui.theme.KiloTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.app_name
import kilo.composeapp.generated.resources.backup_title
import kilo.composeapp.generated.resources.health_badge_connected
import kilo.composeapp.generated.resources.health_badge_not_connected
import kilo.composeapp.generated.resources.health_badge_title
import kilo.composeapp.generated.resources.health_section_title_android
import kilo.composeapp.generated.resources.history_empty
import kilo.composeapp.generated.resources.history_title
import kilo.composeapp.generated.resources.reminder_notification_body
import kilo.composeapp.generated.resources.reminder_notification_title
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.kilo.ui.components.CameraViewfinderScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val hazeState = remember { HazeState() }
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    var isCameraOpen by remember { mutableStateOf(false) }
    val photoPicker = rememberPhotoPicker(onImage = viewModel::onImage)
    val shareSheet = rememberShareSheet()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundBrush),
    ) {
        // Ambient background gradient blobs for liquid glass refraction
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(hazeState),
        ) {
            // Blob A (top right blue / violet accent)
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .offset(x = 180.dp, y = (-40).dp)
                    .clip(CircleShape)
                    .background(colors.blobA.copy(alpha = 0.45f))
                    .blur(70.dp),
            )

            // Blob B (bottom left warm accent)
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .offset(x = (-100).dp, y = 350.dp)
                    .clip(CircleShape)
                    .background(colors.blobB.copy(alpha = 0.35f))
                    .blur(80.dp),
            )
        }

        // Main content column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(Res.string.app_name),
                    style = typography.largeTitle,
                    color = colors.textPrimary,
                )

                // Backup & Export/Import Button
                val backupDesc = stringResource(Res.string.backup_title)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colors.glassTint)
                        .border(1.dp, colors.glassBorder, CircleShape)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = backupDesc,
                            onClick = { viewModel.openBackup() },
                        )
                        .semantics { contentDescription = backupDesc },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "⇅",
                        style = typography.title.copy(fontSize = 18.sp),
                        color = colors.textSecondary,
                    )
                }
            }

            // Scrollable / structured dashboard items
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Hero card
                item(key = "hero_card") {
                    HeroCard(
                        hero = state.hero,
                        unit = state.unit,
                        hazeState = hazeState,
                    )
                }

                // Apple Health / Health Connect Badge (Guideline 2.5.1 Visibility)
                if (state.isHealthSyncSupported) {
                    item(key = "health_badge") {
                        val isConnected = state.healthSyncEnabled
                        val title = if (state.healthProvider == app.kilo.platform.HealthProvider.APPLE_HEALTH) {
                            stringResource(Res.string.health_badge_title)
                        } else {
                            stringResource(Res.string.health_section_title_android)
                        }
                        val statusText = if (isConnected) {
                            stringResource(Res.string.health_badge_connected)
                        } else {
                            stringResource(Res.string.health_badge_not_connected)
                        }
                        GlassCard(
                            hazeState = hazeState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .clickable {
                                    if (!isConnected) viewModel.openHealthOnboarding() else viewModel.openBackup()
                                },
                            shape = RoundedCornerShape(18.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "❤️", fontSize = 16.sp)
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = title,
                                        style = typography.caption.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                                        color = colors.textPrimary,
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(if (isConnected) colors.negative else colors.textSecondary.copy(alpha = 0.5f))
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = statusText,
                                        style = typography.caption,
                                        color = if (isConnected) colors.negative else colors.textSecondary,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }
                }

                // Goal Progress Card
                item(key = "goal_card") {
                    GoalCard(
                        goal = state.goal,
                        unit = state.unit,
                        hazeState = hazeState,
                        onClick = viewModel::openGoalSheet,
                    )
                }

                // Cycle & Nutrition Guide Card
                item(key = "cycle_card") {
                    CycleCard(
                        state = state.cycle,
                        hazeState = hazeState,
                        onSetupClick = viewModel::openCycleSetup,
                        onOpenSheet = viewModel::openCycleSheet,
                    )
                }

                // Streak & Consistency Matrix Card
                val streak = state.streak
                if (streak != null) {
                    item(key = "streak_card") {
                        StreakCard(
                            streak = streak,
                            hazeState = hazeState,
                            onShareClick = viewModel::openShareSheet,
                        )
                    }
                }

                // Period Summary Card (Week / Month)
                item(key = "summary_card") {
                    SummaryCard(
                        summary = state.summary,
                        period = state.summaryPeriod,
                        unit = state.unit,
                        hazeState = hazeState,
                        onSelectPeriod = viewModel::setSummaryPeriod,
                    )
                }

                // Trend Chart
                item(key = "trend_chart") {
                    TrendChart(
                        entries = state.history.map { it.entry },
                        unit = state.unit,
                        hazeState = hazeState,
                    )
                }

                // Action Bar (Camera, Gallery, Manual + Unit toggle)
                item(key = "action_bar") {
                    ActionBar(
                        selectedUnit = state.unit,
                        onUnitChange = viewModel::setUnit,
                        onCameraClick = {
                            if (app.kilo.platform.isCustomCameraSupported) {
                                isCameraOpen = true
                            } else {
                                photoPicker.takePhoto()
                            }
                        },
                        onGalleryClick = { photoPicker.pickFromGallery() },
                        onManualClick = { viewModel.openManual() },
                        hazeState = hazeState,
                    )
                }

                // History Section Header
                item(key = "history_header") {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(Res.string.history_title),
                        style = typography.title,
                        color = colors.textPrimary,
                    )
                }

                // History items or empty notice
                if (state.history.isEmpty()) {
                    item(key = "history_empty") {
                        GlassCard(
                            hazeState = hazeState,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = stringResource(Res.string.history_empty),
                                    style = typography.subhead,
                                    color = colors.textSecondary,
                                )
                            }
                        }
                    }
                } else {
                    items(
                        items = state.history,
                        key = { it.entry.id },
                    ) { item ->
                        HistoryRow(
                            item = item,
                            unit = state.unit,
                            timeZone = TimeZone.currentSystemDefault(),
                            onDelete = viewModel::delete,
                            onClick = { viewModel.openEdit(item.entry) },
                            hazeState = hazeState,
                        )
                    }
                }
            }
        }

        // Modal Entry Sheet (appears on manual open or image pick)
        state.sheet?.let { sheet ->
            EntrySheet(
                sheet = sheet,
                unit = state.unit,
                onInputChange = viewModel::onInputChange,
                onDateSelected = viewModel::onDateSelected,
                onTagSelected = viewModel::onTagSelected,
                onSave = viewModel::save,
                onDismiss = viewModel::dismissSheet,
                hazeState = hazeState,
            )
        }

        // In-App Live Camera Viewfinder Overlay
        AnimatedVisibility(
            visible = isCameraOpen,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            CameraViewfinderScreen(
                onDismiss = { isCameraOpen = false },
                onImageCaptured = { bytes ->
                    isCameraOpen = false
                    viewModel.onImage(bytes)
                },
                onOpenGallery = {
                    isCameraOpen = false
                    photoPicker.pickFromGallery()
                },
            )
        }

        val notifTitle = stringResource(Res.string.reminder_notification_title)
        val notifBody = stringResource(Res.string.reminder_notification_body)

        // Data Backup & Export/Import & Health Sync Sheet
        BackupSheet(
            isOpen = state.isBackupSheetOpen,
            notice = state.backupNotice,
            isHealthSyncSupported = state.isHealthSyncSupported,
            healthProvider = state.healthProvider,
            healthSyncEnabled = state.healthSyncEnabled,
            isSyncingHealth = state.isSyncingHealth,
            isImportingHealth = state.isImportingHealth,
            healthSyncNotice = state.healthSyncNotice,
            onToggleHealthSync = viewModel::setHealthSyncEnabled,
            onSyncAllToHealth = viewModel::syncAllToHealth,
            onImportFromHealth = viewModel::importFromHealth,
            reminderSupported = state.isReminderSupported,
            reminderEnabled = state.reminderEnabled,
            reminderMinutes = state.reminderMinutes,
            onToggleReminder = { enabled -> viewModel.setReminderEnabled(enabled, notifTitle, notifBody) },
            cycleEnabled = state.cycle.isEnabled,
            onToggleCycle = { if (!it) viewModel.disableCycleTracking() },
            onOpenCycleSetup = viewModel::openCycleSetup,
            onExportCsv = viewModel::exportCsv,
            onExportJson = viewModel::exportJson,
            onImport = { viewModel.importData(it) },
            onDismiss = viewModel::dismissBackup,
            hazeState = hazeState,
        )

        // Goal Input / Edit Sheet
        GoalSheet(
            isOpen = state.isGoalSheetOpen,
            goalInput = state.goalInput,
            unit = state.unit,
            hasExistingGoal = state.goal != null,
            error = state.goalError,
            onInputChange = viewModel::onGoalInputChange,
            onSave = viewModel::saveGoal,
            onClear = viewModel::clearGoal,
            onDismiss = viewModel::dismissGoalSheet,
        )

        // Apple Health Onboarding Sheet
        HealthOnboardingSheet(
            isOpen = state.isHealthOnboardingOpen,
            onConnect = { importAlso -> viewModel.connectHealth(importAlso) },
            onDismiss = viewModel::dismissHealthOnboarding,
        )

        // Milestone & Achievement Share Card Sheet
        ShareCardSheet(
            isOpen = state.isShareSheetOpen,
            streak = state.streak,
            latestKg = state.hero?.latest?.entry?.kg,
            unit = state.unit,
            onShare = { text ->
                shareSheet.shareText(text)
                viewModel.dismissShareSheet()
            },
            onDismiss = viewModel::dismissShareSheet,
        )

        // Menstrual Cycle & Evidence-Based Nutrition Sheet
        CycleSheet(
            isOpen = state.isCycleSheetOpen,
            state = state.cycle,
            onUpdateLastPeriodDate = viewModel::updateCycleLastPeriodDate,
            onDisableCycle = viewModel::disableCycleTracking,
            onDismiss = viewModel::dismissCycleSheet,
            hazeState = hazeState,
        )

        // Modal Date Picker for Initial Cycle Setup
        if (state.isCycleSetupOpen) {
            val nowMillis = remember {
                kotlin.time.Clock.System.now().toEpochMilliseconds()
            }
            IosDatePicker(
                selectedDateMillis = nowMillis,
                onDateSelected = { millis ->
                    val instant = Instant.fromEpochMilliseconds(millis)
                    val date = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                    viewModel.enableCycleTracking(date)
                },
                onDismiss = viewModel::dismissCycleSetup,
                hazeState = hazeState,
            )
        }
    }
}
