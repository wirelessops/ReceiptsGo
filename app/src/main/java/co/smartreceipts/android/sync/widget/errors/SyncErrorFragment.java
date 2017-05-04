package co.smartreceipts.android.sync.widget.errors;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.DataPoint;
import co.smartreceipts.android.analytics.events.DefaultDataPointEvent;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.sync.BackupProviderChangeListener;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.errors.SyncErrorType;
import co.smartreceipts.android.sync.provider.SyncProvider;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.widget.Tooltip;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;


public class SyncErrorFragment extends Fragment implements BackupProviderChangeListener {

    @Inject
    Analytics analytics;
    @Inject
    BackupProvidersManager backupProvidersManager;
    @Inject
    NavigationHandler navigationHandler;

    private SyncErrorInteractor mSyncErrorInteractor;
    private SyncErrorPresenter mSyncErrorPresenter;
    private CompositeDisposable mCompositeDisposable;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSyncErrorInteractor = new SyncErrorInteractor(getActivity(), navigationHandler, backupProvidersManager, analytics);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new Tooltip(getContext());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSyncErrorPresenter = new SyncErrorPresenter((Tooltip) view);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCompositeDisposable = new CompositeDisposable();

        mCompositeDisposable.add(mSyncErrorPresenter.getClickStream()
                .subscribe(syncErrorType -> mSyncErrorInteractor.handleClick(syncErrorType)));

        backupProvidersManager.registerChangeListener(this);
        updateForProvider(backupProvidersManager.getSyncProvider());
    }

    @Override
    public void onPause() {
        backupProvidersManager.unregisterChangeListener(this);
        mCompositeDisposable.dispose();
        super.onPause();
    }

    @Override
    public void onProviderChanged(@NonNull SyncProvider newProvider) {
        updateForProvider(newProvider);
    }

    private void updateForProvider(@NonNull SyncProvider provider) {
        mSyncErrorPresenter.present(provider);
        mCompositeDisposable.add(mSyncErrorInteractor.getErrorStream()
                .doOnNext(syncErrorType -> {
                    analytics.record(new DefaultDataPointEvent(Events.Sync.DisplaySyncError).addDataPoint(new DataPoint(SyncErrorType.class.getName(), syncErrorType)));
                    Logger.info(this, "Received sync error: {}.", syncErrorType);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(syncErrorType -> mSyncErrorPresenter.present(syncErrorType)));
    }
}
