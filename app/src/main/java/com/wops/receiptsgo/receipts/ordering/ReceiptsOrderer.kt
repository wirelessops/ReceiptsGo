package com.wops.receiptsgo.receipts.ordering

import android.annotation.SuppressLint
import com.wops.receiptsgo.date.DateUtils
import co.smartreceipts.core.di.scopes.ApplicationScope
import com.wops.receiptsgo.model.Receipt
import com.wops.receiptsgo.model.Trip
import com.wops.receiptsgo.model.factory.ReceiptBuilderFactory
import com.wops.receiptsgo.persistence.database.controllers.impl.ReceiptTableController
import com.wops.receiptsgo.persistence.database.controllers.impl.TripTableController
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata
import com.wops.receiptsgo.persistence.database.operations.OperationFamilyType
import com.wops.receiptsgo.persistence.database.tables.ordering.OrderingPreferencesManager
import com.wops.receiptsgo.receipts.ordering.ReceiptsOrderer.OrderingType.*
import co.smartreceipts.analytics.log.Logger
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.sql.Date
import javax.inject.Inject
import kotlin.math.min

/**
 * Manages the ordering of receipts, helping us to ensure that they always maintain a consistent ordering.
 * This also manages the process of migrating the receipts from the by-date or to the by-custom-id ordering
 * mechanism.
 *
 * This effectively works by using the (number of days * 1000) as the base index for a given receipt. Whenever
 * a receipt position is changed to that of a new receipt group, we grab its assumed day count from the custom
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
        private const val DAYS_TO_ORDER_FACTOR = 1000
        private const val PARTIAL_ORDERING_REMAINDER = 999L

        /**
         * Creates the default custom order id for a given date. In this case, it builds this by using the number of days
         * since the unix epoch multiplied by [DAYS_TO_ORDER_FACTOR]
         */
        fun getDefaultCustomOrderId(date: Date) : Long {
            return DateUtils.getDays(date) * DAYS_TO_ORDER_FACTOR
        }

        /**
         * This function will calculate the custom order id for a provided [receipt], given the rest of the [existingReceipts]
         * that belong to the same parent trip.
         *
         * @param receipt the [Receipt] to calculate the custom order id from
         * @param existingReceipts a [List] of [Receipt], which contain all the existing receipts in this trip.
         *
         * @return the custom order id for [receipt]. Please note that if [existingReceipts] contains the [receipt],
         * it will be ignored for the purpose of calculating the total
         */
        @JvmStatic
        fun getCustomOrderId(receipt: Receipt, existingReceipts: List<Receipt>) : Long {
            var customOrderId = getDefaultCustomOrderId(receipt.date)
            val numberOfDaysForReceipt = DateUtils.getDays(receipt.date)
            existingReceipts.forEach {
                // Ignore situations in which the receipt we're checking is in this list
                if (receipt != it) {
                    // Here we attempt to find all receipts in the current custom order id "date", so we can place this one at the end
                    // Since all custom order ids are based off the closest date (plus an offset number), we can calculate the days from this
                    val customOrderIdAsDays = it.customOrderId / DAYS_TO_ORDER_FACTOR
                    if (numberOfDaysForReceipt == customOrderIdAsDays) {
                        customOrderId++
                    }
                }
            }
            return customOrderId
        }

    }

    @SuppressLint("CheckResult")
    fun initialize() {
        orderingMigrationStore.getMigrationVersion()
                .subscribeOn(backgroundScheduler)
                .flatMapObservable { previousMigration ->
                    if (previousMigration == ReceiptsOrderingMigrationStore.MigrationVersion.V3) {
                        Logger.info(this, "Ordering migration to v3 previously occurred. Ignoring...")
                        return@flatMapObservable Observable.empty<List<Trip>>()
                    } else {
                        return@flatMapObservable tripTableController.get().toObservable()
                                .doOnNext {
                                    Logger.info(this, "Migrating all receipts to use the correct custom_order_id. Version {}", previousMigration)
                                }
                                .flatMap {
                                    return@flatMap Observable.fromIterable(it)
                                }
                                .flatMap { trip ->
                                    return@flatMap receiptTableController.get(trip)
                                            .flatMapObservable { receipts ->
                                                when (getOrderingType(receipts)) {
                                                    None, Legacy -> reorderReceiptsByDate(receipts)
                                                    else -> fixPartialCustomIdOrdering(receipts)
                                                }
                                            }
                                }
                                .doOnComplete {
                                    Logger.info(this, "Successfully migrated all receipts to the correct custom_order_id")
                                    orderingMigrationStore.setOrderingMigrationHasOccurred(true)
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

                    // This calculates the "fake" days at the position that we've moved this item from
                    val fromPositionFakeDays = mutableReceiptsList[fromPosition].customOrderId / DAYS_TO_ORDER_FACTOR

                    // Next, remove this item
                    val movedReceipt = mutableReceiptsList.removeAt(fromPosition)

                    // Then ensure the movedReceipt gets modified to use the new toPositionFakeDays
                    val movedReceiptWithCorrectFakeDays = ReceiptBuilderFactory(movedReceipt).setCustomOrderId(toPositionFakeDays * DAYS_TO_ORDER_FACTOR).build()

                    // And add it back to the list at the proper position
                    mutableReceiptsList.add(toPosition, movedReceiptWithCorrectFakeDays)

                    // From here, count the number of receipts within this "fake day" and update the index of each. This process should
                    // ensure that all of our receipts are now properly ordered
                    val receiptsToUpdateList = mutableListOf<Pair<Receipt, Receipt?>>()
                    var toPositionCount = 0
                    var fromPositionCount = 0
                    for (i in mutableReceiptsList.indices.reversed()) {
                        val receipt = mutableReceiptsList[i]
                        // Whenever the receipt matches the "fake days" of the "to" position, update its custom_order_id
                        if (receipt.customOrderId / DAYS_TO_ORDER_FACTOR == toPositionFakeDays) {
                            val updated = ReceiptBuilderFactory(receipt).setCustomOrderId(toPositionFakeDays * DAYS_TO_ORDER_FACTOR + toPositionCount).build()
                            toPositionCount++
                            if (receipt == movedReceiptWithCorrectFakeDays) {
                                // Pass in the original reference to simplify our testing
                                receiptsToUpdateList.add(Pair(movedReceipt, updated))
                            } else {
                                if (receipt != updated) {
                                    receiptsToUpdateList.add(Pair(receipt, updated))
                                } else {
                                    // Nothing changed so don't bother with an update (ie pair it with a null)
                                    receiptsToUpdateList.add(Pair(receipt, null))
                                }
                            }
                        } else if (receipt.customOrderId / DAYS_TO_ORDER_FACTOR == fromPositionFakeDays) {
                            val updated = ReceiptBuilderFactory(receipt).setCustomOrderId(fromPositionFakeDays * DAYS_TO_ORDER_FACTOR + fromPositionCount).build()
                            fromPositionCount++
                            if (receipt == movedReceiptWithCorrectFakeDays) {
                                // Pass in the original reference to simplify our testing
                                receiptsToUpdateList.add(Pair(movedReceipt, updated))
                            } else {
                                if (receipt != updated) {
                                    receiptsToUpdateList.add(Pair(receipt, updated))
                                } else {
                                    // Nothing changed so don't bother with an update (ie pair it with a null)
                                    receiptsToUpdateList.add(Pair(receipt, null))
                                }
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
                                            .flatMap { receiptOptional ->
                                                if (receiptOptional.isPresent) {
                                                    Observable.just(receiptOptional.get())
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
     * [Receipt.date] field. When complete, all items will follow the [OrderingType.Ordered]
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
                    val ascendingReceipts = receipts.reversed()
                    ascendingReceipts.forEach {
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
                                        .flatMap { receiptOptional ->
                                            if (receiptOptional.isPresent) {
                                                Observable.just(receiptOptional.get())
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
     * Attempts to fix a potential bug that is the result of the [PartiallyOrdered] state that receipts can exist
     * within due to a bug with how receipts could be inserted. We correct this by finding receipts that are not
     * ordered (i.e. the custom order id ends with '999') and using information about the last local modification time
     * to properly order these items. This operates as follows:
     *
     * 1. We pass through all receipts and group them based off of the custom order id "interval"
     *    (i.e. customer order id / [DAYS_TO_ORDER_FACTOR])
     * 2. For that group on the same day, identify if any end with '999'
     * 3. If multiple end in '999', use the last local modification time to properly re-order these,
     *    so that the ones that were modified more recently have a larger custom order id
     *
     * Once we've identified all the incorrect ones, we will update each of these
     *
     * @param receipts the [List] of [Receipt] that were updated
     * @return a [Completable], which will emit success or failure based on the result of this operation
     */
    private fun fixPartialCustomIdOrdering(receipts: List<Receipt>): Observable<Receipt> {
        return Observable.fromCallable {
                    val receiptPairs = mutableListOf<Pair<Receipt, Receipt>>()

                    // First - identify all groups of receipts that occur on the same custom order id interval
                    val receiptsByInterval = mutableMapOf<Long, MutableList<Receipt>>()
                    receipts.forEach {
                        val customOrderIdInterval = it.customOrderId / DAYS_TO_ORDER_FACTOR
                        receiptsByInterval.getOrPut(customOrderIdInterval) { mutableListOf() }.add(it)
                    }

                    // Next we perform operations on each group
                    receiptsByInterval.values.forEach { grouping ->
                        // We sort the receipts within that group based on the customOrderId (or lastLocalModificationTime if those are equal)
                        // The outcome of this sort should result in items was a large custom order id appearing closer to index 0
                        grouping.sortWith(Comparator { receipt1, receipt2 ->
                            if (receipt1.customOrderId == receipt2.customOrderId) {
                                if (receipt1.date == receipt2.date) {
                                    return@Comparator (receipt1.syncState.lastLocalModificationTime.time - receipt2.syncState.lastLocalModificationTime.time).toInt()
                                } else {
                                    return@Comparator (receipt1.date.time - receipt2.date.time).toInt()
                                }
                            } else {
                                return@Comparator (receipt1.customOrderId - receipt2.customOrderId).toInt()
                            }
                        })

                        // Then we update the customOrderId based on the current alignment
                        val checkedReceiptsInGrouping = mutableListOf<Receipt>()
                        grouping.forEach {
                            // Determine the correct customOrderId, using the checked list (so we get the proper ordering)
                            val customOrderId = getCustomOrderId(it, checkedReceiptsInGrouping)
                            if (it.customOrderId != customOrderId) {
                                // Only update if we found a new customOrderId
                                val updatedReceipt = ReceiptBuilderFactory(it).setCustomOrderId(customOrderId).build()
                                receiptPairs.add(Pair(it, updatedReceipt))
                            }
                            // For each receipt that we check the customOrderId for, add it to this list to ensure we calculate the next id correctly
                            checkedReceiptsInGrouping.add(it)
                        }
                    }

                    return@fromCallable receiptPairs
                }
                .flatMap { list ->
                    Observable.fromIterable(list)
                            .flatMap {
                                receiptTableController.update(it.first, it.second, getDatabaseOperationMetadata(list, it))
                                        .flatMap { receiptOptional ->
                                            if (receiptOptional.isPresent) {
                                                Observable.just(receiptOptional.get())
                                            } else {
                                                Observable.error<Receipt>(Exception("Failed to update the receipt custom_order_id"))
                                            }
                                        }
                            }
                }
                .doOnError {
                    Logger.warn(this, "Failed re-ordered this receipts list by date to custom order id", it)
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
        val nextItemIndex = min(list.indexOf(item) + 1, list.size - 1)
        when {
            list.last() == item -> {
                // As the last item to update in this list, we'll update all listeners when it's done (ie non-Silent operation)
                return DatabaseOperationMetadata()
            }
            list[nextItemIndex].second == null -> {
                // If the next item index has a null value (i.e. it's not updating), check if this is true for the subsequent changes
                for (index in nextItemIndex until list.size) {
                    // If any subsequent items do require an update (i.e. the update/second item is NOT null), operate silently
                    if (list[index].second != null) {
                        return DatabaseOperationMetadata(OperationFamilyType.Silent)
                    }
                }
                // Otherwise treat this as the final item and issue a non-silent update
                return DatabaseOperationMetadata()
            }
            else -> {
                return DatabaseOperationMetadata(OperationFamilyType.Silent)
            }
        }
    }

    /**
     * Determines which [OrderingType] the receipts follow for a given set of receipts in a single trip.
     * We assume that if a single receipt uses the [OrderingType.None] approach, they all use this.
     * Similarly, we assume that if a single receipt uses the [OrderingType.Legacy] approach, they all use
     * this. In the former case, we assume the custom order id is 0 and we assume its large date number is
     * the latter
     *
     * @param receipts the [List] of [Receipt] items in a given trip to check
     * @return [OrderingType] that represents how these receipts have been ordered
     */
    private fun getOrderingType(receipts: List<Receipt>): OrderingType {
        var isPartiallyOrdered = false
        receipts.forEach {receipt ->
            when {
                receipt.customOrderId == 0L -> {
                    return None
                }
                receipt.customOrderId / DAYS_TO_ORDER_FACTOR > 20000 -> {
                    return Legacy
                }
                receipt.customOrderId.rem(DAYS_TO_ORDER_FACTOR) == PARTIAL_ORDERING_REMAINDER -> {
                    // Note: We don't exist early on this condition. None/Legacy take precedence
                    isPartiallyOrdered = true
                }
            }
        }
        return if (isPartiallyOrdered) {
            PartiallyOrdered
        } else {
            Ordered
        }
    }

    /**
     * Defines how the receipts are currently ordered. Ideally, all receipts should be ordered as
     * detailed by the [PartiallyOrdered] type, but this is not the case for a few legacy reasons:
     *
     * 1. When updating from [None] to [Legacy], we only upgraded the first trip that the user attempted to re-order
     * 2. When updating from [Legacy] to [PartiallyOrdered], we only upgraded the first trip that the user attempted to re-order
     *
     * As a result, we can have partial ordering split across multiple trips
     */
    enum class OrderingType {
        /**
         * Defines the ordering type that was present before we applied the custom order id (as opposed to ordering based off the date)
         */
        None,

        /**
         * The legacy approach is what occurred when we set the custom order id to the receipt's date, allowing each to operate independently
         */
        Legacy,

        /**
         * This is effectively the same as the [Ordered] case, but it may contain a scenario in which many receipts all share the same
         * position as 999 within that day (i.e. the number of days since the Unix epoch multiplied by 1000 + 999) instead of using the
         * current day as the offset
         */
        PartiallyOrdered,

        /**
         * This approach uses a mix of the receipt date and count of receipts within that day (https://s3.amazonaws.com/smartreceipts/Diagrams/SmartReceiptsCustomSortingOrderDesign.png)
         *
         * Practically speaking, we take the number of days since the Unix epoch multiplied by 1000 and add the current position within that day
         */
        Ordered
    }
}