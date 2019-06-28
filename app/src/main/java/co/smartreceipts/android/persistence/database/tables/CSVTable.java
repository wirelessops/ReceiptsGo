package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.NonNull;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.persistence.database.defaults.TableDefaultsCustomizer;
import co.smartreceipts.android.persistence.database.tables.ordering.OrderingPreferencesManager;

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
