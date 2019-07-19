package co.smartreceipts.android.imports.importer;

import android.app.Activity;
import android.content.ContentResolver;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.io.File;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.imports.FileImportProcessor;
import co.smartreceipts.android.imports.FileImportProcessorFactory;
import co.smartreceipts.android.imports.RequestCodes;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.ocr.OcrManager;
import co.smartreceipts.android.ocr.apis.model.OcrResponse;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ActivityFileResultImporterTest {

    // Class under test
    ActivityFileResultImporter fileResultImporter;

    @Mock
    FileImportProcessorFactory factory;

    @Mock
    FileImportProcessor processor;

    @Mock
    Analytics analytics;

    @Mock
    OcrManager ocrManager;

    @Mock
    OcrResponse ocrResponse;

    @Mock
    Trip trip;

    @Mock
    Uri uri;

    private Exception exception = new Exception("Test");

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(factory.get(anyInt(), any(Trip.class))).thenReturn(processor);
        when(ocrManager.scan(any(File.class))).thenReturn(Observable.just(ocrResponse));
        when(uri.getScheme()).thenReturn(ContentResolver.SCHEME_CONTENT);

        fileResultImporter = new ActivityFileResultImporter(analytics, ocrManager, factory, Schedulers.trampoline(), Schedulers.trampoline());
    }

    @Test
    public void importFileWithProcessingFailure() {
        when(processor.process(uri)).thenReturn(Single.<File>error(exception));

        final TestObserver<ActivityFileResultImporterResponse> testObserver = fileResultImporter.getResultStream().test();
        fileResultImporter.importFile(RequestCodes.NEW_RECEIPT_IMPORT_IMAGE, Activity.RESULT_OK, uri, trip);

        testObserver.assertValue(ActivityFileResultImporterResponse.importerError(exception))
                .assertNotComplete()
                .assertSubscribed();
        verify(analytics).record(any(ErrorEvent.class));
    }

    @Test
    public void importFileWithValidUri() {
        final File file = new File("");
        final int requestCode = RequestCodes.NEW_RECEIPT_IMPORT_IMAGE;
        final int responseCode = Activity.RESULT_OK;
        when(processor.process(uri)).thenReturn(Single.just(file));

        TestObserver<ActivityFileResultImporterResponse> testObserver = fileResultImporter.getResultStream().test();
        fileResultImporter.importFile(requestCode, responseCode, uri, trip);

        testObserver.assertValue(ActivityFileResultImporterResponse.importerResponse(file, ocrResponse, requestCode, responseCode))
                .assertNotComplete()
                .assertNoErrors()
                .assertSubscribed();
    }

    @Test
    public void importFileWithValidSaveLocation() {
        final File file = new File("");
        final int requestCode = RequestCodes.NEW_RECEIPT_IMPORT_IMAGE;
        final int responseCode = Activity.RESULT_OK;
        when(processor.process(uri)).thenReturn(Single.just(file));

        final TestObserver<ActivityFileResultImporterResponse> testObserver = fileResultImporter.getResultStream().test();
        fileResultImporter.importFile(requestCode, responseCode, uri, trip);

        testObserver.assertValue(ActivityFileResultImporterResponse.importerResponse(file, ocrResponse, requestCode, responseCode))
                .assertNotComplete()
                .assertNoErrors()
                .assertSubscribed();
    }
}