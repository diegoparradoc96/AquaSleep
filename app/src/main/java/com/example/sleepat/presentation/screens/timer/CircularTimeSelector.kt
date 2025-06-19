package com.example.sleepat.presentation.screens.timer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


@Composable
fun CircularTimeSelector(
    modifier: Modifier = Modifier,
    initialMinutes: Int,
    onTimeChange: (Int) -> Unit,
    strokeWidth: Float = 30f,
    content: @Composable () -> Unit
) {
    var angle by remember { mutableFloatStateOf(0f) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    LaunchedEffect(initialMinutes) {
        angle = (initialMinutes % 60) * 6f
    }

    // The outer Box is made even larger to provide a more generous touch area.
    Box(
        modifier = modifier.size(340.dp),
        contentAlignment = Alignment.Center
    ) {
        content()

        Canvas(modifier = Modifier
            .matchParentSize()
            .pointerInput(Unit) {
                // Define the touchable area based on the visual circle's properties.
                val visualCircleRadius = 280.dp.toPx() / 2f
                val trackCenterRadius = visualCircleRadius - strokeWidth / 2f

                // Make the touchable ring wider than the visual stroke for better UX.
                val touchableRingWidth = strokeWidth * 3f
                val minRadius = trackCenterRadius - touchableRingWidth / 2f
                val maxRadius = trackCenterRadius + touchableRingWidth / 2f

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)

                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val dx = down.position.x - centerX
                    val dy = down.position.y - centerY
                    val distanceFromCenter = sqrt(dx * dx + dy * dy)

                    // Only process touches that are on or near the circular track.
                    if (distanceFromCenter in minRadius..maxRadius) {
                        // --- TAP LOGIC ---
                        var touchAngle = (atan2(dy, dx) * (180f / PI.toFloat()) + 360f + 90f) % 360f
                        angle = touchAngle
                        onTimeChange((angle / 6f).toInt().coerceIn(0, 59))

                        // --- DRAG LOGIC ---


                        drag(down.id) { change ->
                            val dragDx = change.position.x - centerX
                            val dragDy = change.position.y - centerY
                            touchAngle = (atan2(dragDy, dragDx) * (180f / PI.toFloat()) + 360f + 90f) % 360f
                            angle = touchAngle
                            onTimeChange((angle / 6f).toInt().coerceIn(0, 59))
                            change.consume()
                        }
                    }
                }
            }
        ) {
            // The visual circle remains the same size (280.dp)
            val visualCircleSizePx = 280.dp.toPx()
            val visualRadius = (visualCircleSizePx / 2f) - (strokeWidth / 2f)
            val center = Offset(size.width / 2f, size.height / 2f)

            // Calculate the top-left offset to center the visual circle inside the larger canvas
            val topLeftOffset = (size.width - visualCircleSizePx) / 2f

            // Draw the background track
            drawArc(
                color = surfaceVariantColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(topLeftOffset, topLeftOffset),
                size = Size(visualCircleSizePx, visualCircleSizePx),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )

            // Draw the active part of the selector
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = angle,
                useCenter = false,
                topLeft = Offset(topLeftOffset, topLeftOffset),
                size = Size(visualCircleSizePx, visualCircleSizePx),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Calculate the position for the handle on the visual circle's path
            val angleInRad = (angle - 90) * (PI.toFloat() / 180f)
            val handleX = center.x + visualRadius * cos(angleInRad)
            val handleY = center.y + visualRadius * sin(angleInRad)
            val handleCenter = Offset(handleX, handleY)
            val handleRadius = strokeWidth * 1.4f

            // --- Redesigned Handle ---

            // 1. Draw a soft drop shadow for a 3D effect
            drawCircle(
                color = Color.Black.copy(alpha = 0.2f),
                radius = handleRadius,
                center = handleCenter.copy(x = handleCenter.x + 4.dp.toPx(), y = handleCenter.y + 6.dp.toPx())
            )

            // 2. Draw the main handle body
            drawCircle(
                color = primaryColor,
                radius = handleRadius,
                center = handleCenter
            )

            // 3. Draw the inner circle for contrast and detail
            drawCircle(
                color = Color.White,
                radius = handleRadius * 0.45f,
                center = handleCenter
            )
        }
    }
}
