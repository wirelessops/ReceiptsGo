package com.wops.receiptsgo.graphs;

import com.github.mikephil.charting.data.Entry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.List;

import com.wops.receiptsgo.graphs.entry.LabeledGraphEntry;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.settings.UserPreferenceManager;
import com.wops.receiptsgo.settings.catalog.UserPreference;
import io.reactivex.Maybe;
import io.reactivex.Single;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class GraphsPresenterTest {

    @InjectMocks
    GraphsPresenter presenter;

    @Mock
    GraphsInteractor interactor;

    @Mock
    GraphsView graphsView;

    @Mock
    DatabaseAssistant databaseAssistant;

    @Mock
    UserPreferenceManager preferenceManager;

    @Mock
    Trip trip;

    private List<LabeledGraphEntry> labeledEntries = Arrays.asList(new LabeledGraphEntry(0, "str1"), new LabeledGraphEntry(1, "str2"));
    private List<Entry> entries = Arrays.asList(new Entry(0, 0), new Entry(1, 1));

    private GraphUiIndicator sumByCategoryIndicator = GraphUiIndicator.summationByCategory(labeledEntries);
    private GraphUiIndicator sumByPaymentMethodIndicator = GraphUiIndicator.summationByPaymentMethod(labeledEntries);
    private GraphUiIndicator sumByReimbursementIndicator = GraphUiIndicator.summationByReimbursement(labeledEntries);
    private GraphUiIndicator sumByDateIndicator = GraphUiIndicator.summationByDate(entries);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(preferenceManager.get(UserPreference.Receipts.UsePaymentMethods)).thenReturn(false);

        when(interactor.getSummationByCategories(trip)).thenReturn(Maybe.just(sumByCategoryIndicator));
        when(interactor.getSummationByPaymentMethod(trip)).thenReturn(Maybe.just(sumByPaymentMethodIndicator));
        when(interactor.getSummationByDate(trip)).thenReturn(Maybe.just(sumByDateIndicator));
        when(interactor.getSummationByReimbursement(trip)).thenReturn(Maybe.just(sumByReimbursementIndicator));
    }

    @Test
    public void showEmptyText() {
        when(databaseAssistant.isReceiptsTableEmpty(trip)).thenReturn(Single.just(true));
        when(interactor.getSummationByCategories(trip)).thenReturn(Maybe.empty());
        when(interactor.getSummationByPaymentMethod(trip)).thenReturn(Maybe.empty());
        when(interactor.getSummationByDate(trip)).thenReturn(Maybe.empty());
        when(interactor.getSummationByReimbursement(trip)).thenReturn(Maybe.empty());

        presenter.subscribe(trip);

        verify(graphsView).showEmptyText(true);
        verify(graphsView, never()).present(any());
    }

    @Test(expected=IllegalStateException.class)
    public void throwExceptionWithoutTrip() {
        presenter.subscribe();
    }

    @Test
    public void showAllGraphs() {
        when(databaseAssistant.isReceiptsTableEmpty(trip)).thenReturn(Single.just(false));
        when(preferenceManager.get(UserPreference.Receipts.UsePaymentMethods)).thenReturn(true);

        presenter.subscribe(trip);

        verify(graphsView).showEmptyText(false);
        verify(graphsView).present(sumByCategoryIndicator);
        verify(graphsView).present(sumByPaymentMethodIndicator);
        verify(graphsView).present(sumByDateIndicator);
        verify(graphsView).present(sumByReimbursementIndicator);
    }

    @Test
    public void showAllGraphsWithoutPaymentMethods() {
        when(databaseAssistant.isReceiptsTableEmpty(trip)).thenReturn(Single.just(false));
        when(preferenceManager.get(UserPreference.Receipts.UsePaymentMethods)).thenReturn(false);

        presenter.subscribe(trip);

        verify(graphsView).showEmptyText(false);
        verify(graphsView).present(sumByCategoryIndicator);
        verify(graphsView, never()).present(sumByPaymentMethodIndicator);
        verify(graphsView).present(sumByDateIndicator);
        verify(graphsView).present(sumByReimbursementIndicator);
    }

}
