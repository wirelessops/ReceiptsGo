package co.smartreceipts.android.sync.drive.rx;

import android.support.annotation.NonNull;

import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.sync.utils.SyncSchedulers;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;
import io.reactivex.SingleTransformer;

/**
 * Provides us with some Rx extensions around the {@link Task} object that is used for Google Drive
 * integrations
 */
public class RxDriveTask {

    /**
     * Converts a {@link Task} into a {@link Single}. All operations will be run on our
     * {@link SyncSchedulers}
     *
     * @param task the {@link Task} to convert
     * @param <TResult> the result type of this {@link Task}
     * @return a {@link Single}, which will emit onSuccess/onError based on the results of the task
     */
    @NonNull
    public static <TResult> Single<TResult> toSingle(@NonNull Task<TResult> task) {
        return Single.<TResult>create(emitter -> {
            task.addOnSuccessListener(SyncSchedulers.executor(), result -> {
                if (!emitter.isDisposed()) {
                    emitter.onSuccess(result);
                }
            });
            task.addOnFailureListener(SyncSchedulers.executor(), throwable -> {
                if (!emitter.isDisposed()) {
                    emitter.onError(throwable);
                }
            });
            task.addOnCanceledListener(SyncSchedulers.executor(), () -> {
                if (!emitter.isDisposed()) {
                    emitter.onError(new InterruptedException("This drive task was cancelled"));
                }
            });
        }).subscribeOn(SyncSchedulers.io());
    }

    /**
     * Converts a {@link Task} into an {@link Observable}. All operations will be run on our
     * {@link SyncSchedulers}
     *
     * @param task the {@link Task} to convert
     * @param <TResult> the result type of this {@link Task}
     * @return a {@link Observable}, which will emit onNext/onError based on the results of the task
     */
    @NonNull
    public static <TResult> Observable<TResult> toObservable(@NonNull Task<TResult> task) {
        return Observable.<TResult>create(emitter -> {
            task.addOnSuccessListener(SyncSchedulers.executor(), result -> {
                if (!emitter.isDisposed()) {
                    emitter.onNext(result);
                }
            });
            task.addOnCompleteListener(SyncSchedulers.executor(), originalTask -> {
                if (!emitter.isDisposed()) {
                    emitter.onComplete();
                }
            });
            task.addOnFailureListener(SyncSchedulers.executor(), throwable -> {
                if (!emitter.isDisposed()) {
                    emitter.onError(throwable);
                }
            });
            task.addOnCanceledListener(SyncSchedulers.executor(), () -> {
                if (!emitter.isDisposed()) {
                    emitter.onError(new InterruptedException("This drive task was cancelled"));
                }
            });
        }).subscribeOn(SyncSchedulers.io());
    }

    /**
     * Converts a {@link Task} into an {@link Completable}. All operations will be run on our
     * {@link SyncSchedulers}
     *
     * @param task the {@link Task} to convert
     * @param <TResult> the result type of this {@link Task}
     * @return a {@link Completable}, which will emit onComplete/onError based on the results of the task
     */
    public static <TResult> Completable toCompletable(@NonNull Task<TResult> task) {
        return Completable.<TResult>create(emitter -> {
            task.addOnCompleteListener(SyncSchedulers.executor(), originalTask -> {
                if (!emitter.isDisposed()) {
                    emitter.onComplete();
                }
            });
            task.addOnFailureListener(SyncSchedulers.executor(), throwable -> {
                if (!emitter.isDisposed()) {
                    emitter.onError(throwable);
                }
            });
            task.addOnCanceledListener(SyncSchedulers.executor(), () -> {
                if (!emitter.isDisposed()) {
                    emitter.onError(new InterruptedException("This drive task was cancelled"));
                }
            });
        }).subscribeOn(SyncSchedulers.io());
    }

    /**
     * @return an {@link ObservableTransformer}, which will convert a {@link MetadataBuffer} to a
     * {@link List} of {@link Metadata}, allowing us to more easily interface with the underlying
     * data
     */
    @NonNull
    public static ObservableTransformer<MetadataBuffer, List<Metadata>> transformObservableMetadataBufferToList() {
        return upstream -> upstream.map(metadataBuffer -> {
            try {
                final List<Metadata> metadataList = new ArrayList<>();
                for (final Metadata metadata : metadataBuffer) {
                    if (!metadata.isTrashed()) {
                        metadataList.add(metadata);
                    }
                }
                return metadataList;
            } finally {
                metadataBuffer.release();
            }
        });
    }

    /**
     * @return an {@link SingleTransformer}, which will convert a {@link MetadataBuffer} to a
     * {@link List} of {@link Metadata}, allowing us to more easily interface with the underlying
     * data
     */
    @NonNull
    public static SingleTransformer<MetadataBuffer, List<Metadata>> transformSingleMetadataBufferToList() {
        return upstream -> upstream.map(metadataBuffer -> {
            try {
                final List<Metadata> metadataList = new ArrayList<>();
                for (final Metadata metadata : metadataBuffer) {
                    if (!metadata.isTrashed()) {
                        metadataList.add(metadata);
                    }
                }
                return metadataList;
            } finally {
                metadataBuffer.release();
            }
        });
    }
}
