package co.smartreceipts.android.receipts;

import co.smartreceipts.android.R;

public interface ReceiptsListItem {

    int TYPE_RECEIPT = R.layout.item_receipt_card;
    int TYPE_HEADER = R.layout.item_receipt_header;

    int getListItemType();
}
