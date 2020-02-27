package co.smartreceipts.android.distance.editor

import co.smartreceipts.analytics.Analytics
import co.smartreceipts.analytics.events.Events
import co.smartreceipts.android.model.Distance
import co.smartreceipts.android.persistence.database.controllers.impl.DistanceTableController
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.core.di.scopes.ApplicationScope
import com.hadisatrio.optional.Optional
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@ApplicationScope
class DistanceCreateEditInteractor constructor(
    private val distanceTableController: DistanceTableController,
    private val userPreferenceManager: UserPreferenceManager,
    private val analytics: Analytics,
    private val subscribeOnScheduler: Scheduler = Schedulers.io(),
    private val observeOnScheduler: Scheduler = AndroidSchedulers.mainThread()
) {

    @Inject
    constructor(distanceTableController: DistanceTableController, userPreferenceManager: UserPreferenceManager, analytics: Analytics) :
            this(distanceTableController, userPreferenceManager, analytics, Schedulers.io(), AndroidSchedulers.mainThread())

    fun deleteDistance(distance: Distance) {
        distanceTableController.delete(distance, DatabaseOperationMetadata())
    }

    fun updateDistance(oldDistance: Distance, newDistance: Distance): Observable<Optional<Distance>> {
        analytics.record(Events.Distance.PersistUpdateDistance)

        return distanceTableController.update(oldDistance, newDistance, DatabaseOperationMetadata())
            .subscribeOn(subscribeOnScheduler)
            .observeOn(observeOnScheduler)
    }

    fun createDistance(newDistance: Distance): Observable<Optional<Distance>> {
        analytics.record(Events.Distance.PersistNewDistance)

        return distanceTableController.insert(newDistance, DatabaseOperationMetadata())
            .subscribeOn(subscribeOnScheduler)
            .observeOn(observeOnScheduler)
    }

    fun getDefaultDistanceRate(): Float {
        return userPreferenceManager[UserPreference.Distance.DefaultDistanceRate]
    }

    fun isUsePaymentMethods(): Boolean {
        return userPreferenceManager[UserPreference.Receipts.UsePaymentMethods]
    }

}