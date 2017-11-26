package co.smartreceipts.android.receipts.editor.di;

import co.smartreceipts.android.receipts.editor.CreateEditReceiptFragment;
import co.smartreceipts.android.receipts.editor.date.ReceiptDateView;
import co.smartreceipts.android.receipts.editor.exchange.CurrencyExchangeRateEditorView;
import co.smartreceipts.android.receipts.editor.pricing.EditableReceiptPricingView;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class ReceiptsCreateEditModule {

    @Binds
    abstract EditableReceiptPricingView provideEditableReceiptPricingView(CreateEditReceiptFragment fragment);

    @Binds
    abstract ReceiptDateView provideReceiptDateView(CreateEditReceiptFragment fragment);

    @Binds
    abstract CurrencyExchangeRateEditorView provideCurrencyExchangeRateEditorView(CreateEditReceiptFragment fragment);

}