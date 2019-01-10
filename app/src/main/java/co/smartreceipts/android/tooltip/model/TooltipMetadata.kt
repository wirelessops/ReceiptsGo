package co.smartreceipts.android.tooltip.model

/**
 * Models the required metadata to display a specific tooltip to our end users
 */
data class TooltipMetadata(

        /**
         * @return the [TooltipType] that built this particular set of metadata
         */
        val tooltipType: TooltipType,

        /**
         * @return the [TooltipDisplayStyle] to display
         */
        val displayStyle: TooltipDisplayStyle,

        /**
         * @return the priority. Higher priorities outweigh lower ones (i.e. higher ones will be
         * displayed before lower ones)
         */
        val priority: Int,

        /**
         * @return the [String] for the tooltip message
         */
        val message: String,

        /**
         * @return if we should show a warning icon (ie '!') before the message
         */
        val showWarningIcon: Boolean,

        /**
         * @return if the 'X' icon should appear to allow the user to close the tooltip
         */
        val showCloseIcon: Boolean,

        /**
         * While this is similar to the [showCloseIcon], this may be more relevant in certain contexts,
         * where we need to explicitly show the user text instead of an icon. Instances may only have
         * this or [showCloseIcon] as true but not both.
         *
         * @return if an explicit 'Cancel' button should appear.
         */
        val showCancelButton: Boolean
) {

    /**
     * A convenience constructor, which uses the tooltip values when building this class
     */
    constructor(tooltipType: TooltipType,
                message: String) : this(tooltipType, tooltipType.displayStyle, tooltipType.priority, message, tooltipType.showWarningIcon, tooltipType.showCloseIcon, tooltipType.showCancelButton)

}