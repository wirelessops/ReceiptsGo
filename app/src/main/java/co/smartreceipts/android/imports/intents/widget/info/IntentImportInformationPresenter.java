package co.smartreceipts.android.imports.intents.widget.info;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.activities.SmartReceiptsActivity;
import co.smartreceipts.android.imports.intents.model.FileType;
import co.smartreceipts.android.imports.intents.model.IntentImportResult;
import co.smartreceipts.android.imports.intents.widget.IntentImportProvider;
import co.smartreceipts.android.sync.widget.backups.ImportLocalBackupDialogFragment;
import co.smartreceipts.analytics.log.Logger;
import co.smartreceipts.android.widget.model.UiIndicator;
import co.smartreceipts.android.widget.viper.BaseViperPresenter;
import co.smartreceipts.core.di.scopes.ActivityScope;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Please note that unlike some of our other presenters, the {@link #subscribe()} method should be
 * bound to {@link Activity#onCreate(Bundle)}, and the corresponding {@link #unsubscribe()} method
 * should be bound to {@link Activity#onDestroy()}, since this relies on a permissions request,
 * which survives longer that onResume -> onPause.
 */
@ActivityScope
public class IntentImportInformationPresenter extends BaseViperPresenter<IntentImportInformationView, IntentImportInformationInteractor> {

    private final IntentImportProvider intentImportProvider;
    private final NavigationHandler<SmartReceiptsActivity> navigationHandler;
    private final Scheduler observeOnScheduler;

    @Inject
    public IntentImportInformationPresenter(@NonNull IntentImportInformationView view, @NonNull IntentImportInformationInteractor interactor,
                                            @NonNull IntentImportProvider intentImportProvider,
                                            @NonNull NavigationHandler<SmartReceiptsActivity> navigationHandler) {
        this(view, interactor, intentImportProvider, navigationHandler, AndroidSchedulers.mainThread());
    }

    @VisibleForTesting
    IntentImportInformationPresenter(@NonNull IntentImportInformationView view, @NonNull IntentImportInformationInteractor interactor,
                                     @NonNull IntentImportProvider intentImportProvider,
                                     @NonNull NavigationHandler<SmartReceiptsActivity> navigationHandler,
                                     @NonNull Scheduler observeOnScheduler) {
        super(view, interactor);
        this.intentImportProvider = Preconditions.checkNotNull(intentImportProvider);
        this.navigationHandler = Preconditions.checkNotNull(navigationHandler);
        this.observeOnScheduler = observeOnScheduler;
    }

    @Override
    public void subscribe() {
        compositeDisposable.add(intentImportProvider.getIntentMaybe()
                .flatMapObservable(interactor::process)
                .observeOn(observeOnScheduler)
                .filter(uiIndicator -> uiIndicator.getState() == UiIndicator.State.Success)
                .subscribe(uiIndicator -> {
                            final IntentImportResult result = uiIndicator.getData().get();
                            if (result.getFileType() == FileType.Smr) {
                                navigationHandler.showDialog(ImportLocalBackupDialogFragment.newInstance(result.getUri()));
                            } else {
                                view.presentIntentImportInformation(result.getFileType());
                            }
                        },
                        throwable -> {
                            Logger.error(IntentImportInformationPresenter.this, "Failed to process file import", throwable);
                            view.presentIntentImportFatalError();
                        }));
    }

}
