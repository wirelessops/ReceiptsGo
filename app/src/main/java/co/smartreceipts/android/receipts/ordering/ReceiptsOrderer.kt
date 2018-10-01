package co.smartreceipts.android.receipts.ordering

import android.annotation.SuppressLint
import co.smartreceipts.android.date.DateUtils
import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory
import co.smartreceipts.android.persistence.database.controllers.impl.ReceiptTableController
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType
import co.smartreceipts.android.persistence.database.tables.ordering.OrderingPreferencesManager
import co.smartreceipts.android.receipts.ordering.ReceiptsOrderer.OrderingType.*
import co.smartreceipts.android.utils.log.Logger
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.sql.Date
import javax.inject.Inject

/**
 * Manages the ordering of receipts, helping us to ensure that they always maintain a consistent ordering.
 * This also manages the process of migrating the receipts from the by-date or to the by-custom-id ordering
 * mechanism.
 *
 * This effectively works by using the (number of days * 1000) as the base index for a given receipt. Whenever
 * a receipt position is changed to that of a new receipt group, we grab it's assumed day count from the custom
 * order id (ie "fake days") to ensure that we can track all receipts within this group.
 *
 * More detailed [Receipt.customOrderId] formation [scheme](https://s3.amazonaws.com/smartreceipts/Diagrams/SmartReceiptsCustomSortingOrderDesign.png)
 */
