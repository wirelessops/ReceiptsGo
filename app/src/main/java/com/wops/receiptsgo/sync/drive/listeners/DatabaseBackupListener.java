package com.wops.receiptsgo.sync.drive.listeners;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import com.wops.receiptsgo.persistence.database.controllers.impl.StubTableEventsListener;
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata;
import com.wops.receiptsgo.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.automatic_backups.drive.managers.DriveDatabaseManager;

public class DatabaseBackupListener<ModelType> extends StubTableEventsListener<ModelType> {

    protected final DriveDatabaseManager mDriveDatabaseManager;

    public DatabaseBackupListener(@NonNull DriveDatabaseManager driveDatabaseManager) {
        mDriveDatabaseManager = Preconditions.checkNotNull(driveDatabaseManager);
    }

    @Override
    public void onInsertSuccess(@NonNull ModelType modelType, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (databaseOperationMetadata.getOperationFamilyType() != OperationFamilyType.Sync) {
            mDriveDatabaseManager.syncDatabase();
        }
    }

    @Override
    public void onUpdateSuccess(@NonNull ModelType oldT, @NonNull ModelType newT, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (databaseOperationMetadata.getOperationFamilyType() != OperationFamilyType.Sync) {
            mDriveDatabaseManager.syncDatabase();
        }
    }

    @Override
    public void onDeleteSuccess(@NonNull ModelType modelType, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (databaseOperationMetadata.getOperationFamilyType() != OperationFamilyType.Sync) {
            mDriveDatabaseManager.syncDatabase();
        }
    }
}
