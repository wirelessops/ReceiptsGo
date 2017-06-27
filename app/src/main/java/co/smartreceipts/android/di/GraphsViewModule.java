package co.smartreceipts.android.di;

import co.smartreceipts.android.graphs.GraphsFragment;
import co.smartreceipts.android.graphs.GraphsView;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class GraphsViewModule {
    @Binds
    abstract GraphsView provideGraphsView(GraphsFragment fragment);
}
