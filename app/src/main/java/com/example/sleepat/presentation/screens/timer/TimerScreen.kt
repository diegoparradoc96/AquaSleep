package com.example.sleepat.presentation.screens.timer

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sleepat.R
import com.example.sleepat.presentation.components.LanguageSelector
import com.example.sleepat.presentation.components.updateLanguage
import kotlin.math.sin
import kotlin.random.Random

// Constantes para colores
private object TimerColors {
    val CyanBlue = Color(0xFF0EA5E9)
    val AquaBlue = Color(0xFF06B6D4)
    val TurquoiseGreen = Color(0xFF14B8A6)
    val OceanWave = Color(0xFF0891B2)
    val DeepBlue = Color(0xFF0F172A)
    val MidBlue = Color(0xFF1E293B)
    val AquaGreen = Color(0xFF0F766E)
    val DarkGreen = Color(0xFF134E4A)
    val VeryDarkGreen = Color(0xFF064E3B)
    val TextShadow = Color(0xFF1E293B)
}

// Data class para representar una burbuja
data class Bubble(
    val id: Int,
    val startX: Float,
    val size: Float,
    val oscillation: Float
)

@Composable
fun BubblesAnimation(
    modifier: Modifier = Modifier,
    isAnimating: Boolean = true,
    bubbles: List<Bubble>,
    resetTrigger: Boolean
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
    modifier: Modifier = Modifier,
    isAnimating: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ocean_waves")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isAnimating) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_animation"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        
        // Ondas sutiles del océano - solo se animan cuando isAnimating es true
        for (i in 0..3) {
            val alpha = if (isAnimating) 0.1f - (i * 0.02f) else 0.05f - (i * 0.01f)
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
                color = TimerColors.OceanWave.copy(alpha = alpha)
            )
        }
    }
}

@Composable
private fun TimerCircle(
    modifier: Modifier = Modifier,
    centerColor: Color,
    edgeColor: Color,
    bubbles: List<Bubble>,
    resetTrigger: Boolean,
    isAnimating: Boolean,
    scale: Float,
    elevation: Float = 16.dp.value
) {
    Card(
        modifier = modifier
            .size(250.dp)
            .scale(scale)
            .shadow(
                elevation = elevation.dp,
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
                isAnimating = isAnimating,
                bubbles = bubbles,
                resetTrigger = resetTrigger
            )
        }
    }
}

@Composable
private fun TimerText(
    minutes: Int,
    scale: Float,
    pulseScale: Float = 1f,
    showPulse: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(scale * if (showPulse) pulseScale else 1f)
    ) {
        Text(
            text = "$minutes",
            fontSize = 72.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center,
            style = androidx.compose.ui.text.TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = TimerColors.TextShadow.copy(alpha = 0.8f),
                    offset = androidx.compose.ui.geometry.Offset(3f, 3f),
                    blurRadius = 6f
                )
            ),
            modifier = if (showPulse) {
                Modifier.graphicsLayer {
                    // Efecto de brillo sutil en el texto
                    scaleX = 1f + (pulseScale - 1f) * 0.3f
                    scaleY = 1f + (pulseScale - 1f) * 0.3f
                }
            } else {
                Modifier
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.timer_minutes),
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.95f),
            textAlign = TextAlign.Center,
            style = androidx.compose.ui.text.TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = TimerColors.TextShadow.copy(alpha = 0.6f),
                    offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                    blurRadius = 4f
                )
            ),
            modifier = if (showPulse) {
                Modifier.graphicsLayer {
                    alpha = 0.8f + (pulseScale - 1f) * 2f
                }
            } else {
                Modifier
            }
        )
    }
}

