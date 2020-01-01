package co.smartreceipts.android.persistence.database.controllers.grouping;

import android.content.Context;

import com.github.mikephil.charting.data.Entry;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.core.di.scopes.ApplicationScope;
import co.smartreceipts.android.graphs.entry.LabeledGraphEntry;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.model.impl.ImmutableNetPriceImpl;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.persistence.database.controllers.grouping.results.CategoryGroupingResult;
import co.smartreceipts.android.persistence.database.controllers.grouping.results.SumCategoryGroupingResult;
import co.smartreceipts.android.persistence.database.controllers.grouping.results.SumDateResult;
import co.smartreceipts.android.persistence.database.controllers.grouping.results.SumPaymentMethodGroupingResult;
import co.smartreceipts.android.persistence.database.controllers.grouping.results.SumReimbursementGroupingResult;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

@ApplicationScope
public class GroupingController {

    private final DatabaseHelper databaseHelper;
    private final Context context;
    private final UserPreferenceManager preferenceManager;

    @Inject
    public GroupingController(DatabaseHelper databaseHelper, Context context, UserPreferenceManager preferenceManager) {
        this.databaseHelper = databaseHelper;
        this.context = context;
        this.preferenceManager = preferenceManager;
    }

    public Observable<CategoryGroupingResult> getReceiptsGroupedByCategory(Trip trip) {

        return getReceiptsStream(trip)
                .filter(receipt -> !preferenceManager.get(UserPreference.Receipts.OnlyIncludeReimbursable) || receipt.isReimbursable())
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

                    final Price price = new PriceBuilderFactory().setPrices(prices, trip.getTripCurrency()).build();
                    Preconditions.checkArgument(price instanceof ImmutableNetPriceImpl);
                    ImmutableNetPriceImpl priceNet = (ImmutableNetPriceImpl) price;

                    final Price tax = new PriceBuilderFactory().setPrices(taxes, trip.getTripCurrency()).build();
                    Preconditions.checkArgument(tax instanceof ImmutableNetPriceImpl);
                    ImmutableNetPriceImpl taxNet = (ImmutableNetPriceImpl) tax;

                    return new SumCategoryGroupingResult(categoryGroupingResult.getCategory(), trip.getTripCurrency(),
                            priceNet, taxNet, categoryGroupingResult.getReceipts().size());
                });
    }

    private Observable<SumPaymentMethodGroupingResult> getSummationByPaymentMethod(Trip trip) {
        return getReceiptsStream(trip)
                .filter(receipt -> !preferenceManager.get(UserPreference.Receipts.OnlyIncludeReimbursable) || receipt.isReimbursable())
                .filter(receipt -> !receipt.getPaymentMethod().equals(PaymentMethod.Companion.getNONE())) // thus, we ignore receipts without defined payment method
                .groupBy(Receipt::getPaymentMethod)
                .flatMap(paymentMethodReceiptGroupedObservable -> paymentMethodReceiptGroupedObservable
                        .toList()
                        .map(receipts -> {
                            Price price = getReceiptsPriceSum(receipts, trip.getTripCurrency());
                            return new SumPaymentMethodGroupingResult(paymentMethodReceiptGroupedObservable.getKey(), price);
                        })
                        .toObservable());
    }

    private Observable<SumReimbursementGroupingResult> getSummationByReimbursement(Trip trip) {
        return getReceiptsStream(trip)
                .groupBy(Receipt::isReimbursable)
                .flatMap(booleanReceiptGroupedObservable -> booleanReceiptGroupedObservable
                        .toList()
                        .map(receipts -> {
                            Price price = getReceiptsPriceSum(receipts, trip.getTripCurrency());
                            return new SumReimbursementGroupingResult(booleanReceiptGroupedObservable.getKey(), price);
                        })
                        .toObservable())
                .sorted((o1, o2) -> Boolean.compare(o1.isReimbursable(), o2.isReimbursable())); // non-reimbursable must be the first
    }

    public Single<List<Entry>> getSummationByDateAsGraphEntries(Trip trip) {
        return getReceiptsStream(trip)
                .filter(receipt -> !preferenceManager.get(UserPreference.Receipts.OnlyIncludeReimbursable) || receipt.isReimbursable())
                .groupBy(receipt -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(receipt.getDate());
                    calendar.setTimeZone(receipt.getTimeZone());
                    return (int) TimeUnit.MILLISECONDS.toDays(calendar.getTimeInMillis());
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
                .map(sumCategoryGroupingResult -> new LabeledGraphEntry(sumCategoryGroupingResult.getNetPrice().getPriceAsFloat(),
                        sumCategoryGroupingResult.getCategory().getName()))
                .toList();
    }

    public Single<List<LabeledGraphEntry>> getSummationByReimbursementAsGraphEntries(Trip trip) {
        return getSummationByReimbursement(trip)
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
