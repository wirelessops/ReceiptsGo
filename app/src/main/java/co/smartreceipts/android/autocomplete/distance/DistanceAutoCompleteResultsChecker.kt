package co.smartreceipts.android.autocomplete.distance

import co.smartreceipts.android.autocomplete.AutoCompleteField
import co.smartreceipts.android.autocomplete.AutoCompleteResultsChecker
import co.smartreceipts.core.di.scopes.ApplicationScope
import co.smartreceipts.android.model.Distance
import javax.inject.Inject

/**
 * Provides a simple ability to check if a given [Distance] starts with a specific user input for a
 * well-defined [DistanceAutoCompleteField].
 */
@ApplicationScope
class DistanceAutoCompleteResultsChecker @Inject constructor() : AutoCompleteResultsChecker<Distance> {

    override fun matchesInput(input: CharSequence, field: AutoCompleteField, item: Distance): Boolean {
        return when (field) {
            DistanceAutoCompleteField.Location -> item.location.startsWith(input)
            DistanceAutoCompleteField.Comment -> item.comment.startsWith(input)
            else -> false
        }
    }

    override fun getValue(field: AutoCompleteField, item: Distance): CharSequence {
        return when (field) {
            DistanceAutoCompleteField.Location -> item.location.trim()
            DistanceAutoCompleteField.Comment -> item.comment.trim()
            else -> throw IllegalArgumentException("Unknown field type: $field")
        }
    }

}