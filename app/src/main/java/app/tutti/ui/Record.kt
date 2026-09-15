package app.tutti.ui

import android.graphics.PathMeasure
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import app.tutti.R
import app.tutti.schedule.Score
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/** The program area and label, as fractions of the record's radius. */
private const val OUTER = 0.95f
private const val INNER = 0.43f
private const val LABEL = 0.36f

/**
 * Time runs inward, like a record. Equal-area mapping: every minute of dinner gets the same
 * amount of vinyl, so the busy final minutes near the label stay readable.
 */
fun grooveRadius(fraction: Float, radius: Float): Float {
    val ro = radius * OUTER
    val ri = radius * INNER
    val f = fraction.coerceIn(0f, 1f)
    return sqrt(ro * ro - (ro * ro - ri * ri) * f)
}

/** What's printed on the record's label. [main] stays upright while the disc turns. */
data class DiscLabel(val top: String = "", val main: String = "", val sub: String = "", val bottom: String = "")

/** The pressed vinyl (grooves and bands) only changes when the score does, so it is drawn once and turned. */
private class Pressing {
    var score: Score? = null
    var size = 0
    var colors: TuttiColors? = null
    var image: ImageBitmap? = null
}

/**
 * The record. Each dish owns a slice of the disc; its steps are pressed as bands from the rim
 * (the downbeat) toward the label (the final chord). Hands-on steps are solid ink, heat is fine
 * colored grooves. Optional platter strobe, tonearm, played-area dimming, and cue flash.
 */
@Composable
fun Turntable(
    score: Score?,
    modifier: Modifier = Modifier,
    label: DiscLabel = DiscLabel(),
    labelPaper: Color? = null,
    center: (Size) -> Offset = { Offset(it.width / 2f, it.height / 2f) },
    radius: (Size) -> Float = { minOf(it.width, it.height) / 2f },
    now: () -> Float? = { null },
    rotation: () -> Float = { 0f },
    strobe: (() -> Float)? = null,
    arm: (() -> Float)? = null,
    armDrop: () -> Float = { 1f },
    cueLit: () -> Boolean = { false },
    flashDish: Int = -1,
    flash: () -> Float = { 0f },
) {
    val colors = tutti
    val context = LocalContext.current
    val measurer = rememberTextMeasurer()
    val pressing = remember { Pressing() }
    val curvedPaint = remember {
        android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            typeface = ResourcesCompat.getFont(context, R.font.archivo)
            fontVariationSettings = "'wdth' 125, 'wght' 600"
            letterSpacing = 0.14f
        }
    }

    Canvas(modifier) {
        val c = center(size)
        val r = radius(size)
        val total = score?.let { s -> maxOf(s.tutti, s.slots.maxOfOrNull { it.end } ?: 1).toFloat() }?.coerceAtLeast(60f) ?: 1f
        val nowValue = now()
        val spin = rotation()

        strobe?.let { drawPlatter(c, r, spin, it(), colors) }
        drawSoftShadow(c + Offset(0f, r * 0.04f), r * 1.12f, if (colors.dark) 0.6f else 0.22f)

        val disc = pressed(pressing, score, r, total, colors)
        rotate(spin, c) {
            drawImage(
                disc,
                dstOffset = IntOffset((c.x - disc.width / 2f).roundToInt(), (c.y - disc.height / 2f).roundToInt()),
                dstSize = IntSize(disc.width, disc.height),
                filterQuality = FilterQuality.Medium,
            )
            val amount = flash()
            if (score != null && flashDish in score.recipes.indices && amount > 0f) drawFlash(score, c, r, flashDish, amount)
        }
        nowValue?.let { dimPlayed(c, r, grooveRadius(it / total, r), colors) }
        drawSheen(c, r)
        nowValue?.let {
            drawCircle(Color.White.copy(alpha = 0.6f), grooveRadius(it / total, r), c, style = Stroke(1.4.dp.toPx()))
        }
        drawLabel(c, r, spin, label, labelPaper ?: colors.label, colors, curvedPaint, measurer)
        arm?.let { drawTonearm(c, r, grooveRadius(it(), r), colors, cueLit(), armDrop().coerceIn(0f, 1f)) }
    }
}

