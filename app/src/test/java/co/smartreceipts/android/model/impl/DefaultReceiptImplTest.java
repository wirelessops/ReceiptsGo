package co.smartreceipts.android.model.impl;

import android.os.Parcel;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.sql.Date;
import java.util.Locale;
import java.util.TimeZone;

import co.smartreceipts.android.DefaultObjects;
import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Source;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.utils.TestLocaleToggler;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class DefaultReceiptImplTest {

    private static final int ID = 5;
    private static final String NAME = "Name";
    private static final Date DATE = new Date(1409703721000L);
    private static final TimeZone TIMEZONE = TimeZone.getDefault();
    private static final String COMMENT = "Comment";
    private static final boolean REIMBURSABLE = true;
    private static final int INDEX = 3;
    private static final boolean FULL_PAGE = true;
    private static final boolean IS_SELECTED = true;
    private static final String EXTRA1 = "extra1";
    private static final String EXTRA2 = "extra2";
    private static final String EXTRA3 = "extra3";
    private static final int CUSTOM_ORDER = 2;

    // Class under test
    DefaultReceiptImpl mReceipt;

    Trip mTrip;
    
    File mFile;
    
    PaymentMethod mPaymentMethod;
    
    Category mCategory;
    
    Price mPrice;
    
    Price mTax;

    SyncState mSyncState;

    @Before
    public void setUp() throws Exception {
        TestLocaleToggler.setDefaultLocale(Locale.US);
        mTrip = DefaultObjects.newDefaultTrip();
        mFile = new File(new File("").getAbsolutePath());
        mPaymentMethod = DefaultObjects.newDefaultPaymentMethod();
        mCategory = DefaultObjects.newDefaultCategory();
        mPrice = DefaultObjects.newDefaultPrice();
        mTax = DefaultObjects.newDefaultTax();
        mSyncState = DefaultObjects.newDefaultSyncState();
        mReceipt = new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER);
    }

    @After
    public void tearDown() throws Exception {
        TestLocaleToggler.resetDefaultLocale();
    }

    @Test
    public void getId() {
        assertEquals(ID, mReceipt.getId());
    }

    @Test
    public void getTrip() {
        assertEquals(mTrip, mReceipt.getTrip());
    }

    @Test
    public void getPaymentMethod() {
        assertEquals(mPaymentMethod, mReceipt.getPaymentMethod());
    }

    @Test
    public void getName() {
        assertEquals(NAME, mReceipt.getName());
    }

    @Test
    public void getFile() {
        assertEquals(mFile, mReceipt.getFile());
    }

    @Test
    public void getCategory() {
        assertEquals(mCategory, mReceipt.getCategory());
    }

    @Test
    public void getComment() {
        assertEquals(COMMENT, mReceipt.getComment());
    }

    @Test
    public void getPrice() {
        assertEquals(mPrice, mReceipt.getPrice());
    }

    @Test
    public void getTax() {
        assertEquals(mTax, mReceipt.getTax());
    }

    @Test
    public void getDate() {
        assertEquals(DATE, mReceipt.getDate());
    }

    @Test
    public void getTimeZone() {
        assertEquals(TIMEZONE, mReceipt.getTimeZone());
    }

    @Test
    public void isReimbursable() {
        assertEquals(REIMBURSABLE, mReceipt.isReimbursable());
    }

    @Test
    public void isFullPage() {
        assertEquals(FULL_PAGE, mReceipt.isFullPage());
    }

    @Test
    public void isSelected() {
        assertEquals(IS_SELECTED, mReceipt.isSelected());
    }

    @Test
    public void getIndex() {
        assertEquals(INDEX, mReceipt.getIndex());
    }

    @Test
    public void getExtraEditText1() {
        final Receipt nullExtra1Receipt = new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, null, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER);
        final Receipt noDataExtra1Receipt = new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, DatabaseHelper.NO_DATA, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER);
        
        assertTrue(mReceipt.hasExtraEditText1());
        assertEquals(EXTRA1, mReceipt.getExtraEditText1());
        assertFalse(nullExtra1Receipt.hasExtraEditText1());
        assertNull(nullExtra1Receipt.getExtraEditText1());
        assertFalse(noDataExtra1Receipt.hasExtraEditText1());
        assertNull(noDataExtra1Receipt.getExtraEditText1());
    }

    @Test
    public void getExtraEditText2() {
        final Receipt nullExtra2Receipt = new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, null, EXTRA3, mSyncState, CUSTOM_ORDER);
        final Receipt noDataExtra2Receipt = new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, DatabaseHelper.NO_DATA, EXTRA3, mSyncState, CUSTOM_ORDER);

        assertTrue(mReceipt.hasExtraEditText2());
        assertEquals(EXTRA2, mReceipt.getExtraEditText2());
        assertFalse(nullExtra2Receipt.hasExtraEditText2());
        assertNull(nullExtra2Receipt.getExtraEditText2());
        assertFalse(noDataExtra2Receipt.hasExtraEditText2());
        assertNull(noDataExtra2Receipt.getExtraEditText2());
    }

    @Test
    public void getExtraEditText3() {
        final Receipt nullExtra3Receipt = new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, null, mSyncState, CUSTOM_ORDER);
        final Receipt noDataExtra3Receipt = new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, DatabaseHelper.NO_DATA, mSyncState, CUSTOM_ORDER);

        assertTrue(mReceipt.hasExtraEditText3());
        assertEquals(EXTRA3, mReceipt.getExtraEditText3());
        assertFalse(nullExtra3Receipt.hasExtraEditText3());
        assertNull(nullExtra3Receipt.getExtraEditText3());
        assertFalse(noDataExtra3Receipt.hasExtraEditText3());
        assertNull(noDataExtra3Receipt.getExtraEditText3());
    }

    @Test
    public void getSyncState() {
        assertEquals(mSyncState, mReceipt.getSyncState());
    }

    @Test
    public void compareTo() {
        assertTrue(mReceipt.compareTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER)) == 0);
        assertTrue(mReceipt.compareTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, new Date(DATE.getTime()*2), TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER)) > 0);
        assertTrue(mReceipt.compareTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, new Date(0), TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER)) < 0);
    }

    @Test
    public void testHashCode() {
        Assert.assertEquals(mReceipt.hashCode(), mReceipt.hashCode());
        Assert.assertEquals(mReceipt.hashCode(), new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER).hashCode());
        assertThat(mReceipt.hashCode(), not(equalTo(new Object())));
        assertThat(mReceipt.hashCode(), not(equalTo(mock(Receipt.class))));
        assertThat(mReceipt.hashCode(), not(equalTo(new DefaultReceiptImpl(-1, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER).hashCode())));
        assertThat(mReceipt.hashCode(), not(equalTo(new DefaultReceiptImpl(ID, INDEX + 1, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER).hashCode())));
        assertThat(mReceipt.hashCode(), not(equalTo(new DefaultReceiptImpl(ID, INDEX, mock(Trip.class), mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER).hashCode())));
        assertThat(mReceipt.hashCode(), not(equalTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mock(File.class), mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER).hashCode())));
        assertThat(mReceipt.hashCode(), not(equalTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mock(PaymentMethod.class), NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER).hashCode())));
        assertThat(mReceipt.hashCode(), not(equalTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, "bad", mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER).hashCode())));
        assertThat(mReceipt.hashCode(), not(equalTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mock(Category.class), COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER).hashCode())));
        assertThat(mReceipt.hashCode(), not(equalTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, "bad", mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER).hashCode())));
        assertThat(mReceipt.hashCode(), not(equalTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mock(Price.class), mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER).hashCode())));
        assertThat(mReceipt.hashCode(), not(equalTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mock(Price.class), DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER).hashCode())));
        assertThat(mReceipt.hashCode(), not(equalTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, new Date(System.currentTimeMillis()), TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER).hashCode())));
        assertThat(mReceipt.hashCode(), not(equalTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, !REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER).hashCode())));
        assertThat(mReceipt.hashCode(), not(equalTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, !FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER).hashCode())));
        assertThat(mReceipt.hashCode(), not(equalTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, "bad", EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER).hashCode())));
        assertThat(mReceipt.hashCode(), not(equalTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, "bad", EXTRA3, mSyncState, CUSTOM_ORDER).hashCode())));
        assertThat(mReceipt.hashCode(), not(equalTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, "bad", mSyncState, CUSTOM_ORDER).hashCode())));

        // Special equals cases (we don't check on the sources don't count, and selected doesn't count):
        Assert.assertEquals(mReceipt.hashCode(), new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Parcel, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER).hashCode());
        Assert.assertEquals(mReceipt.hashCode(), new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, !IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER).hashCode());
    }
    
    @Test
    public void testEquals() {
        Assert.assertEquals(mReceipt, mReceipt);
        Assert.assertEquals(mReceipt, new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER));
        assertThat(mReceipt, not(equalTo(new Object())));
        assertThat(mReceipt, not(equalTo(mock(Receipt.class))));
        assertThat(mReceipt, not(equalTo(new DefaultReceiptImpl(-1, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER))));
        assertThat(mReceipt, not(equalTo(new DefaultReceiptImpl(ID, INDEX + 1, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER))));
        assertThat(mReceipt, not(equalTo(new DefaultReceiptImpl(ID, INDEX, mock(Trip.class), mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER))));
        assertThat(mReceipt, not(equalTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mock(File.class), mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER))));
        assertThat(mReceipt, not(equalTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mock(PaymentMethod.class), NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER))));
        assertThat(mReceipt, not(equalTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, "bad", mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER))));
        assertThat(mReceipt, not(equalTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mock(Category.class), COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER))));
        assertThat(mReceipt, not(equalTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, "bad", mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER))));
        assertThat(mReceipt, not(equalTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mock(Price.class), mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER))));
        assertThat(mReceipt, not(equalTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mock(Price.class), DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER))));
        assertThat(mReceipt, not(equalTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, new Date(System.currentTimeMillis()), TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER))));
        assertThat(mReceipt, not(equalTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, !REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER))));
        assertThat(mReceipt, not(equalTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, !FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER))));
        assertThat(mReceipt, not(equalTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, "bad", EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER))));
        assertThat(mReceipt, not(equalTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, "bad", EXTRA3, mSyncState, CUSTOM_ORDER))));
        assertThat(mReceipt, not(equalTo(new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, "bad", mSyncState, CUSTOM_ORDER))));

        // Special equals cases (we don't check on the sources don't count, and selected doesn't count):
        Assert.assertEquals(mReceipt, new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, IS_SELECTED, Source.Parcel, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER));
        Assert.assertEquals(mReceipt, new DefaultReceiptImpl(ID, INDEX, mTrip, mFile, mPaymentMethod, NAME, mCategory, COMMENT, mPrice, mTax, DATE, TIMEZONE, REIMBURSABLE, FULL_PAGE, !IS_SELECTED, Source.Undefined, EXTRA1, EXTRA2, EXTRA3, mSyncState, CUSTOM_ORDER));
    }

    @Test
    public void parcelEquality() {
        final Parcel parcel = Parcel.obtain();
        mReceipt.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        final DefaultReceiptImpl receipt = DefaultReceiptImpl.CREATOR.createFromParcel(parcel);
        assertNotNull(receipt);
        assertEquals(receipt, mReceipt);
    }

}