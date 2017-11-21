package co.smartreceipts.android.model.impl.columns.receipts;

import android.content.Context;
import android.support.annotation.NonNull;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.sync.model.SyncState;

/**
 * Provides a column that returns the category code for a particular receipt
 */
public final class ReceiptIsReimbursableColumn extends AbstractColumnImpl<Receipt> {

    private final Context mContext;

    public ReceiptIsReimbursableColumn(int id, @NonNull String name, @NonNull SyncState syncState,
                                       @NonNull Context context, int customOrderId) {
        super(id, name, syncState, customOrderId);
        mContext = context;
    }

    @Override
    public String getValue(@NonNull Receipt receipt) {
        return (receipt.isReimbursable()) ? mContext.getString(R.string.yes) : mContext.getString(R.string.no);
    }
}
