package co.smartreceipts.android.persistence.database.controllers.grouping.results;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.util.List;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.Receipt;

public class CategoryGroupingResult {

    private final Category category;

    private final List<Receipt> receipts;

    public CategoryGroupingResult(@NonNull Category category, @NonNull List<Receipt> receipts) {
        this.category = Preconditions.checkNotNull(category);
        this.receipts = Preconditions.checkNotNull(receipts);
    }

    @NonNull
    public Category getCategory() {
        return category;
    }

    @NonNull
    public List<Receipt> getReceipts() {
        return receipts;
    }
}
