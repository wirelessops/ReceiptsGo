package co.smartreceipts.android.ocr.widget.alert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import co.smartreceipts.android.R;
import co.smartreceipts.android.ocr.OcrManager;
import co.smartreceipts.android.widget.model.UiIndicator;
import io.reactivex.subjects.PublishSubject;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class OcrStatusAlerterPresenterTest {

    OcrStatusAlerterPresenter presenter;

    @Mock
    OcrStatusAlerterView view;

    @Mock
    OcrManager ocrManager;

    PublishSubject<OcrProcessingStatus> ocrProcessingStatusEvents = PublishSubject.create();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(ocrManager.getOcrProcessingStatus()).thenReturn(ocrProcessingStatusEvents);
        presenter = new OcrStatusAlerterPresenter(view, RuntimeEnvironment.application, ocrManager);
        presenter.subscribe();
        verify(view).displayOcrStatus(UiIndicator.idle()); // We always start with idle
    }

    @Test
    public void uploadingImageEventsAreHandled() {
        ocrProcessingStatusEvents.onNext(OcrProcessingStatus.UploadingImage);
        verify(view).displayOcrStatus(UiIndicator.loading(RuntimeEnvironment.application.getString(R.string.ocr_status_message_uploading_image)));
    }

    @Test
    public void performingScanEventsAreHandled() {
        ocrProcessingStatusEvents.onNext(OcrProcessingStatus.PerformingScan);
        verify(view).displayOcrStatus(UiIndicator.loading(RuntimeEnvironment.application.getString(R.string.ocr_status_message_performing_scan)));
    }

    @Test
    public void retrievingResultsEventsAreHandled() {
        ocrProcessingStatusEvents.onNext(OcrProcessingStatus.RetrievingResults);
        verify(view).displayOcrStatus(UiIndicator.loading(RuntimeEnvironment.application.getString(R.string.ocr_status_message_fetching_results)));
    }

    @Test
    public void unsubscribeEmitsAnIdleEventButIgnoresTheRest() {
        presenter.unsubscribe();
        verify(view, times(2)).displayOcrStatus(UiIndicator.idle());

        ocrProcessingStatusEvents.onNext(OcrProcessingStatus.UploadingImage);
        ocrProcessingStatusEvents.onNext(OcrProcessingStatus.PerformingScan);
        ocrProcessingStatusEvents.onNext(OcrProcessingStatus.RetrievingResults);
        verifyNoMoreInteractions(view);
    }

}