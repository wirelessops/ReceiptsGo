package co.smartreceipts.android.model;

public interface Draggable<T> extends Comparable<T> {

    /**
     * @return - custom order id from the database
     */
    long getCustomOrderId();
}
