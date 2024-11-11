package com.wops.receiptsgo.sync.drive.listeners;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata;
import com.wops.receiptsgo.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.automatic_backups.drive.managers.DriveDatabaseManager;
import com.wops.receiptsgo.sync.drive.managers.DriveReceiptsManager;

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
