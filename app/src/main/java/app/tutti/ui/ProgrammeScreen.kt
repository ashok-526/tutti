package app.tutti.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tutti.model.Recipe
import app.tutti.model.RecipeBook
import app.tutti.schedule.Conductor
import app.tutti.session.TuttiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ProgrammeScreen(state: TuttiState) {
    val menu = state.menu
    val preview = remember(menu.map { it.id }) { if (menu.isEmpty()) null else Conductor.compose(menu) }

    Box(Modifier.fillMaxSize().background(Palette.paper)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 150.dp),
        ) {
            item { Masthead() }
            item {
                Column(Modifier.padding(horizontal = 24.dp).padding(top = 28.dp, bottom = 34.dp)) {
                    BasicText("Tonight’s", style = Type.italic.copy(fontSize = 30.sp, lineHeight = 30.sp, color = Palette.inkSoft))
                    BasicText("Programme", style = Type.display.copy(color = Palette.ink))
                    Spacer(Modifier.height(14.dp))
                    BasicText(
                        "Choose your dishes. Tutti turns them into one score, each dish played by its own " +
                            "instrument, so everything reaches the table on the same final chord.",
                        style = Type.body.copy(color = Palette.inkSoft),
                        modifier = Modifier.widthIn(max = 340.dp),
                    )
                }
            }
            item {
                Row(Modifier.padding(horizontal = 24.dp).padding(bottom = 6.dp), verticalAlignment = Alignment.Bottom) {
                    Label("The movements", Palette.ink)
                    Spacer(Modifier.weight(1f))
                    Label("${state.selected.size} of ${RecipeBook.all.size} chosen", Palette.inkFaint)
                }
                Hairline(Modifier.padding(horizontal = 24.dp), Palette.ink)
            }
            items(RecipeBook.all, key = { it.id }) { recipe ->
                MovementRow(recipe, state.selected.indexOf(recipe.id)) { state.toggle(recipe) }
            }
            item {
                BasicText(
                    "Solid notes are your hands. Hatched notes are the heat doing the work. " +
                        "Tutti never asks for two hands-on tasks at once.",
                    style = Type.small.copy(color = Palette.inkFaint, fontStyle = FontStyle.Italic),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 22.dp),
                )
            }
        }

        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .swallowTouches()
                .background(Brush.verticalGradient(0f to Palette.paper.copy(alpha = 0f), 0.35f to Palette.paper))
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 20.dp, top = 36.dp, bottom = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Label(if (preview != null) "${minutesLabel(preview.tutti)} to the table" else "Pick a dish", Palette.ink)
                    Spacer(Modifier.height(4.dp))
                    BasicText(
                        if (preview != null) "${minutesLabel(preview.handsOnSeconds)} of it hands-on" else "to begin",
                        style = Type.small.copy(color = Palette.inkSoft),
                    )
                }
                PillButton("Compose", onClick = { state.compose() }, enabled = preview != null)
            }
        }
    }
}

@Composable
private fun Masthead() {
    val date = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("EEE d MMM")) }
    Column(Modifier.statusBarsPadding().padding(horizontal = 24.dp).padding(top = 18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(Palette.baton))
            Spacer(Modifier.width(8.dp))
            Label("Tutti", Palette.ink)
            Spacer(Modifier.weight(1f))
            Label("$date · Kitchen hall", Palette.inkFaint)
        }
        Spacer(Modifier.height(12.dp))
        Hairline(color = Palette.ink)
        Spacer(Modifier.height(3.dp))
        Hairline(color = Palette.ink)
    }
}

@Composable
private fun MovementRow(recipe: Recipe, index: Int, onToggle: () -> Unit) {
    val chosen = index >= 0
    val bar by animateDpAsState(if (chosen) 4.dp else 0.dp, spring(dampingRatio = 0.7f, stiffness = 500f), label = "bar")
    val nameColor by animateColorAsState(if (chosen) Palette.ink else Palette.inkFaint, label = "name")

    Column(
        Modifier.pressable(onClick = onToggle).semantics {
            selected = chosen
            stateDescription = if (chosen) "Movement ${roman(index)} of tonight's programme" else "Not in the programme"
        },
    ) {
        Row(
            Modifier.fillMaxWidth().height(96.dp).padding(end = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.width(bar).height(52.dp).background(recipe.color))
            Box(Modifier.width(62.dp - bar), contentAlignment = Alignment.Center) {
                if (chosen) {
                    BasicText(roman(index), style = Type.heading.copy(color = Palette.baton, fontSize = 24.sp))
                } else {
                    Box(
                        Modifier.size(30.dp).clip(CircleShape).border(1.dp, Palette.rule, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { PlusCheckIcon(Palette.inkSoft, checked = false, modifier = Modifier.size(12.dp)) }
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                BasicText(recipe.name, style = Type.heading.copy(color = nameColor))
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicText("for ${recipe.instrument.label}", style = Type.italic.copy(fontSize = 17.sp, lineHeight = 20.sp, color = Palette.inkSoft))
                    BasicText(
                        "  ·  ${minutesLabel(recipe.totalSeconds)}",
                        style = Type.mono.copy(fontSize = 11.sp, color = Palette.inkFaint),
                    )
                }
            }
            MotifStaff(recipe.motif, if (chosen) recipe.color else Palette.inkFaint.copy(alpha = 0.6f), Palette.rule)
        }
        Hairline(Modifier.padding(horizontal = 24.dp))
    }
}
