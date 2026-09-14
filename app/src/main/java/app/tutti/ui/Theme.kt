package app.tutti.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.tutti.R

object Palette {
    // The programme: warm paper and printer's ink.
    val paper = Color(0xFFF3EDE2)
    val paperDeep = Color(0xFFE9E0D0)
    val ink = Color(0xFF1C1A16)
    val inkSoft = Color(0xFF6B6356)
    val inkFaint = Color(0xFF9C927F)
    val rule = Color(0xFFD5CAB6)
    val baton = Color(0xFFD2452F)

    // The stage: a dark hall while you cook.
    val stage = Color(0xFF12100D)
    val stageRaised = Color(0xFF1C1915)
    val stageLine = Color(0xFF302B24)
    val chalk = Color(0xFFF3EDE2)
    val chalkSoft = Color(0xFFA59C8C)
    val chalkFaint = Color(0xFF6A6358)
    val batonLit = Color(0xFFFF6B4E)
}

object Fonts {
    val serif = FontFamily(
        Font(R.font.instrument_serif, FontWeight.Normal),
        Font(R.font.instrument_serif_italic, FontWeight.Normal, FontStyle.Italic),
    )
    val mono = FontFamily(
        Font(R.font.plex_mono, FontWeight.Normal),
        Font(R.font.plex_mono_medium, FontWeight.Medium),
    )
    val sans = FontFamily.SansSerif
}

object Type {
    val display = TextStyle(fontFamily = Fonts.serif, fontSize = 60.sp, lineHeight = 56.sp, letterSpacing = (-0.02).em)
    val title = TextStyle(fontFamily = Fonts.serif, fontSize = 38.sp, lineHeight = 40.sp, letterSpacing = (-0.01).em)
    val heading = TextStyle(fontFamily = Fonts.serif, fontSize = 27.sp, lineHeight = 30.sp)
    val italic = TextStyle(fontFamily = Fonts.serif, fontStyle = FontStyle.Italic, fontSize = 22.sp, lineHeight = 26.sp)
    val body = TextStyle(fontFamily = Fonts.sans, fontSize = 15.sp, lineHeight = 22.sp)
    val small = TextStyle(fontFamily = Fonts.sans, fontSize = 13.sp, lineHeight = 18.sp)
    val label = TextStyle(fontFamily = Fonts.mono, fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.14.em)
    val mono = TextStyle(fontFamily = Fonts.mono, fontSize = 13.sp, letterSpacing = 0.02.em)
    val clock = TextStyle(fontFamily = Fonts.mono, fontWeight = FontWeight.Normal, fontSize = 64.sp, letterSpacing = (-0.03).em)
}

/** Press feedback without Material: a quick, springy scale-down. */
fun Modifier.pressable(enabled: Boolean = true, onClick: () -> Unit): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.965f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 900f),
        label = "press",
    )
    this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = source, indication = null, enabled = enabled, onClick = onClick)
}

/** For bars floating over scrolling content: taps on the bar never reach the rows beneath it. */
fun Modifier.swallowTouches(): Modifier = pointerInput(Unit) { detectTapGestures { } }

fun formatClock(seconds: Int): String {
    val s = maxOf(0, seconds)
    return "%d:%02d".format(s / 60, s % 60)
}

private val ROMAN = listOf("I", "II", "III", "IV", "V", "VI", "VII", "VIII")
fun roman(i: Int) = ROMAN.getOrElse(i) { (i + 1).toString() }
