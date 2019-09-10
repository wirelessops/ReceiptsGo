package co.smartreceipts.android.settings.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import java.text.DecimalFormat;

import co.smartreceipts.android.R;
import wb.android.preferences.FloatSummaryEditTextPreference;

public class MinimumPriceEditTextPreference extends FloatSummaryEditTextPreference {

	private static final float INCLUDE_ALL = -Float.MAX_VALUE;
	private static final float EPSILON = 0.1f;

	public MinimumPriceEditTextPreference(Context context) {
		super(context);
	}

	public MinimumPriceEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MinimumPriceEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public String getText() {
		String text = super.getText();
		try {
    		float value = Float.parseFloat(text);
    		if (Math.abs(value - INCLUDE_ALL) < EPSILON) { // We're including all
    			return "";
    		}
    		else {
    			return text;
    		}
		} catch (NumberFormatException e) {
			return text;
		}
	}

	@Override
	public CharSequence getSummary() {
		if (TextUtils.isEmpty(getText()) || getText().equals(String.valueOf(INCLUDE_ALL))) {
			return getContext().getString(R.string.pref_receipt_minimum_receipts_price_summary_all);
		}
		else {
			try {
				float minPrice = Float.parseFloat(getText());
				DecimalFormat decimalFormat = new DecimalFormat();
				decimalFormat.setMaximumFractionDigits(2);
				decimalFormat.setMinimumFractionDigits(2);
				decimalFormat.setGroupingUsed(false);
				return getContext().getString(R.string.pref_receipt_minimum_receipts_price_summary_partial, decimalFormat.format(minPrice));
			} catch (NumberFormatException e) {
				persistFloat(INCLUDE_ALL);
				return getContext().getString(R.string.pref_receipt_minimum_receipts_price_summary_all);
			}
		}
	}

	@Override
    protected boolean persistString(String value) {
    	if (TextUtils.isEmpty(value)) {
			return persistFloat(INCLUDE_ALL); // Don't persist zero here (we'll recurse and always end at zero)
		}
		else {
	    	try {
	    		return persistFloat(Float.valueOf(value));
			} catch (NumberFormatException e) {
				return false;
			}
		}
    }

}