private fun DrawScope.pressed(pressing: Pressing, score: Score?, r: Float, total: Float, colors: TuttiColors): ImageBitmap {
    val px = ceil(r * 2f).toInt().coerceAtLeast(2)
    val cached = pressing.image
    if (cached != null && pressing.score === score && pressing.size == px && pressing.colors == colors) return cached
    val image = ImageBitmap(px, px)
    CanvasDrawScope().draw(this, layoutDirection, androidx.compose.ui.graphics.Canvas(image), Size(px.toFloat(), px.toFloat())) {
        val cc = Offset(px / 2f, px / 2f)
        drawCircle(colors.vinyl, r, cc)
        drawGrooves(cc, r, colors)
        if (score != null) drawBands(score, cc, r, total)
    }
    pressing.image = image
    pressing.score = score
    pressing.size = px
    pressing.colors = colors
    return image
}

/** A shadow with a real falloff: solid under the object, fading past its edge. */
private fun DrawScope.drawSoftShadow(center: Offset, radius: Float, alpha: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            0f to Color.Black.copy(alpha = alpha),
            0.8f to Color.Black.copy(alpha = alpha),
            1f to Color.Transparent,
            center = center,
            radius = radius,
        ),
        radius = radius,
        center = center,
    )
}

private fun sector(c: Offset, outer: Float, inner: Float, start: Float, sweep: Float) = Path().apply {
    arcTo(Rect(c, outer), start, sweep, forceMoveTo = true)
    arcTo(Rect(c, inner), start + sweep, -sweep, forceMoveTo = false)
    close()
}

private fun DrawScope.drawGrooves(c: Offset, r: Float, colors: TuttiColors) {
    val step = 3.dp.toPx()
    val width = 0.7.dp.toPx()
    var gr = r * LABEL + step
    while (gr < r * 0.985f) {
        drawCircle(colors.groove, gr, c, style = Stroke(width))
        gr += step
    }
    drawCircle(colors.rim, r * OUTER + 2.dp.toPx(), c, style = Stroke(1.dp.toPx()))
    drawCircle(colors.rim, r * INNER - 2.dp.toPx(), c, style = Stroke(1.dp.toPx()))
    drawCircle(colors.rim, r - 0.5.dp.toPx(), c, style = Stroke(1.dp.toPx()))
}

private fun DrawScope.drawBands(score: Score, c: Offset, r: Float, total: Float) {
    val n = score.recipes.size
    val gap = if (n > 1) 4f else 0f
    val span = 360f / n
    val minThickness = 2.dp.toPx()
    val grooveStep = 2.6.dp.toPx()
    val grooveWidth = 0.9.dp.toPx()
    for (slot in score.slots) {
        val ink = score.recipes[slot.dish].color
        val start = -90f + slot.dish * span + gap / 2f
        val sweep = span - gap
        val outer = grooveRadius(slot.start / total, r)
        val inner = minOf(grooveRadius(slot.end / total, r), outer - minThickness)
        if (score.stepOf(slot).handsOn) {
            drawPath(sector(c, outer, inner, start, sweep), ink)
        } else {
            drawPath(sector(c, outer, inner, start, sweep), ink.copy(alpha = 0.07f))
            var gr = inner + grooveStep / 2f
            while (gr < outer) {
                drawArc(ink.copy(alpha = 0.5f), start, sweep, false, Offset(c.x - gr, c.y - gr), Size(gr * 2, gr * 2), style = Stroke(grooveWidth))
                gr += grooveStep
            }
        }
    }
}

private fun DrawScope.drawFlash(score: Score, c: Offset, r: Float, dish: Int, amount: Float) {
    val n = score.recipes.size
    val gap = if (n > 1) 4f else 0f
    val span = 360f / n
    drawPath(sector(c, r * OUTER, r * INNER, -90f + dish * span + gap / 2f, span - gap), Color.White.copy(alpha = 0.3f * amount))
}

