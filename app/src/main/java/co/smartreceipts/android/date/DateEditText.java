package co.smartreceipts.android.date;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.View;
import android.widget.DatePicker;

import com.google.common.base.Preconditions;

import java.sql.Date;
import java.util.Calendar;
import java.util.TimeZone;

import co.smartreceipts.android.model.utils.ModelUtils;
import co.smartreceipts.android.utils.SoftKeyboardManager;

/**
 * Provides a custom implementation of the {@link AppCompatEditText}, which automatically launches
 * a {@link DatePickerDialog} when this view is clicked.
 */
public class DateEditText extends AppCompatEditText implements DatePickerDialog.OnDateSetListener, DialogInterface.OnDismissListener {

	private Date date = new Date(Calendar.getInstance().getTimeInMillis());
    private TimeZone timeZone = TimeZone.getDefault();
    private DateFormatter dateFormatter;
    private DatePickerDialog datePickerDialog = null;

	public DateEditText(Context context) {
		super(context);
        super.setOnClickListener(new LaunchDatePickerDialogOnClickListener());
	}

	public DateEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
        super.setOnClickListener(new LaunchDatePickerDialogOnClickListener());
	}
	
	public DateEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        super.setOnClickListener(new LaunchDatePickerDialogOnClickListener());
	}

    @NonNull
    public Date getDate() {
        return date;
    }

    public void setDate(@NonNull Date date) {
        this.date = Preconditions.checkNotNull(date);
        if (dateFormatter != null) {
            setText(dateFormatter.getFormattedDate(date, timeZone));
        }
    }

    @NonNull
    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(@NonNull TimeZone timeZone) {
        this.timeZone = Preconditions.checkNotNull(timeZone);
        if (dateFormatter != null) {
            setText(dateFormatter.getFormattedDate(date, timeZone));
        }
    }

    /**
     * Supplies an instance of a {@link DateFormatter} to our view for rendering the dates.
     * Technically this isn't the best design pattern, and we should probably look to set the text
     * here via a separate class, but I don't feel like re-writing this for something simple, so
     * we'll just do it live.
     *
     * @param dateFormatter the {@link DateFormatter} instance
     */
    public void setDateFormatter(@NonNull DateFormatter dateFormatter) {
	    this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
        setText(dateFormatter.getFormattedDate(date, timeZone));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
	    final TimeZone activeTimeZone = TimeZone.getDefault(); // Since if the user changes this, we're now using this TimeZone
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(activeTimeZone);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        setDate(new Date(calendar.getTimeInMillis()));
        setTimeZone(activeTimeZone);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        throw new UnsupportedOperationException("We currently do not support custom listeners");
    }

    @Override
    public boolean performClick() {
        // Call super, which generates an AccessibilityEvent and calls the onClick() listener on the view
        super.performClick();
        return true;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        final Parcelable superDate = super.onSaveInstanceState();
        if (superDate != null) {
            return new SavedState(superDate, date, timeZone, datePickerDialog);
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
            timeZone = savedState.getTimeZone();
            if (savedState.wasDialogShowing()) {
                launchCalendarDialog(savedState);
            }
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        datePickerDialog = null;
    }

    /**
     * Launches the calendar dialog
     *
     * @param savedState the saved state to restore or {@code null} to launch a fresh dialog
     */
    private void launchCalendarDialog(@Nullable SavedState savedState) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.setTimeZone(timeZone);
        datePickerDialog = new DatePickerDialog(getContext(), DateEditText.this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setOnDismissListener(DateEditText.this);
        datePickerDialog.show();
        if (savedState != null) {
            datePickerDialog.getDatePicker().init(savedState.getYear(), savedState.getMonthOfYear(), savedState.getDayOfMonth(), datePickerDialog);
        }
        SoftKeyboardManager.hideKeyboard(this);
    }

    /**
     * Utility class that allows us to persist {@link Date} information across config changes
     */
    static class SavedState extends BaseSavedState {

        private final Date savedStateDate;
        private final TimeZone savedStateTimeZone;
        private final boolean savedStatesWasDialogShowing;
        private final int year;
        private final int monthOfYear;
        private final int dayOfMonth;


        public SavedState(@NonNull Parcelable superState,
                          @NonNull Date savedStateDate,
                          @NonNull TimeZone savedStateTimeZone,
                          @Nullable DatePickerDialog datePickerDialog) {
            super(superState);
            this.savedStateDate = Preconditions.checkNotNull(savedStateDate);
            this.savedStateTimeZone = Preconditions.checkNotNull(savedStateTimeZone);
            this.savedStatesWasDialogShowing = datePickerDialog != null;
            if (this.savedStatesWasDialogShowing) {
                final DatePicker datePicker = datePickerDialog.getDatePicker();
                this.year = datePicker.getYear();
                this.monthOfYear = datePicker.getMonth();
                this.dayOfMonth = datePicker.getDayOfMonth();
            } else {
                this.year = -1;
                this.monthOfYear = -1;
                this.dayOfMonth = -1;
            }
        }

        public SavedState(@NonNull Parcel in) {
            super(in);
            savedStateDate = (Date) in.readSerializable();
            savedStateTimeZone = (TimeZone) in.readSerializable();
            savedStatesWasDialogShowing = (in.readByte() != 0);
            year = in.readInt();
            monthOfYear = in.readInt();
            dayOfMonth = in.readInt();
        }

        @NonNull
        Date getDate() {
            return savedStateDate;
        }

        @NonNull
        TimeZone getTimeZone() {
            return savedStateTimeZone;
        }

        boolean wasDialogShowing() {
            return savedStatesWasDialogShowing;
        }

        int getYear() {
            return year;
        }

        int getMonthOfYear() {
            return monthOfYear;
        }

        int getDayOfMonth() {
            return dayOfMonth;
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeSerializable(savedStateDate);
            out.writeSerializable(savedStateTimeZone);
            out.writeByte((byte) (savedStatesWasDialogShowing ? 1 : 0));
            out.writeInt(year);
            out.writeInt(monthOfYear);
            out.writeInt(dayOfMonth);
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

    /**
     * Launches a {@link DatePickerDialog} using the configured date on click
     */
    private final class LaunchDatePickerDialogOnClickListener implements OnClickListener {

        @Override
        public void onClick(@NonNull View view) {
            if (datePickerDialog == null) {
                launchCalendarDialog(null);
            }
        }
    }

}
