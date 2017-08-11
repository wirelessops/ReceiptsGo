package co.smartreceipts.android.imports;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.io.FileNotFoundException;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.impl.DefaultTripImpl;
import co.smartreceipts.android.ocr.OcrManager;
import co.smartreceipts.android.ocr.apis.model.OcrResponse;
import co.smartreceipts.android.permissions.PermissionsDelegate;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ActivityFileResultImporterTest {

    // Class under test
    ActivityFileResultImporter fileResultImporter;

    @Mock
    ContentResolver contentResolver;

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
    Intent intent;

    @Mock
    Trip trip;

    @Mock
    PermissionsDelegate permissionsDelegate;

    @Mock
    Uri uri;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(factory.get(anyInt(), any(DefaultTripImpl.class))).thenReturn(processor);
        when(ocrManager.scan(any(File.class))).thenReturn(Observable.just(ocrResponse));
        when(permissionsDelegate.checkPermissionAndMaybeAsk(Manifest.permission.READ_EXTERNAL_STORAGE)).thenReturn(Completable.complete());
        when(uri.getScheme()).thenReturn(ContentResolver.SCHEME_CONTENT);

        fileResultImporter = new ActivityFileResultImporter(analytics,
                ocrManager, factory, permissionsDelegate, Schedulers.trampoline(), Schedulers.trampoline());
    }

    @Test
    public void onActivityResultCancelled() {
        final TestObserver<ActivityFileResultImporterResponse> testObserver = fileResultImporter.getResultStream().test();

        fileResultImporter.onActivityResult(RequestCodes.IMPORT_GALLERY_IMAGE, Activity.RESULT_CANCELED, null, null, trip);

        testObserver.assertNoValues();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void onActivityResultCancelledWithIndependentOrdering() {
        // Note that we flip the order of these two calls for this test
        fileResultImporter.onActivityResult(RequestCodes.IMPORT_GALLERY_IMAGE, Activity.RESULT_CANCELED, null, null, trip);
        fileResultImporter.getResultStream().test()
                .assertNoValues()
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void onActivityResultWithNullIntentAndNullLocation() {
        final TestObserver<ActivityFileResultImporterResponse> testObserver = fileResultImporter.getResultStream().test();
        fileResultImporter.onActivityResult(RequestCodes.IMPORT_GALLERY_IMAGE, Activity.RESULT_OK, null, null, trip);

        testObserver.assertNoValues();
        testObserver.assertNotComplete();
        testObserver.assertError(FileNotFoundException.class);
        verify(analytics).record(any(ErrorEvent.class));
    }

    @Test
    public void onActivityResultWithNullIntentAndNullLocationWithIndependentOrdering() {
        // Note that we flip the order of these two calls for this test
        fileResultImporter.onActivityResult(RequestCodes.IMPORT_GALLERY_IMAGE, Activity.RESULT_OK, null, null, trip);
        fileResultImporter.getResultStream().test()
                .assertNoValues()
                .assertNotComplete()
                .assertError(FileNotFoundException.class);
        verify(analytics).record(any(ErrorEvent.class));
    }

    @Test
    public void onActivityResultWithIntentNullDataAndNullLocation() {
        when(intent.getData()).thenReturn(null);

        final TestObserver<ActivityFileResultImporterResponse> testObserver = fileResultImporter.getResultStream().test();
        fileResultImporter.onActivityResult(RequestCodes.IMPORT_GALLERY_IMAGE, Activity.RESULT_OK, intent, null, trip);

        testObserver.assertNoValues();
        testObserver.assertNotComplete();
        testObserver.assertError(FileNotFoundException.class);
        verify(analytics).record(any(ErrorEvent.class));
    }

    @Test
    public void onActivityResultWithIntentNullDataAndNullLocationWithIndependentOrdering() {
        when(intent.getData()).thenReturn(null);

        // Note that we flip the order of these two calls for this test
        fileResultImporter.onActivityResult(RequestCodes.IMPORT_GALLERY_IMAGE, Activity.RESULT_OK, intent, null, trip);
        fileResultImporter.getResultStream().test()
                .assertNoValues()
                .assertNotComplete()
                .assertError(FileNotFoundException.class);
        verify(analytics).record(any(ErrorEvent.class));
    }

    @Test
    public void onActivityResultWithProcessingFailure() {
        when(intent.getData()).thenReturn(uri);
        when(processor.process(uri)).thenReturn(Single.<File>error(new Exception("Test")));

        final TestObserver<ActivityFileResultImporterResponse> testObserver = fileResultImporter.getResultStream().test();
        fileResultImporter.onActivityResult(RequestCodes.IMPORT_GALLERY_IMAGE, Activity.RESULT_OK, intent, null, trip);

        testObserver.assertNoValues();
        testObserver.assertNotComplete();
        testObserver.assertError(Exception.class);
        verify(analytics).record(any(ErrorEvent.class));
    }

    @Test
    public void onActivityResultWithProcessingFailureWithIndependentOrdering() {
        when(intent.getData()).thenReturn(uri);
        when(processor.process(uri)).thenReturn(Single.<File>error(new Exception("Test")));

        // Note that we flip the order of these two calls for this test
        fileResultImporter.onActivityResult(RequestCodes.IMPORT_GALLERY_IMAGE, Activity.RESULT_OK, intent, null, trip);
        fileResultImporter.getResultStream().test()
                .assertNoValues()
                .assertNotComplete()
                .assertError(Exception.class);
        verify(analytics).record(any(ErrorEvent.class));
    }

    @Test
    public void onActivityResultWithValidIntent() {
        final File file = new File("");
        final int requestCode = RequestCodes.IMPORT_GALLERY_IMAGE;
        final int responseCode = Activity.RESULT_OK;
        when(intent.getData()).thenReturn(uri);
        when(processor.process(uri)).thenReturn(Single.just(file));

        TestObserver<ActivityFileResultImporterResponse> testObserver = fileResultImporter.getResultStream().test();
        fileResultImporter.onActivityResult(requestCode, responseCode, intent, null, trip);

        testObserver.assertValue(new ActivityFileResultImporterResponse(file, ocrResponse, requestCode, responseCode));
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void onActivityResultWithValidIntentWithIndependentOrdering() {
        final File file = new File("");
        final int requestCode = RequestCodes.IMPORT_GALLERY_IMAGE;
        final int responseCode = Activity.RESULT_OK;
        when(intent.getData()).thenReturn(uri);
        when(processor.process(uri)).thenReturn(Single.just(file));

        // Note that we flip the order of these two calls for this test
        fileResultImporter.onActivityResult(requestCode, responseCode, intent, null, trip);
        fileResultImporter.getResultStream().test()
                .assertValue(new ActivityFileResultImporterResponse(file, ocrResponse, requestCode, responseCode))
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void onActivityResultWithValidSaveLocation() {
        final File file = new File("");
        final int requestCode = RequestCodes.IMPORT_GALLERY_IMAGE;
        final int responseCode = Activity.RESULT_OK;
        when(processor.process(uri)).thenReturn(Single.just(file));

        final TestObserver<ActivityFileResultImporterResponse> testObserver = fileResultImporter.getResultStream().test();
        fileResultImporter.onActivityResult(requestCode, responseCode, null, uri, trip);

        testObserver.assertValue(new ActivityFileResultImporterResponse(file, ocrResponse, requestCode, responseCode));
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void onActivityResultWithValidSaveLocationWithIndependentOrdering() {
//        final Uri uri = Uri.EMPTY;
        final File file = new File("");
        final int requestCode = RequestCodes.IMPORT_GALLERY_IMAGE;
        final int responseCode = Activity.RESULT_OK;
        when(processor.process(uri)).thenReturn(Single.just(file));

        // Note that we flip the order of these two calls for this test
        fileResultImporter.onActivityResult(requestCode, responseCode, null, uri, trip);
        fileResultImporter.getResultStream().test()
                .assertValue(new ActivityFileResultImporterResponse(file, ocrResponse, requestCode, responseCode))
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void checkPermissionIfFile() {
        final File file = new File("");
        final int requestCode = RequestCodes.IMPORT_GALLERY_IMAGE;
        final int responseCode = Activity.RESULT_OK;
        when(intent.getData()).thenReturn(uri);
        when(processor.process(uri)).thenReturn(Single.just(file));
        when(uri.getScheme()).thenReturn(ContentResolver.SCHEME_FILE);

        fileResultImporter.getResultStream().test();
        fileResultImporter.onActivityResult(requestCode, responseCode, intent, null, trip);

        verify(permissionsDelegate).checkPermissionAndMaybeAsk(Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    @Test
    public void dontCheckPermissionIfContent() {
        final File file = new File("");
        final int requestCode = RequestCodes.IMPORT_GALLERY_IMAGE;
        final int responseCode = Activity.RESULT_OK;
        when(intent.getData()).thenReturn(uri);
        when(processor.process(uri)).thenReturn(Single.just(file));
        when(uri.getScheme()).thenReturn(ContentResolver.SCHEME_CONTENT);

        fileResultImporter.getResultStream().test();
        fileResultImporter.onActivityResult(requestCode, responseCode, intent, null, trip);

        verify(permissionsDelegate, never()).checkPermissionAndMaybeAsk(Manifest.permission.READ_EXTERNAL_STORAGE);
    }

}