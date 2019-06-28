package co.smartreceipts.android.imports.importer;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import javax.inject.Inject;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.imports.FileImportProcessorFactory;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.ocr.OcrManager;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

@ApplicationScope
public class ActivityFileResultImporter {

    private final Analytics analytics;
    private final OcrManager ocrManager;
    private final FileImportProcessorFactory factory;

    private final Scheduler subscribeOnScheduler;
    private final Scheduler observeOnScheduler;

    private Subject<Optional<ActivityFileResultImporterResponse>> importSubject = BehaviorSubject.create();
    private Disposable localDisposable;

    @Inject
    ActivityFileResultImporter(Analytics analytics, OcrManager ocrManager, FileImportProcessorFactory factory) {
        this(analytics, ocrManager, factory, Schedulers.io(), AndroidSchedulers.mainThread());
    }

    @VisibleForTesting
    public ActivityFileResultImporter(@NonNull Analytics analytics, @NonNull OcrManager ocrManager,
                               @NonNull FileImportProcessorFactory factory,
                               @NonNull Scheduler subscribeOnScheduler, @NonNull Scheduler observeOnScheduler) {
        this.analytics = Preconditions.checkNotNull(analytics);
        this.ocrManager = Preconditions.checkNotNull(ocrManager);
        this.factory = Preconditions.checkNotNull(factory);
        this.subscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
        this.observeOnScheduler = Preconditions.checkNotNull(observeOnScheduler);
    }

    public void importFile(int requestCode, int resultCode, @NonNull Uri uri, Trip trip) {

        if (localDisposable != null) {
            Logger.warn(this, "Clearing cached local subscription, a previous request was never fully completed");
            localDisposable.dispose();
            localDisposable = null;
        }

        localDisposable = factory.get(requestCode, trip).process(uri)
                .subscribeOn(subscribeOnScheduler)
                .flatMapObservable(file -> ocrManager.scan(file)
                        .map(ocrResponse -> ActivityFileResultImporterResponse.importerResponse(file, ocrResponse,
                                requestCode, resultCode)))
                .observeOn(observeOnScheduler)
                .doOnError(throwable -> {
                    Logger.error(ActivityFileResultImporter.this, "Failed to save import result", throwable);
                    analytics.record(new ErrorEvent(ActivityFileResultImporter.this, throwable));
                })
                .observeOn(observeOnScheduler)
                .subscribeWith(new DisposableObserver<ActivityFileResultImporterResponse>() {
                    @Override
                    public void onNext(ActivityFileResultImporterResponse activityFileResultImporterResponse) {
                        importSubject.onNext(Optional.of(activityFileResultImporterResponse));
                    }

                    @Override
                    public void onError(Throwable e) {
                        importSubject.onNext(Optional.of(ActivityFileResultImporterResponse.importerError(e)));
                    }

                    @Override
                    public void onComplete() {
                        /* no-op */
                    }
                });
    }

    public Observable<ActivityFileResultImporterResponse> getResultStream() {
        return importSubject
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    public void markThatResultsWereConsumed() {
        importSubject.onNext(Optional.absent());
    }


}
