package com.wops.receiptsgo.widget.tooltip;

import androidx.annotation.NonNull;

import com.wops.receiptsgo.widget.tooltip.report.ReportTooltipUiIndicator;
import io.reactivex.Observable;

/**
 * @deprecated Please use the {@link com.wops.receiptsgo.tooltip.TooltipView} instead
 */
@Deprecated
public interface LegacyTooltipView {

    void present(ReportTooltipUiIndicator uiIndicator);

    @NonNull
    Observable<ReportTooltipUiIndicator> getCloseButtonClicks();

    @NonNull
    Observable<ReportTooltipUiIndicator> getTooltipsClicks();

}
