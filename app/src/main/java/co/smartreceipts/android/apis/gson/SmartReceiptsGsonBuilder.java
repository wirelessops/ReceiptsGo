package co.smartreceipts.android.apis.gson;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Receipt;

public class SmartReceiptsGsonBuilder {

    private final ColumnDefinitions<Receipt> mReceiptColumnDefinitions;

    public SmartReceiptsGsonBuilder(@NonNull ColumnDefinitions<Receipt> receiptColumnDefinitions) {
        mReceiptColumnDefinitions = Preconditions.checkNotNull(receiptColumnDefinitions);
    }

    @NonNull
    public Gson create() {
        final GsonBuilder builder = new GsonBuilder();
        builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        builder.registerTypeAdapter(Date.class, new GsonUtcDateAdapter());
        builder.registerTypeAdapter(Column.class, new ColumnGsonAdpater(mReceiptColumnDefinitions));
        builder.registerTypeAdapter(PaymentMethod.class, new PaymentMethodGsonAdapter());
        builder.registerTypeAdapter(Category.class, new CategoryGsonAdapter());
        return builder.create();
    }
}
