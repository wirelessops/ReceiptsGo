package co.smartreceipts.android.imports.locator

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.annotation.VisibleForTesting
import co.smartreceipts.core.di.scopes.ApplicationScope
import co.smartreceipts.analytics.log.Logger
import com.hadisatrio.optional.Optional
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.io.FileNotFoundException
import javax.inject.Inject

@ApplicationScope
class ActivityFileResultLocator @VisibleForTesting
constructor(
    private val subscribeOnScheduler: Scheduler,
    private val observeOnScheduler: Scheduler
) {


    @Inject
    constructor() : this(Schedulers.io(), AndroidSchedulers.mainThread())

    private var localDisposable: Disposable? = null
    private val uriImportSubject = BehaviorSubject.create<Optional<ActivityFileResultLocatorResponse>>()

    val uriStream: Observable<ActivityFileResultLocatorResponse>
        get() = uriImportSubject
            .filter { it.isPresent }
            .map { it.get() }


    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?, proposedImageSaveLocation: Uri?) {

        Logger.info(this, "Performing import of onActivityResult data: {}", data)

        localDisposable?.let {
            Logger.warn(this, "Clearing cached local subscription, a previous request was never fully completed")
            it.dispose()
        }

        localDisposable = getSaveLocation(requestCode, resultCode, data, proposedImageSaveLocation)
            .subscribeOn(subscribeOnScheduler)
            .observeOn(observeOnScheduler)
            .toObservable()
            .subscribe({ uri ->
                uriImportSubject.onNext(Optional.of(ActivityFileResultLocatorResponse.locatorResponse(uri, requestCode, resultCode)))
            },
                { throwable -> uriImportSubject.onNext(Optional.of(ActivityFileResultLocatorResponse.locatorError(throwable))) })
    }

    fun markThatResultsWereConsumed() = uriImportSubject.onNext(Optional.absent())


    private fun getSaveLocation(requestCode: Int, resultCode: Int, data: Intent?, proposedImageSaveLocation: Uri?): Maybe<Uri> {
        return Maybe.create { emitter ->
            if (resultCode == Activity.RESULT_OK) {
                if (data?.data == null && proposedImageSaveLocation == null) {
                    emitter.onError(FileNotFoundException("Unknown intent data and proposed save location for request $requestCode with result $resultCode"))
                } else {
                    when (val uri: Uri? = data?.data ?: proposedImageSaveLocation) {
                        null -> emitter.onError(FileNotFoundException("Null Uri for request $requestCode with result $resultCode"))
                        else -> {
                            Logger.info(this, "Image save location determined as {}", uri)
                            emitter.onSuccess(uri)
                        }
                    }
                }
            } else {
                Logger.warn(this, "Unknown activity result code (likely user cancelled): {} ", resultCode)
                emitter.onComplete()
            }
        }
    }
}
