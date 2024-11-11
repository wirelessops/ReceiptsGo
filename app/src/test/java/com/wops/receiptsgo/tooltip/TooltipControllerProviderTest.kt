package com.wops.receiptsgo.tooltip

import com.wops.receiptsgo.ocr.widget.tooltip.OcrInformationTooltipController
import com.wops.receiptsgo.tooltip.backup.AutomaticBackupRecoveryHintUserController
import com.wops.receiptsgo.tooltip.image.ImageCroppingTooltipController
import com.wops.receiptsgo.tooltip.model.TooltipType
import com.wops.receiptsgo.tooltip.privacy.PrivacyPolicyTooltipController
import com.wops.receiptsgo.tooltip.rating.RateThisAppTooltipController
import com.wops.receiptsgo.tooltip.receipt.paymentmethods.FirstReceiptUsePaymentMethodsQuestionTooltipController
import com.wops.receiptsgo.tooltip.receipt.taxes.FirstReceiptUseTaxesQuestionTooltipController
import com.wops.receiptsgo.tooltip.receipt.taxes.ConfigureSecondTaxHintTooltipController
import com.wops.receiptsgo.tooltip.report.FirstReportHintTooltipController
import org.junit.Assert.assertTrue
import org.junit.Before
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

    @Mock
    private lateinit var firstReceiptUseTaxesQuestionTooltipController: FirstReceiptUseTaxesQuestionTooltipController

    @Mock
    private lateinit var firstReceiptUsePaymentMethodsQuestionTooltipProvider: FirstReceiptUsePaymentMethodsQuestionTooltipController

    @Mock
    private lateinit var croppingTooltipController: ImageCroppingTooltipController

    @Mock
    private lateinit var includeSecondTaxHintTooltipController: ConfigureSecondTaxHintTooltipController

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        tooltipControllerProvider = TooltipControllerProvider(Provider { return@Provider automaticBackupRecoveryHintUserController },
            Provider { return@Provider firstReportHintTooltipController },
            Provider { return@Provider privacyPolicyTooltipController },
            Provider { return@Provider rateThisAppTooltipController },
            Provider { return@Provider ocrInformationTooltipController },
            Provider { return@Provider firstReceiptUseTaxesQuestionTooltipController },
            Provider { return@Provider firstReceiptUsePaymentMethodsQuestionTooltipProvider },
            Provider { return@Provider croppingTooltipController },
            Provider { return@Provider includeSecondTaxHintTooltipController })
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
    fun getRateThisAppTooltipController() {
        assertTrue(tooltipControllerProvider.get(TooltipType.RateThisApp) is RateThisAppTooltipController)
    }

    @Test
    fun getOcrInformationTooltipController() {
        assertTrue(tooltipControllerProvider.get(TooltipType.OcrInformation) is OcrInformationTooltipController)
    }

    @Test
    fun getFirstReceiptUseTaxesTooltipController() {
        assertTrue(tooltipControllerProvider.get(TooltipType.FirstReceiptUseTaxesQuestion) is FirstReceiptUseTaxesQuestionTooltipController)
    }

    @Test
    fun getFirstReceiptUsePaymentMethodsQuestionTooltipController() {
        assertTrue(tooltipControllerProvider.get(TooltipType.FirstReceiptUsePaymentMethodsQuestion) is FirstReceiptUsePaymentMethodsQuestionTooltipController)
    }

    @Test
    fun getImageCroppingTooltipController() {
        assertTrue(tooltipControllerProvider.get(TooltipType.ImageCropping) is ImageCroppingTooltipController)
    }

    @Test
    fun getIncludeSecondTaxHintTooltipController() {
        assertTrue(tooltipControllerProvider.get(TooltipType.ConfigureSecondTaxHint) is ConfigureSecondTaxHintTooltipController)
    }

}