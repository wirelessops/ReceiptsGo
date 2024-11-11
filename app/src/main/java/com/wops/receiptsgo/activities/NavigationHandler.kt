package com.wops.receiptsgo.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.preference.PreferenceActivity
import android.widget.Toast
import androidx.annotation.AnimRes
import androidx.annotation.IdRes
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.wops.analytics.log.Logger.debug
import com.wops.analytics.log.Logger.error
import com.wops.receiptsgo.R
import com.wops.receiptsgo.fragments.ReportInfoFragment
import com.wops.receiptsgo.images.CropImageActivity
import com.wops.receiptsgo.imports.RequestCodes
import com.wops.receiptsgo.model.Distance
import com.wops.receiptsgo.model.Receipt
import com.wops.receiptsgo.model.Trip
import com.wops.receiptsgo.ocr.apis.model.OcrResponse
import com.wops.receiptsgo.search.SearchActivity
import com.wops.receiptsgo.settings.widget.AbstractPreferenceHeaderFragment
import com.wops.receiptsgo.settings.widget.PreferenceHeaderDistanceFragment
import com.wops.receiptsgo.settings.widget.PreferenceHeaderPrivacyFragment
import com.wops.receiptsgo.settings.widget.PreferenceHeaderReceiptsFragment
import com.wops.receiptsgo.settings.widget.PreferenceHeaderReportOutputFragment
import com.wops.receiptsgo.settings.widget.SettingsActivity
import com.wops.receiptsgo.settings.widget.SettingsViewerActivity
import com.wops.receiptsgo.subscriptions.SubscriptionsActivity
import com.wops.receiptsgo.utils.IntentUtils
import com.wops.core.di.scopes.ActivityScope
import com.google.common.base.Preconditions
import java.io.File
import java.lang.ref.WeakReference
import java.sql.Date
import javax.inject.Inject

