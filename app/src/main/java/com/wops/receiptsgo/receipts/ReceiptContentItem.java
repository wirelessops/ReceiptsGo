package com.wops.receiptsgo.receipts;

import androidx.annotation.NonNull;

import com.wops.receiptsgo.model.Receipt;

public class ReceiptContentItem implements ReceiptsListItem {

    private final Receipt receipt;

    public ReceiptContentItem(@NonNull Receipt receipt) {
        this.receipt = receipt;
    }

    @Override
    public int getListItemType() {
        return ReceiptsListItem.TYPE_RECEIPT;
    }

    public Receipt getReceipt() {
        return receipt;
    }
}
