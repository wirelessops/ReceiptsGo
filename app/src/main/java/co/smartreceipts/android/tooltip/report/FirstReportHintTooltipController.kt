package co.smartreceipts.android.tooltip.report

import android.content.Context
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import co.smartreceipts.analytics.Analytics
import co.smartreceipts.analytics.events.Events
import co.smartreceipts.analytics.log.Logger
import co.smartreceipts.android.R
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController
import co.smartreceipts.android.tooltip.TooltipController
import co.smartreceipts.android.tooltip.TooltipView
import co.smartreceipts.android.tooltip.model.TooltipInteraction
import co.smartreceipts.android.tooltip.model.TooltipMetadata
import co.smartreceipts.android.tooltip.model.TooltipType
import co.smartreceipts.android.utils.rx.RxSchedulers
import co.smartreceipts.core.di.scopes.FragmentScope
import com.hadisatrio.optional.Optional
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.functions.Consumer
import javax.inject.Inject
import javax.inject.Named

/**
 * An implementation of the [TooltipController] contract, which simply provides a hint about how
 * reports work within Smart Receipts (i.e. as folders) to help guide users through their first
 * experience with the app.
 *
 * We show this if:
 *  - The user has never interacted with this hint before
 *  - The user has no reports (indicating that they're new to the app)
 */
@FragmentScope
class FirstReportHintTooltipController @Inject constructor(private val context: Context,
                                                           private val tooltipView: TooltipView,
                                                           private val store: FirstReportHintUserInteractionStore,
                                                           private val tripTableController: TripTableController,
                                                           private val analytics: Analytics,
                                                           @Named(RxSchedulers.IO) private val scheduler: Scheduler) : TooltipController {

    @UiThread
    override fun shouldDisplayTooltip(): Single<Optional<TooltipMetadata>> {
        return store.hasUserInteractionOccurred()
                .subscribeOn(scheduler)
                .flatMap { hasUserInteractionOccurred ->
                    if (hasUserInteractionOccurred) {
                        Logger.debug(this, "This user has interacted with the first report hint tooltip before. Ignoring.")
                        return@flatMap Single.just<Optional<TooltipMetadata>>(Optional.absent())
                    } else {
                        tripTableController.get()
                                .map { trips -> trips.size }
                                .map { tripsCount ->
                                    if (tripsCount > 0) {
                                        Logger.debug(this, "This user has existing trips. Ignoring the first report hint tooltip")
                                        return@map Optional.absent<TooltipMetadata>()
                                    } else {
                                        Logger.info(this, "This user has no trips and has never interacted with the report hint. Indicating that we can display it")
                                        return@map Optional.of(newTooltipMetadata())
                                    }
                                }
                    }
                }
    }

    @AnyThread
    override fun handleTooltipInteraction(interaction: TooltipInteraction): Completable {
        return Completable.fromCallable {
            store.setInteractionHasOccurred(true)
            analytics.record(Events.Informational.ClickedFirstReportHintTip)
            Logger.info(this@FirstReportHintTooltipController, "User interacted with the first report creation hint")
        }.subscribeOn(scheduler)
    }

    @UiThread
    override fun consumeTooltipInteraction(): Consumer<TooltipInteraction> {
        return Consumer {
            if (it == TooltipInteraction.CloseCancelButtonClick) {
                tooltipView.hideTooltip()
            }
        }
    }

    private fun newTooltipMetadata() : TooltipMetadata {
        return TooltipMetadata(TooltipType.FirstReportHint, context.getString(R.string.tooltip_first_report_hint))
    }

}
