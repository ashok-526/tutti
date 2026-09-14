package app.tutti.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.tutti.session.TuttiState
import kotlinx.coroutines.delay

@Composable
fun ScoreScreen(state: TuttiState) {
    val score = state.score ?: return
    val colors = tutti
    var auditioning by remember { mutableIntStateOf(-1) }
    var nonce by remember { mutableLongStateOf(0L) }
    var litNote by remember { mutableIntStateOf(-1) }
    val flash = remember { Animatable(0f) }

    LaunchedEffect(nonce) {
        if (auditioning < 0) return@LaunchedEffect
        flash.snapTo(1f)
        val notes = score.recipes[auditioning].motif.size
        for (i in 0 until notes) {
            litNote = i
            delay((60_000f / state.orchestra.bpm / 2f).toLong())
        }
        litNote = -1
        flash.animateTo(0f, tween(600, easing = Motion.out))
        auditioning = -1
    }

    Box(Modifier.fillMaxSize().background(colors.plinth)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding()) {
            Row(Modifier.padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                GlyphButton(Glyphs.Back, "Back to the menu", onClick = { state.back() })
                Text("Your record", style = Type.title.copy(color = colors.ink), modifier = Modifier.padding(start = 4.dp))
            }

            Column(Modifier.padding(horizontal = 24.dp).padding(top = 12.dp)) {
                Text("Serve at ${clockTime(score.tutti)}", style = Type.display.copy(color = colors.ink))
                Text(
                    "Start now. Tutti plays the cues, and the needle reaches the label when every dish is ready.",
                    style = Type.body.copy(color = colors.inkMuted),
                    modifier = Modifier.padding(top = 12.dp).widthIn(max = 360.dp),
                )
            }

            Turntable(
                score = score,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 28.dp)
                    .aspectRatio(1f)
                    .semantics {
                        contentDescription = "The record: ${score.recipes.size} dishes as slices, time running from the rim to the label. " +
                            "${(score.handsOnSeconds + 59) / 60} of ${(score.tutti + 59) / 60} minutes are hands-on, never two tasks at once."
                    },
                label = DiscLabel(
                    top = "SIDE A  ${score.recipes.size} TRACKS",
                    main = "${(score.tutti + 59) / 60}",
                    sub = "MINUTES",
                    bottom = "HANDS ON ${(score.handsOnSeconds + 59) / 60} MIN",
                ),
                radius = { it.width / 2f * 0.98f },
                flashDish = auditioning,
                flash = { flash.value },
            )

            Text(
                "Hear each dish",
                style = Type.headline.copy(color = colors.ink),
                modifier = Modifier.padding(horizontal = 24.dp).padding(top = 40.dp, bottom = 4.dp),
            )
            Text(
                "Every dish has a four-note theme. When it needs you, you'll hear it build.",
                style = Type.body.copy(color = colors.inkMuted),
                modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 12.dp).widthIn(max = 360.dp),
            )
            score.recipes.forEachIndexed { d, recipe ->
                val source = remember { MutableInteractionSource() }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .pressScale(source)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = source,
                            indication = ripple(),
                            role = Role.Button,
                            onClickLabel = "Play the ${recipe.instrument.label.lowercase()} theme",
                            onClick = {
                                auditioning = d
                                nonce = System.nanoTime()
                                state.audition(d)
                            },
                        )
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LabelMark(recipe.color, size = 28.dp)
                    Column(Modifier.weight(1f).padding(start = 16.dp)) {
                        Text("${track(d)}  ${recipe.name}", style = Type.title.copy(color = colors.ink), maxLines = 1)
                        Text(recipe.instrument.label, style = Type.bodySmall.copy(color = colors.inkMuted))
                    }
                    MotifContour(recipe.motif, recipe.color, lit = if (auditioning == d) litNote else -1)
                    Icon(
                        Glyphs.Play,
                        contentDescription = null,
                        tint = colors.ink,
                        modifier = Modifier.padding(start = 12.dp).size(24.dp),
                    )
                }
            }

            Text(
                "Tracklist",
                style = Type.headline.copy(color = colors.ink),
                modifier = Modifier.padding(horizontal = 24.dp).padding(top = 40.dp, bottom = 12.dp),
            )
            score.cues.forEach { slot ->
                val recipe = score.recipes[slot.dish]
                val step = recipe.steps[slot.step]
                Row(Modifier.padding(horizontal = 24.dp, vertical = 10.dp), verticalAlignment = Alignment.Top) {
                    Text(formatClock(slot.start), style = Type.time.copy(color = colors.ink), modifier = Modifier.width(64.dp))
                    LabelMark(recipe.color, size = 14.dp, modifier = Modifier.padding(top = 5.dp))
                    Column(Modifier.weight(1f).padding(start = 12.dp, end = 8.dp)) {
                        Text(step.title, style = Type.title.copy(color = colors.ink))
                        Text(
                            "${recipe.instrument.label}, ${step.action.marking.lowercase()}, ${minutesLabel(step.seconds)}",
                            style = Type.bodySmall.copy(color = colors.inkMuted),
                        )
                    }
                    Tag(if (step.handsOn) "Hands" else "Heat", emphasis = step.handsOn)
                }
            }
            Spacer(Modifier.height(150.dp))
        }

        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .swallowTouches()
                .background(Brush.verticalGradient(0f to colors.plinth.copy(alpha = 0f), 0.3f to colors.plinth))
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 16.dp, top = 36.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .toggleable(value = state.rehearsal, role = Role.Switch, onValueChange = { state.rehearsal = it })
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Switch(
                    checked = state.rehearsal,
                    onCheckedChange = null,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = colors.ink,
                        checkedThumbColor = colors.onInk,
                        uncheckedTrackColor = colors.sunken,
                        uncheckedThumbColor = colors.inkMuted,
                        uncheckedBorderColor = colors.inkFaint,
                    ),
                )
                Column(Modifier.padding(start = 12.dp)) {
                    Text("Rehearsal", style = Type.button.copy(color = colors.ink))
                    Text(if (state.rehearsal) "20× speed" else "Real time", style = Type.bodySmall.copy(color = colors.inkMuted))
                }
            }
            Box(Modifier.padding(start = 8.dp)) {
                PrimaryButton("Drop the needle", onClick = { state.raiseBaton() })
            }
        }
    }
}
