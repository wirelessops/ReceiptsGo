package com.wops.receiptsgo.columns.ordering

import com.wops.core.di.scopes.ApplicationScope
import com.wops.receiptsgo.model.impl.columns.receipts.ReceiptColumnDefinitions
import com.wops.receiptsgo.persistence.database.controllers.impl.PDFTableController
import com.wops.receiptsgo.utils.rx.RxSchedulers
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