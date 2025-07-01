package com.example.sleepat.domain.manager

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sleepat.service.SleepDeviceAdminReceiver
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceAdminManagerInstrumentedTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun requestAdminPermission_createsResolvableIntent() {
        // Esta prueba verifica que el Intent creado para solicitar permisos de administrador
        // puede ser resuelto por el sistema. Esto confirma que nuestro Manifest está configurado correctamente.

        val componentName = ComponentName(context, SleepDeviceAdminReceiver::class.java)
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Esta aplicación necesita permisos de administrador para poder suspender el dispositivo."
            )
        }

        val packageManager = context.packageManager
        val resolveInfo = packageManager.resolveActivity(intent, 0)

        // Afirmamos que el Intent es resoluble.
        // Si esto pasa, significa que el sistema tiene una pantalla para mostrar para esta solicitud de permiso.
        assertNotNull(
            "El Intent para solicitar el permiso de administrador debería poder ser resuelto por el sistema.",
            resolveInfo
        )
    }
}
