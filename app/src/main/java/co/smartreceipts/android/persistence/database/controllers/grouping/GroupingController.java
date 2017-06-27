package co.smartreceipts.android.persistence.database.controllers.grouping;

import android.content.Context;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.graphs.entry.LabeledGraphEntry;
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
import co.smartreceipts.android.persistence.database.controllers.grouping.results.SumDateResult;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

@ApplicationScope
public class GroupingController {

    private final DatabaseHelper databaseHelper;
    private final Context context;

    @Inject
    public GroupingController(DatabaseHelper databaseHelper, Context context) {
        this.databaseHelper = databaseHelper;
        this.context = context;
    }

    public Observable<CategoryGroupingResult> getReceiptsGroupedByCategory(Trip trip) {

        return getReceiptsStream(trip)
                .groupBy(Receipt::getCategory)
                .flatMap(categoryReceiptGroupedObservable -> categoryReceiptGroupedObservable
                        .toList()
                        .map(receipts -> new CategoryGroupingResult(categoryReceiptGroupedObservable.getKey(), receipts))
                        .toObservable());
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
                            Price price = getReceiptsPriceSum(receipts, trip.getTripCurrency());
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
                            Price price = getReceiptsPriceSum(receipts, trip.getTripCurrency());
                            return new ReimbursementGroupingResult(booleanReceiptGroupedObservable.getKey(), price);
                        })
                        .toObservable())
                .sorted((o1, o2) -> Boolean.compare(o1.isReimbursable(), o2.isReimbursable())); // non-reimbursable must be the first
    }

    public Single<List<Entry>> getSummationByDateAsGraphEntries(Trip trip) {
        return getReceiptsStream(trip)
                .groupBy(receipt -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(receipt.getDate());
                    return calendar.get(Calendar.DAY_OF_YEAR);
                })
                .flatMap(dateReceiptGroupedObservable -> dateReceiptGroupedObservable
                        .toList()
                        .map(receipts -> {
                            Price price = getReceiptsPriceSum(receipts, trip.getTripCurrency());
                            return new SumDateResult(dateReceiptGroupedObservable.getKey(), price);
                        })
                        .toObservable())
                .sorted((o1, o2) -> Integer.compare(o1.getDay(), o2.getDay()))
                .map(sumDateResult -> new Entry(sumDateResult.getDay(), sumDateResult.getPrice().getPriceAsFloat()))
                .toList();
    }

    public Single<List<LabeledGraphEntry>> getSummationByCategoryAsGraphEntries(Trip trip) {
        return getSummationByCategory(trip)
                .map(sumCategoryGroupingResult -> new LabeledGraphEntry(sumCategoryGroupingResult.getPrice().getPriceAsFloat(),
                        sumCategoryGroupingResult.getCategory().getName()))
                .toList();
    }

    public Single<List<LabeledGraphEntry>> getSummationByReimbursmentAsGraphEntries(Trip trip) {
        return getSummationByReimbursment(trip)
                .map(reimbursementGroupingResult -> {
                    if (reimbursementGroupingResult.isReimbursable()) {
                        return new LabeledGraphEntry(reimbursementGroupingResult.getPrice().getPriceAsFloat(),
                                context.getString(R.string.graphs_label_reimbursable));
                    } else {
                        return new LabeledGraphEntry(-reimbursementGroupingResult.getPrice().getPriceAsFloat(),
                                context.getString(R.string.graphs_label_non_reimbursable));
                    }
                })
                .toList();
    }

    public Single<List<LabeledGraphEntry>> getSummationByPaymentMethodAsGraphEntries(Trip trip) {
        return getSummationByPaymentMethod(trip)
                .map(paymentMethodGroupingResult -> new LabeledGraphEntry(paymentMethodGroupingResult.getPrice().getPriceAsFloat(),
                        paymentMethodGroupingResult.getPaymentMethod().getMethod()))
                .toList();
    }

    private Observable<Receipt> getReceiptsStream(Trip trip) {
        return databaseHelper.getReceiptsTable()
                .get(trip)
                .subscribeOn(Schedulers.io())
                .toObservable()
                .flatMapIterable(receipts -> receipts);
    }

    private Price getReceiptsPriceSum(List<Receipt> receipts, PriceCurrency desiredCurrency) {
        List<Price> prices = new ArrayList<Price>();

        for (Receipt receipt : receipts) {
            prices.add(receipt.getPrice());
        }

        return new PriceBuilderFactory()
                .setPrices(prices, desiredCurrency)
                .build();
    }

}
