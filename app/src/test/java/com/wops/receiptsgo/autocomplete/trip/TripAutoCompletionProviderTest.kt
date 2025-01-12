package com.wops.receiptsgo.autocomplete.trip

import com.wops.receiptsgo.model.Trip
import com.wops.receiptsgo.persistence.database.controllers.impl.TripTableController
import org.junit.Assert.*
import org.junit.Before

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TripAutoCompletionProviderTest {

    private lateinit var provider: TripAutoCompletionProvider

    @Mock
    private lateinit var tableController: TripTableController

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        provider = TripAutoCompletionProvider(tableController)
    }

    @Test
    fun getAutoCompletionType() {
        assertEquals(Trip::class.java, provider.autoCompletionType)
    }

    @Test
    fun getTableController() {
        assertEquals(tableController, provider.tableController)
    }

    @Test
    fun getSupportedAutoCompleteFields() {
        assertEquals(listOf(TripAutoCompleteField.Name, TripAutoCompleteField.Comment, TripAutoCompleteField.CostCenter), provider.supportedAutoCompleteFields)
    }
}