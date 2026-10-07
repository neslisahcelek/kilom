package app.kilo.ocr

/** Conservative fallback for a rectified LCD with a separately recognized unit label.
 * The decimal point must be visible; missing segments and ambiguous patterns are rejected.
 * [pixels] is an upright 8-bit grayscale image; no image or recognized value is retained.
 */
internal object SevenSegmentReader {
    private data class Box(val left: Int, val top: Int, val right: Int, val bottom: Int) {
        val width get() = right - left + 1
        val height get() = bottom - top + 1
        val centerX get() = (left + right) / 2.0
    }

    fun read(pixels: ByteArray, width: Int, height: Int, unitLeft: Double): String? {
        if (width !in 80..1200 || height !in 60..1200 || pixels.size != width * height) return null
        // A unit on the right of the display separates the large weight from smaller readings.
        if (unitLeft !in 0.55..0.95) return null
        val left = (width * 0.07).toInt()
        val right = (width * (unitLeft - 0.025)).toInt()
        val top = (height * 0.06).toInt()
        val bottom = (height * 0.92).toInt()
        val histogram = IntArray(256)
        for (y in top until bottom) for (x in left until right) histogram[pixels[y * width + x].toInt() and 255]++
        val total = histogram.sum()
        val sum = histogram.indices.sumOf { it.toDouble() * histogram[it] }
        var lowerCount = 0
        var lowerSum = 0.0
        var threshold = 0
        var bestVariance = 0.0
        for (value in 0..254) {
            lowerCount += histogram[value]
            lowerSum += value.toDouble() * histogram[value]
            val upperCount = total - lowerCount
            if (lowerCount == 0 || upperCount == 0) continue
            val difference = lowerSum / lowerCount - (sum - lowerSum) / upperCount
            val variance = lowerCount.toDouble() * upperCount * difference * difference
            if (variance > bestVariance) { bestVariance = variance; threshold = value }
        }
        if (bestVariance == 0.0) return null
        val dark = BooleanArray(pixels.size)
        for (y in top until bottom) for (x in left until right) {
            dark[y * width + x] = (pixels[y * width + x].toInt() and 255) <= threshold
        }

        // Find compact, round dots near the baseline and exclude them from digit columns.
        val visited = BooleanArray(pixels.size)
        val queue = IntArray(pixels.size)
        val dots = mutableListOf<Box>()
        for (start in dark.indices) {
            if (!dark[start] || visited[start]) continue
            var head = 0
            var tail = 1
            queue[0] = start
            visited[start] = true
            var minX = start % width; var maxX = minX
            var minY = start / width; var maxY = minY
            while (head < tail) {
                val index = queue[head++]
                val x = index % width; val y = index / width
                minX = minOf(minX, x); maxX = maxOf(maxX, x)
                minY = minOf(minY, y); maxY = maxOf(maxY, y)
                fun visit(next: Int) {
                    if (dark[next] && !visited[next]) { visited[next] = true; queue[tail++] = next }
                }
                if (x > left) visit(index - 1)
                if (x + 1 < right) visit(index + 1)
                if (y > top) visit(index - width)
                if (y + 1 < bottom) visit(index + width)
            }
            val box = Box(minX, minY, maxX, maxY)
            if (box.top > height * 0.7 && box.width in (height * 0.025).toInt()..(height * 0.12).toInt() &&
                box.height in (height * 0.025).toInt()..(height * 0.12).toInt() &&
                box.width.toDouble() / box.height in 0.6..1.6 &&
                tail.toDouble() / (box.width * box.height) > 0.45
            ) {
                dots += box
                for (index in 0 until tail) dark[queue[index]] = false
            }
        }
        if (dots.size != 1) return null

        val columns = IntArray(width)
        for (y in top until bottom) for (x in left until right) if (dark[y * width + x]) columns[x]++
        val digits = mutableListOf<Box>()
        var x = left
        while (x < right) {
            if (columns[x] <= height * 0.02) { x++; continue }
            val start = x
            while (x < right && columns[x] > height * 0.02) x++
            var minY = bottom; var maxY = top
            for (y in top until bottom) for (column in start until x) if (dark[y * width + column]) {
                minY = minOf(minY, y); maxY = maxOf(maxY, y)
            }
            val box = Box(start, minY, x - 1, maxY)
            // Only discard small specks. A 1 has no horizontal end segments and is
            // shorter than other digits; dropping it can turn 170.75 into 70.75.
            if (box.height > height * 0.15) digits += box
        }
        if (digits.size !in 3..5) return null
        val referenceHeight = digits.maxOf { it.height }
        val baseline = digits.maxOf { it.bottom }
        if (referenceHeight <= height * 0.65) return null
        val dot = dots.single()
        val decimalAfter = digits.indexOfLast { it.centerX < dot.centerX } + 1
        if (decimalAfter !in 2..3 || digits.size - decimalAfter !in 1..2) return null
        if (dot.centerX >= digits[decimalAfter].centerX || dot.top < digits.maxOf { it.bottom } - height * 0.15) return null
        val result = StringBuilder()
        for ((index, box) in digits.withIndex()) {
            val digit = decode(dark, width, box, referenceHeight, baseline) ?: return null
            if (index == decimalAfter) result.append('.')
            result.append(digit)
        }
        return result.toString()
    }

