package co.smartreceipts.android.utils;

import android.content.Context;
import android.support.annotation.NonNull;

public interface Feature {

    /**
     * Checks if this particular feature is enabled
     *
     * @param context the applicaiton {@link Context}
     * @return {@code true} if it is enabled. {@code false} otherwise
     */
    boolean isEnabled(@NonNull Context context);

}
