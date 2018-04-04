package co.smartreceipts.android.persistence.database.restore;

import android.support.annotation.NonNull;

import java.util.List;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.persistence.database.tables.Table;
import co.smartreceipts.android.utils.log.Logger;
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
                table.deleteAllTableRowsBlockiing();
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

            final List<PaymentMethod> paymentMethods = importedBackupDatabase.getPaymentMethodsTable().getBlocking();
            Logger.info(OverwriteDatabaseMerger.this, "Importing {} payment method entries", paymentMethods.size());
            for (final PaymentMethod paymentMethod : paymentMethods) {
                currentDatabase.getPaymentMethodsTable().insertBlocking(paymentMethod, databaseOperationMetadata);
            }

            final List<Category> categories = importedBackupDatabase.getCategoriesTable().getBlocking();
            Logger.info(OverwriteDatabaseMerger.this, "Importing {} category entries", categories.size());
            for (final Category category : categories) {
                currentDatabase.getCategoriesTable().insertBlocking(category, databaseOperationMetadata);
            }

            final List<Trip> trips = importedBackupDatabase.getTripsTable().getBlocking();
            Logger.info(OverwriteDatabaseMerger.this, "Importing {} trip entries", trips.size());
            for (final Trip trip : trips) {
                currentDatabase.getTripsTable().insertBlocking(trip, databaseOperationMetadata);
            }

            final List<Distance> distances = importedBackupDatabase.getDistanceTable().getBlocking();
            Logger.info(OverwriteDatabaseMerger.this, "Importing {} distance entries", distances.size());
            for (final Distance distance : distances) {
                currentDatabase.getDistanceTable().insertBlocking(distance, databaseOperationMetadata);
            }

            final List<Receipt> receipts = importedBackupDatabase.getReceiptsTable().getBlocking();
            Logger.info(OverwriteDatabaseMerger.this, "Importing {} receipt entries", receipts.size());
            for (final Receipt receipt : receipts) {
                currentDatabase.getReceiptsTable().insertBlocking(receipt, databaseOperationMetadata);
            }
        });
    }
}
