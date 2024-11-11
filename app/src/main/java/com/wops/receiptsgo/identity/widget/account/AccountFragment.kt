package com.wops.receiptsgo.identity.widget.account

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.wops.receiptsgo.R
import com.wops.receiptsgo.activities.NavigationHandler
import com.wops.receiptsgo.activities.ReceiptsGoActivity
import com.wops.receiptsgo.databinding.AccountInfoFragmentBinding
import com.wops.receiptsgo.date.DateFormatter
import com.wops.receiptsgo.identity.apis.organizations.OrganizationModel
import com.wops.receiptsgo.identity.widget.account.organizations.OrganizationsListAdapter
import com.wops.receiptsgo.identity.widget.account.subscriptions.SubscriptionsListAdapter
import com.wops.receiptsgo.purchases.subscriptions.RemoteSubscription
import com.wops.receiptsgo.widget.model.UiIndicator
import com.wops.core.identity.store.EmailAddress
import com.jakewharton.rxbinding3.view.clicks
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import javax.inject.Inject


class AccountFragment : Fragment(), AccountView {

    @Inject
    lateinit var presenter: AccountPresenter

    @Inject
    lateinit var router: AccountRouter

    @Inject
    lateinit var dateFormatter: DateFormatter

    @Inject
    lateinit var navigationHandler: NavigationHandler<ReceiptsGoActivity>

    private var wasPreviouslySentToLogin: Boolean = false

    private lateinit var subscriptionsAdapter: SubscriptionsListAdapter

    private lateinit var organizationsAdapter: OrganizationsListAdapter

    override lateinit var applySettingsClicks: Observable<OrganizationModel>
    override lateinit var uploadSettingsClicks: Observable<OrganizationModel>


    override val logoutButtonClicks: Observable<Unit> get() = binding.logoutButton.clicks()

    private var _binding: AccountInfoFragmentBinding? = null
    private val binding get() = _binding!!


    override fun onAttach(context: Context) {
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
        _binding = AccountInfoFragmentBinding.inflate(inflater, container, false)

        subscriptionsAdapter = SubscriptionsListAdapter(dateFormatter)
        binding.subscriptionsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = subscriptionsAdapter
        }

        organizationsAdapter = OrganizationsListAdapter()
        binding.organizationsList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = organizationsAdapter
        }

        applySettingsClicks = organizationsAdapter.getApplySettingsStream()
        uploadSettingsClicks = organizationsAdapter.getUploadSettingsStream()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val toolbar: Toolbar
        if (navigationHandler.isDualPane) {
            toolbar = requireActivity().findViewById(R.id.toolbar)
            binding.toolbar.toolbar.visibility = View.GONE
        } else {
            toolbar = binding.toolbar.toolbar
        }
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
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
        binding.loginFieldEmail.text = emailAddress
    }

    override fun presentOrganizations(uiIndicator: UiIndicator<List<OrganizationModel>>) {
        when (uiIndicator.state) {
            UiIndicator.State.Success -> {
                binding.progressBar.visibility = View.GONE

                binding.organizationGroup.visibility = View.VISIBLE
                organizationsAdapter.setOrganizations(uiIndicator.data.get())
            }
            UiIndicator.State.Loading -> {
                binding.organizationGroup.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
            }
            UiIndicator.State.Error -> {
                binding.organizationGroup.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
            }
            UiIndicator.State.Idle -> {
                binding.organizationGroup.visibility = View.GONE
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun presentApplyingResult(uiIndicator: UiIndicator<Unit>) {
        when (uiIndicator.state) {
            UiIndicator.State.Success -> {
                Toast.makeText(context, getString(R.string.organization_apply_success), Toast.LENGTH_SHORT).show()
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
                binding.progressBar.visibility = View.VISIBLE
            }
            UiIndicator.State.Success -> {
                Toast.makeText(context, getString(R.string.organization_update_success), Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
            UiIndicator.State.Error -> {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, getString(R.string.organization_update_error), Toast.LENGTH_SHORT).show()
            }
            else -> {
                throw IllegalStateException("Updating organization settings must return only Success or Error result")
            }
        }
    }

    override fun presentOcrScans(remainingScans: Int) {
        binding.ocrScansRemaining.text = getString(R.string.ocr_configuration_scans_remaining, remainingScans)

        val listener: View.OnClickListener = View.OnClickListener { router.navigateToOcrFragment() }
        binding.ocrScansRemaining.setOnClickListener(listener)
        binding.ocrLabel.setOnClickListener(listener)
    }

    override fun presentSubscriptions(subscriptions: List<RemoteSubscription>) {
        binding.subscriptionsGroup.visibility = View.VISIBLE

        subscriptionsAdapter.setSubscriptions(subscriptions)
    }


    companion object {
        @JvmStatic
        fun newInstance() = AccountFragment()

        const val OUT_BOOLEAN_WAS_PREVIOUSLY_SENT_TO_LOGIN_SCREEN = "out_bool_was_previously_sent_to_login_screen"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}