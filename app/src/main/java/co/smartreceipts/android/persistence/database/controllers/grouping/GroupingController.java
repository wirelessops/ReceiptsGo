package co.smartreceipts.android.persistence.database.controllers.grouping;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.PriceCurrency;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.controllers.grouping.results.CategoryGroupingResult;
import co.smartreceipts.android.persistence.database.controllers.grouping.results.PaymentMethodGroupingResult;
import co.smartreceipts.android.persistence.database.controllers.grouping.results.ReimbursementGroupingResult;
import co.smartreceipts.android.persistence.database.controllers.grouping.results.SumCategoryGroupingResult;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class GroupingController {

    private final DatabaseHelper databaseHelper;

    @Inject
    public GroupingController(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public Observable<CategoryGroupingResult> getReceiptsGroupedByCategory(Trip trip) {

        return getReceiptsStream(trip)
                .groupBy(Receipt::getCategory)
                .flatMap(categoryReceiptGroupedObservable -> categoryReceiptGroupedObservable
                        .toList()
                        .map(receipts -> new CategoryGroupingResult(categoryReceiptGroupedObservable.getKey(), receipts))
                        .toObservable());
//                .observeOn(AndroidSchedulers.mainThread()); this should be in interactor
    }

    public Observable<SumCategoryGroupingResult> getSummationByCategory(Trip trip) {
        return getReceiptsGroupedByCategory(trip)
                .map(categoryGroupingResult -> {
                    List<Price> prices = new ArrayList<Price>();
                    List<Price> taxes = new ArrayList<Price>();

                    for (Receipt receipt : categoryGroupingResult.getReceipts()) {
                        prices.add(receipt.getPrice());
                        taxes.add(receipt.getTax());
                    }

                    Price price = new PriceBuilderFactory()
                            .setPrices(prices, trip.getTripCurrency())
                            .build();

                    Price tax = new PriceBuilderFactory()
                            .setPrices(taxes, trip.getTripCurrency())
                            .build();

                    return new SumCategoryGroupingResult(categoryGroupingResult.getCategory(),
                            trip.getTripCurrency(), price, tax, categoryGroupingResult.getReceipts().size());
                });
    }

    public Observable<PaymentMethodGroupingResult> getSummationByPaymentMethod(Trip trip) {
        return getReceiptsStream(trip)
                .filter(receipt -> receipt.getPaymentMethod() != null) // thus, we ignore receipts without defined payment method
                .groupBy(Receipt::getPaymentMethod)
                .flatMap(paymentMethodReceiptGroupedObservable -> paymentMethodReceiptGroupedObservable
                        .toList()
                        .map(receipts -> {
                            float price = getReceiptsPriceSum(receipts, trip.getTripCurrency());

                            return new PaymentMethodGroupingResult(paymentMethodReceiptGroupedObservable.getKey(), price);
                        })
                        .toObservable());
    }

    public Observable<ReimbursementGroupingResult> getSummationByReimbursment(Trip trip) {
        return getReceiptsStream(trip)
                .groupBy(Receipt::isReimbursable)
                .flatMap(booleanReceiptGroupedObservable -> booleanReceiptGroupedObservable
                        .toList()
                        .map(receipts -> {
                            float price = getReceiptsPriceSum(receipts, trip.getTripCurrency());
                            return new ReimbursementGroupingResult(booleanReceiptGroupedObservable.getKey(), price);
                        })
                        .toObservable());
    }

    private Observable<Receipt> getReceiptsStream(Trip trip) {
        return databaseHelper.getReceiptsTable()
                .get(trip)
                .subscribeOn(Schedulers.io())
                .toObservable()
                .flatMapIterable(receipts -> receipts);
    }

    private float getReceiptsPriceSum(List<Receipt> receipts, PriceCurrency desiredCurrency) {
        List<Price> prices = new ArrayList<Price>();

        for (Receipt receipt : receipts) {
            prices.add(receipt.getPrice());
        }

        return new PriceBuilderFactory()
                .setPrices(prices, desiredCurrency)
                .build()
                .getPriceAsFloat();

    }

}
