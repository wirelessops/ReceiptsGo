package co.smartreceipts.android.di;

import android.support.v4.app.Fragment;

import co.smartreceipts.android.di.subcomponents.AutomaticBackupsInfoDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.BackupsFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.DeleteRemoteBackupDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.DriveRecoveryDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.ExportBackupDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.GenerateReportFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.GraphsFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.ImportLocalBackupDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.ImportRemoteBackupDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.ReceiptCreateEditFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.ReceiptImageFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.ReceiptsListFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.ReportInfoFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.ReportTooltipFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.TripCreateEditFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.TripFragmentSubcomponent;
import co.smartreceipts.android.fragments.GenerateReportFragment;
import co.smartreceipts.android.fragments.ReceiptImageFragment;
import co.smartreceipts.android.fragments.ReportInfoFragment;
import co.smartreceipts.android.graphs.GraphsFragment;
import co.smartreceipts.android.identity.widget.di.LoginFragmentSubcomponent;
import co.smartreceipts.android.identity.widget.login.LoginFragment;
import co.smartreceipts.android.ocr.widget.configuration.OcrConfigurationFragment;
import co.smartreceipts.android.ocr.widget.di.OcrConfigurationFragmentSubcomponent;
import co.smartreceipts.android.ocr.widget.di.OcrInformationalTooltipFragmentSubcomponent;
import co.smartreceipts.android.ocr.widget.tooltip.OcrInformationalTooltipFragment;
import co.smartreceipts.android.receipts.ReceiptsListFragment;
import co.smartreceipts.android.receipts.editor.ReceiptCreateEditFragment;
import co.smartreceipts.android.sync.widget.backups.AutomaticBackupsInfoDialogFragment;
import co.smartreceipts.android.sync.widget.backups.BackupsFragment;
import co.smartreceipts.android.sync.widget.backups.DeleteRemoteBackupDialogFragment;
import co.smartreceipts.android.sync.widget.backups.ExportBackupDialogFragment;
import co.smartreceipts.android.sync.widget.backups.ImportLocalBackupDialogFragment;
import co.smartreceipts.android.sync.widget.backups.ImportRemoteBackupDialogFragment;
import co.smartreceipts.android.sync.widget.errors.DriveRecoveryDialogFragment;
import co.smartreceipts.android.trips.TripFragment;
import co.smartreceipts.android.trips.editor.TripCreateEditFragment;
import co.smartreceipts.android.widget.tooltip.report.ReportTooltipFragment;
import dagger.Binds;
import dagger.Module;
import dagger.android.AndroidInjector;
import dagger.android.support.FragmentKey;
import dagger.multibindings.IntoMap;

@Module(
        subcomponents = {
                TripFragmentSubcomponent.class,
                TripCreateEditFragmentSubcomponent.class,
                ReceiptCreateEditFragmentSubcomponent.class,
                ReceiptImageFragmentSubcomponent.class,
                ReceiptsListFragmentSubcomponent.class,
                GenerateReportFragmentSubcomponent.class,
                BackupsFragmentSubcomponent.class,
                ReportInfoFragmentSubcomponent.class,
                OcrInformationalTooltipFragmentSubcomponent.class,
                DeleteRemoteBackupDialogFragmentSubcomponent.class,
                AutomaticBackupsInfoDialogFragmentSubcomponent.class,
                ImportRemoteBackupDialogFragmentSubcomponent.class,
                ReportTooltipFragmentSubcomponent.class,
                DriveRecoveryDialogFragmentSubcomponent.class,
                ImportLocalBackupDialogFragmentSubcomponent.class,
                ExportBackupDialogFragmentSubcomponent.class,
                LoginFragmentSubcomponent.class,
                OcrConfigurationFragmentSubcomponent.class,
                GraphsFragmentSubcomponent.class
        }
)
public abstract class SmartReceiptsActivityBindingModule {
    @Binds
    @IntoMap
    @FragmentKey(TripFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> tripFragmentSubcomponentBuilder(
            TripFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(TripCreateEditFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> tripCreateEditFragmentSubcomponentBuilder(
            TripCreateEditFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(ReceiptCreateEditFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> receiptCreateEditFragmentSubcomponentBuilder(
            ReceiptCreateEditFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(ReceiptImageFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> receiptImageFragmentSubcomponentBuilder(
            ReceiptImageFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(ReceiptsListFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> receiptsListFragmentSubcomponentBuilder(
            ReceiptsListFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(GenerateReportFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> generateReportFragmentSubcomponentBuilder(
            GenerateReportFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(BackupsFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> backupsFragmentBuilder(
            BackupsFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(ReportInfoFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> reportInfoFragmentBuilder(
            ReportInfoFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(OcrInformationalTooltipFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> ocrInformationalTooltipFragmentBuilder(
            OcrInformationalTooltipFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(DeleteRemoteBackupDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> deleteRemoteBackupDialogFragmentBuilder(
            DeleteRemoteBackupDialogFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(AutomaticBackupsInfoDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> automaticBackupsInfoDialogFragmentBuilder(
            AutomaticBackupsInfoDialogFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(ImportRemoteBackupDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> importRemoteBackupDialogFragmentBuilder(
            ImportRemoteBackupDialogFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(ReportTooltipFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> syncErrorFragmentBuilder(
            ReportTooltipFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(DriveRecoveryDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> driveRecoveryDialogFragmentBuilder(
            DriveRecoveryDialogFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(ImportLocalBackupDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> importLocalBackupDialogFragmentBuilder(
            ImportLocalBackupDialogFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(ExportBackupDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> exportBackupDialogFragmentBuilder(
            ExportBackupDialogFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(LoginFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> loginFragmentBuilder(
            LoginFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(OcrConfigurationFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> ocrInformationalFragmentBuilder(
            OcrConfigurationFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(GraphsFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> graphsFragmentBuilder(
            GraphsFragmentSubcomponent.Builder builder);

}
