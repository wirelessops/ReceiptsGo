package com.wops.receiptsgo.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.Vector;

import com.wops.receiptsgo.R;
import com.wops.receiptsgo.model.TaxItem;

public class TaxAutoCompleteAdapter extends ArrayAdapter<TaxItem> implements TextWatcher, View.OnFocusChangeListener {

    private static final BigDecimal VAT_0 = BigDecimal.ZERO;
	private static final BigDecimal VAT_5_5 = BigDecimal.valueOf(5.5f);
	private static final BigDecimal VAT_10 = BigDecimal.valueOf(10);
	private static final BigDecimal VAT_20 = BigDecimal.valueOf(20);
	
	private final LayoutInflater mInflater;
	private final int mListItemId;
	private final TaxItem mDefaultValue;
	private final WeakReference<TextView> mPriceBox;
	private final WeakReference<AutoCompleteTextView> mTaxBox;

	private final boolean usePreTaxPrice;
    private final boolean isNewReceipt;
	
	/**
	 * The internal {@link java.util.List} that the {@link ArrayAdapter} uses to track entries caused
	 * some synchronization issues. I used a {@link Vector} to resolve this.
	 */
	private final Vector<TaxItem> mData;
	
	public TaxAutoCompleteAdapter(Context context, TextView priceBox, AutoCompleteTextView taxBox,
								  boolean usePreTaxPrice, float defaultValue, boolean isNewReceipt) {
		super(context, R.layout.item_tax_autocomplete);
		mInflater = LayoutInflater.from(context);
		mData = new Vector<>();
		mListItemId = R.layout.item_tax_autocomplete;
		mDefaultValue = new TaxItem(defaultValue, usePreTaxPrice);
		mPriceBox = new WeakReference<>(priceBox);
		mTaxBox = new WeakReference<>(taxBox);
		priceBox.addTextChangedListener(this);
		taxBox.setOnFocusChangeListener(this);
		this.usePreTaxPrice = usePreTaxPrice;
        this.isNewReceipt = isNewReceipt;
	}
	
	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public TaxItem getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public boolean isEmpty() {
		return mData.isEmpty();
	}

	private static class MyViewHolder {
		public TextView tax_value;
		public TextView tax_percent;
	}
	
	@NonNull
    @Override
	public View getView(int position, View convertView, @NonNull ViewGroup parent) {
		TextView priceBox = mPriceBox.get();
		if (priceBox == null || TextUtils.isEmpty(priceBox.getText())) {
			return null;
		}
		
		final MyViewHolder holder;
		if (convertView == null) {
			holder = new MyViewHolder();
			convertView = mInflater.inflate(mListItemId, parent, false);
			holder.tax_value = convertView.findViewById(R.id.text_tax_value);
			holder.tax_percent = convertView.findViewById(R.id.text_tax_percent);
			convertView.setTag(holder);
		}
		else {
			holder = (MyViewHolder) convertView.getTag();
		}
		TaxItem item = getItem(position);
		item.setPrice(priceBox.getText().toString());
		holder.tax_value.setText(item.toString());
		holder.tax_percent.setText(item.getPercentAsString());
		return convertView;
	}

	@Override
	public void afterTextChanged(Editable editable) {
		// Method stub
	}

	@Override
	public void beforeTextChanged(CharSequence text, int start, int before, int count) {
		// Method stub
		
	}

	@Override
	public synchronized void onTextChanged(CharSequence text, int start, int before, int count) {
		if (text.length() > 0) {
			if (this.isEmpty()) { // If our adapter is empty, let's fill it up
				if (this.hasDefaultValue()) {
					mData.add(mDefaultValue);
				}
                mData.add(new TaxItem(VAT_0, usePreTaxPrice));
				mData.add(new TaxItem(VAT_5_5, usePreTaxPrice));
				mData.add(new TaxItem(VAT_10, usePreTaxPrice));
				mData.add(new TaxItem(VAT_20, usePreTaxPrice));
			}
			TextView taxBox = mTaxBox.get();
			if (this.hasDefaultValue() && taxBox != null) {
                if (isNewReceipt || !usePreTaxPrice) {
                    // If we're editing a receipt in post tax mode, let's have the updates
                    mDefaultValue.setPrice(text.toString());
                    taxBox.setText(mDefaultValue.toString());
                }
			}
		}
		else {
			TextView taxBox = mTaxBox.get();
			if (this.hasDefaultValue() && taxBox != null) {
                if (isNewReceipt || !usePreTaxPrice) {
                    // If we're editing a receipt in post tax mode, let's have the updates
                    mDefaultValue.setPrice("0");
                    taxBox.setText(mDefaultValue.toString());
                }
			}
			mData.clear();
		}
	}

	public boolean hasDefaultValue() {
		return BigDecimal.ZERO.compareTo(mDefaultValue.getPercent()) < 0;
	}

	@Override
	public synchronized void onFocusChange(View view, boolean hasFocus) {
		AutoCompleteTextView taxBox = mTaxBox.get();
		if (taxBox != null) {
			try {
				taxBox.showDropDown();
			}
			catch (Exception e) {
				// Ignore
			}
		}
	}

}
