package com.wops.receiptsgo.trips.navigation

import android.content.Context
import co.smartreceipts.core.di.scopes.FragmentScope
import com.wops.receiptsgo.persistence.LastTripMonitor
import com.wops.receiptsgo.persistence.database.controllers.impl.TripTableController
import com.wops.receiptsgo.utils.ConfigurableStaticFeature
import co.smartreceipts.analytics.log.Logger
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Controls the process in which we attempt to automatically navigate to the user's last trip, speeding up
 * the process in which they can begin adding receipts
 */
@FragmentScope
class LastTripAutoNavigationController @Inject constructor(private val context: Context,
                                                           private val lastTripMonitor: LastTripMonitor,
                                                           private val tripTableController: TripTableController,
                                                           private val lastTripAutoNavigationTracker: LastTripAutoNavigationTracker,
                                                           private val viewReceiptsInTripRouter: ViewReceiptsInTripRouter) {

    private val compositeDisposable = CompositeDisposable()

    fun subscribe() {
        compositeDisposable.add(Single.just(lastTripAutoNavigationTracker.hasNavigatedToLastTrip && ConfigurableStaticFeature.AutomaticallyLaunchLastTrip.isEnabled(context))
                .subscribeOn(Schedulers.io())
                .filter { it -> !it }
                .flatMap {
                    tripTableController.get()
                            .flatMapMaybe {
                                val lastTrip = lastTripMonitor.getLastTrip(it)
                                if (lastTrip != null) {
                                    Maybe.just(lastTrip)
                                } else {
                                    Maybe.empty()
                                }
                            }
                }
                .doOnSuccess {
                    Logger.info(this, "Automatically navigating to trip: ${it.name}")
                    lastTripAutoNavigationTracker.hasNavigatedToLastTrip = true
                    lastTripMonitor.setLastTrip(it)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe ({
                    viewReceiptsInTripRouter.routeToViewReceipts(it)
                }, {
                    Logger.error(this, "Failed to find a suitable last trip to automatically navigate to")
                }))
    }

    fun unsubscribe() {
        compositeDisposable.clear()
    }
}