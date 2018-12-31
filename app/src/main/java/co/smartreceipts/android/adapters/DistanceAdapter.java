package co.smartreceipts.android.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.google.common.base.Preconditions;

import co.smartreceipts.android.date.DateFormatter;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.sync.BackupProvidersManager;

public class DistanceAdapter extends CardAdapter<Distance> {

    private final DateFormatter dateFormatter;

    public DistanceAdapter(@NonNull Context context,
                           @NonNull UserPreferenceManager preferences,
                           @NonNull BackupProvidersManager backupProvidersManager,
                           @NonNull DateFormatter dateFormatter) {
        super(context, preferences, backupProvidersManager);
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
    }

    @Override
    protected String getPrice(Distance data) {
        return data.getDecimalFormattedDistance();
    }

    @Override
    protected void setPriceTextView(TextView textView, Distance data) {
        textView.setText(data.getDecimalFormattedDistance());
    }

    @Override
    protected void setNameTextView(TextView textView, Distance data) {
        if (!TextUtils.isEmpty(data.getLocation())) {
            textView.setText(data.getLocation());
            textView.setVisibility(View.VISIBLE);
        } else if (!TextUtils.isEmpty(data.getComment())) {
            textView.setText(data.getComment());
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void setCategory(TextView textView, Distance data) {
        textView.setText(dateFormatter.getFormattedDate(data.getDisplayableDate()));
    }

    @Override
    protected void setDateTextView(TextView textView, Distance data) {
        textView.setText(data.getPrice().getCurrencyFormattedPrice());
    }
}
