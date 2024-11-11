package com.wops.receiptsgo.tooltip.backup

import android.content.Context
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import com.wops.analytics.Analytics
import com.wops.analytics.events.Events
import com.wops.analytics.log.Logger
import com.wops.receiptsgo.R
import com.wops.receiptsgo.purchases.PurchaseManager
import com.wops.receiptsgo.purchases.model.InAppPurchase
import com.wops.receiptsgo.purchases.wallet.PurchaseWallet
import com.wops.receiptsgo.tooltip.TooltipController
import com.wops.receiptsgo.tooltip.TooltipView
import com.wops.receiptsgo.tooltip.model.TooltipInteraction
import com.wops.receiptsgo.tooltip.model.TooltipMetadata
import com.wops.receiptsgo.tooltip.model.TooltipType
import com.wops.receiptsgo.utils.rx.RxSchedulers
import com.wops.core.di.scopes.FragmentScope
import com.hadisatrio.optional.Optional
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
        val userOwnsSmartReceiptsPlusSingle = purchaseManager.allOwnedPurchasesAndSync
                .map {
                    return@map purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus) ||
                            purchaseWallet.hasActivePurchase(InAppPurchase.PremiumSubscriptionPlan)
                }

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
