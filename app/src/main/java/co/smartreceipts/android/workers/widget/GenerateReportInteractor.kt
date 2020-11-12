package co.smartreceipts.android.workers.widget

import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.workers.EmailAssistant
import co.smartreceipts.android.workers.EmailAssistant.EmailOptions
import co.smartreceipts.core.di.scopes.ApplicationScope
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

@ApplicationScope
class GenerateReportInteractor constructor(
    private val emailAssistant: EmailAssistant,
    private val preferenceManager: UserPreferenceManager,
    private val subscribeOnScheduler: Scheduler = Schedulers.io(),
    private val observeOnScheduler: Scheduler = AndroidSchedulers.mainThread()
) {

    @Inject
    constructor(emailAssistant: EmailAssistant, preferenceManager: UserPreferenceManager) :
            this(emailAssistant, preferenceManager, Schedulers.io(), AndroidSchedulers.mainThread())

    fun generateReport(trip: Trip, options: EnumSet<EmailOptions>): Single<EmailResult> {

        return emailAssistant.emailTrip(trip, options)
            .subscribeOn(subscribeOnScheduler)
            .observeOn(observeOnScheduler)
    }

    fun isLandscapeReportEnabled(): Boolean = preferenceManager.get(UserPreference.ReportOutput.PrintReceiptsTableInLandscape)
}