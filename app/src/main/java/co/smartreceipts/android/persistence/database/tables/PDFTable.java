package co.smartreceipts.android.persistence.database.tables;

import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.persistence.database.defaults.TableDefaultsCustomizer;

/**
 * Stores all database operations related to the {@link Column} model object for PDF Tables
 */
public final class PDFTable extends AbstractColumnTable {

    // SQL Definitions:
    public static final String TABLE_NAME = "pdfcolumns";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TYPE = "type";

    private static final int TABLE_EXISTS_SINCE = 9;

    public PDFTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper, @NonNull ColumnDefinitions<Receipt> receiptColumnDefinitions,
                    boolean isOrdered) {
        super(sqLiteOpenHelper, TABLE_NAME, TABLE_EXISTS_SINCE, receiptColumnDefinitions, COLUMN_ID, COLUMN_TYPE, isOrdered);
    }

    @Override
    protected void insertDefaults(@NonNull TableDefaultsCustomizer customizer) {
        customizer.insertPDFDefaults(this);
    }
}
