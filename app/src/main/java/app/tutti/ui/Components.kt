package app.tutti.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Material Symbols glyphs (Apache 2.0), from Google's published path data. */
object Glyphs {
    private fun glyph(name: String, pathData: String): ImageVector =
        ImageVector.Builder(name = name, defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
            .addPath(pathData = PathParser().parsePathString(pathData).toNodes(), fill = SolidColor(Color.Black))
            .build()

    val Back = glyph("back", "M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z")
    val Close = glyph("close", "M19,6.41L17.59,5 12,10.59 6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 12,13.41 17.59,19 19,17.59 13.41,12z")
    val Check = glyph("check", "M9,16.17L4.83,12l-1.42,1.41L9,19 21,7l-1.41,-1.41z")
    val Add = glyph("add", "M19,13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z")
    val Play = glyph("play", "M8,5v14l11,-7z")
    val VolumeOn = glyph(
        "volume_on",
        "M3,9v6h4l5,5V4L7,9H3zM16.5,12c0,-1.77 -1.02,-3.29 -2.5,-4.03v8.05c1.48,-0.73 2.5,-2.25 2.5,-4.02zM14,3.23v2.06c2.89,0.86 5,3.54 5,6.71s-2.11,5.85 -5,6.71v2.06c4.01,-0.91 7,-4.49 7,-8.77s-2.99,-7.86 -7,-8.77z",
    )
    val VolumeOff = glyph(
        "volume_off",
        "M16.5,12c0,-1.77 -1.02,-3.29 -2.5,-4.03v2.21l2.45,2.45c0.03,-0.2 0.05,-0.41 0.05,-0.63zM19,12c0,0.94 -0.2,1.82 -0.54,2.64l1.51,1.51C20.63,14.91 21,13.5 21,12c0,-4.28 -2.99,-7.86 -7,-8.77v2.06c2.89,0.86 5,3.54 5,6.71zM4.27,3L3,4.27 7.73,9H3v6h4l5,5v-6.73l4.25,4.25c-0.67,0.52 -1.42,0.93 -2.25,1.18v2.06c1.38,-0.31 2.63,-0.95 3.69,-1.81L19.73,21 21,19.73l-9,-9L4.27,3zM12,4L9.91,6.09 12,8.18V4z",
    )
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    signal: Boolean = false,
) {
    val colors = tutti
    val source = remember { MutableInteractionSource() }
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 56.dp).pressScale(source),
        shape = CircleShape,
        interactionSource = source,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (signal) colors.cue else colors.ink,
            contentColor = if (signal) colors.onCue else colors.onInk,
            disabledContainerColor = colors.sunken,
            disabledContentColor = colors.inkMuted,
        ),
        contentPadding = PaddingValues(horizontal = 28.dp, vertical = 16.dp),
    ) {
        Text(text, style = Type.button, maxLines = 1)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val colors = tutti
    val source = remember { MutableInteractionSource() }
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 56.dp).pressScale(source),
        shape = CircleShape,
        interactionSource = source,
        border = BorderStroke(1.5.dp, if (enabled) colors.ink.copy(alpha = 0.5f) else colors.line),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.ink, disabledContentColor = colors.inkMuted),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Text(text, style = Type.button, maxLines = 1)
    }
}

@Composable
fun GlyphButton(glyph: ImageVector, description: String, onClick: () -> Unit, modifier: Modifier = Modifier, tint: Color = tutti.ink) {
    IconButton(onClick = onClick, modifier = modifier.size(48.dp)) {
        Icon(glyph, contentDescription = description, tint = tint)
    }
}

/** A dish's identity mark: its printed record label, spindle hole and all. */
@Composable
fun LabelMark(color: Color, modifier: Modifier = Modifier, size: Dp = 18.dp) {
    val hole = tutti.plinth
    Canvas(modifier.size(size)) {
        drawCircle(color)
        drawCircle(hole, radius = this.size.minDimension * 0.13f)
    }
}

/**
 * A dish in the crate: a square sleeve printed in its label ink. Choosing it slides the record
 * half out of the sleeve, turning as it goes.
 */
@Composable
fun Sleeve(color: Color, chosen: Boolean, modifier: Modifier = Modifier) {
    val colors = tutti
    val out by animateFloatAsState(
        targetValue = if (chosen) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f),
        label = "sleeve",
    )
    Box(modifier.size(width = 84.dp, height = 60.dp)) {
        Canvas(
            Modifier
                .size(54.dp)
                .align(Alignment.CenterStart)
                .offset(x = 3.dp)
                .graphicsLayer {
                    translationX = (26 * out).dp.toPx()
                    rotationZ = 70f * out
                },
        ) {
            val r = size.minDimension / 2f
            drawCircle(colors.vinyl, r)
            drawCircle(colors.groove, r * 0.8f, style = Stroke(0.8.dp.toPx()))
            drawCircle(colors.groove, r * 0.62f, style = Stroke(0.8.dp.toPx()))
            drawCircle(color, r * 0.36f)
            drawLine(colors.label.copy(alpha = 0.7f), center + Offset(0f, -r * 0.3f), center + Offset(0f, -r * 0.12f), 1.5.dp.toPx(), StrokeCap.Round)
            drawCircle(colors.plinth, r * 0.06f)
        }
        Box(
            Modifier
                .size(60.dp)
                .shadow(if (chosen) 6.dp else 1.dp, RoundedCornerShape(4.dp), clip = false)
                .clip(RoundedCornerShape(4.dp))
                .background(color),
        ) {
            Canvas(Modifier.size(60.dp)) {
                drawCircle(Color.Black.copy(alpha = 0.16f), size.minDimension * 0.19f)
                drawCircle(Color.White.copy(alpha = 0.22f), size.minDimension * 0.19f, style = Stroke(1.dp.toPx()))
            }
        }
    }
}

/** A leitmotif as a pitch contour: four notes, rising and falling. */
@Composable
fun MotifContour(motif: List<Int>, color: Color, modifier: Modifier = Modifier, lit: Int = -1) {
    Canvas(modifier.size(width = 56.dp, height = 28.dp)) {
        val count = motif.size
        val xs = List(count) { i -> size.width * (0.1f + 0.8f * i / (count - 1).coerceAtLeast(1)) }
        val ys = motif.map { p -> size.height * (0.84f - 0.68f * (p / 9f)) }
        for (i in 0 until count - 1) {
            drawLine(color.copy(alpha = 0.45f), Offset(xs[i], ys[i]), Offset(xs[i + 1], ys[i + 1]), 1.6.dp.toPx(), StrokeCap.Round)
        }
        for (i in 0 until count) {
            val radius = if (i == lit) 5.dp.toPx() else 3.4.dp.toPx()
            drawCircle(color, radius, Offset(xs[i], ys[i]))
        }
    }
}

/**
 * A small printed chip, like the speed selector on a deck. [cue] is the amber light:
 * reserved for "your hands, now".
 */
@Composable
fun Tag(text: String, modifier: Modifier = Modifier, emphasis: Boolean = false, cue: Boolean = false) {
    val colors = tutti
    Box(
        modifier
            .clip(CircleShape)
            .background(if (cue) colors.cue else colors.sunken)
            .border(1.dp, if (cue) colors.cueEdge else Color.Transparent, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text.uppercase(),
            style = Type.label.copy(
                color = when {
                    cue -> colors.onCue
                    emphasis -> colors.ink
                    else -> colors.inkMuted
                },
            ),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            maxLines = 1,
        )
    }
}
