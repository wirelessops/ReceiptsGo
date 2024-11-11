package com.wops.receiptsgo.persistence.database.tables;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import co.smartreceipts.analytics.log.Logger;
import com.wops.receiptsgo.model.Category;
import com.wops.receiptsgo.persistence.database.defaults.TableDefaultsCustomizer;
import com.wops.receiptsgo.persistence.database.tables.adapters.CategoryDatabaseAdapter;
import com.wops.receiptsgo.persistence.database.tables.ordering.OrderByColumn;
import com.wops.receiptsgo.persistence.database.tables.ordering.OrderByOrderingPreference;
import com.wops.receiptsgo.persistence.database.tables.ordering.OrderingPreferencesManager;

/**
 * Stores all database operations related to the {@link Category} model object
 */
public final class CategoriesTable extends AbstractSqlTable<Category> {

    // SQL Definitions:
    public static final String TABLE_NAME = "categories";

    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_CODE = "code";
    public static final String COLUMN_BREAKDOWN = "breakdown";


    public CategoriesTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper,
                           @NonNull OrderingPreferencesManager orderingPreferencesManager) {
        super(sqLiteOpenHelper, TABLE_NAME, new CategoryDatabaseAdapter(),
                new OrderByOrderingPreference(orderingPreferencesManager, CategoriesTable.class, new OrderByColumn(COLUMN_CUSTOM_ORDER_ID, false), new OrderByColumn(COLUMN_NAME, false)));
    }

    @Override
    public synchronized void onCreate(@NonNull SQLiteDatabase db, @NonNull TableDefaultsCustomizer customizer) {
        super.onCreate(db, customizer);
        final String categories = "CREATE TABLE " + getTableName() + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT, "
                + COLUMN_CODE + " TEXT, "
                + COLUMN_BREAKDOWN + " BOOLEAN DEFAULT 1, "
                + COLUMN_DRIVE_SYNC_ID + " TEXT, "
                + COLUMN_DRIVE_IS_SYNCED + " BOOLEAN DEFAULT 0, "
                + COLUMN_DRIVE_MARKED_FOR_DELETION + " BOOLEAN DEFAULT 0, "
                + COLUMN_LAST_LOCAL_MODIFICATION_TIME + " DATE, "
                + COLUMN_CUSTOM_ORDER_ID + " INTEGER DEFAULT 0, "
                + COLUMN_UUID + " TEXT "
                + ");";

        Logger.debug(this, categories);
        db.execSQL(categories);

        customizer.insertCategoryDefaults(this);
    }

    @Override
    public synchronized void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion, @NonNull TableDefaultsCustomizer customizer) {
        super.onUpgrade(db, oldVersion, newVersion, customizer);
        if (oldVersion <= 2) { 
            final String alterCategories = "ALTER TABLE " + getTableName() +
                                           " ADD " + COLUMN_BREAKDOWN + " BOOLEAN DEFAULT 1";
            db.execSQL(alterCategories);
        }
        if (oldVersion <= 14) {
            onUpgradeToAddSyncInformation(db, oldVersion, newVersion);
        }
        if (oldVersion <= 15) {
            // changing primary key
            final String copyTable = "CREATE TABLE " + getTableName() + "_copy" + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_NAME + " TEXT, "
                    + COLUMN_CODE + " TEXT, "
                    + COLUMN_BREAKDOWN + " BOOLEAN DEFAULT 1, "
                    + COLUMN_DRIVE_SYNC_ID + " TEXT, "
                    + COLUMN_DRIVE_IS_SYNCED + " BOOLEAN DEFAULT 0, "
                    + COLUMN_DRIVE_MARKED_FOR_DELETION + " BOOLEAN DEFAULT 0, "
                    + COLUMN_LAST_LOCAL_MODIFICATION_TIME + " DATE, "
                    + COLUMN_CUSTOM_ORDER_ID + " INTEGER DEFAULT 0"
                    + ");";
            Logger.debug(this, copyTable);
            db.execSQL(copyTable);

            final String baseColumns = TextUtils.join(", ", new String[]{
                    COLUMN_NAME, COLUMN_CODE, COLUMN_BREAKDOWN, COLUMN_DRIVE_SYNC_ID, COLUMN_DRIVE_IS_SYNCED,
                    COLUMN_DRIVE_MARKED_FOR_DELETION, COLUMN_LAST_LOCAL_MODIFICATION_TIME});

            final String insertData = "INSERT INTO " + getTableName() + "_copy"
                    + " (" + baseColumns + ") "
                    + "SELECT " + baseColumns
                    + " FROM " + getTableName() + ";";
            Logger.debug(this, insertData);
            db.execSQL(insertData);

            final String dropOldTable = "DROP TABLE " + getTableName() + ";";
            Logger.debug(this, dropOldTable);
            db.execSQL(dropOldTable);

            final String renameTable = "ALTER TABLE " + getTableName() + "_copy" + " RENAME TO " + getTableName() + ";";
            Logger.debug(this, renameTable);
            db.execSQL(renameTable);
        }

        if (oldVersion <= 18) { // v18 => 19 added UUID column
            onUpgradeToAddUUID(db, oldVersion);
        }
    }
}
