package app.kilo.ocr

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import androidx.exifinterface.media.ExifInterface
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume

private const val MAX_DIMENSION = 2000

actual class ScaleOcr {
    actual suspend fun readText(image: ByteArray): List<String> {
        logOcr("android recognition_started bytes=${image.size}")
        val bitmap = withContext(Dispatchers.Default) { decode(image) } ?: run {
            logOcr("android decode_failed")
            return emptyList()
        }
        logOcr("android decode_completed width=${bitmap.width} height=${bitmap.height}")
        return try {
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            try {
                val results = mutableListOf<String>()

                // Pass 1: Direct upright bitmap
                results += recognizeBitmap(recognizer, bitmap, "pass1_raw")

                // Pass 2: High-contrast grayscale for LCD displays
                coroutineContext.ensureActive()
                val lcdBitmap = applyColorMatrix(bitmap, contrast = 2.2f, brightness = -30f, invert = false)
                try {
                    results += recognizeBitmap(recognizer, lcdBitmap, "pass2_lcd_contrast")
                } finally {
                    if (lcdBitmap !== bitmap) lcdBitmap.recycle()
                }

                // Pass 3: High-contrast inverted for glowing LED displays on dark glass
                coroutineContext.ensureActive()
                val ledBitmap = applyColorMatrix(bitmap, contrast = 2.5f, brightness = -45f, invert = true)
                try {
                    results += recognizeBitmap(recognizer, ledBitmap, "pass3_led_invert")
                } finally {
                    if (ledBitmap !== bitmap) ledBitmap.recycle()
                }

                // Pass 4: Direct SevenSegmentReader on grayscale pixels
                coroutineContext.ensureActive()
                withContext(Dispatchers.Default) {
                    runSevenSegmentOnBitmap(bitmap)
                }?.let { segResult ->
                    logOcr("android pass4_7seg_matched=true")
                    results += segResult
                }

                results
            } finally {
                recognizer.close()
            }
        } finally {
            bitmap.recycle()
        }
    }

    private suspend fun recognizeBitmap(
        recognizer: TextRecognizer,
        bitmap: Bitmap,
        passName: String,
    ): List<String> {
        val text = suspendCancellableCoroutine<com.google.mlkit.vision.text.Text?> { cont ->
            recognizer.process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener {
                    logOcr("android pass=$passName blocks=${it.textBlocks.size}")
                    cont.resume(it)
                }
                .addOnFailureListener {
                    logOcr("android pass=$passName failed exception=${it::class.simpleName}")
                    cont.resume(null)
                }
                .addOnCanceledListener {
                    logOcr("android pass=$passName cancelled")
                    cont.cancel()
                }
        }
        return text?.textBlocks
            ?.flatMap { it.lines }
            ?.sortedBy { it.boundingBox?.top ?: 0 }
            ?.map { it.text }
            ?: emptyList()
    }

    private fun applyColorMatrix(
        src: Bitmap,
        contrast: Float,
        brightness: Float,
        invert: Boolean,
    ): Bitmap {
        val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val cm = ColorMatrix().apply { setSaturation(0f) }
        val scale = if (invert) -contrast else contrast
        val translate = if (invert) 255f - brightness else brightness
        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f,
            )
        )
        cm.postConcat(contrastMatrix)
        val paint = Paint().apply { colorFilter = ColorMatrixColorFilter(cm) }
        canvas.drawBitmap(src, 0f, 0f, paint)
        return out
    }

    private fun runSevenSegmentOnBitmap(src: Bitmap): String? {
        val maxDim = maxOf(src.width, src.height)
        if (maxDim <= 0) return null
        val scale = minOf(1.0f, 900f / maxDim.toFloat())
        val w = (src.width * scale).toInt()
        val h = (src.height * scale).toInt()
        if (w !in 80..1200 || h !in 60..1200) return null
        val scaled = if (w != src.width || h != src.height) {
            Bitmap.createScaledBitmap(src, w, h, true)
        } else {
            src
        }
        return try {
            val argb = IntArray(w * h)
            scaled.getPixels(argb, 0, w, 0, 0, w, h)
            val gray = ByteArray(w * h)
            for (i in argb.indices) {
                val c = argb[i]
                val r = (c shr 16) and 0xFF
                val g = (c shr 8) and 0xFF
                val b = c and 0xFF
                gray[i] = ((r * 77 + g * 150 + b * 29) shr 8).toByte()
            }
            SevenSegmentReader.read(gray, w, h, 0.95)
        } finally {
            if (scaled !== src) scaled.recycle()
        }
    }

    private fun decode(bytes: ByteArray): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_DIMENSION) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val raw = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts) ?: return null

        val orientation = try {
            ExifInterface(ByteArrayInputStream(bytes))
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } catch (e: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> { matrix.postRotate(90f); matrix.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.postRotate(270f); matrix.postScale(-1f, 1f) }
            else -> return raw
        }
        val rotated = Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
        if (rotated !== raw) raw.recycle()
        return rotated
    }
}
