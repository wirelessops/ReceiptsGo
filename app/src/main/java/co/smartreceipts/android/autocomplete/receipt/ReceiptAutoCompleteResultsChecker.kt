package co.smartreceipts.android.autocomplete.receipt

import co.smartreceipts.android.autocomplete.AutoCompleteField
import co.smartreceipts.android.autocomplete.AutoCompleteResultsChecker
import co.smartreceipts.core.di.scopes.ApplicationScope
import co.smartreceipts.android.model.Receipt
import javax.inject.Inject


/**
 * Provides a simple ability to check if a given [Receipt] starts with a specific user input for a
 * well-defined [ReceiptAutoCompleteField].
 */
@ApplicationScope
class ReceiptAutoCompleteResultsChecker @Inject constructor() : AutoCompleteResultsChecker<Receipt> {

    override fun matchesInput(input: CharSequence, field: AutoCompleteField, item: Receipt): Boolean {
        return when (field) {
            ReceiptAutoCompleteField.Name -> item.name.startsWith(input)
            ReceiptAutoCompleteField.Comment -> item.comment.startsWith(input)
            else -> false
        }
    }

    override fun getValue(field: AutoCompleteField, item: Receipt): CharSequence {
        return when (field) {
            ReceiptAutoCompleteField.Name -> item.name.trim()
            ReceiptAutoCompleteField.Comment -> item.comment.trim()
            else -> throw IllegalArgumentException("Unknown field type: $field")
        }
    }

}