package co.smartreceipts.android.ocr.widget.configuration;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.DataPoint;
import co.smartreceipts.android.analytics.events.DefaultDataPointEvent;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.identity.store.EmailAddress;
import co.smartreceipts.android.ocr.purchases.OcrPurchaseTracker;
import co.smartreceipts.android.purchases.PurchaseManager;
import co.smartreceipts.android.purchases.model.AvailablePurchase;
import co.smartreceipts.android.purchases.model.ConsumablePurchase;
import co.smartreceipts.android.purchases.model.PurchaseFamily;
import co.smartreceipts.android.purchases.source.PurchaseSource;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;

@FragmentScope
public class OcrConfigurationInteractor {

    private final IdentityManager identityManager;
    private final OcrPurchaseTracker ocrPurchaseTracker;
    private final PurchaseManager purchaseManager;
    private final UserPreferenceManager userPreferenceManager;
    private final Analytics analytics;

    @Inject
    public OcrConfigurationInteractor(@NonNull IdentityManager identityManager, @NonNull OcrPurchaseTracker ocrPurchaseTracker,
                                      @NonNull PurchaseManager purchaseManager, @NonNull UserPreferenceManager userPreferenceManager,
                                      @NonNull Analytics analytics) {
        this.identityManager = Preconditions.checkNotNull(identityManager);
        this.ocrPurchaseTracker = Preconditions.checkNotNull(ocrPurchaseTracker);
        this.purchaseManager = Preconditions.checkNotNull(purchaseManager);
        this.userPreferenceManager = Preconditions.checkNotNull(userPreferenceManager);
        this.analytics = Preconditions.checkNotNull(analytics);
    }

    @Nullable
    public EmailAddress getEmail() {
        return identityManager.getEmail();
    }

    @NonNull
    public Observable<Integer> getRemainingScansStream() {
        return ocrPurchaseTracker.getRemainingScansStream()
                .observeOn(AndroidSchedulers.mainThread());
    }

    @NonNull
    public Single<List<AvailablePurchase>> getAvailableOcrPurchases() {
        return purchaseManager.getAllAvailablePurchases()
                .flatMapIterable(availablePurchases -> availablePurchases)
                .filter(availablePurchase -> availablePurchase.getInAppPurchase() != null && availablePurchase.getInAppPurchase().getType() == ConsumablePurchase.class && availablePurchase.getInAppPurchase().getPurchaseFamilies().contains(PurchaseFamily.Ocr))
                .toSortedList((purchase1, purchase2) -> new BigDecimal(purchase1.getPriceAmountMicros()).compareTo(new BigDecimal(purchase2.getPriceAmountMicros())))
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void startOcrPurchase(@NonNull AvailablePurchase availablePurchase) {
        if (availablePurchase.getInAppPurchase() != null) {
            analytics.record(new DefaultDataPointEvent(Events.Ocr.OcrPurchaseClicked).addDataPoint(new DataPoint("sku", availablePurchase.getInAppPurchase())));
            purchaseManager.initiatePurchase(availablePurchase.getInAppPurchase(), PurchaseSource.Ocr);
        } else {
            Logger.error(this, "Unexpected state in which the in app purchase is null");
        }
    }

    @NonNull
    public Observable<Boolean> getOcrIsEnabled() {
        return userPreferenceManager.getObservable(UserPreference.Misc.OcrIsEnabled);
    }

    public void setOcrIsEnabled(boolean ocrIsEnabled) {
        analytics.record(new DefaultDataPointEvent(Events.Ocr.OcrIsEnabledToggled).addDataPoint(new DataPoint("value", ocrIsEnabled)));
        userPreferenceManager.set(UserPreference.Misc.OcrIsEnabled, ocrIsEnabled);
    }

    @NonNull
    public Observable<Boolean> getAllowUsToSaveImagesRemotely() {
        return userPreferenceManager.getObservable(UserPreference.Misc.OcrIncognitoMode)
                .map(incognito -> !incognito);
    }

    public void setAllowUsToSaveImagesRemotely(boolean saveImagesRemotely) {
        analytics.record(new DefaultDataPointEvent(Events.Ocr.OcrIncognitoModeToggled).addDataPoint(new DataPoint("value", !saveImagesRemotely)));
        userPreferenceManager.set(UserPreference.Misc.OcrIncognitoMode, !saveImagesRemotely);
    }

}
