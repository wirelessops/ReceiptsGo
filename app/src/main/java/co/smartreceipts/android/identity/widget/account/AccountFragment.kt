package co.smartreceipts.android.identity.widget.account

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.Toast
import co.smartreceipts.android.R
import co.smartreceipts.android.identity.store.EmailAddress
import co.smartreceipts.android.identity.widget.NeededLoginFragment
import co.smartreceipts.android.identity.widget.NeededLoginRouter
import co.smartreceipts.android.widget.model.UiIndicator
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import kotlinx.android.synthetic.main.account_info_fragment.*
import javax.inject.Inject


class AccountFragment : NeededLoginFragment(), AccountView {

    @Inject
    lateinit var presenter: AccountPresenter

    @Inject
    lateinit var router: NeededLoginRouter


    override val logoutButtonClicks: Observable<Any>
        get() = RxView.clicks(logout_button)

    override val applySettingsButtonClicks: Observable<Any>
        get() = RxView.clicks(apply_button)

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        if (savedInstanceState != null) {
            wasPreviouslySentToLogin =
                    savedInstanceState.getBoolean(NeededLoginFragment.OUT_BOOLEAN_WAS_PREVIOUSLY_SENT_TO_LOGIN_SCREEN, false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.account_info_fragment, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val toolbar = activity!!.findViewById<Toolbar>(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
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

    override fun onResume() {
        super.onResume()

        updateProperScreen()

        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.menu_main_my_account)
            subtitle = ""
        }

        this.presenter.subscribe()
    }

    override fun onPause() {
        this.presenter.unsubscribe()
        super.onPause()
    }

    override fun present(emailAddress: EmailAddress) {
        login_field_email.text = emailAddress
    }

    override fun present(uiIndicator: UiIndicator<Boolean>) {
        Toast.makeText(context, "present", Toast.LENGTH_SHORT).show()
    }

    override fun updateProperScreen() {
        wasPreviouslySentToLogin = router.navigateToProperLocation(wasPreviouslySentToLogin)
    }

    override fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newInstance() = AccountFragment()
    }


}