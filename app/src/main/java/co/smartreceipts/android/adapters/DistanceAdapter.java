package co.smartreceipts.android.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

import androidx.annotation.NonNull;

import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.sync.BackupProvidersManager;

public class DistanceAdapter extends CardAdapter<Distance> {

    // TODO: 25.01.2021 add view types, and sticky date headers
    // TODO: 26.01.2021 if we need title to scroll - it also should be a view type to save RecyclerView inner optimizations
    // TODO: 26.01.2021 OR add something that will separate "DISTANCE" header and scrollable part

    public DistanceAdapter(@NonNull Context context,
                           @NonNull UserPreferenceManager preferences,
                           @NonNull BackupProvidersManager backupProvidersManager) {
        super(context, preferences, backupProvidersManager);
    }

    @Override
    protected String getPrice(Distance data) {
        return data.getDecimalFormattedDistance();
    }

    @Override
    protected void setPriceTextView(TextView textView, Distance data) {
        textView.setText(data.getPrice().getCurrencyFormattedPrice());
    }

    @Override
    protected void setNameTextView(TextView textView, Distance data) {
        textView.setText(data.getDecimalFormattedDistance());
    }

    @Override
    protected void setDetailsTextView(TextView textView, Distance data) {
        final String location = data.getLocation();

        if (!location.isEmpty()) {
            textView.setText(location);
        } else {
            textView.setText(data.getComment());
        }

        textView.setTypeface(null, Typeface.BOLD);
    }
}
