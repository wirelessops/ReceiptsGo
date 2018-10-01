package co.smartreceipts.android.model

/**
 * A simple interface for any class that has pricing information via the [Price]
 * interface
 *
 * @author williambaumann
 */
interface Priceable {

    /**
     * Gets the price for this particular item
     *
     * @return the [Price]
     */
    val price: Price
}
