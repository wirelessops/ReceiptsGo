package com.wops.receiptsgo.autocomplete


/**
 * A simple data class, which will hold our auto-completion results. It contains two core fields:
 * a [displayName], which should be shown to the user and a [firstItem], which contains the FIRST
 * object that was used to create this piece of data. All additional items (after the first) should
 * be added to the [additionalItems] list
 */
data class AutoCompleteResult<Type>(val displayName: CharSequence,
                                    val firstItem: Type,
                                    val additionalItems: MutableList<Type> = mutableListOf()) {

    /**
     * Returns the second item that matches [displayName], following the [firstItem]
     */
    fun getSecondItem() : Type? {
        return additionalItems.firstOrNull()
    }

    override fun toString(): String {
        return displayName.toString()
    }
}