package co.smartreceipts.android.di;

import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.distance.editor.DistanceDialogFragment;
import co.smartreceipts.android.distance.editor.di.DistanceDialogFragmentModule;
import co.smartreceipts.android.fragments.DistanceFragment;
import co.smartreceipts.android.fragments.GenerateReportFragment;
import co.smartreceipts.android.fragments.ReceiptImageFragment;
import co.smartreceipts.android.fragments.ReportInfoFragment;
import co.smartreceipts.android.graphs.GraphsFragment;
import co.smartreceipts.android.identity.widget.di.LoginModule;
import co.smartreceipts.android.identity.widget.login.LoginFragment;
import co.smartreceipts.android.ocr.widget.configuration.OcrConfigurationFragment;
import co.smartreceipts.android.ocr.widget.di.OcrConfigurationModule;
import co.smartreceipts.android.ocr.widget.tooltip.OcrInformationalTooltipFragment;
import co.smartreceipts.android.receipts.ReceiptsListFragment;
import co.smartreceipts.android.receipts.delete.DeleteReceiptDialogFragment;
import co.smartreceipts.android.receipts.di.ReceiptsListModule;
import co.smartreceipts.android.receipts.editor.ReceiptCreateEditFragment;
import co.smartreceipts.android.receipts.editor.di.ReceiptsCreateEditModule;
import co.smartreceipts.android.sync.widget.backups.AutomaticBackupsInfoDialogFragment;
import co.smartreceipts.android.sync.widget.backups.BackupsFragment;
import co.smartreceipts.android.sync.widget.backups.DeleteRemoteBackupDialogFragment;
import co.smartreceipts.android.sync.widget.backups.ExportBackupDialogFragment;
import co.smartreceipts.android.sync.widget.backups.ImportLocalBackupDialogFragment;
import co.smartreceipts.android.sync.widget.backups.ImportRemoteBackupDialogFragment;
import co.smartreceipts.android.sync.widget.errors.DriveRecoveryDialogFragment;
import co.smartreceipts.android.trips.TripFragment;
import co.smartreceipts.android.trips.di.TripFragmentModule;
import co.smartreceipts.android.trips.editor.TripCreateEditFragment;
import co.smartreceipts.android.trips.editor.di.TripCreateEditFragmentModule;
import co.smartreceipts.android.widget.tooltip.report.ReportTooltipFragment;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class SmartReceiptsActivityBindingModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = TripFragmentModule.class)
    public abstract TripFragment tripFragment();

    @FragmentScope
    @ContributesAndroidInjector(modules = TripCreateEditFragmentModule.class)
    public abstract TripCreateEditFragment tripCreateEditFragment();

    @FragmentScope
    @ContributesAndroidInjector(modules = ReceiptsCreateEditModule.class)
    public abstract ReceiptCreateEditFragment receiptCreateEditFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract ReceiptImageFragment receiptImageFragment();

    @FragmentScope
    @ContributesAndroidInjector(modules = ReceiptsListModule.class)
    public abstract ReceiptsListFragment receiptsListFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract DeleteReceiptDialogFragment deleteReceiptDialogFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract DistanceFragment distanceFragment();

    @FragmentScope
    @ContributesAndroidInjector(modules = DistanceDialogFragmentModule.class)
    public abstract DistanceDialogFragment distanceDialogFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract GenerateReportFragment generateReportFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract BackupsFragment backupsFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract ReportInfoFragment reportInfoFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract OcrInformationalTooltipFragment ocrInformationalTooltipFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract DeleteRemoteBackupDialogFragment deleteRemoteBackupDialogFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract AutomaticBackupsInfoDialogFragment automaticBackupsInfoDialogFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract ImportRemoteBackupDialogFragment importRemoteBackupDialogFragment();

    @FragmentScope
    @ContributesAndroidInjector(modules = ReportTooltipModule.class)
    public abstract ReportTooltipFragment reportTooltipFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract DriveRecoveryDialogFragment driveRecoveryDialogFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract ImportLocalBackupDialogFragment importLocalBackupDialogFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract ExportBackupDialogFragment exportBackupDialogFragment();

    @FragmentScope
    @ContributesAndroidInjector(modules = LoginModule.class)
    public abstract LoginFragment loginFragment();

    @FragmentScope
    @ContributesAndroidInjector(modules = OcrConfigurationModule.class)
    public abstract OcrConfigurationFragment ocrConfigurationFragment();

    @FragmentScope
    @ContributesAndroidInjector(modules = GraphsViewModule.class)
    public abstract GraphsFragment graphsFragment();

}
