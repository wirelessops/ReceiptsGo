package com.wops.receiptsgo.graphs;

import android.content.Context;

import androidx.annotation.VisibleForTesting;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.wops.receiptsgo.R;
import com.wops.receiptsgo.graphs.entry.LabeledGraphEntry;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.persistence.database.controllers.grouping.GroupingController;
import com.wops.core.di.scopes.ApplicationScope;
import io.reactivex.Maybe;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;

@ApplicationScope
public class GraphsInteractor {

    public final static int PAYMENT_METHODS_MAX_COUNT = 4;

    private final GroupingController groupingController;
    private final Context context;
    private final Scheduler observeOnScheduler;

    @Inject
    public GraphsInteractor(Context context, GroupingController groupingController) {
        this(context, groupingController, AndroidSchedulers.mainThread());
    }

    @VisibleForTesting
    GraphsInteractor(Context context, GroupingController groupingController, Scheduler observeOnScheduler) {
        this.context = context;
        this.groupingController = groupingController;
        this.observeOnScheduler = observeOnScheduler;
    }

    public Maybe<GraphUiIndicator> getSummationByCategories(Trip trip) {
        return groupingController.getSummationByCategoryAsGraphEntries(trip)
                .filter(graphEntries -> !graphEntries.isEmpty())
                .flatMapSingleElement(Single::just)
                .map(GraphUiIndicator::summationByCategory)
                .observeOn(observeOnScheduler);
    }

    public Maybe<GraphUiIndicator> getSummationByReimbursement(Trip trip) {
        return groupingController.getSummationByReimbursementAsGraphEntries(trip)
                .filter(graphEntries -> graphEntries.size() == 2) // no need to show this chart if user have all receipts (non)reimbursable
                .map(GraphUiIndicator::summationByReimbursement)
                .observeOn(observeOnScheduler);
    }

    public Maybe<GraphUiIndicator> getSummationByPaymentMethod(Trip trip) {
        return groupingController.getSummationByPaymentMethodAsGraphEntries(trip)
                .filter(graphEntries -> !graphEntries.isEmpty())
                .flatMapSingleElement(graphEntries -> {
                    List<LabeledGraphEntry> sortedEntries = new ArrayList<>(graphEntries);
                    Collections.sort(sortedEntries);

                    if (sortedEntries.size() > PAYMENT_METHODS_MAX_COUNT) {
                        List<LabeledGraphEntry> finalEntries = new ArrayList<>(sortedEntries.subList(0, PAYMENT_METHODS_MAX_COUNT));

                        float lastEntryValue = 0;
                        for (int i = PAYMENT_METHODS_MAX_COUNT; i < sortedEntries.size(); i++) {
                            lastEntryValue += sortedEntries.get(i).getY();
                        }

                        finalEntries.add(new LabeledGraphEntry(lastEntryValue, context.getString(R.string.graphs_label_others)));

                        return Single.just(finalEntries);

                    } else {
                        return Single.just(sortedEntries);
                    }
                })
                .map(GraphUiIndicator::summationByPaymentMethod)
                .observeOn(observeOnScheduler);
    }

    public Maybe<GraphUiIndicator> getSummationByDate(Trip trip) {
        return groupingController.getSummationByDateAsGraphEntries(trip)
                .filter(graphEntries -> !graphEntries.isEmpty())
                .map(entries -> { // hack to fill days without receipts with 0
                    List<Entry> result = new ArrayList<>(entries);
                    List<Integer> filledDaysNumbers = new ArrayList<>();

                    int firstDay = (int) entries.get(0).getX();
                    int lastDay = (int) entries.get(entries.size() - 1).getX();

                    for (Entry entry : entries) {
                        filledDaysNumbers.add((int) entry.getX());
                    }

                    for (int i = firstDay; i < lastDay; i++) {
                        if (!filledDaysNumbers.contains(i)) {
                            result.add(new Entry(i, 0));
                        }
                    }

                    Collections.sort(result, (o1, o2) -> Float.compare(o1.getX(), o2.getX()));

                    return result;
                })
                .map(GraphUiIndicator::summationByDate)
                .observeOn(observeOnScheduler);
    }

}
