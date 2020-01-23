package co.smartreceipts.android.ocr;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.io.File;

import co.smartreceipts.push.PushManagerImpl;
import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.android.apis.WebServiceManager;
import co.smartreceipts.android.config.ConfigurationManager;
import co.smartreceipts.android.ocr.apis.OcrService;
import co.smartreceipts.android.ocr.apis.model.OcrResponse;
import co.smartreceipts.android.ocr.apis.model.RecognitionRequest;
import co.smartreceipts.android.ocr.apis.model.RecognitionResponse;
import co.smartreceipts.android.ocr.purchases.OcrPurchaseTracker;
import co.smartreceipts.push.ocr.OcrPushMessageReceiver;
import co.smartreceipts.push.ocr.OcrPushMessageReceiverFactory;
import co.smartreceipts.android.ocr.widget.tooltip.OcrInformationalTooltipInteractor;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.utils.ConfigurableResourceFeature;
import co.smartreceipts.aws.s3.S3Manager;
import co.smartreceipts.core.identity.IdentityManager;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class OcrManagerTest {

    private static final String IMG_NAME = "123456789.jpg";
    private static final String ID = "id";

    // Class under test
    OcrManagerImpl ocrManager;

    Context context = ApplicationProvider.getApplicationContext();

    @Mock
    S3Manager s3Manager;

    @Mock
    IdentityManager identityManager;

    @Mock
    WebServiceManager ocrWebServiceManager;

    @Mock
    PushManagerImpl pushManager;

    @Mock
    OcrPurchaseTracker ocrPurchaseTracker;

    @Mock
    OcrInformationalTooltipInteractor ocrInformationalTooltipInteractor;

    @Mock
    OcrPushMessageReceiverFactory ocrPushMessageReceiverFactory;

    @Mock
    OcrPushMessageReceiver pushMessageReceiver;

    @Mock
    UserPreferenceManager userPreferenceManager;

    @Mock
    Analytics analytics;

    @Mock
    ConfigurationManager configurationManager;

    @Mock
    File file;

    @Mock
    OcrService ocrService;

    @Mock
    RecognitionResponse recognitionResponse;

    @Mock
    RecognitionResponse.Recognition recognition;

    @Mock
    RecognitionResponse.RecognitionData recognitionData;

    @Mock
    OcrResponse ocrResponse;

    TestObserver<OcrResponse> testObserver;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        testObserver = new TestObserver<>();

        when(configurationManager.isEnabled(ConfigurableResourceFeature.Ocr)).thenReturn(true);
        when(identityManager.isLoggedIn()).thenReturn(true);
        when(ocrPurchaseTracker.hasAvailableScans()).thenReturn(true);
        when(ocrPushMessageReceiverFactory.get()).thenReturn(pushMessageReceiver);
        when(s3Manager.upload(file, "ocr/")).thenReturn(Observable.just("https://aws.amazon.com/smartreceipts/ocr/" + IMG_NAME));
        when(ocrWebServiceManager.getService(OcrService.class)).thenReturn(ocrService);
        when(recognitionResponse.getRecognition()).thenReturn(recognition);
        when(recognition.getId()).thenReturn(ID);
        when(recognition.getData()).thenReturn(recognitionData);
        when(recognitionData.getRecognitionData()).thenReturn(ocrResponse);
        when(ocrService.scanReceipt(new RecognitionRequest("ocr/" + IMG_NAME, false))).thenReturn(Observable.just(recognitionResponse));
        when(ocrService.scanReceipt(new RecognitionRequest("ocr/" + IMG_NAME, true))).thenReturn(Observable.just(recognitionResponse));
        when(pushMessageReceiver.getPushResponse()).thenReturn(Observable.just(new Object()));
        when(ocrService.getRecognitionResult(ID)).thenReturn(Observable.just(recognitionResponse));
        when(userPreferenceManager.get(UserPreference.Misc.OcrIsEnabled)).thenReturn(true);
        when(userPreferenceManager.get(UserPreference.Misc.OcrIncognitoMode)).thenReturn(false);

        ocrManager = new OcrManagerImpl(s3Manager, identityManager, ocrWebServiceManager, pushManager, ocrPurchaseTracker,
                ocrInformationalTooltipInteractor, userPreferenceManager, analytics, ocrPushMessageReceiverFactory, configurationManager);
    }

    @Test
    public void initialize() {
        ocrManager.initialize();
        verify(ocrPurchaseTracker).initialize();
        verify(ocrInformationalTooltipInteractor).initialize();
    }

    @Test
    public void scanWhenFeatureIsDisabled() {
        when(configurationManager.isEnabled(ConfigurableResourceFeature.Ocr)).thenReturn(false);
        ocrManager.scan(file).subscribe(testObserver);

        testObserver.awaitTerminalEvent();
        testObserver.assertValue(new OcrResponse());
        testObserver.onComplete();
        testObserver.assertNoErrors();
        verifyZeroInteractions(s3Manager, ocrWebServiceManager, pushManager, pushMessageReceiver);
        verify(ocrPurchaseTracker, never()).decrementRemainingScans();
    }

    @Test
    public void scanWhenNotLoggedIn() {
        when(identityManager.isLoggedIn()).thenReturn(false);
        ocrManager.scan(file).subscribe(testObserver);

        testObserver.awaitTerminalEvent();
        testObserver.assertValue(new OcrResponse());
        testObserver.onComplete();
        testObserver.assertNoErrors();
        verifyZeroInteractions(s3Manager, ocrWebServiceManager, pushManager, pushMessageReceiver);
        verify(ocrPurchaseTracker, never()).decrementRemainingScans();
    }

    @Test
    public void scanWithNoAvailableScans() {
        when(ocrPurchaseTracker.hasAvailableScans()).thenReturn(false);
        ocrManager.scan(file).subscribe(testObserver);

        testObserver.awaitTerminalEvent();
        testObserver.assertValue(new OcrResponse());
        testObserver.onComplete();
        testObserver.assertNoErrors();
        verifyZeroInteractions(s3Manager, ocrWebServiceManager, pushManager, pushMessageReceiver);
        verify(ocrPurchaseTracker, never()).decrementRemainingScans();
    }

    @Test
    public void scanWhenNotEnabled() {
        when(userPreferenceManager.get(UserPreference.Misc.OcrIsEnabled)).thenReturn(false);
        ocrManager.scan(file).subscribe(testObserver);

        testObserver.awaitTerminalEvent();
        testObserver.assertValue(new OcrResponse());
        testObserver.onComplete();
        testObserver.assertNoErrors();
        verifyZeroInteractions(s3Manager, ocrWebServiceManager, pushManager, pushMessageReceiver);
        verify(ocrPurchaseTracker, never()).decrementRemainingScans();
    }

    @Test
    public void scanButS3UploadFails() {
        when(s3Manager.upload(file, "ocr/")).thenReturn(Observable.error(new Exception("test")));
        ocrManager.scan(file).subscribe(testObserver);

        testObserver.awaitTerminalEvent();
        testObserver.assertValue(new OcrResponse());
        testObserver.onComplete();
        testObserver.assertNoErrors();
        verify(s3Manager).upload(file, "ocr/");
        verify(pushManager).registerReceiver(pushMessageReceiver);
        verify(pushManager).unregisterReceiver(pushMessageReceiver);
        verify(ocrService, never()).scanReceipt(any(RecognitionRequest.class));
        verify(ocrService, never()).getRecognitionResult(anyString());
        verify(ocrPurchaseTracker, never()).decrementRemainingScans();
        verify(pushMessageReceiver, never()).getPushResponse();
        verifyZeroInteractions(ocrWebServiceManager);
    }

    @Test
    public void scanButS3ReturnsUnexpectedUrl() {
        when(s3Manager.upload(file, "ocr/")).thenReturn(Observable.just("https://test.com"));
        ocrManager.scan(file).subscribe(testObserver);

        testObserver.awaitTerminalEvent();
        testObserver.assertValue(new OcrResponse());
        testObserver.onComplete();
        testObserver.assertNoErrors();
        verify(s3Manager).upload(file, "ocr/");
        verify(pushManager).registerReceiver(pushMessageReceiver);
        verify(pushManager).unregisterReceiver(pushMessageReceiver);
        verify(ocrService, never()).scanReceipt(any(RecognitionRequest.class));
        verify(ocrService, never()).getRecognitionResult(anyString());
        verify(ocrPurchaseTracker, never()).decrementRemainingScans();
        verify(pushMessageReceiver, never()).getPushResponse();
        verifyZeroInteractions(ocrWebServiceManager);
    }

    @Test
    public void scanButRecognitionRequestFails() {
        when(ocrService.scanReceipt(new RecognitionRequest("ocr/" + IMG_NAME, false))).thenReturn(Observable.error(new Exception("test")));
        ocrManager.scan(file).subscribe(testObserver);

        testObserver.awaitTerminalEvent();
        testObserver.assertValue(new OcrResponse());
        testObserver.onComplete();
        testObserver.assertNoErrors();
        verify(s3Manager).upload(file, "ocr/");
        verify(ocrService).scanReceipt(new RecognitionRequest("ocr/" + IMG_NAME, false));
        verify(pushManager).registerReceiver(pushMessageReceiver);
        verify(pushManager).unregisterReceiver(pushMessageReceiver);
        verify(ocrService, never()).getRecognitionResult(anyString());
        verify(ocrPurchaseTracker, never()).decrementRemainingScans();
        verify(pushMessageReceiver, never()).getPushResponse();
    }

    @Test
    public void scanButRecognitionResponseIsInvalidWithNullId() {
        when(recognition.getId()).thenReturn(null);
        ocrManager.scan(file).subscribe(testObserver);

        testObserver.awaitTerminalEvent();
        testObserver.assertValue(new OcrResponse());
        testObserver.onComplete();
        testObserver.assertNoErrors();
        verify(s3Manager).upload(file, "ocr/");
        verify(ocrService).scanReceipt(new RecognitionRequest("ocr/" + IMG_NAME, false));
        verify(pushManager).registerReceiver(pushMessageReceiver);
        verify(pushManager).unregisterReceiver(pushMessageReceiver);
        verify(ocrService, never()).getRecognitionResult(anyString());
        verify(ocrPurchaseTracker, never()).decrementRemainingScans();
        verify(ocrPurchaseTracker, never()).decrementRemainingScans();
        verify(pushMessageReceiver, never()).getPushResponse();
    }

    @Test
    public void scanButGetRecognitionResultDetails() {
        when(ocrService.getRecognitionResult(ID)).thenReturn(Observable.error(new Exception("test")));
        ocrManager.scan(file).subscribe(testObserver);

        testObserver.awaitTerminalEvent();
        testObserver.assertValue(new OcrResponse());
        testObserver.onComplete();
        testObserver.assertNoErrors();
        verify(s3Manager).upload(file, "ocr/");
        verify(ocrService).scanReceipt(new RecognitionRequest("ocr/" + IMG_NAME, false));
        verify(ocrPurchaseTracker, never()).decrementRemainingScans();
        verify(pushManager).registerReceiver(pushMessageReceiver);
        verify(pushManager).unregisterReceiver(pushMessageReceiver);
    }

    @Test
    public void scanCompletes() {
        ocrManager.scan(file).subscribe(testObserver);

        testObserver.awaitTerminalEvent();
        testObserver.assertValue(ocrResponse);
        testObserver.onComplete();
        testObserver.assertNoErrors();
        verify(s3Manager).upload(file, "ocr/");
        verify(ocrService).scanReceipt(new RecognitionRequest("ocr/" + IMG_NAME, false));
        verify(ocrPurchaseTracker).decrementRemainingScans();
        verify(pushManager).registerReceiver(pushMessageReceiver);
        verify(pushManager).unregisterReceiver(pushMessageReceiver);
    }

    @Test
    public void scanCompletesWithIncognitoModeOn() {
        when(userPreferenceManager.get(UserPreference.Misc.OcrIncognitoMode)).thenReturn(true);
        ocrManager.scan(file).subscribe(testObserver);

        testObserver.awaitTerminalEvent();
        testObserver.assertValue(ocrResponse);
        testObserver.onComplete();
        testObserver.assertNoErrors();
        verify(s3Manager).upload(file, "ocr/");
        verify(ocrService).scanReceipt(new RecognitionRequest("ocr/" + IMG_NAME, true));
        verify(ocrPurchaseTracker).decrementRemainingScans();
        verify(pushManager).registerReceiver(pushMessageReceiver);
        verify(pushManager).unregisterReceiver(pushMessageReceiver);
    }

    @Test
    public void scanCompletesEvenIfPushMessageTimesOutStillContinuesProcessing() {
        when(pushMessageReceiver.getPushResponse()).thenReturn(Observable.error(new Exception("timeout")));
        ocrManager.scan(file).subscribe(testObserver);

        testObserver.awaitTerminalEvent();
        testObserver.assertValue(ocrResponse);
        testObserver.onComplete();
        testObserver.assertNoErrors();
        verify(s3Manager).upload(file, "ocr/");
        verify(ocrService).scanReceipt(new RecognitionRequest("ocr/" + IMG_NAME, false));
        verify(ocrPurchaseTracker).decrementRemainingScans();
        verify(pushManager).registerReceiver(pushMessageReceiver);
        verify(pushManager).unregisterReceiver(pushMessageReceiver);
    }

}