package co.smartreceipts.android.widget.tooltip;

import android.support.annotation.NonNull;

import co.smartreceipts.android.widget.tooltip.report.ReportTooltipUiIndicator;
import io.reactivex.Observable;

public interface TooltipView {

    void present(ReportTooltipUiIndicator uiIndicator);

    @NonNull
    Observable<ReportTooltipUiIndicator> getCloseButtonClicks();

    @NonNull
    Observable<ReportTooltipUiIndicator> getTooltipsClicks();

}
