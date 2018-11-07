package co.smartreceipts.android.receipts.ordering

import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory
import co.smartreceipts.android.model.factory.TripBuilderFactory
import co.smartreceipts.android.persistence.database.controllers.impl.ReceiptTableController
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType
import co.smartreceipts.android.persistence.database.tables.ordering.OrderingPreferencesManager
import co.smartreceipts.android.sync.model.impl.DefaultSyncState
import com.hadisatrio.optional.Optional
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import junit.framework.Assert.assertEquals

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import java.util.*

@RunWith(RobolectricTestRunner::class)
class ReceiptsOrdererTest {

    /**
     * Note: This lists below have been defined such that each item maps to a corresponding "ordered"
     * receipt. For instance:
     *  - [NO_ORDERING_RECEIPT_1] => [ORDERED_RECEIPT_1]
     *  - [LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_1] => [ORDERED_RECEIPT_1]
     *  - [PARTIALLY_ORDERED_RECEIPT_1] => [ORDERED_RECEIPT_1]
     *
     * And for the "other" set, they map as:
     *
     *  - [OTHER_PARTIALLY_ORDERED_RECEIPT_1] => [OTHER_ORDERED_RECEIPT_1]
     *
     * Furthermore, each item is sorted in descending (by date / customOrderId) order so that item 1
     * will be return by the #get operation ahead of others (i.e. 1 > 2 > 3 > 4).
     */
    companion object {
        private val TRIP = TripBuilderFactory().build()

        // For items that have no ordering (i.e. customOrderId == 0)
        private val NO_ORDERING_RECEIPT_1 = ReceiptBuilderFactory().setTrip(TRIP).setDate(1513580400148L).setCustomOrderId(0L).build()
        private val NO_ORDERING_RECEIPT_2 = ReceiptBuilderFactory().setTrip(TRIP).setDate(1512778546147L).setCustomOrderId(0L).build()
        private val NO_ORDERING_RECEIPT_3 = ReceiptBuilderFactory().setTrip(TRIP).setDate(1512778546146L).setCustomOrderId(0L).build()
        private val NO_ORDERING_RECEIPT_4 = ReceiptBuilderFactory().setTrip(TRIP).setDate(1512778546145L).setCustomOrderId(0L).build()

        // For items that have the legacy ordering (i.e. customOrderId == date)
        private val LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_1 = ReceiptBuilderFactory().setTrip(TRIP).setDate(1513580400148L).setCustomOrderId(1513580400148L).build()
        private val LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_2 = ReceiptBuilderFactory().setTrip(TRIP).setDate(1512778546147L).setCustomOrderId(1512778546147L).build()
        private val LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_3 = ReceiptBuilderFactory().setTrip(TRIP).setDate(1512778546146L).setCustomOrderId(1512778546146L).build()
        private val LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_4 = ReceiptBuilderFactory().setTrip(TRIP).setDate(1512778546145L).setCustomOrderId(1512778546145L).build()

        // For items that have the partial ordering (i.e. customOrderId endWith 999)
        // Note: We set the names for `PARTIALLY_ORDERED_RECEIPT_1` and `PARTIALLY_ORDERED_RECEIPT_4`, since they are never updated
        // Without this hack, `PARTIALLY_ORDERED_RECEIPT_1` == `ORDERED_RECEIPT_1`, which causes the tests to fail
        private val PARTIALLY_ORDERED_RECEIPT_1 = ReceiptBuilderFactory().setName("1").setTrip(TRIP).setDate(1512778546145L).setCustomOrderId(17509000L).setSyncState(DefaultSyncState(java.sql.Date(1540327972705L))).build()
        private val PARTIALLY_ORDERED_RECEIPT_2 = ReceiptBuilderFactory().setTrip(TRIP).setDate(1512778546147L).setCustomOrderId(17509999L).setSyncState(DefaultSyncState(java.sql.Date(1540327944477L))).build()
        private val PARTIALLY_ORDERED_RECEIPT_3 = ReceiptBuilderFactory().setTrip(TRIP).setDate(1512778546146L).setCustomOrderId(17509999L).setSyncState(DefaultSyncState(java.sql.Date(1540327944476L))).build()
        private val PARTIALLY_ORDERED_RECEIPT_4 = ReceiptBuilderFactory().setName("1").setTrip(TRIP).setDate(1513580400148L).setCustomOrderId(17518000L).setSyncState(DefaultSyncState(java.sql.Date(1540327972705L))).build()

        // The properly ordered representations of each of the items above
        private val ORDERED_RECEIPT_1 = ReceiptBuilderFactory().setTrip(TRIP).setDate(1513580400148L).setCustomOrderId(17518000L).build()
        private val ORDERED_RECEIPT_2 = ReceiptBuilderFactory().setTrip(TRIP).setDate(1512778546147L).setCustomOrderId(17509002L).build()
        private val ORDERED_RECEIPT_3 = ReceiptBuilderFactory().setTrip(TRIP).setDate(1512778546146L).setCustomOrderId(17509001L).build()
        private val ORDERED_RECEIPT_4 = ReceiptBuilderFactory().setTrip(TRIP).setDate(1512778546145L).setCustomOrderId(17509000L).build()

        /** Our second test data set is shown below **/

        // For items that have a different type of partial ordering (i.e. custom order id are equal)
        // Note: We set the names for `OTHER_PARTIALLY_ORDERED_RECEIPT_1` and `OTHER_PARTIALLY_ORDERED_RECEIPT_4`, since they are never updated
        // Without this hack, `OTHER_PARTIALLY_ORDERED_RECEIPT_1` == `OTHER_ORDERED_RECEIPT_1`, which causes the tests to fail
        private val OTHER_PARTIALLY_ORDERED_RECEIPT_1 = ReceiptBuilderFactory().setName("1").setTrip(TRIP).setDate(1512778546125L).setCustomOrderId(17509000L).setSyncState(DefaultSyncState(java.sql.Date(1540327972705L))).build()
        private val OTHER_PARTIALLY_ORDERED_RECEIPT_2 = ReceiptBuilderFactory().setName("2").setTrip(TRIP).setDate(1512778546125L).setCustomOrderId(17509004L).setSyncState(DefaultSyncState(java.sql.Date(1540327944427L))).build()
        private val OTHER_PARTIALLY_ORDERED_RECEIPT_3 = ReceiptBuilderFactory().setName("3").setTrip(TRIP).setDate(1512778546125L).setCustomOrderId(17509004L).setSyncState(DefaultSyncState(java.sql.Date(1540327944426L))).build()
        private val OTHER_PARTIALLY_ORDERED_RECEIPT_4 = ReceiptBuilderFactory().setName("4").setTrip(TRIP).setDate(1513580400128L).setCustomOrderId(17518000L).setSyncState(DefaultSyncState(java.sql.Date(1540327972705L))).build()

        // The properly ordered representations of each of the items above
        private val OTHER_ORDERED_RECEIPT_1 = ReceiptBuilderFactory().setName("1").setTrip(TRIP).setDate(1513580400128L).setCustomOrderId(17518000L).build()
        private val OTHER_ORDERED_RECEIPT_2 = ReceiptBuilderFactory().setName("2").setTrip(TRIP).setDate(1512778546125L).setCustomOrderId(17509002L).build()
        private val OTHER_ORDERED_RECEIPT_3 = ReceiptBuilderFactory().setName("3").setTrip(TRIP).setDate(1512778546125L).setCustomOrderId(17509001L).build()
        private val OTHER_ORDERED_RECEIPT_4 = ReceiptBuilderFactory().setName("4").setTrip(TRIP).setDate(1512778546125L).setCustomOrderId(17509000L).build()
    }

