package app.tutti.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tutti.schedule.Score
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val CLOCK = DateTimeFormatter.ofPattern("h:mm a")
fun clockTime(secondsFromNow: Int): String = LocalTime.now().plusSeconds(secondsFromNow.toLong()).format(CLOCK)
fun minutesLabel(seconds: Int) = "${(seconds + 59) / 60} min"

@Composable
fun Hairline(modifier: Modifier = Modifier, color: Color = Palette.rule) {
    Box(modifier.fillMaxWidth().height(1.dp).background(color))
}

@Composable
fun Label(text: String, color: Color, modifier: Modifier = Modifier) {
    BasicText(text.uppercase(), modifier, style = Type.label.copy(color = color))
}

@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = Palette.ink,
    content: Color = Palette.paper,
    enabled: Boolean = true,
    arrow: Boolean = true,
) {
    Row(
        modifier
            .pressable(enabled, onClick)
            .clip(RoundedCornerShape(50))
            .background(if (enabled) container else container.copy(alpha = 0.25f))
            .padding(horizontal = 24.dp, vertical = 17.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        BasicText(text, style = Type.body.copy(color = content, fontWeight = FontWeight.Medium, fontSize = 16.sp))
        if (arrow) {
            Spacer(Modifier.width(12.dp))
            ArrowIcon(content)
        }
    }
}

@Composable
fun GhostButton(text: String, onClick: () -> Unit, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .pressable(onClick = onClick)
            .clip(RoundedCornerShape(50))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(50))
            .padding(horizontal = 22.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(text, style = Type.body.copy(color = color, fontWeight = FontWeight.Medium, fontSize = 16.sp))
    }
}

@Composable
fun ArrowIcon(color: Color, modifier: Modifier = Modifier, back: Boolean = false) {
    Canvas(modifier.size(18.dp)) {
        val w = size.width
        val s = 1.8.dp.toPx()
        rotate(if (back) 180f else 0f) {
            drawLine(color, Offset(w * 0.12f, w / 2), Offset(w * 0.88f, w / 2), s, StrokeCap.Round)
            drawLine(color, Offset(w * 0.58f, w * 0.2f), Offset(w * 0.88f, w / 2), s, StrokeCap.Round)
            drawLine(color, Offset(w * 0.58f, w * 0.8f), Offset(w * 0.88f, w / 2), s, StrokeCap.Round)
        }
    }
}

@Composable
fun CloseIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(18.dp)) {
        val w = size.width
        val s = 1.8.dp.toPx()
        drawLine(color, Offset(w * 0.2f, w * 0.2f), Offset(w * 0.8f, w * 0.8f), s, StrokeCap.Round)
        drawLine(color, Offset(w * 0.8f, w * 0.2f), Offset(w * 0.2f, w * 0.8f), s, StrokeCap.Round)
    }
}

@Composable
fun PlusCheckIcon(color: Color, checked: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier.size(16.dp)) {
        val w = size.width
        val s = 1.8.dp.toPx()
        if (checked) {
            drawLine(color, Offset(w * 0.15f, w * 0.52f), Offset(w * 0.4f, w * 0.78f), s, StrokeCap.Round)
            drawLine(color, Offset(w * 0.4f, w * 0.78f), Offset(w * 0.86f, w * 0.24f), s, StrokeCap.Round)
        } else {
            drawLine(color, Offset(w / 2, w * 0.15f), Offset(w / 2, w * 0.85f), s, StrokeCap.Round)
            drawLine(color, Offset(w * 0.15f, w / 2), Offset(w * 0.85f, w / 2), s, StrokeCap.Round)
        }
    }
}

