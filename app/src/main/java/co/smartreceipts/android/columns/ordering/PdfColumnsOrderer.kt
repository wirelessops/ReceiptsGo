package co.smartreceipts.android.columns.ordering

import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions
import co.smartreceipts.android.persistence.database.controllers.impl.PDFTableController
import co.smartreceipts.android.utils.rx.RxSchedulers
import io.reactivex.Scheduler
import javax.inject.Inject
import javax.inject.Named


/**
 * An implementation of the [AbstractColumnsOrderer] for PDF columns
 */
@ApplicationScope
class PdfColumnsOrderer @Inject constructor(pdfTableController: PDFTableController,
                                            receiptColumnDefinitions: ReceiptColumnDefinitions,
                                            @Named(RxSchedulers.IO) scheduler: Scheduler) : AbstractColumnsOrderer(pdfTableController, receiptColumnDefinitions, scheduler)