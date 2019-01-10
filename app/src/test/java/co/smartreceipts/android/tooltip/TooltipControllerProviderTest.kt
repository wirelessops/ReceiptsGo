package co.smartreceipts.android.tooltip

import co.smartreceipts.android.ocr.widget.tooltip.OcrInformationTooltipController
import co.smartreceipts.android.tooltip.backup.AutomaticBackupRecoveryHintUserController
import co.smartreceipts.android.tooltip.model.TooltipType
import co.smartreceipts.android.tooltip.privacy.PrivacyPolicyTooltipController
import co.smartreceipts.android.tooltip.rating.RateThisAppTooltipController
import co.smartreceipts.android.tooltip.report.FirstReportHintTooltipController
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import javax.inject.Provider

@RunWith(RobolectricTestRunner::class)
class TooltipControllerProviderTest {

    private lateinit var tooltipControllerProvider: TooltipControllerProvider

    @Mock
    private lateinit var automaticBackupRecoveryHintUserController: AutomaticBackupRecoveryHintUserController

    @Mock
    private lateinit var firstReportHintTooltipController: FirstReportHintTooltipController

    @Mock
    private lateinit var privacyPolicyTooltipController: PrivacyPolicyTooltipController

    @Mock
    private lateinit var rateThisAppTooltipController: RateThisAppTooltipController

    @Mock
    private lateinit var ocrInformationTooltipController: OcrInformationTooltipController

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        tooltipControllerProvider = TooltipControllerProvider(Provider { return@Provider automaticBackupRecoveryHintUserController },
                                                              Provider { return@Provider firstReportHintTooltipController },
                                                              Provider { return@Provider privacyPolicyTooltipController },
                                                              Provider { return@Provider rateThisAppTooltipController },
                                                              Provider { return@Provider ocrInformationTooltipController })
    }

    @Test
    fun getAutomaticBackupRecoveryHintUserController() {
        assertTrue(tooltipControllerProvider.get(TooltipType.AutomaticBackupRecoveryHint) is AutomaticBackupRecoveryHintUserController)
    }

    @Test
    fun getFirstReportHintTooltipController() {
        assertTrue(tooltipControllerProvider.get(TooltipType.FirstReportHint) is FirstReportHintTooltipController)
    }

    @Test
    fun getPrivacyPolicyTooltipController() {
        assertTrue(tooltipControllerProvider.get(TooltipType.PrivacyPolicy) is PrivacyPolicyTooltipController)
    }

    @Test
    fun getAppRatingTooltipController() {
        assertTrue(tooltipControllerProvider.get(TooltipType.RateThisApp) is RateThisAppTooltipController)
    }

}