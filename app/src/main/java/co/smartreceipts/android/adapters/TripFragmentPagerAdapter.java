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
import co.smartreceipts.android.graphs.GraphsFragment;
import co.smartreceipts.android.receipts.ReceiptsListFragment;
import co.smartreceipts.android.utils.ConfigurableResourceFeature;

public class TripFragmentPagerAdapter extends FragmentPagerAdapter {

    private static final int MAX_FRAGMENT_COUNT = 4;

    private final Resources resources;
    private final ConfigurationManager configurationManager;

    private final int graphsTabPosition;
    private final int receiptsTabPosition;
    private final int distanceTabPosition;
    private final int generateTabPosition;

    public TripFragmentPagerAdapter(Resources resources, @NonNull FragmentManager fragmentManager, @NonNull ConfigurationManager configurationManager) {
        super(fragmentManager);
        this.resources = resources;
        this.configurationManager = configurationManager;

        boolean distanceAvailable = configurationManager.isEnabled(ConfigurableResourceFeature.DistanceScreen);
        boolean graphsAvailable = configurationManager.isEnabled(ConfigurableResourceFeature.GraphsScreen);

        graphsTabPosition = graphsAvailable ? 0 : -1;
        receiptsTabPosition = graphsAvailable ? 1 : 0;
        distanceTabPosition = distanceAvailable ? receiptsTabPosition + 1 : -1;
        generateTabPosition = distanceAvailable ? distanceTabPosition + 1 : receiptsTabPosition + 1;

    }


    @Override
    public int getCount() {
        boolean distanceAvailable = configurationManager.isEnabled(ConfigurableResourceFeature.DistanceScreen);
        boolean graphsAvailable = configurationManager.isEnabled(ConfigurableResourceFeature.GraphsScreen);

        if (distanceAvailable && graphsAvailable) {
            return MAX_FRAGMENT_COUNT;
        } else if (!distanceAvailable && !graphsAvailable) {
            return MAX_FRAGMENT_COUNT - 2;
        } else {
            return MAX_FRAGMENT_COUNT - 1;
        }
    }

    @Override
    public Fragment getItem(int position) {
        if (position == graphsTabPosition) return GraphsFragment.newInstance();
        if (position == receiptsTabPosition) return ReceiptsListFragment.newListInstance();
        if (position == distanceTabPosition) return DistanceFragment.newInstance();
        if (position == generateTabPosition) return GenerateReportFragment.newInstance();

        throw new IllegalArgumentException("Unexpected Fragment Position");
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == graphsTabPosition) return resources.getString(R.string.report_info_graphs);
        if (position == receiptsTabPosition) return resources.getString(R.string.report_info_receipts);
        if (position == distanceTabPosition) return resources.getString(R.string.report_info_distance);
        if (position == generateTabPosition) return resources.getString(R.string.report_info_generate);

        throw new IllegalArgumentException("Unexpected Fragment Position");
    }

    public int getGenerateTabPosition() {
        return generateTabPosition;
    }

    public int getReceiptsTabPosition() {
        return receiptsTabPosition;
    }

}
