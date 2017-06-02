package co.smartreceipts.android.adapters;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import co.smartreceipts.android.R;
import co.smartreceipts.android.config.ConfigurationManager;
import co.smartreceipts.android.fragments.DistanceFragment;
import co.smartreceipts.android.fragments.GenerateReportFragment;
import co.smartreceipts.android.receipts.ReceiptsListFragment;

public class TripFragmentPagerAdapter extends FragmentPagerAdapter {

    private static final int FRAGMENT_COUNT = 3;

    private final Resources resources;
    private final ConfigurationManager configurationManager;

    public TripFragmentPagerAdapter(Resources resources, @NonNull FragmentManager fragmentManager, @NonNull ConfigurationManager configurationManager) {
        super(fragmentManager);
        this.resources = resources;
        this.configurationManager = configurationManager;
    }

    @Override
    public int getCount() {
        if (configurationManager.isDistanceTrackingOptionAvailable()) {
            return FRAGMENT_COUNT;
        } else {
            return FRAGMENT_COUNT - 1;
        }
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return ReceiptsListFragment.newListInstance();
        } else if (position == 1) {
            if (configurationManager.isDistanceTrackingOptionAvailable()) {
                return DistanceFragment.newInstance();
            } else {
                return GenerateReportFragment.newInstance();
            }
        } else if (position == 2) {
            return GenerateReportFragment.newInstance();
        } else {
            throw new IllegalArgumentException("Unexpected Fragment Position");
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return resources.getString(R.string.report_info_receipts);
        } else if (position == 1) {
            if (configurationManager.isDistanceTrackingOptionAvailable()) {
                return resources.getString(R.string.report_info_distance);
            } else {
                return resources.getString(R.string.report_info_generate);
            }
        } else if (position == 2) {
            return resources.getString(R.string.report_info_generate);
        } else {
            throw new IllegalArgumentException("Unexpected Fragment Position");
        }
    }

    public int getGenerateTabPosition() {
        if (configurationManager.isDistanceTrackingOptionAvailable()) {
            return 2;
        } else {
            return 1;
        }
    }

}
