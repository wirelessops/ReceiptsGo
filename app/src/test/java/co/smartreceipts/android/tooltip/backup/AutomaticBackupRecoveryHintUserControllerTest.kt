package co.smartreceipts.android.tooltip.backup

import co.smartreceipts.android.analytics.Analytics
import co.smartreceipts.android.analytics.events.Events
import co.smartreceipts.android.purchases.PurchaseManager
import co.smartreceipts.android.purchases.model.InAppPurchase
import co.smartreceipts.android.purchases.wallet.PurchaseWallet
import co.smartreceipts.android.tooltip.TooltipView
import co.smartreceipts.android.tooltip.model.TooltipType
import co.smartreceipts.android.tooltip.model.TooltipInteraction
import co.smartreceipts.android.tooltip.model.TooltipMetadata
import com.hadisatrio.optional.Optional
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyZeroInteractions
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
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
        whenever(purchaseManager.allOwnedPurchases).thenReturn(Observable.just(emptySet()))
        automaticBackupRecoveryHintUserController = AutomaticBackupRecoveryHintUserController(tooltipView, router, store, purchaseWallet, purchaseManager, analytics, scheduler)
    }

    @Test
    fun displayTooltipWithNoInteractionsAndPlusSubscription() {
        whenever(store.hasUserInteractionOccurred()).thenReturn(Single.just(false))
        whenever(purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)).thenReturn(true)
        automaticBackupRecoveryHintUserController.shouldDisplayTooltip()
                .test()
                .await()
                .assertValue(Optional.of<TooltipMetadata>(TooltipType.AutomaticBackupRecoveryHint))
                .assertComplete()
                .assertNoErrors()
    }

    @Test
    fun doNotDisplayTooltipWhenThePurchaseManagerThrowsAnError() {
        whenever(store.hasUserInteractionOccurred()).thenReturn(Single.just(false))
        whenever(purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)).thenReturn(true)
        whenever(purchaseManager.allOwnedPurchases).thenReturn(Observable.error(Exception("test")))
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
        whenever(purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)).thenReturn(false)
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
        whenever(purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)).thenReturn(false)
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
        automaticBackupRecoveryHintUserController.consumeTooltipInteraction().accept(TooltipInteraction.TooltipClick)
        verify(tooltipView).hideTooltip()
        verify(router).navigateToAutomaticBackupConfiguration()
    }

    @Test
    fun consumeTooltipCloseInteraction() {
        automaticBackupRecoveryHintUserController.consumeTooltipInteraction().accept(TooltipInteraction.CloseCancelButtonClick)
        verify(tooltipView).hideTooltip()
        verifyZeroInteractions(router)
    }
}