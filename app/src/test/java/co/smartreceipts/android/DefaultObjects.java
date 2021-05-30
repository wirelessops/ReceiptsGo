package co.smartreceipts.android;

import androidx.annotation.NonNull;

import org.joda.money.CurrencyUnit;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TimeZone;

import co.smartreceipts.android.date.DisplayableDate;
import co.smartreceipts.android.identity.apis.organizations.AppSettings;
import co.smartreceipts.android.identity.apis.organizations.Configurations;
import co.smartreceipts.android.identity.apis.organizations.Error;
import co.smartreceipts.android.identity.apis.organizations.Organization;
import co.smartreceipts.android.model.AutoCompleteMetadata;
import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Keyed;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.CategoryBuilderFactory;
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory;
import co.smartreceipts.android.model.gson.ExchangeRate;
import co.smartreceipts.android.model.impl.SinglePriceImpl;
import co.smartreceipts.android.model.utils.CurrencyUtils;
import co.smartreceipts.core.sync.model.SyncState;
import co.smartreceipts.core.sync.model.impl.DefaultSyncState;
import co.smartreceipts.core.sync.model.impl.Identifier;
import co.smartreceipts.core.sync.model.impl.IdentifierMap;
import co.smartreceipts.core.sync.model.impl.MarkedForDeletionMap;
import co.smartreceipts.core.sync.model.impl.SyncStatusMap;
import co.smartreceipts.core.sync.provider.SyncProvider;

public class DefaultObjects {

    private DefaultObjects() {
    }

    @NonNull
    public static Trip newDefaultTrip() {
        return new Trip(Keyed.MISSING_ID, Keyed.Companion.getMISSING_UUID(),
                new File(new File("").getAbsolutePath()),
                new DisplayableDate(new Date(System.currentTimeMillis()), TimeZone.getDefault()),
                new DisplayableDate(new Date(System.currentTimeMillis()), TimeZone.getDefault()),
                CurrencyUtils.INSTANCE.getDefaultCurrency(), "comment", "costCenter", newAutoCompleteMetadata());
    }

    @NonNull
    public static Trip newDefaultTrip(Price price) {
        return new Trip(Keyed.MISSING_ID, Keyed.Companion.getMISSING_UUID(),
                new File(new File("").getAbsolutePath()),
                new DisplayableDate(new Date(System.currentTimeMillis()), TimeZone.getDefault()),
                new DisplayableDate(new Date(System.currentTimeMillis()), TimeZone.getDefault()),
                CurrencyUtils.INSTANCE.getDefaultCurrency(), "comment", "costCenter", newDefaultSyncState(),
                price, newAutoCompleteMetadata());
    }

    @NonNull
    public static SyncState newDefaultSyncState() {
        return new DefaultSyncState(new IdentifierMap(Collections.singletonMap(SyncProvider.GoogleDrive, new Identifier("abc"))),
                new SyncStatusMap(Collections.singletonMap(SyncProvider.GoogleDrive, true)),
                new MarkedForDeletionMap(Collections.singletonMap(SyncProvider.GoogleDrive, true)),
                new Date(System.currentTimeMillis()));
    }

    @NonNull
    public static PaymentMethod newDefaultPaymentMethod() {
        return new PaymentMethodBuilderFactory().setId(23).setMethod("method").build();
    }

    @NonNull
    public static Category newDefaultCategory() {
        return new CategoryBuilderFactory().setName("name").setCode("code").build();
    }


    @NonNull
    public static Price newDefaultPrice() {
        return new SinglePriceImpl(new BigDecimal(5), CurrencyUnit.USD, new ExchangeRate("USD", Collections.singletonMap("USD", 1.00d)));
    }

    @NonNull
    public static Price newDefaultTax() {
        return new SinglePriceImpl(new BigDecimal(2), CurrencyUnit.USD, new ExchangeRate("USD", Collections.singletonMap("USD", 1.00d)));
    }

    @NonNull
    public static AutoCompleteMetadata newAutoCompleteMetadata() {
        return new AutoCompleteMetadata(false, false, false, false);
    }

    @NonNull
    public static Organization newOrganization() {
        final AppSettings appSettings = new AppSettings(new Configurations(true), new HashMap<>(), new ArrayList<Category>(),
                new ArrayList<PaymentMethod>(), new ArrayList<Column<Receipt>>(), new ArrayList<Column<Receipt>>());

        return new Organization("id", "Organization Name", new Date(2020, 5, 18), appSettings, new ArrayList<>(), new Error());
    }
}
