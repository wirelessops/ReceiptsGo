package co.smartreceipts.android.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.common.base.Preconditions;

import java.util.EnumSet;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.widget.tooltip.report.generate.GenerateInfoTooltipManager;
import co.smartreceipts.android.workers.EmailAssistant;
import dagger.android.support.AndroidSupportInjection;
import wb.android.flex.Flex;

public class GenerateReportFragment extends WBFragment implements View.OnClickListener {

    @Inject
    Flex flex;

    @Inject
    PersistenceManager persistenceManager;

    @Inject
    Analytics analytics;

    @Inject
    NavigationHandler navigationHandler;

    @Inject
    GenerateInfoTooltipManager generateInfoTooltipManager;

    @Inject
    PurchaseWallet purchaseWallet;

    @Inject
    UserPreferenceManager preferenceManager;

    private CheckBox pdfFullCheckbox;
    private CheckBox pdfImagesCheckbox;
    private CheckBox csvCheckbox;
    private CheckBox zipCheckbox;
    private CheckBox zipWithMetadataCheckbox;

    private Trip trip;

    @NonNull
    public static GenerateReportFragment newInstance() {
        return new GenerateReportFragment();
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.generate_report_layout, container, false);
        pdfFullCheckbox = (CheckBox) flex.getSubView(getActivity(), root, R.id.dialog_email_checkbox_pdf_full);
        pdfImagesCheckbox = (CheckBox) flex.getSubView(getActivity(), root, R.id.dialog_email_checkbox_pdf_images);
        csvCheckbox = (CheckBox) flex.getSubView(getActivity(), root, R.id.dialog_email_checkbox_csv);
        zipWithMetadataCheckbox = (CheckBox) flex.getSubView(getActivity(), root, R.id.dialog_email_checkbox_zip_with_metadata);
        zipCheckbox = root.findViewById(R.id.dialog_email_checkbox_zip);
        root.findViewById(R.id.receipt_action_send).setOnClickListener(this);
        root.findViewById(R.id.generate_report_tooltip).setOnClickListener(v -> {
            analytics.record(Events.Informational.ConfigureReport);
            navigationHandler.navigateToSettingsScrollToReportSection();
        });
        return root;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Logger.debug(this, "onActivityCreated");
        trip = ((ReportInfoFragment) getParentFragment()).getTrip();
        Preconditions.checkNotNull(trip, "A valid trip is required");
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setSubtitle(null);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Logger.debug(this, "pre-onSaveInstanceState");
        super.onSaveInstanceState(outState);
        Logger.debug(this, "onSaveInstanceState");
    }

    @Override
    public void onClick(View v) {
        if (!pdfFullCheckbox.isChecked() && !pdfImagesCheckbox.isChecked() && !csvCheckbox.isChecked() &&
                !zipCheckbox.isChecked() && !zipWithMetadataCheckbox.isChecked()) {
            Toast.makeText(getActivity(), flex.getString(getActivity(), R.string.DIALOG_EMAIL_TOAST_NO_SELECTION), Toast.LENGTH_SHORT).show();
            return;
        }

        analytics.record(Events.Generate.GenerateReports);
        generateInfoTooltipManager.reportWasGenerated();
        if (pdfFullCheckbox.isChecked()) {
            analytics.record(Events.Generate.FullPdfReport);
        }
        if (pdfImagesCheckbox.isChecked()) {
            analytics.record(Events.Generate.ImagesPdfReport);
        }
        if (csvCheckbox.isChecked()) {
            analytics.record(Events.Generate.CsvReport);
        }
        if (zipWithMetadataCheckbox.isChecked()) {
            analytics.record(Events.Generate.ZipWithMetadataReport);
        }
        if (zipCheckbox.isChecked()) {
            analytics.record(Events.Generate.ZipReport);
        }

        // TODO: Off the UI thread :/
        if (persistenceManager.getDatabase().getReceiptsTable().getBlocking(trip, true).isEmpty()) {

            if (persistenceManager.getDatabase().getDistanceTable().getBlocking(trip, true).isEmpty() ||
                    !(pdfFullCheckbox.isChecked() || csvCheckbox.isChecked())) {
                // Only allow report processing to continue with no receipts if we're doing a full pdf or CSV report with distances
                Toast.makeText(getActivity(), flex.getString(getActivity(), R.string.DIALOG_EMAIL_TOAST_NO_RECEIPTS), Toast.LENGTH_SHORT).show();
                return;
            } else {
                if (csvCheckbox.isChecked() && !preferenceManager.get(UserPreference.Distance.PrintDistanceTableInReports)) {
                    // user wants to create CSV report with just distances but this option is disabled
                    Toast.makeText(getActivity(), getString(R.string.toast_csv_report_distances, getString(R.string.pref_distance_print_table_title)), Toast.LENGTH_LONG)
                            .show();
                    navigationHandler.navigateToSettingsScrollToDistanceSection();
                    return;
                }
                // Uncheck "Illegal" Items
                pdfImagesCheckbox.setChecked(false);
                zipWithMetadataCheckbox.setChecked(false);
                zipCheckbox.setChecked(false);
            }
        }

        EnumSet<EmailAssistant.EmailOptions> options = EnumSet.noneOf(EmailAssistant.EmailOptions.class);
        if (pdfFullCheckbox.isChecked()) {
            options.add(EmailAssistant.EmailOptions.PDF_FULL);
        }
        if (pdfImagesCheckbox.isChecked()) {
            options.add(EmailAssistant.EmailOptions.PDF_IMAGES_ONLY);
        }
        if (csvCheckbox.isChecked()) {
            options.add(EmailAssistant.EmailOptions.CSV);
        }
        if (zipWithMetadataCheckbox.isChecked()) {
            options.add(EmailAssistant.EmailOptions.ZIP_WITH_METADATA);
        }
        if (zipCheckbox.isChecked()) {
            options.add(EmailAssistant.EmailOptions.ZIP);
        }

        final EmailAssistant emailAssistant = new EmailAssistant(navigationHandler, getActivity(),
                flex, persistenceManager, trip, purchaseWallet);
        emailAssistant.emailTrip(options);
    }
}
