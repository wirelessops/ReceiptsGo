package co.smartreceipts.android.persistence.database.controllers.grouping.results;

import co.smartreceipts.android.model.Price;

public class SumDateResult {

//    private final Date date;
    private final int day;

    private final Price price;

    public SumDateResult(/*Date date*/int day, Price price) {
//        this.date = date;
        this.day = day;
        this.price = price;
    }
//
//    public Date getDate() {
//        return date;
//    }


    public int getDay() {
        return day;
    }

    public Price getPrice() {
        return price;
    }
}
