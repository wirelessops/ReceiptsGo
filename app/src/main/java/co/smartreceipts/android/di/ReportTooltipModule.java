package co.smartreceipts.android.di;

import co.smartreceipts.android.widget.tooltip.report.ReportTooltipFragment;
import co.smartreceipts.android.widget.tooltip.TooltipView;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class ReportTooltipModule {
    @Binds
    abstract TooltipView provideTooltipView(ReportTooltipFragment fragment);
}
