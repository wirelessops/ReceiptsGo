package co.smartreceipts.android.imports;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.google.common.base.Preconditions;

import java.io.FileNotFoundException;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.ocr.OcrManager;
import co.smartreceipts.android.persistence.PersistenceManager;
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


public class ActivityFileResultImporter {

    private static final String TAG = ActivityFileResultImporter.class.getSimpleName();

    private final Context context;
    private final FileImportProcessorFactory factory;
    private final Analytics analytics;
    private final ActivityImporterHeadlessFragment headlessFragment;
    private final OcrManager ocrManager;
    private final Scheduler subscribeOnScheduler;
    private final Scheduler observeOnScheduler;

    public ActivityFileResultImporter(@NonNull Context context, @NonNull FragmentManager fragmentManager, @NonNull Trip trip,
                                      @NonNull PersistenceManager persistenceManager, @NonNull Analytics analytics, @NonNull OcrManager ocrManager) {
        this(context, fragmentManager, new FileImportProcessorFactory(context, trip, persistenceManager), analytics, ocrManager, Schedulers.io(), AndroidSchedulers.mainThread());
    }

    public ActivityFileResultImporter(@NonNull Context context, @NonNull FragmentManager fragmentManager, @NonNull FileImportProcessorFactory factory,
                                      @NonNull Analytics analytics, @NonNull OcrManager ocrManager,
                                      @NonNull Scheduler subscribeOnScheduler, @NonNull Scheduler observeOnScheduler) {
        this.context = Preconditions.checkNotNull(context.getApplicationContext());
        this.factory = Preconditions.checkNotNull(factory);
        this.analytics = Preconditions.checkNotNull(analytics);
        this.ocrManager = Preconditions.checkNotNull(ocrManager);
        this.subscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
        this.observeOnScheduler = Preconditions.checkNotNull(observeOnScheduler);
        
        Preconditions.checkNotNull(fragmentManager);
        ActivityImporterHeadlessFragment headlessFragment = (ActivityImporterHeadlessFragment) fragmentManager.findFragmentByTag(TAG);
        if (headlessFragment == null) {
            headlessFragment = new ActivityImporterHeadlessFragment();
            fragmentManager.beginTransaction().add(headlessFragment, TAG).commit();
        }
        this.headlessFragment = headlessFragment;
        if (this.headlessFragment.importSubject == null) {
            headlessFragment.importSubject = ReplaySubject.create(1);
        }
    }

    public void onActivityResult(final int requestCode, final int resultCode, @Nullable Intent data, @Nullable final Uri proposedImageSaveLocation) {
        Logger.info(this, "Performing import of onActivityResult data: {}", data);

        if (headlessFragment.localDisposable != null) {
            Logger.warn(this, "Clearing cached local subscription, a previous request was never fully completed");
            headlessFragment.localDisposable.dispose();
            headlessFragment.localDisposable = null;
        }
        headlessFragment.localDisposable = getSaveLocation(requestCode, resultCode, data, proposedImageSaveLocation)
                .subscribeOn(subscribeOnScheduler)
                .flatMapSingleElement(uri -> factory.get(requestCode).process(uri))
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
                        headlessFragment.importSubject.onNext(activityFileResultImporterResponse);
                    }

                    @Override
                    public void onError(Throwable e) {
                        headlessFragment.importSubject.onError(e);
                    }

                    @Override
                    public void onComplete() {
                        headlessFragment.importSubject.onComplete();
                    }
                });
    }

    public Observable<ActivityFileResultImporterResponse> getResultStream() {
        return headlessFragment.importSubject;
    }

    public void dispose() {
        if (headlessFragment.localDisposable != null) {
            headlessFragment.localDisposable.dispose();
            headlessFragment.localDisposable = null;
        }
        if (headlessFragment.importSubject != null) {
            this.headlessFragment.importSubject = ReplaySubject.create(1);
        }
    }

    public static final class ActivityImporterHeadlessFragment extends Fragment {
        
        private Subject<ActivityFileResultImporterResponse> importSubject = ReplaySubject.create(1);
        private Disposable localDisposable;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }

    private Maybe<Uri> getSaveLocation(final int requestCode, final int resultCode, @Nullable final Intent data, @Nullable final Uri proposedImageSaveLocation) {
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
