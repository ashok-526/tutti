package app.tutti.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tutti.schedule.Score
import app.tutti.schedule.Slot
import app.tutti.session.Performance
import app.tutti.session.Phase
import app.tutti.session.TuttiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.floor
import kotlin.math.pow

@Composable
fun ConductScreen(state: TuttiState, performance: Performance) {
    LaunchedEffect(performance) { performance.run() }
    LaunchedEffect(performance.finished) {
        if (performance.finished) {
            delay(3400)
            state.finale()
        }
    }

    val colors = tutti
    val motion = animationsEnabled()
    val score = performance.score
    val now = performance.now
    val current = performance.currentHandsOn()
    val total = maxOf(score.tutti, score.slots.maxOf { it.end }).toFloat()

    var beat by remember { mutableDoubleStateOf(0.0) }
    val spin = remember { Animatable(0f) }
    LaunchedEffect(performance.finished, motion) {
        if (performance.finished) {
            if (motion) spin.animateTo(spin.value + 70f, tween(2600, easing = Motion.out))
            return@LaunchedEffect
        }
        while (isActive) {
            withFrameMillis { beat = state.orchestra.beatClock }
            if (motion) spin.snapTo(((beat * 45.0) % 360.0).toFloat())
        }
    }

    val drop = remember { Animatable(0f) }
    LaunchedEffect(performance, performance.finished) {
        if (!performance.finished) {
            if (!motion) { drop.snapTo(1f); return@LaunchedEffect }
            delay(300)
            drop.animateTo(1f, tween(2200, easing = Motion.inOut))
        } else {
            delay(1000)
            if (motion) drop.animateTo(0f, tween(1000, easing = Motion.inOut)) else drop.snapTo(0f)
        }
    }
    val arm by animateFloatAsState(
        targetValue = if (performance.finished) 1f else (now / total).coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 1f, stiffness = Spring.StiffnessMediumLow),
        label = "arm",
    )

    val glow = remember { Animatable(0f) }
    val flash = performance.flash
    LaunchedEffect(flash) {
        if (flash != null) {
            glow.snapTo(1f)
            glow.animateTo(0f, tween(1400, easing = Motion.out))
        }
    }

    val paper by animateColorAsState(
        targetValue = current?.let { score.recipes[it.dish].color } ?: colors.label,
        animationSpec = tween(220, easing = Motion.out),
        label = "label",
    )

    var shownNotice by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(performance.rescored) {
        shownNotice = performance.notice
        delay(3600)
        shownNotice = null
    }
    var muted by remember { mutableStateOf(state.orchestra.muted) }

    val label = when {
        !performance.started -> DiscLabel(top = "SIDE A", main = "${performance.countIn.coerceAtLeast(1)}", sub = "COUNT IN", bottom = "NEEDLE DOWN")
        performance.finished -> DiscLabel(top = "SIDE A", main = "Tutti", sub = "SERVE NOW", bottom = "EVERY DISH READY")
        else -> DiscLabel(
            top = "SERVE AT ${clockTime(((score.tutti - now) / performance.speed).toInt())}",
            main = formatClock((score.tutti - now).toInt()),
            sub = "TO SERVE",
            bottom = current?.let { "${score.recipes[it.dish].instrument.label.uppercase()} NEEDS YOU" } ?: "HANDS FREE",
        )
    }

    Box(Modifier.fillMaxSize().background(colors.plinth)) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                GlyphButton(Glyphs.Close, "Stop the performance", onClick = { state.back() })
                Text(
                    if (performance.speed > 1f) "REHEARSAL  ${performance.speed.toInt()}×" else "LIVE",
                    style = Type.label.copy(color = colors.inkMuted),
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                )
                GlyphButton(
                    if (muted) Glyphs.VolumeOff else Glyphs.VolumeOn,
                    if (muted) "Unmute the orchestra" else "Mute the orchestra",
                    onClick = {
                        muted = !muted
                        state.orchestra.muted = muted
                    },
                )
            }

            Turntable(
                score = score,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .semantics { contentDescription = "Record playing. ${formatClock((score.tutti - now).toInt())} until serving." },
                label = label,
                labelPaper = paper,
                center = { Offset(it.width * 0.45f, it.height * 0.52f) },
                radius = { minOf(it.width * 0.39f, it.height * 0.43f) },
                now = { if (performance.started) now else null },
                rotation = { spin.value },
                strobe = { ((1.0 - (beat - floor(beat))).pow(3.0)).toFloat() },
                arm = { arm },
                armDrop = { drop.value },
                cueLit = { current != null },
                flashDish = flash?.dish ?: -1,
                flash = { glow.value },
            )

            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())) {
                AnimatedContent(
                    targetState = when {
                        !performance.started -> "countin"
                        performance.finished -> "done"
                        current != null -> "hands:${current.dish}:${current.step}"
                        else -> "free"
                    },
                    transitionSpec = {
                        (fadeIn(tween(220, easing = Motion.out)) + slideInVertically(tween(260, easing = Motion.out)) { it / 8 }) togetherWith
                            fadeOut(tween(120))
                    },
                    label = "now",
                ) { key ->
                    when {
                        key == "countin" -> Moment("Needle down.", "The first cue lands on the downbeat. Listen for each dish's theme.")
                        key == "done" -> Moment("Every dish is ready.", "The final chord is playing. Serve.")
                        key.startsWith("hands") -> {
                            val slot = score.slots.firstOrNull { "hands:${it.dish}:${it.step}" == key }
                            if (slot != null) HandsPanel(score, slot, now) { beat } else FreePanel(performance, now)
                        }
                        else -> FreePanel(performance, now)
                    }
                }
                Column(Modifier.padding(start = 24.dp, end = 20.dp, top = 20.dp, bottom = 8.dp)) {
                    score.recipes.indices.forEach { d -> DishLine(performance, d) }
                }
            }

            val enabled = current != null && performance.started && !performance.finished
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 4.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SecondaryButton("+1 minute", onClick = { performance.needMore() }, enabled = enabled, modifier = Modifier.weight(1f))
                PrimaryButton("Done", onClick = { performance.done() }, enabled = enabled, signal = true, modifier = Modifier.weight(1f))
            }
        }

        AnimatedVisibility(
            visible = shownNotice != null,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 148.dp, start = 16.dp, end = 16.dp),
            enter = fadeIn(tween(200, easing = Motion.out)) + slideInVertically(tween(240, easing = Motion.out)) { it / 2 },
            exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { it / 3 },
        ) {
            Snackbar(
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                containerColor = colors.ink,
                contentColor = colors.onInk,
                shape = CircleShape,
            ) {
                Text(shownNotice ?: "", style = Type.button.copy(color = colors.onInk))
            }
        }
    }
}

