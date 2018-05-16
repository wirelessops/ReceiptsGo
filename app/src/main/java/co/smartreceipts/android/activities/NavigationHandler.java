package co.smartreceipts.android.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.AnimRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.google.common.base.Preconditions;

import java.io.File;
import java.lang.ref.WeakReference;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.di.scopes.ActivityScope;
import co.smartreceipts.android.fragments.ReportInfoFragment;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.ocr.apis.model.OcrResponse;
import co.smartreceipts.android.settings.widget.PreferenceHeaderDistanceFragment;
import co.smartreceipts.android.settings.widget.PreferenceHeaderPrivacyFragment;
import co.smartreceipts.android.settings.widget.PreferenceHeaderReportOutputFragment;
import co.smartreceipts.android.settings.widget.SettingsActivity;
import co.smartreceipts.android.settings.widget.SettingsViewerActivity;
import co.smartreceipts.android.utils.IntentUtils;
import co.smartreceipts.android.utils.log.Logger;

import static android.preference.PreferenceActivity.EXTRA_SHOW_FRAGMENT;

@ActivityScope
public class NavigationHandler<T extends FragmentActivity> {

    private static final int DO_NOT_ANIM = 0;
    private static final int MISSING_RES_ID = -1;

    private final FragmentManager fragmentManager;
    private final FragmentProvider fragmentProvider;
    private final WeakReference<FragmentActivity> fragmentActivityWeakReference;
    private final boolean isDualPane;

    @Inject
    public NavigationHandler(T fragmentActivity, FragmentProvider fragmentProvider) {
        fragmentActivityWeakReference = new WeakReference<>(Preconditions.checkNotNull(fragmentActivity));
        fragmentManager = Preconditions.checkNotNull(fragmentActivity.getSupportFragmentManager());
        this.fragmentProvider = Preconditions.checkNotNull(fragmentProvider);
        isDualPane = Preconditions.checkNotNull(fragmentActivity.getResources().getBoolean(R.bool.isTablet));
    }

    public void navigateToHomeTripsFragment() {
        replaceFragment(fragmentProvider.newTripFragmentInstance(true), R.id.content_list);
    }

    public void navigateUpToTripsFragment() {
        replaceFragment(fragmentProvider.newTripFragmentInstance(false), R.id.content_list);
    }

