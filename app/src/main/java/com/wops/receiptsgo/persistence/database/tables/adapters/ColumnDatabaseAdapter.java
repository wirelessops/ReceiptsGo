package com.wops.receiptsgo.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.UUID;

import com.wops.receiptsgo.model.Column;
import com.wops.receiptsgo.model.ColumnDefinitions;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.model.factory.ColumnBuilderFactory;
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata;
import com.wops.receiptsgo.persistence.database.operations.OperationFamilyType;
import com.wops.receiptsgo.persistence.database.tables.AbstractColumnTable;
import com.wops.core.sync.model.SyncState;

/**
 * Implements the {@link DatabaseAdapter} contract for the {@link com.wops.receiptsgo.persistence.database.tables.AbstractColumnTable}
 * for CSVs and PDFs
 */
public final class ColumnDatabaseAdapter implements DatabaseAdapter<Column<Receipt>> {

    private final ColumnDefinitions<Receipt> mReceiptColumnDefinitions;
    private final SyncStateAdapter mSyncStateAdapter;

    public ColumnDatabaseAdapter(@NonNull ColumnDefinitions<Receipt> receiptColumnDefinitions) {
        this(receiptColumnDefinitions, new SyncStateAdapter());
    }

    public ColumnDatabaseAdapter(@NonNull ColumnDefinitions<Receipt> receiptColumnDefinitions, @NonNull SyncStateAdapter syncStateAdapter) {
        mReceiptColumnDefinitions = Preconditions.checkNotNull(receiptColumnDefinitions);
        mSyncStateAdapter = Preconditions.checkNotNull(syncStateAdapter);
    }

    @NonNull
    @Override
    public Column<Receipt> read(@NonNull Cursor cursor) {
        final int idIndex = cursor.getColumnIndex(AbstractColumnTable.COLUMN_ID);
        final int uuidIndex = cursor.getColumnIndex(AbstractColumnTable.COLUMN_UUID);
        final int typeIndex = cursor.getColumnIndex(AbstractColumnTable.COLUMN_TYPE);
        final int customOrderIndex = cursor.getColumnIndex(AbstractColumnTable.COLUMN_CUSTOM_ORDER_ID);

        final int id = cursor.getInt(idIndex);
        final UUID uuid = UUID.fromString(cursor.getString(uuidIndex));
        final int type = cursor.getInt(typeIndex);
        final SyncState syncState = mSyncStateAdapter.read(cursor);
        final long customOrderId = cursor.getLong(customOrderIndex);
        return new ColumnBuilderFactory<>(mReceiptColumnDefinitions)
                .setColumnId(id)
                .setColumnUuid(uuid)
                .setColumnType(type)
                .setSyncState(syncState)
                .setCustomOrderId(customOrderId)
                .build();
    }

    @NonNull
    @Override
    public ContentValues write(@NonNull Column<Receipt> column, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        final ContentValues values = new ContentValues();
        values.put(AbstractColumnTable.COLUMN_TYPE, column.getType());
        values.put(AbstractColumnTable.COLUMN_CUSTOM_ORDER_ID, column.getCustomOrderId());
        values.put(AbstractColumnTable.COLUMN_UUID, column.getUuid().toString());

        if (databaseOperationMetadata.getOperationFamilyType() == OperationFamilyType.Sync) {
            values.putAll(mSyncStateAdapter.write(column.getSyncState()));
        } else {
            values.putAll(mSyncStateAdapter.writeUnsynced(column.getSyncState()));
        }
        return values;
    }

    @NonNull
    @Override
    public Column<Receipt> build(@NonNull Column<Receipt> column, int primaryKey, @NonNull UUID uuid, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        return new ColumnBuilderFactory<>(mReceiptColumnDefinitions)
                .setColumnId(primaryKey)
                .setColumnUuid(uuid)
                .setColumnType(column.getType())
                .setSyncState(mSyncStateAdapter.get(column.getSyncState(), databaseOperationMetadata))
                .setCustomOrderId(column.getCustomOrderId())
                .build();
    }

}
