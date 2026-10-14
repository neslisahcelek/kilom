package app.kilo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import app.kilo.data.PrefsRepository
import app.kilo.data.SettingsPrefsRepository
import app.kilo.data.SettingsWeightRepository
import app.kilo.data.WeightRepository
import app.kilo.platform.rememberHaptics
import app.kilo.platform.rememberScaleOcr
import app.kilo.ui.DashboardFeedback
import app.kilo.ui.DashboardScreen
import app.kilo.ui.DashboardViewModel
import app.kilo.ui.theme.KiloTheme
import com.russhwolf.settings.Settings

import androidx.compose.runtime.LaunchedEffect
import app.kilo.platform.rememberReminderScheduler
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.reminder_notification_body
import kilo.composeapp.generated.resources.reminder_notification_title
import org.jetbrains.compose.resources.stringResource

/**
 * Lightweight manual dependency container for Kilo.
 */
class AppContainer(
    val settings: Settings = Settings(),
    val weights: WeightRepository = SettingsWeightRepository(settings),
    val prefs: PrefsRepository = SettingsPrefsRepository(settings),
)

@Composable
fun App(container: AppContainer = remember { AppContainer() }) {
    KiloTheme {
        val haptics = rememberHaptics()
        val scaleOcr = rememberScaleOcr()
        val healthSync = app.kilo.platform.rememberHealthSync()
        val reminderScheduler = rememberReminderScheduler()

        val viewModel = remember(container, haptics, scaleOcr, healthSync, reminderScheduler) {
            DashboardViewModel(
                weights = container.weights,
                prefs = container.prefs,
                readText = { bytes -> scaleOcr.readText(bytes) },
                feedback = object : DashboardFeedback {
                    override fun success() = haptics.success()
                    override fun scanComplete() = haptics.scanComplete()
                },
                healthSync = healthSync,
                reminderScheduler = reminderScheduler,
            )
        }

        val notifTitle = stringResource(Res.string.reminder_notification_title)
        val notifBody = stringResource(Res.string.reminder_notification_body)
        LaunchedEffect(Unit) {
            viewModel.rescheduleReminderIfEnabled(notifTitle, notifBody)
        }

        DashboardScreen(viewModel = viewModel)
    }
}
