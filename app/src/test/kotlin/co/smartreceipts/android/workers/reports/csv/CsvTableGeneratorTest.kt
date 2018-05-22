package co.smartreceipts.android.workers.reports.csv

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

import java.util.Arrays
import java.util.Collections

import co.smartreceipts.android.model.Column

import org.junit.Assert.*
import org.mockito.ArgumentMatchers.anyListOf
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`

@RunWith(RobolectricTestRunner::class)
class CsvTableGeneratorTest {

    lateinit var csvTableGenerator: CsvTableGenerator<String>

    @Mock
    lateinit var column: Column<String>

    @Before
    @Throws(Exception::class)
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        `when`(column.header).thenReturn(HEADER)
        `when`<String>(column.getValue(anyString())).thenReturn(VALUE)
        `when`(column.getFooter(anyListOf(String::class.java))).thenReturn(FOOTER)
        csvTableGenerator = CsvTableGenerator(Arrays.asList<Column<String>>(column, column, column), true, true)
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
        csvTableGenerator = CsvTableGenerator(Arrays.asList<Column<String>>(column, column, column), false, false)
        assertEquals(expected, csvTableGenerator.generate(Arrays.asList("1", "2")))
    }

    @Test
    fun buildCsvWithNewLineCharacter() {
        val expected = "" +
                "header,header,header\n" +
                "\"va\nlue\",\"va\nlue\",\"va\nlue\"\n" +
                "\"va\nlue\",\"va\nlue\",\"va\nlue\"\n" +
                "footer,footer,footer\n"
        `when`<String>(column.getValue(anyString())).thenReturn("va\nlue")
        assertEquals(expected, csvTableGenerator.generate(Arrays.asList("1", "2")))
    }

    @Test
    fun buildCsvWithQuoteCharacter() {
        val expected = "" +
                "header,header,header\n" +
                "\"\"\"value\"\"\",\"\"\"value\"\"\",\"\"\"value\"\"\"\n" +
                "\"\"\"value\"\"\",\"\"\"value\"\"\",\"\"\"value\"\"\"\n" +
                "footer,footer,footer\n"
        `when`<String>(column.getValue(anyString())).thenReturn("\"value\"")
        assertEquals(expected, csvTableGenerator.generate(Arrays.asList("1", "2")))
    }

    companion object {
        private const val HEADER = "header"
        private const val VALUE = "value"
        private const val FOOTER = "footer"
    }

}