@Composable
fun TimerScreen(
    viewModel: TimerViewModel = hiltViewModel(),
    shouldExtendTimer: Boolean = false
) {
    val context = LocalContext.current
    val selectedMinutes by viewModel.selectedMinutes.collectAsState()
    val timeLeftInSeconds by viewModel.timeLeftInSeconds.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    
    // Estado para controlar el cambio de idioma
    var languageToApply by remember { mutableStateOf<String?>(null) }

    // Efecto para extender el timer automáticamente cuando se presiona el botón en la notificación
    LaunchedEffect(shouldExtendTimer) {
        if (shouldExtendTimer && isTimerRunning) {
            viewModel.extendTimer()
        }
    }
    
    // Efecto para aplicar el cambio de idioma de forma segura
    LaunchedEffect(languageToApply) {
        languageToApply?.let { languageCode ->
            try {
                // Guardar el idioma seleccionado
                val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                sharedPrefs.edit().putString("selected_language", languageCode).apply()
                
                // Delay para permitir que el menú se cierre suavemente
                kotlinx.coroutines.delay(100)
                
                // Usar recreate() que es más estable
                (context as? androidx.activity.ComponentActivity)?.recreate()
            } catch (e: Exception) {
                // Si hay un error, al menos se guarda el idioma
                e.printStackTrace()
            }
        }
    }

    // Burbujas que se regeneran cada vez que cambia el estado del timer
    val sharedBubbles = remember(isTimerRunning) {
        (0..12).map { id ->
            Bubble(
                id = id,
                startX = Random.nextFloat(),
                size = Random.nextFloat() * 15f + 8f,
                oscillation = Random.nextFloat() * 0.02f + 0.01f
            )
        }
    }

    // Animaciones
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
        targetValue = if (isTimerRunning) TimerColors.CyanBlue else TimerColors.AquaBlue,
        animationSpec = tween(800),
        label = "centerColor"
    )
    val edgeColor by animateColorAsState(
        targetValue = if (isTimerRunning) TimerColors.TurquoiseGreen else TimerColors.CyanBlue,
        animationSpec = tween(800),
        label = "edgeColor"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fondo de ondas del océano - solo se anima cuando el timer está corriendo
        OceanWavesBackground(
            modifier = Modifier.fillMaxSize(),
            isAnimating = isTimerRunning
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            TimerColors.DeepBlue.copy(alpha = 0.7f),
                            TimerColors.MidBlue.copy(alpha = 0.8f),
                            TimerColors.AquaGreen.copy(alpha = 0.8f),
                            TimerColors.DarkGreen.copy(alpha = 0.9f),
                            TimerColors.VeryDarkGreen.copy(alpha = 0.95f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
        // Selector de idioma en la parte superior
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            LanguageSelector(
                onLanguageChanged = { languageCode ->
                    languageToApply = languageCode
                }
            )
        }
        // Spacer superior para centrar el componente principal
        Spacer(modifier = Modifier.weight(1f))
        
        Box(
            modifier = Modifier.size(300.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isTimerRunning) {
                // Mostrar solo minutos cuando el timer está corriendo con diseño atractivo
                val minutes = kotlin.math.ceil(timeLeftInSeconds / 60.0).toInt()
                
                // Círculo contenedor con animación de pulso
                TimerCircle(
                    centerColor = centerColor,
                    edgeColor = edgeColor,
                    bubbles = sharedBubbles,
                    resetTrigger = isTimerRunning,
                    isAnimating = isTimerRunning,
                    scale = scaleAnimation * pulseScale,
                    elevation = 20.dp.value
                )
                
                // Texto superpuesto con animación de pulso
                TimerText(
                    minutes = minutes,
                    scale = scaleAnimation,
                    pulseScale = pulseScale,
                    showPulse = true
                )
            } else {
                CircularTimeSelector(
                    modifier = Modifier.fillMaxSize(),
                    initialMinutes = selectedMinutes,
                    onTimeChange = { viewModel.updateSelectedMinutes(it) }
                ) {
                    // Círculo contenedor para selector - sin animación de burbujas
                    TimerCircle(
                        centerColor = centerColor,
                        edgeColor = edgeColor,
                        bubbles = sharedBubbles,
                        resetTrigger = !isTimerRunning,
                        isAnimating = false, // No animar burbujas cuando no esté corriendo
                        scale = scaleAnimation
                    )
                }
                
                // Texto superpuesto para selector
                TimerText(
                    minutes = selectedMinutes,
                    scale = scaleAnimation
                )
            }
        }

        // Spacer flexible para empujar los botones hacia abajo
        Spacer(modifier = Modifier.weight(1f))

        // Botones en la parte inferior
        TimerButtons(
            isTimerRunning = isTimerRunning,
            onStartClick = { viewModel.startTimer() },
            onStopClick = { viewModel.stopTimer() }
        )
        }
    }
}

@Composable
private fun TimerButtons(
    isTimerRunning: Boolean,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedButton(
            onClick = onStartClick,
            enabled = !isTimerRunning,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(
                width = 2.dp,
                color = if (!isTimerRunning) TimerColors.CyanBlue else TimerColors.CyanBlue.copy(alpha = 0.3f)
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (!isTimerRunning) TimerColors.CyanBlue else TimerColors.CyanBlue.copy(alpha = 0.5f),
                disabledContentColor = TimerColors.CyanBlue.copy(alpha = 0.3f)
            )
        ) {
            Text(
                stringResource(R.string.timer_start),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        OutlinedButton(
            onClick = onStopClick,
            enabled = isTimerRunning,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(
                width = 2.dp,
                color = if (isTimerRunning) TimerColors.TurquoiseGreen else TimerColors.TurquoiseGreen.copy(alpha = 0.3f)
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (isTimerRunning) TimerColors.TurquoiseGreen else TimerColors.TurquoiseGreen.copy(alpha = 0.5f),
                disabledContentColor = TimerColors.TurquoiseGreen.copy(alpha = 0.3f)
            )
        ) {
            Text(
                stringResource(R.string.timer_stop),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
