package co.smartreceipts.android.di;

import co.smartreceipts.android.widget.tooltip.report.ReportTooltipFragment;
import co.smartreceipts.android.widget.tooltip.LegacyTooltipView;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class ReportTooltipModule {
    @Binds
    abstract LegacyTooltipView provideTooltipView(ReportTooltipFragment fragment);
}
