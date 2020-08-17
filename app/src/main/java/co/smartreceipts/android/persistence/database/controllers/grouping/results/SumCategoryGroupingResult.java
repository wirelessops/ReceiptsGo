package co.smartreceipts.android.persistence.database.controllers.grouping.results;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import org.joda.money.CurrencyUnit;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.impl.MultiplePriceImpl;

public class SumCategoryGroupingResult {

    private final Category category;

    private final CurrencyUnit baseCurrency;

    private final MultiplePriceImpl netPrice, netTax;

    private final int receiptsCount;

    private final boolean isMultiCurrency;

    public SumCategoryGroupingResult(@NonNull Category category, @NonNull CurrencyUnit baseCurrency,
                                     @NonNull MultiplePriceImpl netPrice, @NonNull MultiplePriceImpl netTax, int receiptsCount) {
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
    public CurrencyUnit getBaseCurrency() {
        return baseCurrency;
    }

    @NonNull
    public MultiplePriceImpl getNetPrice() {
        return netPrice;
    }

    @NonNull
    public MultiplePriceImpl getNetTax() {
        return netTax;
    }

    public int getReceiptsCount() {
        return receiptsCount;
    }

    public boolean isMultiCurrency() {
        return isMultiCurrency;
    }
}
