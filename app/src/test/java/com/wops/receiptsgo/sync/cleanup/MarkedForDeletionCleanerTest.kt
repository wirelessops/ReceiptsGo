package com.wops.receiptsgo.sync.cleanup

import com.wops.receiptsgo.model.Receipt
import com.wops.receiptsgo.persistence.database.controllers.impl.ReceiptTableController
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata
import com.wops.receiptsgo.persistence.database.operations.OperationFamilyType
import com.wops.receiptsgo.persistence.database.tables.ReceiptsTable
import com.wops.core.sync.provider.SyncProvider
import com.wops.receiptsgo.sync.provider.SyncProviderStore
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import java.util.*

@RunWith(RobolectricTestRunner::class)
class MarkedForDeletionCleanerTest {

    private lateinit var markedForDeletionCleaner: MarkedForDeletionCleaner

    @Mock
    private lateinit var receiptsTable: ReceiptsTable

    @Mock
    private lateinit var receiptTableController: ReceiptTableController

    @Mock
    private lateinit var syncProviderStore: SyncProviderStore

    @Mock
    private lateinit var receipt1: Receipt

    @Mock
    private lateinit var receipt2: Receipt

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        whenever(receiptsTable.getAllMarkedForDeletionItems(SyncProvider.GoogleDrive)).thenReturn(Single.just(Arrays.asList(receipt1, receipt2)))

        markedForDeletionCleaner = MarkedForDeletionCleaner(receiptsTable, receiptTableController, syncProviderStore, Schedulers.trampoline())
    }

    @Test
    fun deleteAllMarkedItemsWhenConfiguredForGoogleDriveDoesNothing() {
        whenever(syncProviderStore.provider).thenReturn(SyncProvider.GoogleDrive)
        markedForDeletionCleaner.safelyDeleteAllOutstandingItems()
        verifyZeroInteractions(receiptTableController)
    }

    @Test
    fun deleteAllMarkedItemsWhenConfiguredForNoneDoesDeletesMarkedItems() {
        whenever(syncProviderStore.provider).thenReturn(SyncProvider.None)
        markedForDeletionCleaner.safelyDeleteAllOutstandingItems()
        verify(receiptTableController).delete(receipt1, DatabaseOperationMetadata(OperationFamilyType.Sync))
        verify(receiptTableController).delete(receipt2, DatabaseOperationMetadata(OperationFamilyType.Sync))
    }
}