@ApplicationScope
class ReceiptsOrderer constructor(private val tripTableController: TripTableController,
                                  private val receiptTableController: ReceiptTableController,
                                  private val orderingMigrationStore: ReceiptsOrderingMigrationStore,
                                  private val orderingPreferencesManager: OrderingPreferencesManager,
                                  private val backgroundScheduler: Scheduler) {

    @Inject
    constructor(tripTableController: TripTableController,
                receiptTableController: ReceiptTableController,
                orderingMigrationStore: ReceiptsOrderingMigrationStore,
                orderingPreferencesManager: OrderingPreferencesManager)
            : this(tripTableController, receiptTableController, orderingMigrationStore, orderingPreferencesManager, Schedulers.io())

    companion object {
        const val DAYS_TO_ORDER_FACTOR = 1000

        /**
         * Creates the default custom order id for a given date. In this case, it builds this by using the number of days
         * since the unix epoch multiplied by [DAYS_TO_ORDER_FACTOR] and then added to [DAYS_TO_ORDER_FACTOR] - 1
         */
        fun getDefaultCustomOrderId(date: Date) : Long {
            return DateUtils.getDays(date) * ReceiptsOrderer.DAYS_TO_ORDER_FACTOR + ReceiptsOrderer.DAYS_TO_ORDER_FACTOR - 1
        }
    }

    @SuppressLint("CheckResult")
    fun initialize() {
        orderingMigrationStore.hasOrderingMigrationOccurred()
                .subscribeOn(backgroundScheduler)
                .flatMapObservable { hasOrderingMigrationOccurred ->
                    if (hasOrderingMigrationOccurred) {
                        Logger.info(this, "Ordering migration previously occurred. Ignoring...")
                        return@flatMapObservable Observable.empty<List<Trip>>()
                    } else {
                        return@flatMapObservable tripTableController.get().toObservable()
                                .doOnNext { _ ->
                                    Logger.info(this, "Migrating all receipts to use the correct custom_order_id")
                                }
                                .flatMap {
                                    return@flatMap Observable.fromIterable(it)
                                }
                                .flatMap { trip ->
                                    return@flatMap receiptTableController.get(trip)
                                            .flatMapObservable { receipts ->
                                                val orderingType = getOrderingType(receipts)
                                                when (orderingType) {
                                                    OrderingType.None, OrderingType.Legacy -> reorderReceiptsByDate(receipts)
                                                    else -> Observable.empty()
                                                }
                                            }
                                }
                                .doOnComplete {
                                    Logger.info(this, "Successfully migrated all receipts to the correct custom_order_id")
                                    orderingMigrationStore.setOrderingMigrationOccurred(true)
                                    orderingPreferencesManager.saveReceiptsTableOrdering()
                                }
                    }
                }
                .subscribe({}, {
                    Logger.error(this, "Failed to migrate our receipts to use the correct date", it)
                })
    }

    /**
     * Calculates changes that need to be applied to receipts after reordering.
     *
     * Note: customOrderId = days * 1000; days could be fake for reordered item.
     * New receipt has customOrderId = days * 1000 + 999
     *
     * @param originalReceiptsList the list of all [Receipt]s before reordering.
     * @param fromPosition previous position of the moved item.
     * @param toPosition new position of the moved item.
     *
     * @return [Single] that will emit an update [List] of [Receipt]s
     */
    fun reorderReceiptsInList(originalReceiptsList: List<Receipt>, fromPosition: Int, toPosition: Int): Single<List<Receipt>> {
        return Observable.fromCallable {
                    // First, ensure that we make this list mutable
                    val mutableReceiptsList = originalReceiptsList.toMutableList()

                    // This calculates the "fake" days at the position that we've moved this item to
                    val toPositionFakeDays = mutableReceiptsList[toPosition].customOrderId / DAYS_TO_ORDER_FACTOR

                    // Next, remove this item
                    val movedReceipt = mutableReceiptsList.removeAt(fromPosition)

                    // Then ensure the movedReceipt gets modified to use the new toPositionFakeDays
                    val movedReceiptWithCorrectFakeDays = ReceiptBuilderFactory(movedReceipt).setCustomOrderId(toPositionFakeDays * DAYS_TO_ORDER_FACTOR).build()

                    // And add it back to the list at the proper position
                    mutableReceiptsList.add(toPosition, movedReceiptWithCorrectFakeDays)

                    // From here, count the number of receipts within this "fake day" and update the index of each. This process should
                    // ensure that all of our receipts are now properly ordered
                    val receiptsToUpdateList = mutableListOf<Pair<Receipt, Receipt?>>()
                    var sameFakeDaysNumber = 0
                    for (i in mutableReceiptsList.indices.reversed()) {
                        val receipt = mutableReceiptsList[i]
                        // Whenever the receipt matches the "fake days" of the to position, update it's custom_order_id
                        if (receipt.customOrderId / DAYS_TO_ORDER_FACTOR == toPositionFakeDays) {
                            val updated = ReceiptBuilderFactory(receipt).setCustomOrderId(toPositionFakeDays * DAYS_TO_ORDER_FACTOR + sameFakeDaysNumber).build()
                            sameFakeDaysNumber++
                            if (receipt == movedReceiptWithCorrectFakeDays) {
                                // Pass in the original reference to ease our testing
                                receiptsToUpdateList.add(Pair(movedReceipt, updated))
                            } else {
                                receiptsToUpdateList.add(Pair(receipt, updated))
                            }
                        } else {
                            // Or if this item exists on a different day, don't bother to update (ie pair it with a null)
                            receiptsToUpdateList.add(Pair(receipt, null))
                        }
                    }

                    // Reverse the list to maintain the original ordering (ie before the indices reversal above)
                    receiptsToUpdateList.reverse()

                    return@fromCallable receiptsToUpdateList
                }
                .subscribeOn(backgroundScheduler)
                .flatMap { receiptsToUpdateList ->
                    return@flatMap Observable.fromIterable(receiptsToUpdateList)
                            .concatMap {
                                // Note: We use concatMap to preserve the list ordering
                                if (it.second == null) {
                                    // Return the original receipt if there's nothing to update
                                    return@concatMap Observable.just(it.first)
                                } else {
                                    // Or if there's a change, apply this and update accordingly
                                    return@concatMap receiptTableController.update(it.first, it.second!!, getDatabaseOperationMetadata(receiptsToUpdateList, it))
                                            .flatMap {
                                                if (it.isPresent) {
                                                    Observable.just(it.get())
                                                } else {
                                                    Observable.error<Receipt>(Exception("Failed to update the receipt custom_order_id"))
                                                }
                                            }
                                }
                            }
                }
                .toList()
                .doOnSuccess {
                    Logger.info(this, "Successfully re-ordered this receipts list to the desired position")
                }
                .doOnError {
                    Logger.info(this, "Failed re-ordered this receipts list by moving a receipt from {} to {}", fromPosition, toPosition, it)
                }
    }

    /**
     * Attempts to reorder a given [List] or [Receipt] entities based on the current value of the
     * [Receipt.getDate] field. When complete, all items will follow the [OrderingType.Ordered]
     * methodology
     *
     * @param receipts the [List] of [Receipt] to reorder by their dates
     * @return a [Completable], which will emit success or failure based on the result of this operation
     */
    private fun reorderReceiptsByDate(receipts: List<Receipt>): Observable<Receipt> {
        return Observable.fromCallable {
                    var receiptCountForCurrentDay = 0
                    var dayNumber = -1L

                    val receiptPairs = mutableListOf<Pair<Receipt, Receipt>>()
                    receipts.forEach {
                        val receiptDayNumber = DateUtils.getDays(it.date)
                        if (dayNumber == receiptDayNumber) {
                            receiptCountForCurrentDay += 1
                        } else {
                            dayNumber = receiptDayNumber
                            receiptCountForCurrentDay = 0
                        }
                        val updatedReceipt = ReceiptBuilderFactory(it).setCustomOrderId(dayNumber * DAYS_TO_ORDER_FACTOR + receiptCountForCurrentDay).build()
                        receiptPairs.add(Pair(it, updatedReceipt))
                    }
                    return@fromCallable receiptPairs
                }
                .flatMap { list ->
                    Observable.fromIterable(list)
                            .flatMap {
                                receiptTableController.update(it.first, it.second, getDatabaseOperationMetadata(list, it))
                                        .flatMap {
                                            if (it.isPresent) {
                                                Observable.just(it.get())
                                            } else {
                                                Observable.error<Receipt>(Exception("Failed to update the receipt custom_order_id"))
                                            }
                                        }
                            }
                }
                .doOnError {
                    Logger.info(this, "Failed re-ordered this receipts list by date to custom order id", it)
                }
                .doOnComplete {
                    Logger.info(this, "Successfully re-ordered {} receipts by date to custom order id", receipts.size)
                }
    }

    /**
     * To help speed along the results, we don't want to update the UI when a single item is changed. To help manage this,
     * we use this helper function to determine if this should be a "silent" operation or not. We use normal operations if
     * this is the last item in a particular trip and a "silent" operation if it's any other one
     *
     * @param list a [List] of items
     * @param item the item to check if it's the last one
     * @return the appropriate [DatabaseOperationMetadata], usually [OperationFamilyType.Silent] but [OperationFamilyType.Default]
     * if it's the last
     */
    private fun getDatabaseOperationMetadata(list: List<Pair<Receipt, Receipt?>>, item: Any) : DatabaseOperationMetadata {
        // Find the index as either the next or "last" item
        val nextItemIndex = Math.min(list.indexOf(item) + 1, list.size - 1)
        return if (list.last() == item) {
            // As the last item to update in this list, we'll update all listeners when it's done (ie non-Silent operation)
            DatabaseOperationMetadata()
        } else if (list[nextItemIndex].second == null) {
            // If the next item index has a null value, perform a non-silent operations
            DatabaseOperationMetadata()
        } else {
            DatabaseOperationMetadata(OperationFamilyType.Silent)
        }
    }

    /**
     * Determines which [OrderingType] the receipts follow for a given set of receipts in a single trip.
     * We assume that if a single receipt uses the [OrderingType.None] approach, they all use this.
     * Similarly, we assume that if a single receipt uses the [OrderingType.Legacy] approach, they all use
     * this. In the former case, we assume the custom order id is 0 and we assume it's large date number in
     * the latter
     *
     * @param receipts the [List] of [Receipt] items in a given trip to check
     * @return [OrderingType] that represents how these receipts have been ordered
     */
    private fun getOrderingType(receipts: List<Receipt>): OrderingType {
        receipts.forEach {receipt ->
            if (receipt.customOrderId == 0L) {
                return OrderingType.None
            } else if (receipt.customOrderId / DAYS_TO_ORDER_FACTOR > 20000) {
                return OrderingType.Legacy
            }
        }
        return OrderingType.Ordered
    }

    /**
     * Defines how the receipts are currently ordered. Ideally, all receipts should be ordered as
     * detailed by the [Ordered] type, but this is not the case for a few legacy reasons:
     *
     * 1. When updating from [None] to [Legacy], we only upgraded the first trip that the user attempted to re-order
     * 2. When updating from [Legacy] to [Ordered], we only upgraded the first trip that the user attempted to re-order
     *
     * As a result, we can have partial ordering split across multiple trips
     */
    private enum class OrderingType {
        /**
         * Defines the ordering type that was present before we applied the custom order id (as opposed to ordering based off the date)
         */
        None,

        /**
         * The legacy approach is what occurred when we set the custom order id to the receipts date, allowing each to operate independently
         */
        Legacy,

        /**
         * The current approach uses a mix of the receipt date and count of receipts within that day (https://s3.amazonaws.com/smartreceipts/Diagrams/SmartReceiptsCustomSortingOrderDesign.png)
         *
         * Practically speaking, we multiple the number of days since the Unix epoch by 1000 and add the current position within that day
         */
        Ordered
    }
}