package com.wops.receiptsgo.workers.widget

import co.smartreceipts.analytics.Analytics
import co.smartreceipts.analytics.events.Events
import com.wops.receiptsgo.ad.InterstitialAdPresenter
import com.wops.receiptsgo.model.Trip
import com.wops.receiptsgo.rating.InAppReviewManager
import com.wops.receiptsgo.widget.tooltip.report.generate.GenerateInfoTooltipManager
import com.wops.receiptsgo.widget.viper.BaseViperPresenter
import com.wops.receiptsgo.workers.EmailAssistant.EmailOptions
import co.smartreceipts.core.di.scopes.FragmentScope
import java.util.EnumSet
import javax.inject.Inject

@FragmentScope
class GenerateReportPresenter @Inject constructor(
    view: GenerateReportView, interactor: GenerateReportInteractor,
    private val analytics: Analytics,
    private val generateInfoTooltipManager: GenerateInfoTooltipManager,
    private val interstitialAdPresenter: InterstitialAdPresenter,
    private val inAppReviewManager: InAppReviewManager,
) :
    BaseViperPresenter<GenerateReportView, GenerateReportInteractor>(view, interactor) {

    private var trip: Trip? = null

    fun subscribe(trip: Trip) {
        this.trip = trip

        subscribe()
    }

    override fun subscribe() {

        checkNotNull(trip) { "Use subscribe(trip) method to subscribe" }

        compositeDisposable.add(
            view.generateReportClicks
                .doOnNext {
                    view.present(EmailResult.InProgress)
                    analytics.record(Events.Generate.GenerateReports)
                    generateInfoTooltipManager.reportWasGenerated()

                    recordOptionsAnalyticsEvents(it)
                }
                .flatMap { options -> interactor.generateReport(trip!!, options).toObservable() }
                .subscribe(
                    { view.present(it) },
                    { view.present(EmailResult.Error(GenerationErrors.ERROR_UNDETERMINED)) })
        )

        compositeDisposable.add(
            view.reportSharedEvents
                .flatMap { inAppReviewManager.canShowReview().toObservable() }
                .doOnNext { canShowReview->
                    if (canShowReview) {
                        inAppReviewManager.showReview(view.getActivity)
                    } else {
                        interstitialAdPresenter.showAd(view.getActivity)
                    }
                }
                .subscribe()
        )
    }

    fun isLandscapeReportEnabled(): Boolean = interactor.isLandscapeReportEnabled()

    private fun recordOptionsAnalyticsEvents(options: EnumSet<EmailOptions>) {

        if (options.contains(EmailOptions.PDF_FULL)) {
            analytics.record(Events.Generate.FullPdfReport)
        }

        if (options.contains(EmailOptions.PDF_IMAGES_ONLY)) {
            analytics.record(Events.Generate.ImagesPdfReport)
        }

        if (options.contains(EmailOptions.CSV)) {
            analytics.record(Events.Generate.CsvReport)
        }

        if (options.contains(EmailOptions.ZIP_WITH_METADATA)) {
            analytics.record(Events.Generate.ZipWithMetadataReport)
        }

        if (options.contains(EmailOptions.ZIP)) {
            analytics.record(Events.Generate.ZipReport)
        }
    }
}