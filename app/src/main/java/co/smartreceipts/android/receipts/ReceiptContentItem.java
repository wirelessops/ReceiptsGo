package co.smartreceipts.android.receipts;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Receipt;

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