@Composable
fun SpeakerIcon(color: Color, muted: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier.size(20.dp)) {
        val w = size.width
        val s = 1.6.dp.toPx()
        val body = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.1f, w * 0.38f); lineTo(w * 0.3f, w * 0.38f); lineTo(w * 0.52f, w * 0.18f)
            lineTo(w * 0.52f, w * 0.82f); lineTo(w * 0.3f, w * 0.62f); lineTo(w * 0.1f, w * 0.62f); close()
        }
        drawPath(body, color)
        if (muted) {
            drawLine(color, Offset(w * 0.66f, w * 0.38f), Offset(w * 0.9f, w * 0.62f), s, StrokeCap.Round)
            drawLine(color, Offset(w * 0.9f, w * 0.38f), Offset(w * 0.66f, w * 0.62f), s, StrokeCap.Round)
        } else {
            drawArc(color, -45f, 90f, false, Offset(w * 0.36f, w * 0.3f), Size(w * 0.4f, w * 0.4f), style = Stroke(s, cap = StrokeCap.Round))
            drawArc(color, -45f, 90f, false, Offset(w * 0.26f, w * 0.14f), Size(w * 0.72f, w * 0.72f), style = Stroke(s, cap = StrokeCap.Round))
        }
    }
}

/** A dish's leitmotif, engraved on a tiny five-line staff. */
@Composable
fun MotifStaff(
    motif: List<Int>,
    color: Color,
    lineColor: Color,
    modifier: Modifier = Modifier,
    highlight: Int = -1,
) {
    Canvas(modifier.size(76.dp, 30.dp)) {
        val gap = size.height / 6f
        for (i in 1..5) drawLine(lineColor, Offset(0f, gap * i), Offset(size.width, gap * i), 1.dp.toPx())
        val headW = gap * 1.4f
        val headH = gap * 0.98f
        val spacing = (size.width - headW * 2.2f) / (motif.size - 1).coerceAtLeast(1)
        motif.forEachIndexed { i, p ->
            val degree = intArrayOf(0, 1, 2, 4, 5)[p % 5] + 7 * (p / 5)
            val x = headW * 1.1f + spacing * i
            val y = gap * 5.5f - degree * gap / 2f
            val scale = if (i == highlight) 1.35f else 1f
            val c = if (highlight >= 0 && i != highlight) color.copy(alpha = 0.45f) else color
            rotate(-22f, Offset(x, y)) {
                drawOval(c, Offset(x - headW * scale / 2, y - headH * scale / 2), Size(headW * scale, headH * scale))
            }
            val stemX = x + headW * scale / 2 - 0.6.dp.toPx()
            drawLine(c, Offset(stemX, y - 1f), Offset(stemX, y - gap * 2.7f), 1.2.dp.toPx())
        }
    }
}

/**
 * The score: one staff per dish, blocks for steps (solid = your hands, hatched = the heat),
 * a lane proving you never hold two tasks at once, and a double bar where every dish lands.
 */
