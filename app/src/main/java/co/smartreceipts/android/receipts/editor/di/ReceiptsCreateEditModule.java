package co.smartreceipts.android.receipts.editor.di;

import co.smartreceipts.android.receipts.editor.ReceiptCreateEditFragment;
import co.smartreceipts.android.receipts.editor.currency.ReceiptCurrencyListEditorView;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class ReceiptsCreateEditModule {

    @Binds
    abstract ReceiptCurrencyListEditorView provideReceiptCurrencyListEditorView(ReceiptCreateEditFragment fragment);

}