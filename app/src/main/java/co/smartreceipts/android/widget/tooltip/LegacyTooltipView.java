package co.smartreceipts.android.widget.tooltip;

import android.support.annotation.NonNull;

import co.smartreceipts.android.widget.tooltip.report.ReportTooltipUiIndicator;
import io.reactivex.Observable;

/**
 * @deprecated Please use the {@link co.smartreceipts.android.tooltip.TooltipView} instead
 */
@Deprecated
public interface LegacyTooltipView {

    void present(ReportTooltipUiIndicator uiIndicator);

    @NonNull
    Observable<ReportTooltipUiIndicator> getCloseButtonClicks();

    @NonNull
    Observable<ReportTooltipUiIndicator> getTooltipsClicks();

}
