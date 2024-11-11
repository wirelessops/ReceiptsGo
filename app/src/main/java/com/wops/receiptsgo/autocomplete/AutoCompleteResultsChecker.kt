package com.wops.receiptsgo.autocomplete


/**
 * Manages the process by which we can check if a given item of [Type] matches a given user input
 * for a particular field
 */
interface AutoCompleteResultsChecker<Type> {

    /**
     * Checks if a given user input sequence matches a particular item in our results set
     *
     * @param input the user input [CharSequence]
     * @param field the [AutoCompleteField] to check
     * @param item the [Type] to check for
     *
     * @return true if this is a valid auto-completion result for these params. false otherwise
     */
    fun matchesInput(input: CharSequence, field: AutoCompleteField, item: Type) : Boolean

    /**
     * Gets the value of a particular field for a given item
     *
     * @param field the [AutoCompleteField] to check
     * @param item the [Type] to get the value of
     *
     * @return the [CharSequence] of the value of the [item] for this [field]
     */
    fun getValue(field: AutoCompleteField, item: Type) : CharSequence

}