private fun DrawScope.dimPlayed(c: Offset, r: Float, rNow: Float, colors: TuttiColors) {
    val played = Path().apply {
        fillType = PathFillType.EvenOdd
        addOval(Rect(c, r * OUTER + 3.dp.toPx()))
        addOval(Rect(c, rNow))
    }
    drawPath(played, colors.vinyl.copy(alpha = 0.62f))
}

/** Light on vinyl stays put while the record turns under it. */
private fun DrawScope.drawSheen(c: Offset, r: Float) {
    val brush = Brush.sweepGradient(
        0f to Color.Transparent,
        0.07f to Color.White.copy(alpha = 0.09f),
        0.16f to Color.Transparent,
        0.55f to Color.Transparent,
        0.61f to Color.White.copy(alpha = 0.06f),
        0.7f to Color.Transparent,
        1f to Color.Transparent,
        center = c,
    )
    val vinyl = Path().apply {
        fillType = PathFillType.EvenOdd
        addOval(Rect(c, r))
        addOval(Rect(c, r * LABEL))
    }
    drawPath(vinyl, brush)
}

/** The platter rim's strobe dots: they pulse on every beat. */
private fun DrawScope.drawPlatter(c: Offset, r: Float, spin: Float, pulse: Float, colors: TuttiColors) {
    drawCircle(colors.sunken, r * 1.08f, c)
    drawCircle(colors.line, r * 1.08f, c, style = Stroke(1.dp.toPx()))
    val dots = 48
    val ring = r * 1.045f
    val dot = 2.dp.toPx()
    for (i in 0 until dots) {
        val a = Math.toRadians((spin + i * 360f / dots).toDouble())
        val lit = i % 2 == 0
        val color = if (lit) colors.ink.copy(alpha = 0.3f + 0.7f * pulse) else colors.ink.copy(alpha = 0.18f)
        drawCircle(color, if (lit) dot * (1f + 0.35f * pulse) else dot * 0.8f, Offset(c.x + ring * cos(a).toFloat(), c.y + ring * sin(a).toFloat()))
    }
}

private fun DrawScope.drawLabel(
    c: Offset,
    r: Float,
    spin: Float,
    label: DiscLabel,
    paper: Color,
    colors: TuttiColors,
    paint: android.graphics.Paint,
    measurer: TextMeasurer,
) {
    val rl = r * LABEL
    val ink = if (paper.luminance() > 0.5f) Color(0xFF15171A) else Color(0xFFF6F7F8)
    // Small caps on a dark ink label need every bit of contrast they can get.
    val soft = ink.copy(alpha = if (paper.luminance() > 0.5f) 0.78f else 1f)
    rotate(spin, c) {
        drawCircle(paper, rl, c)
        drawCircle(Color.Black.copy(alpha = 0.14f), rl, c, style = Stroke(1.dp.toPx()))
        paint.color = soft.toArgb()
        paint.textSize = (rl * 0.12f).coerceIn(7.sp.toPx(), 12.sp.toPx())
        drawIntoCanvas { canvas ->
            if (label.top.isNotEmpty()) curved(canvas.nativeCanvas, label.top, c, rl * 0.74f, top = true, paint)
            if (label.bottom.isNotEmpty()) curved(canvas.nativeCanvas, label.bottom, c, rl * 0.74f + paint.textSize * 0.74f, top = false, paint)
        }
        if (label.main.isEmpty()) drawCircle(colors.plinth, 2.6.dp.toPx(), c)
    }
    if (label.main.isNotEmpty()) {
        val mainPx = minOf(rl * 0.42f, 60.sp.toPx())
        val main = measurer.measure(label.main, Type.time.copy(color = ink, fontSize = mainPx.toSp(), lineHeight = mainPx.toSp()))
        val sub = label.sub.takeIf { it.isNotEmpty() }?.let {
            val subPx = minOf(rl * 0.12f, 12.sp.toPx())
            measurer.measure(it, Type.label.copy(color = soft, fontSize = subPx.toSp(), lineHeight = (subPx * 1.3f).toSp()))
        }
        val height = main.size.height + (sub?.size?.height ?: 0)
        val top = c.y - height / 2f
        drawText(main, topLeft = Offset(c.x - main.size.width / 2f, top))
        sub?.let { drawText(it, topLeft = Offset(c.x - it.size.width / 2f, top + main.size.height)) }
    }
}

