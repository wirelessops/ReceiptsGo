package co.smartreceipts.android.trips.editor.di;

import co.smartreceipts.android.autocomplete.AutoCompleteView;
import co.smartreceipts.android.editor.Editor;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.tooltip.StaticTooltipView;
import co.smartreceipts.android.trips.editor.TripCreateEditFragment;
import co.smartreceipts.android.widget.tooltip.TooltipView;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class TripCreateEditFragmentModule {

    @Binds
    abstract Editor<Trip> providesEditor(TripCreateEditFragment fragment);

    @Binds
    abstract AutoCompleteView<Trip> providesReceiptAutoCompleteView(TripCreateEditFragment fragment);

    @Binds
    abstract StaticTooltipView providesStaticTooltipView(TripCreateEditFragment fragment);

}
