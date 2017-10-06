package co.smartreceipts.android.receipts.di;

import co.smartreceipts.android.receipts.ReceiptsListFragment;
import co.smartreceipts.android.receipts.creator.ReceiptCreateActionView;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class ReceiptsListModule {

    @Binds
    abstract ReceiptCreateActionView provideLoginView(ReceiptsListFragment fragment);

}