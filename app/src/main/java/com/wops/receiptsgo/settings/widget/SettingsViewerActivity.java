package com.wops.receiptsgo.settings.widget;

import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.wops.receiptsgo.R;
import com.wops.receiptsgo.settings.widget.editors.columns.CSVColumnsListFragment;
import com.wops.receiptsgo.settings.widget.editors.categories.CategoriesListFragment;
import com.wops.receiptsgo.settings.widget.editors.columns.PDFColumnsListFragment;
import com.wops.receiptsgo.settings.widget.editors.payment.PaymentMethodsListFragment;

public class SettingsViewerActivity extends AppCompatActivity {
	
	public static final String KEY_FLAG = "KeyFlag";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.activity_main_onepane_no_ad_override);
        
		if (savedInstanceState == null) {
			final String key = getKey();
			if (key.equalsIgnoreCase(getString(R.string.pref_receipt_customize_categories_key))) {
				getSupportFragmentManager().beginTransaction().replace(R.id.content_list, CategoriesListFragment.newInstance(), CategoriesListFragment.TAG).commitAllowingStateLoss();
			}
			else if (key.equalsIgnoreCase(getString(R.string.pref_output_custom_csv_key))) {
				getSupportFragmentManager().beginTransaction().replace(R.id.content_list, CSVColumnsListFragment.newInstance(), CSVColumnsListFragment.TAG).commitAllowingStateLoss();
			}
			else if (key.equalsIgnoreCase(getString(R.string.pref_output_custom_pdf_key))) {
				getSupportFragmentManager().beginTransaction().replace(R.id.content_list, PDFColumnsListFragment.newInstance(), PDFColumnsListFragment.TAG).commitAllowingStateLoss();
			}
			else if (key.equals(getString(R.string.pref_receipt_payment_methods_key))) {
				getSupportFragmentManager().beginTransaction().replace(R.id.content_list, PaymentMethodsListFragment.newInstance(), PaymentMethodsListFragment.TAG).commitAllowingStateLoss();
			}
			else {
				finish(); //Unknown Key was passed
			}
		}
	}
	
	private String getKey() {
		Bundle extras = getIntent().getExtras();
		if (extras != null && extras.containsKey(KEY_FLAG)) {
			return extras.getString(KEY_FLAG);
		}
		else {
			return "";
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
	}
	
}
