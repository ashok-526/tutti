package app.tutti.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tutti.model.Recipe
import app.tutti.session.TuttiState

@Composable
fun FinaleScreen(state: TuttiState) {
    val performance = state.performance
    val score = performance?.score ?: state.score ?: return
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) { appear.animateTo(1f, tween(1400, easing = FastOutSlowInEasing)) }

    Box(Modifier.fillMaxSize().background(Palette.paper)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
        ) {
            Row(Modifier.padding(top = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(Palette.baton))
                Spacer(Modifier.width(8.dp))
                Label("Tutti", Palette.ink)
                Spacer(Modifier.weight(1f))
                Label("Performance complete", Palette.inkFaint)
            }
            Spacer(Modifier.height(12.dp))
            Hairline(color = Palette.ink)
            Spacer(Modifier.height(3.dp))
            Hairline(color = Palette.ink)

            FinalChord(score.recipes, appear.value, Modifier.padding(top = 36.dp))

            Column(Modifier.graphicsLayer { alpha = appear.value; translationY = (1f - appear.value) * 40f }) {
                BasicText("Tutti.", style = Type.display.copy(fontSize = 104.sp, lineHeight = 100.sp, color = Palette.ink))
                BasicText("Every dish, together.", style = Type.italic.copy(fontSize = 30.sp, lineHeight = 34.sp, color = Palette.baton))
                Spacer(Modifier.height(16.dp))
                BasicText(
                    "${score.recipes.size} dishes reached the table on one final chord, cued by ear. " +
                        "No timers, no juggling, no cold sides.",
                    style = Type.body.copy(color = Palette.inkSoft),
                )
            }

            Spacer(Modifier.height(30.dp))
            Hairline()
            Row(Modifier.padding(vertical = 16.dp)) {
                FinaleStat("Conducted", minutesLabel(score.tutti), Modifier.weight(1f))
                FinaleStat("Cues", "${performance?.cuesPlayed ?: score.slots.size}", Modifier.weight(1f))
                FinaleStat("Re-scored", "${performance?.rescored ?: 0}×", Modifier.weight(1f))
            }
            Hairline()
            Spacer(Modifier.height(24.dp))
            ScoreTimeline(score)

            Spacer(Modifier.height(36.dp))
            Label("The encore", Palette.ink)
            Spacer(Modifier.height(8.dp))
            Hairline(color = Palette.ink)
            Spacer(Modifier.height(16.dp))
            BasicText("Tonight’s dinner, as a song.", style = Type.heading.copy(color = Palette.ink))
            Spacer(Modifier.height(6.dp))
            BasicText(
                "Each leitmotif enters in the order you cooked it, then the whole orchestra lands the final chord.",
                style = Type.body.copy(color = Palette.inkSoft),
            )
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                PillButton("Play the encore", onClick = { state.playEncore() }, container = Palette.baton)
                Spacer(Modifier.width(10.dp))
                GhostButton("New menu", onClick = { state.newProgramme() }, color = Palette.ink)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun FinaleStat(label: String, value: String, modifier: Modifier) {
    Column(modifier) {
        Label(label, Palette.inkFaint)
        Spacer(Modifier.height(4.dp))
        BasicText(value, style = Type.heading.copy(color = Palette.ink, fontSize = 26.sp))
    }
}

/** The last bar: every dish stacked into one chord under a fermata, then a double bar. */
@Composable
private fun FinalChord(recipes: List<Recipe>, reveal: Float, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxWidth().height(150.dp)) {
        val gap = 12.dp.toPx()
        val staffTop = size.height - gap * 6.5f
        val line = 1.dp.toPx()
        for (i in 0 until 5) {
            val y = staffTop + gap * i
            drawLine(Palette.ink, Offset(0f, y), Offset(size.width * reveal, y), line)
        }
        val chordX = size.width * 0.62f
        val degrees = intArrayOf(0, 2, 4, 7, 9, 11, 14)
        val headW = gap * 1.45f
        val headH = gap * 1.02f
        recipes.forEachIndexed { i, recipe ->
            val t = ((reveal - i * 0.08f) / 0.5f).coerceIn(0f, 1f)
            if (t <= 0f) return@forEachIndexed
            val deg = degrees[i % degrees.size]
            val y = staffTop + gap * 4.5f - deg * gap / 2f
            val x = chordX + if (i % 2 == 1 && deg - degrees[(i - 1).coerceAtLeast(0)] < 2) headW else 0f
            rotate(-22f, Offset(x, y)) {
                drawOval(recipe.color.copy(alpha = t), Offset(x - headW / 2, y - headH / 2 - (1f - t) * 30f), Size(headW, headH))
            }
        }
        val topY = staffTop + gap * 4.5f - degrees[(recipes.size - 1).coerceAtLeast(0) % degrees.size] * gap / 2f
        val fermataY = minOf(topY, staffTop) - gap * 2.2f
        if (reveal > 0.7f) {
            val a = ((reveal - 0.7f) / 0.3f).coerceIn(0f, 1f)
            drawArc(
                Palette.ink.copy(alpha = a), 180f, 180f, false,
                Offset(chordX - gap * 1.6f, fermataY - gap * 0.4f), Size(gap * 3.2f, gap * 3f),
                style = Stroke(2.2.dp.toPx(), cap = StrokeCap.Round),
            )
            drawCircle(Palette.baton.copy(alpha = a), gap * 0.3f, Offset(chordX, fermataY + gap * 0.9f))
        }
        val barX = size.width * reveal - 2.dp.toPx()
        if (reveal > 0.95f) {
            drawLine(Palette.ink, Offset(barX - 6.dp.toPx(), staffTop), Offset(barX - 6.dp.toPx(), staffTop + gap * 4), line)
            drawLine(Palette.ink, Offset(barX, staffTop), Offset(barX, staffTop + gap * 4), 4.dp.toPx())
        }
    }
}
