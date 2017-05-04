package co.smartreceipts.android.ocr.widget.tooltip;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.DataPoint;
import co.smartreceipts.android.analytics.events.DefaultDataPointEvent;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.ocr.purchases.OcrPurchaseTracker;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;


@FragmentScope
public class OcrInformationalTooltipInteractor {

    private static final int SCANS_LEFT_TO_INFORM = 5;

    private final NavigationHandler navigationHandler;
    private final Analytics analytics;
    private final OcrInformationalTooltipStateTracker stateTracker;
    private final OcrPurchaseTracker ocrPurchaseTracker;
    private final IdentityManager identityManager;

    @Inject
    public OcrInformationalTooltipInteractor(Context context, NavigationHandler navigationHandler, Analytics analytics,
                                             OcrPurchaseTracker ocrPurchaseTracker, IdentityManager identityManager) {
        this(navigationHandler, analytics, new OcrInformationalTooltipStateTracker(context), ocrPurchaseTracker, identityManager);
    }

    @VisibleForTesting
    OcrInformationalTooltipInteractor(@NonNull NavigationHandler navigationHandler, @NonNull Analytics analytics,
                                      @NonNull OcrInformationalTooltipStateTracker stateTracker, @NonNull OcrPurchaseTracker ocrPurchaseTracker,
                                      @NonNull IdentityManager identityManager) {
        this.navigationHandler = Preconditions.checkNotNull(navigationHandler);
        this.analytics = Preconditions.checkNotNull(analytics);
        this.stateTracker = Preconditions.checkNotNull(stateTracker);
        this.ocrPurchaseTracker = Preconditions.checkNotNull(ocrPurchaseTracker);
        this.identityManager = Preconditions.checkNotNull(identityManager);
    }

    public Observable<OcrTooltipMessageType> getShowOcrTooltip() {
        return stateTracker.shouldShowOcrInfo()
                .subscribeOn(Schedulers.computation())
                .flatMapObservable(shouldShowTooltip -> {
                    if (ocrPurchaseTracker.getRemainingScans() > 0 && ocrPurchaseTracker.getRemainingScans() <= SCANS_LEFT_TO_INFORM) {
                        return Observable.just(OcrTooltipMessageType.LimitedScansRemaining);
                    } else if (shouldShowTooltip) {
                        if (identityManager.isLoggedIn()) {
                            if (ocrPurchaseTracker.hasAvailableScans()) {
                                return Observable.empty();
                            } else {
                                return Observable.just(OcrTooltipMessageType.NoScansRemaining);
                            }
                        } else {
                            return Observable.just(OcrTooltipMessageType.NotConfigured);
                        }
                    } else {
                        return Observable.empty();
                    }
                })
                .doOnNext(ocrTooltipMessageType -> {
                        analytics.record(new DefaultDataPointEvent(Events.Ocr.OcrInfoTooltipShown).addDataPoint(new DataPoint("type", ocrTooltipMessageType)));
                        if (ocrTooltipMessageType == OcrTooltipMessageType.LimitedScansRemaining) {
                            stateTracker.setShouldShowOcrInfo(true);
                        }
                    });
    }

    public void dismissTooltip() {
        Logger.info(this, "Dismissing OCR Tooltip");
        stateTracker.setShouldShowOcrInfo(false);
        analytics.record(Events.Ocr.OcrInfoTooltipDismiss);
    }

    public void showOcrConfiguration() {
        Logger.info(this, "Displaying OCR Configuration Fragment");
        navigationHandler.navigateToOcrConfigurationFragment();
        analytics.record(Events.Ocr.OcrInfoTooltipOpen);
    }
}
