package co.smartreceipts.automatic_backups.drive.rx

import com.google.android.gms.tasks.Task

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Converts a [Task] into a [Single]. All operations will be run on our
 * [SyncSchedulers]
 *
 * @param task the [Task] to convert
 * @param <TResult> the result type of this [Task]
 * @return a [Single], which will emit onSuccess/onError based on the results of the task
</TResult> */
fun <TResult> Task<TResult>.toSingle(): Single<TResult> {
    return Single.create<TResult> { emitter ->
        addOnSuccessListener { result ->
            if (!emitter.isDisposed) {
                emitter.onSuccess(result)
            }
        }
        addOnFailureListener { throwable ->
            if (!emitter.isDisposed) {
                emitter.onError(throwable)
            }
        }
        addOnCanceledListener {
            if (!emitter.isDisposed) {
                emitter.onError(InterruptedException("This drive task was cancelled"))
            }
        }
    }.subscribeOn(SyncSchedulers.io())
}

/**
 * Converts a [Task] into an [Observable]. All operations will be run on our
 * [SyncSchedulers]
 *
 * @param task the [Task] to convert
 * @param <TResult> the result type of this [Task]
 * @return a [Observable], which will emit onNext/onError based on the results of the task
</TResult> */
fun <TResult> Task<TResult>.toObservable(): Observable<TResult> {
    return Observable.create<TResult> { emitter ->
        addOnSuccessListener { result ->
            if (!emitter.isDisposed) {
                emitter.onNext(result)
            }
        }
        addOnCompleteListener { originalTask ->
            if (!emitter.isDisposed) {
                emitter.onComplete()
            }
        }
        addOnFailureListener { throwable ->
            if (!emitter.isDisposed) {
                emitter.onError(throwable)
            }
        }
        addOnCanceledListener {
            if (!emitter.isDisposed) {
                emitter.onError(InterruptedException("This drive task was cancelled"))
            }
        }
    }.subscribeOn(SyncSchedulers.io())
}

/**
 * Converts a [Task] into an [Completable]. All operations will be run on our
 * [SyncSchedulers]
 *
 * @param task the [Task] to convert
 * @param <TResult> the result type of this [Task]
 * @return a [Completable], which will emit onComplete/onError based on the results of the task
</TResult> */
fun <TResult> Task<TResult>.toCompletable(): Completable {
    return Completable.create { emitter ->
        addOnCompleteListener { originalTask ->
            if (!emitter.isDisposed) {
                emitter.onComplete()
            }
        }
        addOnFailureListener { throwable ->
            if (!emitter.isDisposed) {
                emitter.onError(throwable)
            }
        }
        addOnCanceledListener {
            if (!emitter.isDisposed) {
                emitter.onError(InterruptedException("This drive task was cancelled"))
            }
        }
    }.subscribeOn(SyncSchedulers.io())
}
