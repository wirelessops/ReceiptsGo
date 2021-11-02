package co.smartreceipts.android.ocr.purchases;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;

import org.reactivestreams.Subscriber;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import co.smartreceipts.android.apis.ApiValidationException;
import co.smartreceipts.android.apis.SmartReceiptsApiException;
import co.smartreceipts.android.apis.WebServiceManager;
import co.smartreceipts.android.purchases.PurchaseEventsListener;
import co.smartreceipts.android.purchases.PurchaseManager;
import co.smartreceipts.android.purchases.apis.purchases.MobileAppPurchasesService;
import co.smartreceipts.android.purchases.apis.purchases.PurchaseRequest;
import co.smartreceipts.android.purchases.consumption.DefaultInAppPurchaseConsumer;
import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.model.ManagedProduct;
import co.smartreceipts.android.purchases.model.PurchaseFamily;
import co.smartreceipts.android.purchases.source.PurchaseSource;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import co.smartreceipts.core.di.scopes.ApplicationScope;
import co.smartreceipts.core.identity.IdentityManager;
import co.smartreceipts.analytics.log.Logger;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;


@ApplicationScope
public class OcrPurchaseTracker implements PurchaseEventsListener {

    private static final String GOAL = "Recognition";

    private final IdentityManager identityManager;
    private final WebServiceManager webServiceManager;
    private final PurchaseManager purchaseManager;
    private final PurchaseWallet purchaseWallet;
    private final DefaultInAppPurchaseConsumer defaultInAppPurchaseConsumer;
    private final LocalOcrScansTracker localOcrScansTracker;
    private final Scheduler subscribeOnScheduler;

    @Inject
    public OcrPurchaseTracker(@NonNull IdentityManager identityManager,
                              @NonNull WebServiceManager webServiceManager,
                              @NonNull PurchaseManager purchaseManager,
                              @NonNull PurchaseWallet purchaseWallet,
                              @NonNull DefaultInAppPurchaseConsumer defaultInAppPurchaseConsumer,
                              @NonNull LocalOcrScansTracker localOcrScansTracker) {
        this(identityManager, webServiceManager, purchaseManager, purchaseWallet, defaultInAppPurchaseConsumer, localOcrScansTracker, Schedulers.io());
    }

    @VisibleForTesting
    OcrPurchaseTracker(@NonNull IdentityManager identityManager,
                       @NonNull WebServiceManager webServiceManager,
                       @NonNull PurchaseManager purchaseManager,
                       @NonNull PurchaseWallet purchaseWallet,
                       @NonNull DefaultInAppPurchaseConsumer defaultInAppPurchaseConsumer,
                       @NonNull LocalOcrScansTracker localOcrScansTracker,
                       @NonNull Scheduler subscribeOnScheduler) {
        this.identityManager = Preconditions.checkNotNull(identityManager);
        this.webServiceManager = Preconditions.checkNotNull(webServiceManager);
        this.purchaseManager = Preconditions.checkNotNull(purchaseManager);
        this.purchaseWallet = Preconditions.checkNotNull(purchaseWallet);
        this.defaultInAppPurchaseConsumer = Preconditions.checkNotNull(defaultInAppPurchaseConsumer);
        this.localOcrScansTracker = Preconditions.checkNotNull(localOcrScansTracker);
        this.subscribeOnScheduler = Preconditions.checkNotNull(subscribeOnScheduler);
    }

    @SuppressLint("CheckResult")
    public void initialize() {
        Logger.debug(this, "Initializing...");
        this.purchaseManager.addEventListener(this);

        this.identityManager.isLoggedInStream()
                .subscribeOn(subscribeOnScheduler)
                .filter(isLoggedIn -> isLoggedIn)
                .flatMap(aBoolean -> {
                    // Attempt to update our latest scan count
                    return fetchAndPersistAvailableRecognitions();
                })
                .flatMapSingle(integer -> purchaseManager.getAllOwnedPurchases())
                .flatMap(managedProducts -> {
                     for (final ManagedProduct managedProduct : managedProducts) {
                        if (managedProduct.getInAppPurchase().getPurchaseFamilies().contains(PurchaseFamily.Ocr)) {
                            Logger.debug(OcrPurchaseTracker.this, "Found available OCR purchase: {}", managedProduct.getInAppPurchase());
                            if (!defaultInAppPurchaseConsumer.isConsumed(managedProduct, PurchaseFamily.Ocr)) {
                                return uploadOcrPurchase(managedProduct);
                            } else {
                                Logger.debug(OcrPurchaseTracker.this, "But {} was already consumed...", managedProduct.getInAppPurchase());
                            }
                        }
                    }
                    return Observable.empty();
                })
                .subscribe(o -> Logger.info(OcrPurchaseTracker.this, "Successfully initialized"),
                        throwable -> Logger.error(OcrPurchaseTracker.this, "Failed to initialize.", throwable));
    }

