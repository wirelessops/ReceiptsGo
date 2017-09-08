package co.smartreceipts.android.date;

import android.content.Context;
import android.text.format.DateFormat;

import java.sql.Date;

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
		final Date date = new Date(year - 1900, month, day);  //**This Date constructor is deprecated
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
