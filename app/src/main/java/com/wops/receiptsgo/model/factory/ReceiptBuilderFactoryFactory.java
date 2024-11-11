package com.wops.receiptsgo.model.factory;

import androidx.annotation.NonNull;

import com.wops.receiptsgo.model.Receipt;

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
