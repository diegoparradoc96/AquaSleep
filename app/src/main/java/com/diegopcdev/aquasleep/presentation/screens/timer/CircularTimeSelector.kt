package com.diegopcdev.aquasleep.presentation.screens.timer

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

            // Draw the background track (más sutil y oceánico)
            drawArc(
                color = Color(0xFF1E293B).copy(alpha = 0.3f), // Azul gris oceánico
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(topLeftOffset, topLeftOffset),
                size = Size(visualCircleSizePx, visualCircleSizePx),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )

            // Draw the active part of the selector (gradiente oceánico)
            drawArc(
                color = Color(0xFF0EA5E9), // Azul cyan vibrante del tema oceánico
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

            // --- Burbuja Oceánica como Handle ---

            // 1. Sombra suave para profundidad oceánica
            drawCircle(
                color = Color(0xFF0F172A).copy(alpha = 0.4f), // Azul profundo del océano
                radius = handleRadius * 1.1f,
                center = handleCenter.copy(
                    x = handleCenter.x + 3.dp.toPx(), 
                    y = handleCenter.y + 4.dp.toPx()
                )
            )

            // 2. Fondo transparente de la burbuja (cuerpo principal)
            drawCircle(
                color = Color.White.copy(alpha = 0.25f), // Transparencia como el agua
                radius = handleRadius,
                center = handleCenter
            )

            // 3. Borde brillante de la burbuja (efecto de tensión superficial)
            drawCircle(
                color = Color.White.copy(alpha = 0.9f), // Borde blanco brillante
                radius = handleRadius,
                center = handleCenter,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // 4. Brillo interior para efecto de refracción (highlight principal)
            drawCircle(
                color = Color.White.copy(alpha = 0.6f),
                radius = handleRadius * 0.4f,
                center = Offset(
                    handleCenter.x - handleRadius * 0.3f,
                    handleCenter.y - handleRadius * 0.3f
                )
            )

            // 5. Segundo brillo más pequeño para mayor realismo
            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                radius = handleRadius * 0.15f,
                center = Offset(
                    handleCenter.x + handleRadius * 0.2f,
                    handleCenter.y - handleRadius * 0.4f
                )
            )

            // 6. Reflejos sutiles en el borde para efecto vítreo
            drawCircle(
                color = Color(0xFF0EA5E9).copy(alpha = 0.3f), // Azul cyan del tema
                radius = handleRadius * 0.8f,
                center = handleCenter,
                style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
