package co.smartreceipts.android.receipts.di;

import co.smartreceipts.android.ocr.widget.alert.OcrStatusAlerterView;
import co.smartreceipts.android.receipts.ReceiptsListFragment;
import co.smartreceipts.android.receipts.ReceiptsListView;
import co.smartreceipts.android.receipts.creator.ReceiptCreateActionView;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class ReceiptsListModule {

    @Binds
    abstract ReceiptsListView provideReceiptsListView(ReceiptsListFragment fragment);

    @Binds
    abstract ReceiptCreateActionView provideReceiptCreateActionView(ReceiptsListFragment fragment);

    @Binds
    abstract OcrStatusAlerterView provideOcrStatusAlerterView(ReceiptsListFragment fragment);

}