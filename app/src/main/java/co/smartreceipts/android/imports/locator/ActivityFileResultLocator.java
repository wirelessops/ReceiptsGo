package co.smartreceipts.android.imports.locator;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.hadisatrio.optional.Optional;

import java.io.FileNotFoundException;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

@ApplicationScope
public class ActivityFileResultLocator {

    private Disposable localDisposable;
    private Subject<Optional<ActivityFileResultLocatorResponse>> uriImportSubject = BehaviorSubject.create();

    private final Scheduler subscribeOnScheduler;
    private final Scheduler observeOnScheduler;

    @Inject
    public ActivityFileResultLocator() {
        this(Schedulers.io(), AndroidSchedulers.mainThread());
    }

    @VisibleForTesting
    public ActivityFileResultLocator (Scheduler subscribeOnScheduler, Scheduler observeOnScheduler) {
        this.subscribeOnScheduler = subscribeOnScheduler;
        this.observeOnScheduler = observeOnScheduler;
    }

    public void onActivityResult(final int requestCode, final int resultCode, @Nullable Intent data,
                                 @Nullable final Uri proposedImageSaveLocation) {

        Logger.info(this, "Performing import of onActivityResult data: {}", data);

        if (localDisposable != null) {
            Logger.warn(this, "Clearing cached local subscription, a previous request was never fully completed");
            localDisposable.dispose();
            localDisposable = null;
        }

        localDisposable = getSaveLocation(requestCode, resultCode, data, proposedImageSaveLocation)
                .subscribeOn(subscribeOnScheduler)
                .observeOn(observeOnScheduler)
                .toObservable()
                .subscribeWith(new DisposableObserver<Uri>() {
                    @Override
                    public void onNext(Uri uri) {
                        uriImportSubject.onNext(Optional.of(ActivityFileResultLocatorResponse.LocatorResponse(uri, requestCode, resultCode)));
                    }

                    @Override
                    public void onError(Throwable e) {
                        uriImportSubject.onNext(Optional.of(ActivityFileResultLocatorResponse.LocatorError(e)));
                    }

                    @Override
                    public void onComplete() {
                        /* no-op */
                    }
                });
    }

    public Observable<ActivityFileResultLocatorResponse> getUriStream() {
        return uriImportSubject
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    public void markThatResultsWereConsumed() {
        uriImportSubject.onNext(Optional.absent());
    }


    private Maybe<Uri> getSaveLocation(final int requestCode, final int resultCode, @Nullable final Intent data,
                                            @Nullable final Uri proposedImageSaveLocation) {
        return Maybe.create(emitter -> {
            if (resultCode == Activity.RESULT_OK) {
                if ((data == null || data.getData() == null) && proposedImageSaveLocation == null) {
                    emitter.onError(new FileNotFoundException("Unknown intent data and proposed save location for request " + requestCode + " with result " + resultCode));
                } else {
                    final Uri uri;
                    if (data != null && data.getData() != null) {
                        uri = data.getData();
                    } else {
                        uri = proposedImageSaveLocation;
                    }

                    if (uri == null) {
                        emitter.onError(new FileNotFoundException("Null Uri for request " + requestCode + " with result " + resultCode));
                    } else {
                        Logger.info(ActivityFileResultLocator.this, "Image save location determined as {}", uri);
                        emitter.onSuccess(uri);
                    }
                }
            } else {
                Logger.warn(ActivityFileResultLocator.this, "Unknown activity result code (likely user cancelled): {} ", resultCode);
                emitter.onComplete();
            }
        });
    }
}
