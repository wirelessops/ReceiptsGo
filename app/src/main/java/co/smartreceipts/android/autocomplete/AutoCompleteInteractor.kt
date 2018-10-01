package co.smartreceipts.android.autocomplete

import co.smartreceipts.android.settings.UserPreferenceManager
import co.smartreceipts.android.settings.catalog.UserPreference
import co.smartreceipts.android.utils.log.Logger
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
     * Fetches a list of auto-completion results for a specific [field], given the user's current
     * [input] for that field. We return a [List] to maintain a consistent ordering, but it is
     * expected that all [AutoCompleteResult] instances will have a unique
     * [AutoCompleteResult.displayName].
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
     * @return a [Maybe], which will emit a [List] of [AutoCompleteResult] of [Type] (or nothing)
     */
    fun getAutoCompleteResults(field: AutoCompleteField, input: CharSequence) : Maybe<List<AutoCompleteResult<Type>>> {
        // Confirm that the user has this setting enable
        if (userPreferenceManager.get(UserPreference.Receipts.EnableAutoCompleteSuggestions)) {
            // And that we've typed this exact amount of characters (as the adapters manage filtering afterwards)
            if (input.length == TEXT_LENGTH_TO_FETCH_RESULTS) {
                return provider.tableController.get()
                        .subscribeOn(backgroundScheduler)
                        .flatMapMaybe { getResults ->
                            val results = mutableListOf<AutoCompleteResult<Type>>()
                            val resultsSet = mutableSetOf<CharSequence>()
                            getResults.forEach {
                                if (resultsChecker.matchesInput(input, field, it)) {
                                    val displayName = resultsChecker.getValue(field, it)
                                    // Only allow input with new display names
                                    if (!resultsSet.contains(displayName)) {
                                        resultsSet.add(displayName)
                                        results.add(AutoCompleteResult(displayName, it))
                                    }
                                }
                            }
                            Maybe.just(results.toList())
                        }
                        .onErrorReturn {
                            emptyList()
                        }
                        .doOnSuccess {
                            Logger.info(this, "Adding {} auto-completion results to {}.", it.size, field)
                        }

            }
        }
        return Maybe.empty<List<AutoCompleteResult<Type>>>()
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