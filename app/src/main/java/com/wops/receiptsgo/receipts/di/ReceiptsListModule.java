package com.wops.receiptsgo.receipts.di;

import com.wops.receiptsgo.ocr.widget.alert.OcrStatusAlerterView;
import com.wops.receiptsgo.receipts.ReceiptsListFragment;
import com.wops.receiptsgo.receipts.ReceiptsListView;
import com.wops.receiptsgo.receipts.creator.ReceiptCreateActionView;
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