package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.SessionMode
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.SageGreenBreak
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TimerRing(
    mode: SessionMode,
    focusElapsedSeconds: Int,
    totalTargetSeconds: Int,
    currentCycleFocusSeconds: Int,
    breakElapsedSeconds: Int,
    breakTargetSeconds: Int,
    modifier: Modifier = Modifier
) {
    val isBreak = mode == SessionMode.BREAK
    val progress = if (isBreak) {
        (breakElapsedSeconds.toFloat() / breakTargetSeconds.coerceAtLeast(1)).coerceIn(0f, 1f)
    } else {
        (focusElapsedSeconds.toFloat() / totalTargetSeconds.coerceAtLeast(1)).coerceIn(0f, 1f)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val primaryColor = if (isBreak) SageGreenBreak else AmberPrimary
    val secondaryColor = if (isBreak) Color(0xFFA7F3D0) else Color(0xFFFDE68A)

    Box(
        modifier = modifier
            .size(270.dp)
            .testTag("timer_ring_container"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val radius = diameter / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Background Track
            drawCircle(
                color = Color.Black.copy(alpha = 0.08f),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Progress Arc
            val sweepAngle = progress * 360f
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        primaryColor,
                        secondaryColor,
                        primaryColor
                    )
                ),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 30-Minute Cycle Markers if in Focus Mode
            if (!isBreak && totalTargetSeconds > 1800) {
                val cycleRatio = 1800f / totalTargetSeconds.toFloat()
                var currentMarkerRatio = cycleRatio
                while (currentMarkerRatio < 1.0f) {
                    val angleDeg = -90f + (currentMarkerRatio * 360f)
                    val angleRad = Math.toRadians(angleDeg.toDouble())
                    val markerInner = Offset(
                        center.x + (radius - 14.dp.toPx()) * cos(angleRad).toFloat(),
                        center.y + (radius - 14.dp.toPx()) * sin(angleRad).toFloat()
                    )
                    val markerOuter = Offset(
                        center.x + (radius + 6.dp.toPx()) * cos(angleRad).toFloat(),
                        center.y + (radius + 6.dp.toPx()) * sin(angleRad).toFloat()
                    )
                    drawLine(
                        color = SageGreenBreak.copy(alpha = 0.9f),
                        start = markerInner,
                        end = markerOuter,
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    currentMarkerRatio += cycleRatio
                }
            }
        }

        // Timer Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Surface(
                color = if (isBreak) SageGreenBreak.copy(alpha = 0.15f) else AmberPrimary.copy(alpha = 0.15f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = if (isBreak) "🌱 5-MIN BREAK" else if (mode == SessionMode.PAUSED) "⏸ PAUSED" else "📖 FOCUSING",
                    color = if (isBreak) SageGreenBreak else AmberPrimary,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val remainingSeconds = if (isBreak) {
                (breakTargetSeconds - breakElapsedSeconds).coerceAtLeast(0)
            } else {
                (totalTargetSeconds - focusElapsedSeconds).coerceAtLeast(0)
            }

            val mins = remainingSeconds / 60
            val secs = remainingSeconds % 60
            val timeFormatted = "%02d:%02d".format(mins, secs)

            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("timer_countdown_text")
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (isBreak) {
                Text(
                    text = "Relax & rest eyes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val cycleRemaining = (1800 - currentCycleFocusSeconds).coerceAtLeast(0)
                val cycleMins = cycleRemaining / 60
                Text(
                    text = "5m break in ${cycleMins}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
