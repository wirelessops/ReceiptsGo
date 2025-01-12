package com.wops.receiptsgo.persistence.database.controllers.grouping.results;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import com.wops.receiptsgo.model.Price;

public class SumReimbursementGroupingResult {

    private final boolean isReimbursable;

    private final Price price;

    public SumReimbursementGroupingResult(boolean isReimbursable, @NonNull Price price) {
        this.isReimbursable = isReimbursable;
        this.price = Preconditions.checkNotNull(price);
    }

    public boolean isReimbursable() {
        return isReimbursable;
    }

    @NonNull
    public Price getPrice() {
        return price;
    }
}
