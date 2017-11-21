package co.smartreceipts.android.model.impl.columns.receipts;

import android.content.Context;
import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.AbstractColumnImpl;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.sync.model.SyncState;

/**
 * Provides a column that returns the category code for a particular receipt
 */
public final class ReceiptDateColumn extends AbstractColumnImpl<Receipt> {

    private final Context mContext;
    private final UserPreferenceManager mPreferences;

    public ReceiptDateColumn(int id, @NonNull String name, @NonNull SyncState syncState, @NonNull Context context,
                             @NonNull UserPreferenceManager preferences) {
        super(id, name, syncState);
        mContext = context;
        mPreferences = preferences;
    }

    public ReceiptDateColumn(int id, @NonNull String name, @NonNull SyncState syncState, @NonNull Context context,
                             @NonNull UserPreferenceManager preferences, int customOrderId) {
        super(id, name, syncState, customOrderId);
        mContext = context;
        mPreferences = preferences;
    }

    @Override
    public String getValue(@NonNull Receipt receipt) {
        return receipt.getFormattedDate(mContext, mPreferences.get(UserPreference.General.DateSeparator));
    }
}
