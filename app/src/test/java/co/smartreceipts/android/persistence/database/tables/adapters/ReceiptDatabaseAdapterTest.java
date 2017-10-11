package co.smartreceipts.android.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Collections;
import java.util.TimeZone;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Source;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.model.impl.ImmutableCategoryImpl;
import co.smartreceipts.android.model.impl.ImmutablePaymentMethodImpl;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.persistence.database.tables.Table;
import co.smartreceipts.android.persistence.database.tables.keys.PrimaryKey;
import co.smartreceipts.android.sync.model.SyncState;
import io.reactivex.Single;
import wb.android.storage.StorageManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReceiptDatabaseAdapterTest {

    private static final int ID = 5;
    private static final int PRIMARY_KEY_ID = 11;

    private static final String PATH = "Image.jpg";
    private static final String NAME = "Name";
    private static final String PARENT = "Trip";
    private static final int CATEGORY_ID = 15;
    private static final String CATEGORY_NAME = "Category";
    private static final Category CATEGORY = new ImmutableCategoryImpl(CATEGORY_ID, CATEGORY_NAME, "code");
    private static final double PRICE = 12.55d;
    private static final double TAX = 2.50d;
    private static final String CURRENCY_CODE = "USD";
    private static final double EXCHANGE_RATE_FOR_USD = 1.00d;
    private static final ExchangeRate EXCHANGE_RATE = new ExchangeRate(CURRENCY_CODE, Collections.singletonMap(CURRENCY_CODE, EXCHANGE_RATE_FOR_USD));
    private static final long DATE = 1409703721000L;
    private static final String TIMEZONE = TimeZone.getDefault().getID();
    private static final String COMMENT = "Comment";
    private static final boolean REIMBURSABLE = true;
    private static final int PAYMENT_METHOD_ID = 2;
    private static final int DESCENDING_INDEX = 3;
    private static final int ASCENDING_INDEX = 2;
    private static final int CURSOR_COUNT = 4;
    private static final PaymentMethod PAYMENT_METHOD = new ImmutablePaymentMethodImpl(PAYMENT_METHOD_ID, "method");
    private static final boolean FULL_PAGE = true;
    private static final String EXTRA1 = "extra1";
    private static final String EXTRA2 = "extra2";
    private static final String EXTRA3 = "extra3";


    // Class under test
    ReceiptDatabaseAdapter mReceiptDatabaseAdapter;

    @Mock
    Table<Trip, String> mTripsTable;

    @Mock
    Table<PaymentMethod, Integer> mPaymentMethodsTable;

    @Mock
    Table<Category, Integer> mCategoriesTable;

    @Mock
    StorageManager mStorageManager;

    @Mock
    Trip mTrip;

    @Mock
    Cursor mCursor;

    @Mock
    Receipt mReceipt;

    @Mock
    Price mPrice, mTax;

    @Mock
    PrimaryKey<Receipt, Integer> mPrimaryKey;

    @Mock
    SyncStateAdapter mSyncStateAdapter;

    @Mock
    SyncState mSyncState, mGetSyncState;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        final int idIndex = 1;
        final int pathIndex = 2;
        final int nameIndex = 3;
        final int parentIndex = 4;
        final int categoryIdIndex = 5;
        final int priceIndex = 6;
        final int taxIndex = 7;
        final int exchangeRateIndex = 8;
        final int dateIndex = 9;
        final int timezoneIndex = 10;
        final int commentIndex = 11;
        final int expenseableIndex = 12;
        final int currencyCodeIndex = 13;
        final int paymentMethodKeyIndex = 14;
        final int fullPageImageIndex = 15;
        final int extraEdittext1Index = 17;
        final int extraEdittext2Index = 18;
        final int extraEdittext3Index = 19;

        when(mCursor.getColumnIndex("id")).thenReturn(idIndex);
        when(mCursor.getColumnIndex("path")).thenReturn(pathIndex);
        when(mCursor.getColumnIndex("name")).thenReturn(nameIndex);
        when(mCursor.getColumnIndex("parent")).thenReturn(parentIndex);
        when(mCursor.getColumnIndex("categoryKey")).thenReturn(categoryIdIndex);
        when(mCursor.getColumnIndex("price")).thenReturn(priceIndex);
        when(mCursor.getColumnIndex("tax")).thenReturn(taxIndex);
        when(mCursor.getColumnIndex("exchange_rate")).thenReturn(exchangeRateIndex);
        when(mCursor.getColumnIndex("rcpt_date")).thenReturn(dateIndex);
        when(mCursor.getColumnIndex("timezone")).thenReturn(timezoneIndex);
        when(mCursor.getColumnIndex("comment")).thenReturn(commentIndex);
        when(mCursor.getColumnIndex("expenseable")).thenReturn(expenseableIndex);
        when(mCursor.getColumnIndex("isocode")).thenReturn(currencyCodeIndex);
        when(mCursor.getColumnIndex("paymentMethodKey")).thenReturn(paymentMethodKeyIndex);
        when(mCursor.getColumnIndex("fullpageimage")).thenReturn(fullPageImageIndex);
        when(mCursor.getColumnIndex("extra_edittext_1")).thenReturn(extraEdittext1Index);
        when(mCursor.getColumnIndex("extra_edittext_2")).thenReturn(extraEdittext2Index);
        when(mCursor.getColumnIndex("extra_edittext_3")).thenReturn(extraEdittext3Index);

        when(mCursor.getInt(idIndex)).thenReturn(ID);
        when(mCursor.getString(pathIndex)).thenReturn(PATH);
        when(mCursor.getString(nameIndex)).thenReturn(NAME);
        when(mCursor.getString(parentIndex)).thenReturn(PARENT);
        when(mCursor.getInt(categoryIdIndex)).thenReturn(CATEGORY_ID);
        when(mCursor.getDouble(priceIndex)).thenReturn(PRICE);
        when(mCursor.getDouble(taxIndex)).thenReturn(TAX);
        when(mCursor.getDouble(exchangeRateIndex)).thenReturn(EXCHANGE_RATE_FOR_USD);
        when(mCursor.getLong(dateIndex)).thenReturn(DATE);
        when(mCursor.getString(timezoneIndex)).thenReturn(TIMEZONE);
        when(mCursor.getString(commentIndex)).thenReturn(COMMENT);
        when(mCursor.getInt(expenseableIndex)).thenReturn(REIMBURSABLE ? 1 : 0);
        when(mCursor.getString(currencyCodeIndex)).thenReturn(CURRENCY_CODE);
        when(mCursor.getInt(paymentMethodKeyIndex)).thenReturn(PAYMENT_METHOD_ID);
        when(mCursor.getInt(fullPageImageIndex)).thenReturn(FULL_PAGE ? 1 : 0);
        when(mCursor.getString(extraEdittext1Index)).thenReturn(EXTRA1);
        when(mCursor.getString(extraEdittext2Index)).thenReturn(EXTRA2);
        when(mCursor.getString(extraEdittext3Index)).thenReturn(EXTRA3);
        when(mCursor.getCount()).thenReturn(CURSOR_COUNT);
        when(mCursor.getPosition()).thenReturn(ASCENDING_INDEX - 1);

        when(mReceipt.getId()).thenReturn(ID);
        when(mReceipt.getFile()).thenReturn(new File(PATH));
        when(mReceipt.getName()).thenReturn(NAME);
        when(mReceipt.getTrip()).thenReturn(mTrip);
        when(mReceipt.getCategory()).thenReturn(CATEGORY);
        when(mReceipt.getPrice()).thenReturn(mPrice);
        when(mReceipt.getTax()).thenReturn(mTax);
        when(mReceipt.getDate()).thenReturn(new Date(DATE));
        when(mReceipt.getTimeZone()).thenReturn(TimeZone.getTimeZone(TIMEZONE));
        when(mReceipt.getComment()).thenReturn(COMMENT);
        when(mReceipt.isReimbursable()).thenReturn(REIMBURSABLE);
        when(mReceipt.getPaymentMethod()).thenReturn(PAYMENT_METHOD);
        when(mReceipt.isFullPage()).thenReturn(FULL_PAGE);
        when(mReceipt.getExtraEditText1()).thenReturn(EXTRA1);
        when(mReceipt.getExtraEditText2()).thenReturn(EXTRA2);
        when(mReceipt.getExtraEditText3()).thenReturn(EXTRA3);
        when(mReceipt.getIndex()).thenReturn(DESCENDING_INDEX);
        when(mReceipt.getSource()).thenReturn(Source.Undefined);
        when(mReceipt.getSyncState()).thenReturn(mSyncState);

        when(mTrip.getName()).thenReturn(PARENT);
        when(mTrip.getDirectory()).thenReturn(new File(PARENT));
        when(mTrip.getDefaultCurrencyCode()).thenReturn(CURRENCY_CODE);
        when(mTrip.getTripCurrency()).thenReturn(PriceCurrency.getInstance(CURRENCY_CODE));

        when(mPrice.getPrice()).thenReturn(new BigDecimal(PRICE));
        when(mPrice.getCurrencyCode()).thenReturn(CURRENCY_CODE);
        when(mPrice.getCurrency()).thenReturn(PriceCurrency.getInstance(CURRENCY_CODE));
        when(mPrice.getExchangeRate()).thenReturn(EXCHANGE_RATE);
        when(mTax.getPrice()).thenReturn(new BigDecimal(TAX));
        when(mTax.getCurrencyCode()).thenReturn(CURRENCY_CODE);
        when(mTax.getCurrency()).thenReturn(PriceCurrency.getInstance(CURRENCY_CODE));
        when(mTax.getExchangeRate()).thenReturn(EXCHANGE_RATE);

        when(mTripsTable.findByPrimaryKey(PARENT)).thenReturn(Single.just(mTrip));
        when(mPaymentMethodsTable.findByPrimaryKey(PAYMENT_METHOD_ID)).thenReturn(Single.just(PAYMENT_METHOD));
        when(mCategoriesTable.findByPrimaryKey(CATEGORY_ID)).thenReturn(Single.just(CATEGORY));

        when(mPrimaryKey.getPrimaryKeyValue(mReceipt)).thenReturn(PRIMARY_KEY_ID);
        when(mStorageManager.getFile(new File(PARENT), PATH)).thenReturn(new File(PATH));

        when(mSyncStateAdapter.read(mCursor)).thenReturn(mSyncState);
        when(mSyncStateAdapter.get(any(SyncState.class), any(DatabaseOperationMetadata.class))).thenReturn(mGetSyncState);

        mReceiptDatabaseAdapter = new ReceiptDatabaseAdapter(mTripsTable, mPaymentMethodsTable, mCategoriesTable, mStorageManager, mSyncStateAdapter);
    }

    @Test
    public void read() throws Exception {
        // Note: Full page is backwards in the database
        final Receipt receipt = new ReceiptBuilderFactory(ID)
                .setTrip(mTrip)
                .setName(NAME)
                .setPrice(PRICE)
                .setTax(TAX)
                .setExchangeRate(EXCHANGE_RATE)
                .setCategory(CATEGORY)
                .setFile(new File(PATH))
                .setDate(DATE)
                .setTimeZone(TIMEZONE)
                .setComment(COMMENT)
                .setIsReimbursable(REIMBURSABLE)
                .setCurrency(CURRENCY_CODE)
                .setIsFullPage(!FULL_PAGE)
                .setIndex(DESCENDING_INDEX)
                .setPaymentMethod(PAYMENT_METHOD)
                .setExtraEditText1(EXTRA1)
                .setExtraEditText2(EXTRA2)
                .setExtraEditText3(EXTRA3)
                .setSyncState(mSyncState)
                .build();
        assertEquals(receipt, mReceiptDatabaseAdapter.read(mCursor));
    }

    @Test
    public void readForSelectionDescending() throws Exception {
        // Note: Full page is backwards in the database
        final Receipt receipt = new ReceiptBuilderFactory(ID)
                .setTrip(mTrip)
                .setName(NAME)
                .setPrice(PRICE)
                .setTax(TAX)
                .setExchangeRate(EXCHANGE_RATE)
                .setCategory(CATEGORY)
                .setFile(new File(PATH))
                .setDate(DATE)
                .setTimeZone(TIMEZONE)
                .setComment(COMMENT)
                .setIsReimbursable(REIMBURSABLE)
                .setCurrency(CURRENCY_CODE)
                .setIsFullPage(!FULL_PAGE)
                .setIndex(DESCENDING_INDEX)
                .setPaymentMethod(PAYMENT_METHOD)
                .setExtraEditText1(EXTRA1)
                .setExtraEditText2(EXTRA2)
                .setExtraEditText3(EXTRA3)
                .setSyncState(mSyncState)
                .build();
        assertEquals(receipt, mReceiptDatabaseAdapter.readForSelection(mCursor, mTrip, true));
    }

    @Test
    public void readForSelectionAscending() throws Exception {
        // Note: Full page is backwards in the database
        final Receipt receipt = new ReceiptBuilderFactory(ID)
                .setTrip(mTrip)
                .setName(NAME)
                .setPrice(PRICE)
                .setTax(TAX)
                .setExchangeRate(EXCHANGE_RATE)
                .setCategory(CATEGORY)
                .setFile(new File(PATH))
                .setDate(DATE)
                .setTimeZone(TIMEZONE)
                .setComment(COMMENT)
                .setIsReimbursable(REIMBURSABLE)
                .setCurrency(CURRENCY_CODE)
                .setIsFullPage(!FULL_PAGE)
                .setIndex(ASCENDING_INDEX)
                .setPaymentMethod(PAYMENT_METHOD)
                .setExtraEditText1(EXTRA1)
                .setExtraEditText2(EXTRA2)
                .setExtraEditText3(EXTRA3)
                .setSyncState(mSyncState)
                .build();
        assertEquals(receipt, mReceiptDatabaseAdapter.readForSelection(mCursor, mTrip, false));
    }

    @Test
    public void readForUnmappedCategory() throws Exception {
        when(mCategoriesTable.findByPrimaryKey(CATEGORY_ID)).thenReturn(Single.error(new Exception()));

        // Note: Full page is backwards in the database
        final Receipt receipt = new ReceiptBuilderFactory(ID)
                .setTrip(mTrip)
                .setName(NAME)
                .setPrice(PRICE)
                .setTax(TAX)
                .setExchangeRate(EXCHANGE_RATE)
                .setCategory(null)
                .setFile(new File(PATH))
                .setDate(DATE)
                .setTimeZone(TIMEZONE)
                .setComment(COMMENT)
                .setIsReimbursable(REIMBURSABLE)
                .setCurrency(CURRENCY_CODE)
                .setIsFullPage(!FULL_PAGE)
                .setIndex(DESCENDING_INDEX)
                .setPaymentMethod(PAYMENT_METHOD)
                .setExtraEditText1(EXTRA1)
                .setExtraEditText2(EXTRA2)
                .setExtraEditText3(EXTRA3)
                .setSyncState(mSyncState)
                .build();
        assertEquals(receipt, mReceiptDatabaseAdapter.read(mCursor));
    }

    @Test
    public void readForUnmappedPaymentMethod() throws Exception {
        when(mPaymentMethodsTable.findByPrimaryKey(PAYMENT_METHOD_ID)).thenReturn(Single.error(new Exception()));

        // Note: Full page is backwards in the database
        final Receipt receipt = new ReceiptBuilderFactory(ID)
                .setTrip(mTrip)
                .setName(NAME)
                .setPrice(PRICE)
                .setTax(TAX)
                .setExchangeRate(EXCHANGE_RATE)
                .setCategory(CATEGORY)
                .setFile(new File(PATH))
                .setDate(DATE)
                .setTimeZone(TIMEZONE)
                .setComment(COMMENT)
                .setIsReimbursable(REIMBURSABLE)
                .setCurrency(CURRENCY_CODE)
                .setIsFullPage(!FULL_PAGE)
                .setIndex(DESCENDING_INDEX)
                .setPaymentMethod(null)
                .setExtraEditText1(EXTRA1)
                .setExtraEditText2(EXTRA2)
                .setExtraEditText3(EXTRA3)
                .setSyncState(mSyncState)
                .build();
        assertEquals(receipt, mReceiptDatabaseAdapter.read(mCursor));
    }

    @Test
    public void readForNullCommentDefaultsToEmptyString() throws Exception {
        final int commentIndex = 1100;
        when(mCursor.getColumnIndex("comment")).thenReturn(commentIndex);
        when(mCursor.getString(commentIndex)).thenReturn(null);

        // Note: Full page is backwards in the database
        final Receipt receipt = new ReceiptBuilderFactory(ID)
                .setTrip(mTrip)
                .setName(NAME)
                .setPrice(PRICE)
                .setTax(TAX)
                .setExchangeRate(EXCHANGE_RATE)
                .setCategory(CATEGORY)
                .setFile(new File(PATH))
                .setDate(DATE)
                .setTimeZone(TIMEZONE)
                .setComment("")
                .setIsReimbursable(REIMBURSABLE)
                .setCurrency(CURRENCY_CODE)
                .setIsFullPage(!FULL_PAGE)
                .setIndex(DESCENDING_INDEX)
                .setPaymentMethod(PAYMENT_METHOD)
                .setExtraEditText1(EXTRA1)
                .setExtraEditText2(EXTRA2)
                .setExtraEditText3(EXTRA3)
                .setSyncState(mSyncState)
                .build();
        assertEquals(receipt, mReceiptDatabaseAdapter.read(mCursor));
    }

    @Test
    public void writeUnsycned() throws Exception {
        final String sync = "sync";
        final ContentValues syncValues = new ContentValues();
        syncValues.put(sync, sync);
        when(mSyncStateAdapter.writeUnsynced(mSyncState)).thenReturn(syncValues);

        final ContentValues contentValues = mReceiptDatabaseAdapter.write(mReceipt, new DatabaseOperationMetadata());

        // Note: Full page is backwards in the database
        assertEquals(PATH, contentValues.getAsString("path"));
        assertEquals(NAME, contentValues.getAsString("name"));
        assertEquals(PARENT, contentValues.getAsString("parent"));
        assertEquals(CATEGORY_ID, (int) contentValues.getAsInteger("categoryKey"));
        assertEquals(PRICE, contentValues.getAsDouble("price"), 0.0001d);
        assertEquals(TAX, contentValues.getAsDouble("tax"), 0.0001d);
        assertEquals(EXCHANGE_RATE_FOR_USD, contentValues.getAsDouble("exchange_rate"), 0.0001d);
        assertEquals(DATE, (long) contentValues.getAsLong("rcpt_date"));
        assertEquals(TIMEZONE, contentValues.getAsString("timezone"));
        assertEquals(COMMENT, contentValues.getAsString("comment"));
        assertEquals(REIMBURSABLE, contentValues.getAsBoolean("expenseable"));
        assertEquals(CURRENCY_CODE, contentValues.getAsString("isocode"));
        assertEquals(PAYMENT_METHOD_ID, (int) contentValues.getAsInteger("paymentMethodKey"));
        assertEquals(!FULL_PAGE, contentValues.getAsBoolean("fullpageimage"));
        assertEquals(EXTRA1, contentValues.getAsString("extra_edittext_1"));
        assertEquals(EXTRA2, contentValues.getAsString("extra_edittext_2"));
        assertEquals(EXTRA3, contentValues.getAsString("extra_edittext_3"));
        assertEquals(sync, contentValues.getAsString(sync));
        assertFalse(contentValues.containsKey("id"));
    }

    @Test
    public void write() throws Exception {
        final String sync = "sync";
        final ContentValues syncValues = new ContentValues();
        syncValues.put(sync, sync);
        when(mSyncStateAdapter.write(mSyncState)).thenReturn(syncValues);

        final ContentValues contentValues = mReceiptDatabaseAdapter.write(mReceipt, new DatabaseOperationMetadata(OperationFamilyType.Sync));

        // Note: Full page is backwards in the database
        assertEquals(PATH, contentValues.getAsString("path"));
        assertEquals(NAME, contentValues.getAsString("name"));
        assertEquals(PARENT, contentValues.getAsString("parent"));
        assertEquals(CATEGORY_ID, (int) contentValues.getAsInteger("categoryKey"));
        assertEquals(PRICE, contentValues.getAsDouble("price"), 0.0001d);
        assertEquals(TAX, contentValues.getAsDouble("tax"), 0.0001d);
        assertEquals(EXCHANGE_RATE_FOR_USD, contentValues.getAsDouble("exchange_rate"), 0.0001d);
        assertEquals(DATE, (long) contentValues.getAsLong("rcpt_date"));
        assertEquals(TIMEZONE, contentValues.getAsString("timezone"));
        assertEquals(COMMENT, contentValues.getAsString("comment"));
        assertEquals(REIMBURSABLE, contentValues.getAsBoolean("expenseable"));
        assertEquals(CURRENCY_CODE, contentValues.getAsString("isocode"));
        assertEquals(PAYMENT_METHOD_ID, (int) contentValues.getAsInteger("paymentMethodKey"));
        assertEquals(!FULL_PAGE, contentValues.getAsBoolean("fullpageimage"));
        assertEquals(EXTRA1, contentValues.getAsString("extra_edittext_1"));
        assertEquals(EXTRA2, contentValues.getAsString("extra_edittext_2"));
        assertEquals(EXTRA3, contentValues.getAsString("extra_edittext_3"));
        assertEquals(sync, contentValues.getAsString(sync));
        assertFalse(contentValues.containsKey("id"));
    }

    @Test
    public void build() throws Exception {
        final Receipt receipt = new ReceiptBuilderFactory(PRIMARY_KEY_ID)
                .setTrip(mTrip)
                .setName(NAME)
                .setPrice(PRICE)
                .setTax(TAX)
                .setExchangeRate(EXCHANGE_RATE)
                .setCategory(CATEGORY)
                .setFile(new File(PATH))
                .setDate(DATE)
                .setTimeZone(TIMEZONE)
                .setComment(COMMENT)
                .setIsReimbursable(REIMBURSABLE)
                .setCurrency(CURRENCY_CODE)
                .setIsFullPage(FULL_PAGE)
                .setIndex(DESCENDING_INDEX)
                .setPaymentMethod(PAYMENT_METHOD)
                .setExtraEditText1(EXTRA1)
                .setExtraEditText2(EXTRA2)
                .setExtraEditText3(EXTRA3)
                .setSyncState(mGetSyncState)
                .build();
        assertEquals(receipt, mReceiptDatabaseAdapter.build(mReceipt, mPrimaryKey, mock(DatabaseOperationMetadata.class)));
        assertEquals(receipt.getSyncState(), mReceiptDatabaseAdapter.build(mReceipt, mPrimaryKey, mock(DatabaseOperationMetadata.class)).getSyncState());
    }

}