package co.smartreceipts.android.persistence.database.controllers.grouping.results;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.impl.ImmutableNetPriceImpl;

public class SumCategoryGroupingResult {

    private final Category category;

    private final PriceCurrency baseCurrency;

    private final ImmutableNetPriceImpl netPrice, netTax;

    private final int receiptsCount;

    private final boolean isMultiCurrency;

    public SumCategoryGroupingResult(@NonNull Category category, @NonNull PriceCurrency baseCurrency,
                                     @NonNull ImmutableNetPriceImpl netPrice, @NonNull ImmutableNetPriceImpl netTax, int receiptsCount) {
        this.category = Preconditions.checkNotNull(category);
        this.baseCurrency = Preconditions.checkNotNull(baseCurrency);
        this.netPrice = Preconditions.checkNotNull(netPrice);
        this.netTax = Preconditions.checkNotNull(netTax);
        this.receiptsCount = receiptsCount;
        this.isMultiCurrency = netPrice.getImmutableOriginalPrices().keySet().size() > 1;
    }

    @NonNull
    public Category getCategory() {
        return category;
    }

    @NonNull
    public PriceCurrency getBaseCurrency() {
        return baseCurrency;
    }

    @NonNull
    public ImmutableNetPriceImpl getNetPrice() {
        return netPrice;
    }

    @NonNull
    public ImmutableNetPriceImpl getNetTax() {
        return netTax;
    }

    public int getReceiptsCount() {
        return receiptsCount;
    }

    public boolean isMultiCurrency() {
        return isMultiCurrency;
    }
}
