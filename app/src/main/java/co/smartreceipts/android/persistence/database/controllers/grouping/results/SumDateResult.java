package co.smartreceipts.android.persistence.database.controllers.grouping.results;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.model.Price;

public class SumDateResult {

    private final int day;

    private final Price price;

    public SumDateResult(int day, @NonNull Price price) {
        this.day = day;
        this.price = Preconditions.checkNotNull(price);
    }

    public int getDay() {
        return day;
    }

    @NonNull
    public Price getPrice() {
        return price;
    }
}
