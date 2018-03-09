package co.smartreceipts.android.receipts;

public class ReceiptHeaderItem implements ReceiptsListItem {

    private final String formattedDateText;

    public ReceiptHeaderItem(String formattedDateText) {
        this.formattedDateText = formattedDateText;
    }

    @Override
    public int getListItemType() {
        return ReceiptsListItem.TYPE_HEADER;
    }

    public String getHeaderText() {
        return formattedDateText;
    }
}
