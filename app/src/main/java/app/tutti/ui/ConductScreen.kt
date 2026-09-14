package app.tutti.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tutti.schedule.Score
import app.tutti.schedule.Slot
import app.tutti.session.Performance
import app.tutti.session.Phase
import app.tutti.session.TuttiState
import kotlinx.coroutines.delay
import kotlin.math.floor

@Composable
fun ConductScreen(state: TuttiState, performance: Performance) {
    LaunchedEffect(performance) { performance.run() }
    LaunchedEffect(performance.finished) {
        if (performance.finished) {
            delay(3200)
            state.finale()
        }
    }

    var beat by remember { mutableDoubleStateOf(0.0) }
    LaunchedEffect(Unit) { while (true) withFrameMillis { beat = state.orchestra.beatClock } }
    val beatNow = { beat }

    val score = performance.score
    val now = performance.now
    val current = performance.currentHandsOn()

    val glow = remember { Animatable(0f) }
    val flash = performance.flash
    LaunchedEffect(flash) {
        if (flash != null) {
            glow.snapTo(1f)
            glow.animateTo(0f, tween(1600, easing = LinearOutSlowInEasing))
        }
    }
    val flashColor = flash?.let { score.recipes[it.dish].color } ?: Color.Transparent

    var shownNotice by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(performance.rescored) {
        shownNotice = performance.notice
        delay(3400)
        shownNotice = null
    }
    var muted by remember { mutableStateOf(state.orchestra.muted) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Palette.stage)
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        listOf(flashColor.copy(alpha = 0.32f * glow.value), Color.Transparent),
                        center = Offset(size.width * 0.5f, size.height * 0.48f),
                        radius = size.width * 1.1f,
                    ),
                )
            },
    ) {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).pressable { state.back() }, contentAlignment = Alignment.Center) {
                    CloseIcon(Palette.chalkSoft)
                }
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier.size(6.dp).graphicsLayer {
                        alpha = 0.4f + 0.6f * (1f - (beatNow() % 1.0).toFloat())
                    }.clip(CircleShape).background(Palette.batonLit),
                )
                Spacer(Modifier.width(8.dp))
                Label(if (performance.speed > 1f) "Rehearsal · ${performance.speed.toInt()}×" else "Live", Palette.chalkSoft)
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier.size(44.dp).pressable {
                        muted = !muted
                        state.orchestra.muted = muted
                    },
                    contentAlignment = Alignment.Center,
                ) { SpeakerIcon(Palette.chalkSoft, muted) }
            }

            Row(Modifier.padding(horizontal = 24.dp).padding(top = 6.dp), verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Label("Tutti in", Palette.chalkFaint)
                    BasicText(formatClock((score.tutti - now).toInt()), style = Type.clock.copy(color = Palette.chalk))
                }
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(bottom = 14.dp)) {
                    Label("Tempo", Palette.chalkFaint)
                    BasicText(
                        "${state.orchestra.bpm.toInt()} bpm",
                        style = Type.mono.copy(color = Palette.chalkSoft),
                    )
                }
            }

            ScoreTimeline(
                score, Modifier.padding(horizontal = 18.dp).padding(top = 4.dp),
                now = now, dark = true, rowHeight = 20.dp, showAxis = false, showLane = false,
            )

            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(22.dp))
                AnimatedContent(
                    targetState = current?.let { it.dish * 1000 + it.step } ?: -1,
                    transitionSpec = {
                        (fadeIn(tween(420)) + slideInVertically(tween(420)) { it / 6 }) togetherWith fadeOut(tween(180))
                    },
                    label = "now",
                ) { key ->
                    val slot = score.slots.firstOrNull { it.dish * 1000 + it.step == key }
                    if (slot != null) HandsPanel(score, slot, now, beatNow) else FreePanel(performance, now)
                }
                Spacer(Modifier.height(22.dp))
                Column(Modifier.padding(horizontal = 24.dp)) {
                    Label("The orchestra", Palette.chalkFaint)
                    Spacer(Modifier.height(8.dp))
                    score.recipes.indices.forEach { d -> DishLine(performance, d, beatNow) }
                }
                val upNext = performance.upcoming(3)
                if (upNext.isNotEmpty()) {
                    Spacer(Modifier.height(18.dp))
                    Column(Modifier.padding(horizontal = 24.dp)) {
                        Label("Up next", Palette.chalkFaint)
                        Spacer(Modifier.height(6.dp))
                        upNext.forEach { slot ->
                            val recipe = score.recipes[slot.dish]
                            val step = recipe.steps[slot.step]
                            Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                                BasicText(
                                    "in ${formatClock((slot.start - now).toInt())}",
                                    style = Type.mono.copy(color = Palette.chalkSoft, fontSize = 12.sp),
                                    modifier = Modifier.width(78.dp),
                                )
                                Box(Modifier.size(7.dp).clip(CircleShape).background(recipe.color))
                                Spacer(Modifier.width(12.dp))
                                BasicText(
                                    step.title,
                                    style = Type.heading.copy(fontSize = 19.sp, lineHeight = 22.sp, color = Palette.chalk),
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                )
                                Label(if (step.handsOn) "Hands" else "Heat", if (step.handsOn) Palette.chalkSoft else Palette.chalkFaint)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val enabled = current != null && performance.started && !performance.finished
                Box(Modifier.weight(1f).graphicsLayer { alpha = if (enabled) 1f else 0.35f }) {
                    GhostButton("+1 minute", onClick = { if (enabled) performance.needMore() }, color = Palette.chalk, modifier = Modifier.fillMaxWidth())
                }
                Box(Modifier.weight(1f)) {
                    if (enabled) {
                        PillButton(
                            "Done", onClick = { performance.done() }, arrow = false,
                            container = Palette.chalk, content = Palette.stage, modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        GhostButton("Done", onClick = {}, color = Palette.chalkFaint, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = shownNotice != null,
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 60.dp),
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut(),
        ) {
            BasicText(
                shownNotice ?: "",
                style = Type.small.copy(color = Palette.stage, fontWeight = FontWeight.Medium),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Palette.chalk)
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            )
        }

        AnimatedVisibility(
            visible = !performance.started,
            enter = fadeIn(),
            exit = fadeOut(tween(700)),
        ) {
            Box(Modifier.fillMaxSize().background(Palette.stage), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Label("Count-in", Palette.chalkFaint)
                    AnimatedContent(
                        targetState = performance.countIn,
                        transitionSpec = { (scaleIn(initialScale = 1.5f) + fadeIn(tween(160))) togetherWith fadeOut(tween(120)) },
                        label = "count",
                    ) { n ->
                        BasicText(
                            if (n == 0) " " else "$n",
                            style = Type.display.copy(fontSize = 180.sp, lineHeight = 190.sp, color = if (n == 4) Palette.batonLit else Palette.chalk),
                        )
                    }
                    BasicText("The first cue lands on the downbeat.", style = Type.italic.copy(color = Palette.chalkSoft, fontSize = 20.sp))
                }
            }
        }

        AnimatedVisibility(
            visible = performance.finished,
            enter = fadeIn(tween(900)),
            exit = fadeOut(),
        ) {
            Box(Modifier.fillMaxSize().background(Palette.stage.copy(alpha = 0.92f)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BasicText("Tutti.", style = Type.display.copy(fontSize = 110.sp, lineHeight = 110.sp, color = Palette.chalk))
                    BasicText("Everything, together. Serve.", style = Type.italic.copy(color = Palette.batonLit, fontSize = 24.sp))
                }
            }
        }
    }
}

@Composable
private fun HandsPanel(score: Score, slot: Slot, now: Float, beat: () -> Double) {
    val recipe = score.recipes[slot.dish]
    val step = recipe.steps[slot.step]
    val left = (slot.end - now).toInt()
    val progress = ((now - slot.start) / slot.length.coerceAtLeast(1)).coerceIn(0f, 1f)

    Column(Modifier.padding(horizontal = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(9.dp).clip(CircleShape).background(recipe.color))
            Spacer(Modifier.width(10.dp))
            Label("Your hands · ${recipe.instrument.label}", Palette.chalkSoft)
        }
        Spacer(Modifier.height(12.dp))
        BasicText(step.title, style = Type.title.copy(fontSize = 44.sp, lineHeight = 46.sp, color = Palette.chalk))
        Spacer(Modifier.height(8.dp))
        BasicText(step.detail, style = Type.body.copy(color = Palette.chalkSoft, fontSize = 16.sp))
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicText(step.action.marking, style = Type.italic.copy(color = recipe.color, fontSize = 26.sp))
            BasicText("  ${step.action.hint}", style = Type.small.copy(color = Palette.chalkSoft), modifier = Modifier.weight(1f))
            BeatDots(beat, recipe.color)
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(50)).background(Palette.stageLine)) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(progress).background(recipe.color))
            }
            Spacer(Modifier.width(12.dp))
            BasicText("${formatClock(left)} left", style = Type.mono.copy(color = Palette.chalkSoft, fontSize = 12.sp))
        }
    }
}

