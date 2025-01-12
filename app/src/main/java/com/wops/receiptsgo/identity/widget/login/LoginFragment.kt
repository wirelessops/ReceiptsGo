package com.wops.receiptsgo.identity.widget.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.wops.analytics.log.Logger.debug
import com.wops.receiptsgo.R
import com.wops.receiptsgo.activities.LoginSourceDestination
import com.wops.receiptsgo.activities.NavigationHandler
import com.wops.receiptsgo.activities.ReceiptsGoActivity
import com.wops.receiptsgo.databinding.LoginFragmentBinding
import com.wops.receiptsgo.fragments.WBFragment
import com.wops.receiptsgo.identity.widget.login.model.UiInputValidationIndicator
import com.wops.receiptsgo.utils.SoftKeyboardManager
import com.wops.receiptsgo.utils.getSerializableCompat
import com.wops.receiptsgo.widget.model.UiIndicator
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import javax.inject.Inject

class LoginFragment : WBFragment(), LoginView {

    @Inject
    lateinit var presenter: LoginPresenter

    @Inject
    lateinit var router: LoginRouter

    @Inject
    lateinit var navigationHandler: NavigationHandler<ReceiptsGoActivity>

    private var _binding: LoginFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = LoginFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginFieldEmail.requestFocus()
        SoftKeyboardManager.showKeyboard(binding.loginFieldEmail)

        // Toolbar stuff
        when {
            navigationHandler.isDualPane -> binding.toolbar.toolbar.visibility = View.GONE
            else -> setSupportActionBar(binding.toolbar.toolbar)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            router.navigateBack()
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        debug(this, "onResume")

        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setTitle(R.string.login_toolbar_title)
            subtitle = ""
        }
        presenter.subscribe()
    }

    override fun onPause() {
        debug(this, "onPause")
        presenter.unsubscribe()
        super.onPause()
    }

    override fun onDestroyView() {
        debug(this, "onDestroyView")
        SoftKeyboardManager.hideKeyboard(binding.loginFieldEmail)
        super.onDestroyView()
        _binding = null
    }

    override fun present(uiIndicator: UiIndicator<String>) {
        binding.progress.visibility =
            if (uiIndicator.state == UiIndicator.State.Loading) View.VISIBLE else View.GONE
        if (uiIndicator.state != UiIndicator.State.Idle) {
            binding.loginButton.isEnabled = uiIndicator.state != UiIndicator.State.Loading
            binding.signUpButton.isEnabled = uiIndicator.state != UiIndicator.State.Loading
        }
        if (uiIndicator.data.isPresent) {
            Toast.makeText(context, uiIndicator.data.get(), Toast.LENGTH_SHORT).show()
        }
        if (uiIndicator.state == UiIndicator.State.Success) {
            router.navigateBack()
            val loginSourceDestination =
                arguments?.getSerializableCompat<LoginSourceDestination>(LOGIN_SOURCE_DESTINATION)
            when (loginSourceDestination) {
                LoginSourceDestination.OCR -> router.navigationHandler.navigateToOcrConfigurationFragment()
                LoginSourceDestination.SUBSCRIPTIONS -> router.navigationHandler.navigateToSubscriptionsActivity()
                else -> {}
            }
        }
    }

    override fun present(uiInputValidationIndicator: UiInputValidationIndicator) {
        val enableButtons = uiInputValidationIndicator.isEmailValid && uiInputValidationIndicator.isPasswordValid
        binding.loginFieldsHint.text = uiInputValidationIndicator.message
        binding.loginButton.isEnabled = enableButtons
        binding.signUpButton.isEnabled = enableButtons
        highlightInput(
            binding.emailWrapper,
            uiInputValidationIndicator.isEmailValid || binding.loginFieldEmail.text.toString().isEmpty()
        )
        highlightInput(
            binding.passwordWrapper,
            uiInputValidationIndicator.isPasswordValid || binding.loginFieldPassword.text.toString().isEmpty()
        )
    }

    override fun getEmailTextChanges(): Observable<CharSequence> {
        return binding.loginFieldEmail.textChanges()
    }

    override fun getPasswordTextChanges(): Observable<CharSequence> {
        return binding.loginFieldPassword.textChanges()
    }

    override fun getLoginButtonClicks(): Observable<Unit> {
        return binding.loginButton.clicks()
    }

    override fun getSignUpButtonClicks(): Observable<Unit> {
        return binding.signUpButton.clicks()
    }

    private fun highlightInput(inputLayout: TextInputLayout, isValid: Boolean) {
        inputLayout.error = if (isValid) null else " "
    }

    companion object {
        const val LOGIN_SOURCE_DESTINATION = "login_source_destination"

        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }
}