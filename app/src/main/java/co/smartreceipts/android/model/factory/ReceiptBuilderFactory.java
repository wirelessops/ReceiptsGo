package co.smartreceipts.android.model.factory;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.TimeZone;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.currency.PriceCurrency;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Source;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.model.impl.DefaultReceiptImpl;
import co.smartreceipts.android.model.impl.ImmutableCategoryImpl;
import co.smartreceipts.android.model.impl.ImmutablePaymentMethodImpl;
import co.smartreceipts.android.sync.model.SyncState;
import co.smartreceipts.android.sync.model.impl.DefaultSyncState;

/**
 * A {@link co.smartreceipts.android.model.Receipt} {@link BuilderFactory}
 * implementation, which will be used to generate instances of {@link co.smartreceipts.android.model.Receipt} objects
 */
public class ReceiptBuilderFactory implements BuilderFactory<Receipt> {

    private static final int UNKNOWN_ID = -1;

    private Trip _trip;
    private PaymentMethod _paymentMethod;
    private File _file;
    private String _name;
    private Category _category;
    private String _comment;
    private String _extraEditText1;
    private String _extraEditText2;
    private String _extraEditText3;
    private final PriceBuilderFactory _priceBuilderFactory, _taxBuilderFactory;
    private Date _date;
    private TimeZone _timezone;
    private int _id;
    private int _index;
    private boolean _isReimbursable, _isFullPage, _isSelected;
    private Source _source;
    private SyncState _syncState;

    public ReceiptBuilderFactory() {
        this(UNKNOWN_ID);
    }

    public ReceiptBuilderFactory(int id) {
        _id = id;
        _name = "";
        _comment = "";
        _priceBuilderFactory = new PriceBuilderFactory();
        _taxBuilderFactory = new PriceBuilderFactory();
        _date = new Date(System.currentTimeMillis());
        _timezone = TimeZone.getDefault();
        _index = -1;
        _source = Source.Undefined;
        _syncState = new DefaultSyncState();
    }

    public ReceiptBuilderFactory(@NonNull Receipt receipt) {
        _id = receipt.getId();
        _trip = receipt.getTrip();
        _name = receipt.getName();
        _file = receipt.getFile();
        _priceBuilderFactory = new PriceBuilderFactory().setPrice(receipt.getPrice());
        _taxBuilderFactory = new PriceBuilderFactory().setPrice(receipt.getTax());
        _date = (Date) receipt.getDate().clone();
        _timezone = receipt.getTimeZone();
        _category = receipt.getCategory();
        _comment = receipt.getComment();
        _paymentMethod = receipt.getPaymentMethod();
        _isReimbursable = receipt.isReimbursable();
        _isFullPage = receipt.isFullPage();
        _isSelected = receipt.isSelected();
        _extraEditText1 = receipt.getExtraEditText1();
        _extraEditText2 = receipt.getExtraEditText2();
        _extraEditText3 = receipt.getExtraEditText3();
        _index = receipt.getIndex();
        _source = receipt.getSource();
        _syncState = receipt.getSyncState();
    }

    public ReceiptBuilderFactory(int id, @NonNull Receipt receipt) {
        this(receipt);
        _id = id;
    }

    public ReceiptBuilderFactory setTrip(@NonNull Trip trip) {
        _trip = trip;
        return this;
    }

    public ReceiptBuilderFactory setPaymentMethod(PaymentMethod method) {
        _paymentMethod = method;
        return this;
    }

    public ReceiptBuilderFactory setName(@NonNull String name) {
        _name = name;
        return this;
    }

    @Deprecated
    public ReceiptBuilderFactory setCategory(@NonNull String category) {
        // TODO: Delete me
        _category = new ImmutableCategoryImpl(category, "");
        return this;
    }

    public ReceiptBuilderFactory setCategory(@NonNull Category category) {
        _category = category;
        return this;
    }

    public ReceiptBuilderFactory setComment(@NonNull String comment) {
        _comment = comment;
        return this;
    }

    /**
     * Sets the price of this ReceiptBuilderFactory as a string (useful for user input)
     *
     * @param price - the desired price as a string
     * @return the {@link ReceiptBuilderFactory} instance for method chaining
     */
    public ReceiptBuilderFactory setPrice(String price) {
        _priceBuilderFactory.setPrice(price);
        return this;
    }

