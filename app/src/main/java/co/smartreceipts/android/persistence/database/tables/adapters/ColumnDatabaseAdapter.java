package co.smartreceipts.android.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.ColumnBuilderFactory;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.persistence.database.tables.AbstractColumnTable;
import co.smartreceipts.android.persistence.database.tables.keys.PrimaryKey;
import co.smartreceipts.android.sync.model.SyncState;

/**
 * Implements the {@link DatabaseAdapter} contract for the {@link co.smartreceipts.android.persistence.database.tables.AbstractColumnTable}
 * for CSVs and PDFs
 */
public final class ColumnDatabaseAdapter implements DatabaseAdapter<Column<Receipt>, PrimaryKey<Column<Receipt>, Integer>> {

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
        final int typeIndex = cursor.getColumnIndex(AbstractColumnTable.COLUMN_TYPE);
        final int customOrderIndex = cursor.getColumnIndex(AbstractColumnTable.COLUMN_CUSTOM_ORDER_ID);

        final int id = cursor.getInt(idIndex);
        final int type = cursor.getInt(typeIndex);
        final SyncState syncState = mSyncStateAdapter.read(cursor);
        final long customOrderId = cursor.getLong(customOrderIndex);
        return new ColumnBuilderFactory<>(mReceiptColumnDefinitions)
                .setColumnId(id)
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

        if (databaseOperationMetadata.getOperationFamilyType() == OperationFamilyType.Sync) {
            values.putAll(mSyncStateAdapter.write(column.getSyncState()));
        } else {
            values.putAll(mSyncStateAdapter.writeUnsynced(column.getSyncState()));
        }
        return values;
    }

    @NonNull
    @Override
    public Column<Receipt> build(@NonNull Column<Receipt> column, @NonNull PrimaryKey<Column<Receipt>,
            Integer> primaryKey, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        return new ColumnBuilderFactory<>(mReceiptColumnDefinitions)
                .setColumnId(primaryKey.getPrimaryKeyValue(column))
                .setColumnType(column.getType())
                .setSyncState(mSyncStateAdapter.get(column.getSyncState(), databaseOperationMetadata))
                .setCustomOrderId(column.getCustomOrderId())
                .build();
    }

}
