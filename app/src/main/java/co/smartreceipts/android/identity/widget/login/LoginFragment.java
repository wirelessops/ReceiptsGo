package co.smartreceipts.android.identity.widget.login;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.smartreceipts.android.R;
import co.smartreceipts.android.identity.widget.login.model.UiInputValidationIndicator;
import co.smartreceipts.android.utils.SoftKeyboardManager;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.widget.model.UiIndicator;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.Observable;


public class LoginFragment extends Fragment implements LoginView {

    @Inject
    LoginPresenter presenter;

    @Inject
    LoginRouter router;

    @BindView(R.id.login_fields_hint)
    TextView loginFieldsHintMessage;

    @BindView(R.id.login_field_email)
    EditText emailInput;

    @BindView(R.id.login_field_password)
    EditText passwordInput;

    @BindView(R.id.progress)
    ProgressBar progress;

    @BindView(R.id.login_button)
    Button loginButton;

    @BindView(R.id.sign_up_button)
    Button signUpButton;

    private Unbinder unbinder;

    @NonNull
    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.unbinder = ButterKnife.bind(this, view);
        this.emailInput.requestFocus();
        SoftKeyboardManager.showKeyboard(this.emailInput);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            return router.navigateBack();
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.debug(this, "onResume");

        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.login_toolbar_title);
            actionBar.setSubtitle("");
        }

        this.presenter.subscribe();
    }

    @Override
    public void onPause() {
        Logger.debug(this, "onPause");
        this.presenter.unsubscribe();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        Logger.debug(this, "onDestroyView");
        SoftKeyboardManager.hideKeyboard(emailInput);
        this.unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void present(@NonNull UiIndicator uiIndicator) {
        progress.setVisibility(uiIndicator.getState() == UiIndicator.State.Loading ? View.VISIBLE : View.GONE);
        if (uiIndicator.getState() != UiIndicator.State.Idle) {
            loginButton.setEnabled(uiIndicator.getState() != UiIndicator.State.Loading);
            signUpButton.setEnabled(uiIndicator.getState() != UiIndicator.State.Loading);
        }
        if (uiIndicator.getMessage().isPresent()) {
            Toast.makeText(getContext(), uiIndicator.getMessage().get(), Toast.LENGTH_SHORT).show();
        }
        if (uiIndicator.getState() == UiIndicator.State.Success) {
            router.navigateBack();
        }
    }

    @Override
    public void present(@NonNull UiInputValidationIndicator uiInputValidationIndicator) {
        final boolean enableButtons = uiInputValidationIndicator.isEmailValid() && uiInputValidationIndicator.isPasswordValid();
        loginFieldsHintMessage.setText(uiInputValidationIndicator.getMessage());
        loginButton.setEnabled(enableButtons);
        signUpButton.setEnabled(enableButtons);
        highlightInput(emailInput, uiInputValidationIndicator.isEmailValid());
        highlightInput(passwordInput, uiInputValidationIndicator.isPasswordValid());
    }

    @NonNull
    @Override
    public Observable<CharSequence> getEmailTextChanges() {
        return RxTextView.textChanges(emailInput);
    }

    @NonNull
    @Override
    public Observable<CharSequence> getPasswordTextChanges() {
        return RxTextView.textChanges(passwordInput);
    }

    @NonNull
    @Override
    public Observable<Object> getLoginButtonClicks() {
        return RxView.clicks(loginButton);
    }

    @NonNull
    @Override
    public Observable<Object> getSignUpButtonClicks() {
        return RxView.clicks(signUpButton);
    }

    private void highlightInput(@NonNull EditText editText, boolean isValid) {
        final int color;
        if (isValid) {
            color = ResourcesCompat.getColor(editText.getResources(), R.color.smart_receipts_colorSuccess, editText.getContext().getTheme());
        } else {
            color = ResourcesCompat.getColor(editText.getResources(), R.color.smart_receipts_colorAccent, editText.getContext().getTheme());
        }
        editText.getBackground().mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

}