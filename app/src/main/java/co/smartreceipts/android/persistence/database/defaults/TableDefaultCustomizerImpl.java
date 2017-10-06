package co.smartreceipts.android.persistence.database.defaults;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.List;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory;
import co.smartreceipts.android.model.impl.ImmutableCategoryImpl;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.tables.CSVTable;
import co.smartreceipts.android.persistence.database.tables.CategoriesTable;
import co.smartreceipts.android.persistence.database.tables.PDFTable;
import co.smartreceipts.android.persistence.database.tables.PaymentMethodsTable;

public class TableDefaultCustomizerImpl implements TableDefaultsCustomizer {

    private final Context context;
    private final ReceiptColumnDefinitions receiptColumnDefinitions;

    @Inject
    public TableDefaultCustomizerImpl(@NonNull Context context, @NonNull ReceiptColumnDefinitions receiptColumnDefinitions) {
        this.context = Preconditions.checkNotNull(context.getApplicationContext());
        this.receiptColumnDefinitions = Preconditions.checkNotNull(receiptColumnDefinitions);
    }

    @Override
    public void insertCSVDefaults(@NonNull final CSVTable csvTable) {
        final DatabaseOperationMetadata databaseOperationMetadata = new DatabaseOperationMetadata();
        final List<Column<Receipt>> columns = receiptColumnDefinitions.getCsvDefaults();
        final int size = columns.size();
        for (int i = 0; i < size; i++) {
            csvTable.insertBlocking(columns.get(i), databaseOperationMetadata);
        }
    }

    @Override
    public void insertPDFDefaults(@NonNull final PDFTable pdfTable) {
        final DatabaseOperationMetadata databaseOperationMetadata = new DatabaseOperationMetadata();
        final List<Column<Receipt>> columns = receiptColumnDefinitions.getPdfDefaults();
        final int size = columns.size();
        for (int i = 0; i < size; i++) {
            pdfTable.insertBlocking(columns.get(i), databaseOperationMetadata);
        }
    }

