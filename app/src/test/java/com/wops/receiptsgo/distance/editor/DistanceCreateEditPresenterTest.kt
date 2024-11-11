package com.wops.receiptsgo.distance.editor

import com.wops.receiptsgo.R
import com.wops.receiptsgo.autocomplete.AutoCompletePresenter
import com.wops.receiptsgo.model.Distance
import com.wops.receiptsgo.widget.model.UiIndicator
import com.hadisatrio.optional.Optional
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DistanceCreateEditPresenterTest {

    // Class under test
    private lateinit var presenter: DistanceCreateEditPresenter

    private val view = mock<DistanceCreateEditView>()
    private val interactor = mock<DistanceCreateEditInteractor>()

    private val autoCompletePresenter = mock<AutoCompletePresenter<Distance>>()
    private val distanceAutoCompletePresenter = mock<DistanceAutoCompletePresenter>()
    private val distance = mock<Distance>()
    private val newDistance = mock<Distance>()

    @Before
    fun setUp() {
        whenever(view.createDistanceClicks).thenReturn(Observable.never())
        whenever(view.deleteDistanceClicks).thenReturn(Observable.never())
        whenever(view.updateDistanceClicks).thenReturn(Observable.never())
        whenever(view.hideAutoCompleteVisibilityClick).thenReturn(Observable.never())
        whenever(view.unHideAutoCompleteVisibilityClick).thenReturn(Observable.never())
        whenever(view.editableItem).thenReturn((distance))

        doNothing().whenever(interactor).deleteDistance(distance)
        whenever(interactor.createDistance(distance)).thenReturn(Observable.just(Optional.of(distance)))
        whenever(interactor.updateDistance(distance, newDistance)).thenReturn(Observable.just(Optional.of(newDistance)))

        presenter = DistanceCreateEditPresenter(view, interactor, autoCompletePresenter, distanceAutoCompletePresenter)
    }

    @Test
    fun deleteDistanceTest() {
        whenever(view.deleteDistanceClicks).thenReturn(Observable.just(distance))

        presenter.subscribe()

        verify(interactor).deleteDistance(distance)
        verify(view).present(UiIndicator.success())
    }

    @Test
    fun createDistanceTest() {
        whenever(view.createDistanceClicks).thenReturn(Observable.just(distance))

        presenter.subscribe()

        verify(interactor).createDistance(distance)
        verify(view).present(UiIndicator.success())
    }

    @Test
    fun updateDistanceTest() {
        whenever(view.updateDistanceClicks).thenReturn(Observable.just(newDistance))

        presenter.subscribe()

        verify(interactor).updateDistance(distance, newDistance)
        verify(view).present(UiIndicator.success())
    }

    @Test
    fun updateDistanceErrorTest() {
        whenever(view.updateDistanceClicks).thenReturn(Observable.just(newDistance))
        whenever(interactor.updateDistance(distance, newDistance)).thenReturn(Observable.just(Optional.absent()))

        presenter.subscribe()

        verify(interactor).updateDistance(distance, newDistance)
        verify(view).present(UiIndicator.error(R.string.distance_update_failed))
    }
}