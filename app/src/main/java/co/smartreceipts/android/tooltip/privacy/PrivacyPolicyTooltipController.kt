package co.smartreceipts.android.tooltip.privacy

import android.support.annotation.AnyThread
import android.support.annotation.UiThread
import com.hadisatrio.optional.Optional

import co.smartreceipts.android.analytics.Analytics
import co.smartreceipts.android.analytics.events.Events
import co.smartreceipts.android.di.scopes.FragmentScope
import co.smartreceipts.android.tooltip.StaticTooltipView
import co.smartreceipts.android.tooltip.TooltipController
import co.smartreceipts.android.tooltip.model.StaticTooltip
import co.smartreceipts.android.tooltip.model.TooltipInteraction
import co.smartreceipts.android.utils.log.Logger
import co.smartreceipts.android.utils.rx.RxSchedulers
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.functions.Consumer
import javax.inject.Inject
import javax.inject.Named

/**
 * An implementation of the [TooltipController] contract to display a Privacy Policy tooltip
 */
@FragmentScope
class PrivacyPolicyTooltipController @Inject constructor(private val tooltipView: StaticTooltipView,
                                                         private val router: PrivacyPolicyRouter,
                                                         private val store: PrivacyPolicyUserInteractionStore,
                                                         private val analytics: Analytics,
                                                         @Named(RxSchedulers.IO) private val scheduler: Scheduler) : TooltipController {

    @UiThread
    override fun shouldDisplayTooltip(): Single<Optional<StaticTooltip>> {
        return store.hasUserInteractionOccurred()
                .map { hasUserInteractionOccurred -> if (!hasUserInteractionOccurred) Optional.of(StaticTooltip.PrivacyPolicy) else Optional.absent() }
    }

    @AnyThread
    override fun handleTooltipInteraction(interaction: TooltipInteraction): Completable {
        return Completable.fromCallable {
            store.setUserHasInteractedWithPrivacyPolicy(true)
            analytics.record(Events.Informational.ClickedPrivacyPolicyTip)
            Logger.info(this@PrivacyPolicyTooltipController, "User interacted with the privacy policy settings information")
        }.subscribeOn(scheduler)
    }

    @UiThread
    override fun consumeTooltipInteraction(): Consumer<TooltipInteraction> {
        return Consumer {
            tooltipView.hideTooltip()
            if (it == TooltipInteraction.TooltipClick) {
                router.navigateToPrivacyPolicyControls()
            }
        }
    }

}
