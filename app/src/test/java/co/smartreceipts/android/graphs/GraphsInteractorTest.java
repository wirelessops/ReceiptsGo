package co.smartreceipts.android.graphs;

import android.content.Context;

import com.github.mikephil.charting.data.BaseEntry;
import com.github.mikephil.charting.data.Entry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.graphs.entry.LabeledGraphEntry;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.database.controllers.grouping.GroupingController;
import io.reactivex.Single;

import static co.smartreceipts.android.R.string.graphs_label_non_reimbursable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class GraphsInteractorTest {

    private static final String LABEL_1 = "label 1";
    private static final String LABEL_2 = "label 2";
    private static final String LABEL_3 = "label 3";
    private static final String LABEL_4 = "label 4";
    private static final String LABEL_5 = "label 5";
    private static final String LABEL_6 = "label 6";
    private static final String LABEL_7 = "label 7";
    private static final String LABEL_8 = "label 8";

    // Class under test
    GraphsInteractor interactor;

    @Mock
    GroupingController groupingController;

    @Mock
    Context context;

    @Mock
    Trip trip;

    private List<LabeledGraphEntry> entries;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        interactor = new GraphsInteractor(context, groupingController);

        when(context.getString(R.string.graphs_label_others)).thenReturn("Others");
        when(context.getString(R.string.graphs_label_reimbursable)).thenReturn("Reimbursable");
        when(context.getString(graphs_label_non_reimbursable)).thenReturn("Non-reimbursable");

    }

    @Test
    public void getSumByCategoryNormal() {
        entries = new ArrayList<>();
        entries.add(new LabeledGraphEntry(10, LABEL_1));
        entries.add(new LabeledGraphEntry(9, LABEL_2));
        entries.add(new LabeledGraphEntry(18, LABEL_3));
        entries.add(new LabeledGraphEntry(7, LABEL_4));
        entries.add(new LabeledGraphEntry(6, LABEL_5));
        entries.add(new LabeledGraphEntry(3, LABEL_6));

        when(groupingController.getSummationByCategoryAsGraphEntries(trip)).thenReturn(Single.just(entries));

        interactor.getSummationByCategories(trip)
                .test()
                .assertNoErrors()
                .assertComplete()
                .assertValue(graphUiIndicator -> {
                    if (graphUiIndicator.getGraphType() == GraphUiIndicator.GraphType.SummationByCategory) {
                        List<? extends BaseEntry> graphUiIndicatorEntries = graphUiIndicator.getEntries();
                        return graphUiIndicatorEntries.containsAll(entries) && graphUiIndicatorEntries.size() == entries.size();
                    }
                    return false;
                });
    }

    @Test
    public void getSumCategoryLot() {
        entries = new ArrayList<>();
        entries.add(new LabeledGraphEntry(10, LABEL_1));
        entries.add(new LabeledGraphEntry(9, LABEL_2));
        entries.add(new LabeledGraphEntry(18, LABEL_3));
        entries.add(new LabeledGraphEntry(7, LABEL_4));
        entries.add(new LabeledGraphEntry(6, LABEL_5));
        entries.add(new LabeledGraphEntry(3, LABEL_6));
        entries.add(new LabeledGraphEntry(2, LABEL_7));
        entries.add(new LabeledGraphEntry(1, LABEL_8));

        when(groupingController.getSummationByCategoryAsGraphEntries(trip)).thenReturn(Single.just(entries));

        List<LabeledGraphEntry> sortedEntries = new ArrayList<>(entries);
        Collections.sort(sortedEntries);

        List<LabeledGraphEntry> expectedEntries = sortedEntries.subList(0, GraphsInteractor.CATEGORIES_MAX_COUNT);
        expectedEntries.add(new LabeledGraphEntry(2 + 1, context.getString(R.string.graphs_label_others)));

        interactor.getSummationByCategories(trip)
                .test()
                .assertNoErrors()
                .assertComplete()
                .assertValue(graphUiIndicator -> {
                    if (graphUiIndicator.getGraphType() == GraphUiIndicator.GraphType.SummationByCategory) {
                        List<? extends BaseEntry> graphUiIndicatorEntries = graphUiIndicator.getEntries();
                        return graphUiIndicatorEntries.containsAll(expectedEntries) && graphUiIndicatorEntries.size() == expectedEntries.size();
                    }
                    return false;
                });
    }

    @Test
    public void getSumByReimbursmentNormal() {
        entries = new ArrayList<>();
        entries.add(new LabeledGraphEntry(15, context.getString(R.string.graphs_label_non_reimbursable)));
        entries.add(new LabeledGraphEntry(5, context.getString(R.string.graphs_label_reimbursable)));

        when(groupingController.getSummationByReimbursmentAsGraphEntries(trip)).thenReturn(Single.just(entries));

        List<LabeledGraphEntry> sortedEntries = new ArrayList<>(entries);
        Collections.sort(sortedEntries);

        interactor.getSummationByReimbursment(trip)
                .test()
                .assertNoErrors()
                .assertComplete()
                .assertResult(GraphUiIndicator.summationByReimbursment(sortedEntries));

    }

    @Test
    public void getNothingSumByReimbursmentIfOneType() {
        entries = new ArrayList<>();
        entries.add(new LabeledGraphEntry(10, context.getString(R.string.graphs_label_reimbursable)));

        when(groupingController.getSummationByReimbursmentAsGraphEntries(trip)).thenReturn(Single.just(entries));

        interactor.getSummationByReimbursment(trip)
                .test()
                .assertNoErrors()
                .assertComplete()
                .assertNoValues();
    }

    @Test
    public void getSumByPaymentMethodNormal() {
        entries = new ArrayList<>();
        entries.add(new LabeledGraphEntry(15, LABEL_1));
        entries.add(new LabeledGraphEntry(3, LABEL_2));
        entries.add(new LabeledGraphEntry(5, LABEL_3));

        when(groupingController.getSummationByPaymentMethodAsGraphEntries(trip)).thenReturn(Single.just(entries));

        List<LabeledGraphEntry> sortedEntries = new ArrayList<>(entries);
        Collections.sort(sortedEntries);

        interactor.getSummationByPaymentMethod(trip)
                .test()
                .assertNoErrors()
                .assertComplete()
                .assertValues(GraphUiIndicator.summationByPaymentMethod(sortedEntries));
    }

    @Test
    public void getSumByPaymentMethodLot() {
        entries = new ArrayList<>();
        entries.add(new LabeledGraphEntry(15, LABEL_1));
        entries.add(new LabeledGraphEntry(3, LABEL_2));
        entries.add(new LabeledGraphEntry(5, LABEL_3));
        entries.add(new LabeledGraphEntry(5, LABEL_4));
        entries.add(new LabeledGraphEntry(2, LABEL_5));
        entries.add(new LabeledGraphEntry(2, LABEL_6));

        when(groupingController.getSummationByPaymentMethodAsGraphEntries(trip)).thenReturn(Single.just(entries));

        List<LabeledGraphEntry> sortedEntries = new ArrayList<>(entries);
        Collections.sort(sortedEntries);

        List<LabeledGraphEntry> expectedEntries = sortedEntries.subList(0, GraphsInteractor.PAYMENT_METHODS_MAX_COUNT);
        expectedEntries.add(new LabeledGraphEntry(2 + 2, context.getString(R.string.graphs_label_others)));

        interactor.getSummationByPaymentMethod(trip)
                .test()
                .assertNoErrors()
                .assertComplete()
                .assertValues(GraphUiIndicator.summationByPaymentMethod(expectedEntries));
    }

    @Test
    public void fillEmptyDaysWithZero() {
        float someValue = 5;
        List<Entry> entries = new ArrayList<>(Arrays.asList(
                new Entry(2, someValue),
                new Entry(5, someValue),
                new Entry(7, someValue)

        ));

        when(groupingController.getSummationByDateAsGraphEntries(trip)).thenReturn(Single.just(entries));

        List<Entry> expectedEntries = new ArrayList<>(Arrays.asList(
                new Entry(2, someValue),
                new Entry(3, 0),
                new Entry(4, 0),
                new Entry(5, someValue),
                new Entry(6, 0),
                new Entry(7, someValue)
        ));

        GraphUiIndicator graphUiIndicator = interactor.getSummationByDate(trip).blockingGet();

        List<? extends BaseEntry> resultEntries = graphUiIndicator.getEntries();

        for (int i = 0; i < resultEntries.size(); i++) {
            Entry e1 = (Entry) resultEntries.get(i);
            Entry e2 = expectedEntries.get(i);

            assertTrue(e1.getX() == e2.getX() && e1.getY() == e2.getY());
        }

        assertEquals(expectedEntries.size(), resultEntries.size());
        assertEquals(GraphUiIndicator.GraphType.SummationByDate, graphUiIndicator.getGraphType());
    }

}
