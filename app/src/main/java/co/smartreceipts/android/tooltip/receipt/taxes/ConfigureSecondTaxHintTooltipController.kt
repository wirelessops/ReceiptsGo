package co.smartreceipts.android.tooltip.receipt.taxes

import android.content.Context
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import co.smartreceipts.analytics.Analytics
import co.smartreceipts.analytics.events.Events
import co.smartreceipts.analytics.log.Logger
import co.smartreceipts.android.R
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.tooltip.TooltipController
import co.smartreceipts.android.tooltip.TooltipView
import co.smartreceipts.android.tooltip.model.TooltipInteraction
import co.smartreceipts.android.tooltip.model.TooltipMetadata
import co.smartreceipts.android.tooltip.model.TooltipType
import co.smartreceipts.android.tooltip.receipt.FirstReceiptQuestionsUserInteractionStore
import co.smartreceipts.android.utils.rx.RxSchedulers
import co.smartreceipts.core.di.scopes.FragmentScope
import com.hadisatrio.optional.Optional
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.functions.Consumer
import io.reactivex.functions.Function3
import javax.inject.Inject
import javax.inject.Named

@FragmentScope
class ConfigureSecondTaxHintTooltipController @Inject constructor(
    private val context: Context,
    private val tooltipView: TooltipView,
    private val router: ConfigureSecondTaxHintRouter,
    private val store: FirstReceiptQuestionsUserInteractionStore,
    private val userPreferenceManager: UserPreferenceManager,
    private val analytics: Analytics,
    @Named(RxSchedulers.IO) private val scheduler: Scheduler
) : TooltipController {

    @UiThread
    override fun shouldDisplayTooltip(): Single<Optional<TooltipMetadata>> {

        return Single.zip(userPreferenceManager.getSingle(UserPreference.Receipts.IncludeTaxField),
            userPreferenceManager.getSingle(UserPreference.Receipts.IncludeTax2Field),
            store.hasUserInteractionWithSecondTaxHintOccurred(),
            Function3<Boolean, Boolean, Boolean, Boolean> { isTaxIncluded, isTax2Included, userInteractedWithThisHint ->
                isTaxIncluded && !isTax2Included && !userInteractedWithThisHint
            }
        )
            .subscribeOn(scheduler)
            .map { shouldDisplay ->
                if (shouldDisplay) {
                    Logger.debug(this, "The user has not see the second tax hint before. Displaying...")
                    return@map Optional.of(
                        TooltipMetadata(
                            TooltipType.ConfigureSecondTaxHint,
                            context.getString(R.string.tooltip_include_second_tax_hint)
                        )
                    )
                } else {
                    Logger.debug(this, "This user has interacted with the second tax hint tooltip before. Ignoring.")
                    return@map Optional.absent<TooltipMetadata>()
                }
            }
    }

    @AnyThread
    override fun handleTooltipInteraction(interaction: TooltipInteraction): Completable {
        return Completable.fromCallable {
            store.setInteractionWithSecondTaxHintOccured(true)
            analytics.record(Events.Informational.ClickedConfigureSecondTaxTip)
            Logger.info(this@ConfigureSecondTaxHintTooltipController, "User interacted with the second tax hint tip")
        }.subscribeOn(scheduler)
    }

    @UiThread
    override fun consumeTooltipInteraction(): Consumer<TooltipInteraction> {
        return Consumer {
            tooltipView.hideTooltip()
            if (it == TooltipInteraction.TooltipClick) {
                router.navigateToTaxSettings()
            }
        }
    }
}