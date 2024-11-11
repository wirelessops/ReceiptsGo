package com.wops.receiptsgo.autocomplete

import com.wops.receiptsgo.persistence.database.controllers.TableController


/**
 * Defines a contract from which auto-completion behavior can be provided for a well-defined [Type].
 * We use this to determine the exact fields that we aim to use
 */
interface AutoCompletionProvider<Type> {

    /**
     * @return The [Class] of [Type] that this auto complete behavior will be provided for
     */
    val autoCompletionType : Class<Type>

    /**
     * @return A [TableController] of [Type], which will be used to interact with the table results
     */
    val tableController : TableController<Type>

    /**
     * @return A [List] of [AutoCompleteField], representing the available fields for auto-completion
     */
    val supportedAutoCompleteFields : List<AutoCompleteField>

}