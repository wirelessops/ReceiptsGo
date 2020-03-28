package co.smartreceipts.android.sync.drive.listeners;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.automatic_backups.drive.managers.DriveDatabaseManager;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class DatabaseBackupListenerTest {

    // Class under test
    DatabaseBackupListener<Object> mListener;

    @Mock
    DriveDatabaseManager mDriveDatabaseManager;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mListener = new DatabaseBackupListener<>(mDriveDatabaseManager);
    }

    @Test
    public void onInsertSuccess() {
        mListener.onInsertSuccess(new Object(), new DatabaseOperationMetadata());
        verify(mDriveDatabaseManager).syncDatabase();
    }

    @Test
    public void onSyncInsertSuccess() {
        mListener.onInsertSuccess(new Object(), new DatabaseOperationMetadata(OperationFamilyType.Sync));
        verify(mDriveDatabaseManager, never()).syncDatabase();
    }

    @Test
    public void onUpdateSuccess() {
        mListener.onUpdateSuccess(new Object(), new Object(), new DatabaseOperationMetadata());
        verify(mDriveDatabaseManager).syncDatabase();
    }

    @Test
    public void onSyncUpdateSuccess() {
        mListener.onUpdateSuccess(new Object(), new Object(), new DatabaseOperationMetadata(OperationFamilyType.Sync));
        verify(mDriveDatabaseManager, never()).syncDatabase();
    }

    @Test
    public void onDeleteSuccess() {
        mListener.onDeleteSuccess(new Object(), new DatabaseOperationMetadata());
        verify(mDriveDatabaseManager).syncDatabase();
    }

    @Test
    public void onSyncDeleteSuccess() {
        mListener.onDeleteSuccess(new Object(), new DatabaseOperationMetadata(OperationFamilyType.Sync));
        verify(mDriveDatabaseManager, never()).syncDatabase();
    }

}