    @Override
    public void insertCategoryDefaults(@NonNull final CategoriesTable categoriesTable) {
        final DatabaseOperationMetadata databaseOperationMetadata = new DatabaseOperationMetadata();
        final Resources resources = context.getResources();
        categoriesTable.insertBlocking(new ImmutableCategoryImpl(resources.getString(R.string.category_null), resources.getString(R.string.category_null_code)), databaseOperationMetadata);
        categoriesTable.insertBlocking(new ImmutableCategoryImpl(resources.getString(R.string.category_airfare), resources.getString(R.string.category_airfare_code)), databaseOperationMetadata);
        categoriesTable.insertBlocking(new ImmutableCategoryImpl(resources.getString(R.string.category_breakfast), resources.getString(R.string.category_breakfast_code)), databaseOperationMetadata);
        categoriesTable.insertBlocking(new ImmutableCategoryImpl(resources.getString(R.string.category_dinner), resources.getString(R.string.category_dinner_code)), databaseOperationMetadata);
        categoriesTable.insertBlocking(new ImmutableCategoryImpl(resources.getString(R.string.category_entertainment), resources.getString(R.string.category_entertainment_code)), databaseOperationMetadata);
        categoriesTable.insertBlocking(new ImmutableCategoryImpl(resources.getString(R.string.category_gasoline), resources.getString(R.string.category_gasoline_code)), databaseOperationMetadata);
        categoriesTable.insertBlocking(new ImmutableCategoryImpl(resources.getString(R.string.category_gift), resources.getString(R.string.category_gift_code)), databaseOperationMetadata);
        categoriesTable.insertBlocking(new ImmutableCategoryImpl(resources.getString(R.string.category_hotel), resources.getString(R.string.category_hotel_code)), databaseOperationMetadata);
        categoriesTable.insertBlocking(new ImmutableCategoryImpl(resources.getString(R.string.category_laundry), resources.getString(R.string.category_laundry_code)), databaseOperationMetadata);
        categoriesTable.insertBlocking(new ImmutableCategoryImpl(resources.getString(R.string.category_lunch), resources.getString(R.string.category_lunch_code)), databaseOperationMetadata);
        categoriesTable.insertBlocking(new ImmutableCategoryImpl(resources.getString(R.string.category_other), resources.getString(R.string.category_other_code)), databaseOperationMetadata);
        categoriesTable.insertBlocking(new ImmutableCategoryImpl(resources.getString(R.string.category_parking_tolls), resources.getString(R.string.category_parking_tolls_code)), databaseOperationMetadata);
        categoriesTable.insertBlocking(new ImmutableCategoryImpl(resources.getString(R.string.category_postage_shipping), resources.getString(R.string.category_postage_shipping_code)), databaseOperationMetadata);
        categoriesTable.insertBlocking(new ImmutableCategoryImpl(resources.getString(R.string.category_car_rental), resources.getString(R.string.category_car_rental_code)), databaseOperationMetadata);
        categoriesTable.insertBlocking(new ImmutableCategoryImpl(resources.getString(R.string.category_taxi_bus), resources.getString(R.string.category_taxi_bus_code)), databaseOperationMetadata);
        categoriesTable.insertBlocking(new ImmutableCategoryImpl(resources.getString(R.string.category_telephone_fax), resources.getString(R.string.category_telephone_fax_code)), databaseOperationMetadata);
        categoriesTable.insertBlocking(new ImmutableCategoryImpl(resources.getString(R.string.category_tip), resources.getString(R.string.category_tip_code)), databaseOperationMetadata);
        categoriesTable.insertBlocking(new ImmutableCategoryImpl(resources.getString(R.string.category_train), resources.getString(R.string.category_train_code)), databaseOperationMetadata);
        categoriesTable.insertBlocking(new ImmutableCategoryImpl(resources.getString(R.string.category_books_periodicals), resources.getString(R.string.category_books_periodicals_code)), databaseOperationMetadata);
        categoriesTable.insertBlocking(new ImmutableCategoryImpl(resources.getString(R.string.category_cell_phone), resources.getString(R.string.category_cell_phone_code)), databaseOperationMetadata);
        categoriesTable.insertBlocking(new ImmutableCategoryImpl(resources.getString(R.string.category_dues_subscriptions), resources.getString(R.string.category_dues_subscriptions_code)), databaseOperationMetadata);
        categoriesTable.insertBlocking(new ImmutableCategoryImpl(resources.getString(R.string.category_meals_justified), resources.getString(R.string.category_meals_justified_code)), databaseOperationMetadata);
        categoriesTable.insertBlocking(new ImmutableCategoryImpl(resources.getString(R.string.category_stationery_stations), resources.getString(R.string.category_stationery_stations_code)), databaseOperationMetadata);
        categoriesTable.insertBlocking(new ImmutableCategoryImpl(resources.getString(R.string.category_training_fees), resources.getString(R.string.category_training_fees_code)), databaseOperationMetadata);
    }

    @Override
    public void insertPaymentMethodDefaults(@NonNull final PaymentMethodsTable paymentMethodsTable) {
        final DatabaseOperationMetadata databaseOperationMetadata = new DatabaseOperationMetadata();
        paymentMethodsTable.insertBlocking(new PaymentMethodBuilderFactory().setMethod(context.getString(R.string.payment_method_default_corporate_card)).build(), databaseOperationMetadata);
        paymentMethodsTable.insertBlocking(new PaymentMethodBuilderFactory().setMethod(context.getString(R.string.payment_method_default_personal_card)).build(), databaseOperationMetadata);
        paymentMethodsTable.insertBlocking(new PaymentMethodBuilderFactory().setMethod(context.getString(R.string.payment_method_default_check)).build(), databaseOperationMetadata);
        paymentMethodsTable.insertBlocking(new PaymentMethodBuilderFactory().setMethod(context.getString(R.string.payment_method_default_cash)).build(), databaseOperationMetadata);
    }
}
