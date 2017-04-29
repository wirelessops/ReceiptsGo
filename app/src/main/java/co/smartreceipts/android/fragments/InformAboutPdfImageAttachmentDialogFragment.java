package co.smartreceipts.android.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.imports.intents.model.FileType;
import co.smartreceipts.android.settings.UserPreferenceManager;
import dagger.android.support.AndroidSupportInjection;

public class InformAboutPdfImageAttachmentDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private static final String ARG_CONTENT_TYPE_STRING_ID = "arg_content_type_string_id";
    private static final String BOOL_ACTION_SEND_SHOW_HELP_DIALOG = "ShowHelpDialog";

    @Inject
    UserPreferenceManager preferenceManager;

    @StringRes
    private int contentTypeStringResId;

    public static boolean shouldInformAboutPdfImageAttachmentDialogFragment(@NonNull UserPreferenceManager preferences) {
        return Preconditions.checkNotNull(preferences).getSharedPreferences().getBoolean(BOOL_ACTION_SEND_SHOW_HELP_DIALOG, true);
    }

    @NonNull
    public static InformAboutPdfImageAttachmentDialogFragment newInstance(@NonNull FileType fileType) {
        final InformAboutPdfImageAttachmentDialogFragment fragment = new InformAboutPdfImageAttachmentDialogFragment();
        final Bundle args = new Bundle();
        final int stringId = Preconditions.checkNotNull(fileType) == FileType.Pdf ? R.string.pdf : R.string.image;
        args.putInt(ARG_CONTENT_TYPE_STRING_ID, stringId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contentTypeStringResId = getArguments().getInt(ARG_CONTENT_TYPE_STRING_ID);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.dialog_attachment_title, getString(contentTypeStringResId)));
        builder.setMessage(getString(R.string.dialog_attachment_text, getString(contentTypeStringResId)));
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.dialog_attachment_positive, this);
        builder.setNegativeButton(R.string.dialog_attachment_negative, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        if (which == DialogInterface.BUTTON_NEGATIVE) {
            preferenceManager.getSharedPreferences().edit().putBoolean(BOOL_ACTION_SEND_SHOW_HELP_DIALOG, false).apply();
        }
        dismiss();
    }
}
