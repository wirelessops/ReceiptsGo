package com.wops.receiptsgo.persistence.database.defaults;

import androidx.annotation.NonNull;

import com.wops.receiptsgo.persistence.database.tables.CSVTable;
import com.wops.receiptsgo.persistence.database.tables.CategoriesTable;
import com.wops.receiptsgo.persistence.database.tables.PDFTable;
import com.wops.receiptsgo.persistence.database.tables.PaymentMethodsTable;

public interface TableDefaultsCustomizer {

    void insertCSVDefaults(@NonNull CSVTable table);

    void insertPDFDefaults(@NonNull PDFTable table);

    void insertCategoryDefaults(@NonNull CategoriesTable table);

    void insertPaymentMethodDefaults(@NonNull PaymentMethodsTable table);
}
