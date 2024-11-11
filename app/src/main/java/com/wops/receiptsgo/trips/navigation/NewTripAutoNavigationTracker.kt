package com.wops.receiptsgo.trips.navigation

import com.wops.receiptsgo.model.Trip
import com.wops.receiptsgo.persistence.database.controllers.impl.TripTableController
import co.smartreceipts.core.di.scopes.ApplicationScope
import com.hadisatrio.optional.Optional
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

@ApplicationScope
class NewTripAutoNavigationTracker @Inject constructor(
    tripTableController: TripTableController
) {

    private val compositeDisposable = CompositeDisposable()
    private val createdTripSubject = BehaviorSubject.create<Optional<Trip>>()

    init {
        tripTableController.insertStream()
            .filter { it.throwable == null }
            .map { it.get() }
            .subscribe { createdTripSubject.onNext(Optional.of(it)) }
    }

    fun subscribe(viewReceiptsInTripRouter: ViewReceiptsInTripRouter) {
        compositeDisposable.add(createdTripSubject.filter { it.isPresent }
            .map { it.get() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                createdTripSubject.onNext(Optional.absent())
                viewReceiptsInTripRouter.routeToViewReceipts(it)
            })
    }

    fun dispose() {
        compositeDisposable.clear()
    }
}