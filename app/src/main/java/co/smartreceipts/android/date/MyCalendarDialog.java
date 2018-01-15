package co.smartreceipts.android.date;

import android.content.Context;
import android.text.format.DateFormat;

import java.sql.Date;
import java.util.Calendar;

import wb.android.dialog.CalendarDialog;

public class MyCalendarDialog extends CalendarDialog {

	
	public interface Listener {
		void onDateSet(Date date);
	}
	
	private final Context mContext;
	private final DateManager mDateManager;
	private DateEditText mEdit, mEnd;
	private long mDuration;
	private Listener mDateSetListener;
	
	public MyCalendarDialog(Context context, DateManager manager) {
		super();
		mContext = context;
		mDateManager = manager;
	}
	
	public void setListener(Listener listener) {
		mDateSetListener = listener;
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public final void onDateSet(int day, int month, int year) {
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		final Date date = new Date(calendar.getTimeInMillis());

		mDateManager.setCachedDate(date);
		String dateString = DateFormat.getDateFormat(mContext).format(date);
		//This block is for mEdit
		if (mEdit != null) { 
			mEdit.setText(dateString);
			mEdit.date = date;
			if (mDateSetListener != null) mDateSetListener.onDateSet(date);
		}
		//This block is for mEnd
		if (mEnd != null && mEnd.date == null) { //ugly hack (order dependent set methods below)
			final Date endDate = new Date(date.getTime() + mDuration*86400000L+3600000L); //+3600000 for DST hack
			String endString = DateFormat.getDateFormat(mContext).format(endDate);
			mEnd.setText(endString);
			mEnd.date = endDate;
		}
	}
	
	public final void setEditText(final DateEditText edit) {
		mEdit = edit;
		mEnd = null;
	}
	
	public final void setEnd(final DateEditText end, final long duration) {
		mEnd = end;
		mDuration = duration;
	}

}