    @Override
    public void onPurchaseSuccess(@NonNull InAppPurchase inAppPurchase, @NonNull PurchaseSource purchaseSource) {
        if (inAppPurchase.getPurchaseFamilies().contains(PurchaseFamily.Ocr)) {
            final ManagedProduct managedProduct = purchaseWallet.getLocalInAppManagedProduct(inAppPurchase);
            if (managedProduct != null) {
                this.identityManager.isLoggedInStream()
                        .subscribeOn(subscribeOnScheduler)
                        .filter(isLoggedIn -> isLoggedIn)
                        .flatMap(aBoolean -> uploadOcrPurchase(managedProduct))
                        .subscribe(o -> { /*onNext*/ },
                                throwable -> Logger.error(OcrPurchaseTracker.this, "Failed to upload purchase of " + managedProduct.getInAppPurchase(), throwable),
                                () -> Logger.info(OcrPurchaseTracker.this, "Successfully uploaded and consumed purchase of {}.", managedProduct.getInAppPurchase())
                        );
            }
        }
    }

    @Override
    public void onPurchaseFailed(@NonNull PurchaseSource purchaseSource) {

    }

    @Override
    public void onPurchasePending() {
        /*no-op*/
    }

    /**
     * @return the remaining Ocr scan count that is allowed for this user. Please note that is
     * this not the authority for this (ie it's not the server), this may not be fully accurate, so we
     * may still get a remote error after a scan
     */
    public int getRemainingScans() {
        return localOcrScansTracker.getRemainingScans();
    }

    /**
     * @return the remaining Ocr scan count that is allowed for this user. Please note that is
     * this not the authority for this (ie it's not the server), this may not be fully accurate, so we
     * may still get a remote error after a scan. Additionally, please note that this {@link Observable}
     * will only call {@link Subscriber#onNext(Object)} with the latest value (and never onComplete or
     * onError) to allow us to continually get the updated value
     */
    public Observable<Integer> getRemainingScansStream() {
        return localOcrScansTracker.getRemainingScansStream();
    }

    /**
     * @return {@code true} if we have OCR scans remaining. {@code false} otherwise. Please note that is
     * this not the authority for this (ie it's not the server), this may not be fully accurate, so we
     * may still get a remote error after a scan
     */
    public boolean hasAvailableScans() {
        return localOcrScansTracker.getRemainingScans() > 0;
    }

    /**
     * Decrements the remaining scan count by 1, to indicate that we've successfully used one of our scans
     */
    public void decrementRemainingScans() {
        localOcrScansTracker.decrementRemainingScans();
    }

    @NonNull
    private Observable<Object> uploadOcrPurchase(@NonNull final ManagedProduct managedProduct) {
        if (!managedProduct.getInAppPurchase().getPurchaseFamilies().contains(PurchaseFamily.Ocr)) {
            throw new IllegalArgumentException("Unsupported purchase type: " + managedProduct.getInAppPurchase());
        }

        Logger.info(this, "Uploading consumable purchase: {}", managedProduct.getInAppPurchase());
        return webServiceManager.getService(MobileAppPurchasesService.class).addPurchase(new PurchaseRequest(managedProduct, GOAL))
                .flatMap(purchaseResponse -> {
                    Logger.debug(OcrPurchaseTracker.this, "Received purchase response of {}", purchaseResponse);
                    return defaultInAppPurchaseConsumer.consumePurchase(managedProduct, PurchaseFamily.Ocr)
                            .andThen(Observable.just(new Object()));
                })
                .onErrorResumeNext(throwable -> {
                    Logger.debug(this, "Got error while tried to upload purchase " + managedProduct.getInAppPurchase().getSku()
                            + ": " + throwable.getLocalizedMessage());
                    if (throwable instanceof SmartReceiptsApiException) {
                        final SmartReceiptsApiException smartReceiptsApiException = (SmartReceiptsApiException) throwable;
                        final List<String> errors;
                        if (smartReceiptsApiException.getErrorResponse() != null && smartReceiptsApiException.getErrorResponse().getErrors() != null) {
                            errors = smartReceiptsApiException.getErrorResponse().getErrors();
                        } else {
                            errors = Collections.emptyList();
                        }
                        if (smartReceiptsApiException.getResponse().code() == 422 && errors.contains("Purchase has already been taken")) {
                            Logger.warn(OcrPurchaseTracker.this, "Found repeat purchase. Consuming it now");
                            return defaultInAppPurchaseConsumer.consumePurchase(managedProduct, PurchaseFamily.Ocr)
                                    .andThen(Observable.just(new Object()));
                        }
                    }
                    return Observable.error(throwable);
                })
                .flatMap(o -> fetchAndPersistAvailableRecognitions());

    }

    @NonNull
    private Observable<Integer> fetchAndPersistAvailableRecognitions() {
        return this.identityManager.getMe()
                .subscribeOn(subscribeOnScheduler)
                .flatMap(meResponse -> {
                    if (meResponse.getUser() != null) {
                        return Observable.just(meResponse.getUser().getRecognitionsAvailable());
                    } else {
                        return Observable.error(new ApiValidationException("Failed to get a user response back"));
                    }
                })
                .doOnNext(recognitionsAvailable -> {
                    if (recognitionsAvailable != null) {
                        localOcrScansTracker.setRemainingScans(recognitionsAvailable);
                    }
                })
                .doOnError(throwable -> Logger.error(OcrPurchaseTracker.this, "Failed to get the available OCR scans", throwable))
                .onErrorReturn(throwable -> {
                    return 0; // ignore errors and keep moving to get the owned purchases
                });
    }
}
