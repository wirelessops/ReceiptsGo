package co.smartreceipts.android.persistence.database.controllers.grouping.results;

import co.smartreceipts.android.model.Price;

public class SumDateResult {

    private final int day;

    private final Price price;

    public SumDateResult(int day, Price price) {
        this.day = day;
        this.price = price;
    }

    public int getDay() {
        return day;
    }

    public Price getPrice() {
        return price;
    }
}
