package co.smartreceipts.android.autocomplete.distance

import co.smartreceipts.android.model.Distance
import co.smartreceipts.android.persistence.database.controllers.impl.DistanceTableController
import org.junit.Assert.*
import org.junit.Before

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DistanceAutoCompletionProviderTest {

    private lateinit var provider: DistanceAutoCompletionProvider

    @Mock
    private lateinit var tableController: DistanceTableController

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        provider = DistanceAutoCompletionProvider(tableController)
    }

    @Test
    fun getAutoCompletionType() {
        assertEquals(Distance::class.java, provider.autoCompletionType)
    }

    @Test
    fun getTableController() {
        assertEquals(tableController, provider.tableController)
    }

    @Test
    fun getSupportedAutoCompleteFields() {
        assertEquals(listOf(DistanceAutoCompleteField.Location), provider.supportedAutoCompleteFields)
    }
}