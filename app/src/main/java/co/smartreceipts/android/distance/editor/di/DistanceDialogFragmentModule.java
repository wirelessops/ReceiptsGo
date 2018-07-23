package co.smartreceipts.android.distance.editor.di;

import co.smartreceipts.android.autocomplete.AutoCompleteView;
import co.smartreceipts.android.distance.editor.DistanceDialogFragment;
import co.smartreceipts.android.model.Distance;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class DistanceDialogFragmentModule {

    @Binds
    abstract AutoCompleteView<Distance> providesReceiptAutoCompleteView(DistanceDialogFragment fragment);

}
