package co.smartreceipts.android.di.subcomponents;

import co.smartreceipts.android.di.ReportTooltipModule;
import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.widget.tooltip.report.ReportTooltipFragment;
import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@FragmentScope
@Subcomponent(modules = ReportTooltipModule.class)
public interface ReportTooltipFragmentSubcomponent extends AndroidInjector<ReportTooltipFragment> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<ReportTooltipFragment> {

    }
}
