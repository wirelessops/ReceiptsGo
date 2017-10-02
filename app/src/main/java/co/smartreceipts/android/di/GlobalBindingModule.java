package co.smartreceipts.android.di;

import android.app.Activity;
import android.app.Service;
import android.support.v4.app.Fragment;

import co.smartreceipts.android.activities.SmartReceiptsActivity;
import co.smartreceipts.android.activities.di.SmartReceiptsActivitySubcomponent;
import co.smartreceipts.android.di.subcomponents.CSVColumnsListFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.CategoriesListFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.DeleteRemoteBackupProgressDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.DistanceDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.DistanceFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.DownloadRemoteBackupImagesProgressDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.ExportBackupWorkerProgressDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.FeedbackDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.ImportLocalBackupWorkerProgressDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.ImportRemoteBackupWorkerProgressDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.InformAboutPdfImageAttachmentDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.PDFColumnsListFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.PaymentMethodsListFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.RatingDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.ReceiptMoveCopyDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.SelectAutomaticBackupProviderDialogFragmentSubcomponent;
import co.smartreceipts.android.di.subcomponents.SettingsActivitySubcomponent;
import co.smartreceipts.android.distance.editor.DistanceDialogFragment;
import co.smartreceipts.android.fragments.DistanceFragment;
import co.smartreceipts.android.fragments.InformAboutPdfImageAttachmentDialogFragment;
import co.smartreceipts.android.fragments.ReceiptMoveCopyDialogFragment;
import co.smartreceipts.android.fragments.SelectAutomaticBackupProviderDialogFragment;
import co.smartreceipts.android.ocr.widget.di.OcrConfigurationFragmentSubcomponent;
import co.smartreceipts.android.rating.FeedbackDialogFragment;
import co.smartreceipts.android.rating.RatingDialogFragment;
import co.smartreceipts.android.settings.widget.editors.CSVColumnsListFragment;
import co.smartreceipts.android.settings.widget.editors.categories.CategoriesListFragment;
import co.smartreceipts.android.settings.widget.editors.PDFColumnsListFragment;
import co.smartreceipts.android.settings.widget.editors.payment.PaymentMethodsListFragment;
import co.smartreceipts.android.settings.widget.SettingsActivity;
import co.smartreceipts.android.sync.drive.services.DriveCompletionEventService;
import co.smartreceipts.android.sync.drive.services.di.DriveCompletionEventServiceSubcomponent;
import co.smartreceipts.android.sync.widget.backups.DeleteRemoteBackupProgressDialogFragment;
import co.smartreceipts.android.sync.widget.backups.DownloadRemoteBackupImagesProgressDialogFragment;
import co.smartreceipts.android.sync.widget.backups.ExportBackupWorkerProgressDialogFragment;
import co.smartreceipts.android.sync.widget.backups.ImportLocalBackupWorkerProgressDialogFragment;
import co.smartreceipts.android.sync.widget.backups.ImportRemoteBackupWorkerProgressDialogFragment;
import dagger.Binds;
import dagger.Module;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.android.ServiceKey;
import dagger.android.support.FragmentKey;
import dagger.multibindings.IntoMap;

@Module(
        subcomponents = {
                SmartReceiptsActivitySubcomponent.class,
                SettingsActivitySubcomponent.class,
                DriveCompletionEventServiceSubcomponent.class,
                CSVColumnsListFragmentSubcomponent.class,
                PDFColumnsListFragmentSubcomponent.class,
                DistanceFragmentSubcomponent.class,
                DistanceDialogFragmentSubcomponent.class,
                InformAboutPdfImageAttachmentDialogFragmentSubcomponent.class,
                DeleteRemoteBackupProgressDialogFragmentSubcomponent.class,
                DownloadRemoteBackupImagesProgressDialogFragmentSubcomponent.class,
                ExportBackupWorkerProgressDialogFragmentSubcomponent.class,
                ImportLocalBackupWorkerProgressDialogFragmentSubcomponent.class,
                ImportRemoteBackupWorkerProgressDialogFragmentSubcomponent.class,
                FeedbackDialogFragmentSubcomponent.class,
                OcrConfigurationFragmentSubcomponent.class,
                RatingDialogFragmentSubcomponent.class,
                PaymentMethodsListFragmentSubcomponent.class,
                CategoriesListFragmentSubcomponent.class,
                ReceiptMoveCopyDialogFragmentSubcomponent.class,
                SelectAutomaticBackupProviderDialogFragmentSubcomponent.class,
        }
)
public abstract class GlobalBindingModule {
    @Binds
    @IntoMap
    @ActivityKey(SmartReceiptsActivity.class)
    public abstract AndroidInjector.Factory<? extends Activity> smartReceiptsActivitySubcomponentBuilder(
            SmartReceiptsActivitySubcomponent.Builder builder);

    @Binds
    @IntoMap
    @ActivityKey(SettingsActivity.class)
    public abstract AndroidInjector.Factory<? extends Activity> settingsActivitySubcomponentBuilder(
            SettingsActivitySubcomponent.Builder builder);

    @Binds
    @IntoMap
    @ServiceKey(DriveCompletionEventService.class)
    public abstract AndroidInjector.Factory<? extends Service> driveCompletionEventServiceSubcomponentBuilder(
            DriveCompletionEventServiceSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(CSVColumnsListFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> csvColumnListFragmentSubcomponentBuilder(
            CSVColumnsListFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(PDFColumnsListFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> pdfColumnListFragmentSubcomponentBuilder(
            PDFColumnsListFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(DistanceFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> distanceFragmentBuilder(
            DistanceFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(DistanceDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> distanceDialogFragmentBuilder(
            DistanceDialogFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(InformAboutPdfImageAttachmentDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> informDialogBuilder(
            InformAboutPdfImageAttachmentDialogFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(DeleteRemoteBackupProgressDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> deleteRemoteBackupProgressFragmentBuilder(
            DeleteRemoteBackupProgressDialogFragmentSubcomponent.Builder builder);


    @Binds
    @IntoMap
    @FragmentKey(DownloadRemoteBackupImagesProgressDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> downloadRemoteBackupImagesProgressFragmentBuilder(
            DownloadRemoteBackupImagesProgressDialogFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(ExportBackupWorkerProgressDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> exportBackupWorkerProgressDialogFragmentBuilder(
            ExportBackupWorkerProgressDialogFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(ImportLocalBackupWorkerProgressDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> importLocalBackupWorkerProgressDialogFragmentBuilder(
            ImportLocalBackupWorkerProgressDialogFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(ImportRemoteBackupWorkerProgressDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> importRemoteBackupWorkerProgressDialogFragmentBuilder(
            ImportRemoteBackupWorkerProgressDialogFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(FeedbackDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> feedbackDialogFragmentBuilder(
            FeedbackDialogFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(RatingDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> ratingDialogFragmentBuilder(
            RatingDialogFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(PaymentMethodsListFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> paymentMethodsListFragmentBuilder(
            PaymentMethodsListFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(CategoriesListFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> categoriesListFragmentBuilder(
            CategoriesListFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(ReceiptMoveCopyDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> receiptMoveCopyDialogFragmentBuilder(
            ReceiptMoveCopyDialogFragmentSubcomponent.Builder builder);

    @Binds
    @IntoMap
    @FragmentKey(SelectAutomaticBackupProviderDialogFragment.class)
    public abstract AndroidInjector.Factory<? extends Fragment> selectAutomaticBackupProviderDialogFragmentBuilder(
            SelectAutomaticBackupProviderDialogFragmentSubcomponent.Builder builder);
}
