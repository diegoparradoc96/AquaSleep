package com.example.sleepat.domain.manager

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.sleepat.service.SleepDeviceAdminReceiver

class DeviceAdminManager(private val context: Context) {

    private val devicePolicyManager: DevicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    private val componentName: ComponentName =
        ComponentName(context, SleepDeviceAdminReceiver::class.java)

    fun isAdminActive(): Boolean {
        return devicePolicyManager.isAdminActive(componentName)
    }

    fun createAdminPermissionIntent(): Intent {
        return Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Esta aplicación necesita permisos de administrador para poder suspender el dispositivo cuando el temporizador finalice."
            )
        }
    }

    fun lockScreen() {
        if (isAdminActive()) {
            devicePolicyManager.lockNow()
        }
    }
}
