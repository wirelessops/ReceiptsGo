package com.wops.receiptsgo.persistence.database.defaults;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import com.wops.receiptsgo.persistence.database.tables.CSVTable;
import com.wops.receiptsgo.persistence.database.tables.CategoriesTable;
import com.wops.receiptsgo.persistence.database.tables.PDFTable;
import com.wops.receiptsgo.persistence.database.tables.PaymentMethodsTable;

public class WhiteLabelFriendlyTableDefaultsCustomizer implements TableDefaultsCustomizer {

    DefaultTableDefaultCustomizerImpl tableDefaultsCustomizer;

    public WhiteLabelFriendlyTableDefaultsCustomizer(@NonNull DefaultTableDefaultCustomizerImpl tableDefaultCustomizer) {
        this.tableDefaultsCustomizer = Preconditions.checkNotNull(tableDefaultCustomizer);
    }

    @Override
    public void insertCSVDefaults(@NonNull CSVTable table) {
        tableDefaultsCustomizer.insertCSVDefaults(table);
    }

    @Override
    public void insertPDFDefaults(@NonNull PDFTable table) {
        tableDefaultsCustomizer.insertPDFDefaults(table);
    }

    @Override
    public void insertCategoryDefaults(@NonNull CategoriesTable table) {
        tableDefaultsCustomizer.insertCategoryDefaults(table);
    }

    @Override
    public void insertPaymentMethodDefaults(@NonNull PaymentMethodsTable table) {
        tableDefaultsCustomizer.insertPaymentMethodDefaults(table);
    }
}
