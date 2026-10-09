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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.backup_close
import kilo.composeapp.generated.resources.backup_export_csv
import kilo.composeapp.generated.resources.backup_export_json
import kilo.composeapp.generated.resources.backup_import
import kilo.composeapp.generated.resources.backup_import_empty
import kilo.composeapp.generated.resources.backup_import_success
import kilo.composeapp.generated.resources.backup_subtitle
import kilo.composeapp.generated.resources.backup_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun BackupSheet(
    isOpen: Boolean,
    notice: BackupNotice?,
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
                            .navigationBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
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

                        Spacer(Modifier.height(20.dp))

                        // Close button
                        Text(
                            text = stringResource(Res.string.backup_close),
                            style = typography.body.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.textSecondary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(onClick = onDismiss)
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
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
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
