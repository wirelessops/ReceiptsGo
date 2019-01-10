package co.smartreceipts.android.tooltip.privacy

import android.support.annotation.AnyThread
import android.support.annotation.UiThread
import com.hadisatrio.optional.Optional

import co.smartreceipts.android.analytics.Analytics
import co.smartreceipts.android.analytics.events.Events
import co.smartreceipts.android.di.scopes.FragmentScope
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController
import co.smartreceipts.android.tooltip.TooltipView
import co.smartreceipts.android.tooltip.TooltipController
import co.smartreceipts.android.tooltip.model.TooltipType
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
 * An implementation of the [TooltipController] contract to display a Privacy Policy tooltip. This
 * helps us to inform the user about his/her privacy rights in conjunction with the GDPR. We
 * likely don't need this as the app makes a point of disabling the more PII/questionable tracking
 * items by default, but it's a good addition to be safe.
 *
 * The rules for showing this item are:
 *  - If the user interacted with the privacy policy tooltip, don't show it again
 *  - If the user has not interacted with this tooltip, show it if the user is in the EU
 *  - If the user has not interacted with this tooltip and the user is not in the EU, don't show it
 *  until after the user has created his/her first trip (to avoid from confusing new users from
 *  how to use the app, since we have a lot of settings)
 */
@FragmentScope
class PrivacyPolicyTooltipController @Inject constructor(private val tooltipView: TooltipView,
                                                         private val router: PrivacyPolicyRouter,
                                                         private val store: PrivacyPolicyUserInteractionStore,
                                                         private val regionChecker: RegionChecker,
                                                         private val tripTableController: TripTableController,
                                                         private val analytics: Analytics,
                                                         @Named(RxSchedulers.IO) private val scheduler: Scheduler) : TooltipController {

    @UiThread
    override fun shouldDisplayTooltip(): Single<Optional<TooltipType>> {
        return store.hasUserInteractionOccurred()
                .flatMap { hasUserInteractionOccurred ->
                    if (hasUserInteractionOccurred) {
                        // If an interaction has already occurred, don't show the privacy tooltip again
                        return@flatMap Single.just<Optional<TooltipType>>(Optional.absent())
                    } else {
                        if (regionChecker.isInTheEuropeanUnion()) {
                            Logger.debug(this, "The user is in the EU. Indicating that we can display the privacy tooltip...")
                            return@flatMap Single.just(Optional.of(TooltipType.PrivacyPolicy))
                        } else {
                            return@flatMap tripTableController.get()
                                    .map { trips -> trips.size }
                                    .map { tripCount ->
                                        if (tripCount > 0) {
                                            Logger.debug(this, "The user is NOT in the EU but we have at least one trip. Indicating that we can display the privacy tooltip...")
                                            return@map Optional.of(TooltipType.PrivacyPolicy)
                                        } else {
                                            Logger.debug(this, "The user is NOT in the EU and we have no trips. Ignoring the privacy tooltip for now...")
                                            return@map Optional.absent<TooltipType>()
                                        }
                                    }
                        }
                    }
                }
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
