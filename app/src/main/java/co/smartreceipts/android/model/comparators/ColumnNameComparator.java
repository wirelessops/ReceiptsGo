package co.smartreceipts.android.model.comparators;

import android.support.annotation.NonNull;

import java.util.Comparator;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.utils.sorting.AlphabeticalCaseInsensitiveCharSequenceComparator;
import co.smartreceipts.android.workers.reports.ReportResourcesManager;

public class ColumnNameComparator<T extends Column<?>> implements Comparator<T> {

    private final Comparator<CharSequence> charSequenceComparator = new AlphabeticalCaseInsensitiveCharSequenceComparator();
    private final ReportResourcesManager reportResourcesManager;

    public ColumnNameComparator(ReportResourcesManager reportResourcesManager) {
        this.reportResourcesManager = reportResourcesManager;
    }

    @Override
    public int compare(@NonNull T column1, @NonNull T column2) {
        final String name1 = reportResourcesManager.getFlexString(column1.getHeaderStringResId());
        final String name2 = reportResourcesManager.getFlexString(column2.getHeaderStringResId());

        return charSequenceComparator.compare(name1, name2);
    }
}
