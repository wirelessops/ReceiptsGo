package com.wops.receiptsgo.widget.tooltip.report.generate.data;

public interface GenerateInfoTooltipStorage {

    void tooltipWasDismissed();

    boolean wasTooltipDismissed();

    void reportWasGenerated();

    boolean wasReportEverGenerated();

}
