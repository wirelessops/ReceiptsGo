package com.wops.receiptsgo.di;

import com.wops.receiptsgo.graphs.GraphsFragment;
import com.wops.receiptsgo.graphs.GraphsView;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class GraphsViewModule {
    @Binds
    abstract GraphsView provideGraphsView(GraphsFragment fragment);
}
