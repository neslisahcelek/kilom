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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kilo.platform.rememberPhotoPicker
import app.kilo.ui.components.ActionBar
import app.kilo.ui.components.EntrySheet
import app.kilo.ui.components.GlassCard
import app.kilo.ui.components.HeroCard
import app.kilo.ui.components.HistoryRow
import app.kilo.ui.theme.KiloTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.app_name
import kilo.composeapp.generated.resources.history_empty
import kilo.composeapp.generated.resources.history_title
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val hazeState = remember { HazeState() }
    val colors = KiloTheme.colors
    val typography = KiloTheme.type

    val photoPicker = rememberPhotoPicker(onImage = viewModel::onImage)

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

                // Action Bar (Camera, Gallery, Manual + Unit toggle)
                item(key = "action_bar") {
                    ActionBar(
                        selectedUnit = state.unit,
                        onUnitChange = viewModel::setUnit,
                        onCameraClick = { photoPicker.takePhoto() },
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
                onSave = viewModel::save,
                onDismiss = viewModel::dismissSheet,
                hazeState = hazeState,
            )
        }
    }
}
