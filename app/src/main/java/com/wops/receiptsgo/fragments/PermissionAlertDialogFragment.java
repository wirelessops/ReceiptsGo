package com.wops.receiptsgo.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.wops.receiptsgo.R;

/**
 * Dialog Fragment which asks if user wants to leave feedback
 */
public class PermissionAlertDialogFragment extends DialogFragment {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @NonNull
    public static PermissionAlertDialogFragment newInstance(@NonNull AppCompatActivity activity) {
        final PermissionAlertDialogFragment permissionDialogFragment = new PermissionAlertDialogFragment();
        final Bundle args = new Bundle();
        args.putBoolean("shouldShowRationale", activity.shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE));
        permissionDialogFragment.setArguments(args);
        return permissionDialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setCancelable(false)
                .setTitle(getString(R.string.storage_permission_required));

        if (getArguments().getBoolean("shouldShowRationale")) {
            builder.setIcon(R.mipmap.ic_launcher)
                    .setMessage(getString(R.string.permission_must_be_granted))
                    .setPositiveButton(getString(R.string.ok), null);
        } else {
            builder.setIcon(R.drawable.ic_error_outline_24dp)
                    .setMessage(getString(R.string.approve_permission))
                    .setNegativeButton(getString(R.string.no), null)
                    .setPositiveButton(getString(R.string.yes), (dialogInterface, i) -> {
                        final Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        getContext().startActivity(intent);
                    });
        }
        return builder.create();
    }
}
