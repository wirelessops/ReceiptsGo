package com.wops.receiptsgo.tooltip.backup

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import co.smartreceipts.analytics.Analytics
import co.smartreceipts.analytics.events.Events
import com.wops.receiptsgo.R
import com.wops.receiptsgo.purchases.PurchaseManager
import com.wops.receiptsgo.purchases.model.InAppPurchase
import com.wops.receiptsgo.purchases.wallet.PurchaseWallet
import com.wops.receiptsgo.tooltip.TooltipView
import com.wops.receiptsgo.tooltip.model.TooltipInteraction
import com.wops.receiptsgo.tooltip.model.TooltipMetadata
import com.wops.receiptsgo.tooltip.model.TooltipType
import com.hadisatrio.optional.Optional
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AutomaticBackupRecoveryHintUserControllerTest {

    companion object {
        private val TOOLTIP_METADATA = TooltipMetadata(
            TooltipType.AutomaticBackupRecoveryHint,
            ApplicationProvider.getApplicationContext<Context>()
                .getString(R.string.tooltip_automatic_backups_recovery_hint)
        )
    }

    private lateinit var automaticBackupRecoveryHintUserController: AutomaticBackupRecoveryHintUserController

    @Mock
    private lateinit var tooltipView: TooltipView

    @Mock
    private lateinit var router: AutomaticBackupRecoveryHintRouter

    @Mock
    private lateinit var store: AutomaticBackupRecoveryHintUserInteractionStore

    @Mock
    private lateinit var purchaseWallet: PurchaseWallet

    @Mock
    private lateinit var purchaseManager: PurchaseManager

    @Mock
    private lateinit var analytics: Analytics

    private val scheduler = Schedulers.trampoline()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        whenever(purchaseManager.allOwnedPurchasesAndSync).thenReturn(Single.just(emptySet()))
        whenever(purchaseWallet.hasActivePurchase(InAppPurchase.PremiumSubscriptionPlan)).thenReturn(false)
        whenever(purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)).thenReturn(false)

        automaticBackupRecoveryHintUserController = AutomaticBackupRecoveryHintUserController(
            ApplicationProvider.getApplicationContext(),
            tooltipView,
            router,
            store,
            purchaseWallet,
            purchaseManager,
            analytics,
            scheduler
        )
    }

    @Test
    fun displayTooltipWithNoInteractionsAndPlusSubscription() {
        whenever(store.hasUserInteractionOccurred()).thenReturn(Single.just(false))
        whenever(purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)).thenReturn(true)

        automaticBackupRecoveryHintUserController.shouldDisplayTooltip()
            .test()
            .await()
            .assertValue(Optional.of(TOOLTIP_METADATA))
            .assertComplete()
            .assertNoErrors()
    }

    @Test
    fun displayTooltipWithNoInteractionsAndPremiumPlanSubscription() {
        whenever(store.hasUserInteractionOccurred()).thenReturn(Single.just(false))
        whenever(purchaseWallet.hasActivePurchase(InAppPurchase.PremiumSubscriptionPlan)).thenReturn(true)

        automaticBackupRecoveryHintUserController.shouldDisplayTooltip()
            .test()
            .await()
            .assertValue(Optional.of(TOOLTIP_METADATA))
            .assertComplete()
            .assertNoErrors()
    }

    @Test
    fun doNotDisplayTooltipWhenThePurchaseManagerThrowsAnError() {
        whenever(store.hasUserInteractionOccurred()).thenReturn(Single.just(false))
        whenever(purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)).thenReturn(true)
        whenever(purchaseManager.allOwnedPurchasesAndSync).thenReturn(Single.error(Exception("test")))

        automaticBackupRecoveryHintUserController.shouldDisplayTooltip()
            .test()
            .await()
            .assertValue(Optional.absent())
            .assertComplete()
            .assertNoErrors()
    }

    @Test
    fun doNotDisplayTooltipWithNoInteractionsAndNoPlusSubscription() {
        whenever(store.hasUserInteractionOccurred()).thenReturn(Single.just(false))

        automaticBackupRecoveryHintUserController.shouldDisplayTooltip()
            .test()
            .await()
            .assertValue(Optional.absent())
            .assertComplete()
            .assertNoErrors()
    }

    @Test
    fun doNotDisplayTooltipWithInteractionsAndNoPlusSubscription() {
        whenever(store.hasUserInteractionOccurred()).thenReturn(Single.just(true))

        automaticBackupRecoveryHintUserController.shouldDisplayTooltip()
            .test()
            .await()
            .assertValue(Optional.absent())
            .assertComplete()
            .assertNoErrors()
    }

    @Test
    fun doNotDisplayTooltipWithInteractionsAndPlusSubscription() {
        whenever(store.hasUserInteractionOccurred()).thenReturn(Single.just(true))
        whenever(purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)).thenReturn(true)

        automaticBackupRecoveryHintUserController.shouldDisplayTooltip()
            .test()
            .await()
            .assertValue(Optional.absent())
            .assertComplete()
            .assertNoErrors()
    }

    @Test
    fun handleTooltipInteraction() {
        val interaction = TooltipInteraction.TooltipClick

        automaticBackupRecoveryHintUserController.handleTooltipInteraction(interaction)
            .test()
            .await()
            .assertComplete()
            .assertNoErrors()

        verify(store).setUserHasInteractedWithAutomaticBackupRecoveryHint(true)
        verify(analytics).record(Events.Informational.ClickedAutomaticBackupRecoveryHintTip)
    }

    @Test
    fun consumeTooltipClickInteraction() {
        automaticBackupRecoveryHintUserController.consumeTooltipInteraction()
            .accept(TooltipInteraction.TooltipClick)

        verify(tooltipView).hideTooltip()
        verify(router).navigateToAutomaticBackupConfiguration()
    }

    @Test
    fun consumeTooltipCloseInteraction() {
        automaticBackupRecoveryHintUserController.consumeTooltipInteraction()
            .accept(TooltipInteraction.CloseCancelButtonClick)

        verify(tooltipView).hideTooltip()
        verifyZeroInteractions(router)
    }
}