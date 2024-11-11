package com.wops.receiptsgo.autocomplete

/**
 * Defines a distinct set of fields that can be used for auto-completion
 */
interface AutoCompleteField {

    /**
     * @return the [String] name of this field
     */
    fun name() : String
}