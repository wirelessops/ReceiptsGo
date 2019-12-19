package co.smartreceipts.android.sync.drive.managers;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.io.File;

import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.ErrorEvent;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.sync.drive.device.GoogleDriveSyncMetadata;
import co.smartreceipts.android.sync.drive.rx.DriveStreamsManager;
import co.smartreceipts.core.sync.model.impl.Identifier;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class DriveDatabaseManagerTest {

    // Class under test
    DriveDatabaseManager mDriveDatabaseManager;

    File mDatabaseFile;

    @Mock
    DriveStreamsManager mDriveStreamsManager;

    @Mock
    GoogleDriveSyncMetadata mGoogleDriveSyncMetadata;

    @Mock
    Analytics mAnalytics;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        mDatabaseFile = new File(ApplicationProvider.getApplicationContext().getExternalFilesDir(null), DatabaseHelper.DATABASE_NAME);
        if (!mDatabaseFile.createNewFile()) {
            throw new RuntimeException("Failed to create database file... Failing this test");
        }

        mDriveDatabaseManager = new DriveDatabaseManager(ApplicationProvider.getApplicationContext(), mDriveStreamsManager, mGoogleDriveSyncMetadata, mAnalytics, Schedulers.trampoline(), Schedulers.trampoline());
    }

    @After
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void tearDown() throws Exception {
        mDatabaseFile.delete();
    }

    @Test
    public void syncDatabaseForTheFirstTime() {
        final Identifier identifier = new Identifier("newId");
        when(mDriveStreamsManager.uploadFileToDrive(mDatabaseFile)).thenReturn(Single.just(identifier));

        mDriveDatabaseManager.syncDatabase();
        verify(mGoogleDriveSyncMetadata).setDatabaseSyncIdentifier(identifier);
    }

    @Test
    public void syncExistingDatabase() {
        final Identifier identifier = new Identifier("oldId");
        when(mGoogleDriveSyncMetadata.getDatabaseSyncIdentifier()).thenReturn(identifier);
        when(mDriveStreamsManager.updateDriveFile(identifier, mDatabaseFile)).thenReturn(Single.just(identifier));

        mDriveDatabaseManager.syncDatabase();
        verify(mGoogleDriveSyncMetadata).setDatabaseSyncIdentifier(identifier);
    }

    @Test
    public void syncDatabaseError() {
        final Exception e = new Exception();
        when(mDriveStreamsManager.uploadFileToDrive(mDatabaseFile)).thenReturn(Single.error(e));

        mDriveDatabaseManager.syncDatabase();
        verify(mGoogleDriveSyncMetadata, never()).setDatabaseSyncIdentifier(any(Identifier.class));
        verify(mAnalytics).record(any(ErrorEvent.class));
    }

}