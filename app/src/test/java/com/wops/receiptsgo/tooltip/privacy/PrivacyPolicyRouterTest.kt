package com.wops.receiptsgo.tooltip.privacy

import com.wops.receiptsgo.activities.NavigationHandler
import com.wops.receiptsgo.activities.ReceiptsGoActivity
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.mockito.Mock
import com.nhaarman.mockitokotlin2.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PrivacyPolicyRouterTest {

    lateinit var router: PrivacyPolicyRouter

    @Mock
    lateinit var navigationHandler: NavigationHandler<ReceiptsGoActivity>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        router = PrivacyPolicyRouter(navigationHandler)
    }

    @Test
    fun navigateToPrivacyPolicyControls() {
        router.navigateToPrivacyPolicyControls()
        verify(navigationHandler).navigateToSettingsScrollToPrivacySection()
    }

}