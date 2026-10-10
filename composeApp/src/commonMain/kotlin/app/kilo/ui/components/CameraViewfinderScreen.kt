package app.kilo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kilo.platform.CameraPermissionStatus
import app.kilo.platform.CameraPreview
import app.kilo.platform.ViewfinderRect
import app.kilo.platform.rememberCameraController
import androidx.lifecycle.compose.LifecycleResumeEffect
import kilo.composeapp.generated.resources.Res
import kilo.composeapp.generated.resources.camera_choose_gallery
import kilo.composeapp.generated.resources.camera_permission_denied_desc
import kilo.composeapp.generated.resources.camera_permission_denied_title
import kilo.composeapp.generated.resources.camera_permission_open_settings
import kilo.composeapp.generated.resources.camera_unavailable_desc
import kilo.composeapp.generated.resources.camera_unavailable_title
import kilo.composeapp.generated.resources.viewfinder_instruction
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun CameraViewfinderScreen(
    onDismiss: () -> Unit,
    onImageCaptured: (ByteArray) -> Unit,
    onOpenGallery: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val controller = rememberCameraController()
    val scope = rememberCoroutineScope()
    var isCapturing by remember { mutableStateOf(false) }

    LifecycleResumeEffect(controller) {
        controller.refreshPermission()
        onPauseOrDispose {}
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        val screenW = maxWidth
        val screenH = maxHeight

        when (controller.permissionStatus) {
            CameraPermissionStatus.GRANTED -> {
                // 1. Live Native Camera Preview
                CameraPreview(
                    controller = controller,
                    modifier = Modifier.fillMaxSize(),
                )

                // 2. Viewfinder Reticle Calculations
                val reticleW = screenW * 0.82f
                val reticleH = reticleW / 2.3f
                val reticleTop = screenH * 0.32f

                // Normalized crop coordinates [0..1]
                val leftNorm = 0.09f
                val rightNorm = 0.91f
                val topNorm = (reticleTop / screenH).coerceIn(0f, 1f)
                val bottomNorm = ((reticleTop + reticleH) / screenH).coerceIn(0f, 1f)
                val viewfinderRect = remember(topNorm, bottomNorm) {
                    ViewfinderRect(
                        left = leftNorm,
                        top = topNorm,
                        right = rightNorm,
                        bottom = bottomNorm,
                    )
                }

                // 3. Dark Scrim & Cutout around Viewfinder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(reticleTop)
                        .align(Alignment.TopCenter)
                        .background(Color.Black.copy(alpha = 0.55f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenH - (reticleTop + reticleH))
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.55f))
                )
                Box(
                    modifier = Modifier
                        .width(screenW * 0.09f)
                        .height(reticleH)
                        .align(Alignment.TopStart)
                        .padding(top = reticleTop)
                        .background(Color.Black.copy(alpha = 0.55f))
                )
                Box(
                    modifier = Modifier
                        .width(screenW * 0.09f)
                        .height(reticleH)
                        .align(Alignment.TopEnd)
                        .padding(top = reticleTop)
                        .background(Color.Black.copy(alpha = 0.55f))
                )

                // 4. Center Reticle Frame with corner highlights
                Box(
                    modifier = Modifier
                        .width(reticleW)
                        .height(reticleH)
                        .align(Alignment.TopCenter)
                        .padding(top = reticleTop)
                        .clip(RoundedCornerShape(18.dp))
                        .border(2.dp, Color(0xFF0A84FF).copy(alpha = 0.85f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(2.dp)
                            .background(Color.White.copy(alpha = 0.35f))
                    )
                }

                // 5. Instruction Pill above reticle
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = reticleTop - 48.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.viewfinder_instruction),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                // 6. Top Bar: Close and Torch Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CloseButton(onClick = onDismiss)

                    // Torch Toggle Button
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (controller.isTorchActive) Color(0xFFFFCC00) else Color.Black.copy(alpha = 0.5f)
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                role = Role.Button,
                                onClick = { controller.toggleTorch() },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "⚡",
                            color = if (controller.isTorchActive) Color.Black else Color.White,
                            fontSize = 18.sp,
                        )
                    }
                }

                // 7. Bottom Bar: Gallery & Shutter Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 32.dp, vertical = 40.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                role = Role.Button,
                                onClick = {
                                    onDismiss()
                                    onOpenGallery()
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "🖼", fontSize = 22.sp)
                    }

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(4.dp, Color.White, CircleShape)
                            .padding(5.dp)
                            .clip(CircleShape)
                            .background(if (isCapturing) Color.Gray else Color.White)
                            .clickable(
                                enabled = !isCapturing,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                role = Role.Button,
                                onClick = {
                                    if (isCapturing) return@clickable
                                    isCapturing = true
                                    scope.launch {
                                        val bytes = controller.capture(viewfinderRect)
                                        isCapturing = false
                                        if (bytes != null && bytes.isNotEmpty()) {
                                            onImageCaptured(bytes)
                                        }
                                    }
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isCapturing) {
                            CircularProgressIndicator(
                                color = Color(0xFF0A84FF),
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.size(50.dp))
                }
            }

            CameraPermissionStatus.CHECKING -> {
                TopCloseOnlyBar(onDismiss = onDismiss)

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF0A84FF),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp),
                    )
                }
            }

            CameraPermissionStatus.DENIED -> {
                TopCloseOnlyBar(onDismiss = onDismiss)

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CameraNoticeCard(
                        icon = "🔒",
                        title = stringResource(Res.string.camera_permission_denied_title),
                        subtitle = stringResource(Res.string.camera_permission_denied_desc),
                        primaryButtonText = stringResource(Res.string.camera_permission_open_settings),
                        onPrimaryClick = { controller.openSettings() },
                        secondaryButtonText = stringResource(Res.string.camera_choose_gallery),
                        onSecondaryClick = {
                            onDismiss()
                            onOpenGallery()
                        },
                    )
                }
            }

            CameraPermissionStatus.NOT_SUPPORTED -> {
                TopCloseOnlyBar(onDismiss = onDismiss)

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CameraNoticeCard(
                        icon = "📷",
                        title = stringResource(Res.string.camera_unavailable_title),
                        subtitle = stringResource(Res.string.camera_unavailable_desc),
                        primaryButtonText = stringResource(Res.string.camera_choose_gallery),
                        onPrimaryClick = {
                            onDismiss()
                            onOpenGallery()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun TopCloseOnlyBar(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CloseButton(onClick = onDismiss)
    }
}

@Composable
private fun CloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.5f))
            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "✕", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CameraNoticeCard(
    icon: String,
    title: String,
    subtitle: String,
    primaryButtonText: String,
    onPrimaryClick: () -> Unit,
    secondaryButtonText: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(horizontal = 28.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF1C1C1E).copy(alpha = 0.95f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
            .padding(horizontal = 24.dp, vertical = 28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = icon, fontSize = 28.sp)
            }

            Text(
                text = title,
                color = Color.White,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0A84FF))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = onPrimaryClick,
                    )
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = primaryButtonText,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            if (secondaryButtonText != null && onSecondaryClick != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            onClick = onSecondaryClick,
                        )
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = secondaryButtonText,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}
