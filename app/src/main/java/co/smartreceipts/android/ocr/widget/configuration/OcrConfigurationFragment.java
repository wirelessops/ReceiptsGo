package co.smartreceipts.android.ocr.widget.configuration;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.jakewharton.rxbinding3.widget.RxCompoundButton;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.analytics.Analytics;
import co.smartreceipts.analytics.events.Events;
import co.smartreceipts.android.databinding.OcrConfigurationFragmentBinding;
import co.smartreceipts.android.databinding.SimpleRecyclerViewBinding;
import co.smartreceipts.core.identity.store.EmailAddress;
import co.smartreceipts.android.purchases.model.AvailablePurchase;
import co.smartreceipts.analytics.log.Logger;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

public class OcrConfigurationFragment extends Fragment implements OcrConfigurationView {

    private static String OUT_STRING_DELAYED_PURCHASE = "out_string_delayed_purchase_id";

    @Inject
    OcrConfigurationPresenter presenter;

    @Inject
    OcrConfigurationRouter router;

    @Inject
    Analytics analytics;

    private CheckBox ocrIsEnabledCheckbox;
    private CheckBox allowUsToSaveImagesRemotelyCheckbox;

    private OcrPurchasesListAdapter ocrPurchasesListAdapter;
    private SimpleRecyclerViewBinding rootBinding;
    private OcrConfigurationFragmentBinding headerBinding;

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
        rootBinding = SimpleRecyclerViewBinding.inflate(inflater, container, false);

        final RecyclerView recyclerView = rootBinding.list;
        headerBinding = OcrConfigurationFragmentBinding.inflate(inflater, container, false);
        this.ocrPurchasesListAdapter = new OcrPurchasesListAdapter(headerBinding.getRoot());
        recyclerView.setAdapter(this.ocrPurchasesListAdapter);

        ocrIsEnabledCheckbox = headerBinding.ocrIsEnabled;
        allowUsToSaveImagesRemotelyCheckbox = headerBinding.ocrSaveScansToImproveResults;

        return rootBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
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
        rootBinding = null;
        headerBinding = null;
    }

    @Override
    public void present(@Nullable EmailAddress emailAddress) {
        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null && emailAddress != null) {
            actionBar.setSubtitle(getContext().getString(R.string.ocr_configuration_my_account, emailAddress));
        }
    }

    @Override
    public void present(int remainingScans) {
        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getContext().getString(R.string.ocr_configuration_scans_remaining, remainingScans));
        }
    }

    @Override
    public void present(@NonNull List<AvailablePurchase> availablePurchases) {
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
    public Observable<AvailablePurchase> getAvailablePurchaseClicks() {
        return ocrPurchasesListAdapter.getAvailablePurchaseClicks();
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
}
