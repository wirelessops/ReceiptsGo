package co.smartreceipts.android.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.UUID;

import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.persistence.database.tables.PaymentMethodsTable;
import co.smartreceipts.core.sync.model.SyncState;

/**
 * Implements the {@link DatabaseAdapter} contract for the {@link co.smartreceipts.android.persistence.database.tables.PaymentMethodsTable}
 */
public final class PaymentMethodDatabaseAdapter implements DatabaseAdapter<PaymentMethod> {

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
        final int uuidIndex = cursor.getColumnIndex(PaymentMethodsTable.COLUMN_UUID);
        final int methodIndex = cursor.getColumnIndex(PaymentMethodsTable.COLUMN_METHOD);
        final int customOrderIdIndex = cursor.getColumnIndex(PaymentMethodsTable.COLUMN_CUSTOM_ORDER_ID);

        final int id = cursor.getInt(idIndex);
        final UUID uuid = UUID.fromString(cursor.getString(uuidIndex));
        final String method = cursor.getString(methodIndex);
        final SyncState syncState = mSyncStateAdapter.read(cursor);
        final long customOrderId = cursor.getLong(customOrderIdIndex);
        return new PaymentMethodBuilderFactory()
                .setId(id)
                .setUuid(uuid)
                .setMethod(method)
                .setSyncState(syncState)
                .setCustomOrderId(customOrderId)
                .build();
    }

    @NonNull
    @Override
    public ContentValues write(@NonNull PaymentMethod paymentMethod, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        final ContentValues values = new ContentValues();
        values.put(PaymentMethodsTable.COLUMN_METHOD, paymentMethod.getMethod());
        values.put(PaymentMethodsTable.COLUMN_CUSTOM_ORDER_ID, paymentMethod.getCustomOrderId());
        values.put(PaymentMethodsTable.COLUMN_UUID, paymentMethod.getUuid().toString());
        if (databaseOperationMetadata.getOperationFamilyType() == OperationFamilyType.Sync) {
            values.putAll(mSyncStateAdapter.write(paymentMethod.getSyncState()));
        } else {
            values.putAll(mSyncStateAdapter.writeUnsynced(paymentMethod.getSyncState()));
        }
        return values;
    }

    @NonNull
    @Override
    public PaymentMethod build(@NonNull PaymentMethod paymentMethod, int primaryKey,
                               @NonNull UUID uuid, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        return new PaymentMethodBuilderFactory()
                .setId(primaryKey)
                .setUuid(uuid)
                .setMethod(paymentMethod.getMethod())
                .setSyncState(mSyncStateAdapter.get(paymentMethod.getSyncState(), databaseOperationMetadata))
                .setCustomOrderId(paymentMethod.getCustomOrderId())
                .build();
    }
}
