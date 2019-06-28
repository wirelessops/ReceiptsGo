package co.smartreceipts.android.persistence.database.defaults;

import androidx.annotation.NonNull;

import co.smartreceipts.android.persistence.database.tables.CSVTable;
import co.smartreceipts.android.persistence.database.tables.CategoriesTable;
import co.smartreceipts.android.persistence.database.tables.PDFTable;
import co.smartreceipts.android.persistence.database.tables.PaymentMethodsTable;

public interface TableDefaultsCustomizer {

    void insertCSVDefaults(@NonNull CSVTable table);

    void insertPDFDefaults(@NonNull PDFTable table);

    void insertCategoryDefaults(@NonNull CategoriesTable table);

    void insertPaymentMethodDefaults(@NonNull PaymentMethodsTable table);
}
