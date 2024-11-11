package com.wops.receiptsgo.receipts.editor.di;

import com.wops.receiptsgo.autocomplete.AutoCompleteView;
import com.wops.receiptsgo.editor.Editor;
import com.wops.receiptsgo.keyboard.decimal.SamsungDecimalInputView;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.receipts.editor.ReceiptCreateEditFragment;
import com.wops.receiptsgo.receipts.editor.date.ReceiptDateView;
import com.wops.receiptsgo.receipts.editor.exchange.CurrencyExchangeRateEditorView;
import com.wops.receiptsgo.receipts.editor.paymentmethods.PaymentMethodsView;
import com.wops.receiptsgo.receipts.editor.pricing.EditableReceiptPricingView;
import com.wops.receiptsgo.receipts.editor.toolbar.ReceiptsEditorToolbarView;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class ReceiptsCreateEditModule {

    @Binds
    abstract Editor<Receipt> providesEditor(ReceiptCreateEditFragment fragment);

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

    @Binds
    abstract ReceiptsEditorToolbarView providesReceiptsEditorToolbarView(ReceiptCreateEditFragment fragment);

    @Binds
    abstract PaymentMethodsView providesPaymentMethodsView(ReceiptCreateEditFragment fragment);
    
}