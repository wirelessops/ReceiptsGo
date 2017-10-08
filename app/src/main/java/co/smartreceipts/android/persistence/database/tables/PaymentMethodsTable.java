package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.persistence.database.defaults.TableDefaultsCustomizer;
import co.smartreceipts.android.persistence.database.tables.adapters.PaymentMethodDatabaseAdapter;
import co.smartreceipts.android.persistence.database.tables.keys.PaymentMethodPrimaryKey;
import co.smartreceipts.android.utils.log.Logger;

/**
 * Stores all database operations related to the {@link PaymentMethod} model object
 */
public final class PaymentMethodsTable extends AbstractSqlTable<PaymentMethod, Integer> {

    // SQL Definitions:
    public static final String TABLE_NAME = "paymentmethods";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_METHOD = "method";


    public PaymentMethodsTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper) {
        super(sqLiteOpenHelper, TABLE_NAME, new PaymentMethodDatabaseAdapter(), new PaymentMethodPrimaryKey());
    }

    @Override
    public synchronized void onCreate(@NonNull SQLiteDatabase db, @NonNull TableDefaultsCustomizer customizer) {
        super.onCreate(db, customizer);
        final String sql = "CREATE TABLE " + getTableName() + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_METHOD + " TEXT, "
                + AbstractSqlTable.COLUMN_DRIVE_SYNC_ID + " TEXT, "
                + AbstractSqlTable.COLUMN_DRIVE_IS_SYNCED + " BOOLEAN DEFAULT 0, "
                + AbstractSqlTable.COLUMN_DRIVE_MARKED_FOR_DELETION + " BOOLEAN DEFAULT 0, "
                + AbstractSqlTable.COLUMN_LAST_LOCAL_MODIFICATION_TIME + " DATE"
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

        if (oldVersion <= 14) {
            onUpgradeToAddSyncInformation(db, oldVersion, newVersion);
        }

        if (oldVersion <= 15) {
            // TODO: 02.10.2017 add 'custom_order_id' column
        }
    }

}
