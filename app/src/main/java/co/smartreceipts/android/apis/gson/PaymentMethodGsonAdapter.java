package co.smartreceipts.android.apis.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.factory.PaymentMethodBuilderFactory;

public class PaymentMethodGsonAdapter implements GsonAdapter<PaymentMethod> {

    private final String METHOD = "Code";
    private final String CUSTOM_ORDER = "Custom_order_id";

    @Override
    public PaymentMethod deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();
        final String method = jsonObject.get(METHOD).getAsString();
        final int customOrderId = jsonObject.get(CUSTOM_ORDER).getAsInt();
        return new PaymentMethodBuilderFactory()
                .setMethod(method)
                .setCustomOrderId(customOrderId)
                .build();
    }
}
