package com.wops.receiptsgo.distance.editor

import com.wops.receiptsgo.DefaultObjects
import com.wops.receiptsgo.autocomplete.AutoCompleteResult
import com.wops.receiptsgo.autocomplete.distance.DistanceAutoCompleteField
import com.wops.receiptsgo.model.AutoCompleteUpdateEvent
import com.wops.receiptsgo.model.Distance
import com.wops.receiptsgo.model.factory.DistanceBuilderFactory
import com.hadisatrio.optional.Optional
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.joda.money.CurrencyUnit
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DistanceAutoCompletePresenterTest {

    // Class under test
    private lateinit var presenter: DistanceAutoCompletePresenter

    private lateinit var distance:Distance
    private lateinit var autoCompleteUpdateEventLocation:AutoCompleteUpdateEvent<Distance>
    private lateinit var autoCompleteResult: AutoCompleteResult<Distance>

    private val view = mock<DistanceCreateEditView>()
    private val interactor = mock<DistanceCreateEditInteractor>()
    private val newDistance = mock<Distance>()

    @Before
    fun setUp() {
        distance = DistanceBuilderFactory().setCurrency(CurrencyUnit.USD)
                .setTrip(DefaultObjects.newDefaultTrip()).build()
        autoCompleteResult = AutoCompleteResult(distance.location, distance)
        autoCompleteUpdateEventLocation = AutoCompleteUpdateEvent(autoCompleteResult, DistanceAutoCompleteField.Location, 0)

        presenter = DistanceAutoCompletePresenter(view, interactor)

        whenever(view.hideAutoCompleteVisibilityClick).thenReturn(Observable.never())
        whenever(view.unHideAutoCompleteVisibilityClick).thenReturn(Observable.never())
    }

    @Test
    fun hideAutoCompleteValueSucceeds() {
        whenever(view.hideAutoCompleteVisibilityClick).thenReturn(Observable.just(autoCompleteUpdateEventLocation))
        whenever(interactor.updateDistance(distance, DistanceBuilderFactory(distance)
                .setLocationHiddenFromAutoComplete(true)
                .build())).thenReturn(Observable.just(Optional.of(newDistance)))

        presenter.subscribe()

        verify(view).removeValueFromAutoComplete(autoCompleteUpdateEventLocation.position)
    }

    @Test
    fun unHideAutoCompleteValueSucceeds() {
        whenever(view.unHideAutoCompleteVisibilityClick).thenReturn(Observable.just(autoCompleteUpdateEventLocation))
        whenever(interactor.updateDistance(distance, DistanceBuilderFactory(distance)
                .setLocationHiddenFromAutoComplete(false)
                .build())).thenReturn(Observable.just(Optional.of(newDistance)))

        presenter.subscribe()

        verify(view).sendAutoCompleteUnHideEvent(autoCompleteUpdateEventLocation.position)
    }

    @Test
    fun unHideAutoCompleteValueErrors() {
        whenever(view.unHideAutoCompleteVisibilityClick).thenReturn(Observable.just(autoCompleteUpdateEventLocation))
        whenever(interactor.updateDistance(distance, DistanceBuilderFactory(distance)
                .setLocationHiddenFromAutoComplete(false)
                .build())).thenReturn(Observable.just(Optional.absent()))

        presenter.subscribe()

        verify(view).displayAutoCompleteError()
    }
}