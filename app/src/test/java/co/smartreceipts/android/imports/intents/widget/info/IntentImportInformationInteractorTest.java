package co.smartreceipts.android.imports.intents.widget.info;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.activities.SmartReceiptsActivity;
import co.smartreceipts.android.imports.intents.IntentImportProcessor;
import co.smartreceipts.android.imports.intents.model.FileType;
import co.smartreceipts.android.imports.intents.model.IntentImportResult;
import co.smartreceipts.android.permissions.ActivityPermissionsRequester;
import co.smartreceipts.android.permissions.PermissionAuthorizationResponse;
import co.smartreceipts.android.permissions.PermissionStatusChecker;
import co.smartreceipts.android.permissions.exceptions.PermissionsNotGrantedException;
import co.smartreceipts.android.widget.model.UiIndicator;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@RunWith(RobolectricTestRunner.class)
public class IntentImportInformationInteractorTest {

    @InjectMocks
    IntentImportInformationInteractor interactor;

    @Mock
    IntentImportProcessor intentImportProcessor;

    @Mock
    PermissionStatusChecker permissionStatusChecker;

    @Mock
    ActivityPermissionsRequester<SmartReceiptsActivity> permissionRequester;

    Intent intent = new Intent(Intent.ACTION_SEND);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        interactor = new IntentImportInformationInteractor(intentImportProcessor, permissionStatusChecker, permissionRequester);
    }

    @Test
    public void processEmptyMaybe() {
        when(intentImportProcessor.process(intent)).thenReturn(Maybe.empty());
        interactor.process(intent).test()
                .assertValue(UiIndicator.idle())
                .assertComplete()
                .assertNoErrors();

        verifyZeroInteractions(permissionRequester);
    }

    @Test
    public void processContentScheme() {
        final IntentImportResult result = new IntentImportResult(Uri.parse("content://uri"), FileType.Image);
        when(intentImportProcessor.process(intent)).thenReturn(Maybe.just(result));

        final TestObserver<UiIndicator<IntentImportResult>> testObserver1 = interactor.process(intent).test();
        testObserver1.awaitTerminalEvent();
        testObserver1.assertValues(UiIndicator.idle(), UiIndicator.success(result))
                .assertComplete()
                .assertNoErrors();

        // Verify that we only get this result once
        final TestObserver<UiIndicator<IntentImportResult>> testObserver2 = interactor.process(intent).test();
        testObserver2.awaitTerminalEvent();
        testObserver2.assertValue(UiIndicator.idle())
                .assertComplete()
                .assertNoErrors();

        verifyZeroInteractions(permissionRequester);
    }

    @Test
    public void processFileSchemeWithPermissions() {
        final IntentImportResult result = new IntentImportResult(Uri.parse("file://uri"), FileType.Image);
        when(intentImportProcessor.process(intent)).thenReturn(Maybe.just(result));
        when(permissionStatusChecker.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)).thenReturn(Single.just(true));

        final TestObserver<UiIndicator<IntentImportResult>> testObserver1 = interactor.process(intent).test();
        testObserver1.awaitTerminalEvent();
        testObserver1.assertValues(UiIndicator.idle(), UiIndicator.success(result))
                .assertComplete()
                .assertNoErrors();

        // Verify that we only get this result once
        final TestObserver<UiIndicator<IntentImportResult>> testObserver2 = interactor.process(intent).test();
        testObserver2.awaitTerminalEvent();
        testObserver2.assertValue(UiIndicator.idle())
                .assertComplete()
                .assertNoErrors();

        verifyZeroInteractions(permissionRequester);
    }

    @Test
    public void processFileSchemeWithoutPermissionsAndUserGrantsThem() {
        final IntentImportResult result = new IntentImportResult(Uri.parse("file://uri"), FileType.Image);
        when(intentImportProcessor.process(intent)).thenReturn(Maybe.just(result));
        when(permissionStatusChecker.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)).thenReturn(Single.just(false));
        when(permissionRequester.request(Manifest.permission.READ_EXTERNAL_STORAGE)).thenReturn(Single.just(new PermissionAuthorizationResponse(Manifest.permission.READ_EXTERNAL_STORAGE, true)));

        final TestObserver<UiIndicator<IntentImportResult>> testObserver1 = interactor.process(intent).test();
        testObserver1.awaitTerminalEvent();
        testObserver1.assertValues(UiIndicator.idle(), UiIndicator.success(result))
                .assertComplete()
                .assertNoErrors();

        // Verify that we only get this result once
        final TestObserver<UiIndicator<IntentImportResult>> testObserver2 = interactor.process(intent).test();
        testObserver2.awaitTerminalEvent();
        testObserver2.assertValue(UiIndicator.idle())
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void processFileSchemeWithoutPermissionsAndUserDoesNotGrantThem() {
        final IntentImportResult result = new IntentImportResult(Uri.parse("file://uri"), FileType.Image);
        when(intentImportProcessor.process(intent)).thenReturn(Maybe.just(result));
        when(permissionStatusChecker.isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)).thenReturn(Single.just(false));
        when(permissionRequester.request(Manifest.permission.READ_EXTERNAL_STORAGE)).thenReturn(Single.just(new PermissionAuthorizationResponse(Manifest.permission.READ_EXTERNAL_STORAGE, false)));

        final TestObserver<UiIndicator<IntentImportResult>> testObserver1 = interactor.process(intent).test();
        testObserver1.awaitTerminalEvent();
        testObserver1.assertValues(UiIndicator.idle())
                .assertNotComplete()
                .assertError(PermissionsNotGrantedException.class);

        // Verify that we get this result every time (ie always request)
        final TestObserver<UiIndicator<IntentImportResult>> testObserver2 = interactor.process(intent).test();
        testObserver2.awaitTerminalEvent();
        testObserver2.assertValues(UiIndicator.idle())
                .assertNotComplete()
                .assertError(PermissionsNotGrantedException.class);
    }

}