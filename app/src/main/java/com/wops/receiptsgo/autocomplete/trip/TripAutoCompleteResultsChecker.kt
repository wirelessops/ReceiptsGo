package com.wops.receiptsgo.autocomplete.trip

import com.wops.receiptsgo.autocomplete.AutoCompleteField
import com.wops.receiptsgo.autocomplete.AutoCompleteResultsChecker
import com.wops.core.di.scopes.ApplicationScope
import com.wops.receiptsgo.model.Trip
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