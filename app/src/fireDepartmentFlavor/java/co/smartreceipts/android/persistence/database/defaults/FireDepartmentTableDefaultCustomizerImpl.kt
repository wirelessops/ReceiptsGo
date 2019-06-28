package co.smartreceipts.android.persistence.database.defaults

import android.content.Context
import androidx.annotation.StringRes
import co.smartreceipts.android.R
import co.smartreceipts.android.model.factory.CategoryBuilderFactory
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata
import co.smartreceipts.android.persistence.database.tables.CSVTable
import co.smartreceipts.android.persistence.database.tables.CategoriesTable
import co.smartreceipts.android.persistence.database.tables.PDFTable
import co.smartreceipts.android.persistence.database.tables.PaymentMethodsTable
import com.google.common.base.Preconditions

class FireDepartmentTableDefaultCustomizerImpl(private val context: Context,
                                               private val receiptColumnDefinitions: ReceiptColumnDefinitions) : TableDefaultsCustomizer {

    override fun insertCSVDefaults(csvTable: CSVTable) {
        val databaseOperationMetadata = DatabaseOperationMetadata()
        csvTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumnFromDefinition(ReceiptColumnDefinitions.ActualDefinition.DATE)), databaseOperationMetadata)
        csvTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumnFromDefinition(ReceiptColumnDefinitions.ActualDefinition.NAME)), databaseOperationMetadata)
        csvTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumnFromDefinition(ReceiptColumnDefinitions.ActualDefinition.PRICE)), databaseOperationMetadata)
        csvTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumnFromDefinition(ReceiptColumnDefinitions.ActualDefinition.PAYMENT_METHOD)), databaseOperationMetadata)
        csvTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumnFromDefinition(ReceiptColumnDefinitions.ActualDefinition.CATEGORY_NAME)), databaseOperationMetadata)
        csvTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumnFromDefinition(ReceiptColumnDefinitions.ActualDefinition.USER_ID)), databaseOperationMetadata)
        csvTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumnFromDefinition(ReceiptColumnDefinitions.ActualDefinition.COMMENT)), databaseOperationMetadata)
    }

    override fun insertPDFDefaults(pdfTable: PDFTable) {
        val databaseOperationMetadata = DatabaseOperationMetadata()
        pdfTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumnFromDefinition(ReceiptColumnDefinitions.ActualDefinition.DATE)), databaseOperationMetadata)
        pdfTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumnFromDefinition(ReceiptColumnDefinitions.ActualDefinition.NAME)), databaseOperationMetadata)
        pdfTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumnFromDefinition(ReceiptColumnDefinitions.ActualDefinition.PRICE)), databaseOperationMetadata)
        pdfTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumnFromDefinition(ReceiptColumnDefinitions.ActualDefinition.PAYMENT_METHOD)), databaseOperationMetadata)
        pdfTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumnFromDefinition(ReceiptColumnDefinitions.ActualDefinition.CATEGORY_NAME)), databaseOperationMetadata)
        pdfTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumnFromDefinition(ReceiptColumnDefinitions.ActualDefinition.USER_ID)), databaseOperationMetadata)
        pdfTable.insertBlocking(Preconditions.checkNotNull(receiptColumnDefinitions.getColumnFromDefinition(ReceiptColumnDefinitions.ActualDefinition.COMMENT)), databaseOperationMetadata)
    }

    override fun insertCategoryDefaults(categoriesTable: CategoriesTable) {
        insertCategoryWithSameNameAndCode(categoriesTable, R.string.fire_department_category_trucks)
        insertCategoryWithSameNameAndCode(categoriesTable, R.string.fire_department_category_station)
        insertCategoryWithSameNameAndCode(categoriesTable, R.string.fire_department_category_computer_it)
        insertCategoryWithSameNameAndCode(categoriesTable, R.string.fire_department_category_office_supplies)
        insertCategoryWithSameNameAndCode(categoriesTable, R.string.fire_department_category_training_fees)
        insertCategoryWithSameNameAndCode(categoriesTable, R.string.fire_department_category_gear)
        insertCategoryWithSameNameAndCode(categoriesTable, R.string.fire_department_category_uniforms)
        insertCategoryWithSameNameAndCode(categoriesTable, R.string.fire_department_category_equipment)
        insertCategoryWithSameNameAndCode(categoriesTable, R.string.fire_department_category_hotel)
        insertCategoryWithSameNameAndCode(categoriesTable, R.string.fire_department_category_meals)
        insertCategoryWithSameNameAndCode(categoriesTable, R.string.fire_department_category_fuel)
        insertCategoryWithSameNameAndCode(categoriesTable, R.string.fire_department_category_other)
    }

    override fun insertPaymentMethodDefaults(paymentMethodsTable: PaymentMethodsTable) {
        val databaseOperationMetadata = DatabaseOperationMetadata()
        paymentMethodsTable.insertBlocking(PaymentMethodBuilderFactory().setMethod(context.getString(R.string.fire_department_payment_method_charge)).build(), databaseOperationMetadata)
        paymentMethodsTable.insertBlocking(PaymentMethodBuilderFactory().setMethod(context.getString(R.string.fire_department_payment_method_cash)).build(), databaseOperationMetadata)
        paymentMethodsTable.insertBlocking(PaymentMethodBuilderFactory().setMethod(context.getString(R.string.fire_department_payment_method_department_credit_card)).build(), databaseOperationMetadata)
        paymentMethodsTable.insertBlocking(PaymentMethodBuilderFactory().setMethod(context.getString(R.string.fire_department_payment_method_department_check)).build(), databaseOperationMetadata)
    }


    private fun insertCategoryWithSameNameAndCode(categoriesTable: CategoriesTable, @StringRes stringResId: Int) {
        val category = context.getString(stringResId)
        val databaseOperationMetadata = DatabaseOperationMetadata()
        categoriesTable.insertBlocking(CategoryBuilderFactory().setName(category).setCode(category).build(), databaseOperationMetadata)
    }
}