    public ReceiptBuilderFactory setPrice(double price) {
        _priceBuilderFactory.setPrice(price);
        return this;
    }

    public ReceiptBuilderFactory setPrice(BigDecimal price) {
        _priceBuilderFactory.setPrice(price);
        return this;
    }

    public ReceiptBuilderFactory setPrice(Price price) {
        _priceBuilderFactory.setPrice(price);
        return this;
    }

    public ReceiptBuilderFactory setExchangeRate(ExchangeRate exchangeRate) {
        _priceBuilderFactory.setExchangeRate(exchangeRate);
        _taxBuilderFactory.setExchangeRate(exchangeRate);
        return this;
    }

    /**
     * Sets the tax of this ReceiptBuilderFactory as a string (useful for user input)
     *
     * @param tax - the desired tax as a string
     * @return the {@link ReceiptBuilderFactory} instance for method chaining
     */
    public ReceiptBuilderFactory setTax(String tax) {
        _taxBuilderFactory.setPrice(tax);
        return this;
    }

    public ReceiptBuilderFactory setTax(double tax) {
        _taxBuilderFactory.setPrice(tax);
        return this;
    }

    public ReceiptBuilderFactory setTax(Price tax) {
        _taxBuilderFactory.setPrice(tax);
        return this;
    }

    public ReceiptBuilderFactory setFile(File file) {
        _file = file;
        return this;
    }

    public ReceiptBuilderFactory setImage(File image) {
        _file = image;
        return this;
    }

    public ReceiptBuilderFactory setPDF(File pdf) {
        _file = pdf;
        return this;
    }

    public ReceiptBuilderFactory setDate(Date date) {
        _date = date;
        return this;
    }

    public ReceiptBuilderFactory setDate(long datetime) {
        _date = new Date(datetime);
        return this;
    }

    public ReceiptBuilderFactory setTimeZone(@Nullable String timeZoneId) {
        if (timeZoneId != null) {
            _timezone = TimeZone.getTimeZone(timeZoneId);
        }
        return this;
    }

    public ReceiptBuilderFactory setTimeZone(TimeZone timeZone) {
        _timezone = timeZone;
        return this;
    }

    public ReceiptBuilderFactory setIsReimbursable(boolean isReimbursable) {
        _isReimbursable = isReimbursable;
        return this;
    }

    public ReceiptBuilderFactory setIsFullPage(boolean isFullPage) {
        _isFullPage = isFullPage;
        return this;
    }

    public ReceiptBuilderFactory setIsSelected(boolean isSelected) {
        _isSelected = isSelected;
        return this;
    }

    public ReceiptBuilderFactory setCurrency(PriceCurrency currency) {
        _priceBuilderFactory.setCurrency(currency);
        _taxBuilderFactory.setCurrency(currency);
        return this;
    }

    public ReceiptBuilderFactory setCurrency(String currencyCode) {
        _priceBuilderFactory.setCurrency(currencyCode);
        _taxBuilderFactory.setCurrency(currencyCode);
        return this;
    }

    public ReceiptBuilderFactory setExtraEditText1(String extraEditText1) {
        _extraEditText1 = extraEditText1;
        return this;
    }

    public ReceiptBuilderFactory setExtraEditText2(String extraEditText2) {
        _extraEditText2 = extraEditText2;
        return this;
    }

    public ReceiptBuilderFactory setExtraEditText3(String extraEditText3) {
        _extraEditText3 = extraEditText3;
        return this;
    }

    public ReceiptBuilderFactory setIndex(int index) {
        _index = index;
        return this;
    }

    public ReceiptBuilderFactory setSyncState(@NonNull SyncState syncState) {
        _syncState = Preconditions.checkNotNull(syncState);
        return this;
    }

    @Override
    @NonNull
    public Receipt build() {
        return new DefaultReceiptImpl(_id, _index, _trip, _file, _paymentMethod == null ? ImmutablePaymentMethodImpl.NONE : _paymentMethod, _name, _category, _comment, _priceBuilderFactory.build(), _taxBuilderFactory.build(), _date, _timezone, _isReimbursable, _isFullPage, _isSelected, _source, _extraEditText1, _extraEditText2, _extraEditText3, _syncState);
    }

}