@Composable
private fun FreePanel(performance: Performance, now: Float) {
    val score = performance.score
    val next = score.cues.firstOrNull { it.start > now && score.stepOf(it).handsOn }
    Column(Modifier.padding(horizontal = 24.dp)) {
        Label("Your hands are free", Palette.chalkSoft)
        Spacer(Modifier.height(12.dp))
        if (next != null) {
            val recipe = score.recipes[next.dish]
            BasicText("Listen for the", style = Type.title.copy(fontSize = 44.sp, lineHeight = 46.sp, color = Palette.chalk))
            BasicText(recipe.instrument.label.lowercase() + ".", style = Type.title.copy(fontSize = 44.sp, lineHeight = 46.sp, color = recipe.color, fontStyle = FontStyle.Italic))
            Spacer(Modifier.height(10.dp))
            BasicText(
                "${recipe.steps[next.step].title} in ${formatClock((next.start - now).toInt())}. Its leitmotif will build as the moment comes.",
                style = Type.body.copy(color = Palette.chalkSoft, fontSize = 16.sp),
            )
        } else {
            BasicText("Let the heat", style = Type.title.copy(fontSize = 44.sp, lineHeight = 46.sp, color = Palette.chalk))
            BasicText("finish the piece.", style = Type.title.copy(fontSize = 44.sp, lineHeight = 46.sp, color = Palette.batonLit, fontStyle = FontStyle.Italic))
            Spacer(Modifier.height(10.dp))
            BasicText("Nothing left for your hands. Warm the plates.", style = Type.body.copy(color = Palette.chalkSoft, fontSize = 16.sp))
        }
    }
}

