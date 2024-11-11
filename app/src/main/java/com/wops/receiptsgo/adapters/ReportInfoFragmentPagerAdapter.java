package com.wops.receiptsgo.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.wops.receiptsgo.config.ConfigurationManager;
import com.wops.receiptsgo.fragments.DistanceFragment;
import com.wops.receiptsgo.graphs.GraphsFragment;
import com.wops.receiptsgo.receipts.ReceiptsListFragment;
import com.wops.receiptsgo.utils.ConfigurableResourceFeature;
import com.wops.receiptsgo.workers.widget.GenerateReportFragment;

public class ReportInfoFragmentPagerAdapter extends FragmentPagerAdapter {

    private static final int MAX_FRAGMENT_COUNT = 4;

    private final ConfigurationManager configurationManager;

    private final int graphsTabPosition;
    private final int receiptsTabPosition;
    private final int distanceTabPosition;
    private final int generateTabPosition;

    public ReportInfoFragmentPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull ConfigurationManager configurationManager) {
        super(fragmentManager);
        this.configurationManager = configurationManager;

        boolean distanceAvailable = configurationManager.isEnabled(ConfigurableResourceFeature.DistanceScreen);
        boolean graphsAvailable = configurationManager.isEnabled(ConfigurableResourceFeature.GraphsScreen);

        receiptsTabPosition = 0;
        distanceTabPosition = distanceAvailable ? receiptsTabPosition + 1 : -1;
        generateTabPosition = distanceAvailable ? distanceTabPosition + 1 : receiptsTabPosition + 1;
        graphsTabPosition = graphsAvailable ? generateTabPosition + 1 : -1;
    }


    @Override
    public int getCount() {
        boolean distanceAvailable = configurationManager.isEnabled(ConfigurableResourceFeature.DistanceScreen);
        boolean graphsAvailable = configurationManager.isEnabled(ConfigurableResourceFeature.GraphsScreen);

        return MAX_FRAGMENT_COUNT - (distanceAvailable ? 0 : 1) - (graphsAvailable ? 0 : 1);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == graphsTabPosition) return GraphsFragment.newInstance();
        if (position == receiptsTabPosition) return ReceiptsListFragment.newListInstance();
        if (position == distanceTabPosition) return DistanceFragment.newInstance();
        if (position == generateTabPosition) return GenerateReportFragment.newInstance();

        throw new IllegalArgumentException("Unexpected Fragment Position");
    }

    public int getGenerateTabPosition() {
        return generateTabPosition;
    }

    public int getReceiptsTabPosition() {
        return receiptsTabPosition;
    }

    public int getDistancesTabPosition() {
        return distanceTabPosition;
    }

    public int getGraphsTabPosition() {
        return graphsTabPosition;
    }
}
