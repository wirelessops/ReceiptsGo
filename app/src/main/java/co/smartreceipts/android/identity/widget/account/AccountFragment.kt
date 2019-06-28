package co.smartreceipts.android.identity.widget.account

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.widget.Toolbar
import android.view.*
import android.widget.Toast
import co.smartreceipts.android.R
import co.smartreceipts.android.date.DateFormatter
import co.smartreceipts.android.identity.apis.organizations.OrganizationUser
import co.smartreceipts.android.identity.store.EmailAddress
import co.smartreceipts.android.identity.widget.account.subscriptions.SubscriptionsListAdapter
import co.smartreceipts.android.purchases.subscriptions.RemoteSubscription
import co.smartreceipts.android.widget.model.UiIndicator
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import kotlinx.android.synthetic.main.account_info_fragment.*
import kotlinx.android.synthetic.main.account_info_fragment.view.*
import javax.inject.Inject


class AccountFragment : Fragment(), AccountView {

    @Inject
    lateinit var presenter: AccountPresenter

    @Inject
    lateinit var router: AccountRouter

    @Inject
    lateinit var dateFormatter: DateFormatter

    private var wasPreviouslySentToLogin: Boolean = false

    private lateinit var subscriptionsAdapter: SubscriptionsListAdapter


    override val logoutButtonClicks: Observable<Any> get() = RxView.clicks(logout_button)
    override val applySettingsClicks: Observable<Any> get() = RxView.clicks(organization_caution)
    override val updateSettingsClicks: Observable<Any> get() = RxView.clicks(organization_update_button)


    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        if (savedInstanceState != null) {
            wasPreviouslySentToLogin = savedInstanceState.getBoolean(OUT_BOOLEAN_WAS_PREVIOUSLY_SENT_TO_LOGIN_SCREEN, false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.account_info_fragment, container, false)

        subscriptionsAdapter = SubscriptionsListAdapter(dateFormatter)

        view.subscriptions_list.apply {
            layoutManager = LinearLayoutManager(getContext());
            adapter = subscriptionsAdapter
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val fragmentActivity = requireActivity()
        val toolbar = fragmentActivity.findViewById<Toolbar>(R.id.toolbar)
        (fragmentActivity as AppCompatActivity).setSupportActionBar(toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item?.itemId == android.R.id.home) {
            router.navigateBack()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onStart() {
        super.onStart()

        updateProperScreen()

        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.menu_main_my_account)
            subtitle = ""
        }

        this.presenter.subscribe()
    }

    override fun onStop() {
        this.presenter.unsubscribe()

        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(OUT_BOOLEAN_WAS_PREVIOUSLY_SENT_TO_LOGIN_SCREEN, wasPreviouslySentToLogin)
    }

    override fun updateProperScreen() {
        wasPreviouslySentToLogin = router.navigateToProperLocation(wasPreviouslySentToLogin)
    }

    override fun presentEmail(emailAddress: EmailAddress) {
        login_field_email.text = emailAddress
    }

    override fun presentOrganization(uiIndicator: UiIndicator<AccountInteractor.OrganizationModel>) {
        when (uiIndicator.state) {
            UiIndicator.State.Success -> {
                progress_bar.visibility = View.GONE
                showOrganization(uiIndicator.data.get())
            }
            UiIndicator.State.Loading -> {
                progress_bar.visibility = View.VISIBLE
            }
            UiIndicator.State.Error -> {
                organization_group.visibility = View.GONE
                progress_bar.visibility = View.GONE
            }
            UiIndicator.State.Idle -> {
                organization_group.visibility = View.GONE
                progress_bar.visibility = View.GONE
            }
        }
    }

    override fun presentApplyingResult(uiIndicator: UiIndicator<Unit>) {
        when (uiIndicator.state) {
            UiIndicator.State.Success -> {
                Toast.makeText(context, getString(R.string.organization_apply_success), Toast.LENGTH_SHORT).show()
                organization_caution.visibility = View.GONE
                organization_update_button.visibility = View.GONE
            }
            UiIndicator.State.Error -> {
                Toast.makeText(context, getString(R.string.organization_apply_error), Toast.LENGTH_SHORT).show()
            }
            else -> {
                throw IllegalStateException("Applying settings must return only Success or Error result")
            }
        }
    }

    override fun presentUpdatingResult(uiIndicator: UiIndicator<Unit>) {
        when (uiIndicator.state) {
            UiIndicator.State.Loading -> {
                progress_bar.visibility = View.VISIBLE
            }
            UiIndicator.State.Success -> {
                Toast.makeText(context, getString(R.string.organization_update_success), Toast.LENGTH_SHORT).show()
                progress_bar.visibility = View.GONE
                organization_update_button.visibility = View.GONE
                organization_caution.visibility = View.GONE
            }
            UiIndicator.State.Error -> {
                progress_bar.visibility = View.GONE
                Toast.makeText(context, getString(R.string.organization_update_error), Toast.LENGTH_SHORT).show()
            }
            else -> {
                throw IllegalStateException("Updating organization settings must return only Success or Error result")
            }
        }
    }

    override fun presentOcrScans(remainingScans: Int) {
        ocr_scans_remaining.text = getString(R.string.ocr_configuration_scans_remaining, remainingScans)

        val listener: View.OnClickListener = View.OnClickListener { router.navigateToOcrFragment() }
        ocr_scans_remaining.setOnClickListener(listener)
        ocr_label.setOnClickListener(listener)
    }

    override fun presentSubscriptions(subscriptions: List<RemoteSubscription>) {
        subscriptions_group.visibility = View.VISIBLE

        subscriptionsAdapter.setSubscriptions(subscriptions)
    }

    private fun showOrganization(organizationModel: AccountInteractor.OrganizationModel) {
        organization_group.visibility = View.VISIBLE

        organization_name.text = organizationModel.organization.name
        user_role.text = organizationModel.userRole.name

        when {
            organizationModel.settingsMatch -> organization_caution.visibility = View.GONE
            else -> {
                organization_caution.visibility = View.VISIBLE
                if (organizationModel.userRole == OrganizationUser.UserRole.ADMIN) {
                    organization_update_button.visibility = View.VISIBLE
                }
            }
        }
    }

    companion object {
        @JvmStatic fun newInstance() = AccountFragment()

        const val OUT_BOOLEAN_WAS_PREVIOUSLY_SENT_TO_LOGIN_SCREEN = "out_bool_was_previously_sent_to_login_screen"
    }

}