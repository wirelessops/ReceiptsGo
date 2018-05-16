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
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * An implementation of the [TooltipController] contract to display a Privacy Policy tooltip
 */
@FragmentScope
class PrivacyPolicyTooltipController @Inject constructor(private val tooltipView: StaticTooltipView,
                                                         private val router: PrivacyPolicyRouter,
                                                         private val store: PrivacyPolicyUserInterationStore,
                                                         private val analytics: Analytics) : TooltipController {

    @UiThread
    override fun shouldDisplayTooltip(): Single<Optional<StaticTooltip>> {
        return store.hasUserInteractionOccurred()
                .map { shouldShow -> if (shouldShow) Optional.of(StaticTooltip.PrivacyPolicy) else Optional.absent() }
    }

    @AnyThread
    override fun handleTooltipInteraction(interaction: TooltipInteraction): Observable<TooltipInteraction> {
        return Observable.just(interaction)
                .subscribeOn(Schedulers.io())
                .doOnNext { _ ->
                    store.setUserHasInteractedWithPrivacyPolicy(true)
                    analytics.record(Events.Informational.ClickedPrivacyPolicyTip)
                    Logger.info(this@PrivacyPolicyTooltipController, "User interacted with the privacy policy settings information")
                }
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
