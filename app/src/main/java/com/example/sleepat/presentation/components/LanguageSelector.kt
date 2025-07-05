package com.example.sleepat.presentation.components

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sleepat.R
import com.example.sleepat.service.TimerForegroundService
import java.util.*

data class Language(
    val code: String,
    val name: String,
    val flag: String
)

@Composable
fun LanguageSelector(
    modifier: Modifier = Modifier,
    onLanguageChanged: (String) -> Unit
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    
    val languages = listOf(
        Language("es", "Español", "🇪🇸"),
        Language("en", "English", "🇺🇸")
    )
    
    val currentLanguage = getCurrentLanguage(context)
    val selectedLanguage = languages.find { it.code == currentLanguage } ?: languages[0]
    
    Box(modifier = modifier) {
        // Botón selector de idioma
        Card(
            modifier = Modifier
                .clickable { showMenu = true }
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.2f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "🌐",
                    fontSize = 18.sp
                )
                Text(
                    text = selectedLanguage.flag,
                    fontSize = 16.sp
                )
                Text(
                    text = selectedLanguage.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Menú desplegable
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(
                Color.White,
                shape = RoundedCornerShape(12.dp)
            )
        ) {
            languages.forEach { language ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = language.flag,
                                fontSize = 18.sp
                            )
                            Text(
                                text = language.name,
                                fontSize = 16.sp,
                                fontWeight = if (language.code == currentLanguage) FontWeight.Bold else FontWeight.Normal,
                                color = if (language.code == currentLanguage) Color(0xFF0EA5E9) else Color.Black
                            )
                        }
                    },
                    onClick = {
                        // Cerrar el menú inmediatamente
                        showMenu = false
                        // Aplicar el cambio de idioma en el próximo frame
                        onLanguageChanged(language.code)
                    },
                    modifier = Modifier.background(
                        if (language.code == currentLanguage) 
                            Color(0xFF0EA5E9).copy(alpha = 0.1f) 
                        else 
                            Color.Transparent
                    )
                )
            }
        }
    }
}

private fun getCurrentLanguage(context: Context): String {
    return context.resources.configuration.locales[0].language
}

fun updateLanguage(context: Context, languageCode: String) {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    
    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)
    
    // Guardar el idioma en SharedPreferences para persistencia
    val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    sharedPrefs.edit().putString("selected_language", languageCode).apply()
    
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
    
    // Notificar al servicio sobre el cambio de idioma si está ejecutándose
    try {
        val serviceIntent = Intent(context, TimerForegroundService::class.java)
        serviceIntent.action = "UPDATE_LANGUAGE"
        context.startService(serviceIntent)
    } catch (e: Exception) {
        // El servicio no está disponible, continuar normalmente
    }
}

fun getStoredLanguage(context: Context): String {
    val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    return sharedPrefs.getString("selected_language", "es") ?: "es"
}
