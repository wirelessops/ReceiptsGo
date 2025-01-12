package com.wops.receiptsgo.trips.editor.date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.sql.Date;

import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.settings.UserPreferenceManager;
import com.wops.receiptsgo.settings.catalog.UserPreference;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class TripDatesPresenterTest {

    private static final int DEFAULT_REPORT_DURATION_IN_DAYS = 5;
    private static final Date DATE = new Date(1518801797879L);
    private static final Date DATE_IN_5_DAYS = new Date(1519233797879L);

    @Mock
    TripDateView tripDateView;

    @Mock
    UserPreferenceManager userPreferenceManager;

    @Mock
    Trip trip;

    @Mock
    Consumer<Date> dateConsumer;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(userPreferenceManager.get(UserPreference.General.DefaultReportDuration)).thenReturn(DEFAULT_REPORT_DURATION_IN_DAYS);
        when(tripDateView.getStartDateChanges()).thenReturn(Observable.just(DATE));
        when(tripDateView.displayEndDate()).thenReturn(dateConsumer);
    }

    @Test
    public void subscribeWithNullTripToEditUpdatesEndDateBasedOnSettings() throws Exception {
        final TripDatesPresenter presenter = new TripDatesPresenter(tripDateView, userPreferenceManager, null);
        presenter.subscribe();
        verify(dateConsumer).accept(DATE_IN_5_DAYS);
    }

    @Test
    public void subscribeWithNonNullTripToEditDoesNothing() throws Exception {
        final TripDatesPresenter presenter = new TripDatesPresenter(tripDateView, userPreferenceManager, trip);
        presenter.subscribe();
        verifyZeroInteractions(dateConsumer);
    }

}