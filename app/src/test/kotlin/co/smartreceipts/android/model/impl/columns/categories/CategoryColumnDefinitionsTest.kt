package co.smartreceipts.android.model.impl.columns.categories

import co.smartreceipts.android.model.Column
import co.smartreceipts.android.sync.model.impl.DefaultSyncState
import co.smartreceipts.android.workers.reports.ReportResourcesManager
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class CategoryColumnDefinitionsTest {

    // Class under test
    private lateinit var categoryColumnDefinitions: CategoryColumnDefinitions

    @Mock
    private lateinit var reportResourceManager: ReportResourcesManager


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        whenever(reportResourceManager.getFlexString(any<Int>())).thenReturn(anyString)
    }

    @Test
    fun checkWhenMultiCurrency() {
        categoryColumnDefinitions = CategoryColumnDefinitions(reportResourceManager, true)

        val allColumns = categoryColumnDefinitions.allColumns

        assert(allColumns.isNotEmpty())
        assert(allColumns.size == CategoryColumnDefinitions.ActualDefinition.values().size)
        assert(allColumns.contains(CategoryExchangedPriceColumn(Column.UNKNOWN_ID, DefaultSyncState())))
    }

    @Test
    fun checkWhenNotMultiCurrency() {
        categoryColumnDefinitions = CategoryColumnDefinitions(reportResourceManager, false)

        val allColumns = categoryColumnDefinitions.allColumns

        assert(allColumns.isNotEmpty())
        assert(allColumns.size == CategoryColumnDefinitions.ActualDefinition.values().size - 1)
        assert(!allColumns.contains(CategoryExchangedPriceColumn(Column.UNKNOWN_ID, DefaultSyncState())))
    }

    companion object {
        private const val anyString = "string"
    }
}