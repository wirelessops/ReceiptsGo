package com.wops.receiptsgo.tooltip.model

/**
 * Tracks various types of user interactions that may have occurred with a tooltip
 */
enum class TooltipInteraction {

    /**
     * Indicates that the tooltip main view was clicked
     */
    TooltipClick,

    /**
     * Indicates that the close or cancel button was clicked
     */
    CloseCancelButtonClick,

    /**
     * Indicates the the 'Yes' button was clicked
     */
    YesButtonClick,

    /**
     * Indicates the the 'No' button was clicked
     */
    NoButtonClick,

}