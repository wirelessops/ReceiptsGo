package co.smartreceipts.android.model.impl.columns.categories

import co.smartreceipts.android.model.Keyed
import co.smartreceipts.core.sync.model.impl.DefaultSyncState
import co.smartreceipts.android.workers.reports.ReportResourcesManager
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
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

        whenever(reportResourceManager.getFlexString(any())).thenReturn(anyString)
    }

    @Test
    fun checkWhenNotMultiCurrencyTaxDisabled() {
        categoryColumnDefinitions = CategoryColumnDefinitions(reportResourceManager, multiCurrency = false, taxEnabled = false)

        val allColumns = categoryColumnDefinitions.allColumns

        assert(allColumns.isNotEmpty())
        assert(allColumns.size == CategoryColumnDefinitions.ActualDefinition.values().size - 2)
        assert(!allColumns.contains(CategoryExchangedPriceColumn(Keyed.MISSING_ID, DefaultSyncState())))
        assert(!allColumns.contains(CategoryTaxColumn(Keyed.MISSING_ID, DefaultSyncState())))
    }

    @Test
    fun checkWhenMultiCurrencyTaxDisabled() {
        categoryColumnDefinitions = CategoryColumnDefinitions(reportResourceManager, multiCurrency = true, taxEnabled = false)

        val allColumns = categoryColumnDefinitions.allColumns

        assert(allColumns.isNotEmpty())
        assert(allColumns.size == CategoryColumnDefinitions.ActualDefinition.values().size - 1)
        assert(!allColumns.contains(CategoryTaxColumn(Keyed.MISSING_ID, DefaultSyncState())))
    }

    @Test
    fun checkWhenNotMultiCurrencyTaxEnabled() {
        categoryColumnDefinitions = CategoryColumnDefinitions(reportResourceManager, multiCurrency = false, taxEnabled = true)

        val allColumns = categoryColumnDefinitions.allColumns

        assert(allColumns.isNotEmpty())
        assert(allColumns.size == CategoryColumnDefinitions.ActualDefinition.values().size - 1)
        assert(!allColumns.contains(CategoryExchangedPriceColumn(Keyed.MISSING_ID, DefaultSyncState())))
    }

    @Test
    fun checkWhenMultiCurrencyTaxEnabled() {
        categoryColumnDefinitions = CategoryColumnDefinitions(reportResourceManager, multiCurrency = true, taxEnabled = true)

        val allColumns = categoryColumnDefinitions.allColumns

        assert(allColumns.isNotEmpty())
        assert(allColumns.size == CategoryColumnDefinitions.ActualDefinition.values().size)
        assert(allColumns.contains(CategoryExchangedPriceColumn(Keyed.MISSING_ID, DefaultSyncState())))
        assert(allColumns.contains(CategoryTaxColumn(Keyed.MISSING_ID, DefaultSyncState())))
    }

    companion object {
        private const val anyString = "string"
    }
}