package co.smartreceipts.android.autocomplete.receipt

import co.smartreceipts.android.autocomplete.AutoCompleteField
import co.smartreceipts.android.autocomplete.AutoCompletionProvider
import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.persistence.database.controllers.TableController
import co.smartreceipts.android.persistence.database.controllers.impl.ReceiptTableController
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