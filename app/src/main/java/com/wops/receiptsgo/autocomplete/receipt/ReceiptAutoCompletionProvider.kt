package com.wops.receiptsgo.autocomplete.receipt

import com.wops.receiptsgo.autocomplete.AutoCompleteField
import com.wops.receiptsgo.autocomplete.AutoCompletionProvider
import com.wops.core.di.scopes.ApplicationScope
import com.wops.receiptsgo.model.Receipt
import com.wops.receiptsgo.persistence.database.controllers.TableController
import com.wops.receiptsgo.persistence.database.controllers.impl.ReceiptTableController
import java.util.*
import javax.inject.Inject

/**
 * Implements the [AutoCompletionProvider] contract for [Receipt] instances
 */
@ApplicationScope
class ReceiptAutoCompletionProvider(override val autoCompletionType: Class<Receipt>,
                                    override val tableController: TableController<Receipt>,
                                    override val supportedAutoCompleteFields: List<AutoCompleteField>) : AutoCompletionProvider<Receipt> {

    @Inject
    constructor(tableController: ReceiptTableController) :
            this(Receipt::class.java, tableController, Arrays.asList(ReceiptAutoCompleteField.Name, ReceiptAutoCompleteField.Comment))

}