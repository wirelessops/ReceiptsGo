package co.smartreceipts.android.date;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

import java.sql.Date;

public class DateEditText extends AppCompatEditText {

	public Date date;
	
	public DateEditText(Context context) {
		super(context);
		date = null;
	}
	
	public DateEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		date = null;
	}
	
	public DateEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		date = null;
	}

    @Override
    public Parcelable onSaveInstanceState() {
        final Parcelable superDate = super.onSaveInstanceState();
        if (superDate != null) {
            return new SavedState(superDate, date);
        } else {
            return null;
        }
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            final SavedState savedState = (SavedState) state;
            super.onRestoreInstanceState(savedState.getSuperState());
            date = savedState.getDate();
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    @Override
    public boolean performClick() {
        // Call super, which generates an AccessibilityEvent and calls the onClick() listener on the view
        super.performClick();
        return true;
    }

    /**
     * Utility class that allows us to persist {@link Date} information across config changes
     */
    static class SavedState extends BaseSavedState {

        private final Date mDate;

        public SavedState(@NonNull Parcelable superDate, @Nullable Date date) {
            super(superDate);
            mDate = date;
        }

        public SavedState(@NonNull Parcel in) {
            super(in);
            mDate = (Date) in.readSerializable();
        }

        @Nullable
        public Date getDate() {
            return mDate;
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeSerializable(mDate);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(@NonNull Parcel in) {
                return new SavedState(in);
            }
            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}
