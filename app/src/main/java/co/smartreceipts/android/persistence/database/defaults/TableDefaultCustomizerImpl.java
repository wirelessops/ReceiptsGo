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
import co.smartreceipts.android.model.factory.CategoryBuilderFactory;
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.tables.CSVTable;
import co.smartreceipts.android.persistence.database.tables.CategoriesTable;
import co.smartreceipts.android.persistence.database.tables.PDFTable;
import co.smartreceipts.android.persistence.database.tables.PaymentMethodsTable;

public class TableDefaultCustomizerImpl implements TableDefaultsCustomizer {

    @Inject Context context;
    @Inject ReceiptColumnDefinitions receiptColumnDefinitions;

    @Inject
    public TableDefaultCustomizerImpl() {
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
        categoriesTable.insertBlocking(new CategoryBuilderFactory().setName(resources.getString(R.string.category_null))
                .setCode(resources.getString(R.string.category_null_code)).build(), databaseOperationMetadata);
        categoriesTable.insertBlocking(new CategoryBuilderFactory().setName(resources.getString(R.string.category_airfare))
                .setCode(resources.getString(R.string.category_airfare_code)).build(), databaseOperationMetadata);
        categoriesTable.insertBlocking(new CategoryBuilderFactory().setName(resources.getString(R.string.category_breakfast))
                .setCode(resources.getString(R.string.category_breakfast_code)).build(), databaseOperationMetadata);
        categoriesTable.insertBlocking(new CategoryBuilderFactory().setName(resources.getString(R.string.category_dinner))
                .setCode(resources.getString(R.string.category_dinner_code)).build(), databaseOperationMetadata);
        categoriesTable.insertBlocking(new CategoryBuilderFactory().setName(resources.getString(R.string.category_entertainment))
                .setCode(resources.getString(R.string.category_entertainment_code)).build(), databaseOperationMetadata);
        categoriesTable.insertBlocking(new CategoryBuilderFactory().setName(resources.getString(R.string.category_gasoline))
                .setCode(resources.getString(R.string.category_gasoline_code)).build(), databaseOperationMetadata);
        categoriesTable.insertBlocking(new CategoryBuilderFactory().setName(resources.getString(R.string.category_gift))
                .setCode(resources.getString(R.string.category_gift_code)).build(), databaseOperationMetadata);
        categoriesTable.insertBlocking(new CategoryBuilderFactory().setName(resources.getString(R.string.category_hotel))
                .setCode(resources.getString(R.string.category_hotel_code)).build(), databaseOperationMetadata);
        categoriesTable.insertBlocking(new CategoryBuilderFactory().setName(resources.getString(R.string.category_laundry))
                .setCode(resources.getString(R.string.category_laundry_code)).build(), databaseOperationMetadata);
        categoriesTable.insertBlocking(new CategoryBuilderFactory().setName(resources.getString(R.string.category_lunch))
                .setCode(resources.getString(R.string.category_lunch_code)).build(), databaseOperationMetadata);
        categoriesTable.insertBlocking(new CategoryBuilderFactory().setName(resources.getString(R.string.category_other))
                .setCode(resources.getString(R.string.category_other_code)).build(), databaseOperationMetadata);
        categoriesTable.insertBlocking(new CategoryBuilderFactory().setName(resources.getString(R.string.category_parking_tolls))
                .setCode(resources.getString(R.string.category_parking_tolls_code)).build(), databaseOperationMetadata);
        categoriesTable.insertBlocking(new CategoryBuilderFactory().setName(resources.getString(R.string.category_postage_shipping))
                .setCode(resources.getString(R.string.category_postage_shipping_code)).build(), databaseOperationMetadata);
        categoriesTable.insertBlocking(new CategoryBuilderFactory().setName(resources.getString(R.string.category_car_rental))
                .setCode(resources.getString(R.string.category_car_rental_code)).build(), databaseOperationMetadata);
        categoriesTable.insertBlocking(new CategoryBuilderFactory().setName(resources.getString(R.string.category_taxi_bus))
                .setCode(resources.getString(R.string.category_taxi_bus_code)).build(), databaseOperationMetadata);
        categoriesTable.insertBlocking(new CategoryBuilderFactory().setName(resources.getString(R.string.category_telephone_fax))
                .setCode(resources.getString(R.string.category_telephone_fax_code)).build(), databaseOperationMetadata);
        categoriesTable.insertBlocking(new CategoryBuilderFactory().setName(resources.getString(R.string.category_tip))
                .setCode(resources.getString(R.string.category_tip_code)).build(), databaseOperationMetadata);
        categoriesTable.insertBlocking(new CategoryBuilderFactory().setName(resources.getString(R.string.category_train))
                .setCode(resources.getString(R.string.category_train_code)).build(), databaseOperationMetadata);
        categoriesTable.insertBlocking(new CategoryBuilderFactory().setName(resources.getString(R.string.category_books_periodicals))
                .setCode(resources.getString(R.string.category_books_periodicals_code)).build(), databaseOperationMetadata);
        categoriesTable.insertBlocking(new CategoryBuilderFactory().setName(resources.getString(R.string.category_cell_phone))
                .setCode(resources.getString(R.string.category_cell_phone_code)).build(), databaseOperationMetadata);
        categoriesTable.insertBlocking(new CategoryBuilderFactory().setName(resources.getString(R.string.category_dues_subscriptions))
                .setCode(resources.getString(R.string.category_dues_subscriptions_code)).build(), databaseOperationMetadata);
        categoriesTable.insertBlocking(new CategoryBuilderFactory().setName(resources.getString(R.string.category_meals_justified))
                .setCode(resources.getString(R.string.category_meals_justified_code)).build(), databaseOperationMetadata);
        categoriesTable.insertBlocking(new CategoryBuilderFactory().setName(resources.getString(R.string.category_stationery_stations))
                .setCode(resources.getString(R.string.category_stationery_stations_code)).build(), databaseOperationMetadata);
        categoriesTable.insertBlocking(new CategoryBuilderFactory().setName(resources.getString(R.string.category_training_fees))
                .setCode(resources.getString(R.string.category_training_fees_code)).build(), databaseOperationMetadata);
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
