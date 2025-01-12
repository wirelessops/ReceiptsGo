package com.wops.receiptsgo.imports.intents.widget.info;

import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import com.wops.receiptsgo.activities.NavigationHandler;
import com.wops.receiptsgo.activities.ReceiptsGoActivity;
import com.wops.receiptsgo.imports.intents.model.FileType;
import com.wops.receiptsgo.imports.intents.model.IntentImportResult;
import com.wops.receiptsgo.imports.intents.widget.IntentImportProvider;
import com.wops.receiptsgo.sync.widget.backups.ImportLocalBackupDialogFragment;
import com.wops.receiptsgo.widget.model.UiIndicator;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class IntentImportInformationPresenterTest {

    IntentImportInformationPresenter presenter;

    @Mock
    IntentImportInformationView view;

    @Mock
    IntentImportInformationInteractor interactor;

    @Mock
    IntentImportProvider intentImportProvider;

    @Mock
    NavigationHandler<ReceiptsGoActivity> navigationHandler;

    @Mock
    Intent intent;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(intentImportProvider.getIntentMaybe()).thenReturn(Maybe.just(intent));
        presenter = new IntentImportInformationPresenter(view, interactor, intentImportProvider, navigationHandler, Schedulers.trampoline());
    }

    @After
    public void tearDown() throws Exception {
        PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext()).edit().clear().apply();
    }

    @Test
    public void idleIndicatorsAreNotConsumed() {
        when(interactor.process(intent)).thenReturn(Observable.just(UiIndicator.idle(), UiIndicator.idle()));
        presenter.subscribe();

        verifyZeroInteractions(view);
        verifyZeroInteractions(navigationHandler);
    }

    @Test
    public void importSmrFileTriggersDialog() {
        when(interactor.process(intent)).thenReturn(Observable.just(UiIndicator.idle(), UiIndicator.success(new IntentImportResult(Uri.EMPTY, FileType.Smr))));
        presenter.subscribe();

        verify(navigationHandler).showDialog(any(ImportLocalBackupDialogFragment.class));
        verifyZeroInteractions(view);
    }

    @Test
    public void importImageTheFirstTimeShowsView() {
        when(interactor.process(intent)).thenReturn(Observable.just(UiIndicator.idle(), UiIndicator.success(new IntentImportResult(Uri.EMPTY, FileType.Image))));
        presenter.subscribe();

        verify(view).presentIntentImportInformation(FileType.Image);
        verifyNoMoreInteractions(view);
        verifyZeroInteractions(navigationHandler);
    }

    @Test
    public void importPdfSubsequentlyShowsGenericView() {
        when(interactor.process(intent)).thenReturn(Observable.just(UiIndicator.idle(), UiIndicator.success(new IntentImportResult(Uri.EMPTY, FileType.Pdf))));
        presenter.subscribe();

        verify(view).presentIntentImportInformation(FileType.Pdf);
        verifyNoMoreInteractions(view);
        verifyZeroInteractions(navigationHandler);
    }

    @Test
    public void errorsDisplayFatalView() {
        when(interactor.process(intent)).thenReturn(Observable.error(new Exception("test")));
        presenter.subscribe();

        verify(view).presentIntentImportFatalError();
        verifyNoMoreInteractions(view);
        verifyZeroInteractions(navigationHandler);
    }

}