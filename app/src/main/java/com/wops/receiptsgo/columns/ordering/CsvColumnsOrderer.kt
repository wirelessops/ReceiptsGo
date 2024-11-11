package com.wops.receiptsgo.columns.ordering

import co.smartreceipts.core.di.scopes.ApplicationScope
import com.wops.receiptsgo.model.impl.columns.receipts.ReceiptColumnDefinitions
import com.wops.receiptsgo.persistence.database.controllers.impl.CSVTableController
import com.wops.receiptsgo.utils.rx.RxSchedulers
import io.reactivex.Scheduler
import javax.inject.Inject
import javax.inject.Named


/**
 * An implementation of the [AbstractColumnsOrderer] for CSV columns
 */
@ApplicationScope
class CsvColumnsOrderer @Inject constructor(csvTableController: CSVTableController,
                                            receiptColumnDefinitions: ReceiptColumnDefinitions,
                                            @Named(RxSchedulers.IO) scheduler: Scheduler) : AbstractColumnsOrderer(csvTableController, receiptColumnDefinitions, scheduler)