    public void navigateToReportInfoFragment(@NonNull Trip trip) {
        if (isDualPane) {
            // we don't need to keep several ReportInfoFragment instances in backstack
            final String tag = ReportInfoFragment.class.getName();
            if (fragmentManager.findFragmentByTag(tag) != null) {
                fragmentManager.popBackStackImmediate(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }

            replaceFragment(fragmentProvider.newReportInfoFragment(trip), R.id.content_details);
        } else {
            replaceFragment(fragmentProvider.newReportInfoFragment(trip), R.id.content_list);
        }
    }

    public void navigateToCreateNewReceiptFragment(@NonNull Trip trip, @Nullable File file, @Nullable OcrResponse ocrResponse) {
        if (isDualPane) {
            replaceFragmentWithAnimation(fragmentProvider.newCreateReceiptFragment(trip, file, ocrResponse), R.id.content_details, R.anim.enter_from_bottom, DO_NOT_ANIM);
        } else {
            replaceFragmentWithAnimation(fragmentProvider.newCreateReceiptFragment(trip, file, ocrResponse), R.id.content_list, R.anim.enter_from_bottom, DO_NOT_ANIM);
        }
    }

    public void navigateToEditReceiptFragment(@NonNull Trip trip, @NonNull Receipt receiptToEdit) {
        if (isDualPane) {
            replaceFragment(fragmentProvider.newEditReceiptFragment(trip, receiptToEdit), R.id.content_details);
        } else {
            replaceFragment(fragmentProvider.newEditReceiptFragment(trip, receiptToEdit), R.id.content_list);
        }
    }

    public void navigateToCreateTripFragment() {
        if (isDualPane) {
            replaceFragmentWithAnimation(fragmentProvider.newCreateTripFragment(), R.id.content_details, R.anim.enter_from_bottom, DO_NOT_ANIM);
        } else {
            replaceFragmentWithAnimation(fragmentProvider.newCreateTripFragment(), R.id.content_list, R.anim.enter_from_bottom, DO_NOT_ANIM);
        }
    }

    public void navigateToEditTripFragment(@NonNull Trip tripToEdit) {
        if (isDualPane) {
            replaceFragment(fragmentProvider.newEditTripFragment(tripToEdit), R.id.content_details);
        } else {
            replaceFragment(fragmentProvider.newEditTripFragment(tripToEdit), R.id.content_list);
        }
    }

    public void navigateToOcrConfigurationFragment() {
        if (isDualPane) {
            replaceFragment(fragmentProvider.newOcrConfigurationFragment(), R.id.content_details);
        } else {
            replaceFragment(fragmentProvider.newOcrConfigurationFragment(), R.id.content_list);
        }
    }

    public void navigateToViewReceiptImage(@NonNull Receipt receipt) {
        if (isDualPane) {
            replaceFragment(fragmentProvider.newReceiptImageFragment(receipt), R.id.content_details);
        } else {
            replaceFragment(fragmentProvider.newReceiptImageFragment(receipt), R.id.content_list);
        }
    }

    public void navigateToViewReceiptPdf(@NonNull Receipt receipt) {
        final FragmentActivity activity = fragmentActivityWeakReference.get();
        if (activity != null && receipt.getFile() != null) {
            try {
                final Intent intent;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Logger.debug(this, "Creating a PDF view intent with a content scheme");
                    intent = IntentUtils.getViewIntent(activity, receipt.getFile(), "application/pdf");
                } else {
                    Logger.debug(this, "Creating a PDF view intent with a file scheme");
                    intent = IntentUtils.getLegacyViewIntent(activity, receipt.getFile(), "application/pdf");
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                activity.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(activity, R.string.error_no_pdf_activity_viewer, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void navigateToBackupMenu() {
        if (isDualPane) {
            replaceFragment(fragmentProvider.newBackupsFragment(), R.id.content_details);
        } else {
            replaceFragment(fragmentProvider.newBackupsFragment(), R.id.content_list);
        }
    }

    public void navigateToLoginScreen() {
        if (isDualPane) {
            replaceFragment(fragmentProvider.newLoginFragment(), R.id.content_details);
        } else {
            replaceFragment(fragmentProvider.newLoginFragment(), R.id.content_list);
        }
    }

    public void navigateToSettings() {
        final FragmentActivity activity = fragmentActivityWeakReference.get();
        if (activity != null) {
            final Intent intent = new Intent(activity, SettingsActivity.class);
            activity.startActivity(intent);
        }
    }

    public void navigateToSettingsScrollToReportSection() {
        final FragmentActivity activity = fragmentActivityWeakReference.get();
        if (activity != null) {
            final Intent intent = new Intent(activity, SettingsActivity.class);
            if (isDualPane) {
                intent.putExtra(EXTRA_SHOW_FRAGMENT, PreferenceHeaderReportOutputFragment.class.getName());
            } else {
                intent.putExtra(SettingsActivity.EXTRA_GO_TO_CATEGORY, R.string.pref_output_header_key);
            }

            activity.startActivity(intent);
        }
    }

    public void navigateToSettingsScrollToDistanceSection() {
        final FragmentActivity activity = fragmentActivityWeakReference.get();
        if (activity != null) {
            final Intent intent = new Intent(activity, SettingsActivity.class);
            if (isDualPane) {
                intent.putExtra(EXTRA_SHOW_FRAGMENT, PreferenceHeaderDistanceFragment.class.getName());
            } else {
                intent.putExtra(SettingsActivity.EXTRA_GO_TO_CATEGORY, R.string.pref_distance_header_key);
            }

            activity.startActivity(intent);
        }
    }

    public void navigateToSettingsScrollToPrivacySection() {
        final FragmentActivity activity = fragmentActivityWeakReference.get();
        if (activity != null) {
            final Intent intent = new Intent(activity, SettingsActivity.class);
            if (isDualPane) {
                intent.putExtra(EXTRA_SHOW_FRAGMENT, PreferenceHeaderPrivacyFragment.class.getName());
            } else {
                intent.putExtra(SettingsActivity.EXTRA_GO_TO_CATEGORY, R.string.pref_privacy_header_key);
            }

            activity.startActivity(intent);
        }
    }

    public void navigateToCategoriesEditor() {
        final FragmentActivity activity = fragmentActivityWeakReference.get();
        if (activity != null) {
            final Intent intent = new Intent(activity, SettingsViewerActivity.class);
            intent.putExtra(SettingsViewerActivity.KEY_FLAG, activity.getString(R.string.pref_receipt_customize_categories_key));
            activity.startActivity(intent);
        }
    }

    public void navigateToPaymentMethodsEditor() {
        final FragmentActivity activity = fragmentActivityWeakReference.get();
        if (activity != null) {
            final Intent intent = new Intent(activity, SettingsViewerActivity.class);
            intent.putExtra(SettingsViewerActivity.KEY_FLAG, activity.getString(R.string.pref_receipt_payment_methods_key));
            activity.startActivity(intent);
        }
    }

    public boolean navigateBack() {
        try {
            return fragmentManager.popBackStackImmediate();
        } catch (final IllegalStateException e) {
            // This exception is always thrown if saveInstanceState was already been called.
            return false;
        }
    }

    public boolean navigateBackDelayed() {
        try {
            fragmentManager.popBackStack();
            return true;
        } catch (final IllegalStateException e) {
            // This exception is always thrown if saveInstanceState was already been called.
            return false;
        }
    }

    public void showDialog(@NonNull DialogFragment dialogFragment) {
        final String tag = dialogFragment.getClass().getName();
        try {
            dialogFragment.show(fragmentManager, tag);
        } catch (IllegalStateException e) {
            // This exception is always thrown if saveInstanceState was already been called.
        }
    }

    public boolean isDualPane() {
        return isDualPane;
    }

    public boolean shouldFinishOnBackNavigation() {
        return fragmentManager.getBackStackEntryCount() == 1;
    }

    private void replaceFragment(@NonNull Fragment fragment, @IdRes int layoutResId) {
        replaceFragmentWithAnimation(fragment, layoutResId, MISSING_RES_ID, MISSING_RES_ID);
    }

    private void replaceFragmentWithAnimation(@NonNull Fragment fragment, @IdRes int layoutResId, @AnimRes int enterAnimId, @AnimRes int exitAnimId) {
        final String tag = fragment.getClass().getName();
        boolean wasFragmentPopped;
        try {
            wasFragmentPopped = fragmentManager.popBackStackImmediate(tag, 0);
        } catch (final IllegalStateException e) {
            // This exception is always thrown if saveInstanceState was already been called.
            wasFragmentPopped = false;
        }
        if (!wasFragmentPopped) {
            final FragmentTransaction transaction = fragmentManager.beginTransaction();
            if (enterAnimId >= 0 && exitAnimId >= 0) {
                transaction.setCustomAnimations(enterAnimId, exitAnimId);
            }
            transaction.replace(layoutResId, fragment, tag)
                    .addToBackStack(tag)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
    }
}
