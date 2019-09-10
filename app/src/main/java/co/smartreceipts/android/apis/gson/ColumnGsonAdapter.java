package co.smartreceipts.android.apis.gson;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.ColumnDefinitions;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.ColumnBuilderFactory;

public class ColumnGsonAdapter implements GsonAdapter<Column<Receipt>> {

    private final ColumnDefinitions<Receipt> mReceiptColumnDefinitions;

    public ColumnGsonAdapter(@NonNull ColumnDefinitions<Receipt> receiptColumnDefinitions) {
        mReceiptColumnDefinitions = Preconditions.checkNotNull(receiptColumnDefinitions);
    }

    @Override
    public Column<Receipt> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final int type = json.getAsInt();
        return new ColumnBuilderFactory<>(mReceiptColumnDefinitions).setColumnType(type).build();
    }
}
