package co.smartreceipts.android.di;

import co.smartreceipts.android.activities.SmartReceiptsActivity;
import co.smartreceipts.android.di.scopes.ActivityScope;
import co.smartreceipts.android.di.scopes.FragmentScope;
import co.smartreceipts.android.di.scopes.ServiceScope;
import co.smartreceipts.android.fragments.ImportPhotoPdfDialogFragment;
import co.smartreceipts.android.fragments.ReceiptMoveCopyDialogFragment;
import co.smartreceipts.android.fragments.SelectAutomaticBackupProviderDialogFragment;
import co.smartreceipts.android.imports.intents.di.IntentImportInformationModule;
import co.smartreceipts.android.permissions.PermissionRequesterHeadlessFragment;
import co.smartreceipts.android.rating.FeedbackDialogFragment;
import co.smartreceipts.android.rating.RatingDialogFragment;
import co.smartreceipts.android.receipts.attacher.ReceiptAttachmentDialogFragment;
import co.smartreceipts.android.receipts.attacher.ReceiptRemoveAttachmentDialogFragment;
import co.smartreceipts.android.settings.widget.SettingsActivity;
import co.smartreceipts.android.settings.widget.editors.categories.CategoriesListFragment;
import co.smartreceipts.android.settings.widget.editors.columns.CSVColumnsListFragment;
import co.smartreceipts.android.settings.widget.editors.columns.PDFColumnsListFragment;
import co.smartreceipts.android.settings.widget.editors.payment.PaymentMethodsListFragment;
import co.smartreceipts.android.sync.drive.services.DriveCompletionEventService;
import co.smartreceipts.android.sync.widget.backups.DeleteRemoteBackupProgressDialogFragment;
import co.smartreceipts.android.sync.widget.backups.DownloadRemoteBackupImagesProgressDialogFragment;
import co.smartreceipts.android.sync.widget.backups.ExportBackupWorkerProgressDialogFragment;
import co.smartreceipts.android.sync.widget.backups.ImportLocalBackupWorkerProgressDialogFragment;
import co.smartreceipts.android.sync.widget.backups.ImportRemoteBackupWorkerProgressDialogFragment;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class GlobalBindingModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = {
            SmartReceiptsActivityModule.class,
            SmartReceiptsActivityBindingModule.class,
            IntentImportInformationModule.class,
            SmartReceiptsActivityAdModule.class
    })
    public abstract SmartReceiptsActivity smartReceiptsActivity();

    @ActivityScope
    @ContributesAndroidInjector
    public abstract SettingsActivity settingsActivity();

    @ServiceScope
    @ContributesAndroidInjector
    public abstract DriveCompletionEventService driveCompletionEventService();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract CSVColumnsListFragment csvColumnsListFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract PDFColumnsListFragment pdfColumnsListFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract DeleteRemoteBackupProgressDialogFragment deleteRemoteBackupProgressDialogFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract DownloadRemoteBackupImagesProgressDialogFragment downloadRemoteBackupImagesProgressDialogFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract ExportBackupWorkerProgressDialogFragment exportBackupWorkerProgressDialogFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract ImportLocalBackupWorkerProgressDialogFragment importLocalBackupWorkerProgressDialogFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract ImportRemoteBackupWorkerProgressDialogFragment importRemoteBackupWorkerProgressDialogFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract FeedbackDialogFragment feedbackDialogFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract RatingDialogFragment ratingDialogFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract PaymentMethodsListFragment paymentMethodsListFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract CategoriesListFragment categoriesListFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract ReceiptMoveCopyDialogFragment receiptMoveCopyDialogFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract SelectAutomaticBackupProviderDialogFragment selectAutomaticBackupProviderDialogFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract ReceiptAttachmentDialogFragment receiptAttachmentDialogFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract ReceiptRemoveAttachmentDialogFragment receiptRemoveAttachmentDialogFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract ImportPhotoPdfDialogFragment importPhotoPdfDialogFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract PermissionRequesterHeadlessFragment permissionRequesterHeadlessFragment();

}
