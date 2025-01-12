package com.wops.receiptsgo.search

import com.wops.receiptsgo.model.factory.ReceiptBuilderFactory
import com.wops.receiptsgo.model.factory.TripBuilderFactory
import com.wops.receiptsgo.persistence.DatabaseHelper
import com.wops.receiptsgo.persistence.database.tables.CategoriesTable
import com.wops.receiptsgo.persistence.database.tables.PaymentMethodsTable
import com.wops.receiptsgo.persistence.database.tables.ReceiptsTable
import com.wops.receiptsgo.persistence.database.tables.TripsTable
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SearchInteractorTest {

    companion object {
        const val input = "some text"

        const val receiptId1 = 1
        const val receiptId2 = 2

        const val tripId1 = 1
        const val tripId2 = 5

        val trip1 = TripBuilderFactory().setId(tripId1).build()
        val trip2 = TripBuilderFactory().setId(tripId2).build()

        val receipt1 = ReceiptBuilderFactory(receiptId1).setTrip(trip1).build()
        val receipt2 = ReceiptBuilderFactory(receiptId2).setTrip(trip1).build()
    }

    // Class under test
    private lateinit var interactor: SearchInteractor

    private val databaseHelper = mock<DatabaseHelper>()

    @Before
    fun setUp() {
        val tripsTable = mock<TripsTable>()
        val receiptsTable = mock<ReceiptsTable>()
        val categoriesTable = mock<CategoriesTable>()
        val paymentMethodsTable = mock<PaymentMethodsTable>()

        whenever(databaseHelper.tripsTable).thenReturn(tripsTable)
        whenever(databaseHelper.receiptsTable).thenReturn(receiptsTable)
        whenever(databaseHelper.categoriesTable).thenReturn(categoriesTable)
        whenever(databaseHelper.paymentMethodsTable).thenReturn(paymentMethodsTable)


        whenever(tripsTable.get()).thenReturn(Single.just(listOf(trip1, trip2)))
        whenever(receiptsTable.get()).thenReturn(Single.just(listOf(receipt1, receipt2)))

        // trips
        whenever(databaseHelper.search(input, TripsTable.TABLE_NAME, TripsTable.COLUMN_ID, TripsTable.COLUMN_FROM,
            TripsTable.COLUMN_NAME, TripsTable.COLUMN_COMMENT)).thenReturn(Single.just(listOf(tripId1.toString())))

        // receipts
        whenever(databaseHelper.search(input, ReceiptsTable.TABLE_NAME, ReceiptsTable.COLUMN_ID, ReceiptsTable.COLUMN_DATE,
            ReceiptsTable.COLUMN_NAME, ReceiptsTable.COLUMN_COMMENT, ReceiptsTable.COLUMN_PRICE)).thenReturn(Single.just(listOf(receiptId1.toString(), receiptId2.toString())))

        // categories
        whenever(databaseHelper.search(input, CategoriesTable.TABLE_NAME, CategoriesTable.COLUMN_ID, null,
            CategoriesTable.COLUMN_NAME, CategoriesTable.COLUMN_CODE)).thenReturn(Single.just(emptyList()))

        // payment methods
        whenever(databaseHelper.search(input, PaymentMethodsTable.TABLE_NAME, PaymentMethodsTable.COLUMN_ID, null,
            PaymentMethodsTable.COLUMN_METHOD)).thenReturn(Single.just(emptyList()))


        interactor = SearchInteractor(databaseHelper, Schedulers.trampoline(), Schedulers.trampoline())
    }

    @Test
    fun getSearchResultsEmptyInputTest() {
        interactor.getSearchResults("").test()
            .assertNoErrors()
            .assertComplete()
            .assertResult(SearchInteractor.SearchResults(emptyList(), emptyList()))
    }

    @Test
    fun getSearchResultsTest() {

        interactor.getSearchResults(input).test()
            .assertComplete()
            .assertResult(SearchInteractor.SearchResults(listOf(trip1), listOf(receipt1, receipt2).sorted() ))

        verify(databaseHelper).search(eq(input), eq(CategoriesTable.TABLE_NAME), any(), anyOrNull(), anyVararg())
        verify(databaseHelper).search(eq(input), eq(PaymentMethodsTable.TABLE_NAME), any(), anyOrNull(), anyVararg())
        verify(databaseHelper).search(eq(input), eq(ReceiptsTable.TABLE_NAME), any(), anyOrNull(), anyVararg())
        verify(databaseHelper).search(eq(input), eq(TripsTable.TABLE_NAME), any(), anyOrNull(), anyVararg())
    }

}