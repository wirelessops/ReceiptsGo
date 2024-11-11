package com.wops.receiptsgo.di;

import com.wops.receiptsgo.distance.editor.DistanceCreateEditFragment;
import com.wops.receiptsgo.distance.editor.di.DistanceCreateEditFragmentModule;
import com.wops.receiptsgo.fragments.DistanceFragment;
import com.wops.receiptsgo.fragments.ReceiptImageFragment;
import com.wops.receiptsgo.fragments.ReportInfoFragment;
import com.wops.receiptsgo.graphs.GraphsFragment;
import com.wops.receiptsgo.identity.widget.account.AccountFragment;
import com.wops.receiptsgo.identity.widget.di.AccountModule;
import com.wops.receiptsgo.identity.widget.di.LoginModule;
import com.wops.receiptsgo.identity.widget.login.LoginFragment;
import com.wops.receiptsgo.ocr.widget.configuration.OcrConfigurationFragment;
import com.wops.receiptsgo.ocr.widget.di.OcrConfigurationModule;
import com.wops.receiptsgo.ocr.widget.tooltip.ReceiptCreateEditFragmentTooltipFragment;
import com.wops.receiptsgo.ocr.widget.tooltip.di.ReceiptCreateEditFragmentTooltipFragmentModule;
import com.wops.receiptsgo.receipts.ReceiptsListFragment;
import com.wops.receiptsgo.receipts.delete.DeleteReceiptDialogFragment;
import com.wops.receiptsgo.receipts.di.ReceiptsListModule;
import com.wops.receiptsgo.receipts.editor.ReceiptCreateEditFragment;
import com.wops.receiptsgo.receipts.editor.di.ReceiptsCreateEditModule;
import com.wops.receiptsgo.sync.widget.backups.AutomaticBackupsInfoDialogFragment;
import com.wops.receiptsgo.sync.widget.backups.BackupsFragment;
import com.wops.receiptsgo.sync.widget.backups.DeleteRemoteBackupDialogFragment;
import com.wops.receiptsgo.sync.widget.backups.ExportBackupDialogFragment;
import com.wops.receiptsgo.sync.widget.backups.ImportLocalBackupDialogFragment;
import com.wops.receiptsgo.sync.widget.backups.ImportRemoteBackupDialogFragment;
import com.wops.receiptsgo.sync.widget.backups.RenameRemoteBackupDialogFragment;
import com.wops.receiptsgo.sync.widget.errors.DriveRecoveryDialogFragment;
import com.wops.receiptsgo.trips.TripFragment;
import com.wops.receiptsgo.trips.di.TripFragmentModule;
import com.wops.receiptsgo.trips.editor.TripCreateEditFragment;
import com.wops.receiptsgo.trips.editor.di.TripCreateEditFragmentModule;
import com.wops.receiptsgo.widget.tooltip.report.ReportTooltipFragment;
import com.wops.receiptsgo.workers.widget.GenerateReportFragment;
import com.wops.receiptsgo.workers.widget.di.GenerateReportModule;
import com.wops.core.di.scopes.FragmentScope;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ReceiptsGoActivityBindingModule {

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
    @ContributesAndroidInjector(modules = DistanceCreateEditFragmentModule.class)
    public abstract DistanceCreateEditFragment distanceCreateEditFragment();

    @FragmentScope
    @ContributesAndroidInjector(modules = GenerateReportModule.class)
    public abstract GenerateReportFragment generateReportFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract BackupsFragment backupsFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract ReportInfoFragment reportInfoFragment();

    @FragmentScope
    @ContributesAndroidInjector(modules = ReceiptCreateEditFragmentTooltipFragmentModule.class)
    public abstract ReceiptCreateEditFragmentTooltipFragment receiptCreateEditFragmentTooltipFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract RenameRemoteBackupDialogFragment renameRemoteBackupDialogFragment();

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
    @ContributesAndroidInjector(modules = AccountModule.class)
    public abstract AccountFragment accountFragment();

    @FragmentScope
    @ContributesAndroidInjector(modules = OcrConfigurationModule.class)
    public abstract OcrConfigurationFragment ocrConfigurationFragment();

    @FragmentScope
    @ContributesAndroidInjector(modules = GraphsViewModule.class)
    public abstract GraphsFragment graphsFragment();

}
