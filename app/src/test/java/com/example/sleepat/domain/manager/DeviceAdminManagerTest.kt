package com.example.sleepat.domain.manager

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.sleepat.service.SleepDeviceAdminReceiver
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DeviceAdminManagerTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockDevicePolicyManager: DevicePolicyManager

    @Captor
    private lateinit var intentCaptor: ArgumentCaptor<Intent>

    private lateinit var deviceAdminManager: DeviceAdminManager

    private lateinit var componentName: ComponentName

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        // Mock the getSystemService call to return our mocked manager
        whenever(mockContext.getSystemService(Context.DEVICE_POLICY_SERVICE)).thenReturn(mockDevicePolicyManager)

        // Mock getPackageName because ComponentName's constructor needs it
        whenever(mockContext.packageName).thenReturn("com.example.sleepat")

        // This is the component our app uses to identify the admin receiver
        componentName = ComponentName(mockContext, SleepDeviceAdminReceiver::class.java)

        // Initialize the class under test
        deviceAdminManager = DeviceAdminManager(mockContext)
    }

    @Test
    fun `isAdminActive returns true when policy manager says admin is active`() {
        // Arrange: Tell the mock manager to return true
        whenever(mockDevicePolicyManager.isAdminActive(any())).thenReturn(true)

        // Act: Call the method we are testing
        val isActive = deviceAdminManager.isAdminActive()

        // Assert: Check if the result is what we expect
        assertTrue(isActive)
    }

    @Test
    fun `isAdminActive returns false when policy manager says admin is not active`() {
        // Arrange
        whenever(mockDevicePolicyManager.isAdminActive(any())).thenReturn(false)

        // Act
        val isActive = deviceAdminManager.isAdminActive()

        // Assert
        assertFalse(isActive)
    }



    @Test
    fun `lockScreen calls lockNow when admin is active`() {
        // Arrange
        whenever(mockDevicePolicyManager.isAdminActive(any())).thenReturn(true)

        // Act
        deviceAdminManager.lockScreen()

        // Assert
        verify(mockDevicePolicyManager).lockNow()
    }

    @Test
    fun `lockScreen does not call lockNow when admin is not active`() {
        // Arrange
        whenever(mockDevicePolicyManager.isAdminActive(any())).thenReturn(false)

        // Act
        deviceAdminManager.lockScreen()

        // Assert
        verify(mockDevicePolicyManager, never()).lockNow()
    }
}
