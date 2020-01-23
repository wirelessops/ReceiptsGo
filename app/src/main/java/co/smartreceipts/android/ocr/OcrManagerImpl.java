package co.smartreceipts.android.ocr;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;

import java.io.File;

import javax.inject.Inject;

import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.analytics.events.ErrorEvent;
import co.smartreceipts.analytics.events.Events;
import co.smartreceipts.android.apis.ApiValidationException;
import co.smartreceipts.android.apis.WebServiceManager;
import co.smartreceipts.android.config.ConfigurationManager;
import co.smartreceipts.android.ocr.apis.OcrService;
import co.smartreceipts.android.ocr.apis.model.OcrResponse;
import co.smartreceipts.android.ocr.apis.model.RecognitionRequest;
import co.smartreceipts.android.ocr.purchases.OcrPurchaseTracker;
import co.smartreceipts.android.ocr.widget.alert.OcrProcessingStatus;
import co.smartreceipts.android.ocr.widget.tooltip.OcrInformationalTooltipInteractor;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.utils.ConfigurableResourceFeature;
import co.smartreceipts.aws.s3.S3Manager;
import co.smartreceipts.core.di.scopes.ApplicationScope;
import co.smartreceipts.core.identity.IdentityManager;
import co.smartreceipts.analytics.log.Logger;
import co.smartreceipts.push.PushManagerImpl;
import co.smartreceipts.push.ocr.OcrPushMessageReceiver;
import co.smartreceipts.push.ocr.OcrPushMessageReceiverFactory;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

@ApplicationScope
public class OcrManagerImpl implements OcrManager {

    private static final String OCR_FOLDER = "ocr/";

    private final S3Manager s3Manager;
    private final IdentityManager identityManager;
    private final WebServiceManager ocrWebServiceManager;
    private final PushManagerImpl pushManager;
    private final UserPreferenceManager userPreferenceManager;
    private final Analytics analytics;
    private final OcrPurchaseTracker ocrPurchaseTracker;
    private final OcrInformationalTooltipInteractor ocrInformationalTooltipInteractor;
    private final OcrPushMessageReceiverFactory pushMessageReceiverFactory;
    private final ConfigurationManager configurationManager;
    private final BehaviorSubject<OcrProcessingStatus> ocrProcessingStatusSubject = BehaviorSubject.createDefault(OcrProcessingStatus.Idle);

    @Inject
    public OcrManagerImpl(@NonNull S3Manager s3Manager,
                          @NonNull IdentityManager identityManager,
                          @NonNull WebServiceManager webServiceManager,
                          @NonNull PushManagerImpl pushManager,
                          @NonNull OcrPurchaseTracker ocrPurchaseTracker,
                          @NonNull OcrInformationalTooltipInteractor ocrInformationalTooltipInteractor,
                          @NonNull UserPreferenceManager userPreferenceManager,
                          @NonNull Analytics analytics,
                          @NonNull ConfigurationManager configurationManager) {
        this(s3Manager, identityManager, webServiceManager, pushManager, ocrPurchaseTracker, ocrInformationalTooltipInteractor,
                userPreferenceManager, analytics, new OcrPushMessageReceiverFactory(), configurationManager);
    }

    @VisibleForTesting
    OcrManagerImpl(@NonNull S3Manager s3Manager,
                   @NonNull IdentityManager identityManager,
                   @NonNull WebServiceManager webServiceManager,
                   @NonNull PushManagerImpl pushManager,
                   @NonNull OcrPurchaseTracker ocrPurchaseTracker,
                   @NonNull OcrInformationalTooltipInteractor ocrInformationalTooltipInteractor,
                   @NonNull UserPreferenceManager userPreferenceManager,
                   @NonNull Analytics analytics,
                   @NonNull OcrPushMessageReceiverFactory pushMessageReceiverFactory,
                   @NonNull ConfigurationManager configurationManager) {
        this.s3Manager = Preconditions.checkNotNull(s3Manager);
        this.identityManager = Preconditions.checkNotNull(identityManager);
        this.ocrWebServiceManager = Preconditions.checkNotNull(webServiceManager);
        this.pushManager = Preconditions.checkNotNull(pushManager);
        this.ocrPurchaseTracker = Preconditions.checkNotNull(ocrPurchaseTracker);
        this.ocrInformationalTooltipInteractor = Preconditions.checkNotNull(ocrInformationalTooltipInteractor);
        this.userPreferenceManager = Preconditions.checkNotNull(userPreferenceManager);
        this.analytics = Preconditions.checkNotNull(analytics);
        this.pushMessageReceiverFactory = Preconditions.checkNotNull(pushMessageReceiverFactory);
        this.configurationManager = Preconditions.checkNotNull(configurationManager);
    }

    @Override
    public void initialize() {
        ocrPurchaseTracker.initialize();
        ocrInformationalTooltipInteractor.initialize();
    }

