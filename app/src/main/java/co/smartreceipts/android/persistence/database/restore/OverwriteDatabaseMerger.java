package co.smartreceipts.android.persistence.database.restore;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.DistanceBuilderFactory;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.persistence.database.tables.Table;
import co.smartreceipts.analytics.log.Logger;
import io.reactivex.Completable;

/**
 * A {@link DatabaseMerger} implementation, which overwrites the current database with an imported
 * one
 */
public class OverwriteDatabaseMerger implements DatabaseMerger {


    @NonNull
    @Override
    public Completable merge(@NonNull DatabaseHelper currentDatabase, @NonNull DatabaseHelper importedBackupDatabase) {
        return Completable.fromAction(() -> {
            Logger.info(OverwriteDatabaseMerger.this, "Overwriting database entries for the merge process");

            // TODO: Delete old files receipt files as well (note: how to handle file name conflicts?)

            for (final Table table : currentDatabase.getTables()) {
                Logger.info(OverwriteDatabaseMerger.this, "Deleting all rows in {}", table.getTableName());
                table.deleteAllTableRowsBlocking();
            }
            
            final DatabaseOperationMetadata databaseOperationMetadata = new DatabaseOperationMetadata(OperationFamilyType.Import);
            
            final List<Column<Receipt>> pdfColumns = importedBackupDatabase.getPDFTable().getBlocking();
            Logger.info(OverwriteDatabaseMerger.this, "Importing {} pdf column entries", pdfColumns.size());
            for (final Column<Receipt> pdfColumn : pdfColumns) {
                currentDatabase.getPDFTable().insertBlocking(pdfColumn, databaseOperationMetadata);
            }

            final List<Column<Receipt>> csvColumns = importedBackupDatabase.getCSVTable().getBlocking();
            Logger.info(OverwriteDatabaseMerger.this, "Importing {} csv column entries", csvColumns.size());
            for (final Column<Receipt> csvColumn : csvColumns) {
                currentDatabase.getCSVTable().insertBlocking(csvColumn, databaseOperationMetadata);
            }

            // Note: This attempts to map an "imported" payment method to the inserted one (as primary keys can differ)
            final Map<PaymentMethod, PaymentMethod> paymentMethodMap = new HashMap<>();
            final List<PaymentMethod> paymentMethods = importedBackupDatabase.getPaymentMethodsTable().getBlocking();
            Logger.info(OverwriteDatabaseMerger.this, "Importing {} payment method entries", paymentMethods.size());
            for (final PaymentMethod paymentMethod : paymentMethods) {
                final PaymentMethod result = currentDatabase.getPaymentMethodsTable().insertBlocking(paymentMethod, databaseOperationMetadata).get();
                paymentMethodMap.put(paymentMethod, result);
            }

            // Note: This attempts to map an "imported" category to the inserted one (as primary keys can differ)
            final Map<Category, Category> categoryMap = new HashMap<>();
            final List<Category> categories = importedBackupDatabase.getCategoriesTable().getBlocking();
            Logger.info(OverwriteDatabaseMerger.this, "Importing {} category entries", categories.size());
            for (final Category category : categories) {
                final Category result = currentDatabase.getCategoriesTable().insertBlocking(category, databaseOperationMetadata).get();
                categoryMap.put(category, result);
            }

            // Note: This attempts to map an "imported" trip to the inserted one
            final Map<Trip, Trip> tripMap = new HashMap<>();
            final List<Trip> trips = importedBackupDatabase.getTripsTable().getBlocking();
            Logger.info(OverwriteDatabaseMerger.this, "Importing {} trip entries", trips.size());
            for (final Trip trip : trips) {
                final Trip result = currentDatabase.getTripsTable().insertBlocking(trip, databaseOperationMetadata).get();
                tripMap.put(trip, result);
            }

            final List<Distance> distances = importedBackupDatabase.getDistanceTable().getBlocking();
            Logger.info(OverwriteDatabaseMerger.this, "Importing {} distance entries", distances.size());
            for (final Distance importedDistance : distances) {
                final Distance distanceToInsert = new DistanceBuilderFactory(importedDistance).setTrip(tripMap.get(importedDistance.getTrip())).build();
                currentDatabase.getDistanceTable().insertBlocking(distanceToInsert, databaseOperationMetadata);
            }

            final List<Receipt> receipts = importedBackupDatabase.getReceiptsTable().getBlocking();
            Logger.info(OverwriteDatabaseMerger.this, "Importing {} receipt entries", receipts.size());
            for (final Receipt importedReceipt : receipts) {
                final ReceiptBuilderFactory builder = new ReceiptBuilderFactory(importedReceipt)
                        .setTrip(tripMap.get(importedReceipt.getTrip()))
                        .setCategory(categoryMap.get(importedReceipt.getCategory()))
                        .setPaymentMethod(paymentMethodMap.get(importedReceipt.getPaymentMethod()));
                final Receipt receiptToInsert = builder.build();
                currentDatabase.getReceiptsTable().insertBlocking(receiptToInsert, databaseOperationMetadata);
            }
        });
    }
}
