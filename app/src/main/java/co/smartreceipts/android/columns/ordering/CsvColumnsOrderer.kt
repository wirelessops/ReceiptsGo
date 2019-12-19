package co.smartreceipts.android.columns.ordering

import co.smartreceipts.core.di.scopes.ApplicationScope
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions
import co.smartreceipts.android.persistence.database.controllers.impl.CSVTableController
import co.smartreceipts.android.utils.rx.RxSchedulers
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