    @Override
    @NonNull
    public Observable<OcrResponse> scan(@NonNull File file) {
        Preconditions.checkNotNull(file);

        ocrProcessingStatusSubject.onNext(OcrProcessingStatus.Idle);
        final boolean isOcrEnabled = configurationManager.isEnabled(ConfigurableResourceFeature.Ocr);
        if (isOcrEnabled && identityManager.isLoggedIn() && ocrPurchaseTracker.hasAvailableScans() && userPreferenceManager.get(UserPreference.Misc.OcrIsEnabled)) {
            Logger.info(OcrManagerImpl.this, "Initiating scan of {}.", file);
            final OcrPushMessageReceiver ocrPushMessageReceiver = pushMessageReceiverFactory.get();
            ocrProcessingStatusSubject.onNext(OcrProcessingStatus.UploadingImage);
            return s3Manager.upload(file, OCR_FOLDER)
                    .doOnSubscribe(disposable -> {
                        pushManager.registerReceiver(ocrPushMessageReceiver);
                        analytics.record(Events.Ocr.OcrRequestStarted);
                    })
                    .subscribeOn(Schedulers.io())
                    .flatMap(s3Url -> {
                        Logger.debug(OcrManagerImpl.this, "S3 upload completed. Preparing url for delivery to our APIs.");
                        if (s3Url != null && s3Url.indexOf(OCR_FOLDER) > 0) {
                            return Observable.just(s3Url.substring(s3Url.indexOf(OCR_FOLDER)));
                        } else {
                            return Observable.error(new ApiValidationException("Failed to receive a valid url: " + s3Url));
                        }
                    })
                    .flatMap(s3Url -> {
                        Logger.debug(OcrManagerImpl.this, "Uploading OCR request for processing");
                        ocrProcessingStatusSubject.onNext(OcrProcessingStatus.PerformingScan);
                        final boolean incognito = userPreferenceManager.get(UserPreference.Misc.OcrIncognitoMode);
                        return ocrWebServiceManager.getService(OcrService.class).scanReceipt(new RecognitionRequest(s3Url, incognito));
                    })
                    .flatMap(recognitionResponse -> {
                        if (recognitionResponse != null && recognitionResponse.getRecognition() != null && recognitionResponse.getRecognition().getId() != null) {
                            return Observable.just(recognitionResponse.getRecognition().getId());
                        } else {
                            return Observable.error(new ApiValidationException("Failed to receive a valid recognition upload response."));
                        }
                    })
                    .flatMap(recognitionId -> {
                        Logger.debug(OcrManagerImpl.this, "Awaiting completion of recognition request {}.", recognitionId);
                        return ocrPushMessageReceiver.getPushResponse()
                                .doOnNext(ignore -> analytics.record(Events.Ocr.OcrPushMessageReceived))
                                .doOnError(ignore -> analytics.record(Events.Ocr.OcrPushMessageTimeOut))
                                .onErrorReturn(throwable -> {
                                    Logger.warn(OcrManagerImpl.this, "Ocr request timed out. Attempting to get response as is");
                                    return new Object();
                                })
                                .map(o -> recognitionId);
                    })
                    .flatMap(recognitionId -> {
                        Logger.debug(OcrManagerImpl.this, "Scan completed. Fetching results for {}.", recognitionId);
                        ocrProcessingStatusSubject.onNext(OcrProcessingStatus.RetrievingResults);
                        return ocrWebServiceManager.getService(OcrService.class).getRecognitionResult(recognitionId);
                    })
                    .flatMap(recognitionResponse -> {
                        Logger.debug(OcrManagerImpl.this, "Parsing OCR Response");
                        if (recognitionResponse != null &&
                                recognitionResponse.getRecognition() != null &&
                                recognitionResponse.getRecognition().getData() != null &&
                                recognitionResponse.getRecognition().getData().getRecognitionData() != null) {
                            return Observable.just(recognitionResponse.getRecognition().getData().getRecognitionData());
                        } else {
                            return Observable.error(new ApiValidationException("Failed to receive a valid recognition complete response."));
                        }
                    })
                    .doOnNext(ignore -> {
                        ocrPurchaseTracker.decrementRemainingScans();
                        analytics.record(Events.Ocr.OcrRequestSucceeded);
                    })
                    .doOnError(throwable -> {
                        analytics.record(Events.Ocr.OcrRequestFailed);
                        analytics.record(new ErrorEvent(OcrManagerImpl.this, throwable));
                    })
                    .onErrorReturnItem(new OcrResponse())
                    .doOnTerminate(() -> {
                        ocrProcessingStatusSubject.onNext(OcrProcessingStatus.Idle);
                        pushManager.unregisterReceiver(ocrPushMessageReceiver);
                    });
        } else {
            Logger.debug(OcrManagerImpl.this, "Ignoring ocr scan of as: isFeatureEnabled = {}, isLoggedIn = {}, hasAvailableScans = {}.", isOcrEnabled, identityManager.isLoggedIn(), ocrPurchaseTracker.hasAvailableScans());
            return Observable.just(new OcrResponse());
        }
    }

    @Override
    @NonNull
    public Observable<OcrProcessingStatus> getOcrProcessingStatus() {
        return ocrProcessingStatusSubject.subscribeOn(Schedulers.computation());
    }
}
