package co.smartreceipts.android.columns.ordering

import co.smartreceipts.android.model.Column
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.factory.ColumnBuilderFactory
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions
import co.smartreceipts.android.persistence.database.controllers.impl.ColumnTableController
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType
import co.smartreceipts.android.utils.log.Logger
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler


/**
 * Manages our ordering of our PDF & CSV columns, helping us to ensure that we can keep things in
 * a consistent order. This class also provides utility methods to allow us to insert a new column
 * at a specific position
 */
abstract class AbstractColumnsOrderer(private val columnTableController: ColumnTableController,
                                      private val receiptColumnDefinitions: ReceiptColumnDefinitions,
                                      private val scheduler: Scheduler) {

    /**
     * Inserts a given column definition, [definitionToInsert], after another column ([after]), which
     * may be in our current list. If [after] is not in our list, we will add [definitionToInsert] to
     * the end of the list
     *
     * @param definitionToInsert the definition to add to our list
     * @param after the item to add [definitionToInsert] after
     *
     * @return a [Completable], which will emit onComplete or onError based on the result of this operation
     */
    fun insertColumnAfter(definitionToInsert: ReceiptColumnDefinitions.ActualDefinition, after: ReceiptColumnDefinitions.ActualDefinition) : Completable {
        return columnTableController.get()
                .subscribeOn(scheduler)
                .map { columns ->
                    // First, loop through each item to see if we can find the 'after' index
                    var customOrderId = columns.size.toLong()
                    for (index in 0 until columns.size) {
                        if (columns[index].type == after.columnType) {
                            customOrderId = (index + 1).toLong()
                        }
                    }

                    // Use this to build the column that we need to insert
                    val columnToInsert = receiptColumnDefinitions.getColumnFromDefinition(
                            definition = definitionToInsert,
                            customOrderId = customOrderId
                    )

                    // To be safe here, we will update all of our column indices to the appropriate position
                    val columnsToUpdate = mutableListOf<Pair<Column<Receipt>, Column<Receipt>>>()
                    for (index in 0 until columns.size) {
                        val oldColumn = columns[index]
                        val newCustomOrderId = if (index >= customOrderId) index + 1 else index
                        val newColumn = ColumnBuilderFactory(receiptColumnDefinitions, oldColumn)
                                .setCustomOrderId(newCustomOrderId.toLong())
                                .build()
                        columnsToUpdate.add(Pair(oldColumn, newColumn))
                    }

                    return@map ColumnTableActionsData(columnToInsert, columnsToUpdate)
                }
                .flatMap { columnTableActionsData ->
                    // First, we perform our series of updates
                    Observable.fromIterable(columnTableActionsData.columnsToUpdate)
                            .flatMap { pair ->
                                columnTableController.update(pair.first, pair.second, DatabaseOperationMetadata(OperationFamilyType.Silent))
                                        .flatMap { resultOptional ->
                                            if (resultOptional.isPresent) {
                                                Observable.just(resultOptional.get())
                                            } else {
                                                Observable.error<Column<Receipt>>(Exception("Failed to update the column custom_order_id"))
                                            }
                                        }
                            }
                            .toList() // Use this to collect / wait for all update results
                            .flatMap { _ ->
                                columnTableController.insert(columnTableActionsData.columnToInsert, DatabaseOperationMetadata())
                                        .firstOrError()
                            }
                }
                .doOnError {
                    Logger.warn(this, "Failed to insert {} after {}", definitionToInsert, after)
                }
                .doOnSuccess {
                    Logger.info(this, "Successfully inserted {} after {}", definitionToInsert, after)
                }
                .ignoreElement()
    }

    /**
     * A private inner class, which we use to store the required metadata that we need to insert and
     * update metadata for our columns
     */
    private data class ColumnTableActionsData(
            val columnToInsert: Column<Receipt>,
            val columnsToUpdate: List<Pair<Column<Receipt>, Column<Receipt>>>)
}