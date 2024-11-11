package com.wops.receiptsgo.persistence.database.controllers.alterations;

import androidx.test.core.app.ApplicationProvider;

import com.squareup.picasso.Picasso;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.sql.Date;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import com.wops.receiptsgo.model.Receipt;
import com.wops.receiptsgo.model.Trip;
import com.wops.receiptsgo.model.factory.BuilderFactory1;
import com.wops.receiptsgo.model.factory.ReceiptBuilderFactory;
import com.wops.receiptsgo.persistence.database.tables.ReceiptsTable;
import dagger.Lazy;
import io.reactivex.Single;
import wb.android.storage.StorageManager;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

// TODO: Re-write this class to test with real models instead of mocks
@RunWith(RobolectricTestRunner.class)
public class ReceiptTableActionAlterationsTest {

    private static final Date DATE = new Date(1512778546145L);
    private static final long CUSTOM_ORDER_ID = 17509000L;
    private static final TimeZone TIMEZONE = TimeZone.getTimeZone("America/New_York");

    // Class under test
    ReceiptTableActionAlterations receiptTableActionAlterations;

    File file1;

    File file2;

    @Mock
    ReceiptsTable receiptsTable;

    @Mock
    StorageManager storageManager;

    @Mock
    BuilderFactory1<Receipt, ReceiptBuilderFactory> receiptBuilderFactoryFactory;

    @Mock
    ReceiptBuilderFactory receiptBuilderFactory;

    @Mock
    Lazy<Picasso> picassoLazy;

    @Mock
    Picasso picasso;

    @Mock
    Receipt receipt;

    @Mock
    Trip trip;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(receipt.getTrip()).thenReturn(trip);
        when(receipt.getDate()).thenReturn(DATE);
        when(receipt.getTimeZone()).thenReturn(TIMEZONE);
        when(trip.getDirectory()).thenReturn(new File(System.getProperty("java.io.tmpdir")));
        when(receiptBuilderFactory.build()).thenReturn(receipt);
        when(receiptBuilderFactoryFactory.build(receipt)).thenReturn(receiptBuilderFactory);
        when(receiptBuilderFactory.setIndex(anyInt())).thenReturn(receiptBuilderFactory);
        when(picassoLazy.get()).thenReturn(picasso);

        doAnswer(invocation -> new File((String) invocation.getArgument(1))).when(storageManager).getFile(any(File.class), anyString());
        doAnswer(invocation -> new File((String) invocation.getArgument(1))).when(storageManager).rename(any(File.class), anyString());
        doAnswer(invocation -> {
            when(receipt.getFile()).thenReturn(invocation.getArgument(0));
            return receiptBuilderFactory;
        }).when(receiptBuilderFactory).setFile(any(File.class));
        doAnswer(invocation -> {
            when(receipt.getIndex()).thenReturn((Integer) invocation.getArguments()[0]);
            return receiptBuilderFactory;
        }).when(receiptBuilderFactory).setIndex(anyInt());
        doAnswer(invocation -> {
            when(receipt.getIndex()).thenReturn((Integer) invocation.getArguments()[0]);
            return receiptBuilderFactory;
        }).when(receiptBuilderFactory).setIndex(anyInt());
        doAnswer(invocation -> {
            when(receipt.getCustomOrderId()).thenReturn((Long) invocation.getArguments()[0]);
            return receiptBuilderFactory;
        }).when(receiptBuilderFactory).setCustomOrderId(anyLong());

        receiptTableActionAlterations = new ReceiptTableActionAlterations(ApplicationProvider.getApplicationContext(), receiptsTable, storageManager, receiptBuilderFactoryFactory, picassoLazy);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @After
    public void tearDown() {
        if (file1 != null) {
            file1.delete();
        }
        if (file2 != null) {
            file2.delete();
        }
    }

    @Test
    public void preInsertWithoutFile() {
        final String name = "name";
        final List<Receipt> receiptsInTrip = Arrays.asList(mock(Receipt.class), mock(Receipt.class), mock(Receipt.class));
        when(receiptsTable.get(trip)).thenReturn(Single.just(receiptsInTrip));
        when(receipt.getName()).thenReturn(name);

        receiptTableActionAlterations.preInsert(receipt)
                .test()
                .assertValue(receipt)
                .assertComplete()
                .assertNoErrors();

        assertNull(receipt.getFile());
        verify(receiptBuilderFactory).setIndex(4);
        verify(receiptBuilderFactory).setCustomOrderId(CUSTOM_ORDER_ID);
    }

    @Test
    public void preInsertWithFile() {
        final String name = "name";
        final List<Receipt> receiptsInTrip = Arrays.asList(mock(Receipt.class), mock(Receipt.class), mock(Receipt.class));
        when(receiptsTable.get(trip)).thenReturn(Single.just(receiptsInTrip));
        when(receipt.getName()).thenReturn(name);
        when(receipt.getFile()).thenReturn(new File("12345.jpg"));

        receiptTableActionAlterations.preInsert(receipt)
                .test()
                .assertValue(receipt)
                .assertComplete()
                .assertNoErrors();

        assertEquals(new File("4_name.jpg"), receipt.getFile());
        verify(receiptBuilderFactory).setIndex(4);
        verify(receiptBuilderFactory).setCustomOrderId(CUSTOM_ORDER_ID);
    }

