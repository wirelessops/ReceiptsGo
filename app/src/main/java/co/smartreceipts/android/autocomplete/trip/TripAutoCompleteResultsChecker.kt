package co.smartreceipts.android.autocomplete.trip

import co.smartreceipts.android.autocomplete.AutoCompleteField
import co.smartreceipts.android.autocomplete.AutoCompleteResultsChecker
import co.smartreceipts.android.di.scopes.ApplicationScope
import co.smartreceipts.android.model.Trip
import javax.inject.Inject

/**
 * Provides a simple ability to check if a given [Trip] starts with a specific user input for a
 * well-defined [TripAutoCompleteField].
 */
@ApplicationScope
class TripAutoCompleteResultsChecker @Inject constructor() : AutoCompleteResultsChecker<Trip> {

    override fun matchesInput(input: CharSequence, field: AutoCompleteField, item: Trip): Boolean {
        return when (field) {
            TripAutoCompleteField.Name -> item.name.startsWith(input)
            TripAutoCompleteField.Comment -> item.comment.startsWith(input)
            TripAutoCompleteField.CostCenter -> item.costCenter.startsWith(input)
            else -> false
        }
    }

    override fun getValue(field: AutoCompleteField, item: Trip): CharSequence {
        return when (field) {
            TripAutoCompleteField.Name -> item.name.trim()
            TripAutoCompleteField.Comment -> item.comment.trim()
            TripAutoCompleteField.CostCenter -> item.costCenter.trim()
            else -> throw IllegalArgumentException("Unknown field type: $field")
        }
    }

}