package com.wops.receiptsgo.receipts;

import com.wops.receiptsgo.R;

public interface ReceiptsListItem {

    int TYPE_RECEIPT = R.layout.item_receipt_card;
    int TYPE_HEADER = R.layout.item_round_header;

    int getListItemType();
}
