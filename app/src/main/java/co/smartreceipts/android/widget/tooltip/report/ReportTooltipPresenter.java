package co.smartreceipts.android.widget.tooltip.report;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import javax.inject.Inject;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.sync.BackupProviderChangeListener;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.provider.SyncProvider;
import co.smartreceipts.android.widget.tooltip.TooltipView;
import co.smartreceipts.android.widget.viper.BasePresenter;

@FragmentScope
public class ReportTooltipPresenter extends BasePresenter<TooltipView, ReportTooltipInteractor<? extends FragmentActivity>> implements BackupProviderChangeListener {

    private final BackupProvidersManager backupProvidersManager;

    @Inject
    public ReportTooltipPresenter(@NonNull TooltipView view, @NonNull ReportTooltipInteractor interactor,
                                  @NonNull BackupProvidersManager backupProvidersManager) {
        super(view, interactor);

        this.backupProvidersManager = backupProvidersManager;
    }

    @Override
    public void subscribe() {
        backupProvidersManager.registerChangeListener(this);
        updateProvider(backupProvidersManager.getSyncProvider());

        compositeDisposable.add(interactor.checkTooltipCauses()
                .subscribe(view::present));

        compositeDisposable.add(view.getTooltipsClicks()
                .subscribe(uiIndicator -> {
                    view.present(ReportTooltipUiIndicator.none());
                    if (uiIndicator.getState() == ReportTooltipUiIndicator.State.SyncError) {
                        interactor.handleClickOnErrorTooltip(uiIndicator.getErrorType().get());
                    } else if (uiIndicator.getState() == ReportTooltipUiIndicator.State.GenerateInfo) {
                        interactor.generateInfoTooltipClosed();
                    }
                }));

        compositeDisposable.add(view.getCloseButtonClicks()
                .subscribe(uiIndicator -> {
                    view.present(ReportTooltipUiIndicator.none());
                    if (uiIndicator.getState() == ReportTooltipUiIndicator.State.GenerateInfo) {
                        interactor.generateInfoTooltipClosed();
                    }
                }));
    }

    @Override
    public void unsubscribe() {
        super.unsubscribe();

        backupProvidersManager.unregisterChangeListener(this);
    }

    @Override
    public void onProviderChanged(@NonNull SyncProvider newProvider) {
        updateProvider(newProvider);
    }

    private void updateProvider(@NonNull SyncProvider provider) {
        if (provider == SyncProvider.None) {
            view.present(ReportTooltipUiIndicator.none());
        }
    }
}
