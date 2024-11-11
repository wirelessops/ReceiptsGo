package com.wops.receiptsgo.widget.tooltip.report.generate

import com.hadisatrio.optional.Optional

import javax.inject.Inject

import co.smartreceipts.core.di.scopes.ApplicationScope
import com.wops.receiptsgo.model.Trip
import com.wops.receiptsgo.persistence.DatabaseHelper
import co.smartreceipts.analytics.log.Logger
import com.wops.receiptsgo.utils.rx.RxSchedulers
import com.wops.receiptsgo.widget.tooltip.TooltipManager
import com.wops.receiptsgo.widget.tooltip.report.generate.data.GenerateInfoTooltipStorage
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import javax.inject.Named

/**
 * Generate info tooltip should be shown under the following conditions:
 *
 *  - If it was never previously dismissed -- GenerateInfoTooltipStorage
 *  - The user only has a single report
 *  - The user has one or more receipts in the report
 *  - The user has never created a report -- GenerateInfoTooltipStorage
 *  - There are no GoogleDriveSync errors to show (these should be the highest priority)
 *
 * TODO: Convert this to use our new [TooltipType] format
 */
@ApplicationScope
class GenerateInfoTooltipManager @Inject constructor(private val databaseHelper: DatabaseHelper,
                                                     private val preferencesStorage: GenerateInfoTooltipStorage,
                                                     @Named(RxSchedulers.IO) private val ioScheduler: Scheduler) : TooltipManager {

    fun needToShowGenerateTooltip(): Single<Boolean> {

        return databaseHelper.tripsTable
                .get()
                .map { trips ->
                    if (trips.size == 1) {
                        return@map Optional.of<Trip>(trips[0])
                    } else {
                        return@map Optional.absent<Trip>()
                    }
                }
                .filter{ it.isPresent }
                .map { it.get() }
                .flatMapSingle{ trip -> databaseHelper.receiptsTable.get(trip) }
                .map { receipts ->
                    // tooltip wasn't dismissed, user has never generated report before and user has some receipts
                    !preferencesStorage.wasTooltipDismissed() &&
                            !preferencesStorage.wasReportEverGenerated() &&
                            receipts.size != 0
                }
                .onErrorReturn { false }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    fun reportWasGenerated() {
        Completable.fromAction {
                    preferencesStorage.reportWasGenerated()
                    Logger.debug(this, "Report was generated")
                }
                .subscribeOn(ioScheduler)
                .subscribe()
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    override fun tooltipWasDismissed() {
        Completable.fromAction {
                    preferencesStorage.tooltipWasDismissed()
                    Logger.debug(this, "Generate info tooltip was dismissed")
                }
                .subscribeOn(ioScheduler)
                .subscribe()
    }
}

