package co.smartreceipts.android.receipts.creator;

import android.support.annotation.NonNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.Events;
import io.reactivex.subjects.PublishSubject;

import static org.junit.Assert.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReceiptCreateActionPresenterTest {
    
    @InjectMocks
    ReceiptCreateActionPresenter presenter;
    
    @Mock
    ReceiptCreateActionView view;
    
    @Mock
    Analytics analytics;
    
    PublishSubject<Object> createNewReceiptMenuButtonClicks = PublishSubject.create();
    
    PublishSubject<Object> dismissCreateNewReceiptMenuButtonClicks = PublishSubject.create();
    
    PublishSubject<Object> createNewReceiptFromCameraButtonClicks = PublishSubject.create();
    
    PublishSubject<Object> createNewReceiptFromImportedFileButtonClicks = PublishSubject.create();
    
    PublishSubject<Object> createNewReceiptFromPlainTextButtonClicks = PublishSubject.create();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(view.getCreateNewReceiptMenuButtonClicks()).thenReturn(createNewReceiptMenuButtonClicks);
        when(view.getDismissCreateNewReceiptMenuButtonClicks()).thenReturn(dismissCreateNewReceiptMenuButtonClicks);
        when(view.getCreateNewReceiptFromCameraButtonClicks()).thenReturn(createNewReceiptFromCameraButtonClicks);
        when(view.getCreateNewReceiptFromImportedFileButtonClicks()).thenReturn(createNewReceiptFromImportedFileButtonClicks);
        when(view.getCreateNewReceiptFromPlainTextButtonClicks()).thenReturn(createNewReceiptFromPlainTextButtonClicks);
        presenter.subscribe();
    }

    @Test
    public void createNewReceiptMenuButtonClicksDisplaysReceiptCreationMenuOptions() {
        createNewReceiptMenuButtonClicks.onNext(new Object());
        verify(view).displayReceiptCreationMenuOptions();
    }

    @Test
    public void dismissCreateNewReceiptMenuButtonClicksHideseceiptCreationMenuOptions() {
        dismissCreateNewReceiptMenuButtonClicks.onNext(new Object());
        verify(view).hideReceiptCreationMenuOptions();
    }

    @Test
    public void createNewReceiptFromCameraButtonClicksCreatesNewReceiptViaCamera() {
        createNewReceiptFromCameraButtonClicks.onNext(new Object());
        verify(view).createNewReceiptViaCamera();
        verify(analytics).record(Events.Receipts.AddPictureReceipt);
    }

    @Test
    public void createNewReceiptFromImportedFileButtonClicksCreatesNewReceiptViaFileImport() {
        createNewReceiptFromImportedFileButtonClicks.onNext(new Object());
        verify(view).createNewReceiptViaFileImport();
        verify(analytics).record(Events.Receipts.ImportPictureReceipt);
    }

    @Test
    public void createNewReceiptFromPlainTextButtonClicksCreatesNewReceiptViaPlainText() {
        createNewReceiptFromPlainTextButtonClicks.onNext(new Object());
        verify(view).createNewReceiptViaPlainText();
        verify(analytics).record(Events.Receipts.AddTextReceipt);
    }

    @Test
    public void allClicksAreIgnoredAfterUnsubscribe() {
        presenter.unsubscribe();

        createNewReceiptMenuButtonClicks.onNext(new Object());
        verify(view, never()).displayReceiptCreationMenuOptions();

        dismissCreateNewReceiptMenuButtonClicks.onNext(new Object());
        verify(view, never()).hideReceiptCreationMenuOptions();

        createNewReceiptFromCameraButtonClicks.onNext(new Object());
        verify(view, never()).createNewReceiptViaCamera();

        createNewReceiptFromImportedFileButtonClicks.onNext(new Object());
        verify(view, never()).createNewReceiptViaFileImport();

        createNewReceiptFromPlainTextButtonClicks.onNext(new Object());
        verify(view, never()).createNewReceiptViaPlainText();

        verifyZeroInteractions(analytics);
    }

}