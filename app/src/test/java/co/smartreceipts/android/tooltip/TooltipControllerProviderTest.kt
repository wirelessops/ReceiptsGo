package co.smartreceipts.android.tooltip

import co.smartreceipts.android.tooltip.backup.AutomaticBackupRecoveryHintUserController
import co.smartreceipts.android.tooltip.model.StaticTooltip
import co.smartreceipts.android.tooltip.privacy.PrivacyPolicyTooltipController
import co.smartreceipts.android.tooltip.rating.AppRatingTooltipController
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

    lateinit var tooltipControllerProvider: TooltipControllerProvider

    @Mock
    lateinit var automaticBackupRecoveryHintUserController: AutomaticBackupRecoveryHintUserController

    @Mock
    lateinit var privacyPolicyTooltipController: PrivacyPolicyTooltipController

    @Mock
    lateinit var appRatingTooltipController: AppRatingTooltipController

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        tooltipControllerProvider = TooltipControllerProvider(Provider { return@Provider automaticBackupRecoveryHintUserController },
                                                              Provider { return@Provider privacyPolicyTooltipController },
                                                              Provider { return@Provider appRatingTooltipController })
    }

    @Test
    fun getAutomaticBackupRecoveryHintUserController() {
        assertTrue(tooltipControllerProvider.get(StaticTooltip.AutomaticBackupRecoveryHint) is AutomaticBackupRecoveryHintUserController)
    }

    @Test
    fun getPrivacyPolicyTooltipController() {
        assertTrue(tooltipControllerProvider.get(StaticTooltip.PrivacyPolicy) is PrivacyPolicyTooltipController)
    }

    @Test
    fun getAppRatingTooltipController() {
        assertTrue(tooltipControllerProvider.get(StaticTooltip.RateThisApp) is AppRatingTooltipController)
    }

}