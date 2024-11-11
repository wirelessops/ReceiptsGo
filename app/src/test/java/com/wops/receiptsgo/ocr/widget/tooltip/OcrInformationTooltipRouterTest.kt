package com.wops.receiptsgo.ocr.widget.tooltip

import com.wops.receiptsgo.activities.NavigationHandler
import com.wops.receiptsgo.activities.ReceiptsGoActivity
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test

import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OcrInformationTooltipRouterTest {

    lateinit var router: OcrInformationTooltipRouter

    @Mock
    lateinit var navigationHandler: NavigationHandler<ReceiptsGoActivity>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        router = OcrInformationTooltipRouter(navigationHandler)
    }

    @Test
    fun navigateToOcrConfigurationScreen() {
        router.navigateToOcrConfigurationScreen()
        verify(navigationHandler).navigateToOcrConfigurationFragment()
    }

}