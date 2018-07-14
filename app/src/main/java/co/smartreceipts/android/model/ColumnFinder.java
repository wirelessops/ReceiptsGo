package co.smartreceipts.android.model;

public interface ColumnFinder {

    /**
     * This method is used for DB upgrade to find the right column type by column header value which was saved to old DB
     *
     * @param header the column header which was saved to the DB
     * @return column type, or -1 if couldn't determine column type
     */
    int getColumnTypeByHeaderValue(String header);
}
