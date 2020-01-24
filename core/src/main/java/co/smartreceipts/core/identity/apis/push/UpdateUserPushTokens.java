package co.smartreceipts.core.identity.apis.push;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class UpdateUserPushTokens implements Serializable {

    @SerializedName("registration_ids")
    private List<String> registrationIds;

    public UpdateUserPushTokens(@NonNull List<String> registrationIds) {
        this.registrationIds = Preconditions.checkNotNull(registrationIds);
    }

    @Nullable
    public List<String> getRegistrationIds() {
        return registrationIds;
    }
}
