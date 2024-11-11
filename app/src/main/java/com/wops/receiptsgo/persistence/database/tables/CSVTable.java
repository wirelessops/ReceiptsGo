package com.wops.receiptsgo.persistence.database.tables;

import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.NonNull;

import com.wops.receiptsgo.model.Column;
import com.wops.receiptsgo.model.ColumnDefinitions;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.persistence.database.defaults.TableDefaultsCustomizer;
import com.wops.receiptsgo.persistence.database.tables.ordering.OrderingPreferencesManager;

/**
 * Stores all database operations related to the {@link Column} model object for CSV Tables
 */
public final class CSVTable extends AbstractColumnTable {

    // SQL Definitions:
    public static final String TABLE_NAME = "csvcolumns";

    private static final int TABLE_EXISTS_SINCE = 2;

    public CSVTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper, @NonNull ColumnDefinitions<Receipt> receiptColumnDefinitions,
                    @NonNull OrderingPreferencesManager orderingPreferencesManager) {
        super(sqLiteOpenHelper, TABLE_NAME, TABLE_EXISTS_SINCE, receiptColumnDefinitions, orderingPreferencesManager, CSVTable.class);
    }

    @Override
    protected void insertDefaults(@NonNull TableDefaultsCustomizer customizer) {
        customizer.insertCSVDefaults(this);
    }
}
