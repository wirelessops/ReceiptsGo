package com.wops.receiptsgo.workers.widget

import com.wops.receiptsgo.model.Trip
import com.wops.receiptsgo.settings.UserPreferenceManager
import com.wops.receiptsgo.settings.catalog.UserPreference
import com.wops.receiptsgo.workers.EmailAssistant
import com.wops.receiptsgo.workers.EmailAssistant.EmailOptions
import com.wops.core.di.scopes.ApplicationScope
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