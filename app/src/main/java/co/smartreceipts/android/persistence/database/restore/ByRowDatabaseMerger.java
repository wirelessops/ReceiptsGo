package co.smartreceipts.android.persistence.database.restore;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Completable;

/**
 * <p>
 * A {@link DatabaseMerger} implementation, which attempts to merge the individual rows of each
 * database, avoiding conflicts
 * </p>
 * <p>
 * Note: I was lazy and built an O(n^2) import process when comparing old vs new. We should probably
 * change to a Map approach at some point to improve manual import times, but I didn't push for this
 * as imports are relatively rare.
 * </p>
 */
public class ByRowDatabaseMerger implements DatabaseMerger {

    @NonNull
    @Override
    public Completable merge(@NonNull DatabaseHelper currentDatabase, @NonNull DatabaseHelper importedBackupDatabase) {
        return Completable.fromAction(() -> {
            Logger.info(ByRowDatabaseMerger.this, "Importing database entries by row, preferring the existing item where appropriate");

            final DatabaseOperationMetadata databaseOperationMetadata = new DatabaseOperationMetadata(OperationFamilyType.Import);

            Logger.info(ByRowDatabaseMerger.this, "Removing all existing pdf entries as we lack a good conflict resolution tool.");
            currentDatabase.getPDFTable().deleteAllTableRowsBlockiing();

            Logger.info(ByRowDatabaseMerger.this, "Removing all existing csv entries as we lack a good conflict resolution tool.");
            currentDatabase.getCSVTable().deleteAllTableRowsBlockiing();

            final List<Column<Receipt>> pdfColumns = importedBackupDatabase.getPDFTable().getBlocking();
            Logger.info(ByRowDatabaseMerger.this, "Importing {} pdf column entries", pdfColumns.size());
            for (final Column<Receipt> pdfColumn : pdfColumns) {
                currentDatabase.getPDFTable().insertBlocking(pdfColumn, databaseOperationMetadata);
            }
            
            final List<Column<Receipt>> csvColumns = importedBackupDatabase.getCSVTable().getBlocking();
            Logger.info(ByRowDatabaseMerger.this, "Importing {} csv column entries", csvColumns.size());
            for (final Column<Receipt> csvColumn : csvColumns) {
                currentDatabase.getCSVTable().insertBlocking(csvColumn, databaseOperationMetadata);
            }

            // Note: This attempts to map an "imported" payment method to a current one in the case in which an import is not required (ie a match)
            final Map<PaymentMethod, PaymentMethod> paymentMethodMap = new HashMap<>();
            final List<PaymentMethod> existingPaymentMethods = new ArrayList<>(currentDatabase.getPaymentMethodsTable().getBlocking());
            final List<PaymentMethod> importedPaymentMethods = new ArrayList<>(importedBackupDatabase.getPaymentMethodsTable().getBlocking());
            Logger.info(ByRowDatabaseMerger.this, "Importing {} payment method entries", importedPaymentMethods.size());
            for (final PaymentMethod importedPaymentMethod : importedPaymentMethods) {
                boolean wasDuplicateFound = false;
                for (final PaymentMethod existingPaymentMethod : existingPaymentMethods) {
                    if (importedPaymentMethod.getMethod().equals(existingPaymentMethod.getMethod())) {
                        wasDuplicateFound = true;
                        Logger.debug(ByRowDatabaseMerger.this, "Found a situation in which both databases have a payment method with the same attributes: {}. Ignoring import...", importedPaymentMethod);
                        paymentMethodMap.put(importedPaymentMethod, existingPaymentMethod);
                        break; // To exit inner loop early
                    }
                }
                if (!wasDuplicateFound) {
                    Logger.debug(ByRowDatabaseMerger.this, "Importing payment method: {}", importedPaymentMethod.getMethod());
                    final PaymentMethod result = currentDatabase.getPaymentMethodsTable().insertBlocking(importedPaymentMethod, databaseOperationMetadata).get();
                    paymentMethodMap.put(importedPaymentMethod, result);
                }
            }

            // Note: This attempts to map an "imported" category to a current one in the case in which an import is not required (ie a match)
            final Map<Category, Category> categoryMap = new HashMap<>();
            final List<Category> existingCategories = new ArrayList<>(currentDatabase.getCategoriesTable().getBlocking());
            final List<Category> importedCategories = new ArrayList<>(importedBackupDatabase.getCategoriesTable().getBlocking());
            Logger.info(ByRowDatabaseMerger.this, "Importing {} category entries", importedCategories.size());
            for (final Category importedCategory : importedCategories) {
                boolean wasDuplicateFound = false;
                for (final Category existingCategory : existingCategories) {
                    if (importedCategory.getCode().equals(existingCategory.getCode()) && 
                            importedCategory.getName().equals(existingCategory.getName())) {
                        wasDuplicateFound = true;
                        Logger.debug(ByRowDatabaseMerger.this, "Found a situation in which both databases have a category with the same attributes: {}. Ignoring import...", importedCategory);
                        categoryMap.put(importedCategory, existingCategory);
                        break; // To exit inner loop early
                    }
                }
                if (!wasDuplicateFound) {
                    Logger.debug(ByRowDatabaseMerger.this, "Importing category: {}", importedCategory);
                    final Category result = currentDatabase.getCategoriesTable().insertBlocking(importedCategory, databaseOperationMetadata).get();
                    categoryMap.put(importedCategory, result);
                }
            }
            
            final List<Trip> existingTrips = new ArrayList<>(currentDatabase.getTripsTable().getBlocking());
            final List<Trip> importedTrips = new ArrayList<>(importedBackupDatabase.getTripsTable().getBlocking());
            Logger.info(ByRowDatabaseMerger.this, "Importing {} trip entries", importedTrips.size());
            for (final Trip importedTrip : importedTrips) {
                boolean wasDuplicateFound = false;
                for (final Trip existingTrip : existingTrips) {
                    if (importedTrip.getName().equals(existingTrip.getName())) {
                        wasDuplicateFound = true;
                        Logger.debug(ByRowDatabaseMerger.this, "Found a situation in which both databases have a trip with the same attributes: {}. Ignoring import...", importedTrip);
                        break; // To exit inner loop early
                    }
                }
                if (!wasDuplicateFound) {
                    Logger.debug(ByRowDatabaseMerger.this, "Importing trip: {}", importedTrip);
                    currentDatabase.getTripsTable().insertBlocking(importedTrip, databaseOperationMetadata);
                }
            }

            final List<Distance> existingDistances = new ArrayList<>(currentDatabase.getDistanceTable().getBlocking());
            final List<Distance> importedDistances = new ArrayList<>(importedBackupDatabase.getDistanceTable().getBlocking());
            Logger.info(ByRowDatabaseMerger.this, "Importing {} distance entries", importedDistances.size());
            for (final Distance importedDistance : importedDistances) {
                boolean wasDuplicateFound = false;
                for (final Distance existingDistance : existingDistances) {
                    if (importedDistance.getTrip().getName().equals(existingDistance.getTrip().getName()) && 
                            importedDistance.getLocation().equals(existingDistance.getLocation()) && 
                            importedDistance.getDate().equals(existingDistance.getDate())) {
                        wasDuplicateFound = true;
                        Logger.debug(ByRowDatabaseMerger.this, "Found a situation in which both databases have a distance with the same attributes: {}. Ignoring import...", importedDistance);
                        break; // To exit inner loop early
                    }
                }
                if (!wasDuplicateFound) {
                    Logger.debug(ByRowDatabaseMerger.this, "Importing distance: {}", importedDistance);
                    currentDatabase.getDistanceTable().insertBlocking(importedDistance, databaseOperationMetadata);
                }
            }

            final List<Receipt> existingReceipts = new ArrayList<>(currentDatabase.getReceiptsTable().getBlocking());
            final List<Receipt> importedReceipts = new ArrayList<>(importedBackupDatabase.getReceiptsTable().getBlocking());
            Logger.info(ByRowDatabaseMerger.this, "Importing {} receipt entries", importedReceipts.size());
            for (final Receipt importedReceipt : importedReceipts) {
                boolean wasDuplicateFound = false;
                for (final Receipt existingReceipt : existingReceipts) {
                    if (importedReceipt.getTrip().getName().equals(existingReceipt.getTrip().getName()) &&
                            importedReceipt.getName().equals(existingReceipt.getName()) &&
                            importedReceipt.getDate().equals(existingReceipt.getDate())) {
                        wasDuplicateFound = true;
                        Logger.debug(ByRowDatabaseMerger.this, "Found a situation in which both databases have a receipt with the same attributes: {}. Ignoring import...", importedReceipt);
                        break; // To exit inner loop early
                    }
                }
                if (!wasDuplicateFound) {
                    Logger.debug(ByRowDatabaseMerger.this, "Importing receipt: {}", importedReceipt);
                    // Here we explicitly map these "mapped" values to the new result set before importing
                    final Receipt receiptToInsert = new ReceiptBuilderFactory(importedReceipt).setCategory(categoryMap.get(importedReceipt.getCategory())).setPaymentMethod(paymentMethodMap.get(importedReceipt.getPaymentMethod())).build();
                    currentDatabase.getReceiptsTable().insertBlocking(receiptToInsert, databaseOperationMetadata);
                }
            }
        });
    }
}