    lateinit var receiptsOrderer: ReceiptsOrderer

    @Mock
    lateinit var tripTableController: TripTableController

    @Mock
    lateinit var receiptTableController: ReceiptTableController

    @Mock
    lateinit var orderingMigrationStore: ReceiptsOrderingMigrationStore

    @Mock
    lateinit var orderingPreferencesManager: OrderingPreferencesManager

    @Mock
    lateinit var unorderedTrip: Trip

    @Mock
    lateinit var legacyOrderedTrip: Trip

    @Mock
    lateinit var partiallyOrderedTrip: Trip

    @Mock
    lateinit var otherPartiallyOrderedTrip: Trip

    @Mock
    lateinit var orderedTrip: Trip

    @Mock
    lateinit var otherOrderedTrip: Trip

    @Mock
    lateinit var receipt: Receipt

    @Mock
    lateinit var updatedReceipt1: Receipt

    @Mock
    lateinit var updatedReceipt2: Receipt

    @Mock
    lateinit var updatedReceipt3: Receipt

    @Mock
    lateinit var updatedReceipt4: Receipt

    @Mock
    lateinit var updatedReceipt5: Receipt

    @Mock
    lateinit var updatedReceipt6: Receipt

    @Mock
    lateinit var updatedReceipt7: Receipt

