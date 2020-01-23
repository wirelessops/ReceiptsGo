package co.smartreceipts.android.launch

import android.annotation.SuppressLint
import co.smartreceipts.core.di.scopes.ApplicationScope
import co.smartreceipts.android.persistence.LastTripMonitor
import co.smartreceipts.android.persistence.database.controllers.impl.DistanceTableController
import co.smartreceipts.android.persistence.database.controllers.impl.ReceiptTableController
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController
import co.smartreceipts.analytics.log.Logger
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * As it can take some time to read our database for trip, receipt, and distance entries, we use this
 * class to prefetch (ie cache) what we assume the user will likely require based on the trip the user
 * last interacted with. This is most useful when called in early in the Application#onCreate lifecycle,
 * so can load as much of this into memory as possible before beginning to interact with the UI lifecycle
 * commands (eg Activities & Fragments)
 */
@ApplicationScope
class OnLaunchDataPreFetcher @Inject constructor(private val tripTableController: TripTableController,
                                                 private val receiptTableController: ReceiptTableController,
                                                 private val distanceTableController: DistanceTableController,
                                                 private val lastTripMonitor: LastTripMonitor) {

    /**
     * Loads all trips and the receipt / distance entries for our last trip
     */
    @SuppressLint("CheckResult")
    fun loadUserData() {
        tripTableController.get()
                .subscribeOn(Schedulers.io())
                .flatMapMaybe {
                    val lastTrip = lastTripMonitor.getLastTrip(it)
                    if (lastTrip != null) {
                        Maybe.just(lastTrip)
                    } else {
                        Maybe.empty()
                    }
                }
                .observeOn(Schedulers.io())
                .subscribe ({
                    Logger.info(this, "Pre-Fetching data for ${it.name}")
                    receiptTableController.get(it)
                    distanceTableController.get(it)
                }, {
                    Logger.info(this, "Failed to pre-fetch data for this session")
                })
    }

}