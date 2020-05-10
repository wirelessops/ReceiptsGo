package co.smartreceipts.android.trips.editor

import co.smartreceipts.analytics.Analytics
import co.smartreceipts.android.autocomplete.AutoCompleteResult
import co.smartreceipts.android.autocomplete.trip.TripAutoCompleteField
import co.smartreceipts.android.model.AutoCompleteUpdateEvent
import co.smartreceipts.android.model.Trip
import co.smartreceipts.android.model.factory.TripBuilderFactory
import co.smartreceipts.android.persistence.PersistenceManager
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController
import com.hadisatrio.optional.Optional
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TripAutoCompletePresenterTest {

    // Class under test
    private lateinit var presenter: TripCreateEditFragmentPresenter

    private lateinit var trip:Trip
    private lateinit var autoCompleteUpdateEventName:AutoCompleteUpdateEvent<Trip>
    private lateinit var autoCompleteResult: AutoCompleteResult<Trip>

    private val analytics = mock<Analytics>()
    private val tripTableController = mock<TripTableController>()
    private val persistenceManager = mock<PersistenceManager>()

    private val view = mock<TripCreateEditFragment>()
    private val newTrip = mock<Trip>()

    @Before
    fun setUp() {
        trip = TripBuilderFactory().build()
        autoCompleteResult = AutoCompleteResult(trip.name, trip)
        autoCompleteUpdateEventName = AutoCompleteUpdateEvent(autoCompleteResult, TripAutoCompleteField.Name, 0)

        presenter = TripCreateEditFragmentPresenter(view, analytics, tripTableController, persistenceManager)

        whenever(view.hideAutoCompleteVisibilityClick).thenReturn(Observable.never())
        whenever(view.unHideAutoCompleteVisibilityClick).thenReturn(Observable.never())
    }

    @Test
    fun hideAutoCompleteValueSucceeds() {
        whenever(view.hideAutoCompleteVisibilityClick).thenReturn(Observable.just(autoCompleteUpdateEventName))
        whenever(presenter.updateTrip(trip, TripBuilderFactory(trip)
                .setNameHiddenFromAutoComplete(true)
                .build())).thenReturn(Observable.just(Optional.of(newTrip)))

        presenter.subscribe()

        verify(view).removeValueFromAutoComplete(autoCompleteUpdateEventName.position)
    }

    @Test
    fun unHideAutoCompleteValueSucceeds() {
        whenever(view.unHideAutoCompleteVisibilityClick).thenReturn(Observable.just(autoCompleteUpdateEventName))
        whenever(presenter.updateTrip(trip, TripBuilderFactory(trip)
                .setNameHiddenFromAutoComplete(false)
                .build())).thenReturn(Observable.just(Optional.of(newTrip)))

        presenter.subscribe()

        verify(view).sendAutoCompleteUnHideEvent(autoCompleteUpdateEventName.position)
    }

    @Test
    fun unHideAutoCompleteValueErrors() {
        whenever(view.unHideAutoCompleteVisibilityClick).thenReturn(Observable.just(autoCompleteUpdateEventName))
        whenever(presenter.updateTrip(trip, TripBuilderFactory(trip)
                .setNameHiddenFromAutoComplete(false)
                .build())).thenReturn(Observable.just(Optional.absent()))

        presenter.subscribe()

        verify(view).displayAutoCompleteError()
    }
}