package com.wops.receiptsgo.distance.editor

import co.smartreceipts.analytics.Analytics
import com.wops.receiptsgo.model.Distance
import com.wops.receiptsgo.persistence.database.controllers.impl.DistanceTableController
import com.wops.receiptsgo.persistence.database.operations.DatabaseOperationMetadata
import com.wops.receiptsgo.settings.UserPreferenceManager
import com.hadisatrio.optional.Optional
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test

class DistanceCreateEditInteractorTest {

    // Class under test
    private lateinit var interactor: DistanceCreateEditInteractor

    private val distanceTableController = mock<DistanceTableController>()
    private val userPreferenceManager = mock<UserPreferenceManager>()
    private val analytics = mock<Analytics>()
    private val distance = mock<Distance>()
    private val newDistance = mock<Distance>()


    @Before
    fun setUp() {
        interactor = DistanceCreateEditInteractor(
            distanceTableController,
            userPreferenceManager,
            analytics,
            Schedulers.trampoline(),
            Schedulers.trampoline()
        )

        doNothing().whenever(distanceTableController).delete(distance, DatabaseOperationMetadata())
        whenever(distanceTableController.insert(distance, DatabaseOperationMetadata())).thenReturn(Observable.just(Optional.of(distance)))
        whenever(distanceTableController.update(distance, newDistance, DatabaseOperationMetadata())).thenReturn(
            Observable.just(Optional.of(newDistance))
        )
    }

    @Test
    fun deleteDistanceTest() {
        interactor.deleteDistance(distance)

        verify(distanceTableController).delete(distance, DatabaseOperationMetadata())
    }

    @Test
    fun insertDistanceTest() {
        interactor.createDistance(distance).test()
            .assertComplete()
            .assertNoErrors()
            .assertResult(Optional.of(distance))

        verify(distanceTableController).insert(distance, DatabaseOperationMetadata())

    }

    @Test
    fun updateDistanceTest() {
        interactor.updateDistance(distance, newDistance).test()
            .assertNoErrors()
            .assertComplete()
            .assertResult(Optional.of(newDistance))

        verify(distanceTableController).update(distance, newDistance, DatabaseOperationMetadata())
    }


}