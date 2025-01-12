package com.wops.receiptsgo.sync.cleanup

import com.wops.analytics.log.Logger
import com.wops.receiptsgo.model.Receipt
import com.wops.receiptsgo.persistence.PersistenceManager
import com.wops.receiptsgo.persistence.database.controllers.impl.ReceiptTableController
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata
import com.wops.receiptsgo.persistence.database.operations.OperationFamilyType
import com.wops.receiptsgo.persistence.database.tables.ReceiptsTable
import com.wops.receiptsgo.sync.provider.SyncProviderStore
import com.wops.core.di.scopes.ApplicationScope
import com.wops.core.sync.provider.SyncProvider
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * In situations in which no [SyncProvider.None] is designated as the desired sync provider, we can
 * potentially enter a situation in which items are marked for deletion on Google Drive but are never
 * actually deleted (as the delete never syncs to Google Drive). This utility class allows us to
 * clean up these entities
 */
@ApplicationScope
class MarkedForDeletionCleaner constructor(private val receiptsTable: ReceiptsTable,
                                           private val receiptTableController: ReceiptTableController,
                                           private val syncProviderStore: SyncProviderStore,
                                           private val backgroundScheduler: Scheduler) {

    @Inject
    constructor(persistenceManager: PersistenceManager,
                receiptTableController: ReceiptTableController,
                syncProviderStore: SyncProviderStore) : this(persistenceManager.database.receiptsTable, receiptTableController, syncProviderStore, Schedulers.io())

    /**
     * In situations in which no [SyncProvider.None] is designated as the desired sync provider, we can
     * potentially enter a situation in which items are marked for deletion on Google Drive but are never
     * actually deleted (as the delete never syncs to Google Drive). This utility method allows us to
     * clean up these entities
     */
    fun safelyDeleteAllOutstandingItems() {
        Observable.fromCallable {
                    return@fromCallable syncProviderStore.provider
                }
                .subscribeOn(backgroundScheduler)
                .flatMap {
                    if (it == SyncProvider.None) {
                        Logger.info(this, "No sync provider is currently configured. " +
                                "Checking if we should delete outstanding items that are marked for deletion.")
                        return@flatMap receiptsTable.getAllMarkedForDeletionItems(SyncProvider.GoogleDrive)
                                .doOnSuccess { receipts ->
                                    Logger.info(this, "Found {} receipts that are marked for deletion", receipts.size)
                                }
                                .flatMapObservable { receipts ->
                                    Observable.fromIterable(receipts)
                                }
                    } else {
                        return@flatMap Observable.empty<Receipt>()
                    }
                }
                .subscribe ({ receipt ->
                    receiptTableController.delete(receipt, DatabaseOperationMetadata(OperationFamilyType.Sync))
                }, {
                    Logger.error(this, "Our deletion stream was interrupted", it)
                })
    }
}