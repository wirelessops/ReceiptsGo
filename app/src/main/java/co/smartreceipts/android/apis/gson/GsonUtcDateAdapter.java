package co.smartreceipts.android.apis.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import co.smartreceipts.android.date.Iso8601DateFormat;

public class GsonUtcDateAdapter implements GsonAdapter<Date> {

    private final DateFormat iso8601DateFormat = new Iso8601DateFormat();

    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return iso8601DateFormat.parse(json.getAsString());
        } catch (ParseException e) {
            throw new JsonParseException(e);
        }
    }
}
