package com.wops.receiptsgo.di;

import com.wops.receiptsgo.activities.SmartReceiptsActivity;
import com.wops.receiptsgo.fragments.ReceiptMoveCopyDialogFragment;
import com.wops.receiptsgo.fragments.SelectAutomaticBackupProviderDialogFragment;
import com.wops.receiptsgo.images.CropImageActivity;
import com.wops.receiptsgo.images.di.CropModule;
import com.wops.receiptsgo.imports.intents.di.IntentImportInformationModule;
import com.wops.receiptsgo.permissions.PermissionRequesterHeadlessFragment;
import com.wops.receiptsgo.rating.FeedbackDialogFragment;
import com.wops.receiptsgo.rating.RatingDialogFragment;
import com.wops.receiptsgo.receipts.attacher.ReceiptAttachmentDialogFragment;
import com.wops.receiptsgo.receipts.attacher.ReceiptRemoveAttachmentDialogFragment;
import com.wops.receiptsgo.search.SearchActivity;
import com.wops.receiptsgo.search.SearchModule;
import com.wops.receiptsgo.settings.widget.SettingsActivity;
import com.wops.receiptsgo.settings.widget.editors.categories.CategoriesListFragment;
import com.wops.receiptsgo.settings.widget.editors.categories.CategoryEditorDialogFragment;
import com.wops.receiptsgo.settings.widget.editors.columns.CSVColumnsListFragment;
import com.wops.receiptsgo.settings.widget.editors.columns.PDFColumnsListFragment;
import com.wops.receiptsgo.settings.widget.editors.payment.PaymentMethodsListFragment;
import com.wops.receiptsgo.subscriptions.SubscriptionsActivity;
import com.wops.receiptsgo.subscriptions.di.SubscriptionsModule;
import com.wops.receiptsgo.sync.widget.backups.DeleteRemoteBackupProgressDialogFragment;
import com.wops.receiptsgo.sync.widget.backups.DownloadRemoteBackupImagesProgressDialogFragment;
import com.wops.receiptsgo.sync.widget.backups.ExportBackupWorkerProgressDialogFragment;
import com.wops.receiptsgo.sync.widget.backups.ImportLocalBackupWorkerProgressDialogFragment;
import com.wops.receiptsgo.sync.widget.backups.ImportRemoteBackupWorkerProgressDialogFragment;
import com.wops.receiptsgo.sync.widget.backups.RenameRemoteBackupProgressDialogFragment;
import co.smartreceipts.core.di.scopes.ActivityScope;
import co.smartreceipts.core.di.scopes.FragmentScope;
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

    @ActivityScope
    @ContributesAndroidInjector(modules = CropModule.class)
    public abstract CropImageActivity cropImageActivity();

    @ActivityScope
    @ContributesAndroidInjector(modules = SearchModule.class)
    public abstract SearchActivity searchActivity();

    @ActivityScope
    @ContributesAndroidInjector(modules = SubscriptionsModule.class)
    public abstract SubscriptionsActivity subscriptionsActivity();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract CSVColumnsListFragment csvColumnsListFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract PDFColumnsListFragment pdfColumnsListFragment();

    @FragmentScope
    @ContributesAndroidInjector
    public abstract RenameRemoteBackupProgressDialogFragment renameRemoteBackupProgressDialogFragment();

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
    public abstract CategoryEditorDialogFragment categoryEditorDialogFragment();

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
    public abstract PermissionRequesterHeadlessFragment permissionRequesterHeadlessFragment();

}
