package com.wops.receiptsgo.trips.editor.di;

import com.wops.receiptsgo.autocomplete.AutoCompleteView;
import com.wops.receiptsgo.editor.Editor;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.tooltip.TooltipView;
import com.wops.receiptsgo.trips.editor.TripCreateEditFragment;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class TripCreateEditFragmentModule {

    @Binds
    abstract Editor<Trip> providesEditor(TripCreateEditFragment fragment);

    @Binds
    abstract AutoCompleteView<Trip> providesReceiptAutoCompleteView(TripCreateEditFragment fragment);

    @Binds
    abstract TooltipView providesTooltipView(TripCreateEditFragment fragment);

}
