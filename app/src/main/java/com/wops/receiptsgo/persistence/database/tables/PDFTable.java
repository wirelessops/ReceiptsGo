package com.wops.receiptsgo.persistence.database.tables;

import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.NonNull;

import com.wops.receiptsgo.model.Column;
import com.wops.receiptsgo.model.ColumnDefinitions;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.persistence.database.defaults.TableDefaultsCustomizer;
import com.wops.receiptsgo.persistence.database.tables.ordering.OrderingPreferencesManager;

/**
 * Stores all database operations related to the {@link Column} model object for PDF Tables
 */
public final class PDFTable extends AbstractColumnTable {

    // SQL Definitions:
    public static final String TABLE_NAME = "pdfcolumns";

    private static final int TABLE_EXISTS_SINCE = 9;

    public PDFTable(@NonNull SQLiteOpenHelper sqLiteOpenHelper,
                    @NonNull ColumnDefinitions<Receipt> receiptColumnDefinitions,
                    @NonNull OrderingPreferencesManager orderingPreferencesManager) {
        super(sqLiteOpenHelper, TABLE_NAME, TABLE_EXISTS_SINCE, receiptColumnDefinitions, orderingPreferencesManager, PDFTable.class);
    }

    @Override
    protected void insertDefaults(@NonNull TableDefaultsCustomizer customizer) {
        customizer.insertPDFDefaults(this);
    }
}
