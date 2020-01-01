package co.smartreceipts.android.search

import co.smartreceipts.core.di.scopes.ApplicationScope
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.persistence.DatabaseHelper
import co.smartreceipts.android.persistence.database.tables.CategoriesTable
import co.smartreceipts.android.persistence.database.tables.PaymentMethodsTable
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable
import co.smartreceipts.android.persistence.database.tables.TripsTable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@ApplicationScope
class SearchInteractor(
    private val databaseHelper: DatabaseHelper,
    private val subscribeOnScheduler: Scheduler = Schedulers.io(),
    private val observeOnScheduler: Scheduler = AndroidSchedulers.mainThread()
) {

    data class SearchResults(val trips: List<Trip>, val receipts: List<Receipt>) {
        fun isEmpty() = trips.isEmpty() && receipts.isEmpty()
    }


    @Inject
    constructor(databaseHelper: DatabaseHelper) : this(databaseHelper, Schedulers.io(), AndroidSchedulers.mainThread())

    fun getSearchResults(input: String): Single<SearchResults> {

        if (input.isEmpty()) {
            return Single.just(SearchResults(emptyList(), emptyList()))
        }

        return Single.zip(
            searchReceiptsByCategory(input),
            searchReceiptsByPaymentMethod(input),
            searchReceiptCommonly(input),

            Function3<List<String>, List<String>, List<String>, List<String>> { byCategorySearch, byPaymentMethodSearch, byCommonSearch ->
                val receipts = ArrayList<String>()
                receipts.apply {
                    addAll(byCategorySearch)
                    addAll(byPaymentMethodSearch)
                    addAll(byCommonSearch)
                    distinct()
                }
            }
        )
            .flatMap { getReceiptsByIds(it) }
            .flatMap { receipts ->
                searchForTrips(input)
                    .map { trips ->
                        SearchResults(trips.sorted(), receipts.sorted())
                    }

            }
            .subscribeOn(subscribeOnScheduler)
            .observeOn(observeOnScheduler)
    }

    /**
     * @return List of receipt's id's
     */
    private fun searchReceiptsByCategory(input: String): Single<List<String>> {
        return databaseHelper.search(
            input, CategoriesTable.TABLE_NAME, CategoriesTable.COLUMN_ID, null,
            CategoriesTable.COLUMN_NAME, CategoriesTable.COLUMN_CODE
        )
            .flatMap { categories ->
                Observable.fromIterable(categories)
                    .flatMap { categoryId ->
                        databaseHelper.search(
                            categoryId, ReceiptsTable.TABLE_NAME, ReceiptsTable.COLUMN_ID, null,
                            ReceiptsTable.COLUMN_CATEGORY_ID
                        )
                            .toObservable()
                    }
                    .flatMap { Observable.fromIterable(it) }
                    .toList()
            }
    }

    /**
     * @return List of receipt's id's
     */
    private fun searchReceiptsByPaymentMethod(input: String): Single<List<String>> {
        return databaseHelper.search(
            input, PaymentMethodsTable.TABLE_NAME, PaymentMethodsTable.COLUMN_ID, null,
            PaymentMethodsTable.COLUMN_METHOD
        )
            .flatMap { paymentMethods ->
                Observable.fromIterable(paymentMethods)
                    .flatMap { paymentMethodId ->
                        databaseHelper.search(
                            paymentMethodId, ReceiptsTable.TABLE_NAME, ReceiptsTable.COLUMN_ID, null,
                            ReceiptsTable.COLUMN_PAYMENT_METHOD_ID
                        )
                            .toObservable()
                    }
                    .flatMap { Observable.fromIterable(it) }
                    .toList()
            }
    }

    private fun searchReceiptCommonly(input: String): Single<List<String>> {
        // receipts found by receipt's name, comment, price
        return databaseHelper.search(
            input, ReceiptsTable.TABLE_NAME, ReceiptsTable.COLUMN_ID, ReceiptsTable.COLUMN_DATE,
            ReceiptsTable.COLUMN_NAME, ReceiptsTable.COLUMN_COMMENT, ReceiptsTable.COLUMN_PRICE
        )
    }

    private fun getReceiptsByIds(ids: List<String>): Single<List<Receipt>> {
        return databaseHelper.receiptsTable.get()
            .map { allReceipts ->
                val results = ArrayList<Receipt>()

                for (receipt in allReceipts) {
                    if (ids.contains(receipt.id.toString())) {
                        results.add(receipt)
                    }
                }

                return@map results
            }
    }

    private fun searchForTrips(input: String): Single<List<Trip>> {
        // Single with the list of trips found by trip's name, comment
        return databaseHelper.search(
            input, TripsTable.TABLE_NAME, TripsTable.COLUMN_ID, TripsTable.COLUMN_FROM,
            TripsTable.COLUMN_NAME, TripsTable.COLUMN_COMMENT
        )
            .flatMap { tripsIdList ->
                databaseHelper.tripsTable.get()
                    .map { allTrips ->
                        val results = ArrayList<Trip>()

                        for (trip in allTrips) {
                            if (tripsIdList.contains(trip.id.toString())) {
                                results.add(trip)
                            }
                        }

                        return@map results
                    }
            }

    }
}