private fun curved(canvas: android.graphics.Canvas, text: String, c: Offset, radius: Float, top: Boolean, paint: android.graphics.Paint) {
    val path = android.graphics.Path().apply {
        addArc(RectF(c.x - radius, c.y - radius, c.x + radius, c.y + radius), 180f, if (top) 180f else -180f)
    }
    val length = PathMeasure(path, false).length
    val width = paint.measureText(text)
    canvas.drawTextOnPath(text, path, ((length - width) / 2f).coerceAtLeast(0f), 0f, paint)
}

/** The stylus sits where the arm's reach meets the groove at [rs]; the right-hand solution, as on a real deck. */
private fun armStylus(c: Offset, pivot: Offset, rs: Float, length: Float): Offset? {
    val d = (pivot - c).getDistance()
    if (d > rs + length || d < abs(rs - length)) return null
    val a = (rs * rs - length * length + d * d) / (2f * d)
    val h = sqrt(maxOf(0f, rs * rs - a * a))
    val u = (pivot - c) / d
    val base = c + u * a
    val perp = Offset(-u.y, u.x)
    val s1 = base + perp * h
    val s2 = base - perp * h
    return if (s1.x > s2.x) s1 else s2
}

private fun rotateVector(v: Offset, degrees: Float): Offset {
    val a = Math.toRadians(degrees.toDouble())
    val cs = cos(a).toFloat()
    val sn = sin(a).toFloat()
    return Offset(v.x * cs - v.y * sn, v.x * sn + v.y * cs)
}

private fun DrawScope.drawTonearm(c: Offset, r: Float, stylusRadius: Float, colors: TuttiColors, cueLit: Boolean, drop: Float) {
    val pivot = c + Offset(r * 0.99f, -r * 0.95f)
    val length = r * 1.13f
    val rest = pivot + Offset(r * 0.2f, length * 0.98f)
    val onRecord = armStylus(c, pivot, stylusRadius, length) ?: rest
    val stylus = lerp(rest, onRecord, drop)
    val dir = (stylus - pivot) / (stylus - pivot).getDistance()
    val head = rotateVector(dir, 24f)

    drawSoftShadow(pivot + Offset(2.dp.toPx(), 4.dp.toPx()), 26.dp.toPx(), if (colors.dark) 0.5f else 0.16f)
    drawLine(colors.arm, pivot - dir * (length * 0.1f), pivot - dir * (length * 0.24f), 15.dp.toPx(), StrokeCap.Butt)
    drawLine(colors.arm, pivot, stylus, 5.dp.toPx(), StrokeCap.Round)
    drawLine(colors.armHighlight.copy(alpha = 0.55f), pivot + Offset(-dir.y, dir.x) * 1.dp.toPx(), stylus + Offset(-dir.y, dir.x) * 1.dp.toPx(), 1.dp.toPx(), StrokeCap.Round)
    drawLine(colors.arm, stylus - head * 4.dp.toPx(), stylus + head * 17.dp.toPx(), 13.dp.toPx(), StrokeCap.Round)
    val light = stylus + head * 11.dp.toPx()
    drawCircle(if (cueLit) colors.cue else colors.armHighlight.copy(alpha = 0.4f), 5.dp.toPx(), light)
    if (cueLit) drawCircle(colors.cueEdge, 5.dp.toPx(), light, style = Stroke(1.2.dp.toPx()))

    drawCircle(colors.surface, 17.dp.toPx(), pivot)
    drawCircle(colors.line, 17.dp.toPx(), pivot, style = Stroke(1.dp.toPx()))
    drawCircle(colors.arm, 6.5.dp.toPx(), pivot)
}