    @Mock
    lateinit var updatedReceipt8: Receipt

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(orderingMigrationStore.getMigrationVersion()).thenReturn(Single.just(ReceiptsOrderingMigrationStore.MigrationVersion.NotMigrated))
        whenever(tripTableController.get()).thenReturn(Single.just(Arrays.asList(unorderedTrip, legacyOrderedTrip, partiallyOrderedTrip, orderedTrip, otherPartiallyOrderedTrip, otherOrderedTrip)))
        whenever(receiptTableController.get(unorderedTrip)).thenReturn(Single.just(Arrays.asList(NO_ORDERING_RECEIPT_1, NO_ORDERING_RECEIPT_2, NO_ORDERING_RECEIPT_3, NO_ORDERING_RECEIPT_4)))
        whenever(receiptTableController.get(legacyOrderedTrip)).thenReturn(Single.just(Arrays.asList(ReceiptsOrdererTest.LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_1, LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_2, LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_3, LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_4)))
        whenever(receiptTableController.get(partiallyOrderedTrip)).thenReturn(Single.just(Arrays.asList(PARTIALLY_ORDERED_RECEIPT_1, PARTIALLY_ORDERED_RECEIPT_2, PARTIALLY_ORDERED_RECEIPT_3, PARTIALLY_ORDERED_RECEIPT_4)))
        whenever(receiptTableController.get(orderedTrip)).thenReturn(Single.just(Arrays.asList(ORDERED_RECEIPT_1, ORDERED_RECEIPT_2, ORDERED_RECEIPT_3, ORDERED_RECEIPT_4)))

        // Second test data set
        whenever(receiptTableController.get(otherPartiallyOrderedTrip)).thenReturn(Single.just(Arrays.asList(OTHER_PARTIALLY_ORDERED_RECEIPT_1, OTHER_PARTIALLY_ORDERED_RECEIPT_2, OTHER_PARTIALLY_ORDERED_RECEIPT_3, OTHER_PARTIALLY_ORDERED_RECEIPT_4)))
        whenever(receiptTableController.get(otherOrderedTrip)).thenReturn(Single.just(Arrays.asList(OTHER_ORDERED_RECEIPT_1, OTHER_ORDERED_RECEIPT_2, OTHER_ORDERED_RECEIPT_3, OTHER_ORDERED_RECEIPT_4)))

        // Note: Stub return here to keep the flow working
        whenever(receiptTableController.update(any(), any(), any())).thenReturn(Observable.just(Optional.of(receipt)))
        whenever(receiptTableController.update(eq(ORDERED_RECEIPT_1), any(), any())).thenReturn(Observable.just(Optional.of(updatedReceipt1)))
        whenever(receiptTableController.update(eq(ORDERED_RECEIPT_2), any(), any())).thenReturn(Observable.just(Optional.of(updatedReceipt2)))
        whenever(receiptTableController.update(eq(ORDERED_RECEIPT_3), any(), any())).thenReturn(Observable.just(Optional.of(updatedReceipt3)))
        whenever(receiptTableController.update(eq(ORDERED_RECEIPT_4), any(), any())).thenReturn(Observable.just(Optional.of(updatedReceipt4)))
        whenever(receiptTableController.update(eq(OTHER_ORDERED_RECEIPT_1), any(), any())).thenReturn(Observable.just(Optional.of(updatedReceipt5)))
        whenever(receiptTableController.update(eq(OTHER_ORDERED_RECEIPT_2), any(), any())).thenReturn(Observable.just(Optional.of(updatedReceipt6)))
        whenever(receiptTableController.update(eq(OTHER_ORDERED_RECEIPT_3), any(), any())).thenReturn(Observable.just(Optional.of(updatedReceipt7)))
        whenever(receiptTableController.update(eq(OTHER_ORDERED_RECEIPT_4), any(), any())).thenReturn(Observable.just(Optional.of(updatedReceipt8)))

