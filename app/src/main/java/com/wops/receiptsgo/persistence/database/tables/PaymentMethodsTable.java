package com.wops.receiptsgo.persistence.database.tables;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.NonNull;

import com.wops.receiptsgo.model.PaymentMethod;
import com.wops.receiptsgo.persistence.database.defaults.TableDefaultsCustomizer;
import com.wops.receiptsgo.persistence.database.tables.adapters.PaymentMethodDatabaseAdapter;
import com.wops.receiptsgo.persistence.database.tables.ordering.OrderByColumn;
import com.wops.receiptsgo.persistence.database.tables.ordering.OrderByDatabaseDefault;
import com.wops.receiptsgo.persistence.database.tables.ordering.OrderByOrderingPreference;
import com.wops.receiptsgo.persistence.database.tables.ordering.OrderingPreferencesManager;
import com.wops.analytics.log.Logger;

/**
 * Stores all database operations related to the {@link PaymentMethod} model object
 */
public final class PaymentMethodsTable extends AbstractSqlTable<PaymentMethod> {

    // SQL Definitions:
    public static final String TABLE_NAME = "paymentmethods";

    public static final String COLUMN_METHOD = "method";
    public static final String COLUMN_REIMBURSABLE = "expenseable";

    public PaymentMethodsTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper,
                               @NonNull OrderingPreferencesManager orderingPreferencesManager) {

        super(sqLiteOpenHelper, TABLE_NAME, new PaymentMethodDatabaseAdapter(), new OrderByOrderingPreference(orderingPreferencesManager,
                PaymentMethodsTable.class, new OrderByColumn(COLUMN_CUSTOM_ORDER_ID, false), new OrderByDatabaseDefault()));
    }

    @Override
    public synchronized void onCreate(@NonNull SQLiteDatabase db, @NonNull TableDefaultsCustomizer customizer) {
        super.onCreate(db, customizer);
        final String sql = "CREATE TABLE " + getTableName() + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_METHOD + " TEXT, "
                + COLUMN_REIMBURSABLE + " BOOLEAN DEFAULT 0, "
                + AbstractSqlTable.COLUMN_DRIVE_SYNC_ID + " TEXT, "
                + AbstractSqlTable.COLUMN_DRIVE_IS_SYNCED + " BOOLEAN DEFAULT 0, "
                + AbstractSqlTable.COLUMN_DRIVE_MARKED_FOR_DELETION + " BOOLEAN DEFAULT 0, "
                + AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME + " DATE, "
                + AbstractSqlTable.COLUMN_CUSTOM_ORDER_ID + " INTEGER DEFAULT 0, "
                + AbstractSqlTable.COLUMN_UUID + " TEXT "
                + ");";

        Logger.debug(this, sql);
        db.execSQL(sql);

        customizer.insertPaymentMethodDefaults(this);
    }

    @Override
    public synchronized void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion, @NonNull TableDefaultsCustomizer customizer) {
        super.onUpgrade(db, oldVersion, newVersion, customizer);
        if (oldVersion <= 11) {
            final String sql = "CREATE TABLE " + getTableName() + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_METHOD + " TEXT"
                    + ");";

            Logger.debug(this, sql);
            db.execSQL(sql);
            customizer.insertPaymentMethodDefaults(this);
        }

        if (oldVersion <= 14) { // v14 => v15. adding sync info
            onUpgradeToAddSyncInformation(db, oldVersion, newVersion);
        }

        if (oldVersion <= 15) { // v15 => v16. adding custom_order_id column
            final String addCustomOrderColumn = String.format("ALTER TABLE %s ADD COLUMN %s INTEGER DEFAULT 0",
                    getTableName(), AbstractColumnTable.COLUMN_CUSTOM_ORDER_ID);
            Logger.debug(this, addCustomOrderColumn);
            db.execSQL(addCustomOrderColumn);
        }

        if (oldVersion <= 16) { // v16 => 17. add the custom_order_id column to iOS (which was forgotten)
            if (!hasCustomOrderIdColumn(db)) {
                final String addCustomOrderColumn = String.format("ALTER TABLE %s ADD COLUMN %s INTEGER DEFAULT 0", getTableName(), AbstractColumnTable.COLUMN_CUSTOM_ORDER_ID);
                final String updateDefaultCustomOrder = String.format("UPDATE %s SET %s = ROWID", getTableName(), AbstractColumnTable.COLUMN_CUSTOM_ORDER_ID);
                Logger.debug(this, addCustomOrderColumn);
                Logger.debug(this, updateDefaultCustomOrder);
                db.execSQL(addCustomOrderColumn);
                db.execSQL(updateDefaultCustomOrder);
            }
        }

        if (oldVersion <= 18) { // v18 => 19 added UUID column
            onUpgradeToAddUUID(db, oldVersion);
        }

        if (oldVersion <= 21) { // v20 => 21 added reimbursable column
            final String alterPaymentMethods = "ALTER TABLE " + getTableName() + " ADD COLUMN " + COLUMN_REIMBURSABLE + " BOOLEAN DEFAULT 0";
            Logger.debug(this, alterPaymentMethods);
            db.execSQL(alterPaymentMethods);
        }
    }

    /**
     * When upgrading our database to version 16 on iOS, we unfortunately forgot to add the `custom_order_id` column
     * to the Payment methods table. This method is responsible for executing a SQLite PRAGMA command to check if
     * this column is present or not in the table. If not, we can add it in database version 17 (or higher) to ensure
     * parity between the two platforms.
     *
     * @param db the current database
     * @return {@code true} if the `custom_order_id` column is present, {@code false} otherwise
     * @see <a href="https://www.sqlite.org/pragma.html#pragma_table_info">PRAGMA table_info</a>
     */
    private boolean hasCustomOrderIdColumn(@NonNull SQLiteDatabase db) {
        Cursor cursor = null;
        try {
            final String pragmaTableInfo = String.format("PRAGMA table_info(%s)", getTableName());
            cursor = db.rawQuery(pragmaTableInfo, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int columnNameIndex = cursor.getColumnIndex("name");
                if (columnNameIndex >= 0) {
                    do {
                        if (AbstractColumnTable.COLUMN_CUSTOM_ORDER_ID.equals(cursor.getString(columnNameIndex))) {
                            return true;
                        }
                    }
                    while (cursor.moveToNext());
                }
            }
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}
