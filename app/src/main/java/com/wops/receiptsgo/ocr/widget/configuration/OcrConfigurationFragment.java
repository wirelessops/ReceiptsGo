package com.wops.receiptsgo.ocr.widget.configuration;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.android.billingclient.api.ProductDetails;
import com.jakewharton.rxbinding3.view.RxView;
import com.jakewharton.rxbinding3.widget.RxCompoundButton;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.analytics.events.Events;
import co.smartreceipts.analytics.log.Logger;
import com.wops.receiptsgo.R;
import com.wops.receiptsgo.activities.NavigationHandler;
import com.wops.receiptsgo.databinding.OcrConfigurationFragmentBinding;
import co.smartreceipts.core.identity.store.EmailAddress;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import kotlin.Unit;

public class OcrConfigurationFragment extends Fragment implements OcrConfigurationView {

    private static final String OUT_STRING_DELAYED_PURCHASE = "out_string_delayed_purchase_id";

    @Inject
    OcrConfigurationPresenter presenter;

    @Inject
    OcrConfigurationRouter router;

    @Inject
    Analytics analytics;

    @Inject
    NavigationHandler navigationHandler;

    private Button logoutButton;
    private CheckBox ocrIsEnabledCheckbox;
    private CheckBox allowUsToSaveImagesRemotelyCheckbox;
    private Button subscriptionButton;

    private OcrPurchasesListAdapter ocrPurchasesListAdapter;
    private OcrConfigurationFragmentBinding binding;

    private String delayedPurchaseId = null;
    private PublishSubject<String> delayedPurchaseIdSubject = PublishSubject.create();

    public static OcrConfigurationFragment newInstance() {
        return new OcrConfigurationFragment();
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

        if (savedInstanceState == null) {
            analytics.record(Events.Ocr.OcrViewConfigurationPage);
        } else {
            delayedPurchaseId = savedInstanceState.getString(OUT_STRING_DELAYED_PURCHASE, null);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(OUT_STRING_DELAYED_PURCHASE, delayedPurchaseId);
    }

    @SuppressLint("InflateParams")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = OcrConfigurationFragmentBinding.inflate(inflater, container, false);
        this.ocrPurchasesListAdapter = new OcrPurchasesListAdapter();
        binding.purchasesList.setAdapter(this.ocrPurchasesListAdapter);

        logoutButton = binding.logoutButton;
        ocrIsEnabledCheckbox = binding.ocrIsEnabled;
        allowUsToSaveImagesRemotelyCheckbox = binding.ocrSaveScansToImproveResults;
        subscriptionButton = binding.subscriptionsButton;

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Toolbar toolbar;
        if (navigationHandler.isDualPane()) {
            toolbar = getActivity().findViewById(R.id.toolbar);
            binding.toolbar.setVisibility(View.GONE);
        } else {
            toolbar = binding.toolbar;
        }
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
        Logger.debug(this, "onResume");
        super.onResume();
        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        presenter.subscribe();

        if (delayedPurchaseId != null) {
            delayedPurchaseIdSubject.onNext(delayedPurchaseId);
            delayedPurchaseId = null;
        }
    }

    @Override
    public void onPause() {
        Logger.debug(this, "onPause");
        presenter.unsubscribe();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        ocrPurchasesListAdapter = null;
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void present(@Nullable EmailAddress emailAddress) {
        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null && emailAddress != null) {
            actionBar.setSubtitle(getContext().getString(R.string.ocr_configuration_my_account, emailAddress));
        }
    }

    @Override
    public void present(int remainingScans, boolean isUserLoggedIn) {
        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            if (isUserLoggedIn) {
                actionBar.setTitle(getContext().getString(R.string.ocr_configuration_scans_remaining, remainingScans));
                binding.logoutButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void present(@NonNull List<ProductDetails> availablePurchases) {
        ocrPurchasesListAdapter.setAvailablePurchases(availablePurchases);
    }

    @Override
    public void delayPurchaseAndPresentNeedToLogin(@NonNull String delayedPurchaseId) {
        this.delayedPurchaseId = delayedPurchaseId;
        router.navigateToLoginScreen();
    }

    @NonNull
    @Override
    public Observable<Boolean> getOcrIsEnabledCheckboxStream() {
        return RxCompoundButton.checkedChanges(ocrIsEnabledCheckbox);
    }

    @NonNull
    @Override
    public Observable<Boolean> getAllowUsToSaveImagesRemotelyCheckboxStream() {
        return RxCompoundButton.checkedChanges(allowUsToSaveImagesRemotelyCheckbox);
    }

    @NonNull
    @Override
    public Observable<ProductDetails> getAvailablePurchaseClicks() {
        return ocrPurchasesListAdapter.getAvailablePurchaseClicks();
    }

    @NonNull
    @Override
    public Observable<Unit> getSubscriptionClicks() {
        return RxView.clicks(subscriptionButton);
    }

    @NonNull
    @Override
    public Consumer<? super Boolean> getOcrIsEnabledConsumer() {
        return isChecked -> ocrIsEnabledCheckbox.setChecked(isChecked);
    }

    @NonNull
    @Override
    public Consumer<? super Boolean> getAllowUsToSaveImagesRemotelyConsumer() {
        return isChecked -> allowUsToSaveImagesRemotelyCheckbox.setChecked(isChecked);
    }

    @NotNull
    @Override
    public Observable<String> getDelayedPurchaseIdStream() {
        return delayedPurchaseIdSubject;
    }

    @NonNull
    @Override
    public Observable<Unit> getLogoutButtonClicks() {
        return RxView.clicks(logoutButton);
    }

    @Override
    public void navigateToLoginScreen() {
        getParentFragmentManager().popBackStack();
        router.navigateToLoginScreen();
    }

    @Override
    public void navigateToSubscriptionsScreen() {
        router.navigateToSubscriptionsScreen();
    }

    @Override
    public void toggleSubscriptions(boolean isSubscriptionsEnabled) {
        binding.subscriptionsButton.setVisibility(isSubscriptionsEnabled ? View.VISIBLE : View.GONE);

        binding.purchasesListTitle.setVisibility(isSubscriptionsEnabled ? View.GONE : View.VISIBLE);
        binding.purchasesList.setVisibility(isSubscriptionsEnabled ? View.GONE : View.VISIBLE);
    }
}
