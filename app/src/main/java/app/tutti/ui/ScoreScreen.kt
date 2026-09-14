package app.tutti.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tutti.session.TuttiState
import kotlinx.coroutines.delay

@Composable
fun ScoreScreen(state: TuttiState) {
    val score = state.score ?: return
    var auditioning by remember { mutableIntStateOf(-1) }
    var auditionNonce by remember { mutableLongStateOf(0L) }
    var litNote by remember { mutableIntStateOf(-1) }

    LaunchedEffect(auditionNonce) {
        if (auditioning < 0) return@LaunchedEffect
        val notes = score.recipes[auditioning].motif.size
        for (i in 0 until notes) {
            litNote = i
            delay((60_000f / state.orchestra.bpm / 2f).toLong())
        }
        delay(400)
        litNote = -1
        auditioning = -1
    }

    Box(Modifier.fillMaxSize().background(Palette.paper)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).pressable { state.back() }, contentAlignment = Alignment.Center) {
                    ArrowIcon(Palette.ink, back = true)
                }
                Spacer(Modifier.weight(1f))
                Label("The score · D major", Palette.inkSoft)
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.width(44.dp))
            }

            Column(Modifier.padding(horizontal = 24.dp).padding(top = 18.dp, bottom = 26.dp)) {
                BasicText("${(score.tutti + 59) / 60} minutes,", style = Type.title.copy(color = Palette.ink))
                BasicText("one final chord.", style = Type.title.copy(color = Palette.baton, fontStyle = FontStyle.Italic))
            }

            Row(Modifier.padding(horizontal = 24.dp)) {
                Stat("Downbeat", clockTime(0), Modifier.weight(1f))
                Stat("Your hands", minutesLabel(score.handsOnSeconds), Modifier.weight(1f))
                Stat("Tutti", clockTime(score.tutti), Modifier.weight(1f), accent = true)
            }
            Spacer(Modifier.height(26.dp))
            ScoreTimeline(score, Modifier.padding(horizontal = 18.dp))
            Row(Modifier.padding(horizontal = 24.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(18.dp, 9.dp).clip(RoundedCornerShape(2.dp)).background(Palette.ink))
                BasicText("  your hands", style = Type.small.copy(color = Palette.inkSoft))
                Spacer(Modifier.width(18.dp))
                Box(Modifier.size(18.dp, 6.dp).border(1.dp, Palette.inkFaint, RoundedCornerShape(2.dp)))
                BasicText("  the heat", style = Type.small.copy(color = Palette.inkSoft))
                Spacer(Modifier.width(18.dp))
                Box(Modifier.size(3.dp, 12.dp).background(Palette.ink))
                BasicText("  served", style = Type.small.copy(color = Palette.inkSoft))
            }

            SectionTitle("Meet the orchestra", "tap to hear each leitmotif")
            score.recipes.forEachIndexed { d, recipe ->
                val playing = auditioning == d
                val lift by animateDpAsState(if (playing) (-2).dp else 0.dp, spring(dampingRatio = 0.5f), label = "lift")
                Row(
                    Modifier
                        .fillMaxWidth()
                        .pressable {
                            auditioning = d
                            auditionNonce = System.nanoTime()
                            state.audition(d)
                        }
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicText(roman(d), style = Type.heading.copy(fontSize = 22.sp, color = recipe.color), modifier = Modifier.width(40.dp))
                    Column(Modifier.weight(1f).offset(y = lift)) {
                        BasicText(recipe.instrument.label, style = Type.heading.copy(color = Palette.ink))
                        BasicText(recipe.name, style = Type.small.copy(color = Palette.inkSoft))
                    }
                    MotifStaff(recipe.motif, recipe.color, Palette.rule, highlight = if (playing) litNote else -1)
                }
                Hairline(Modifier.padding(horizontal = 24.dp))
            }

            SectionTitle("Cue sheet", "${score.slots.size} cues")
            score.cues.forEach { slot ->
                val recipe = score.recipes[slot.dish]
                val step = recipe.steps[slot.step]
                Row(Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                    BasicText(formatClock(slot.start), style = Type.mono.copy(color = Palette.inkSoft), modifier = Modifier.width(54.dp).padding(top = 5.dp))
                    Box(Modifier.padding(top = 9.dp).size(8.dp).clip(CircleShape).background(recipe.color))
                    Column(Modifier.weight(1f).padding(start = 14.dp)) {
                        BasicText(step.title, style = Type.heading.copy(fontSize = 21.sp, lineHeight = 25.sp, color = Palette.ink))
                        BasicText(
                            "${recipe.instrument.label} · ${step.action.marking} · ${minutesLabel(step.seconds)}",
                            style = Type.small.copy(color = Palette.inkSoft),
                        )
                    }
                    Label(if (step.handsOn) "Hands" else "Heat", if (step.handsOn) Palette.ink else Palette.inkFaint, Modifier.padding(top = 7.dp))
                }
            }
            Spacer(Modifier.height(170.dp))
        }

        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .swallowTouches()
                .background(Brush.verticalGradient(0f to Palette.paper.copy(alpha = 0f), 0.3f to Palette.paper))
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 20.dp, top = 34.dp, bottom = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RehearsalToggle(state.rehearsal) { state.rehearsal = !state.rehearsal }
                Spacer(Modifier.weight(1f))
                PillButton("Raise the baton", onClick = { state.raiseBaton() }, container = Palette.baton)
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier, accent: Boolean = false) {
    Column(modifier) {
        Label(label, Palette.inkFaint)
        Spacer(Modifier.height(4.dp))
        BasicText(value, style = Type.heading.copy(fontSize = 23.sp, color = if (accent) Palette.baton else Palette.ink))
    }
}

@Composable
private fun SectionTitle(title: String, aside: String) {
    Column(Modifier.padding(horizontal = 24.dp).padding(top = 34.dp, bottom = 4.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Label(title, Palette.ink)
            Spacer(Modifier.weight(1f))
            BasicText(aside, style = Type.small.copy(color = Palette.inkFaint, fontStyle = FontStyle.Italic))
        }
        Spacer(Modifier.height(8.dp))
        Hairline(color = Palette.ink)
    }
}

@Composable
private fun RehearsalToggle(on: Boolean, onToggle: () -> Unit) {
    val knob by animateDpAsState(if (on) 16.dp else 2.dp, spring(dampingRatio = 0.6f, stiffness = 700f), label = "knob")
    Row(Modifier.pressable(onClick = onToggle).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(34.dp, 20.dp)
                .clip(RoundedCornerShape(50))
                .background(if (on) Palette.ink else Palette.paperDeep)
                .border(1.dp, if (on) Palette.ink else Palette.rule, RoundedCornerShape(50)),
        ) {
            Box(
                Modifier
                    .offset(x = knob, y = 2.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (on) Palette.paper else Palette.inkFaint),
            )
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Label("Rehearsal", Palette.ink)
            BasicText(if (on) "20× speed" else "real time", style = Type.small.copy(fontSize = 12.sp, color = Palette.inkSoft))
        }
    }
}
