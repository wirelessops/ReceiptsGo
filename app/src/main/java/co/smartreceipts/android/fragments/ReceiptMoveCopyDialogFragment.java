package co.smartreceipts.android.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.databinding.DialogReceiptMoveCopyBinding;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.impl.ReceiptTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.StubTableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.impl.TripTableController;
import dagger.android.support.AndroidSupportInjection;

public class ReceiptMoveCopyDialogFragment extends DialogFragment implements Dialog.OnClickListener {

    public static final String TAG = ReceiptMoveCopyDialogFragment.class.getSimpleName();

    @Inject
    TripTableController tripTableController;
    @Inject
    ReceiptTableController receiptTableController;

    private Receipt mReceipt;
    private TableEventsListener<Trip> mTripTableEventsListener;
    private Spinner mTripSpinner;
    private ViewGroup container;
    private DialogReceiptMoveCopyBinding binding;

    public static ReceiptMoveCopyDialogFragment newInstance(@NonNull Receipt receipt) {
        final ReceiptMoveCopyDialogFragment fragment = new ReceiptMoveCopyDialogFragment();
        final Bundle args = new Bundle();
        args.putParcelable(Receipt.PARCEL_KEY, receipt);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReceipt = getArguments().getParcelable(Receipt.PARCEL_KEY);
        mTripTableEventsListener = new UpdateAdapterTrips();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.container = container;
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        binding = DialogReceiptMoveCopyBinding.inflate(inflater, container, false);
        mTripSpinner = binding.moveCopySpinner;
        mTripSpinner.setPrompt(getString(R.string.report));

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.move_copy_item, mReceipt.getName()));
        builder.setView(binding.getRoot());
        builder.setCancelable(true);

        // Note: we change this order from standard Android, so move appears next to copy
        builder.setPositiveButton(R.string.move, this);
        builder.setNeutralButton(android.R.string.cancel, this);
        builder.setNegativeButton(R.string.copy, this);

        return builder.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        tripTableController.subscribe(mTripTableEventsListener);
        tripTableController.get();
    }

    @Override
    public void onPause() {
        tripTableController.unsubscribe(mTripTableEventsListener);
        super.onPause();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        final TripNameAdapterWrapper wrapper = (TripNameAdapterWrapper) mTripSpinner.getSelectedItem();
        if (which == DialogInterface.BUTTON_POSITIVE) {
            if (wrapper != null) {
                receiptTableController.move(mReceipt, wrapper.mTrip);
                dismiss();
            }
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            if (wrapper != null) {
                receiptTableController.copy(mReceipt, wrapper.mTrip);
                dismiss();
            }
        } else {
            dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private final class UpdateAdapterTrips extends StubTableEventsListener<Trip> {

        @Override
        public void onGetSuccess(@NonNull List<Trip> list) {
            final List<TripNameAdapterWrapper> wrappers = new ArrayList<>();
            for (final Trip trip : list) {
                wrappers.add(new TripNameAdapterWrapper(trip));
            }
            final ArrayAdapter<TripNameAdapterWrapper> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, wrappers);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mTripSpinner.setAdapter(adapter);
        }
    }

    /**
     * A lazy implementation to take advantage that array adapters just call #toString
     */
    private final class TripNameAdapterWrapper {
        private final Trip mTrip;

        public TripNameAdapterWrapper(@NonNull Trip trip) {
            mTrip = Preconditions.checkNotNull(trip);
        }

        @Override
        public String toString() {
            return mTrip.getName();
        }
    }
}