        receiptsOrderer = ReceiptsOrderer(tripTableController, receiptTableController, orderingMigrationStore, orderingPreferencesManager, Schedulers.trampoline())
    }

    @Test
    fun initializeWhenWeHavePreviouslyMigratedToV2() {
        whenever(orderingMigrationStore.getMigrationVersion()).thenReturn(Single.just(ReceiptsOrderingMigrationStore.MigrationVersion.V2))

        receiptsOrderer.initialize()

        verify(orderingMigrationStore, never()).setOrderingMigrationHasOccurred(any())
        verifyZeroInteractions(receiptTableController, tripTableController, orderingPreferencesManager)
    }

    @Test
    fun initializeWhenWeHaveNotPreviouslyMigrated() {
        whenever(orderingMigrationStore.getMigrationVersion()).thenReturn(Single.just(ReceiptsOrderingMigrationStore.MigrationVersion.NotMigrated))
        receiptsOrderer.initialize()
        verify(receiptTableController).update(eq(NO_ORDERING_RECEIPT_1), eq(ORDERED_RECEIPT_1), any())
        verify(receiptTableController).update(eq(NO_ORDERING_RECEIPT_2), eq(ORDERED_RECEIPT_2), any())
        verify(receiptTableController).update(eq(NO_ORDERING_RECEIPT_3), eq(ORDERED_RECEIPT_3), any())
        verify(receiptTableController).update(eq(NO_ORDERING_RECEIPT_4), eq(ORDERED_RECEIPT_4), any())
        verify(receiptTableController).update(eq(LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_1), eq(ORDERED_RECEIPT_1), any())
        verify(receiptTableController).update(eq(LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_2), eq(ORDERED_RECEIPT_2), any())
        verify(receiptTableController).update(eq(LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_3), eq(ORDERED_RECEIPT_3), any())
        verify(receiptTableController).update(eq(LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_4), eq(ORDERED_RECEIPT_4), any())
        verify(receiptTableController, never()).update(eq(PARTIALLY_ORDERED_RECEIPT_1), any(), any())
        verify(receiptTableController).update(eq(PARTIALLY_ORDERED_RECEIPT_2), eq(ORDERED_RECEIPT_2), any())
        verify(receiptTableController).update(eq(PARTIALLY_ORDERED_RECEIPT_3), eq(ORDERED_RECEIPT_3), any())
        verify(receiptTableController, never()).update(eq(PARTIALLY_ORDERED_RECEIPT_4), any(), any())
        verify(receiptTableController, never()).update(eq(ORDERED_RECEIPT_1), any(), any())
        verify(receiptTableController, never()).update(eq(ORDERED_RECEIPT_2), any(), any())
        verify(receiptTableController, never()).update(eq(ORDERED_RECEIPT_3), any(), any())
        verify(receiptTableController, never()).update(eq(ORDERED_RECEIPT_4), any(), any())
        verify(receiptTableController, never()).update(eq(OTHER_PARTIALLY_ORDERED_RECEIPT_1), any(), any())
        verify(receiptTableController).update(eq(OTHER_PARTIALLY_ORDERED_RECEIPT_2), eq(OTHER_ORDERED_RECEIPT_2), any())
        verify(receiptTableController).update(eq(OTHER_PARTIALLY_ORDERED_RECEIPT_3), eq(OTHER_ORDERED_RECEIPT_3), any())
        verify(receiptTableController, never()).update(eq(OTHER_PARTIALLY_ORDERED_RECEIPT_4), any(), any())
        verify(receiptTableController, never()).update(eq(OTHER_ORDERED_RECEIPT_1), any(), any())
        verify(receiptTableController, never()).update(eq(OTHER_ORDERED_RECEIPT_2), any(), any())
        verify(receiptTableController, never()).update(eq(OTHER_ORDERED_RECEIPT_3), any(), any())
        verify(receiptTableController, never()).update(eq(OTHER_ORDERED_RECEIPT_4), any(), any())
        verify(orderingMigrationStore).setOrderingMigrationHasOccurred(true)
        verify(orderingPreferencesManager).saveReceiptsTableOrdering()
    }

    @Test
    fun initializeWhenWeHavePreviouslyMigratedToV1() {
        whenever(orderingMigrationStore.getMigrationVersion()).thenReturn(Single.just(ReceiptsOrderingMigrationStore.MigrationVersion.V1))
        receiptsOrderer.initialize()
        verify(receiptTableController).update(eq(NO_ORDERING_RECEIPT_4), eq(ORDERED_RECEIPT_4), any())
        verify(receiptTableController).update(eq(NO_ORDERING_RECEIPT_3), eq(ORDERED_RECEIPT_3), any())
        verify(receiptTableController).update(eq(NO_ORDERING_RECEIPT_2), eq(ORDERED_RECEIPT_2), any())
        verify(receiptTableController).update(eq(NO_ORDERING_RECEIPT_1), eq(ORDERED_RECEIPT_1), any())
        verify(receiptTableController).update(eq(LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_4), eq(ORDERED_RECEIPT_4), any())
        verify(receiptTableController).update(eq(LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_3), eq(ORDERED_RECEIPT_3), any())
        verify(receiptTableController).update(eq(LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_2), eq(ORDERED_RECEIPT_2), any())
        verify(receiptTableController).update(eq(LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_1), eq(ORDERED_RECEIPT_1), any())
        verify(receiptTableController, never()).update(eq(PARTIALLY_ORDERED_RECEIPT_1), any(), any())
        verify(receiptTableController).update(eq(PARTIALLY_ORDERED_RECEIPT_2), eq(ORDERED_RECEIPT_2), any())
        verify(receiptTableController).update(eq(PARTIALLY_ORDERED_RECEIPT_3), eq(ORDERED_RECEIPT_3), any())
        verify(receiptTableController, never()).update(eq(PARTIALLY_ORDERED_RECEIPT_4), any(), any())
        verify(receiptTableController, never()).update(eq(ORDERED_RECEIPT_4), any(), any())
        verify(receiptTableController, never()).update(eq(ORDERED_RECEIPT_3), any(), any())
        verify(receiptTableController, never()).update(eq(ORDERED_RECEIPT_2), any(), any())
        verify(receiptTableController, never()).update(eq(ORDERED_RECEIPT_1), any(), any())
        verify(receiptTableController, never()).update(eq(OTHER_PARTIALLY_ORDERED_RECEIPT_1), any(), any())
        verify(receiptTableController).update(eq(OTHER_PARTIALLY_ORDERED_RECEIPT_2), eq(OTHER_ORDERED_RECEIPT_2), any())
        verify(receiptTableController).update(eq(OTHER_PARTIALLY_ORDERED_RECEIPT_3), eq(OTHER_ORDERED_RECEIPT_3), any())
        verify(receiptTableController, never()).update(eq(OTHER_PARTIALLY_ORDERED_RECEIPT_4), any(), any())
        verify(receiptTableController, never()).update(eq(OTHER_ORDERED_RECEIPT_1), any(), any())
        verify(receiptTableController, never()).update(eq(OTHER_ORDERED_RECEIPT_2), any(), any())
        verify(receiptTableController, never()).update(eq(OTHER_ORDERED_RECEIPT_3), any(), any())
        verify(receiptTableController, never()).update(eq(OTHER_ORDERED_RECEIPT_4), any(), any())
        verify(orderingMigrationStore).setOrderingMigrationHasOccurred(true)
        verify(orderingPreferencesManager).saveReceiptsTableOrdering()
    }

    @Test
    fun initializeWhenWeHavePreviouslyMigratedToV1AndPartiallyOrderedReturnsAreRetrievedInADifferentOrder() {
        whenever(receiptTableController.get(partiallyOrderedTrip)).thenReturn(Single.just(Arrays.asList(PARTIALLY_ORDERED_RECEIPT_1, PARTIALLY_ORDERED_RECEIPT_3, PARTIALLY_ORDERED_RECEIPT_2, PARTIALLY_ORDERED_RECEIPT_4)))
        whenever(receiptTableController.get(otherPartiallyOrderedTrip)).thenReturn(Single.just(Arrays.asList(OTHER_PARTIALLY_ORDERED_RECEIPT_1, OTHER_PARTIALLY_ORDERED_RECEIPT_3, OTHER_PARTIALLY_ORDERED_RECEIPT_2, OTHER_PARTIALLY_ORDERED_RECEIPT_4)))
        whenever(orderingMigrationStore.getMigrationVersion()).thenReturn(Single.just(ReceiptsOrderingMigrationStore.MigrationVersion.V1))
        receiptsOrderer.initialize()
        verify(receiptTableController).update(eq(NO_ORDERING_RECEIPT_4), eq(ORDERED_RECEIPT_4), any())
        verify(receiptTableController).update(eq(NO_ORDERING_RECEIPT_3), eq(ORDERED_RECEIPT_3), any())
        verify(receiptTableController).update(eq(NO_ORDERING_RECEIPT_2), eq(ORDERED_RECEIPT_2), any())
        verify(receiptTableController).update(eq(NO_ORDERING_RECEIPT_1), eq(ORDERED_RECEIPT_1), any())
        verify(receiptTableController).update(eq(LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_4), eq(ORDERED_RECEIPT_4), any())
        verify(receiptTableController).update(eq(LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_3), eq(ORDERED_RECEIPT_3), any())
        verify(receiptTableController).update(eq(LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_2), eq(ORDERED_RECEIPT_2), any())
        verify(receiptTableController).update(eq(LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_1), eq(ORDERED_RECEIPT_1), any())
        verify(receiptTableController, never()).update(eq(PARTIALLY_ORDERED_RECEIPT_1), any(), any())
        verify(receiptTableController).update(eq(PARTIALLY_ORDERED_RECEIPT_3), eq(ORDERED_RECEIPT_3), any())
        verify(receiptTableController).update(eq(PARTIALLY_ORDERED_RECEIPT_2), eq(ORDERED_RECEIPT_2), any())
        verify(receiptTableController, never()).update(eq(PARTIALLY_ORDERED_RECEIPT_4), any(), any())
        verify(receiptTableController, never()).update(eq(ORDERED_RECEIPT_4), any(), any())
        verify(receiptTableController, never()).update(eq(ORDERED_RECEIPT_3), any(), any())
        verify(receiptTableController, never()).update(eq(ORDERED_RECEIPT_2), any(), any())
        verify(receiptTableController, never()).update(eq(ORDERED_RECEIPT_1), any(), any())
        verify(receiptTableController, never()).update(eq(OTHER_PARTIALLY_ORDERED_RECEIPT_1), any(), any())
        verify(receiptTableController).update(eq(OTHER_PARTIALLY_ORDERED_RECEIPT_3), eq(OTHER_ORDERED_RECEIPT_3), any())
        verify(receiptTableController).update(eq(OTHER_PARTIALLY_ORDERED_RECEIPT_2), eq(OTHER_ORDERED_RECEIPT_2), any())
        verify(receiptTableController, never()).update(eq(OTHER_PARTIALLY_ORDERED_RECEIPT_4), any(), any())
        verify(receiptTableController, never()).update(eq(OTHER_ORDERED_RECEIPT_1), any(), any())
        verify(receiptTableController, never()).update(eq(OTHER_ORDERED_RECEIPT_2), any(), any())
        verify(receiptTableController, never()).update(eq(OTHER_ORDERED_RECEIPT_3), any(), any())
        verify(receiptTableController, never()).update(eq(OTHER_ORDERED_RECEIPT_4), any(), any())
        verify(orderingMigrationStore).setOrderingMigrationHasOccurred(true)
        verify(orderingPreferencesManager).saveReceiptsTableOrdering()
    }

    @Test
    fun initializeWhenAnUpdateFailureOccurs() {
        whenever(receiptTableController.update(any(), any(), any())).thenReturn(Observable.error(Exception("Test")))

        receiptsOrderer.initialize()

        verify(orderingMigrationStore, never()).setOrderingMigrationHasOccurred(any())
        verify(orderingPreferencesManager, never()).saveReceiptsTableOrdering()
    }

    @Test
    fun reorderReceiptsInListToMoveFirstReceiptToTheEndAcrossDateBoundary() {
        val list = listOf(ORDERED_RECEIPT_1, ORDERED_RECEIPT_2, ORDERED_RECEIPT_3, ORDERED_RECEIPT_4)
        receiptsOrderer.reorderReceiptsInList(list, 0, 3)
                .test()
                .assertValue(listOf(updatedReceipt2, updatedReceipt3, updatedReceipt4, updatedReceipt1))
                .assertNoErrors()
                .assertComplete()
        verify(receiptTableController).update(ORDERED_RECEIPT_2, ReceiptBuilderFactory(ORDERED_RECEIPT_2).setCustomOrderId(17509003L).build(), DatabaseOperationMetadata(OperationFamilyType.Silent))
        verify(receiptTableController).update(ORDERED_RECEIPT_3, ReceiptBuilderFactory(ORDERED_RECEIPT_3).setCustomOrderId(17509002L).build(), DatabaseOperationMetadata(OperationFamilyType.Silent))
        verify(receiptTableController).update(ORDERED_RECEIPT_4, ReceiptBuilderFactory(ORDERED_RECEIPT_4).setCustomOrderId(17509001L).build(), DatabaseOperationMetadata(OperationFamilyType.Silent))
        verify(receiptTableController).update(ORDERED_RECEIPT_1, ReceiptBuilderFactory(ORDERED_RECEIPT_1).setCustomOrderId(17509000L).build(), DatabaseOperationMetadata())
    }

    @Test
    fun reorderReceiptsInListToMoveFirstReceiptToTheMiddleAcrossDateBoundary() {
        val list = listOf(ORDERED_RECEIPT_1, ORDERED_RECEIPT_2, ORDERED_RECEIPT_3, ORDERED_RECEIPT_4)
        receiptsOrderer.reorderReceiptsInList(list, 0, 1)
                .test()
                .assertValue(listOf(updatedReceipt2, updatedReceipt1, ORDERED_RECEIPT_3, ORDERED_RECEIPT_4))
                .assertNoErrors()
                .assertComplete()
        verify(receiptTableController).update(ORDERED_RECEIPT_2, ReceiptBuilderFactory(ORDERED_RECEIPT_2).setCustomOrderId(17509003L).build(), DatabaseOperationMetadata(OperationFamilyType.Silent))
        verify(receiptTableController).update(ORDERED_RECEIPT_1, ReceiptBuilderFactory(ORDERED_RECEIPT_1).setCustomOrderId(17509002L).build(), DatabaseOperationMetadata())
        verify(receiptTableController, never()).update(eq(ORDERED_RECEIPT_3), any(), any())
        verify(receiptTableController, never()).update(eq(ORDERED_RECEIPT_4), any(), any())
    }

    @Test
    fun reorderReceiptsInListToMoveLastReceiptToTheStartOfTheListAndAcrossDateBoundary() {
        val list = listOf(ORDERED_RECEIPT_1, ORDERED_RECEIPT_2, ORDERED_RECEIPT_3, ORDERED_RECEIPT_4)
        receiptsOrderer.reorderReceiptsInList(list, 3, 0)
                .test()
                .assertValue(listOf(updatedReceipt4, ORDERED_RECEIPT_1, updatedReceipt2, updatedReceipt3))
                .assertNoErrors()
                .assertComplete()
        verify(receiptTableController, never()).update(eq(ORDERED_RECEIPT_1), any(), any())
        verify(receiptTableController).update(ORDERED_RECEIPT_4, ReceiptBuilderFactory(ORDERED_RECEIPT_4).setCustomOrderId(17518001L).build(), DatabaseOperationMetadata(OperationFamilyType.Silent))
        verify(receiptTableController).update(ORDERED_RECEIPT_2, ReceiptBuilderFactory(ORDERED_RECEIPT_2).setCustomOrderId(17509001L).build(), DatabaseOperationMetadata(OperationFamilyType.Silent))
        verify(receiptTableController).update(ORDERED_RECEIPT_3, ReceiptBuilderFactory(ORDERED_RECEIPT_3).setCustomOrderId(17509000L).build(), DatabaseOperationMetadata())
    }

    @Test
    fun reorderReceiptsInListToMoveReceiptBackwardOneSpotWithinTheSameDay() {
        val list = listOf(ORDERED_RECEIPT_1, ORDERED_RECEIPT_2, ORDERED_RECEIPT_3, ORDERED_RECEIPT_4)
        receiptsOrderer.reorderReceiptsInList(list, 2, 3)
                .test()
                .assertValue(listOf(ORDERED_RECEIPT_1, ORDERED_RECEIPT_2, updatedReceipt4, updatedReceipt3))
                .assertNoErrors()
                .assertComplete()
        verify(receiptTableController, never()).update(eq(ORDERED_RECEIPT_1), any(), any())
        verify(receiptTableController, never()).update(eq(ORDERED_RECEIPT_2), any(), any())
        verify(receiptTableController).update(ORDERED_RECEIPT_4, ReceiptBuilderFactory(ORDERED_RECEIPT_4).setCustomOrderId(17509001L).build(), DatabaseOperationMetadata(OperationFamilyType.Silent))
        verify(receiptTableController).update(ORDERED_RECEIPT_3, ReceiptBuilderFactory(ORDERED_RECEIPT_3).setCustomOrderId(17509000L).build(), DatabaseOperationMetadata())
    }

    @Test
    fun reorderReceiptsInListToMoveReceiptBackwardTwoSpotWithinTheSameDay() {
        val list = listOf(ORDERED_RECEIPT_1, ORDERED_RECEIPT_2, ORDERED_RECEIPT_3, ORDERED_RECEIPT_4)
        receiptsOrderer.reorderReceiptsInList(list, 1, 3)
                .test()
                .assertValue(listOf(ORDERED_RECEIPT_1, updatedReceipt3, updatedReceipt4, updatedReceipt2))
                .assertNoErrors()
                .assertComplete()
        verify(receiptTableController, never()).update(eq(ORDERED_RECEIPT_1), any(), any())
        verify(receiptTableController).update(ORDERED_RECEIPT_3, ReceiptBuilderFactory(ORDERED_RECEIPT_3).setCustomOrderId(17509002L).build(), DatabaseOperationMetadata(OperationFamilyType.Silent))
        verify(receiptTableController).update(ORDERED_RECEIPT_4, ReceiptBuilderFactory(ORDERED_RECEIPT_4).setCustomOrderId(17509001L).build(), DatabaseOperationMetadata(OperationFamilyType.Silent))
        verify(receiptTableController).update(ORDERED_RECEIPT_2, ReceiptBuilderFactory(ORDERED_RECEIPT_2).setCustomOrderId(17509000L).build(), DatabaseOperationMetadata())
    }

    @Test
    fun reorderReceiptsInListToMoveReceiptForwardTwoSpotsWithinTheSameDay() {
        val list = listOf(ORDERED_RECEIPT_1, ORDERED_RECEIPT_2, ORDERED_RECEIPT_3, ORDERED_RECEIPT_4)
        receiptsOrderer.reorderReceiptsInList(list, 3, 1)
                .test()
                .assertValue(listOf(ORDERED_RECEIPT_1, updatedReceipt4, updatedReceipt2, updatedReceipt3))
                .assertNoErrors()
                .assertComplete()
        verify(receiptTableController, never()).update(eq(ORDERED_RECEIPT_1), any(), any())
        verify(receiptTableController).update(ORDERED_RECEIPT_4, ReceiptBuilderFactory(ORDERED_RECEIPT_4).setCustomOrderId(17509002L).build(), DatabaseOperationMetadata(OperationFamilyType.Silent))
        verify(receiptTableController).update(ORDERED_RECEIPT_2, ReceiptBuilderFactory(ORDERED_RECEIPT_2).setCustomOrderId(17509001L).build(), DatabaseOperationMetadata(OperationFamilyType.Silent))
        verify(receiptTableController).update(ORDERED_RECEIPT_3, ReceiptBuilderFactory(ORDERED_RECEIPT_3).setCustomOrderId(17509000L).build(), DatabaseOperationMetadata())
    }

    @Test
    fun reorderReceiptsInListToMoveReceiptForwardOneSpotWithinTheSameDay() {
        val list = listOf(ORDERED_RECEIPT_1, ORDERED_RECEIPT_2, ORDERED_RECEIPT_3, ORDERED_RECEIPT_4)
        receiptsOrderer.reorderReceiptsInList(list, 3, 2)
                .test()
                .assertValue(listOf(ORDERED_RECEIPT_1, ORDERED_RECEIPT_2, updatedReceipt4, updatedReceipt3))
                .assertNoErrors()
                .assertComplete()
        verify(receiptTableController, never()).update(eq(ORDERED_RECEIPT_1), any(), any())
        verify(receiptTableController, never()).update(eq(ORDERED_RECEIPT_2), any(), any())
        verify(receiptTableController).update(ORDERED_RECEIPT_4, ReceiptBuilderFactory(ORDERED_RECEIPT_4).setCustomOrderId(17509001L).build(), DatabaseOperationMetadata(OperationFamilyType.Silent))
        verify(receiptTableController).update(ORDERED_RECEIPT_3, ReceiptBuilderFactory(ORDERED_RECEIPT_3).setCustomOrderId(17509000L).build(), DatabaseOperationMetadata())
    }

    @Test
    fun getCustomOrderIdForNewReceiptWithExistingOnesInTheSameDay() {
        val receipts = listOf(ORDERED_RECEIPT_3, ORDERED_RECEIPT_4)
        val nextCustomOrderID = ReceiptsOrderer.getCustomOrderId(ORDERED_RECEIPT_2, receipts)
        assertEquals(ORDERED_RECEIPT_2.customOrderId, nextCustomOrderID)
    }

    @Test
    fun getCustomOrderIdFoExitingReceiptInTheList() {
        val receipts = listOf(ORDERED_RECEIPT_2, ORDERED_RECEIPT_3, ORDERED_RECEIPT_4)
        val nextCustomOrderID = ReceiptsOrderer.getCustomOrderId(ORDERED_RECEIPT_2, receipts)
        assertEquals(ORDERED_RECEIPT_2.customOrderId, nextCustomOrderID)
    }

    @Test
    fun getCustomOrderIdForNewReceiptNoReceiptsThatDay() {
        val receipts = emptyList<Receipt>()
        val nextCustomOrderID = ReceiptsOrderer.getCustomOrderId(ORDERED_RECEIPT_4, receipts)
        assertEquals(ORDERED_RECEIPT_4.customOrderId, nextCustomOrderID)
    }
}