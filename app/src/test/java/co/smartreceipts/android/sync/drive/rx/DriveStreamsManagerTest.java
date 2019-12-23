package co.smartreceipts.android.sync.drive.rx;

import android.os.Bundle;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.hadisatrio.optional.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.Collections;
import java.util.List;

import co.smartreceipts.android.sync.drive.error.DriveThrowableToSyncErrorTranslator;
import co.smartreceipts.android.sync.model.RemoteBackupMetadata;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.core.sync.model.impl.Identifier;
import co.smartreceipts.android.sync.provider.SyncProvider;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.Subject;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class DriveStreamsManagerTest {

    // Class under test
    DriveStreamsManager driveStreamsManager;

    @Mock
    DriveDataStreams driveDataStreams;

    @Mock
    DriveStreamMappings driveStreamMappings;

    @Mock
    Subject<Optional<Throwable>> driveErrorStream;

    @Mock
    DriveThrowableToSyncErrorTranslator syncErrorTranslator;

    @Mock
    RemoteBackupMetadata remoteBackupMetadata;

    @Mock
    File driveFile;

    @Mock
    FileList fileList;
    
    @Mock
    Identifier identifier;

    @Mock
    Throwable criticalSyncError;

    @Mock
    SyncState currentSyncState, newSyncState;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(syncErrorTranslator.get(any(Throwable.class))).thenReturn(criticalSyncError);
        driveStreamsManager = new DriveStreamsManager(driveDataStreams, driveStreamMappings, driveErrorStream, syncErrorTranslator);
        driveStreamsManager.onConnected(new Bundle());
    }

    @Test
    public void getRemoteBackups() throws Exception {
        final List<RemoteBackupMetadata> remoteBackups = Collections.singletonList(remoteBackupMetadata);
        when(driveDataStreams.getSmartReceiptsFolders()).thenReturn(Single.just(remoteBackups));

        final TestObserver<List<RemoteBackupMetadata>> testObserver = driveStreamsManager.getRemoteBackups().test();
        testObserver.assertValue(remoteBackups);
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void getRemoteBackupsErrorIsForwarded() throws Exception {
        final Exception exception = new Exception("test");
        when(driveDataStreams.getSmartReceiptsFolders()).thenReturn(Single.error(exception));

        final TestObserver<List<RemoteBackupMetadata>> testObserver = driveStreamsManager.getRemoteBackups().test();
        testObserver.assertNoValues();
        testObserver.assertNotComplete();
        testObserver.assertError(Exception.class);
        verify(driveErrorStream).onNext(Optional.of(criticalSyncError));
    }

    @Test
    public void getFilesInFolder() throws Exception {
        when(driveDataStreams.getFilesInFolder(anyString())).thenReturn(Single.just(fileList));

        final TestObserver<FileList> testObserver = driveStreamsManager.getFilesInFolder(anyString()).test();
        testObserver.assertValue(fileList);
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void getFilesInFolderErrorIsForwarded() throws Exception {
        final Exception exception = new Exception("test");
        when(driveDataStreams.getFilesInFolder(anyString())).thenReturn(Single.error(exception));

        final TestObserver<FileList> testObserver = driveStreamsManager.getFilesInFolder(anyString()).test();
        testObserver.assertNoValues();
        testObserver.assertNotComplete();
        testObserver.assertError(Exception.class);
        verify(driveErrorStream).onNext(Optional.of(criticalSyncError));
    }

    @Test
    public void getFilesInFolderForName() throws Exception {
        final String filename = "filename";
        final String folderId = "folderId";

        when(driveDataStreams.getFilesInFolder(folderId, filename)).thenReturn(Single.just(fileList));

        final TestObserver<FileList> testObserver = driveStreamsManager.getFilesInFolder(folderId, filename).test();
        testObserver.assertValue(fileList);
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void getFilesInFolderErrorIsForwardedForName() throws Exception {
        final String filename = "filename";
        final String folderId = "folderId";
        final Exception exception = new Exception("test");
        when(driveDataStreams.getFilesInFolder(folderId, filename)).thenReturn(Single.error(exception));

        final TestObserver<FileList> testObserver = driveStreamsManager.getFilesInFolder(folderId, filename).test();
        testObserver.assertNoValues();
        testObserver.assertNotComplete();
        testObserver.assertError(Exception.class);
        verify(driveErrorStream).onNext(Optional.of(criticalSyncError));
    }

    @Test
    public void getMetadata() throws Exception {
        when(driveDataStreams.getMetadata(anyString())).thenReturn(Single.just(driveFile));

        final TestObserver<File> testObserver = driveStreamsManager.getMetadata(anyString()).test();
        testObserver.assertValue(driveFile);
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void getMetadataErrorIsForwarded() throws Exception {
        final Exception exception = new Exception("test");
        when(driveDataStreams.getMetadata(anyString())).thenReturn(Single.error(exception));

        final TestObserver<File> testObserver = driveStreamsManager.getMetadata(anyString()).test();
        testObserver.assertNoValues();
        testObserver.assertNotComplete();
        testObserver.assertError(Exception.class);
        verify(driveErrorStream).onNext(Optional.of(criticalSyncError));
    }

    @Test
    public void uploadFileToDriveWithSyncState() throws Exception {
        final java.io.File file = new java.io.File("/");
        when(driveDataStreams.getSmartReceiptsFolder()).thenReturn(Observable.just(driveFile));
        when(driveDataStreams.createFileInFolder(driveFile, file)).thenReturn(Single.just(driveFile));
        when(driveStreamMappings.postInsertSyncState(currentSyncState, driveFile)).thenReturn(newSyncState);

        final TestObserver<SyncState> testObserver = driveStreamsManager.uploadFileToDrive(currentSyncState, file).test();
        testObserver.assertValue(newSyncState);
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void uploadFileToDriveWithSyncStateErrorIsForwarded() throws Exception {
        final java.io.File file = new java.io.File("/");
        final Exception exception = new Exception("test");
        when(driveDataStreams.getSmartReceiptsFolder()).thenReturn(Observable.error(exception));

        final TestObserver<SyncState> testObserver = driveStreamsManager.uploadFileToDrive(currentSyncState, file).test();
        testObserver.assertNoValues();
        testObserver.assertNotComplete();
        testObserver.assertError(Exception.class);
        verify(driveErrorStream).onNext(Optional.of(criticalSyncError));
    }

    @Test
    public void uploadFileToDrive() throws Exception {
        final String id = "id";
        final java.io.File file = new java.io.File("/");
        when(driveDataStreams.getSmartReceiptsFolder()).thenReturn(Observable.just(driveFile));
        when(driveDataStreams.createFileInFolder(driveFile, file)).thenReturn(Single.just(driveFile));
        when(driveFile.getId()).thenReturn(id);

        final TestObserver<Identifier> testObserver = driveStreamsManager.uploadFileToDrive(file).test();
        testObserver.assertValue(new Identifier(id));
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void uploadFileToDriveErrorIsForwarded() throws Exception {
        final java.io.File file = new java.io.File("/");
        final Exception exception = new Exception("test");
        when(driveDataStreams.getSmartReceiptsFolder()).thenReturn(Observable.error(exception));

        final TestObserver<Identifier> testObserver = driveStreamsManager.uploadFileToDrive(file).test();
        testObserver.assertNoValues();
        testObserver.assertNotComplete();
        testObserver.assertError(Exception.class);
        verify(driveErrorStream).onNext(Optional.of(criticalSyncError));
    }

    @Test
    public void updateDriveFileWithSyncState() throws Exception {
        final java.io.File file = new java.io.File("/");
        when(currentSyncState.getSyncId(SyncProvider.GoogleDrive)).thenReturn(identifier);
        when(driveDataStreams.updateFile(identifier, file)).thenReturn(Single.just(driveFile));
        when(driveStreamMappings.postUpdateSyncState(currentSyncState, driveFile)).thenReturn(newSyncState);

        final TestObserver<SyncState> testObserver = driveStreamsManager.updateDriveFile(currentSyncState, file).test();
        testObserver.assertValue(newSyncState);
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void updateDriveFileWithSyncStateWithoutIdentifier() throws Exception {
        final java.io.File file = new java.io.File("/");
        when(currentSyncState.getSyncId(SyncProvider.GoogleDrive)).thenReturn(null);

        final TestObserver<SyncState> testObserver = driveStreamsManager.updateDriveFile(currentSyncState, file).test();
        testObserver.assertNoValues();
        testObserver.assertNotComplete();
        testObserver.assertError(Exception.class);
        verify(driveErrorStream).onNext(Optional.of(criticalSyncError));
    }

    @Test
    public void updateDriveFileWithIdentifier() throws Exception {
        final java.io.File file = new java.io.File("/");
        final String id = "id";
        when(driveDataStreams.updateFile(identifier, file)).thenReturn(Single.just(driveFile));
        when(driveFile.getId()).thenReturn(id);

        final TestObserver<Identifier> testObserver = driveStreamsManager.updateDriveFile(identifier, file).test();
        testObserver.assertValue(new Identifier(id));
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void updateDriveFileWithIdentifierErrorIsForwarded() throws Exception {
        final java.io.File file = new java.io.File("/");
        final Exception exception = new Exception("test");
        when(driveDataStreams.updateFile(identifier, file)).thenReturn(Single.error(exception));

        final TestObserver<Identifier> testObserver = driveStreamsManager.updateDriveFile(identifier, file).test();
        testObserver.assertNoValues();
        testObserver.assertNotComplete();
        testObserver.assertError(Exception.class);
        verify(driveErrorStream).onNext(Optional.of(criticalSyncError));
    }

    @Test
    public void deleteDriveFileWithIdentifierSuccessfully() throws Exception {
        when(currentSyncState.getSyncId(SyncProvider.GoogleDrive)).thenReturn(identifier);
        when(driveDataStreams.delete(identifier)).thenReturn(Single.just(true));
        when(driveStreamMappings.postDeleteSyncState(currentSyncState, true)).thenReturn(newSyncState);

        final TestObserver<SyncState> testObserver = driveStreamsManager.deleteDriveFile(currentSyncState, true).test();
        testObserver.assertValue(newSyncState);
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void deleteDriveFileWithoutIdentifierSuccessfully() throws Exception {
        when(currentSyncState.getSyncId(SyncProvider.GoogleDrive)).thenReturn(identifier);
        when(driveDataStreams.delete(identifier)).thenReturn(Single.just(true));
        when(driveStreamMappings.postDeleteSyncState(currentSyncState, true)).thenReturn(newSyncState);

        final TestObserver<SyncState> testObserver = driveStreamsManager.deleteDriveFile(currentSyncState, true).test();
        testObserver.assertValue(newSyncState);
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void deleteDriveFileWithIdentifierFailsSoWeReturnTheOriginalState() throws Exception {
        when(currentSyncState.getSyncId(SyncProvider.GoogleDrive)).thenReturn(identifier);
        when(driveDataStreams.delete(identifier)).thenReturn(Single.just(false));
        when(driveStreamMappings.postDeleteSyncState(currentSyncState, true)).thenReturn(newSyncState);

        final TestObserver<SyncState> testObserver = driveStreamsManager.deleteDriveFile(currentSyncState, true).test();
        testObserver.assertValue(currentSyncState);
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void deleteDriveFileErrorIsForwarded() throws Exception {
        final Exception exception = new Exception("test");
        when(currentSyncState.getSyncId(SyncProvider.GoogleDrive)).thenReturn(identifier);
        when(driveDataStreams.delete(identifier)).thenReturn(Single.error(exception));

        final TestObserver<SyncState> testObserver = driveStreamsManager.deleteDriveFile(currentSyncState, true).test();
        testObserver.assertNoValues();
        testObserver.assertNotComplete();
        testObserver.assertError(Exception.class);
        verify(driveErrorStream).onNext(Optional.of(criticalSyncError));
    }

    @Test
    public void delete() throws Exception {
        when(driveDataStreams.delete(identifier)).thenReturn(Single.just(true));

        final TestObserver<Boolean> testObserver = driveStreamsManager.delete(identifier).test();
        testObserver.assertValue(true);
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void deleteErrorIsForwarded() throws Exception {
        final Exception exception = new Exception("test");
        when(driveDataStreams.delete(identifier)).thenReturn(Single.error(exception));

        final TestObserver<Boolean> testObserver = driveStreamsManager.delete(identifier).test();
        testObserver.assertNoValues();
        testObserver.assertNotComplete();
        testObserver.assertError(Exception.class);
        verify(driveErrorStream).onNext(Optional.of(criticalSyncError));
    }

    @Test
    public void clearCachedData() throws Exception {
        driveStreamsManager.clearCachedData();
        verify(driveDataStreams).clear();
    }

    @Test
    public void download() throws Exception {
        final String fileId = "fileId";
        final java.io.File file = new java.io.File("/");
        when(driveDataStreams.download(fileId, file)).thenReturn(Single.just(file));

        final TestObserver<java.io.File> testObserver = driveStreamsManager.download(fileId, file).test();
        testObserver.assertValue(file);
        testObserver.assertComplete();
        testObserver.assertNoErrors();
    }

    @Test
    public void downloadErrorIsForwarded() throws Exception {
        final String fileId = "fileId";
        final Exception exception = new Exception("test");
        final java.io.File file = new java.io.File("/");
        when(driveDataStreams.download(fileId, file)).thenReturn(Single.error(exception));

        final TestObserver<java.io.File> testObserver = driveStreamsManager.download(fileId, file).test();
        testObserver.assertNoValues();
        testObserver.assertNotComplete();
        testObserver.assertError(Exception.class);
        verify(driveErrorStream).onNext(Optional.of(criticalSyncError));
    }

}