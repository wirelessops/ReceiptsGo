package co.smartreceipts.android.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.persistence.database.tables.PaymentMethodsTable;
import co.smartreceipts.android.persistence.database.tables.keys.PrimaryKey;
import co.smartreceipts.android.sync.model.SyncState;

/**
 * Implements the {@link DatabaseAdapter} contract for the {@link co.smartreceipts.android.persistence.database.tables.PaymentMethodsTable}
 */
public final class PaymentMethodDatabaseAdapter implements DatabaseAdapter<PaymentMethod, PrimaryKey<PaymentMethod, Integer>> {

    private final SyncStateAdapter mSyncStateAdapter;

    public PaymentMethodDatabaseAdapter() {
        this(new SyncStateAdapter());
    }

    public PaymentMethodDatabaseAdapter(@NonNull SyncStateAdapter syncStateAdapter) {
        mSyncStateAdapter = Preconditions.checkNotNull(syncStateAdapter);
    }

    @NonNull
    @Override
    public PaymentMethod read(@NonNull Cursor cursor) {
        final int idIndex = cursor.getColumnIndex(PaymentMethodsTable.COLUMN_ID);
        final int methodIndex = cursor.getColumnIndex(PaymentMethodsTable.COLUMN_METHOD);
        final int customOrderIdIndex = cursor.getColumnIndex(PaymentMethodsTable.COLUMN_CUSTOM_ORDER_ID);

        final int id = cursor.getInt(idIndex);
        final String method = cursor.getString(methodIndex);
        final SyncState syncState = mSyncStateAdapter.read(cursor);
        final int customOrderId = cursor.getInt(customOrderIdIndex);
        return new PaymentMethodBuilderFactory().setId(id).setMethod(method).setSyncState(syncState).setCustomOrderId(customOrderId).build();
    }

    @NonNull
    @Override
    public ContentValues write(@NonNull PaymentMethod paymentMethod, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        final ContentValues values = new ContentValues();
        values.put(PaymentMethodsTable.COLUMN_METHOD, paymentMethod.getMethod());
        values.put(PaymentMethodsTable.COLUMN_CUSTOM_ORDER_ID, paymentMethod.getCustomOrderId());
        if (databaseOperationMetadata.getOperationFamilyType() == OperationFamilyType.Sync) {
            values.putAll(mSyncStateAdapter.write(paymentMethod.getSyncState()));
        } else {
            values.putAll(mSyncStateAdapter.writeUnsynced(paymentMethod.getSyncState()));
        }
        return values;
    }

    @NonNull
    @Override
    public PaymentMethod build(@NonNull PaymentMethod paymentMethod, @NonNull PrimaryKey<PaymentMethod, Integer> primaryKey, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        return new PaymentMethodBuilderFactory()
                .setId(primaryKey.getPrimaryKeyValue(paymentMethod))
                .setMethod(paymentMethod.getMethod())
                .setSyncState(mSyncStateAdapter.get(paymentMethod.getSyncState(), databaseOperationMetadata))
                .setCustomOrderId(paymentMethod.getCustomOrderId())
                .build();
    }
}
