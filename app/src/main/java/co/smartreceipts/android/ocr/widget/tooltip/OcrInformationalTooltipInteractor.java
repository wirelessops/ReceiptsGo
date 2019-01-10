package co.smartreceipts.android.ocr.widget.tooltip;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.DataPoint;
import co.smartreceipts.android.analytics.events.DefaultDataPointEvent;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.config.ConfigurationManager;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.identity.IdentityManager;
import co.smartreceipts.android.ocr.purchases.OcrPurchaseTracker;
import co.smartreceipts.android.utils.ConfigurableResourceFeature;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

@ApplicationScope
public class OcrInformationalTooltipInteractor {

    @VisibleForTesting
    static final int SCANS_LEFT_TO_INFORM = 5;

    private final Analytics analytics;
    private final OcrInformationalTooltipStateTracker stateTracker;
    private final OcrPurchaseTracker ocrPurchaseTracker;
    private final IdentityManager identityManager;
    private final ConfigurationManager configurationManager;
    private final Scheduler scheduler;

    private int lastRemainingScans = 0;

    @Inject
    public OcrInformationalTooltipInteractor(@NonNull Analytics analytics,
                                             @NonNull OcrInformationalTooltipStateTracker stateTracker,
                                             @NonNull OcrPurchaseTracker ocrPurchaseTracker,
                                             @NonNull IdentityManager identityManager,
                                             @NonNull ConfigurationManager configurationManager) {
        this(analytics, stateTracker, ocrPurchaseTracker, identityManager, configurationManager, Schedulers.computation());
    }

    @VisibleForTesting
    OcrInformationalTooltipInteractor(@NonNull Analytics analytics,
                                      @NonNull OcrInformationalTooltipStateTracker stateTracker,
                                      @NonNull OcrPurchaseTracker ocrPurchaseTracker,
                                      @NonNull IdentityManager identityManager,
                                      @NonNull ConfigurationManager configurationManager,
                                      @NonNull Scheduler scheduler) {
        this.analytics = Preconditions.checkNotNull(analytics);
        this.stateTracker = Preconditions.checkNotNull(stateTracker);
        this.ocrPurchaseTracker = Preconditions.checkNotNull(ocrPurchaseTracker);
        this.identityManager = Preconditions.checkNotNull(identityManager);
        this.configurationManager = Preconditions.checkNotNull(configurationManager);
        this.scheduler = Preconditions.checkNotNull(scheduler);
    }

    /**
     * Initializes the tooltip tracking logic, so it can begin monitoring the remaining count of OCR scans
     */
    @SuppressLint("CheckResult")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void initialize() {
        ocrPurchaseTracker.getRemainingScansStream()
                .subscribeOn(scheduler)
                .subscribe(remainingScans -> {
                    if (remainingScans == SCANS_LEFT_TO_INFORM) {
                        Logger.info(OcrInformationalTooltipInteractor.this, "{} scans remaining. Re-enabling our few scans remaining tracker.", remainingScans);
                        stateTracker.setShouldShowOcrInfo(true);
                    }
                    if (lastRemainingScans == 1 && remainingScans == 0) {
                        Logger.info(OcrInformationalTooltipInteractor.this, "No scans. Re-enabling our few scans remaining tracker.");
                        stateTracker.setShouldShowOcrInfo(true);
                    }
                    // Make this the last operation for this stream
                    lastRemainingScans = remainingScans;
                });
    }

    /**
     * @return an {@link Observable}, which will emit a {@link OcrTooltipMessageType} or empty if
     * it is not appropriate to display one
     */
    @NonNull
    public Observable<OcrTooltipMessageType> getShowOcrTooltip() {
        // configurationManager.isEnabled(ConfigurableResourceFeature.Ocr)
        return stateTracker.shouldShowOcrInfo()
                .subscribeOn(Schedulers.computation())
                .flatMapObservable(shouldShowTooltip -> {
                    if (shouldShowTooltip) {
                        if (!configurationManager.isEnabled(ConfigurableResourceFeature.Ocr)) {
                            Logger.info(OcrInformationalTooltipInteractor.this, "OCR is not configured. Disabling the tooltip");
                            return Observable.empty();
                        }
                        if (ocrPurchaseTracker.getRemainingScans() > 0 && ocrPurchaseTracker.getRemainingScans() <= SCANS_LEFT_TO_INFORM) {
                            return Observable.just(OcrTooltipMessageType.LimitedScansRemaining);
                        }
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
                    Logger.info(OcrInformationalTooltipInteractor.this, "Displaying OCR Tooltip: {}", ocrTooltipMessageType);
                    analytics.record(new DefaultDataPointEvent(Events.Ocr.OcrInfoTooltipShown).addDataPoint(new DataPoint("type", ocrTooltipMessageType)));
                });
    }

    public void markTooltipInteraction() {
        stateTracker.setShouldShowOcrInfo(false);
        analytics.record(Events.Ocr.OcrInfoTooltipDismiss);
    }

}
