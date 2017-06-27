package co.smartreceipts.android.persistence.database.controllers.grouping.results;

import co.smartreceipts.android.model.Price;

public class ReimbursementGroupingResult {

    private final boolean isReimbursable;

    private final Price price;

    public ReimbursementGroupingResult(boolean isReimbursable, Price price) {
        this.isReimbursable = isReimbursable;
        this.price = price;
    }

    public boolean isReimbursable() {
        return isReimbursable;
    }

    public Price getPrice() {
        return price;
    }
}
