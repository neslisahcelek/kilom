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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kilo.platform.CameraPreview
import app.kilo.platform.ViewfinderRect
import app.kilo.platform.rememberCameraController
import app.kilo.ui.theme.KiloTheme
import kilo.composeapp.generated.resources.Res
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

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        val screenW = maxWidth
        val screenH = maxHeight

        // 1. Live Native Camera Preview
        CameraPreview(
            controller = controller,
            modifier = Modifier.fillMaxSize(),
        )

        // 2. Viewfinder Reticle Calculations
        // Typical bathroom scale display aspect ratio is ~ 2.4 : 1
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
        // Top dark mask
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(reticleTop)
                .align(Alignment.TopCenter)
                .background(Color.Black.copy(alpha = 0.55f))
        )
        // Bottom dark mask
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(screenH - (reticleTop + reticleH))
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.55f))
        )
        // Left dark mask
        Box(
            modifier = Modifier
                .width(screenW * 0.09f)
                .height(reticleH)
                .align(Alignment.TopStart)
                .padding(top = reticleTop)
                .background(Color.Black.copy(alpha = 0.55f))
        )
        // Right dark mask
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
            // Subtle frosted glass center guide line
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
            // Close Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = onDismiss,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "✕", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

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
            // Gallery Shortcut
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

            // Main Shutter Button (Apple Camera style)
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

            // Empty spacer for symmetrical alignment
            Spacer(modifier = Modifier.size(50.dp))
        }
    }
}
