package com.aura.dating.feature.settings.viewmodel

import com.aura.dating.core.preferences.AppSettingsStorage
import com.aura.dating.core.presence.PresenceManager
import com.aura.dating.domain.auth.usecase.LogoutUseCase
import com.aura.dating.domain.moderation.usecase.DeleteAccountUseCase
import com.aura.dating.domain.moderation.usecase.GetBlockedUsersUseCase
import com.aura.dating.domain.moderation.usecase.UnblockUserUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val getBlockedUsersUseCase: GetBlockedUsersUseCase = mockk(relaxed = true)
    private val unblockUserUseCase: UnblockUserUseCase = mockk(relaxed = true)
    private val deleteAccountUseCase: DeleteAccountUseCase = mockk(relaxed = true)
    private val logoutUseCase: LogoutUseCase = mockk(relaxed = true)
    private val appSettingsStorage: AppSettingsStorage = mockk(relaxed = true)
    private val presenceManager: PresenceManager = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0

        Dispatchers.setMain(testDispatcher)

        every { getBlockedUsersUseCase.blockedUsersFlow } returns flowOf(emptyList())
        every { appSettingsStorage.pushNotificationsEnabledFlow } returns flowOf(true)
        every { appSettingsStorage.newMatchesPushFlow } returns flowOf(true)
        every { appSettingsStorage.messagesPushFlow } returns flowOf(true)
        every { appSettingsStorage.likesPushFlow } returns flowOf(true)
        every { appSettingsStorage.showOnlineStatusFlow } returns flowOf(true)
        every { appSettingsStorage.showDistanceFlow } returns flowOf(true)

        viewModel = SettingsViewModel(
            getBlockedUsersUseCase = getBlockedUsersUseCase,
            unblockUserUseCase = unblockUserUseCase,
            deleteAccountUseCase = deleteAccountUseCase,
            logoutUseCase = logoutUseCase,
            appSettingsStorage = appSettingsStorage,
            presenceManager = presenceManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(android.util.Log::class)
    }

    @Test
    fun `toggleShowOnline updates storage and syncs presence immediately`() = runTest {
        viewModel.toggleShowOnline(false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { appSettingsStorage.setShowOnlineStatus(false) }
        coVerify { presenceManager.setOnlineStatus(false) }
    }

    @Test
    fun `toggleShowDistance updates storage`() = runTest {
        viewModel.toggleShowDistance(false)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { appSettingsStorage.setShowDistance(false) }
    }
}
