package com.example.sleepat.presentation.screens.timer

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.assertEquals
import com.example.sleepat.domain.manager.DeviceAdminManager
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : org.junit.rules.TestWatcher() {
    override fun starting(description: org.junit.runner.Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: org.junit.runner.Description) {
        Dispatchers.resetMain()
    }
}

@ExperimentalCoroutinesApi
class TimerViewModelTest {

    @Mock
    private lateinit var mockDeviceAdminManager: DeviceAdminManager

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: TimerViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        whenever(mockDeviceAdminManager.isAdminActive()).thenReturn(true)
        viewModel = TimerViewModel(mockDeviceAdminManager)
    }

    @Test
    fun `initial state is correct`() {
        assertEquals(15, viewModel.selectedMinutes.value)
        assertEquals(15 * 60, viewModel.timeLeftInSeconds.value)
        assertEquals(false, viewModel.isTimerRunning.value)
    }

    @Test
    fun `updateSelectedMinutes updates time when timer is not running`() {
        viewModel.updateSelectedMinutes(30)
        assertEquals(30, viewModel.selectedMinutes.value)
        assertEquals(30 * 60, viewModel.timeLeftInSeconds.value)
    }

    @Test
    fun `startTimer starts countdown and updates timeLeftInSeconds`() = runTest(mainDispatcherRule.testDispatcher) {
        val initialSeconds = 15 * 60
        viewModel.startTimer()
        assertEquals(true, viewModel.isTimerRunning.value)

        advanceTimeBy(1001)
        runCurrent()
        assertEquals(initialSeconds - 1, viewModel.timeLeftInSeconds.value)

        advanceTimeBy(1001)
        runCurrent()
        assertEquals(initialSeconds - 2, viewModel.timeLeftInSeconds.value)
    }

    @Test
    fun `timer stops automatically when it reaches zero`() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel.updateSelectedMinutes(1) // 60 seconds
        viewModel.startTimer()

        advanceTimeBy(60_001L)
        runCurrent()

        assertEquals(0, viewModel.timeLeftInSeconds.value)
        assertEquals(false, viewModel.isTimerRunning.value)
    }

    @Test
    fun `stopTimer cancels the timer and resets timeLeft`() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel.updateSelectedMinutes(25)
        viewModel.startTimer()

        advanceTimeBy(2001)
        runCurrent()
        assertEquals(25 * 60 - 2, viewModel.timeLeftInSeconds.value)

        viewModel.stopTimer()
        runCurrent()

        assertEquals(false, viewModel.isTimerRunning.value)
        assertEquals(25 * 60, viewModel.timeLeftInSeconds.value)
    }
}
