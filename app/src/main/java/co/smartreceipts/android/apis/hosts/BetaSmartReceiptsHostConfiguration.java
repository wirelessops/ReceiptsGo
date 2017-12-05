package co.smartreceipts.android.apis.hosts;

import android.support.annotation.NonNull;

import co.smartreceipts.android.apis.gson.SmartReceiptsGsonBuilder;
import co.smartreceipts.android.identity.store.IdentityStore;

public class BetaSmartReceiptsHostConfiguration extends SmartReceiptsHostConfiguration {

    public BetaSmartReceiptsHostConfiguration(@NonNull IdentityStore identityStore, @NonNull SmartReceiptsGsonBuilder smartReceiptsGsonBuilder) {
        super(identityStore, smartReceiptsGsonBuilder);
    }

    @NonNull
    @Override
    public String getBaseUrl() {
        return "https://beta.smartreceipts.co";
    }

}
