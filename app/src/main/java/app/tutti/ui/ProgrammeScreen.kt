package app.tutti.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.tutti.model.Recipe
import app.tutti.model.RecipeBook
import app.tutti.schedule.Conductor
import app.tutti.session.TuttiState

@Composable
fun ProgrammeScreen(state: TuttiState) {
    val colors = tutti
    val menu = state.menu
    val preview = remember(menu.map { it.id }) { if (menu.isEmpty()) null else Conductor.compose(menu) }
    // With large system fonts the summary and the pill no longer fit side by side, so they stack.
    val stacked = LocalDensity.current.fontScale > 1.15f

    Box(Modifier.fillMaxSize().background(colors.plinth)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Box(Modifier.fillMaxWidth().height(380.dp)) {
                Turntable(
                    score = preview,
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics {
                            contentDescription = if (preview == null) {
                                "An empty record. Pick a dish to press it."
                            } else {
                                "Your record: ${menu.size} dishes, ${(preview.tutti + 59) / 60} minutes to the table."
                            }
                        },
                    label = if (preview != null) {
                        DiscLabel(top = "TUTTI  SIDE A", main = "${(preview.tutti + 59) / 60}", sub = "MINUTES", bottom = "${menu.size} TRACKS")
                    } else {
                        DiscLabel(top = "TUTTI  SIDE A", bottom = "PICK A DISH")
                    },
                    center = { Offset(it.width * 0.66f, it.height * 0.42f) },
                    radius = { it.width * 0.5f },
                )
                Text(
                    "Tutti",
                    style = Type.label.copy(color = colors.ink, fontSize = 15.sp, letterSpacing = 0.14.em),
                    modifier = Modifier.statusBarsPadding().padding(start = 24.dp, top = 16.dp),
                )
            }

            Column(Modifier.padding(horizontal = 24.dp)) {
                Text("Tonight's record", style = Type.display.copy(color = colors.ink))
                Text(
                    "Pick your dishes. Each one is pressed as its own groove, and every groove ends on the same final chord.",
                    style = Type.body.copy(color = colors.inkMuted),
                    modifier = Modifier.padding(top = 12.dp).widthIn(max = 360.dp),
                )
            }

            Column(
                Modifier.padding(horizontal = 12.dp).padding(top = 28.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                RecipeBook.all.forEach { recipe ->
                    CrateRow(recipe, state.selected.indexOf(recipe.id)) { state.toggle(recipe) }
                }
            }
            Spacer(Modifier.height(if (stacked) 240.dp else 140.dp))
        }

        val summary: @Composable () -> Unit = {
            Text(
                if (preview != null) "${minutesLabel(preview.tutti)} to the table" else "Pick a dish",
                style = Type.title.copy(color = colors.ink),
            )
            Text(
                if (preview != null) "${minutesLabel(preview.handsOnSeconds)} of it hands-on" else "to press your record",
                style = Type.bodySmall.copy(color = colors.inkMuted),
            )
        }
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .swallowTouches()
                .drawBehind {
                    // A short fixed fade, then solid plinth, so text never sits over rows however tall the bar grows.
                    val fade = 24.dp.toPx()
                    drawRect(
                        Brush.verticalGradient(0f to colors.plinth.copy(alpha = 0f), 1f to colors.plinth, startY = 0f, endY = fade),
                        size = Size(size.width, fade),
                    )
                    drawRect(colors.plinth, topLeft = Offset(0f, fade), size = Size(size.width, size.height - fade))
                }
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 16.dp, top = 36.dp, bottom = 16.dp),
        ) {
            if (stacked) {
                summary()
                PrimaryButton(
                    "Press the record",
                    onClick = { state.compose() },
                    enabled = preview != null,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { summary() }
                    PrimaryButton("Press the record", onClick = { state.compose() }, enabled = preview != null)
                }
            }
        }
        StatusBarScrim()
    }
}

@Composable
private fun CrateRow(recipe: Recipe, index: Int, onToggle: () -> Unit) {
    val colors = tutti
    val chosen = index >= 0
    val source = remember { MutableInteractionSource() }
    val presence by animateFloatAsState(if (chosen) 1f else 0.58f, tween(200, easing = Motion.out), label = "presence")

    Row(
        Modifier
            .fillMaxWidth()
            .pressScale(source)
            .clip(RoundedCornerShape(16.dp))
            .toggleable(
                value = chosen,
                interactionSource = source,
                indication = ripple(),
                role = Role.Checkbox,
                onValueChange = { onToggle() },
            )
            .semantics { stateDescription = if (chosen) "Track ${track(index)} on the record" else "Not on the record" }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Sleeve(recipe.color, chosen, Modifier.graphicsLayer { alpha = presence })
        Column(Modifier.weight(1f).padding(start = 8.dp, end = 12.dp)) {
            Text(recipe.name, style = Type.title.copy(color = if (chosen) colors.ink else colors.inkMuted), maxLines = 1)
            Text(
                "${recipe.instrument.label.lowercase()}, ${minutesLabel(recipe.totalSeconds)}",
                style = Type.bodySmall.copy(color = colors.inkMuted),
            )
        }
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (chosen) colors.ink else Color.Transparent)
                .border(1.5.dp, if (chosen) colors.ink else colors.line, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (chosen) {
                Text(track(index), style = Type.label.copy(color = colors.onInk))
            } else {
                Icon(Glyphs.Add, contentDescription = null, tint = colors.inkMuted, modifier = Modifier.size(20.dp))
            }
        }
    }
}
