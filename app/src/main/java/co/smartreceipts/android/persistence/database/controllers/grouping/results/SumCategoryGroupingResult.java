package co.smartreceipts.android.persistence.database.controllers.grouping.results;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.currency.PriceCurrency;

public class SumCategoryGroupingResult {

    private final Category category;

    private final PriceCurrency currency;

    private final Price price, tax;

    private final int receiptsCount;

    public SumCategoryGroupingResult(@NonNull Category category, @NonNull PriceCurrency currency,
                                     @NonNull Price price, @NonNull Price tax, int receiptsCount) {
        this.category = Preconditions.checkNotNull(category);
        this.currency = Preconditions.checkNotNull(currency);
        this.price = Preconditions.checkNotNull(price);
        this.tax = Preconditions.checkNotNull(tax);
        this.receiptsCount = receiptsCount;
    }

    @NonNull
    public Category getCategory() {
        return category;
    }

    @NonNull
    public PriceCurrency getCurrency() {
        return currency;
    }

    @NonNull
    public Price getPrice() {
        return price;
    }

    @NonNull
    public Price getTax() {
        return tax;
    }

    public int getReceiptsCount() {
        return receiptsCount;
    }
}
