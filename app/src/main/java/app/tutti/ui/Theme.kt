package app.tutti.ui

import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.tutti.R
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** The turntable: an aluminum plinth, black vinyl, printed label inks, one amber cue light. */
@Immutable
data class TuttiColors(
    val plinth: Color,
    val plinthShade: Color,
    val surface: Color,
    val sunken: Color,
    val ink: Color,
    val inkMuted: Color,
    val inkFaint: Color,
    val line: Color,
    val onInk: Color,
    val vinyl: Color,
    val groove: Color,
    val rim: Color,
    val label: Color,
    val onLabel: Color,
    val arm: Color,
    val armHighlight: Color,
    val cue: Color,
    val onCue: Color,
    val cueEdge: Color,
    val dark: Boolean,
)

val LightTutti = TuttiColors(
    plinth = Color(0xFFE3E6E9),
    plinthShade = Color(0xFFD5D9DD),
    surface = Color(0xFFF0F2F4),
    sunken = Color(0xFFCCD1D6),
    ink = Color(0xFF15171A),
    inkMuted = Color(0xFF4D535A),
    inkFaint = Color(0xFF737A82),
    line = Color(0xFFC3C9CF),
    onInk = Color(0xFFF0F2F4),
    vinyl = Color(0xFF111214),
    groove = Color(0xFF2A2C31),
    rim = Color(0xFF3A3D42),
    label = Color(0xFFF3F4F5),
    onLabel = Color(0xFF15171A),
    arm = Color(0xFF2B2F35),
    armHighlight = Color(0xFF9AA1A9),
    cue = Color(0xFFFFB21F),
    onCue = Color(0xFF15171A),
    cueEdge = Color(0xFFB87900),
    dark = false,
)

val DarkTutti = TuttiColors(
    plinth = Color(0xFF15171A),
    plinthShade = Color(0xFF0E1012),
    surface = Color(0xFF1F2226),
    sunken = Color(0xFF0B0C0E),
    ink = Color(0xFFECEEF0),
    inkMuted = Color(0xFFA7ADB4),
    inkFaint = Color(0xFF858C94),
    line = Color(0xFF2E3238),
    onInk = Color(0xFF15171A),
    vinyl = Color(0xFF08090A),
    groove = Color(0xFF1D1E21),
    rim = Color(0xFF44474D),
    label = Color(0xFF26292D),
    onLabel = Color(0xFFECEEF0),
    arm = Color(0xFFB9BFC6),
    armHighlight = Color(0xFFE6E9EC),
    cue = Color(0xFFFFB21F),
    onCue = Color(0xFF15171A),
    cueEdge = Color(0xFFFFCF70),
    dark = true,
)

val LocalTutti = staticCompositionLocalOf { LightTutti }

val tutti: TuttiColors
    @Composable get() = LocalTutti.current

@OptIn(ExperimentalTextApi::class)
private fun archivo(weight: Int, width: Float) = Font(
    resId = R.font.archivo,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight), FontVariation.width(width)),
)

/** One family, three widths: condensed for numerals, normal for reading, expanded for label caps. */
object Fonts {
    val condensed = FontFamily(archivo(600, 62f), archivo(800, 62f), archivo(900, 62f))
    val text = FontFamily(archivo(400, 100f), archivo(500, 100f), archivo(600, 100f), archivo(700, 100f))
    val expanded = FontFamily(archivo(500, 125f), archivo(600, 125f), archivo(700, 125f))
}

object Type {
    private const val TABULAR = "tnum"
    val numeral = TextStyle(fontFamily = Fonts.condensed, fontWeight = FontWeight(900), fontSize = 84.sp, lineHeight = 80.sp, fontFeatureSettings = TABULAR)
    val display = TextStyle(fontFamily = Fonts.condensed, fontWeight = FontWeight(800), fontSize = 58.sp, lineHeight = 56.sp, letterSpacing = (-0.005).em)
    val headline = TextStyle(fontFamily = Fonts.text, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 36.sp, letterSpacing = (-0.02).em)
    val title = TextStyle(fontFamily = Fonts.text, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 24.sp, letterSpacing = (-0.005).em)
    val body = TextStyle(fontFamily = Fonts.text, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp)
    val bodySmall = TextStyle(fontFamily = Fonts.text, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp)
    val label = TextStyle(fontFamily = Fonts.expanded, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.08.em)
    val button = TextStyle(fontFamily = Fonts.text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 20.sp)
    val time = TextStyle(fontFamily = Fonts.condensed, fontWeight = FontWeight(800), fontSize = 22.sp, lineHeight = 24.sp, fontFeatureSettings = TABULAR)
}

@Composable
fun TuttiTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkTutti else LightTutti
    val base = if (colors.dark) darkColorScheme() else lightColorScheme()
    val scheme = base.copy(
        primary = colors.ink,
        onPrimary = colors.onInk,
        secondary = colors.cue,
        onSecondary = colors.onCue,
        background = colors.plinth,
        onBackground = colors.ink,
        surface = colors.surface,
        onSurface = colors.ink,
        surfaceVariant = colors.sunken,
        onSurfaceVariant = colors.inkMuted,
        surfaceContainerHighest = colors.sunken,
        outline = colors.inkFaint,
        outlineVariant = colors.line,
    )
    MaterialTheme(
        colorScheme = scheme,
        typography = Typography(
            displayLarge = Type.display,
            headlineMedium = Type.headline,
            titleMedium = Type.title,
            bodyLarge = Type.body,
            bodyMedium = Type.bodySmall,
            labelLarge = Type.button,
            labelSmall = Type.label,
        ),
        shapes = Shapes(
            extraSmall = RoundedCornerShape(4.dp),
            small = RoundedCornerShape(4.dp),
            medium = RoundedCornerShape(8.dp),
            large = RoundedCornerShape(16.dp),
        ),
    ) {
        CompositionLocalProvider(LocalTutti provides colors, content = content)
    }
}

object Motion {
    /** Strong ease-out for anything responding to the cook. */
    val out = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)
    /** For things already on screen changing place. */
    val inOut = CubicBezierEasing(0.77f, 0f, 0.175f, 1f)
}

/** False when the system "Remove animations" setting is on. */
@Composable
fun animationsEnabled(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    }
}

/** Presses sink slightly: the interface heard you. */
fun Modifier.pressScale(source: InteractionSource): Modifier = composed {
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(if (pressed) 90 else 180, easing = Motion.out),
        label = "press",
    )
    graphicsLayer { scaleX = scale; scaleY = scale }
}

fun Modifier.pressable(enabled: Boolean = true, onClick: () -> Unit): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    this
        .pressScale(source)
        .clickable(interactionSource = source, indication = ripple(), enabled = enabled, onClick = onClick)
}

/** For bars floating over scrolling content: taps on the bar never reach the rows beneath it. */
fun Modifier.swallowTouches(): Modifier = pointerInput(Unit) { detectTapGestures { } }

fun formatClock(seconds: Int): String {
    val s = maxOf(0, seconds)
    return "%d:%02d".format(s / 60, s % 60)
}

private val CLOCK = DateTimeFormatter.ofPattern("h:mm a")
fun clockTime(secondsFromNow: Int): String = LocalTime.now().plusSeconds(secondsFromNow.toLong()).format(CLOCK)
fun minutesLabel(seconds: Int) = "${(seconds + 59) / 60} min"

/** Record track numbering: side A, one track per dish. */
fun track(dish: Int) = "A${dish + 1}"
