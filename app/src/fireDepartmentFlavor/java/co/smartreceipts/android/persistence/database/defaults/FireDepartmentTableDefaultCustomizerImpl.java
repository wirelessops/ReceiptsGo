package co.smartreceipts.android.persistence.database.defaults;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.google.common.base.Preconditions;

import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.CategoryBuilderFactory;
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.tables.CSVTable;
import co.smartreceipts.android.persistence.database.tables.CategoriesTable;
import co.smartreceipts.android.persistence.database.tables.PDFTable;
import co.smartreceipts.android.persistence.database.tables.PaymentMethodsTable;

public class FireDepartmentTableDefaultCustomizerImpl implements TableDefaultsCustomizer {

    private final Context context;
    private final ReceiptColumnDefinitions receiptColumnDefinitions;

    public FireDepartmentTableDefaultCustomizerImpl(@NonNull Context context, @NonNull ReceiptColumnDefinitions receiptColumnDefinitions) {
        this.context = Preconditions.checkNotNull(context);
        this.receiptColumnDefinitions = Preconditions.checkNotNull(receiptColumnDefinitions);
    }

    @Override
    public void insertCSVDefaults(@NonNull final CSVTable csvTable) {
        final DatabaseOperationMetadata databaseOperationMetadata = new DatabaseOperationMetadata();
        csvTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumn(ReceiptColumnDefinitions.ActualDefinition.DATE)), databaseOperationMetadata);
        csvTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumn(ReceiptColumnDefinitions.ActualDefinition.NAME)), databaseOperationMetadata);
        csvTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumn(ReceiptColumnDefinitions.ActualDefinition.PRICE)), databaseOperationMetadata);
        csvTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumn(ReceiptColumnDefinitions.ActualDefinition.PAYMENT_METHOD)), databaseOperationMetadata);
        csvTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumn(ReceiptColumnDefinitions.ActualDefinition.CATEGORY_NAME)), databaseOperationMetadata);
        csvTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumn(ReceiptColumnDefinitions.ActualDefinition.USER_ID)), databaseOperationMetadata);
        csvTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumn(ReceiptColumnDefinitions.ActualDefinition.COMMENT)), databaseOperationMetadata);
    }

    @Override
    public void insertPDFDefaults(@NonNull final PDFTable pdfTable) {
        final DatabaseOperationMetadata databaseOperationMetadata = new DatabaseOperationMetadata();
        pdfTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumn(ReceiptColumnDefinitions.ActualDefinition.DATE)), databaseOperationMetadata);
        pdfTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumn(ReceiptColumnDefinitions.ActualDefinition.NAME)), databaseOperationMetadata);
        pdfTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumn(ReceiptColumnDefinitions.ActualDefinition.PRICE)), databaseOperationMetadata);
        pdfTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumn(ReceiptColumnDefinitions.ActualDefinition.PAYMENT_METHOD)), databaseOperationMetadata);
        pdfTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumn(ReceiptColumnDefinitions.ActualDefinition.CATEGORY_NAME)), databaseOperationMetadata);
        pdfTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumn(ReceiptColumnDefinitions.ActualDefinition.USER_ID)), databaseOperationMetadata);
        pdfTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumn(ReceiptColumnDefinitions.ActualDefinition.COMMENT)), databaseOperationMetadata);
    }

    @Override
    public void insertCategoryDefaults(@NonNull final CategoriesTable categoriesTable) {
        insertCategoryWithSameNameAndCode(categoriesTable, R.string.fire_department_category_trucks);
        insertCategoryWithSameNameAndCode(categoriesTable, R.string.fire_department_category_station);
        insertCategoryWithSameNameAndCode(categoriesTable, R.string.fire_department_category_computer_it);
        insertCategoryWithSameNameAndCode(categoriesTable, R.string.fire_department_category_office_supplies);
        insertCategoryWithSameNameAndCode(categoriesTable, R.string.fire_department_category_training_fees);
        insertCategoryWithSameNameAndCode(categoriesTable, R.string.fire_department_category_gear);
        insertCategoryWithSameNameAndCode(categoriesTable, R.string.fire_department_category_uniforms);
        insertCategoryWithSameNameAndCode(categoriesTable, R.string.fire_department_category_equipment);
        insertCategoryWithSameNameAndCode(categoriesTable, R.string.fire_department_category_hotel);
        insertCategoryWithSameNameAndCode(categoriesTable, R.string.fire_department_category_meals);
        insertCategoryWithSameNameAndCode(categoriesTable, R.string.fire_department_category_fuel);
        insertCategoryWithSameNameAndCode(categoriesTable, R.string.fire_department_category_other);
    }

    @Override
    public void insertPaymentMethodDefaults(@NonNull final PaymentMethodsTable paymentMethodsTable) {
        final DatabaseOperationMetadata databaseOperationMetadata = new DatabaseOperationMetadata();
        paymentMethodsTable.insertBlocking(new PaymentMethodBuilderFactory().setMethod(context.getString(R.string.fire_department_payment_method_charge)).build(), databaseOperationMetadata);
        paymentMethodsTable.insertBlocking(new PaymentMethodBuilderFactory().setMethod(context.getString(R.string.fire_department_payment_method_department_credit_card)).build(), databaseOperationMetadata);
        paymentMethodsTable.insertBlocking(new PaymentMethodBuilderFactory().setMethod(context.getString(R.string.fire_department_payment_method_cash_reimbursable)).build(), databaseOperationMetadata);
        paymentMethodsTable.insertBlocking(new PaymentMethodBuilderFactory().setMethod(context.getString(R.string.fire_department_payment_method_department_check)).build(), databaseOperationMetadata);
    }


    private void insertCategoryWithSameNameAndCode(@NonNull CategoriesTable categoriesTable, @StringRes int stringResId) {
        final String category = context.getString(stringResId);
        final DatabaseOperationMetadata databaseOperationMetadata = new DatabaseOperationMetadata();
        categoriesTable.insertBlocking(new CategoryBuilderFactory().setName(category).setCode(category).build(), databaseOperationMetadata);
    }
}
