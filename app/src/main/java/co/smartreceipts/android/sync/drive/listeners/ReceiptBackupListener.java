package co.smartreceipts.android.sync.drive.listeners;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.sync.drive.managers.DriveDatabaseManager;
import co.smartreceipts.android.sync.drive.managers.DriveReceiptsManager;

public class ReceiptBackupListener extends DatabaseBackupListener<Receipt> {

    private final DriveReceiptsManager mDriveReceiptsManager;

    public ReceiptBackupListener(@NonNull DriveDatabaseManager driveDatabaseManager, @NonNull DriveReceiptsManager driveReceiptsManager) {
        super(driveDatabaseManager);
        mDriveReceiptsManager = Preconditions.checkNotNull(driveReceiptsManager);
    }

    @Override
    public void onInsertSuccess(@NonNull Receipt receipt, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        super.onInsertSuccess(receipt, databaseOperationMetadata);
        if (databaseOperationMetadata.getOperationFamilyType() != OperationFamilyType.Sync) {
            mDriveReceiptsManager.handleInsertOrUpdate(receipt);
        }
    }

    @Override
    public void onUpdateSuccess(@NonNull Receipt oldReceipt, @NonNull Receipt newReceipt, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (newReceipt.getFile() != null) {
            mDriveDatabaseManager.syncDatabase();
        }
        if (databaseOperationMetadata.getOperationFamilyType() != OperationFamilyType.Sync) {
            mDriveReceiptsManager.handleInsertOrUpdate(newReceipt);
        }
    }

    @Override
    public void onDeleteSuccess(@NonNull Receipt receipt, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        super.onDeleteSuccess(receipt, databaseOperationMetadata);
        if (databaseOperationMetadata.getOperationFamilyType() != OperationFamilyType.Sync) {
            mDriveReceiptsManager.handleDelete(receipt);
        }
    }
}
