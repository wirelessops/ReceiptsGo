package co.smartreceipts.android.trips.navigation

import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.persistence.LastTripMonitor
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 * A simple application scope tracker for if we've previously navigated to the last trip
 */
@ApplicationScope
class LastTripAutoNavigationTracker constructor(var hasNavigatedToLastTrip: Boolean) {

    @Inject
    constructor() : this(false)
}