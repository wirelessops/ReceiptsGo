package co.smartreceipts.android.persistence.database.tables.adapters;

import android.content.ContentValues;
import android.database.Cursor;

import org.junit.After;
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
import java.util.UUID;

import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Source;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;
import co.smartreceipts.android.persistence.database.tables.Table;
import co.smartreceipts.android.sync.model.SyncState;
import io.reactivex.Single;
import wb.android.storage.StorageManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ReceiptDatabaseAdapterTest {

    private static final int ID = 5;

    private static final UUID RECEIPT_UUID = UUID.randomUUID();
    private static final int PARENT_TRIP_ID = 128;
    private static final File PARENT_DIR = new File(System.getProperty("java.io.tmpdir"), "Trip");
    private static final File RECEIPT_FILE = new File(PARENT_DIR, "Image.jpg");
    private static final String NAME = "Name";
    private static final int CATEGORY_ID = 15;
    private static final String CATEGORY_NAME = "Category";
    private static final UUID CATEGORY_UUID = UUID.randomUUID();
    private static final Category CATEGORY = new Category(CATEGORY_ID, CATEGORY_UUID, CATEGORY_NAME, "code");
    private static final double PRICE = 12.55d;
    private static final double TAX = 2.50d;
    private static final String CURRENCY_CODE = "USD";
    private static final double EXCHANGE_RATE_FOR_USD = 1.00d;
    private static final ExchangeRate EXCHANGE_RATE = new ExchangeRate(CURRENCY_CODE, Collections.singletonMap(CURRENCY_CODE, EXCHANGE_RATE_FOR_USD));
    private static final long DATE = 1409703721000L;
    private static final long CUSTOM_ORDER_ID = 16316000L;
    private static final String TIMEZONE = TimeZone.getDefault().getID();
    private static final String COMMENT = "Comment";
    private static final boolean REIMBURSABLE = true;
    private static final int PAYMENT_METHOD_ID = 2;
    private static final UUID PAYMENT_METHOD_UUID = UUID.randomUUID();
    private static final int DESCENDING_INDEX = 3;
    private static final int ASCENDING_INDEX = 2;
    private static final int CURSOR_COUNT = 4;
    private static final PaymentMethod PAYMENT_METHOD = new PaymentMethod(PAYMENT_METHOD_ID, PAYMENT_METHOD_UUID, "method");
    private static final boolean FULL_PAGE = true;
    private static final String EXTRA1 = "extra1";
    private static final String EXTRA2 = "extra2";
    private static final String EXTRA3 = "extra3";


    // Class under test
    ReceiptDatabaseAdapter mReceiptDatabaseAdapter;

    @Mock
    Table<Trip> mTripsTable;

    @Mock
    Table<PaymentMethod> mPaymentMethodsTable;

    @Mock
    Table<Category> mCategoriesTable;

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
    SyncStateAdapter mSyncStateAdapter;

    @Mock
    SyncState mSyncState, mGetSyncState;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        assertTrue(PARENT_DIR.exists() || PARENT_DIR.mkdirs());
        assertTrue(RECEIPT_FILE.exists() || RECEIPT_FILE.createNewFile());

        final int idIndex = 1;
        final int pathIndex = 2;
        final int nameIndex = 3;
        final int parentKeyIndex = 4;
        final int categoryIdIndex = 5;
        final int priceIndex = 6;
        final int taxIndex = 7;
        final int exchangeRateIndex = 8;
        final int dateIndex = 9;
        final int timezoneIndex = 10;
        final int commentIndex = 11;
        final int expensableIndex = 12;
        final int currencyCodeIndex = 13;
        final int paymentMethodKeyIndex = 14;
        final int fullPageImageIndex = 15;
        final int extraEditText1Index = 17;
        final int extraEditText2Index = 18;
        final int extraEditText3Index = 19;
        final int customOrderIdIndex = 20;
        final int uuidIndex = 21;

        when(mCursor.getColumnIndex(ReceiptsTable.COLUMN_ID)).thenReturn(idIndex);
        when(mCursor.getColumnIndex(ReceiptsTable.COLUMN_PATH)).thenReturn(pathIndex);
        when(mCursor.getColumnIndex(ReceiptsTable.COLUMN_NAME)).thenReturn(nameIndex);
        when(mCursor.getColumnIndex(ReceiptsTable.COLUMN_PARENT_TRIP_ID)).thenReturn(parentKeyIndex);
        when(mCursor.getColumnIndex(ReceiptsTable.COLUMN_CATEGORY_ID)).thenReturn(categoryIdIndex);
        when(mCursor.getColumnIndex(ReceiptsTable.COLUMN_PRICE)).thenReturn(priceIndex);
        when(mCursor.getColumnIndex(ReceiptsTable.COLUMN_TAX)).thenReturn(taxIndex);
        when(mCursor.getColumnIndex(ReceiptsTable.COLUMN_EXCHANGE_RATE)).thenReturn(exchangeRateIndex);
        when(mCursor.getColumnIndex(ReceiptsTable.COLUMN_DATE)).thenReturn(dateIndex);
        when(mCursor.getColumnIndex(ReceiptsTable.COLUMN_TIMEZONE)).thenReturn(timezoneIndex);
        when(mCursor.getColumnIndex(ReceiptsTable.COLUMN_COMMENT)).thenReturn(commentIndex);
        when(mCursor.getColumnIndex(ReceiptsTable.COLUMN_REIMBURSABLE)).thenReturn(expensableIndex);
        when(mCursor.getColumnIndex(ReceiptsTable.COLUMN_ISO4217)).thenReturn(currencyCodeIndex);
        when(mCursor.getColumnIndex(ReceiptsTable.COLUMN_PAYMENT_METHOD_ID)).thenReturn(paymentMethodKeyIndex);
        when(mCursor.getColumnIndex(ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE)).thenReturn(fullPageImageIndex);
        when(mCursor.getColumnIndex(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1)).thenReturn(extraEditText1Index);
        when(mCursor.getColumnIndex(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2)).thenReturn(extraEditText2Index);
        when(mCursor.getColumnIndex(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3)).thenReturn(extraEditText3Index);
        when(mCursor.getColumnIndex(ReceiptsTable.COLUMN_CUSTOM_ORDER_ID)).thenReturn(customOrderIdIndex);
        when(mCursor.getColumnIndex(ReceiptsTable.COLUMN_UUID)).thenReturn(uuidIndex);

        when(mCursor.getInt(idIndex)).thenReturn(ID);
        when(mCursor.getString(pathIndex)).thenReturn(RECEIPT_FILE.getName());
        when(mCursor.getString(nameIndex)).thenReturn(NAME);
        when(mCursor.getInt(parentKeyIndex)).thenReturn(PARENT_TRIP_ID);
        when(mCursor.getInt(categoryIdIndex)).thenReturn(CATEGORY_ID);
        when(mCursor.getDouble(priceIndex)).thenReturn(PRICE);
        when(mCursor.getDouble(taxIndex)).thenReturn(TAX);
        when(mCursor.getDouble(exchangeRateIndex)).thenReturn(EXCHANGE_RATE_FOR_USD);
        when(mCursor.getLong(dateIndex)).thenReturn(DATE);
        when(mCursor.getString(timezoneIndex)).thenReturn(TIMEZONE);
        when(mCursor.getString(commentIndex)).thenReturn(COMMENT);
        when(mCursor.getInt(expensableIndex)).thenReturn(REIMBURSABLE ? 1 : 0);
        when(mCursor.getString(currencyCodeIndex)).thenReturn(CURRENCY_CODE);
        when(mCursor.getInt(paymentMethodKeyIndex)).thenReturn(PAYMENT_METHOD_ID);
        when(mCursor.getInt(fullPageImageIndex)).thenReturn(FULL_PAGE ? 1 : 0);
        when(mCursor.getString(extraEditText1Index)).thenReturn(EXTRA1);
        when(mCursor.getString(extraEditText2Index)).thenReturn(EXTRA2);
        when(mCursor.getString(extraEditText3Index)).thenReturn(EXTRA3);
        when(mCursor.getLong(customOrderIdIndex)).thenReturn(CUSTOM_ORDER_ID);
        when(mCursor.getCount()).thenReturn(CURSOR_COUNT);
        when(mCursor.getPosition()).thenReturn(ASCENDING_INDEX - 1);
        when(mCursor.getString(uuidIndex)).thenReturn(RECEIPT_UUID.toString());

        when(mReceipt.getId()).thenReturn(ID);
        when(mReceipt.getFile()).thenReturn(RECEIPT_FILE);
        when(mReceipt.getName()).thenReturn(NAME);
        when(mReceipt.getTrip()).thenReturn(mTrip);
        when(mReceipt.getCategory()).thenReturn(CATEGORY);
        when(mReceipt.getPrice()).thenReturn(mPrice);
        when(mReceipt.getTax()).thenReturn(mTax);
        when(mReceipt.getDate()).thenReturn(new Date(DATE));
        when(mReceipt.getCustomOrderId()).thenReturn(CUSTOM_ORDER_ID);
        when(mReceipt.getTimeZone()).thenReturn(TimeZone.getTimeZone(TIMEZONE));
        when(mReceipt.getComment()).thenReturn(COMMENT);
        when(mReceipt.isReimbursable()).thenReturn(REIMBURSABLE);
        when(mReceipt.getPaymentMethod()).thenReturn(PAYMENT_METHOD);
        when(mReceipt.isFullPage()).thenReturn(FULL_PAGE);
        when(mReceipt.getExtraEditText1()).thenReturn(EXTRA1);
        when(mReceipt.getExtraEditText2()).thenReturn(EXTRA2);
        when(mReceipt.getExtraEditText3()).thenReturn(EXTRA3);
        when(mReceipt.getIndex()).thenReturn(DESCENDING_INDEX);
        when(mReceipt.getSyncState()).thenReturn(mSyncState);
        when(mReceipt.getUuid()).thenReturn(RECEIPT_UUID);

        when(mTrip.getId()).thenReturn(PARENT_TRIP_ID);
        when(mTrip.getName()).thenReturn(PARENT_DIR.getName());
        when(mTrip.getDirectory()).thenReturn(PARENT_DIR);
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

        when(mTripsTable.findByPrimaryKey(PARENT_TRIP_ID)).thenReturn(Single.just(mTrip));
        when(mPaymentMethodsTable.findByPrimaryKey(PAYMENT_METHOD_ID)).thenReturn(Single.just(PAYMENT_METHOD));
        when(mCategoriesTable.findByPrimaryKey(CATEGORY_ID)).thenReturn(Single.just(CATEGORY));

        when(mStorageManager.getFile(PARENT_DIR, RECEIPT_FILE.getName())).thenReturn(RECEIPT_FILE);

        when(mSyncStateAdapter.read(mCursor)).thenReturn(mSyncState);
        when(mSyncStateAdapter.get(any(SyncState.class), any(DatabaseOperationMetadata.class))).thenReturn(mGetSyncState);

        mReceiptDatabaseAdapter = new ReceiptDatabaseAdapter(mTripsTable, mPaymentMethodsTable, mCategoriesTable, mStorageManager, mSyncStateAdapter);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @After
    public void tearDown() throws Exception {
        RECEIPT_FILE.delete();
    }

    @Test
    public void read() throws Exception {
        // Note: Full page is backwards in the database
        final Receipt receipt = new ReceiptBuilderFactory(ID)
                .setUuid(RECEIPT_UUID)
                .setTrip(mTrip)
                .setName(NAME)
                .setPrice(PRICE)
                .setTax(TAX)
                .setExchangeRate(EXCHANGE_RATE)
                .setCategory(CATEGORY)
                .setFile(RECEIPT_FILE)
                .setDate(DATE)
                .setCustomOrderId(CUSTOM_ORDER_ID)
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
    public void readFileThatDoesNotExist() throws Exception {
        // Delete the receipt file
        assertTrue(RECEIPT_FILE.delete());

        // Note: Full page is backwards in the database
        final Receipt receipt = new ReceiptBuilderFactory(ID)
                .setUuid(RECEIPT_UUID)
                .setTrip(mTrip)
                .setName(NAME)
                .setPrice(PRICE)
                .setTax(TAX)
                .setExchangeRate(EXCHANGE_RATE)
                .setCategory(CATEGORY)
                .setFile(null)
                .setDate(DATE)
                .setCustomOrderId(CUSTOM_ORDER_ID)
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
                .setUuid(RECEIPT_UUID)
                .setTrip(mTrip)
                .setName(NAME)
                .setPrice(PRICE)
                .setTax(TAX)
                .setExchangeRate(EXCHANGE_RATE)
                .setCategory(CATEGORY)
                .setFile(RECEIPT_FILE)
                .setDate(DATE)
                .setCustomOrderId(CUSTOM_ORDER_ID)
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
                .setUuid(RECEIPT_UUID)
                .setTrip(mTrip)
                .setName(NAME)
                .setPrice(PRICE)
                .setTax(TAX)
                .setExchangeRate(EXCHANGE_RATE)
                .setCategory(CATEGORY)
                .setFile(RECEIPT_FILE)
                .setDate(DATE)
                .setCustomOrderId(CUSTOM_ORDER_ID)
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
                .setUuid(RECEIPT_UUID)
                .setTrip(mTrip)
                .setName(NAME)
                .setPrice(PRICE)
                .setTax(TAX)
                .setExchangeRate(EXCHANGE_RATE)
                .setFile(RECEIPT_FILE)
                .setDate(DATE)
                .setCustomOrderId(CUSTOM_ORDER_ID)
                .setCustomOrderId(CUSTOM_ORDER_ID)
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
                .setUuid(RECEIPT_UUID)
                .setTrip(mTrip)
                .setName(NAME)
                .setPrice(PRICE)
                .setTax(TAX)
                .setExchangeRate(EXCHANGE_RATE)
                .setCategory(CATEGORY)
                .setFile(RECEIPT_FILE)
                .setDate(DATE)
                .setCustomOrderId(CUSTOM_ORDER_ID)
                .setTimeZone(TIMEZONE)
                .setComment(COMMENT)
                .setIsReimbursable(REIMBURSABLE)
                .setCurrency(CURRENCY_CODE)
                .setIsFullPage(!FULL_PAGE)
                .setIndex(DESCENDING_INDEX)
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
                .setUuid(RECEIPT_UUID)
                .setTrip(mTrip)
                .setName(NAME)
                .setPrice(PRICE)
                .setTax(TAX)
                .setExchangeRate(EXCHANGE_RATE)
                .setCategory(CATEGORY)
                .setFile(RECEIPT_FILE)
                .setDate(DATE)
                .setCustomOrderId(CUSTOM_ORDER_ID)
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
    public void writeUnsynced() throws Exception {
        final String sync = "sync";
        final ContentValues syncValues = new ContentValues();
        syncValues.put(sync, sync);
        when(mSyncStateAdapter.writeUnsynced(mSyncState)).thenReturn(syncValues);

        final ContentValues contentValues = mReceiptDatabaseAdapter.write(mReceipt, new DatabaseOperationMetadata());

        // Note: Full page is backwards in the database
        assertEquals(RECEIPT_FILE.getName(), contentValues.getAsString(ReceiptsTable.COLUMN_PATH));
        assertEquals(NAME, contentValues.getAsString(ReceiptsTable.COLUMN_NAME));
        assertTrue(PARENT_TRIP_ID == contentValues.getAsInteger(ReceiptsTable.COLUMN_PARENT_TRIP_ID));
        assertEquals(CATEGORY_ID, (int) contentValues.getAsInteger(ReceiptsTable.COLUMN_CATEGORY_ID));
        assertEquals(PRICE, contentValues.getAsDouble(ReceiptsTable.COLUMN_PRICE), 0.0001d);
        assertEquals(TAX, contentValues.getAsDouble(ReceiptsTable.COLUMN_TAX), 0.0001d);
        assertEquals(EXCHANGE_RATE_FOR_USD, contentValues.getAsDouble(ReceiptsTable.COLUMN_EXCHANGE_RATE), 0.0001d);
        assertEquals(DATE, (long) contentValues.getAsLong(ReceiptsTable.COLUMN_DATE));
        assertEquals(TIMEZONE, contentValues.getAsString(ReceiptsTable.COLUMN_TIMEZONE));
        assertEquals(COMMENT, contentValues.getAsString(ReceiptsTable.COLUMN_COMMENT));
        assertEquals(REIMBURSABLE, contentValues.getAsBoolean(ReceiptsTable.COLUMN_REIMBURSABLE));
        assertEquals(CURRENCY_CODE, contentValues.getAsString(ReceiptsTable.COLUMN_ISO4217));
        assertEquals(PAYMENT_METHOD_ID, (int) contentValues.getAsInteger(ReceiptsTable.COLUMN_PAYMENT_METHOD_ID));
        assertEquals(!FULL_PAGE, contentValues.getAsBoolean(ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE));
        assertEquals(EXTRA1, contentValues.getAsString(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1));
        assertEquals(EXTRA2, contentValues.getAsString(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2));
        assertEquals(EXTRA3, contentValues.getAsString(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3));
        assertEquals(CUSTOM_ORDER_ID, (long) contentValues.getAsLong(ReceiptsTable.COLUMN_CUSTOM_ORDER_ID));
        assertEquals(RECEIPT_UUID.toString(), contentValues.getAsString(ReceiptsTable.COLUMN_UUID));
        assertEquals(sync, contentValues.getAsString(sync));
        assertFalse(contentValues.containsKey(ReceiptsTable.COLUMN_ID));
    }

    @Test
    public void write() throws Exception {
        final String sync = "sync";
        final ContentValues syncValues = new ContentValues();
        syncValues.put(sync, sync);
        when(mSyncStateAdapter.write(mSyncState)).thenReturn(syncValues);

        final ContentValues contentValues = mReceiptDatabaseAdapter.write(mReceipt, new DatabaseOperationMetadata(OperationFamilyType.Sync));

        // Note: Full page is backwards in the database
        assertEquals(RECEIPT_FILE.getName(), contentValues.getAsString(ReceiptsTable.COLUMN_PATH));
        assertEquals(NAME, contentValues.getAsString(ReceiptsTable.COLUMN_NAME));
        assertTrue(PARENT_TRIP_ID == contentValues.getAsInteger(ReceiptsTable.COLUMN_PARENT_TRIP_ID));
        assertEquals(CATEGORY_ID, (int) contentValues.getAsInteger(ReceiptsTable.COLUMN_CATEGORY_ID));
        assertEquals(PRICE, contentValues.getAsDouble(ReceiptsTable.COLUMN_PRICE), 0.0001d);
        assertEquals(TAX, contentValues.getAsDouble(ReceiptsTable.COLUMN_TAX), 0.0001d);
        assertEquals(EXCHANGE_RATE_FOR_USD, contentValues.getAsDouble(ReceiptsTable.COLUMN_EXCHANGE_RATE), 0.0001d);
        assertEquals(DATE, (long) contentValues.getAsLong(ReceiptsTable.COLUMN_DATE));
        assertEquals(TIMEZONE, contentValues.getAsString(ReceiptsTable.COLUMN_TIMEZONE));
        assertEquals(COMMENT, contentValues.getAsString(ReceiptsTable.COLUMN_COMMENT));
        assertEquals(REIMBURSABLE, contentValues.getAsBoolean(ReceiptsTable.COLUMN_REIMBURSABLE));
        assertEquals(CURRENCY_CODE, contentValues.getAsString(ReceiptsTable.COLUMN_ISO4217));
        assertEquals(PAYMENT_METHOD_ID, (int) contentValues.getAsInteger(ReceiptsTable.COLUMN_PAYMENT_METHOD_ID));
        assertEquals(!FULL_PAGE, contentValues.getAsBoolean(ReceiptsTable.COLUMN_NOTFULLPAGEIMAGE));
        assertEquals(EXTRA1, contentValues.getAsString(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_1));
        assertEquals(EXTRA2, contentValues.getAsString(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_2));
        assertEquals(EXTRA3, contentValues.getAsString(ReceiptsTable.COLUMN_EXTRA_EDITTEXT_3));
        assertEquals(CUSTOM_ORDER_ID, (long) contentValues.getAsLong(ReceiptsTable.COLUMN_CUSTOM_ORDER_ID));
        assertEquals(RECEIPT_UUID.toString(), contentValues.getAsString(ReceiptsTable.COLUMN_UUID));
        assertEquals(sync, contentValues.getAsString(sync));
        assertFalse(contentValues.containsKey(ReceiptsTable.COLUMN_ID));
    }

    @Test
    public void build() throws Exception {
        final Receipt receipt = new ReceiptBuilderFactory(ID)
                .setUuid(RECEIPT_UUID)
                .setTrip(mTrip)
                .setName(NAME)
                .setPrice(PRICE)
                .setTax(TAX)
                .setExchangeRate(EXCHANGE_RATE)
                .setCategory(CATEGORY)
                .setFile(RECEIPT_FILE)
                .setDate(DATE)
                .setCustomOrderId(CUSTOM_ORDER_ID)
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
        assertEquals(receipt, mReceiptDatabaseAdapter.build(mReceipt, ID, RECEIPT_UUID, mock(DatabaseOperationMetadata.class)));
        assertEquals(receipt.getSyncState(), mReceiptDatabaseAdapter.build(mReceipt, ID, RECEIPT_UUID, mock(DatabaseOperationMetadata.class)).getSyncState());
    }

}