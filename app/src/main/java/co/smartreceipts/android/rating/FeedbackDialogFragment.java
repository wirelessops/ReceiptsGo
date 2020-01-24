package co.smartreceipts.android.rating;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.analytics.events.Events;
import co.smartreceipts.android.purchases.plus.SmartReceiptsTitle;
import co.smartreceipts.android.workers.EmailAssistant;
import dagger.android.support.AndroidSupportInjection;

/**
 * Dialog Fragment which asks if user wants to leave feedback
 */
public class FeedbackDialogFragment extends DialogFragment {

    @Inject
    Analytics analytics;

    @Inject
    SmartReceiptsTitle smartReceiptsTitle;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                    analytics.record(Events.Ratings.UserAcceptedSendingFeedback);
                    openEmailAssistant();
                })
                .setNegativeButton(R.string.no, (dialogInterface, which) -> {
                    analytics.record(Events.Ratings.UserDeclinedSendingFeedback);
                    dismiss();
                })
                .setMessage(R.string.leave_feedback_text);
        return builder.create();
    }

    private void openEmailAssistant() {
        final Intent intent = EmailAssistant.getEmailDeveloperIntent(getString(R.string.feedback, smartReceiptsTitle.get()));
        startActivity(Intent.createChooser(intent, getResources().getString(R.string.send_email)));
    }
}
