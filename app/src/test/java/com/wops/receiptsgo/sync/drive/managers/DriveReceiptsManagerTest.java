package com.wops.receiptsgo.sync.drive.managers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import co.smartreceipts.analytics.Analytics;
import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.model.factory.ReceiptBuilderFactory;
import com.wops.receiptsgo.model.factory.ReceiptBuilderFactoryFactory;
import com.wops.receiptsgo.persistence.database.controllers.TableController;
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata;
import com.wops.receiptsgo.persistence.database.operations.OperationFamilyType;
import com.wops.receiptsgo.persistence.database.tables.ReceiptsTable;
import com.wops.receiptsgo.persistence.database.tables.TripsTable;
import co.smartreceipts.automatic_backups.drive.managers.DriveDatabaseManager;
import co.smartreceipts.automatic_backups.drive.rx.DriveStreamMappings;
import co.smartreceipts.automatic_backups.drive.rx.DriveStreamsManager;
import co.smartreceipts.core.sync.model.SyncState;
import co.smartreceipts.core.sync.model.impl.Identifier;
import com.wops.receiptsgo.sync.network.NetworkManager;
import co.smartreceipts.core.sync.provider.SyncProvider;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class DriveReceiptsManagerTest {

    private static final int RECEIPT_1_PRIMARY_KEY = 1;
    private static final int RECEIPT_2_PRIMARY_KEY = 2;

    // Class under testing
    DriveReceiptsManager driveReceiptsManager;

    @Mock
    TableController<Receipt> receiptTableController;

    @Mock
    TripsTable tripsTable;

    @Mock
    ReceiptsTable receiptsTable;

    @Mock
    DriveStreamsManager driveStreamsManager;

    @Mock
    DriveDatabaseManager driveDatabaseManager;

    @Mock
    NetworkManager networkManager;

    @Mock
    Analytics analytics;

    @Mock
    DriveStreamMappings driveStreamMappings;

    @Mock
    ReceiptBuilderFactoryFactory receiptBuilderFactoryFactory;

    @Mock
    ReceiptBuilderFactory receiptBuilderFactory1, receiptBuilderFactory2;

    @Mock
    Trip trip;

    @Mock
    Receipt receipt1, receipt2;

    @Mock
    SyncState syncState1, syncState2, newSyncState1, newSyncState2;

    @Mock
    File file;

    @Captor
    ArgumentCaptor<Receipt> receiptCaptor;

    @Captor
    ArgumentCaptor<Receipt> updatedReceiptCaptor;

    @Captor
    ArgumentCaptor<DatabaseOperationMetadata> operationMetadataCaptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(receipt1.getSyncState()).thenReturn(syncState1);
        when(receipt2.getSyncState()).thenReturn(syncState2);

        when(receiptBuilderFactory1.build()).thenReturn(receipt1);
        when(receiptBuilderFactory2.build()).thenReturn(receipt2);
        doAnswer(new Answer<ReceiptBuilderFactory>() {
            @Override
            public ReceiptBuilderFactory answer(InvocationOnMock invocation) throws Throwable {
                return receiptBuilderFactory1;
            }
        }).when(receiptBuilderFactoryFactory).build(receipt1);
        doAnswer(new Answer<ReceiptBuilderFactory>() {
            @Override
            public ReceiptBuilderFactory answer(InvocationOnMock invocation) throws Throwable {
                return receiptBuilderFactory2;
            }
        }).when(receiptBuilderFactoryFactory).build(receipt2);
        doAnswer(new Answer<ReceiptBuilderFactory>() {
            @Override
            public ReceiptBuilderFactory answer(InvocationOnMock invocation) throws Throwable {
                when(receipt1.getSyncState()).thenReturn((SyncState)invocation.getArguments()[0]);
                return receiptBuilderFactory1;
            }
        }).when(receiptBuilderFactory1).setSyncState(any(SyncState.class));
        doAnswer(new Answer<ReceiptBuilderFactory>() {
            @Override
            public ReceiptBuilderFactory answer(InvocationOnMock invocation) throws Throwable {
                when(receipt2.getSyncState()).thenReturn((SyncState)invocation.getArguments()[0]);
                return receiptBuilderFactory2;
            }
        }).when(receiptBuilderFactory2).setSyncState(any(SyncState.class));

        when(networkManager.isNetworkAvailable()).thenReturn(true);
        when(receipt1.getTrip()).thenReturn(trip);
        when(receipt2.getTrip()).thenReturn(trip);
        when(receipt1.getId()).thenReturn(RECEIPT_1_PRIMARY_KEY);
        when(receipt2.getId()).thenReturn(RECEIPT_2_PRIMARY_KEY);
        when(tripsTable.get()).thenReturn(Single.just(Collections.singletonList(trip)));
        when(receiptsTable.findByPrimaryKey(RECEIPT_1_PRIMARY_KEY)).thenReturn(Single.just(receipt1));
        when(receiptsTable.findByPrimaryKey(RECEIPT_2_PRIMARY_KEY)).thenReturn(Single.just(receipt2));

        driveReceiptsManager = new DriveReceiptsManager(receiptTableController, tripsTable, receiptsTable, driveStreamsManager,
                driveDatabaseManager, networkManager, analytics, driveStreamMappings, receiptBuilderFactoryFactory, Schedulers.trampoline(), Schedulers.trampoline());
    }

    @Test
    public void handleDelete() {
        when(driveStreamsManager.deleteDriveFile(syncState1, true)).thenReturn(Single.just(newSyncState1));
        when(syncState1.isSynced(SyncProvider.GoogleDrive)).thenReturn(false);
        when(syncState1.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(true);

        driveReceiptsManager.handleDelete(receipt1);

        verify(receiptTableController).delete(receiptCaptor.capture(), operationMetadataCaptor.capture());
        assertEquals(receipt1, receiptCaptor.getValue());
        assertEquals(newSyncState1, receiptCaptor.getValue().getSyncState());
        assertEquals(OperationFamilyType.Sync, operationMetadataCaptor.getValue().getOperationFamilyType());
    }

    @Test
    public void handleDeleteForStaleReceipt() {
        when(driveStreamsManager.deleteDriveFile(syncState1, true)).thenReturn(Single.just(newSyncState1));
        when(syncState1.isSynced(SyncProvider.GoogleDrive)).thenReturn(false);
        when(syncState1.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(true);
        when(receiptsTable.findByPrimaryKey(RECEIPT_1_PRIMARY_KEY)).thenReturn(Single.just(mock(Receipt.class)));

        driveReceiptsManager.handleDelete(receipt1);

        verify(receiptTableController, never()).delete(receiptCaptor.capture(), operationMetadataCaptor.capture());
    }

    @Test
    public void handleDeleteWithoutNetwork() {
        when(driveStreamsManager.deleteDriveFile(syncState1, true)).thenReturn(Single.just(newSyncState1));
        when(syncState1.isSynced(SyncProvider.GoogleDrive)).thenReturn(false);
        when(syncState1.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(true);
        when(networkManager.isNetworkAvailable()).thenReturn(false);

        driveReceiptsManager.handleDelete(receipt1);

        verify(receiptTableController, never()).delete(receiptCaptor.capture(), operationMetadataCaptor.capture());
    }

    @Test(expected = IllegalArgumentException.class)
    public void handleDeleteForIllegalSyncState() {
        when(syncState1.isSynced(SyncProvider.GoogleDrive)).thenReturn(true);
        driveReceiptsManager.handleDelete(receipt1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handleDeleteForIllegalMarkedForDeletionState() {
        when(syncState1.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(false);
        driveReceiptsManager.handleDelete(receipt1);
    }

    @Test
    public void handleInsertOrUpdateWithoutNetwork() {
        when(driveStreamsManager.uploadFileToDrive(syncState1, file)).thenReturn(Single.just(newSyncState1));
        when(syncState1.getSyncId(SyncProvider.GoogleDrive)).thenReturn(null);
        when(syncState1.isSynced(SyncProvider.GoogleDrive)).thenReturn(false);
        when(syncState1.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(false);
        when(receipt1.getFile()).thenReturn(file);
        when(file.exists()).thenReturn(true);
        when(networkManager.isNetworkAvailable()).thenReturn(false);

        driveReceiptsManager.handleInsertOrUpdate(receipt1);

        verify(receiptTableController, never()).update(receiptCaptor.capture(), updatedReceiptCaptor.capture(), operationMetadataCaptor.capture());
    }

    @Test
    public void handleInsertOrUpdateForNewFile() {
        when(driveStreamsManager.uploadFileToDrive(syncState1, file)).thenReturn(Single.just(newSyncState1));
        when(syncState1.getSyncId(SyncProvider.GoogleDrive)).thenReturn(null);
        when(syncState1.isSynced(SyncProvider.GoogleDrive)).thenReturn(false);
        when(syncState1.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(false);
        when(receipt1.getFile()).thenReturn(file);
        when(file.exists()).thenReturn(true);

        driveReceiptsManager.handleInsertOrUpdate(receipt1);

        verify(receiptTableController).update(receiptCaptor.capture(), updatedReceiptCaptor.capture(), operationMetadataCaptor.capture());
        assertNotNull(receiptCaptor.getValue());
        assertEquals(receipt1, updatedReceiptCaptor.getValue());
        assertEquals(newSyncState1, updatedReceiptCaptor.getValue().getSyncState());
        assertEquals(OperationFamilyType.Sync, operationMetadataCaptor.getValue().getOperationFamilyType());
    }

    @Test
    public void handleInsertOrUpdateForNewFileButStaleReceipt() {
        when(driveStreamsManager.uploadFileToDrive(syncState1, file)).thenReturn(Single.just(newSyncState1));
        when(syncState1.getSyncId(SyncProvider.GoogleDrive)).thenReturn(null);
        when(syncState1.isSynced(SyncProvider.GoogleDrive)).thenReturn(false);
        when(syncState1.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(false);
        when(receipt1.getFile()).thenReturn(file);
        when(file.exists()).thenReturn(true);
        when(receiptsTable.findByPrimaryKey(RECEIPT_1_PRIMARY_KEY)).thenReturn(Single.just(mock(Receipt.class)));

        driveReceiptsManager.handleInsertOrUpdate(receipt1);

        verify(receiptTableController, never()).update(receiptCaptor.capture(), updatedReceiptCaptor.capture(), operationMetadataCaptor.capture());
    }

    @Test
    public void handleInsertOrUpdateForNonExistingNewFile() {
        when(driveStreamMappings.postInsertSyncState()).thenReturn(newSyncState1);
        when(syncState1.getSyncId(SyncProvider.GoogleDrive)).thenReturn(null);
        when(syncState1.isSynced(SyncProvider.GoogleDrive)).thenReturn(false);
        when(syncState1.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(false);
        when(receipt1.getFile()).thenReturn(file);
        when(file.exists()).thenReturn(false);

        driveReceiptsManager.handleInsertOrUpdate(receipt1);

        verify(receiptTableController).update(receiptCaptor.capture(), updatedReceiptCaptor.capture(), operationMetadataCaptor.capture());
        assertNotNull(receiptCaptor.getValue());
        assertEquals(receipt1, updatedReceiptCaptor.getValue());
        assertEquals(newSyncState1, updatedReceiptCaptor.getValue().getSyncState());
        assertEquals(OperationFamilyType.Sync, operationMetadataCaptor.getValue().getOperationFamilyType());
    }

    @Test
    public void handleInsertOrUpdateForNonExistingNewFileButStaleReceipt() {
        when(driveStreamMappings.postInsertSyncState()).thenReturn(newSyncState1);
        when(syncState1.getSyncId(SyncProvider.GoogleDrive)).thenReturn(null);
        when(syncState1.isSynced(SyncProvider.GoogleDrive)).thenReturn(false);
        when(syncState1.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(false);
        when(receipt1.getFile()).thenReturn(file);
        when(file.exists()).thenReturn(false);
        when(receiptsTable.findByPrimaryKey(RECEIPT_1_PRIMARY_KEY)).thenReturn(Single.just(mock(Receipt.class)));

        driveReceiptsManager.handleInsertOrUpdate(receipt1);

        verify(receiptTableController, never()).update(receiptCaptor.capture(), updatedReceiptCaptor.capture(), operationMetadataCaptor.capture());
    }

    @Test
    public void handleInsertWithoutFile() {
        when(driveStreamMappings.postInsertSyncState()).thenReturn(newSyncState1);
        when(syncState1.getSyncId(SyncProvider.GoogleDrive)).thenReturn(null);
        when(syncState1.isSynced(SyncProvider.GoogleDrive)).thenReturn(false);
        when(syncState1.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(false);
        when(receipt1.getFile()).thenReturn(null);

        driveReceiptsManager.handleInsertOrUpdate(receipt1);

        verify(receiptTableController).update(receiptCaptor.capture(), updatedReceiptCaptor.capture(), operationMetadataCaptor.capture());
        assertNotNull(receiptCaptor.getValue());
        assertEquals(receipt1, updatedReceiptCaptor.getValue());
        assertEquals(newSyncState1, updatedReceiptCaptor.getValue().getSyncState());
        assertEquals(OperationFamilyType.Sync, operationMetadataCaptor.getValue().getOperationFamilyType());
    }

    @Test
    public void handleInsertWithoutFileButStaleReceipt() {
        when(driveStreamMappings.postInsertSyncState()).thenReturn(newSyncState1);
        when(syncState1.getSyncId(SyncProvider.GoogleDrive)).thenReturn(null);
        when(syncState1.isSynced(SyncProvider.GoogleDrive)).thenReturn(false);
        when(syncState1.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(false);
        when(receipt1.getFile()).thenReturn(null);
        when(receiptsTable.findByPrimaryKey(RECEIPT_1_PRIMARY_KEY)).thenReturn(Single.just(mock(Receipt.class)));

        driveReceiptsManager.handleInsertOrUpdate(receipt1);

        verify(receiptTableController, never()).update(receiptCaptor.capture(), updatedReceiptCaptor.capture(), operationMetadataCaptor.capture());
    }

    @Test
    public void handleUpdateWithNewFile() {
        final Identifier identifier = new Identifier("id");
        when(driveStreamsManager.updateDriveFile(syncState1, file)).thenReturn(Single.just(newSyncState1));
        when(syncState1.getSyncId(SyncProvider.GoogleDrive)).thenReturn(identifier);
        when(syncState1.isSynced(SyncProvider.GoogleDrive)).thenReturn(false);
        when(syncState1.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(false);
        when(receipt1.getFile()).thenReturn(file);
        when(file.exists()).thenReturn(true);

        driveReceiptsManager.handleInsertOrUpdate(receipt1);

        verify(receiptTableController).update(receiptCaptor.capture(), updatedReceiptCaptor.capture(), operationMetadataCaptor.capture());
        assertNotNull(receiptCaptor.getValue());
        assertEquals(receipt1, updatedReceiptCaptor.getValue());
        assertEquals(newSyncState1, updatedReceiptCaptor.getValue().getSyncState());
        assertEquals(OperationFamilyType.Sync, operationMetadataCaptor.getValue().getOperationFamilyType());
    }

    @Test
    public void handleUpdateWithNewFileButStaleReceipt() {
        final Identifier identifier = new Identifier("id");
        when(driveStreamsManager.updateDriveFile(syncState1, file)).thenReturn(Single.just(newSyncState1));
        when(syncState1.getSyncId(SyncProvider.GoogleDrive)).thenReturn(identifier);
        when(syncState1.isSynced(SyncProvider.GoogleDrive)).thenReturn(false);
        when(syncState1.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(false);
        when(receipt1.getFile()).thenReturn(file);
        when(file.exists()).thenReturn(true);
        when(receiptsTable.findByPrimaryKey(RECEIPT_1_PRIMARY_KEY)).thenReturn(Single.just(mock(Receipt.class)));

        driveReceiptsManager.handleInsertOrUpdate(receipt1);

        verify(receiptTableController, never()).update(receiptCaptor.capture(), updatedReceiptCaptor.capture(), operationMetadataCaptor.capture());
    }

    @Test
    public void handleUpdateToDeleteExistingFile() {
        final Identifier identifier = new Identifier("id");
        when(driveStreamsManager.deleteDriveFile(syncState1, false)).thenReturn(Single.just(newSyncState1));
        when(syncState1.getSyncId(SyncProvider.GoogleDrive)).thenReturn(identifier);
        when(syncState1.isSynced(SyncProvider.GoogleDrive)).thenReturn(false);
        when(syncState1.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(false);
        when(receipt1.getFile()).thenReturn(null);

        driveReceiptsManager.handleInsertOrUpdate(receipt1);

        verify(receiptTableController).update(receiptCaptor.capture(), updatedReceiptCaptor.capture(), operationMetadataCaptor.capture());
        assertNotNull(receiptCaptor.getValue());
        assertEquals(receipt1, updatedReceiptCaptor.getValue());
        assertEquals(newSyncState1, updatedReceiptCaptor.getValue().getSyncState());
        assertEquals(OperationFamilyType.Sync, operationMetadataCaptor.getValue().getOperationFamilyType());
    }

    @Test
    public void handleUpdateToDeleteExistingFileButStaleReceipt() {
        final Identifier identifier = new Identifier("id");
        when(driveStreamsManager.deleteDriveFile(syncState1, false)).thenReturn(Single.just(newSyncState1));
        when(syncState1.getSyncId(SyncProvider.GoogleDrive)).thenReturn(identifier);
        when(syncState1.isSynced(SyncProvider.GoogleDrive)).thenReturn(false);
        when(syncState1.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(false);
        when(receipt1.getFile()).thenReturn(null);
        when(receiptsTable.findByPrimaryKey(RECEIPT_1_PRIMARY_KEY)).thenReturn(Single.just(mock(Receipt.class)));

        driveReceiptsManager.handleInsertOrUpdate(receipt1);

        verify(receiptTableController, never()).update(receiptCaptor.capture(), updatedReceiptCaptor.capture(), operationMetadataCaptor.capture());
    }

    @Test(expected = IllegalArgumentException.class)
    public void handleInsertOrUpdateForIllegalSyncState() {
        when(syncState1.isSynced(SyncProvider.GoogleDrive)).thenReturn(true);
        driveReceiptsManager.handleInsertOrUpdate(receipt1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void handleInsertOrUpdateForIllegalMarkedForDeletionState() {
        when(syncState1.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(true);
        driveReceiptsManager.handleInsertOrUpdate(receipt1);
    }

    @Test
    public void initialize() {
        final DriveReceiptsManager spiedManager = spy(driveReceiptsManager);
        doNothing().when(spiedManager).handleInsertOrUpdateInternal(any(Receipt.class));
        doNothing().when(spiedManager).handleDeleteInternal(any(Receipt.class));

        when(syncState1.isSynced(SyncProvider.GoogleDrive)).thenReturn(false);
        when(syncState1.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(false);
        when(syncState2.isSynced(SyncProvider.GoogleDrive)).thenReturn(false);
        when(syncState2.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(true);
        when(receiptsTable.getUnsynced(trip, SyncProvider.GoogleDrive)).thenReturn(Single.just(Arrays.asList(receipt1, receipt2)));

        spiedManager.syncReceipts();

        verify(spiedManager).handleInsertOrUpdateInternal(receipt1);
        verify(spiedManager).handleDeleteInternal(receipt2);
        verify(driveDatabaseManager).syncDatabase();
    }

    @Test
    public void initializeWithoutNetwork() {
        final DriveReceiptsManager spiedManager = spy(driveReceiptsManager);
        doNothing().when(spiedManager).handleInsertOrUpdateInternal(any(Receipt.class));
        doNothing().when(spiedManager).handleDeleteInternal(any(Receipt.class));

        when(syncState1.isSynced(SyncProvider.GoogleDrive)).thenReturn(false);
        when(syncState1.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(false);
        when(syncState2.isSynced(SyncProvider.GoogleDrive)).thenReturn(false);
        when(syncState2.isMarkedForDeletion(SyncProvider.GoogleDrive)).thenReturn(true);
        when(receiptsTable.getUnsynced(trip, SyncProvider.GoogleDrive)).thenReturn(Single.just(Arrays.asList(receipt1, receipt2)));
        when(networkManager.isNetworkAvailable()).thenReturn(false);

        spiedManager.syncReceipts();

        verify(spiedManager, never()).handleInsertOrUpdateInternal(receipt1);
        verify(spiedManager, never()).handleDeleteInternal(receipt2);
        verify(driveDatabaseManager, never()).syncDatabase();
    }

}