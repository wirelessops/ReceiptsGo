package com.wops.receiptsgo.graphs;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.data.BaseEntry;
import com.github.mikephil.charting.data.Entry;
import com.google.common.base.Preconditions;

import java.util.List;

import com.wops.receiptsgo.graphs.entry.LabeledGraphEntry;

public class GraphUiIndicator {

    public enum GraphType {
        SummationByCategory, SummationByPaymentMethod, SummationByReimbursement, SummationByDate
    }

    private final GraphType graphType;
    private final List<? extends BaseEntry> entries;

    private GraphUiIndicator(@NonNull GraphType graphType, @NonNull List<? extends BaseEntry> entries) {
        this.graphType = Preconditions.checkNotNull(graphType);
        this.entries = Preconditions.checkNotNull(entries);
    }

    @NonNull
    public static GraphUiIndicator summationByCategory(@NonNull List<LabeledGraphEntry> entries) {
        return new GraphUiIndicator(GraphType.SummationByCategory, entries);
    }

    @NonNull
    public static GraphUiIndicator summationByReimbursement(@NonNull List<LabeledGraphEntry> entries ) {
        return new GraphUiIndicator(GraphType.SummationByReimbursement, entries);
    }

    @NonNull
    public static GraphUiIndicator summationByPaymentMethod(@NonNull List<LabeledGraphEntry> entries) {
        return new GraphUiIndicator(GraphType.SummationByPaymentMethod, entries);
    }

    @NonNull
    public static GraphUiIndicator summationByDate(@NonNull List<Entry> entries) {
        return new GraphUiIndicator(GraphType.SummationByDate, entries);
    }

    public GraphType getGraphType() {
        return graphType;
    }

    public List<? extends BaseEntry> getEntries() {
        return entries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GraphUiIndicator that = (GraphUiIndicator) o;

        if (graphType != that.graphType) return false;
        return entries.equals(that.entries);

    }

    @Override
    public int hashCode() {
        int result = graphType.hashCode();
        result = 31 * result + entries.hashCode();
        return result;
    }
}
