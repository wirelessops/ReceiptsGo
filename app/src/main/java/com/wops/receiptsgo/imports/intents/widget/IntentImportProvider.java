package com.wops.receiptsgo.imports.intents.widget;

import android.content.Intent;
import androidx.annotation.NonNull;

import io.reactivex.Maybe;

public interface IntentImportProvider {

    /**
     * @return {@link Maybe} that will contain an {@link Intent} so long as it is not null
     */
    @NonNull
    Maybe<Intent> getIntentMaybe();

}
