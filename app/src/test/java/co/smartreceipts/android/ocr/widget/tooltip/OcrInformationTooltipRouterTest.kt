package co.smartreceipts.android.ocr.widget.tooltip

import co.smartreceipts.android.activities.NavigationHandler
import co.smartreceipts.android.activities.SmartReceiptsActivity
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
    lateinit var navigationHandler: NavigationHandler<SmartReceiptsActivity>

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