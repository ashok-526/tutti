package app.tutti.ui

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.tutti.session.TipJar
import app.tutti.session.TuttiState

@Composable
fun FinaleScreen(state: TuttiState) {
    val performance = state.performance
    val score = performance?.score ?: state.score ?: return
    val colors = tutti
    val motion = animationsEnabled()
    val settle = remember { Animatable(if (motion) 0f else 1f) }
    LaunchedEffect(Unit) { if (motion) settle.animateTo(1f, tween(2200, easing = Motion.out)) }

    Box(Modifier.fillMaxSize().background(colors.plinth)) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Text(
            "Tutti",
            style = Type.label.copy(color = colors.ink, fontSize = 15.sp, letterSpacing = 0.14.em),
            modifier = Modifier.padding(start = 24.dp, top = 16.dp),
        )

        Turntable(
            score = score,
            modifier = Modifier
                .fillMaxWidth()
                .height(372.dp)
                .semantics { contentDescription = "The record, played through to the label." },
            label = DiscLabel(top = "SIDE A  PLAYED", main = "Tutti", sub = "SERVED", bottom = "${score.recipes.size} DISHES TOGETHER"),
            center = { Offset(it.width * 0.46f, it.height * 0.5f) },
            radius = { minOf(it.width * 0.4f, it.height * 0.44f) },
            rotation = { 150f * settle.value },
            arm = { 1f },
            armDrop = { 1f - settle.value },
        )

        Column(Modifier.padding(horizontal = 24.dp)) {
            Text("Every dish, together.", style = Type.display.copy(color = colors.ink))
            Text(
                "${score.recipes.size} dishes reached the table on the same final chord, cued by ear.",
                style = Type.body.copy(color = colors.inkMuted),
                modifier = Modifier.padding(top = 12.dp).widthIn(max = 360.dp),
            )
        }

        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 28.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surface)
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Reading("Length", formatClock(score.tutti))
            Reading("Cues played", "${performance?.cuesPlayed ?: score.slots.size}")
            Reading("Re-scored", "${performance?.rescored ?: 0}×")
        }

        Text(
            "Flip it over",
            style = Type.headline.copy(color = colors.ink),
            modifier = Modifier.padding(horizontal = 24.dp).padding(top = 40.dp),
        )
        Text(
            "The encore plays tonight's dinner back as a short song: each theme enters in the order you cooked it, then the final chord.",
            style = Type.body.copy(color = colors.inkMuted),
            modifier = Modifier.padding(horizontal = 24.dp).padding(top = 8.dp).widthIn(max = 360.dp),
        )
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 20.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PrimaryButton("Play the encore", onClick = { state.playEncore() }, modifier = Modifier.weight(1f))
            SecondaryButton("New menu", onClick = { state.newProgramme() }, modifier = Modifier.weight(1f))
        }
        TipJarSection(state.tipJar)
        Spacer(Modifier.height(8.dp))
    }
    StatusBarScrim()
    }
}

/** An optional thank-you once dinner is served. It only appears when RevenueCat has tips to offer. */
@Composable
private fun TipJarSection(jar: TipJar) {
    LaunchedEffect(Unit) { jar.load() }
    if (jar.tips.isEmpty()) return
    val colors = tutti
    val activity = LocalView.current.context as Activity
    Text(
        "Tip the band",
        style = Type.headline.copy(color = colors.ink),
        modifier = Modifier.padding(horizontal = 24.dp).padding(top = 16.dp),
    )
    // After a tip, the thanks takes this line's place so it's seen without scrolling.
    Text(
        jar.note ?: "Tutti is free, and everything in it stays free. If dinner came together, you can thank the band.",
        style = Type.body.copy(color = if (jar.note != null) colors.ink else colors.inkMuted),
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp)
            .widthIn(max = 360.dp)
            .semantics { liveRegion = LiveRegionMode.Polite },
    )
    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .padding(start = 20.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
    ) {
        jar.tips.forEach { tip ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(tip.name, style = Type.body.copy(color = colors.ink), modifier = Modifier.weight(1f).padding(end = 12.dp))
                SecondaryButton(
                    tip.price,
                    onClick = { jar.give(activity, tip) },
                    modifier = Modifier.semantics { contentDescription = "Tip: ${tip.name}, ${tip.price}" },
                    enabled = jar.pending == null,
                )
            }
        }
    }
}

@Composable
private fun Reading(label: String, value: String) {
    val colors = tutti
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(label, style = Type.body.copy(color = colors.inkMuted), modifier = Modifier.weight(1f))
        Text(value, style = Type.time.copy(color = colors.ink))
    }
}
