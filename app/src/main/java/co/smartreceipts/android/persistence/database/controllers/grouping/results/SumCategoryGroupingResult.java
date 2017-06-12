package co.smartreceipts.android.persistence.database.controllers.grouping.results;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.PriceCurrency;

public class SumCategoryGroupingResult {

    private final Category category;

    private final PriceCurrency currency;

    private final Price price, tax;

    private final int receiptsCount;

    public SumCategoryGroupingResult(Category category, PriceCurrency currency, Price price, Price tax, int receiptsCount) {
        this.category = category;
        this.currency = currency;
        this.price = price;
        this.tax = tax;
        this.receiptsCount = receiptsCount;
    }

    public Category getCategory() {
        return category;
    }

    public PriceCurrency getCurrency() {
        return currency;
    }

    public Price getPrice() {
        return price;
    }

    public Price getTax() {
        return tax;
    }

    public int getReceiptsCount() {
        return receiptsCount;
    }
}
