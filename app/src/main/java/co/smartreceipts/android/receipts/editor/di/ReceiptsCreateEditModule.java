package co.smartreceipts.android.receipts.editor.di;

import co.smartreceipts.android.autocomplete.AutoCompleteView;
import co.smartreceipts.android.keyboard.decimal.SamsungDecimalInputView;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.receipts.editor.ReceiptCreateEditFragment;
import co.smartreceipts.android.receipts.editor.date.ReceiptDateView;
import co.smartreceipts.android.receipts.editor.exchange.CurrencyExchangeRateEditorView;
import co.smartreceipts.android.receipts.editor.pricing.EditableReceiptPricingView;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class ReceiptsCreateEditModule {

    @Binds
    abstract EditableReceiptPricingView provideEditableReceiptPricingView(ReceiptCreateEditFragment fragment);

    @Binds
    abstract ReceiptDateView provideReceiptDateView(ReceiptCreateEditFragment fragment);

    @Binds
    abstract CurrencyExchangeRateEditorView provideCurrencyExchangeRateEditorView(ReceiptCreateEditFragment fragment);

    @Binds
    abstract SamsungDecimalInputView provideSamsungDecimalInputView(ReceiptCreateEditFragment fragment);

    @Binds
    abstract AutoCompleteView<Receipt> providesReceiptAutoCompleteView(ReceiptCreateEditFragment fragment);
    
}