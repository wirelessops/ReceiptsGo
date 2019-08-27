package co.smartreceipts.android.workers.reports.csv

import co.smartreceipts.android.model.Column
import co.smartreceipts.android.workers.reports.ReportResourcesManager
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyListOf
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import java.util.*

@RunWith(RobolectricTestRunner::class)
class CsvTableGeneratorTest {

    lateinit var csvTableGenerator: CsvTableGenerator<String>

    @Mock
    lateinit var column: Column<String>

    @Mock
    lateinit var reportResourceManager: ReportResourcesManager

    @Before
    @Throws(Exception::class)
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(column.headerStringResId).thenReturn(HEADER_ID)
        whenever(column.getValue(anyString())).thenReturn(VALUE)
        whenever(column.getFooter(anyListOf(String::class.java))).thenReturn(FOOTER)

        whenever(reportResourceManager.getFlexString(HEADER_ID)).thenReturn(HEADER)

        csvTableGenerator = CsvTableGenerator(
            reportResourceManager,
            Arrays.asList<Column<String>>(column, column, column), true, true
        )
    }

    @Test
    fun buildCsvWithEmptyData() {
        assertEquals("", csvTableGenerator.generate(emptyList()))
    }

    @Test
    fun buildCsvWithHeaderAndFooters() {
        val expected = "" +
                "header,header,header\n" +
                "value,value,value\n" +
                "value,value,value\n" +
                "footer,footer,footer\n"
        assertEquals(expected, csvTableGenerator.generate(Arrays.asList("1", "2")))
    }

    @Test
    fun buildCsvWithoutHeaderAndFooters() {
        val expected = "" +
                "value,value,value\n" +
                "value,value,value\n"
        csvTableGenerator = CsvTableGenerator(
            reportResourceManager,
            Arrays.asList<Column<String>>(column, column, column), false, false
        )
        assertEquals(expected, csvTableGenerator.generate(Arrays.asList("1", "2")))
    }

    @Test
    fun buildCsvWithNewLineCharacter() {
        val expected = "" +
                "header,header,header\n" +
                "\"va\nlue\",\"va\nlue\",\"va\nlue\"\n" +
                "\"va\nlue\",\"va\nlue\",\"va\nlue\"\n" +
                "footer,footer,footer\n"
        whenever(column.getValue(anyString())).thenReturn("va\nlue")
        assertEquals(expected, csvTableGenerator.generate(Arrays.asList("1", "2")))
    }

    @Test
    fun buildCsvWithQuoteCharacter() {
        val expected = "" +
                "header,header,header\n" +
                "\"\"\"value\"\"\",\"\"\"value\"\"\",\"\"\"value\"\"\"\n" +
                "\"\"\"value\"\"\",\"\"\"value\"\"\",\"\"\"value\"\"\"\n" +
                "footer,footer,footer\n"
        whenever(column.getValue(anyString())).thenReturn("\"value\"")
        assertEquals(expected, csvTableGenerator.generate(Arrays.asList("1", "2")))
    }

    companion object {
        private const val HEADER_ID = 1
        private const val HEADER = "header"
        private const val VALUE = "value"
        private const val FOOTER = "footer"
    }

}