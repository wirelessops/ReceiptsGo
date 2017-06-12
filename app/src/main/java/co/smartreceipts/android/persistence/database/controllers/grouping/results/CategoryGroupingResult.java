package co.smartreceipts.android.persistence.database.controllers.grouping.results;

import java.util.List;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.Receipt;

public class CategoryGroupingResult {

    private final Category category;

    private final List<Receipt> receipts;

    public CategoryGroupingResult(Category category, List<Receipt> receipts) {
        this.category = category;
        this.receipts = receipts;
    }

    public Category getCategory() {
        return category;
    }

    public List<Receipt> getReceipts() {
        return receipts;
    }
}