@Composable
private fun Moment(headline: String, body: String) {
    val colors = tutti
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
        Text(headline, style = Type.headline.copy(color = colors.ink), modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        Text(body, style = Type.body.copy(color = colors.inkMuted), modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun HandsPanel(score: Score, slot: Slot, now: Float, beat: () -> Double) {
    val colors = tutti
    val recipe = score.recipes[slot.dish]
    val step = recipe.steps[slot.step]
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Tag("Your hands", cue = true)
            LabelMark(recipe.color, size = 16.dp, modifier = Modifier.padding(start = 12.dp))
            Text(
                "${recipe.instrument.label}, ${recipe.name}",
                style = Type.bodySmall.copy(color = colors.inkMuted),
                modifier = Modifier.padding(start = 6.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            step.title,
            style = Type.headline.copy(color = colors.ink, fontSize = 34.sp, lineHeight = 38.sp),
            modifier = Modifier.padding(top = 12.dp).semantics { liveRegion = LiveRegionMode.Polite },
            maxLines = 2,
        )
        Text(step.detail, style = Type.body.copy(color = colors.inkMuted), modifier = Modifier.padding(top = 6.dp), maxLines = 2)
        Row(Modifier.padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(step.action.marking, style = Type.time.copy(color = colors.ink))
            Text(step.action.hint, style = Type.bodySmall.copy(color = colors.inkMuted), modifier = Modifier.padding(start = 10.dp).weight(1f), maxLines = 1)
            BeatPips(beat)
            Text(
                formatClock((slot.end - now).toInt()),
                style = Type.time.copy(color = colors.ink),
                modifier = Modifier.padding(start = 14.dp),
            )
        }
    }
}

@Composable
private fun FreePanel(performance: Performance, now: Float) {
    val colors = tutti
    val score = performance.score
    val next = score.cues.firstOrNull { it.start > now && score.stepOf(it).handsOn }
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
        Tag("Hands free", modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        if (next != null) {
            val recipe = score.recipes[next.dish]
            Text(
                "Next: ${recipe.steps[next.step].title}",
                style = Type.headline.copy(color = colors.ink, fontSize = 34.sp, lineHeight = 38.sp),
                modifier = Modifier.padding(top = 12.dp),
                maxLines = 2,
            )
            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                LabelMark(recipe.color, size = 16.dp)
                Text(
                    "The ${recipe.instrument.label.lowercase()} comes in with its theme in ${formatClock((next.start - now).toInt())}.",
                    style = Type.body.copy(color = colors.inkMuted),
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        } else {
            Text(
                "The heat finishes it.",
                style = Type.headline.copy(color = colors.ink, fontSize = 34.sp, lineHeight = 38.sp),
                modifier = Modifier.padding(top = 12.dp),
            )
            Text("Nothing left for your hands. Warm the plates.", style = Type.body.copy(color = colors.inkMuted), modifier = Modifier.padding(top = 8.dp))
        }
    }
}

/** Four pips, one per beat of the bar: the downbeat you can chop to. */
@Composable
private fun BeatPips(beat: () -> Double) {
    val colors = tutti
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(4) { i ->
            Box(
                Modifier
                    .size(8.dp)
                    .graphicsLayer {
                        val b = beat()
                        val lit = floor(b).toInt().mod(4) == i
                        val phase = (b - floor(b)).toFloat()
                        val s = if (lit) 1f + 0.5f * (1f - phase) * (1f - phase) else 1f
                        scaleX = s
                        scaleY = s
                        alpha = if (lit) 1f else 0.28f
                    }
                    .clip(CircleShape)
                    .background(colors.ink),
            )
        }
    }
}

/** A dish's state, told four ways: waiting, cooking, needs you, holding. */
@Composable
private fun DishLine(performance: Performance, dish: Int) {
    val colors = tutti
    val recipe = performance.score.recipes[dish]
    val status = performance.dishNow(dish)
    val step = status.slot?.let { recipe.steps[it.step] }
    val detail = when (status.phase) {
        Phase.ACTIVE, Phase.COOKING -> step?.title ?: ""
        Phase.WAITING -> "Starts with ${step?.title ?: "its first step"}"
        Phase.READY -> "Holding for the final chord"
    }
    val background: Color
    val content: Color
    val border: Color
    val reading: String
    when (status.phase) {
        Phase.ACTIVE -> { background = colors.cue; content = colors.onCue; border = colors.cueEdge; reading = "hands" }
        Phase.COOKING -> { background = colors.sunken; content = colors.ink; border = Color.Transparent; reading = formatClock(status.secondsLeft) }
        Phase.WAITING -> { background = Color.Transparent; content = colors.inkMuted; border = colors.line; reading = "in ${formatClock(status.secondsLeft)}" }
        Phase.READY -> { background = colors.ink; content = colors.onInk; border = Color.Transparent; reading = "ready" }
    }
    val description = when (status.phase) {
        Phase.ACTIVE -> "${recipe.name} needs your hands"
        Phase.COOKING -> "${recipe.name} cooking, ${formatClock(status.secondsLeft)} left"
        Phase.WAITING -> "${recipe.name} starts in ${formatClock(status.secondsLeft)}"
        Phase.READY -> "${recipe.name} ready"
    }
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .semantics(mergeDescendants = true) { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LabelMark(recipe.color, size = 24.dp)
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(recipe.instrument.label, style = Type.button.copy(color = colors.ink))
            Text(
                detail,
                style = Type.bodySmall.copy(color = colors.inkMuted),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(
            Modifier
                .clip(CircleShape)
                .background(background)
                .border(1.dp, border, CircleShape)
                .padding(horizontal = 12.dp, vertical = 5.dp),
        ) {
            Text(reading, style = Type.time.copy(color = content, fontSize = 17.sp))
        }
    }
}