@ActivityScope
class NavigationHandler<T : FragmentActivity> @Inject constructor(
    fragmentActivity: T,
    fragmentProvider: FragmentProvider
) {
    private val fragmentManager: FragmentManager
    private val fragmentProvider: FragmentProvider
    private val fragmentActivityWeakReference: WeakReference<FragmentActivity>
    val isDualPane: Boolean

    init {
        fragmentActivityWeakReference = WeakReference(Preconditions.checkNotNull(fragmentActivity))
        fragmentManager = Preconditions.checkNotNull(
            fragmentActivity.supportFragmentManager
        )
        this.fragmentProvider = Preconditions.checkNotNull(fragmentProvider)
        isDualPane = Preconditions.checkNotNull(fragmentActivity.resources.getBoolean(R.bool.isTablet))
    }

    fun navigateToHomeTripsFragment() {
        replaceFragment(fragmentProvider.newTripFragmentInstance(), R.id.content_list)
    }

    fun navigateToReportInfoFragment(trip: Trip) {
        if (isDualPane) {
            // we don't need to keep several ReportInfoFragment instances in backstack
            val tag = ReportInfoFragment::class.java.name
            if (fragmentManager.findFragmentByTag(tag) != null) {
                fragmentManager.popBackStackImmediate(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
            replaceFragment(fragmentProvider.newReportInfoFragment(trip), R.id.content_details)
        } else {
            replaceFragment(fragmentProvider.newReportInfoFragment(trip), R.id.content_list)
        }
    }

    fun navigateToCreateNewReceiptFragment(trip: Trip, file: File?, ocrResponse: OcrResponse?) {
        if (isDualPane) {
            replaceFragmentWithAnimation(
                fragmentProvider.newCreateReceiptFragment(trip, file, ocrResponse),
                R.id.content_details,
                R.anim.enter_from_bottom,
                DO_NOT_ANIM
            )
        } else {
            replaceFragmentWithAnimation(
                fragmentProvider.newCreateReceiptFragment(trip, file, ocrResponse),
                R.id.content_list,
                R.anim.enter_from_bottom,
                DO_NOT_ANIM
            )
        }
    }

    fun navigateToEditReceiptFragment(trip: Trip, receiptToEdit: Receipt) {
        if (isDualPane) {
            replaceFragment(fragmentProvider.newEditReceiptFragment(trip, receiptToEdit), R.id.content_details)
        } else {
            replaceFragment(fragmentProvider.newEditReceiptFragment(trip, receiptToEdit), R.id.content_list)
        }
    }

    fun navigateToCreateNewDistanceFragment(trip: Trip, suggestedDate: Date?) {
        if (isDualPane) {
            replaceFragmentWithAnimation(
                fragmentProvider.newCreateDistanceFragment(trip, suggestedDate),
                R.id.content_details,
                R.anim.enter_from_bottom,
                DO_NOT_ANIM
            )
        } else {
            replaceFragmentWithAnimation(
                fragmentProvider.newCreateDistanceFragment(trip, suggestedDate),
                R.id.content_list,
                R.anim.enter_from_bottom,
                DO_NOT_ANIM
            )
        }
    }

    fun navigateToEditDistanceFragment(trip: Trip, distance: Distance) {
        if (isDualPane) {
            replaceFragment(fragmentProvider.newEditDistanceFragment(trip, distance), R.id.content_details)
        } else {
            replaceFragment(fragmentProvider.newEditDistanceFragment(trip, distance), R.id.content_list)
        }
    }

    fun navigateToCreateTripFragment(existingTrips: List<Trip>) {
        if (isDualPane) {
            replaceFragmentWithAnimation(
                fragmentProvider.newCreateTripFragment(existingTrips),
                R.id.content_details,
                R.anim.enter_from_bottom,
                DO_NOT_ANIM
            )
        } else {
            replaceFragmentWithAnimation(
                fragmentProvider.newCreateTripFragment(existingTrips),
                R.id.content_list,
                R.anim.enter_from_bottom,
                DO_NOT_ANIM
            )
        }
    }

    fun navigateToEditTripFragment(tripToEdit: Trip) {
        if (isDualPane) {
            replaceFragment(fragmentProvider.newEditTripFragment(tripToEdit), R.id.content_details)
        } else {
            replaceFragment(fragmentProvider.newEditTripFragment(tripToEdit), R.id.content_list)
        }
    }

    fun navigateToOcrConfigurationFragment() {
        if (isDualPane) {
            replaceFragment(fragmentProvider.newOcrConfigurationFragment(), R.id.content_details)
        } else {
            replaceFragment(fragmentProvider.newOcrConfigurationFragment(), R.id.content_list)
        }
    }

    fun navigateToViewReceiptImage(receipt: Receipt) {
        if (isDualPane) {
            replaceFragment(fragmentProvider.newReceiptImageFragment(receipt), R.id.content_details)
        } else {
            replaceFragment(fragmentProvider.newReceiptImageFragment(receipt), R.id.content_list)
        }
    }

    fun navigateToViewReceiptPdf(receipt: Receipt) {
        val activity = fragmentActivityWeakReference.get()
        if (activity != null && receipt.file != null) {
            try {
                val intent: Intent
                intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    debug(this, "Creating a PDF view intent with a content scheme")
                    IntentUtils.getViewIntent(activity, receipt.file, "application/pdf")
                } else {
                    debug(this, "Creating a PDF view intent with a file scheme")
                    IntentUtils.getLegacyViewIntent(activity, receipt.file, "application/pdf")
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                activity.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(activity, R.string.error_no_pdf_activity_viewer, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun navigateToBackupMenu() {
        if (isDualPane) {
            replaceFragment(fragmentProvider.newBackupsFragment(), R.id.content_details)
        } else {
            replaceFragment(fragmentProvider.newBackupsFragment(), R.id.content_list)
        }
    }

    @JvmOverloads
    fun navigateToLoginScreen(loginSourceDestination: LoginSourceDestination? = null) {
        if (isDualPane) {
            replaceFragment(fragmentProvider.newLoginFragment(loginSourceDestination), R.id.content_details)
        } else {
            replaceFragment(fragmentProvider.newLoginFragment(loginSourceDestination), R.id.content_list)
        }
    }

    fun navigateToAccountScreen() {
        if (isDualPane) {
            replaceFragment(fragmentProvider.newAccountFragment(), R.id.content_details)
        } else {
            replaceFragment(fragmentProvider.newAccountFragment(), R.id.content_list)
        }
    }

    fun navigateToSettings() {
        val activity = fragmentActivityWeakReference.get()
        if (activity != null) {
            val intent = Intent(activity, SettingsActivity::class.java)
            activity.startActivityForResult(intent, RequestCodes.SETTINGS_REQUEST)
        }
    }

    fun navigateToSettingsScrollToReportSection() {
        val activity = fragmentActivityWeakReference.get()
        if (activity != null) {
            val intent = Intent(activity, SettingsActivity::class.java)
            if (isDualPane) {
                intent.putExtra(
                    PreferenceActivity.EXTRA_SHOW_FRAGMENT,
                    PreferenceHeaderReportOutputFragment::class.java.name
                )
            } else {
                intent.putExtra(SettingsActivity.EXTRA_GO_TO_CATEGORY, R.string.pref_output_header_key)
            }
            activity.startActivityForResult(intent, RequestCodes.SETTINGS_REQUEST)
        }
    }

    fun navigateToSettingsScrollToDistanceSection() {
        navigateToSettingsHeader(PreferenceHeaderDistanceFragment::class.java, R.string.pref_distance_header_key)
    }

    fun navigateToSettingsScrollToPrivacySection() {
        navigateToSettingsHeader(PreferenceHeaderPrivacyFragment::class.java, R.string.pref_privacy_header_key)
    }

    fun navigateToSettingsScrollToReceiptSettings() {
        navigateToSettingsHeader(PreferenceHeaderReceiptsFragment::class.java, R.string.pref_receipt_header_key)
    }

    private fun navigateToSettingsHeader(headerFragment: Class<out AbstractPreferenceHeaderFragment?>, headerKey: Int) {
        val activity = fragmentActivityWeakReference.get()
        if (activity != null) {
            val intent = Intent(activity, SettingsActivity::class.java)
            if (isDualPane) {
                intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, headerFragment.name)
            } else {
                intent.putExtra(SettingsActivity.EXTRA_GO_TO_CATEGORY, headerKey)
            }
            activity.startActivityForResult(intent, RequestCodes.SETTINGS_REQUEST)
        }
    }

    fun navigateToCategoriesEditor() {
        val activity = fragmentActivityWeakReference.get()
        if (activity != null) {
            val intent = Intent(activity, SettingsViewerActivity::class.java)
            intent.putExtra(
                SettingsViewerActivity.KEY_FLAG,
                activity.getString(R.string.pref_receipt_customize_categories_key)
            )
            activity.startActivity(intent)
        }
    }

    fun navigateToPaymentMethodsEditor() {
        val activity = fragmentActivityWeakReference.get()
        if (activity != null) {
            val intent = Intent(activity, SettingsViewerActivity::class.java)
            intent.putExtra(
                SettingsViewerActivity.KEY_FLAG,
                activity.getString(R.string.pref_receipt_payment_methods_key)
            )
            activity.startActivity(intent)
        }
    }

    fun navigateBack(): Boolean {
        return try {
            fragmentManager.popBackStackImmediate()
        } catch (e: IllegalStateException) {
            // This exception is always thrown if saveInstanceState was already been called.
            false
        }
    }

    fun navigateBackDelayed(): Boolean {
        return try {
            fragmentManager.popBackStack()
            true
        } catch (e: IllegalStateException) {
            // This exception is always thrown if saveInstanceState was already been called.
            false
        }
    }

    fun showDialog(dialogFragment: DialogFragment) {
        val tag = dialogFragment.javaClass.name
        try {
            dialogFragment.show(fragmentManager, tag)
        } catch (e: IllegalStateException) {
            // This exception is always thrown if saveInstanceState was already been called.
        }
    }

    fun shouldFinishOnBackNavigation(): Boolean {
        return fragmentManager.backStackEntryCount == 1
    }

    fun navigateToCropActivity(fragment: Fragment, imageFile: File, requestCode: Int) {
        val intent = Intent(fragment.requireContext(), CropImageActivity::class.java)
        intent.putExtra(CropImageActivity.EXTRA_IMAGE_PATH, imageFile.absolutePath)
        fragment.startActivityForResult(intent, requestCode)
    }

    fun navigateToSearchActivity() {
        val activity = fragmentActivityWeakReference.get()
        if (activity != null) {
            val intent = Intent(activity, SearchActivity::class.java)
            activity.startActivityForResult(intent, RequestCodes.SEARCH_REQUEST)
        }
    }

    fun navigateToSubscriptionsActivity() {
        val activity = fragmentActivityWeakReference.get()
        if (activity != null) {
            val intent = Intent(activity, SubscriptionsActivity::class.java)
            activity.startActivityForResult(intent, RequestCodes.SUBSCRIPTIONS_REQUEST)
        }
    }

    private fun replaceFragment(fragment: Fragment, @IdRes layoutResId: Int) {
        replaceFragmentWithAnimation(fragment, layoutResId, MISSING_RES_ID, MISSING_RES_ID)
    }

    private fun replaceFragmentWithAnimation(
        fragment: Fragment,
        @IdRes layoutResId: Int,
        @AnimRes enterAnimId: Int,
        @AnimRes exitAnimId: Int
    ) {
        val tag = fragment.javaClass.name
        val wasFragmentPopped: Boolean
        wasFragmentPopped = try {
            fragmentManager.popBackStackImmediate(tag, 0)
        } catch (e: IllegalStateException) {
            // This exception is always thrown if saveInstanceState was already been called.
            false
        }
        if (!wasFragmentPopped) {
            val transaction = fragmentManager.beginTransaction()
            if (enterAnimId != MISSING_RES_ID && exitAnimId != MISSING_RES_ID) {
                transaction.setCustomAnimations(enterAnimId, exitAnimId)
            }
            try {
                transaction.replace(layoutResId, fragment, tag)
                    .addToBackStack(tag)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
            } catch (e: IllegalStateException) {
                // Note: We avoid crashes here in favor of failing. All of our core app interactions are designed to be stateful anyway
                error(this, "Failed to perform fragment transition to {}", fragment.javaClass.name)
            }
        }
    }

    companion object {
        private const val DO_NOT_ANIM = 0
        private const val MISSING_RES_ID = 0
    }
}

/**
 * List of possible "from" destinations which can be used to redirect to login
 */
enum class LoginSourceDestination {
    OCR,
    SUBSCRIPTIONS,
}