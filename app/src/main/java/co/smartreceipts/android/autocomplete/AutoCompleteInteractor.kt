package co.smartreceipts.android.autocomplete

import co.smartreceipts.android.autocomplete.distance.DistanceAutoCompleteField
import co.smartreceipts.android.autocomplete.receipt.ReceiptAutoCompleteField
import co.smartreceipts.android.autocomplete.trip.TripAutoCompleteField
import co.smartreceipts.android.model.Distance
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.analytics.log.Logger
import io.reactivex.Maybe
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

/**
 * Manages the use case interaction logic for fetching a list of auto-completion results
 */
class AutoCompleteInteractor<Type> constructor(private val provider: AutoCompletionProvider<Type>,
                                               private val resultsChecker: AutoCompleteResultsChecker<Type>,
                                               private val userPreferenceManager: UserPreferenceManager,
                                               private val backgroundScheduler: Scheduler) {

    constructor(provider: AutoCompletionProvider<Type>,
                resultsChecker: AutoCompleteResultsChecker<Type>,
                userPreferenceManager: UserPreferenceManager) : this(provider, resultsChecker, userPreferenceManager, Schedulers.io())

    /**
     * Fetches a mutable list of auto-completion results for a specific [field], given the user's current
     * [input] for that field. We return a [MutableList] to maintain a consistent ordering and allow
     * removal and additions to the adapter, but it is expected that all [AutoCompleteResult] instances
     * will have a unique [AutoCompleteResult.displayName].
     *
     * We return a [Maybe] from this, since we except to either have a valid list of nothing,
     * depending on if the user has enabled the [UserPreference.Receipts.EnableAutoCompleteSuggestions]
     * suggestion and if (s)he has only typed a single character. Once the user has typed more than
     * one character, we defer the filtering operation to what is natively included in the underlying
     * ArrayAdapter and hence don't need to continually emit results
     *
     * @param field the [AutoCompleteField] to use
     * @param input the current user input [CharSequence]
     *
     * @return a [Maybe], which will emit a [MutableList] of [AutoCompleteResult] of [Type] (or nothing)
     */
    fun getAutoCompleteResults(field: AutoCompleteField, input: CharSequence) : Maybe<MutableList<AutoCompleteResult<Type>>> {
        // Confirm that the user has this setting enable
        if (userPreferenceManager[UserPreference.Receipts.EnableAutoCompleteSuggestions]) {
            // And that we've typed this exact amount of characters (as the adapters manage filtering afterwards)
            if (input.length >= TEXT_LENGTH_TO_FETCH_RESULTS) {
                return provider.tableController.get()
                        .subscribeOn(backgroundScheduler)
                        .flatMapMaybe { getResults ->
                            val results = mutableListOf<AutoCompleteResult<Type>>()
                            val resultsSet = mutableMapOf<CharSequence, AutoCompleteResult<Type>>()
                            getResults.forEach {
                                if (resultsChecker.matchesInput(input, field, it)) {
                                    // make sure value wasn't removed by user
                                    val removedByUser = when (it) {
                                        is Receipt -> when (field) {
                                            ReceiptAutoCompleteField.Name -> it.autoCompleteMetadata.isNameHiddenFromAutoComplete
                                            ReceiptAutoCompleteField.Comment -> it.autoCompleteMetadata.isCommentHiddenFromAutoComplete
                                            else -> false
                                        }
                                        is Trip -> when (field) {
                                            TripAutoCompleteField.Name -> it.autoCompleteMetadata.isNameHiddenFromAutoComplete
                                            TripAutoCompleteField.Comment -> it.autoCompleteMetadata.isCommentHiddenFromAutoComplete
                                            TripAutoCompleteField.CostCenter -> it.autoCompleteMetadata.isCostCenterHiddenFromAutoComplete
                                            else -> false
                                        }
                                        is Distance -> when (field) {
                                            DistanceAutoCompleteField.Location -> it.autoCompleteMetadata.isLocationHiddenFromAutoComplete
                                            DistanceAutoCompleteField.Comment -> it.autoCompleteMetadata.isCommentHiddenFromAutoComplete
                                            else -> false
                                        }
                                        else -> false
                                    }

                                    if (!removedByUser) {
                                        val displayName = resultsChecker.getValue(field, it)
                                        // Only allow input with new display names
                                        if (!resultsSet.contains(displayName)) {
                                            val result = AutoCompleteResult(displayName, it)
                                            resultsSet[displayName] = result
                                            results.add(result)
                                        } else {
                                            resultsSet[displayName]!!.additionalItems.add(it)
                                        }
                                    }
                                }
                            }
                            Maybe.just(results)
                        }
                        .onErrorReturn {
                            mutableListOf()
                        }
                        .doOnSuccess {
                            Logger.info(this, "Adding {} auto-completion results to {}.", it.size, field)
                        }
            }
        }
        return Maybe.empty()
    }

    /**
     * @return A [List] of [AutoCompleteField], representing the available fields for auto-completion
     * as determined by the [AutoCompletionProvider]
     */
    val supportedAutoCompleteFields : List<AutoCompleteField> = provider.supportedAutoCompleteFields

    companion object {
        /**
         * We only fetch results when the user has entered a single character
         */
        private const val TEXT_LENGTH_TO_FETCH_RESULTS = 1
    }
}