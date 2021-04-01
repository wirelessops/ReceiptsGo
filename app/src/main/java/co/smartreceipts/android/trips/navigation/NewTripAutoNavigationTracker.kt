package co.smartreceipts.android.trips.navigation

import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController
import co.smartreceipts.core.di.scopes.FragmentScope
import com.hadisatrio.optional.Optional
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

@FragmentScope
class NewTripAutoNavigationTracker @Inject constructor(
    private val viewReceiptsInTripRouter: ViewReceiptsInTripRouter,
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

    fun subscribe() {
        compositeDisposable.add(createdTripSubject.filter { it.isPresent }
            .map { it.get() }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                createdTripSubject.onNext(Optional.absent())
                viewReceiptsInTripRouter.routeToViewReceipts(it)
            })
    }

    fun unsubscribe() {
        compositeDisposable.clear()
    }
}