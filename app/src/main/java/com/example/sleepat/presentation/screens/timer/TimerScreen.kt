package com.example.sleepat.presentation.screens.timer

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.graphics.StrokeCap

import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

import kotlin.math.sin
import kotlin.random.Random

// Data class para representar una burbuja
data class Bubble(
    val id: Int,
    val startX: Float,
    val startY: Float,
    val size: Float,
    val speed: Float,
    val oscillation: Float
)

@Composable
fun BubblesAnimation(
    modifier: Modifier = Modifier,
    isAnimating: Boolean = true,
    centerColor: Color,
    edgeColor: Color,
    bubbles: List<Bubble>,
    resetTrigger: Boolean // Nuevo parámetro para forzar el reinicio
) {
    // Cada vez que cambia resetTrigger, la animación se reinicia completamente
    val infiniteTransition = rememberInfiniteTransition(label = "bubbles_$resetTrigger")
    val animationTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubbleAnimation"
    )

    Canvas(modifier = modifier) {
        if (isAnimating) {
            bubbles.forEach { bubble ->
                val progress = (animationTime + bubble.id * 0.1f) % 1f
                // Las burbujas empiezan desde abajo (fuera de la pantalla) y suben
                val y = size.height + bubble.size - (progress * (size.height + bubble.size * 2))
                val oscillationOffset = sin(progress * 6.28f * 3f + bubble.id) * 25f * bubble.oscillation
                val x = size.width * bubble.startX + oscillationOffset
                
                if (y > -bubble.size && y < size.height + bubble.size) {
                    // Alpha que se desvanece en los bordes
                    val alpha = when {
                        y < bubble.size -> (y + bubble.size) / (bubble.size * 2)
                        y > size.height - bubble.size -> (size.height - y + bubble.size) / (bubble.size * 2)
                        else -> 1f
                    }.coerceIn(0f, 0.7f)
                    
                    val center = Offset(x, y)
                    
                    // Fondo transparente de la burbuja
                    drawCircle(
                        color = Color.White.copy(alpha = alpha * 0.15f),
                        radius = bubble.size,
                        center = center
                    )
                    
                    // Borde blanco de la burbuja
                    drawCircle(
                        color = Color.White.copy(alpha = alpha * 0.8f),
                        radius = bubble.size,
                        center = center,
                        style = Stroke(
                            width = 1.5f,
                            cap = StrokeCap.Round
                        )
                    )
                    
                    // Brillo interior para efecto de refracción
                    drawCircle(
                        color = Color.White.copy(alpha = alpha * 0.3f),
                        radius = bubble.size * 0.4f,
                        center = Offset(
                            x - bubble.size * 0.3f,
                            y - bubble.size * 0.3f
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun OceanWavesBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ocean_waves")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_animation"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        
        // Ondas sutiles del océano
        for (i in 0..3) {
            val alpha = 0.1f - (i * 0.02f)
            val waveHeight = 30f + (i * 10f)
            val frequency = 0.006f + (i * 0.002f)
            
            val path = androidx.compose.ui.graphics.Path()
            path.moveTo(0f, height * 0.3f + (i * height * 0.1f))
            
            for (x in 0..width.toInt() step 5) {
                val y = height * 0.3f + (i * height * 0.1f) + 
                        sin((x * frequency + waveOffset * 6.28f)) * waveHeight
                path.lineTo(x.toFloat(), y)
            }
            
            path.lineTo(width, height)
            path.lineTo(0f, height)
            path.close()
            
            drawPath(
                path = path,
                color = Color(0xFF0891B2).copy(alpha = alpha)
            )
        }
    }
}

@Composable
fun TimerScreen(
    viewModel: TimerViewModel = hiltViewModel(),
    shouldExtendTimer: Boolean = false
) {
    val selectedMinutes by viewModel.selectedMinutes.collectAsState()
    val timeLeftInSeconds by viewModel.timeLeftInSeconds.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()

    // Efecto para extender el timer automáticamente cuando se presiona el botón en la notificación
    LaunchedEffect(shouldExtendTimer) {
        if (shouldExtendTimer && isTimerRunning) {
            viewModel.extendTimer()
        }
    }

    // Burbujas compartidas que se regeneran cada vez que cambia el estado del timer
    val sharedBubbles = remember(isTimerRunning) {
        (0..12).map { id ->
            Bubble(
                id = id,
                startX = Random.nextFloat(),
                startY = 1.2f,
                size = Random.nextFloat() * 15f + 8f,
                speed = Random.nextFloat() * 0.02f + 0.01f,
                oscillation = Random.nextFloat() * 0.02f + 0.01f
            )
        }
    }

    // Animaciones
    val infiniteRotation = rememberInfiniteTransition(label = "rotation")
    val rotationAngle by infiniteRotation.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulseAnimation = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Animación de transición entre estados
    val targetScale = if (isTimerRunning) 1f else 0.9f
    val scaleAnimation by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    // Animación de colores del gradiente
    val centerColor by animateColorAsState(
        targetValue = if (isTimerRunning) Color(0xFF0EA5E9) else Color(0xFF06B6D4), // Azul cyan vibrante
        animationSpec = tween(800),
        label = "centerColor"
    )
    val edgeColor by animateColorAsState(
        targetValue = if (isTimerRunning) Color(0xFF14B8A6) else Color(0xFF0EA5E9), // Verde agua/turquesa
        animationSpec = tween(800),
        label = "edgeColor"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fondo de ondas del océano
        OceanWavesBackground(
            modifier = Modifier.fillMaxSize()
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A).copy(alpha = 0.7f), // Azul oscuro profundo (superficie)
                            Color(0xFF1E293B).copy(alpha = 0.8f), // Azul gris medio
                            Color(0xFF0F766E).copy(alpha = 0.8f), // Verde azulado (agua media)
                            Color(0xFF134E4A).copy(alpha = 0.9f), // Verde oscuro (profundidades)
                            Color(0xFF064E3B).copy(alpha = 0.95f) // Verde muy oscuro (fondo marino)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
        // Spacer superior para centrar el componente principal
        Spacer(modifier = Modifier.weight(1f))
        
        Box(
            modifier = Modifier.size(300.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isTimerRunning) {
                // Mostrar solo minutos cuando el timer está corriendo con diseño atractivo
                val minutes = kotlin.math.ceil(timeLeftInSeconds / 60.0).toInt()
                
                // Círculo contenedor transparente con borde
                Card(
                    modifier = Modifier
                        .size(250.dp)
                        .scale(scaleAnimation * if (isTimerRunning) pulseScale else 1f)
                        .shadow(
                            elevation = if (isTimerRunning) 20.dp else 16.dp,
                            shape = CircleShape,
                            ambientColor = centerColor.copy(alpha = 0.3f),
                            spotColor = edgeColor.copy(alpha = 0.3f)
                        ),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    border = BorderStroke(
                        width = 3.dp,
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                centerColor.copy(alpha = 0.8f),
                                edgeColor.copy(alpha = 0.8f),
                                centerColor.copy(alpha = 0.8f)
                            )
                        )
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    ) {
                        // Burbujas animadas de fondo
                        BubblesAnimation(
                            modifier = Modifier.fillMaxSize(),
                            isAnimating = isTimerRunning,
                            centerColor = centerColor,
                            edgeColor = edgeColor,
                            bubbles = sharedBubbles,
                            resetTrigger = isTimerRunning
                        )
                    }
                }
                
                // Texto que NO rota (superpuesto al círculo)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.scale(scaleAnimation * if (isTimerRunning) pulseScale else 1f)
                ) {
                    Text(
                        text = "$minutes",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color(0xFF1E293B).copy(alpha = 0.8f),
                                offset = androidx.compose.ui.geometry.Offset(3f, 3f),
                                blurRadius = 6f
                            )
                        ),
                        modifier = Modifier.graphicsLayer {
                            // Efecto de brillo sutil en el texto
                            scaleX = 1f + (pulseScale - 1f) * 0.3f
                            scaleY = 1f + (pulseScale - 1f) * 0.3f
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "minutos",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.95f),
                        textAlign = TextAlign.Center,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color(0xFF1E293B).copy(alpha = 0.6f),
                                offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        ),
                        modifier = Modifier.graphicsLayer {
                            alpha = 0.8f + (pulseScale - 1f) * 2f
                        }
                    )
                }
            } else {
                CircularTimeSelector(
                    modifier = Modifier.fillMaxSize(),
                    initialMinutes = selectedMinutes,
                    onTimeChange = { viewModel.updateSelectedMinutes(it) }
                ) {
                    // Círculo contenedor transparente con borde (idéntico al del timer corriendo)
                    Card(
                        modifier = Modifier
                            .size(250.dp)
                            .scale(scaleAnimation)
                            .shadow(
                                elevation = 16.dp,
                                shape = CircleShape,
                                ambientColor = centerColor.copy(alpha = 0.3f),
                                spotColor = edgeColor.copy(alpha = 0.3f)
                            ),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        ),
                        border = BorderStroke(
                            width = 3.dp,
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    centerColor.copy(alpha = 0.8f),
                                    edgeColor.copy(alpha = 0.8f),
                                    centerColor.copy(alpha = 0.8f)
                                )
                            )
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        ) {
                            // Burbujas animadas de fondo (idénticas al timer corriendo)
                            BubblesAnimation(
                                modifier = Modifier.fillMaxSize(),
                                isAnimating = true,
                                centerColor = centerColor,
                                edgeColor = edgeColor,
                                bubbles = sharedBubbles,
                                resetTrigger = !isTimerRunning
                            )
                        }
                    }
                }
                
                // Texto que se superpone al círculo (idéntico al del timer corriendo)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.scale(scaleAnimation)
                ) {
                    Text(
                        text = "$selectedMinutes",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color(0xFF1E293B).copy(alpha = 0.8f),
                                offset = androidx.compose.ui.geometry.Offset(3f, 3f),
                                blurRadius = 6f
                            )
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "minutos",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.95f),
                        textAlign = TextAlign.Center,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color(0xFF1E293B).copy(alpha = 0.6f),
                                offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                                blurRadius = 4f
                            )
                        )
                    )
                }
            }
        }

        // Spacer flexible para empujar los botones hacia abajo
        Spacer(modifier = Modifier.weight(1f))

        // Botones en la parte inferior con diseño elegante y bordes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.startTimer() },
                enabled = !isTimerRunning,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(
                    width = 2.dp,
                    color = if (!isTimerRunning) Color(0xFF0EA5E9) else Color(0xFF0EA5E9).copy(alpha = 0.3f) // Azul cyan
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (!isTimerRunning) Color(0xFF0EA5E9) else Color(0xFF0EA5E9).copy(alpha = 0.5f),
                    disabledContentColor = Color(0xFF0EA5E9).copy(alpha = 0.3f)
                )
            ) {
                Text(
                    "Iniciar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            OutlinedButton(
                onClick = { viewModel.stopTimer() },
                enabled = isTimerRunning,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(
                    width = 2.dp,
                    color = if (isTimerRunning) Color(0xFF14B8A6) else Color(0xFF14B8A6).copy(alpha = 0.3f) // Verde turquesa
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isTimerRunning) Color(0xFF14B8A6) else Color(0xFF14B8A6).copy(alpha = 0.5f),
                    disabledContentColor = Color(0xFF14B8A6).copy(alpha = 0.3f)
                )
            ) {
                Text(
                    "Detener",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        }
    }
}
