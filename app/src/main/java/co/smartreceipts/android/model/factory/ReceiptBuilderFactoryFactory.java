package co.smartreceipts.android.model.factory;

import androidx.annotation.NonNull;

import co.smartreceipts.android.model.Receipt;

/**
 * Stupid, ugly java factory-factory pattern... But using to allow easier UT mocking
 */
public class ReceiptBuilderFactoryFactory implements BuilderFactory1<Receipt, ReceiptBuilderFactory> {

    @NonNull
    @Override
    public ReceiptBuilderFactory build(@NonNull Receipt receipt) {
        return new ReceiptBuilderFactory(receipt);
    }
}
