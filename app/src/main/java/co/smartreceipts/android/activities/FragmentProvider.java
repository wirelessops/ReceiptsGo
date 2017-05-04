package co.smartreceipts.android.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;

import javax.inject.Inject;

import co.smartreceipts.android.fragments.ReceiptImageFragment;
import co.smartreceipts.android.fragments.ReportInfoFragment;
import co.smartreceipts.android.identity.widget.login.LoginFragment;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.ocr.apis.model.OcrResponse;
import co.smartreceipts.android.ocr.widget.configuration.OcrConfigurationFragment;
import co.smartreceipts.android.receipts.editor.ReceiptCreateEditFragment;
import co.smartreceipts.android.sync.widget.backups.BackupsFragment;
import co.smartreceipts.android.trips.TripFragment;
import co.smartreceipts.android.trips.editor.TripCreateEditFragment;
import co.smartreceipts.android.utils.cache.FragmentArgumentCache;

import static co.smartreceipts.android.receipts.editor.ReceiptCreateEditFragment.ARG_FILE;
import static co.smartreceipts.android.receipts.editor.ReceiptCreateEditFragment.ARG_OCR;
import static co.smartreceipts.android.trips.TripFragment.ARG_NAVIGATE_TO_VIEW_LAST_TRIP;

public class FragmentProvider {

    @Inject
    FragmentArgumentCache fragmentArgumentCache;

    @Inject
    public FragmentProvider() {
    }

    /**
     * Creates a {@link TripFragment} instance
     *
     * @return a new trip fragment
     */
    @NonNull
    public TripFragment newTripFragmentInstance(boolean navigateToViewLastTrip) {
        final Bundle args = new Bundle();
        args.putBoolean(ARG_NAVIGATE_TO_VIEW_LAST_TRIP, navigateToViewLastTrip);
        fragmentArgumentCache.put(args, TripFragment.class);

        return TripFragment.newInstance();
    }

    /**
     * Creates a {@link ReportInfoFragment} instance
     *
     * @param trip the trip to display info for
     * @return a new report info fragment
     */
    @NonNull
    public ReportInfoFragment newReportInfoFragment(@NonNull Trip trip) {
        Bundle args = new Bundle();
        args.putParcelable(Trip.PARCEL_KEY, trip);
        fragmentArgumentCache.put(args, ReportInfoFragment.class);

        return ReportInfoFragment.newInstance();
    }

    /**
     * Creates a {@link ReceiptCreateEditFragment} for a new receipt
     *
     * @param trip the parent trip of this receipt
     * @param file the file associated with this receipt or null if we do not have one
     * @return the new instance of this fragment
     */
    @NonNull
    public ReceiptCreateEditFragment newCreateReceiptFragment(@NonNull Trip trip, @Nullable File file, @Nullable OcrResponse ocrResponse) {
        final Bundle args = new Bundle();
        args.putParcelable(Trip.PARCEL_KEY, trip);
        args.putParcelable(Receipt.PARCEL_KEY, null);
        args.putSerializable(ARG_FILE, file);
        args.putSerializable(ARG_OCR, ocrResponse);
        fragmentArgumentCache.put(args, ReceiptCreateEditFragment.class);

        return ReceiptCreateEditFragment.newInstance();
    }

    /**
     * Creates a {@link ReceiptCreateEditFragment} to edit an existing receipt
     *
     * @param trip the parent trip of this receipt
     * @param receiptToEdit the receipt to edit
     * @return the new instance of this fragment
     */
    @NonNull
    public ReceiptCreateEditFragment newEditReceiptFragment(@NonNull Trip trip, @NonNull Receipt receiptToEdit) {
        final Bundle args = new Bundle();
        args.putParcelable(Trip.PARCEL_KEY, trip);
        args.putParcelable(Receipt.PARCEL_KEY, receiptToEdit);
        args.putSerializable(ARG_FILE, null);
        args.putSerializable(ARG_OCR, null);
        fragmentArgumentCache.put(args, ReceiptCreateEditFragment.class);

        return ReceiptCreateEditFragment.newInstance();
    }

    /**
     * Creates a {@link ReceiptImageFragment} instance
     *
     * @param receipt the receipt to show the image for
     * @return a new instance of this fragment
     */
    @NonNull
    public ReceiptImageFragment newReceiptImageFragment(@NonNull Receipt receipt) {
        Bundle args = new Bundle();
        args.putParcelable(Receipt.PARCEL_KEY, receipt);
        fragmentArgumentCache.put(args, ReceiptImageFragment.class);

        return ReceiptImageFragment.newInstance();
    }

    /**
     * Creates a {@link BackupsFragment} instance
     *
     * @return a new instance of this fragment
     */
    @NonNull
    public BackupsFragment newBackupsFragment() {
        return new BackupsFragment();
    }

    /**
     * Creates a {@link LoginFragment} instance
     *
     * @return a new instance of this fragment
     */
    @NonNull
    public LoginFragment newLoginFragment() {
        return LoginFragment.newInstance();
    }

    /**
     * Creates a {@link OcrConfigurationFragment} instance
     *
     * @return a new instance of this fragment
     */
    @NonNull
    public OcrConfigurationFragment newOcrConfigurationFragment() {
        return OcrConfigurationFragment.newInstance();
    }

    /**
     * Creates a {@link TripCreateEditFragment} for a new trip
     *
     * @return the new instance of this fragment
     */
    @NonNull
    public TripCreateEditFragment newCreateTripFragment() {
        fragmentArgumentCache.remove(TripCreateEditFragment.class);
        return TripCreateEditFragment.newInstance();
    }

    /**
     * Creates a {@link TripCreateEditFragment} to edit an existing trip
     *
     * @param tripToEdit the trip to edit
     * @return the new instance of this fragment
     */
    @NonNull
    public TripCreateEditFragment newEditTripFragment(@NonNull Trip tripToEdit) {
        Bundle args = new Bundle();
        args.putParcelable(Trip.PARCEL_KEY, tripToEdit);
        fragmentArgumentCache.put(args, TripCreateEditFragment.class);

        return TripCreateEditFragment.newInstance();
    }
}
