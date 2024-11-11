package com.wops.receiptsgo.tooltip.backup

import com.wops.receiptsgo.activities.NavigationHandler
import com.wops.receiptsgo.activities.SmartReceiptsActivity
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AutomaticBackupRecoveryHintRouterTest {

    lateinit var hintRouter: AutomaticBackupRecoveryHintRouter

    @Mock
    lateinit var navigationHandler: NavigationHandler<SmartReceiptsActivity>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        hintRouter = AutomaticBackupRecoveryHintRouter(navigationHandler)
    }

    @Test
    fun navigateToPrivacyPolicyControls() {
        hintRouter.navigateToAutomaticBackupConfiguration()
        verify(navigationHandler).navigateToBackupMenu()
    }
}