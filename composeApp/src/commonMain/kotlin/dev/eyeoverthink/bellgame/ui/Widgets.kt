package dev.eyeoverthink.bellgame.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.eyeoverthink.bellgame.engine.ChshConstants
import dev.eyeoverthink.bellgame.engine.MeasurementAngles
import kotlin.math.cos
import kotlin.math.sin

/**
 * A track from 50% to 100% with the three ceilings marked and the live rate as a dot.
 */
@Composable
fun CeilingGauge(rate: Double, rounds: Int, modifier: Modifier = Modifier) {
    Column(modifier) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            val left = 10.dp.toPx()
            val right = size.width - 10.dp.toPx()
            val y = size.height * 0.62f
            drawLine(
                color = BellColors.Line,
                start = Offset(left, y),
                end = Offset(right, y),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round,
            )
            ceilingMarks().forEach { mark ->
                val x = xOf(mark.value, left, right)
                drawLine(
                    color = mark.color,
                    start = Offset(x, y - 16.dp.toPx()),
                    end = Offset(x, y + 12.dp.toPx()),
                    strokeWidth = 2.dp.toPx(),
                )
            }
            if (rounds > 0) {
                drawCircle(
                    color = BellColors.Ink,
                    radius = 7.dp.toPx(),
                    center = Offset(xOf(rate, left, right), y),
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            ceilingMarks().forEach { mark ->
                Text(mark.label, color = mark.color, fontSize = 11.sp)
            }
        }
    }
}

/**
 * Running win rate against the three horizontal ceilings.
 */
@Composable
fun ConvergenceChart(samples: List<Double>, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val left = 6.dp.toPx()
        val right = size.width - 6.dp.toPx()
        val top = 10.dp.toPx()
        val bottom = size.height - 10.dp.toPx()
        ceilingMarks().forEach { mark ->
            val y = yOf(mark.value, top, bottom)
            drawLine(
                color = mark.color.copy(alpha = 0.9f),
                start = Offset(left, y),
                end = Offset(right, y),
                strokeWidth = 1.5.dp.toPx(),
            )
        }
        if (samples.size < 2) {
            return@Canvas
        }
        val path = Path()
        samples.forEachIndexed { index, sample ->
            val x = left + (right - left) * index / (samples.lastIndex)
            val y = yOf(sample, top, bottom)
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        drawPath(
            path = path,
            color = BellColors.Ink,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

/**
 * Alice's and Bob's measurement directions on the XZ plane.
 * Zero points up (Z). Positive angles turn toward the right (X).
 */
@Composable
fun AngleDial(angles: MeasurementAngles, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val radius = size.minDimension / 2f * 0.78f
        drawCircle(
            color = BellColors.Line,
            radius = radius,
            center = center,
            style = Stroke(width = 2.dp.toPx()),
        )
        drawLine(
            BellColors.Line,
            Offset(center.x, center.y - radius),
            Offset(center.x, center.y + radius),
            strokeWidth = 1.dp.toPx(),
        )
        drawLine(
            BellColors.Line,
            Offset(center.x - radius, center.y),
            Offset(center.x + radius, center.y),
            strokeWidth = 1.dp.toPx(),
        )
        needle(angles.bobWhenOne, BellColors.Brass, dashed = true, radius = radius)
        needle(angles.bobWhenZero, BellColors.Brass, dashed = false, radius = radius)
        needle(angles.aliceWhenOne, BellColors.Ion, dashed = true, radius = radius)
        needle(angles.aliceWhenZero, BellColors.Ion, dashed = false, radius = radius)
    }
}

/**
 * A big number with a short caption, used on the score and the bench.
 */
@Composable
fun Metric(value: String, caption: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier.padding(vertical = 4.dp)) {
        Text(
            text = value,
            color = color,
            fontFamily = FontFamily.Monospace,
            fontSize = 28.sp,
        )
        Text(text = caption, color = BellColors.Muted, fontSize = 12.sp)
    }
}

/**
 * One input or output bit, drawn as a small tile.
 */
@Composable
fun BitToken(label: String, value: Int, emphasize: Boolean, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = BellColors.Muted, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(42.dp)
                .background(
                    color = if (emphasize) BellColors.Ion else BellColors.Line,
                    shape = RoundedCornerShape(8.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = value.toString(),
                color = if (emphasize) BellColors.Night else BellColors.Ink,
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
            )
        }
    }
}

private data class CeilingMark(val value: Double, val color: Color, val label: String)

private fun ceilingMarks(): List<CeilingMark> = listOf(
    CeilingMark(ChshConstants.CLASSICAL_WIN_RATE, BellColors.Brass, "75% local"),
    CeilingMark(ChshConstants.tsirelsonWinRate, BellColors.Ion, "85.36% quantum"),
    CeilingMark(ChshConstants.PR_WIN_RATE, BellColors.Alarm, "100% PR"),
)

private fun xOf(value: Double, left: Float, right: Float): Float {
    val fraction = ((value - LOWER_RATE) / RATE_SPAN).toFloat().coerceIn(0f, 1f)
    return left + (right - left) * fraction
}

private fun yOf(value: Double, top: Float, bottom: Float): Float {
    val fraction = ((value - LOWER_RATE) / RATE_SPAN).toFloat().coerceIn(0f, 1f)
    return bottom - (bottom - top) * fraction
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.needle(
    angle: Double,
    color: Color,
    dashed: Boolean,
    radius: Float,
) {
    val end = Offset(
        x = center.x + sin(angle).toFloat() * radius,
        y = center.y - cos(angle).toFloat() * radius,
    )
    drawLine(
        color = color,
        start = center,
        end = end,
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round,
        pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(14f, 10f)) else null,
    )
    drawCircle(color = color, radius = 5.dp.toPx(), center = end)
}

private const val LOWER_RATE = 0.5
private const val RATE_SPAN = 0.5