    @Test
    public void preInsertWithIllegalCharactersInName() {
        final String name = "before_|\\\\?*<\\:>+[]/'\n\r\t\0\f_after";
        final List<Receipt> receiptsInTrip = Arrays.asList(mock(Receipt.class), mock(Receipt.class), mock(Receipt.class));
        when(receiptsTable.get(trip)).thenReturn(Single.just(receiptsInTrip));
        when(receipt.getName()).thenReturn(name);
        when(receipt.getFile()).thenReturn(new File("12345.jpg"));

        receiptTableActionAlterations.preInsert(receipt)
                .test()
                .assertValue(receipt)
                .assertComplete()
                .assertNoErrors();

        assertEquals(new File("4_before__after.jpg"), receipt.getFile());
        verify(receiptBuilderFactory).setIndex(4);
        verify(receiptBuilderFactory).setCustomOrderId(CUSTOM_ORDER_ID);
    }

    @Test
    public void preUpdateWithoutFile() {
        final List<Receipt> receiptsInTrip = Arrays.asList(mock(Receipt.class), mock(Receipt.class), mock(Receipt.class));
        when(receiptsTable.get(trip)).thenReturn(Single.just(receiptsInTrip));
        final Receipt oldReceipt = mock(Receipt.class);
        when(oldReceipt.getDate()).thenReturn(DATE);
        when(oldReceipt.getTimeZone()).thenReturn(TIMEZONE);
        when(receipt.getFile()).thenReturn(null);

        receiptTableActionAlterations.preUpdate(oldReceipt, receipt)
                .test()
                .assertValue(receipt)
                .assertComplete()
                .assertNoErrors();
    }

    @Test
    public void preUpdateWithBrandNewFile() {
        final String name = "name";
        final List<Receipt> receiptsInTrip = Arrays.asList(mock(Receipt.class), mock(Receipt.class), mock(Receipt.class));
        when(receiptsTable.get(trip)).thenReturn(Single.just(receiptsInTrip));
        final Receipt oldReceipt = mock(Receipt.class);
        when(oldReceipt.getFile()).thenReturn(null);
        when(oldReceipt.getDate()).thenReturn(DATE);
        when(oldReceipt.getTimeZone()).thenReturn(TIMEZONE);
        when(receipt.getIndex()).thenReturn(4);
        when(receipt.getName()).thenReturn(name);
        when(receipt.getFile()).thenReturn(new File("12345.jpg"));

        final List<Receipt> result = receiptTableActionAlterations.preUpdate(oldReceipt, receipt)
                .test()
                .assertComplete()
                .assertNoErrors()
                .values();

        assertEquals(1, result.size());
        final Receipt receipt = result.get(0);
        assertEquals(new File("4_name.jpg"), receipt.getFile());
    }

    @Test
    public void preUpdateWithUpdatedFile() throws Exception {
        this.file1 = new File(System.getProperty("java.io.tmpdir"), "1_name.jpg");
        this.file2 = new File(System.getProperty("java.io.tmpdir"), "12345.jpg");
        assertTrue(this.file1.createNewFile());
        assertTrue(this.file2.createNewFile());

        final String name = "name";
        final List<Receipt> receiptsInTrip = Arrays.asList(mock(Receipt.class), mock(Receipt.class), mock(Receipt.class));
        when(receiptsTable.get(trip)).thenReturn(Single.just(receiptsInTrip));
        final Receipt oldReceipt = mock(Receipt.class);
        when(oldReceipt.getDate()).thenReturn(DATE);
        when(oldReceipt.getTimeZone()).thenReturn(TIMEZONE);
        when(oldReceipt.getIndex()).thenReturn(1);
        when(oldReceipt.getName()).thenReturn(name);
        when(oldReceipt.getFile()).thenReturn(file1);
        when(receipt.getIndex()).thenReturn(1);
        when(receipt.getName()).thenReturn(name);
        when(receipt.getFile()).thenReturn(file2);

        final List<Receipt> onNextResults = receiptTableActionAlterations.preUpdate(oldReceipt, receipt)
                        .test()
                        .assertComplete()
                        .assertNoErrors()
                        .values();

        assertNotNull(onNextResults);
        assertEquals(1, onNextResults.size());
        final Receipt result = onNextResults.get(0);
        assertNotNull(result.getFile());
        assertEquals("1_name.jpg", result.getFile().getName());
        verify(picasso).invalidate(file1);
    }

