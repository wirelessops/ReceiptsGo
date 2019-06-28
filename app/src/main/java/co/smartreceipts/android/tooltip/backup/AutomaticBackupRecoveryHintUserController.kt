package co.smartreceipts.android.tooltip.backup

import android.content.Context
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import co.smartreceipts.android.R
import com.hadisatrio.optional.Optional

import co.smartreceipts.android.analytics.Analytics
import co.smartreceipts.android.analytics.events.Events
import co.smartreceipts.android.di.scopes.FragmentScope
import co.smartreceipts.android.purchases.PurchaseManager
import co.smartreceipts.android.purchases.model.InAppPurchase
import co.smartreceipts.android.purchases.wallet.PurchaseWallet
import co.smartreceipts.android.tooltip.TooltipView
import co.smartreceipts.android.tooltip.TooltipController
import co.smartreceipts.android.tooltip.model.TooltipType
import co.smartreceipts.android.tooltip.model.TooltipInteraction
import co.smartreceipts.android.tooltip.model.TooltipMetadata
import co.smartreceipts.android.utils.log.Logger
import co.smartreceipts.android.utils.rx.RxSchedulers
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import javax.inject.Inject
import javax.inject.Named

/**
 * An implementation of the [TooltipController] contract to a hint to the user about attempting to
 * recover a previous automatic backup
 */
@FragmentScope
class AutomaticBackupRecoveryHintUserController @Inject constructor(private val context: Context,
                                                                    private val tooltipView: TooltipView,
                                                                    private val router: AutomaticBackupRecoveryHintRouter,
                                                                    private val store: AutomaticBackupRecoveryHintUserInteractionStore,
                                                                    private val purchaseWallet: PurchaseWallet,
                                                                    private val purchaseManager: PurchaseManager,
                                                                    private val analytics: Analytics,
                                                                    @Named(RxSchedulers.IO) private val scheduler: Scheduler) : TooltipController {

    @UiThread
    override fun shouldDisplayTooltip(): Single<Optional<TooltipMetadata>> {
        // Note: We fetch allOwnedPurchases first to ensure that the purchaseWallet is properly initialized
        val userOwnsSmartReceiptsPlusSingle = purchaseManager.allOwnedPurchases
                .map {
                    return@map purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)
                }
                .firstOrError()

        // Combine if an interaction has occurred (don't show) and if the user has plus (only show if they do)
        return Single.zip(userOwnsSmartReceiptsPlusSingle, store.hasUserInteractionOccurred(), BiFunction<Boolean, Boolean, Boolean> { userOwnsPlus, userInteractionHasOccurred ->
                    return@BiFunction userOwnsPlus and !userInteractionHasOccurred
                })
                .subscribeOn(scheduler)
                .map { showTooltip -> if (showTooltip) Optional.of(newTooltipMetadata()) else Optional.absent() }
                .onErrorReturnItem(Optional.absent())
    }

    @AnyThread
    override fun handleTooltipInteraction(interaction: TooltipInteraction): Completable {
        return Completable.fromCallable {
            store.setUserHasInteractedWithAutomaticBackupRecoveryHint(true)
            analytics.record(Events.Informational.ClickedAutomaticBackupRecoveryHintTip)
            Logger.info(this@AutomaticBackupRecoveryHintUserController, "User interacted with the automatic backup recovery hint")
        }.subscribeOn(scheduler)
    }

    @UiThread
    override fun consumeTooltipInteraction(): Consumer<TooltipInteraction> {
        return Consumer {
            tooltipView.hideTooltip()
            if (it == TooltipInteraction.TooltipClick) {
                router.navigateToAutomaticBackupConfiguration()
            }
        }
    }

    private fun newTooltipMetadata() : TooltipMetadata {
        return TooltipMetadata(TooltipType.AutomaticBackupRecoveryHint, context.getString(R.string.tooltip_automatic_backups_recovery_hint))
    }

}
