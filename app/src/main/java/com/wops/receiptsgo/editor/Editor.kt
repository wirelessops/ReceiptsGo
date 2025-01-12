package com.wops.receiptsgo.editor

/**
 * A simple contract that allows us to access an optional model of [Type] that we're attempting to edit
 */
interface Editor<Type> {

    /**
     * @return the [Type] we're trying to edit (if one is present)
     */
    val editableItem: Type?
}