package co.smartreceipts.android.imports;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;

import java.io.FileNotFoundException;

import javax.inject.Inject;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.ocr.OcrManager;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;

@ApplicationScope
public class ActivityFileResultImporter {

    private final Analytics analytics;
    private final OcrManager ocrManager;
    private final FileImportProcessorFactory factory;

    private final Scheduler subscribeOnScheduler;
    private final Scheduler observeOnScheduler;

    private Subject<ActivityFileResultImporterResponse> importSubject = ReplaySubject.create(1);
    private Disposable localDisposable;

    @Inject
    ActivityFileResultImporter(Analytics analytics, OcrManager ocrManager, FileImportProcessorFactory factory) {
        this(analytics, ocrManager, factory, Schedulers.io(), AndroidSchedulers.mainThread());
    }

    @VisibleForTesting
    ActivityFileResultImporter(@NonNull Analytics analytics, @NonNull OcrManager ocrManager,
                               @NonNull FileImportProcessorFactory factory,
                               @NonNull Scheduler subscribeOnScheduler, @NonNull Scheduler observeOnScheduler) {
        this.analytics = Preconditions.checkNotNull(analytics);
        this.ocrManager = Preconditions.checkNotNull(ocrManager);
        this.factory = Preconditions.checkNotNull(factory);
        this.subscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
        this.observeOnScheduler = Preconditions.checkNotNull(observeOnScheduler);
    }

    public void onActivityResult(final int requestCode, final int resultCode, @Nullable Intent data,
                                 @Nullable final Uri proposedImageSaveLocation, @NonNull Trip trip) {

        Logger.info(this, "Performing import of onActivityResult data: {}", data);

        if (localDisposable != null) {
            Logger.warn(this, "Clearing cached local subscription, a previous request was never fully completed");
            localDisposable.dispose();
            localDisposable = null;
        }
        localDisposable = getSaveLocation(requestCode, resultCode, data, proposedImageSaveLocation)
                .subscribeOn(subscribeOnScheduler)
                .flatMapSingleElement(uri -> factory.get(requestCode, trip).process(uri))
                .flatMapObservable(file -> ocrManager.scan(file)
                        .map(ocrResponse -> new ActivityFileResultImporterResponse(file, ocrResponse, requestCode, resultCode)))
                .doOnError(throwable -> {
                    Logger.error(ActivityFileResultImporter.this, "Failed to save import result", throwable);
                    analytics.record(new ErrorEvent(ActivityFileResultImporter.this, throwable));
                })
                .observeOn(observeOnScheduler)
                .subscribeWith(new DisposableObserver<ActivityFileResultImporterResponse>() {
                    @Override
                    public void onNext(ActivityFileResultImporterResponse activityFileResultImporterResponse) {
                        importSubject.onNext(activityFileResultImporterResponse);
                    }

                    @Override
                    public void onError(Throwable e) {
                        importSubject.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        importSubject.onComplete();
                    }
                });
    }

    public Observable<ActivityFileResultImporterResponse> getResultStream() {
        return importSubject;
    }

    public void dispose() {
        if (localDisposable != null) {
            localDisposable.dispose();
            localDisposable = null;
        }
        if (importSubject != null) {
            importSubject = ReplaySubject.create(1);
        }
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
                        Logger.info(ActivityFileResultImporter.this, "Image save location determined as {}", uri);
                        emitter.onSuccess(uri);
                    }
                }
            } else {
                Logger.warn(ActivityFileResultImporter.this, "Unknown activity result code (likely user cancelled): {} ", resultCode);
                emitter.onComplete();
            }
        });
    }
}
