package com.wops.receiptsgo.model.impl;

import android.os.Parcel;

import com.wops.receiptsgo.model.ProcessingStatus;

/**
 * Tracks the different processing states that a given {@link com.wops.receiptsgo.model.Receipt} can reside in.
 */
public enum ReceiptProcessingStatus implements ProcessingStatus {

    /**
     * Indicates that this Receipt should still be treated as in new (i.e. not submitted)
     */
    Created("Created"),

    /**
     * Indicates that a Receipt has been submitted
     */
    Submitted("Submitted"),

    /**
     * Indicates that this Receipt has been reimbursed by whomever is paying for it
     */
    Reimbursed("Reimbursed"),

    /**
     * Indicates that we're in an unknown state - shouldn't be normally be used
     */
    None("None");

    private final String mStatus;

    ReceiptProcessingStatus(final String status) {
        mStatus = status;
    }

    @Override
    public String getProcessingStatus() {
        return mStatus;
    }

    /**
     * Attempts to generate an enum representation of a given status string
     *
     * @param status - the provided status {@link String}
     * @return the corresponding {@link TripProcessingStatus} or {@link TripProcessingStatus#None} if none match
     */
    public static ReceiptProcessingStatus findProcessingStatusForString(final String status) {
        final ReceiptProcessingStatus[] receiptProcessingStatuses = ReceiptProcessingStatus.values();
        for (int i = 0; i < receiptProcessingStatuses.length; i++) {
            if (receiptProcessingStatuses[i].getProcessingStatus().equals(status)) {
                return receiptProcessingStatuses[i];
            }
        }
        return None;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mStatus);
    }

    public static final Creator<ReceiptProcessingStatus> CREATOR = new Creator<ReceiptProcessingStatus>() {
        public ReceiptProcessingStatus createFromParcel(Parcel source) {
            return findProcessingStatusForString(source.readString());
        }

        public ReceiptProcessingStatus[] newArray(int size) {
            return new ReceiptProcessingStatus[size];
        }
    };
}
