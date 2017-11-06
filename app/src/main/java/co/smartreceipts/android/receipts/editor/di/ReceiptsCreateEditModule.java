package co.smartreceipts.android.receipts.editor.di;

import co.smartreceipts.android.currency.widget.CurrencyListEditorView;
import co.smartreceipts.android.receipts.editor.CreateEditFragmentListEditor;
import dagger.Binds;
import dagger.Module;

@Module
public abstract class ReceiptsCreateEditModule {

    @Binds
    abstract CurrencyListEditorView provideCurrencyListEditorView(CreateEditFragmentListEditor fragment);

}