package com.wops.receiptsgo.di;

import com.wops.receiptsgo.widget.tooltip.report.ReportTooltipFragment;
import com.wops.receiptsgo.widget.tooltip.LegacyTooltipView;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class ReportTooltipModule {
    @Binds
    abstract LegacyTooltipView provideTooltipView(ReportTooltipFragment fragment);
}
