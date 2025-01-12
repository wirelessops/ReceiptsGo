package com.wops.receiptsgo.imports.importer

import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.wops.analytics.Analytics
import com.wops.analytics.events.ErrorEvent
import com.wops.analytics.log.Logger
import com.wops.receiptsgo.imports.FileImportProcessorFactory
import com.wops.receiptsgo.model.Trip
import com.wops.core.di.scopes.ApplicationScope
import com.hadisatrio.optional.Optional
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import javax.inject.Inject

@ApplicationScope
class ActivityFileResultImporter @VisibleForTesting constructor(
    private val analytics: Analytics,
    private val factory: FileImportProcessorFactory,
    private val subscribeOnScheduler: Scheduler,
    private val observeOnScheduler: Scheduler
) {

    @Inject
    constructor(analytics: Analytics, factory: FileImportProcessorFactory)
            : this(analytics, factory, Schedulers.io(), AndroidSchedulers.mainThread())

    private val importSubject = BehaviorSubject.create<Optional<ActivityFileResultImporterResponse>>()
    private var localDisposable: Disposable? = null

    val resultStream: Observable<ActivityFileResultImporterResponse>
        get() = importSubject
            .filter { it.isPresent }
            .map { it.get() }


    fun importFile(requestCode: Int, resultCode: Int, uri: Uri, trip: Trip) {
        localDisposable?.let {
            Logger.warn(this, "Clearing cached local subscription, a previous request was never fully completed")
            it.dispose()
        }

        localDisposable =
            factory.get(requestCode, trip).process(uri)
                .subscribeOn(subscribeOnScheduler)
                .map { file -> ActivityFileResultImporterResponse.importerResponse(file, requestCode, resultCode)}
                .observeOn(observeOnScheduler)
                .doOnError { throwable ->
                    Logger.error(this, "Failed to save import result", throwable)
                    analytics.record(ErrorEvent(this, throwable))
                }
                .subscribe({ response -> importSubject.onNext(Optional.of(response)) },
                    { throwable -> importSubject.onNext(Optional.of(ActivityFileResultImporterResponse.importerError(throwable))) })


    }


    fun markThatResultsWereConsumed() = importSubject.onNext(Optional.absent())

}
