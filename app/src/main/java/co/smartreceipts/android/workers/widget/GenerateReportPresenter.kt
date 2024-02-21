package co.smartreceipts.android.workers.widget

import android.app.Activity
import co.smartreceipts.analytics.Analytics
import co.smartreceipts.analytics.events.Events
import co.smartreceipts.android.ad.InterstitialAdPresenter
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.utils.InAppReviewManager
import co.smartreceipts.android.widget.tooltip.report.generate.GenerateInfoTooltipManager
import co.smartreceipts.android.widget.viper.BaseViperPresenter
import co.smartreceipts.android.workers.EmailAssistant.EmailOptions
import co.smartreceipts.core.di.scopes.FragmentScope
import java.util.*
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
    }

    fun isLandscapeReportEnabled(): Boolean = interactor.isLandscapeReportEnabled()

    fun onReportShared(activity: Activity) {
        if (inAppReviewManager.isReviewAvailable) {
            inAppReviewManager.showReview(activity)
        } else {
            interstitialAdPresenter.showAd(activity)
        }
    }

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