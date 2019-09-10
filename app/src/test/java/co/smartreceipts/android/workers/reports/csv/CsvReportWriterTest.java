package co.smartreceipts.android.workers.reports.csv;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class CsvReportWriterTest {

    // Class under test
    CsvReportWriter writer;

    File outputFile = new File("csv.csv");

    @Before
    public void setUp() {
        writer = new CsvReportWriter(outputFile);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @After
    public void tearDown() throws Exception {
        outputFile.delete();
    }

    @Test
    public void write() throws Exception {
        final String csv = "column1, column2, column3, column4,\nhello, שלום, привет, hello,";
        writer.write(csv);

        try (BOMInputStream bomInputStream = new BOMInputStream(new FileInputStream(outputFile))) {
            final String fileString = IOUtils.toString(bomInputStream, "UTF-8");
            assertEquals(fileString, csv);
            assertTrue(bomInputStream.hasBOM());
        }
    }

}