    private fun decode(dark: BooleanArray, stride: Int, box: Box, referenceHeight: Int, baseline: Int): Char? {
        if (box.width < referenceHeight * 0.22) {
            // Validate both separated vertical strokes rather than stretching the
            // tight bounding box to a full-width seven-segment digit template.
            if (box.width < referenceHeight * 0.025 || box.height !in
                (referenceHeight * 0.6).toInt()..(referenceHeight * 0.9).toInt() ||
                baseline - box.bottom > referenceHeight * 0.18
            ) return null
            fun coverage(from: Double, to: Double): Double {
                var count = 0; var total = 0
                for (y in box.top + (from * box.height).toInt() until box.top + (to * box.height).toInt()) {
                    for (x in box.left..box.right) {
                        total++; if (dark[y * stride + x]) count++
                    }
                }
                return count.toDouble() / total.coerceAtLeast(1)
            }
            return if (coverage(0.0, 0.35) >= .6 && coverage(.65, 1.0) >= .6 &&
                coverage(.4, .6) <= .35
            ) '1' else null
        }
        if (box.height < referenceHeight * 0.6 || baseline - box.bottom > referenceHeight * 0.18) return null
        if (box.width.toDouble() / box.height !in 0.18..0.65) return null
        // 4 and 7 omit horizontal end segments too. Align their sampling windows
        // with the whole digit row instead of stretching their shorter ink bounds.
        val cell = box.copy(top = baseline - referenceHeight + 1, bottom = baseline)
        // Rectified LCD digits may still lean. Try upright and slanted segment windows.
        val upright = arrayOf(
            doubleArrayOf(.3, .02, .7, .12), doubleArrayOf(.05, .18, .25, .38),
            doubleArrayOf(.75, .18, .95, .38), doubleArrayOf(.3, .44, .7, .56),
            doubleArrayOf(.05, .65, .25, .85), doubleArrayOf(.75, .65, .95, .85),
            doubleArrayOf(.3, .9, .7, .98),
        )
        val slanted = arrayOf(
            upright[0], doubleArrayOf(.18, .18, .42, .38), doubleArrayOf(.68, .18, .92, .38),
            upright[3], doubleArrayOf(.02, .65, .26, .85), doubleArrayOf(.58, .65, .82, .85),
            doubleArrayOf(.15, .9, .55, .98),
        )
        val recognized = listOf(upright, slanted).mapNotNull { regions ->
            val segments = regions.map { region ->
                var count = 0; var total = 0
                for (y in cell.top + (region[1] * cell.height).toInt() until cell.top + (region[3] * cell.height).toInt()) {
                    for (x in cell.left + (region[0] * cell.width).toInt() until cell.left + (region[2] * cell.width).toInt()) {
                        total++; if (dark[y * stride + x]) count++
                    }
                }
                val ratio = count.toDouble() / total.coerceAtLeast(1)
                when { ratio >= .6 -> '1'; ratio <= .3 -> '0'; else -> '?' }
            }.joinToString("")
            when (segments) {
                "1110111" -> '0'; "0010010" -> '1'; "1011101" -> '2'; "1011011" -> '3'
                "0111010" -> '4'; "1101011" -> '5'; "1101111" -> '6'; "1010010" -> '7'
                "1111111" -> '8'; "1111011" -> '9'; else -> null
            }
        }.distinct()
        return recognized.singleOrNull()
    }
}
