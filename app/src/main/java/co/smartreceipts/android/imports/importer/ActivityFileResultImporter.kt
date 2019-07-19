package co.smartreceipts.android.imports.importer

import android.net.Uri
import androidx.annotation.VisibleForTesting
import co.smartreceipts.android.analytics.Analytics
import co.smartreceipts.android.analytics.events.ErrorEvent
import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.imports.FileImportProcessorFactory
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.ocr.OcrManager
import co.smartreceipts.android.utils.log.Logger
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
    private val ocrManager: OcrManager,
    private val factory: FileImportProcessorFactory,
    private val subscribeOnScheduler: Scheduler,
    private val observeOnScheduler: Scheduler
) {

    @Inject constructor(
        analytics: Analytics,
        ocrManager: OcrManager,
        factory: FileImportProcessorFactory
    ) : this(analytics, ocrManager, factory, Schedulers.io(), AndroidSchedulers.mainThread())

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
                .flatMapObservable { file ->
                    ocrManager.scan(file)
                        .map { response ->
                            ActivityFileResultImporterResponse.importerResponse(file, response, requestCode, resultCode)
                        }
                }
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
