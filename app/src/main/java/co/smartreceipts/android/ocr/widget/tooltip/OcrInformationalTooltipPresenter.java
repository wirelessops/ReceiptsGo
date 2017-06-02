package co.smartreceipts.android.ocr.widget.tooltip;

import android.support.annotation.NonNull;
import android.view.View;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.R;
import co.smartreceipts.android.ocr.purchases.OcrPurchaseTracker;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.widget.tooltip.Tooltip;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;

public class OcrInformationalTooltipPresenter{

    private final OcrInformationalTooltipInteractor interactor;
    private final Tooltip tooltip;
    private final OcrPurchaseTracker ocrPurchaseTracker;

    private CompositeDisposable compositeDisposable;

    public OcrInformationalTooltipPresenter(@NonNull OcrInformationalTooltipInteractor interactor, @NonNull Tooltip tooltip,
                                            @NonNull OcrPurchaseTracker ocrPurchaseTracker) {
        this.interactor = Preconditions.checkNotNull(interactor);
        this.tooltip = Preconditions.checkNotNull(tooltip);
        this.ocrPurchaseTracker = Preconditions.checkNotNull(ocrPurchaseTracker);

        this.tooltip.setTooltipClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                OcrInformationalTooltipPresenter.this.interactor.showOcrConfiguration();
                OcrInformationalTooltipPresenter.this.tooltip.setVisibility(GONE);
            }
        });
        this.tooltip.showCloseIcon(v -> {
            OcrInformationalTooltipPresenter.this.interactor.dismissTooltip();
            OcrInformationalTooltipPresenter.this.tooltip.setVisibility(GONE);
        });
        this.tooltip.setVisibility(GONE);
    }

    public void onResume() {
        compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(interactor.getShowOcrTooltip()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(ocrTooltipMessageType -> {
                    Logger.info(OcrInformationalTooltipPresenter.this, "Showing OCR Tooltip for {}", ocrTooltipMessageType);
                    if (ocrTooltipMessageType == OcrTooltipMessageType.NotConfigured) {
                        tooltip.setInfoMessage(R.string.ocr_informational_tooltip_configure_text);
                    } else if (ocrTooltipMessageType == OcrTooltipMessageType.LimitedScansRemaining || ocrTooltipMessageType == OcrTooltipMessageType.NoScansRemaining) {
                        final int remainingScans = ocrPurchaseTracker.getRemainingScans();
                        tooltip.setInfoMessage(tooltip.getContext().getResources().getQuantityString(R.plurals.ocr_informational_tooltip_limited_scans_text, remainingScans, remainingScans));
                    } else {
                        throw new IllegalArgumentException("Unknown message type" + ocrTooltipMessageType);
                    }
                    tooltip.setVisibility(VISIBLE);
                }));
    }

    public void onPause() {
        compositeDisposable.dispose();
        compositeDisposable = null;
    }
}
