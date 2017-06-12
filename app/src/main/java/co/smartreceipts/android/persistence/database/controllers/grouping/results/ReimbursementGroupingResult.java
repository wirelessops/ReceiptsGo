package co.smartreceipts.android.persistence.database.controllers.grouping.results;

public class ReimbursementGroupingResult {

    private final boolean isReimbursable;

    private final float price;

    public ReimbursementGroupingResult(boolean isReimbursable, float price) {
        this.isReimbursable = isReimbursable;
        this.price = price;
    }

    public boolean isReimbursable() {
        return isReimbursable;
    }

    public float getPrice() {
        return price;
    }
}
