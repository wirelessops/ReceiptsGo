package co.smartreceipts.android.model.impl.columns.receipts;

import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.sync.model.SyncState;

/**
 * Provides a column that returns the category code for a particular receipt
 */
public final class ReceiptCommentColumn extends AbstractColumnImpl<Receipt> {

    public ReceiptCommentColumn(int id, @NonNull String name, @NonNull SyncState syncState, int customOrderId) {
        super(id, name, syncState, customOrderId);
    }

    @Override
    public String getValue(@NonNull Receipt receipt) {
        return receipt.getComment();
    }
}
