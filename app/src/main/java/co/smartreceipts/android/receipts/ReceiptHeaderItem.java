package co.smartreceipts.android.receipts;

public class ReceiptHeaderItem implements ReceiptsListItem {

    private final String formattedDateText;
    private final long dateTime; // we need to keep some unique long value to be able to provide stable id for adapter

    public ReceiptHeaderItem(long dateTime, String formattedDateText) {
        this.formattedDateText = formattedDateText;
        this.dateTime = dateTime;
    }

    @Override
    public int getListItemType() {
        return ReceiptsListItem.TYPE_HEADER;
    }

    public String getHeaderText() {
        return formattedDateText;
    }

    public long getDateTime() {
        return dateTime;
    }
}
