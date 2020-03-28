package co.smartreceipts.android.sync.drive.listeners;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.io.File;

import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.automatic_backups.drive.managers.DriveDatabaseManager;
import co.smartreceipts.android.sync.drive.managers.DriveReceiptsManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReceiptBackupListenerTest {

    // Class under test
    ReceiptBackupListener mListener;

    @Mock
    DriveDatabaseManager mDriveDatabaseManager;
    
    @Mock
    DriveReceiptsManager mDriveReceiptsManager;
    
    @Mock
    Receipt mReceipt;

    @Mock
    Receipt mOldReceipt;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mListener = new ReceiptBackupListener(mDriveDatabaseManager, mDriveReceiptsManager);
    }

    @Test
    public void onInsertSuccess() {
        mListener.onInsertSuccess(mReceipt, new DatabaseOperationMetadata());
        verify(mDriveDatabaseManager).syncDatabase();
        verify(mDriveReceiptsManager).handleInsertOrUpdate(mReceipt);
    }

    @Test
    public void onSyncInsertSuccess() {
        mListener.onInsertSuccess(mReceipt, new DatabaseOperationMetadata(OperationFamilyType.Sync));
        verify(mDriveDatabaseManager, never()).syncDatabase();
        verify(mDriveReceiptsManager, never()).handleInsertOrUpdate(any(Receipt.class));
    }

    @Test
    public void onUpdateSuccess() {
        mListener.onUpdateSuccess(mOldReceipt, mReceipt, new DatabaseOperationMetadata());
        verify(mDriveDatabaseManager, never()).syncDatabase();
        verify(mDriveReceiptsManager).handleInsertOrUpdate(mReceipt);
    }

    @Test
    public void onSyncUpdateSuccess() {
        mListener.onUpdateSuccess(mOldReceipt, mReceipt, new DatabaseOperationMetadata(OperationFamilyType.Sync));
        verify(mDriveDatabaseManager, never()).syncDatabase();
        verify(mDriveReceiptsManager, never()).handleInsertOrUpdate(any(Receipt.class));
    }

    @Test
    public void onSyncUpdateSuccessForReceiptWithFile() {
        // We always try to sync database changes when they include a receipt file to ensure we update this stuff immediately
        when(mReceipt.getFile()).thenReturn(mock(File.class));
        mListener.onUpdateSuccess(mOldReceipt, mReceipt, new DatabaseOperationMetadata(OperationFamilyType.Sync));
        verify(mDriveDatabaseManager).syncDatabase();
        verify(mDriveReceiptsManager, never()).handleInsertOrUpdate(any(Receipt.class));
    }

    @Test
    public void onDeleteSuccess() {
        mListener.onDeleteSuccess(mReceipt, new DatabaseOperationMetadata());
        verify(mDriveDatabaseManager).syncDatabase();
        verify(mDriveReceiptsManager).handleDelete(mReceipt);
    }

    @Test
    public void onSyncDeleteSuccess() {
        mListener.onDeleteSuccess(mReceipt, new DatabaseOperationMetadata(OperationFamilyType.Sync));
        verify(mDriveDatabaseManager, never()).syncDatabase();
        verify(mDriveReceiptsManager, never()).handleDelete(any(Receipt.class));
    }

}