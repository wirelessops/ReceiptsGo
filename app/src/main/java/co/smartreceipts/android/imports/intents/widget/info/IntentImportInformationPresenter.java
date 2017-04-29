package co.smartreceipts.android.imports.intents.widget.info;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.activities.SmartReceiptsActivity;
import co.smartreceipts.android.di.scopes.ActivityScope;
import co.smartreceipts.android.fragments.InformAboutPdfImageAttachmentDialogFragment;
import co.smartreceipts.android.imports.intents.model.FileType;
import co.smartreceipts.android.imports.intents.model.IntentImportResult;
import co.smartreceipts.android.imports.intents.widget.IntentImportProvider;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.sync.widget.backups.ImportLocalBackupDialogFragment;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.widget.model.UiIndicator;
import co.smartreceipts.android.widget.viper.BasePresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Please note that unlike some of our other presenters, the {@link #subscribe()} method should be
 * bound to {@link Activity#onCreate(Bundle)}, and the corresponding {@link #unsubscribe()} method
 * should be bound to {@link Activity#onDestroy()}, since this relies on a permissions request,
 * which survives longer that onResume -> onPause.
 */
@ActivityScope
public class IntentImportInformationPresenter extends BasePresenter<IntentImportInformationView, IntentImportInformationInteractor> {

    private final IntentImportProvider intentImportProvider;
    private final PersistenceManager persistenceManager;
    private final NavigationHandler<SmartReceiptsActivity> navigationHandler;

    @Inject
    public IntentImportInformationPresenter(@NonNull IntentImportInformationView view, @NonNull IntentImportInformationInteractor interactor,
                                            @NonNull IntentImportProvider intentImportProvider, @NonNull PersistenceManager persistenceManager,
                                            @NonNull NavigationHandler<SmartReceiptsActivity> navigationHandler) {
        super(view, interactor);
        this.intentImportProvider = Preconditions.checkNotNull(intentImportProvider);
        this.persistenceManager = Preconditions.checkNotNull(persistenceManager);
        this.navigationHandler = Preconditions.checkNotNull(navigationHandler);
    }

    @Override
    public void subscribe() {
        compositeDisposable.add(intentImportProvider.getIntentMaybe()
                .flatMapObservable(interactor::process)
                .observeOn(AndroidSchedulers.mainThread())
                .filter(uiIndicator -> uiIndicator.getState() == UiIndicator.State.Success)
                .subscribe(uiIndicator -> {
                    final IntentImportResult result = uiIndicator.getData().get();
                    if (result.getFileType() == FileType.Smr) {
                        navigationHandler.showDialog(ImportLocalBackupDialogFragment.newInstance(result.getUri()));
                    } else {
                        if (InformAboutPdfImageAttachmentDialogFragment.shouldInformAboutPdfImageAttachmentDialogFragment(persistenceManager.getPreferenceManager())) {
                            view.presentFirstTimeInformation(result.getFileType());
                        } else {
                            view.presentGenericImportInformation(result.getFileType());
                        }
                    }
                },
                throwable -> {
                    Logger.error(IntentImportInformationPresenter.this, "Failed to process file import", throwable);
                    view.presentFatalError();
                }));
    }

}