    @Test
    public void preUpdateWithUpdatedFileAndFileType() throws Exception {
        this.file1 = new File("1_name.jpg");
        this.file2 = new File("12345.pdf");
        assertTrue(this.file1.createNewFile());
        assertTrue(this.file2.createNewFile());

        final String name = "name";
        final List<Receipt> receiptsInTrip = Arrays.asList(mock(Receipt.class), mock(Receipt.class), mock(Receipt.class));
        when(receiptsTable.get(trip)).thenReturn(Single.just(receiptsInTrip));
        final Receipt oldReceipt = mock(Receipt.class);
        when(oldReceipt.getIndex()).thenReturn(1);
        when(oldReceipt.getName()).thenReturn(name);
        when(oldReceipt.getFile()).thenReturn(file1);
        when(oldReceipt.getDate()).thenReturn(DATE);
        when(oldReceipt.getTimeZone()).thenReturn(TIMEZONE);
        when(receipt.getIndex()).thenReturn(1);
        when(receipt.getName()).thenReturn(name);
        when(receipt.getFile()).thenReturn(file2);

        final List<Receipt> onNextResults =
                receiptTableActionAlterations.preUpdate(oldReceipt, receipt)
                        .test()
                        .assertComplete()
                        .assertNoErrors()
                        .values();

        assertNotNull(onNextResults);
        assertTrue(onNextResults.size() == 1);
        final Receipt result = onNextResults.get(0);
        assertNotNull(result.getFile());
        assertEquals("1_name.pdf", result.getFile().getName());
        assertTrue(result.getFile().delete());
    }

    @Test
    public void preUpdateWithNewIndex() {
        final String name = "name";
        final List<Receipt> receiptsInTrip = Arrays.asList(mock(Receipt.class), mock(Receipt.class), mock(Receipt.class));
        when(receiptsTable.get(trip)).thenReturn(Single.just(receiptsInTrip));
        final Receipt oldReceipt = mock(Receipt.class);
        when(oldReceipt.getIndex()).thenReturn(1);
        when(oldReceipt.getName()).thenReturn(name);
        when(oldReceipt.getFile()).thenReturn(new File("1_name.jpg"));
        when(oldReceipt.getDate()).thenReturn(DATE);
        when(oldReceipt.getTimeZone()).thenReturn(TIMEZONE);
        when(receipt.getIndex()).thenReturn(4);
        when(receipt.getName()).thenReturn(name);
        when(receipt.getFile()).thenReturn(new File("1_name.jpg"));

        final List<Receipt> onNextResults =
                receiptTableActionAlterations.preUpdate(oldReceipt, receipt)
                        .test()
                        .assertComplete()
                        .assertNoErrors()
                        .values();

        assertNotNull(onNextResults);
        assertTrue(onNextResults.size() == 1);
        final Receipt result = onNextResults.get(0);
        assertEquals(new File("4_name.jpg"), result.getFile());
    }

    @Test
    public void postUpdateNull() throws Exception {
        receiptTableActionAlterations.postUpdate(receipt, null)
                .test()
                .assertNoValues()
                .assertNotComplete()
                .assertError(Exception.class);

        verifyZeroInteractions(storageManager);
    }

    @Test
    public void postUpdateSuccessWithoutFile() throws Exception {
        final Receipt updatedReceipt = mock(Receipt.class);
        receiptTableActionAlterations.postUpdate(receipt, updatedReceipt)
                .test()
                .assertValue(updatedReceipt)
                .assertComplete()
                .assertNoErrors();

        verifyZeroInteractions(storageManager);
    }

    @Test
    public void postUpdateSuccessWithSameFile() throws Exception {
        final Receipt updatedReceipt = mock(Receipt.class);
        final File file = new File("abc");
        when(receipt.getFile()).thenReturn(file);
        when(updatedReceipt.getFile()).thenReturn(file);

        receiptTableActionAlterations.postUpdate(receipt, updatedReceipt)
                .test()
                .assertValue(updatedReceipt)
                .assertComplete()
                .assertNoErrors();

        verify(storageManager, never()).delete(file);
    }

    @Test
    public void postUpdateSuccessWithNewFile() throws Exception {
        final Receipt updatedReceipt = mock(Receipt.class);
        final File file = new File("abc");
        final File newFile = new File("efg");
        when(receipt.getFile()).thenReturn(file);
        when(updatedReceipt.getFile()).thenReturn(newFile);

        receiptTableActionAlterations.postUpdate(receipt, updatedReceipt)
                .test()
                .assertValue(updatedReceipt)
                .assertComplete()
                .assertNoErrors();

        verify(storageManager).delete(file);
        verify(picasso).invalidate(file);
    }

    @Test
    public void postDeleteNull() throws Exception {
        receiptTableActionAlterations.postDelete(null)
                .test()
                .assertNoValues()
                .assertNotComplete()
                .assertError(Exception.class);

        verifyZeroInteractions(storageManager);
    }

    @Test
    public void postDeleteSuccessWithoutFile() throws Exception {
        receiptTableActionAlterations.postDelete(receipt)
                .test()
                .assertValue(receipt)
                .assertComplete()
                .assertNoErrors();

        verifyZeroInteractions(storageManager);
    }

    @Test
    public void postDeleteSuccessWithFile() throws Exception {
        final File file = new File("abc");
        when(receipt.getFile()).thenReturn(file);

        receiptTableActionAlterations.postDelete(receipt)
                .test()
                .assertValue(receipt)
                .assertComplete()
                .assertNoErrors();

        verify(storageManager).delete(file);
    }


}