package com.wops.receiptsgo.rating;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import javax.inject.Inject;

import com.wops.receiptsgo.R;
import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.analytics.events.Events;
import com.wops.receiptsgo.utils.IntentUtils;
import dagger.android.support.AndroidSupportInjection;

/**
 * Dialog Fragment which asks if user wants to rate the app
 */
public class RatingDialogFragment extends DialogFragment {

    @Inject
    Analytics analytics;
    @Inject
    AppRatingManager appRatingManager;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getContext();

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.apprating_dialog_title, getApplicationName()))
                .setMessage(R.string.apprating_dialog_message)
                .setNegativeButton(R.string.apprating_dialog_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        analytics.record(Events.Ratings.UserSelectedNever);
                        dismiss();
                    }
                })
                .setNeutralButton(R.string.apprating_dialog_neutral, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        analytics.record(Events.Ratings.UserSelectedLater);
                        prorogueRatingPrompt();
                    }
                })
                .setPositiveButton(R.string.apprating_dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        analytics.record(Events.Ratings.UserSelectedRate);
                        launchRatingIntent();
                    }
                });
        return builder.create();
    }

    private void launchRatingIntent() {
        Context context = getContext();
        if (context != null) {
            context.startActivity(IntentUtils.getRatingIntent(context));
        }
    }

    private void prorogueRatingPrompt() {
        appRatingManager.prorogueRatingPrompt();
    }

    private String getApplicationName() {
        final PackageManager packageManager = getContext().getPackageManager();
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getContext().getPackageName(), 0);
            return packageManager.getApplicationLabel(applicationInfo).toString();
        } catch (final PackageManager.NameNotFoundException e) {
            return null;
        }
    }
}
