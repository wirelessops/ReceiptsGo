package co.smartreceipts.android.tooltip.rating

import android.support.annotation.AnyThread
import android.support.annotation.UiThread
import co.smartreceipts.android.analytics.Analytics
import co.smartreceipts.android.analytics.events.Events
import co.smartreceipts.android.di.scopes.FragmentScope
import co.smartreceipts.android.rating.AppRatingManager
import co.smartreceipts.android.tooltip.StaticTooltipView
import co.smartreceipts.android.tooltip.TooltipController
import co.smartreceipts.android.tooltip.model.StaticTooltip
import co.smartreceipts.android.tooltip.model.TooltipInteraction
import co.smartreceipts.android.utils.log.Logger
import com.hadisatrio.optional.Optional
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * An implementation of the [TooltipController] contract to display a "Rate This App" tooltip
 */
@FragmentScope
class AppRatingTooltipController @Inject constructor(private val tooltipView: StaticTooltipView,
                                                     private var router: AppRatingTooltipRouter,
                                                     private val appRatingManager: AppRatingManager,
                                                     private val analytics: Analytics) : TooltipController {

    @UiThread
    override fun shouldDisplayTooltip(): Single<Optional<StaticTooltip>> {
        return appRatingManager.checkIfNeedToAskRating()
                .map { shouldShow -> if (shouldShow) Optional.of(StaticTooltip.RateThisApp) else Optional.absent() }
                .doOnSuccess{
                    if (it.isPresent) {
                        analytics.record(Events.Ratings.RatingPromptShown)
                    }
                }
    }

    @AnyThread
    override fun handleTooltipInteraction(interaction: TooltipInteraction): Observable<TooltipInteraction> {
        return Observable.just(interaction)
                .subscribeOn(Schedulers.io())
                .filter {
                    return@filter it == TooltipInteraction.YesButtonClick || it == TooltipInteraction.NoButtonClick
                }
                .doOnNext {
                    appRatingManager.dontShowRatingPromptAgain()
                    if (it == TooltipInteraction.YesButtonClick) {
                        analytics.record(Events.Ratings.UserAcceptedRatingPrompt)
                    } else {
                        analytics.record(Events.Ratings.UserDeclinedRatingPrompt)
                    }
                }
    }

    @UiThread
    override fun consumeTooltipInteraction(): Consumer<TooltipInteraction> {
        return Consumer {
            when (it) {
                TooltipInteraction.YesButtonClick -> {
                    router.navigateToRatingOptions()
                    tooltipView.hideTooltip()
                }
                TooltipInteraction.NoButtonClick -> {
                    router.navigateToFeedbackOptions()
                    tooltipView.hideTooltip()
                }
                else -> Logger.warn(this, "Handling unknown tooltip interaction: {}", it)
            }
        }
    }

}