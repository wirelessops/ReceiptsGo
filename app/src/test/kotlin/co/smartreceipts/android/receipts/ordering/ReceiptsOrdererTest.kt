package co.smartreceipts.android.receipts.ordering

import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory
import co.smartreceipts.android.model.factory.TripBuilderFactory
import co.smartreceipts.android.persistence.database.controllers.impl.ReceiptTableController
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController
import co.smartreceipts.android.persistence.database.tables.ordering.OrderingPreferencesManager
import com.hadisatrio.optional.Optional
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import java.util.*

@RunWith(RobolectricTestRunner::class)
class ReceiptsOrdererTest {

    companion object {
        private val TRIP = TripBuilderFactory().build()

        private val NO_ORDERING_RECEIPT_1 = ReceiptBuilderFactory().setTrip(TRIP).setDate(1512778546145L).setCustomOrderId(0L).build()
        private val NO_ORDERING_RECEIPT_2 = ReceiptBuilderFactory().setTrip(TRIP).setDate(1512778546147L).setCustomOrderId(0L).build()
        private val NO_ORDERING_RECEIPT_3 = ReceiptBuilderFactory().setTrip(TRIP).setDate(1513580400148L).setCustomOrderId(0L).build()

        private val LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_1 = ReceiptBuilderFactory().setTrip(TRIP).setDate(1512778546145L).setCustomOrderId(1512778546145L).build()
        private val LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_2 = ReceiptBuilderFactory().setTrip(TRIP).setDate(1512778546147L).setCustomOrderId(1512778546147L).build()
        private val LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_3 = ReceiptBuilderFactory().setTrip(TRIP).setDate(1513580400148L).setCustomOrderId(1513580400148L).build()

        private val ORDERED_RECEIPT_1 = ReceiptBuilderFactory().setTrip(TRIP).setDate(1512778546145L).setCustomOrderId(17509000L).build()
        private val ORDERED_RECEIPT_2 = ReceiptBuilderFactory().setTrip(TRIP).setDate(1512778546147L).setCustomOrderId(17509001L).build()
        private val ORDERED_RECEIPT_3 = ReceiptBuilderFactory().setTrip(TRIP).setDate(1513580400148L).setCustomOrderId(17518000L).build()
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
    lateinit var orderedTrip: Trip

    @Mock
    lateinit var receipt: Receipt

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(orderingMigrationStore.hasOrderingMigrationOccurred()).thenReturn(Single.just(false))
        whenever(tripTableController.get()).thenReturn(Single.just(Arrays.asList(unorderedTrip, legacyOrderedTrip, orderedTrip)))
        whenever(receiptTableController.get(unorderedTrip)).thenReturn(Single.just(Arrays.asList(NO_ORDERING_RECEIPT_1, NO_ORDERING_RECEIPT_2, NO_ORDERING_RECEIPT_3)))
        whenever(receiptTableController.get(legacyOrderedTrip)).thenReturn(Single.just(Arrays.asList(ReceiptsOrdererTest.LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_1, LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_2, LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_3)))
        whenever(receiptTableController.get(orderedTrip)).thenReturn(Single.just(Arrays.asList(ORDERED_RECEIPT_1, ORDERED_RECEIPT_2, ORDERED_RECEIPT_3)))

        // Note: Stub return here to keep the flow working
        whenever(receiptTableController.update(any(), any(), any())).thenReturn(Observable.just(Optional.of(receipt)))

        receiptsOrderer = ReceiptsOrderer(tripTableController, receiptTableController, orderingMigrationStore, orderingPreferencesManager, Schedulers.trampoline())
    }

    @Test
    fun initializeWhenWeHavePreviouslyMigrated() {
        whenever(orderingMigrationStore.hasOrderingMigrationOccurred()).thenReturn(Single.just(true))

        receiptsOrderer.initialize()

        verify(orderingMigrationStore, never()).setOrderingMigrationOccurred(any())
        verifyZeroInteractions(receiptTableController, tripTableController, orderingPreferencesManager)
    }

    @Test
    fun initializeWhenWeHaveNotPreviouslyMigrated() {
        receiptsOrderer.initialize()
        verify(receiptTableController).update(eq(NO_ORDERING_RECEIPT_1), eq(ORDERED_RECEIPT_1), any())
        verify(receiptTableController).update(eq(NO_ORDERING_RECEIPT_2), eq(ORDERED_RECEIPT_2), any())
        verify(receiptTableController).update(eq(NO_ORDERING_RECEIPT_3), eq(ORDERED_RECEIPT_3), any())
        verify(receiptTableController).update(eq(LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_1), eq(ORDERED_RECEIPT_1), any())
        verify(receiptTableController).update(eq(LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_2), eq(ORDERED_RECEIPT_2), any())
        verify(receiptTableController).update(eq(LEGACY_ORDERING_BY_UNIX_DATE_RECEIPT_3), eq(ORDERED_RECEIPT_3), any())
        verify(receiptTableController, never()).update(eq(ORDERED_RECEIPT_1), any(), any())
        verify(receiptTableController, never()).update(eq(ORDERED_RECEIPT_2), any(), any())
        verify(receiptTableController, never()).update(eq(ORDERED_RECEIPT_3), any(), any())
        verify(orderingMigrationStore).setOrderingMigrationOccurred(true)
        verify(orderingPreferencesManager).saveReceiptsTableOrdering()
    }

    @Test
    fun initializeWhenAnUpdateFailureOccurs() {
        whenever(receiptTableController.update(any(), any(), any())).thenReturn(Observable.error(Exception("Test")))

        receiptsOrderer.initialize()

        verify(orderingMigrationStore, never()).setOrderingMigrationOccurred(any())
        verify(orderingPreferencesManager, never()).saveReceiptsTableOrdering()
    }
}