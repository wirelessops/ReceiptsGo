package co.smartreceipts.android.autocomplete


/**
 * A simple data class, which will hold our auto-completion results. It contains two core fields:
 * a [displayName], which should be shown to the user and a [item], which contains the object that
 * was used to create this piece of data
 */
data class AutoCompleteResult<Type>(val displayName: CharSequence,
                                    val item: Type) {

    override fun toString(): String {
        return displayName.toString()
    }
}