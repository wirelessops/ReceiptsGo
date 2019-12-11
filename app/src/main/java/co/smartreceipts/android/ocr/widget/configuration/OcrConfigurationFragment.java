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

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.smartreceipts.android.R;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.identity.store.EmailAddress;
import co.smartreceipts.android.purchases.model.AvailablePurchase;
import co.smartreceipts.android.utils.log.Logger;
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

    @BindView(R.id.ocr_is_enabled)
    CheckBox ocrIsEnabledCheckbox;

    @BindView(R.id.ocr_save_scans_to_improve_results)
    CheckBox allowUsToSaveImagesRemotelyCheckbox;

    private OcrPurchasesListAdapter ocrPurchasesListAdapter;
    private Unbinder unbinder;

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
        final View rootView = inflater.inflate(R.layout.simple_recycler_view, container, false);
        final RecyclerView recyclerView = rootView.findViewById(android.R.id.list);
        final View headerView = inflater.inflate(R.layout.ocr_configuration_fragment, null);

        this.ocrPurchasesListAdapter = new OcrPurchasesListAdapter(headerView);
        this.unbinder = ButterKnife.bind(this, headerView);

        recyclerView.setAdapter(this.ocrPurchasesListAdapter);

        return rootView;
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
        unbinder.unbind();
        ocrPurchasesListAdapter = null;
        super.onDestroyView();
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

    @Override
    public Observable<String> getDelayedPurchaseIdStream() {
        return delayedPurchaseIdSubject;
    }
}