@Composable
fun ScoreTimeline(
    score: Score,
    modifier: Modifier = Modifier,
    now: Float? = null,
    dark: Boolean = false,
    rowHeight: Dp = 36.dp,
    showAxis: Boolean = true,
    showLane: Boolean = true,
) {
    val measurer = rememberTextMeasurer()
    val ink = if (dark) Palette.chalk else Palette.ink
    val faint = if (dark) Palette.stageLine else Palette.rule
    val soft = if (dark) Palette.chalkSoft else Palette.inkSoft
    val accent = if (dark) Palette.batonLit else Palette.baton
    val ground = if (dark) Palette.stage else Palette.paper
    val rows = score.recipes.size
    val axisH = if (showAxis) 22.dp else 4.dp
    val laneH = if (showLane) 28.dp else 0.dp

    val description = remember(score) {
        buildString {
            append("Score for ${score.recipes.size} dishes: ${(score.tutti + 59) / 60} minutes to the final chord, ")
            append("${(score.handsOnSeconds + 59) / 60} minutes hands-on, never two tasks at once. ")
            score.recipes.forEachIndexed { d, recipe ->
                append("${recipe.name} on ${recipe.instrument.label.lowercase()}, ${score.slotsFor(d).size} steps. ")
            }
        }
    }
    Canvas(modifier.fillMaxWidth().height(axisH + rowHeight * rows + laneH).semantics { contentDescription = description }) {
        val gutter = 26.dp.toPx()
        val right = size.width - 6.dp.toPx()
        val total = maxOf(score.tutti, score.slots.maxOfOrNull { it.end } ?: 1).toFloat().coerceAtLeast(60f)
        fun x(t: Float) = gutter + (right - gutter) * (t / total)
        val top = axisH.toPx()
        val rh = rowHeight.toPx()
        val bottom = top + rh * rows + laneH.toPx()
        val tickStyle = TextStyle(fontFamily = Fonts.mono, fontSize = 9.sp, color = soft)

        if (showAxis) {
            val every = if (total > 3000) 10 else 5
            var m = 0
            while (m * 60 <= total) {
                val xx = x(m * 60f)
                drawLine(faint, Offset(xx, top - 3.dp.toPx()), Offset(xx, bottom), 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 6f)))
                if (x(total) - xx > 44.dp.toPx()) drawText(measurer, "$m′", Offset(xx + 3.dp.toPx(), 0f), tickStyle)
                m += every
            }
        }

        score.recipes.forEachIndexed { d, recipe ->
            val cy = top + rh * d + rh / 2
            drawLine(faint, Offset(gutter, cy), Offset(right, cy), 1f)
            drawText(measurer, roman(d), Offset(0f, cy - 10.dp.toPx()),
                TextStyle(fontFamily = Fonts.serif, fontSize = 16.sp, color = recipe.color))
            for (slot in score.slotsFor(d)) {
                val step = recipe.steps[slot.step]
                val x0 = x(slot.start.toFloat())
                val x1 = maxOf(x(slot.end.toFloat()), x0 + 2.dp.toPx())
                if (step.handsOn) {
                    val h = rh * 0.5f
                    drawRoundRect(recipe.color, Offset(x0, cy - h / 2), Size(x1 - x0, h), CornerRadius(3.dp.toPx()))
                } else {
                    val h = rh * 0.32f
                    drawRoundRect(recipe.color.copy(alpha = 0.2f), Offset(x0, cy - h / 2), Size(x1 - x0, h), CornerRadius(2.dp.toPx()))
                    clipRect(x0, cy - h / 2, x1, cy + h / 2) {
                        var hx = x0 - h
                        while (hx < x1) {
                            drawLine(recipe.color.copy(alpha = 0.7f), Offset(hx, cy + h / 2), Offset(hx + h, cy - h / 2), 1.dp.toPx())
                            hx += 4.dp.toPx()
                        }
                    }
                }
            }
        }

        if (showLane) {
            val ly = top + rh * rows + laneH.toPx() / 2
            drawText(measurer, "you", Offset(0f, ly - 7.dp.toPx()), tickStyle)
            drawLine(faint, Offset(gutter, ly), Offset(right, ly), 1f)
            for (slot in score.slots) {
                if (!score.stepOf(slot).handsOn) continue
                val x0 = x(slot.start.toFloat())
                drawRoundRect(score.recipes[slot.dish].color, Offset(x0, ly - 4.dp.toPx()),
                    Size(maxOf(x(slot.end.toFloat()) - x0, 3.dp.toPx()), 8.dp.toPx()), CornerRadius(4.dp.toPx()))
            }
        }

        if (now != null) {
            val px = x(now.coerceIn(0f, total))
            drawRect(ground.copy(alpha = 0.55f), Offset(gutter, top - 3.dp.toPx()), Size(px - gutter, bottom - top + 3.dp.toPx()))
            drawLine(accent, Offset(px, top - 4.dp.toPx()), Offset(px, bottom), 2.dp.toPx())
            drawCircle(accent, 3.5.dp.toPx(), Offset(px, top - 4.dp.toPx()))
        }

        val tx = x(score.tutti.toFloat())
        drawLine(ink, Offset(tx - 4.dp.toPx(), top - 2.dp.toPx()), Offset(tx - 4.dp.toPx(), bottom), 1.dp.toPx())
        drawLine(ink, Offset(tx, top - 2.dp.toPx()), Offset(tx, bottom), 3.dp.toPx())
        if (showAxis) {
            val layout = measurer.measure("TUTTI", TextStyle(fontFamily = Fonts.mono, fontWeight = FontWeight.Medium, fontSize = 9.sp, color = accent))
            drawText(layout, topLeft = Offset(tx - layout.size.width - 2.dp.toPx(), 0f))
        }
    }
}