@Composable
private fun BeatDots(beat: () -> Double, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(4) { i ->
            Box(
                Modifier
                    .size(8.dp)
                    .graphicsLayer {
                        val b = beat()
                        val lit = floor(b).toInt().mod(4) == i
                        val phase = (b - floor(b)).toFloat()
                        val s = if (lit) 1f + 0.6f * (1f - phase) * (1f - phase) else 1f
                        scaleX = s
                        scaleY = s
                        alpha = if (lit) 1f else 0.25f
                    }
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}

@Composable
private fun DishLine(performance: Performance, dish: Int, beat: () -> Double) {
    val score = performance.score
    val recipe = score.recipes[dish]
    val status = performance.dishNow(dish)
    val step = status.slot?.let { recipe.steps[it.step] }
    val (line, aside) = when (status.phase) {
        Phase.READY -> "Ready, holding for the chord" to "✓"
        Phase.ACTIVE -> (step?.title ?: "") to "hands"
        Phase.COOKING -> (step?.title ?: "") to formatClock(status.secondsLeft)
        Phase.WAITING -> "Enters with “${step?.title ?: ""}”" to "in ${formatClock(status.secondsLeft)}"
    }
    val sounding = status.phase == Phase.ACTIVE || status.phase == Phase.COOKING

    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .graphicsLayer {
                    val phase = (beat() % 1.0).toFloat()
                    val s = if (sounding) 1f + 0.35f * (1f - phase) * (1f - phase) else 1f
                    scaleX = s
                    scaleY = s
                    alpha = if (status.phase == Phase.WAITING) 0.35f else 1f
                }
                .clip(CircleShape)
                .background(recipe.color),
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            BasicText(
                "${recipe.instrument.label} · ${recipe.name}",
                style = Type.small.copy(color = Palette.chalk, fontWeight = FontWeight.Medium),
            )
            BasicText(line, style = Type.small.copy(color = Palette.chalkSoft), maxLines = 1)
        }
        BasicText(aside, style = Type.mono.copy(color = if (status.phase == Phase.READY) recipe.color else Palette.chalkSoft, fontSize = 12.sp))
    }
}
