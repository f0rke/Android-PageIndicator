package de.f0rke.pageindicatorexample;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.List;

import de.f0rke.pageindicator.CircularPagerAdapter;

/**
 * Created by moritzkochig on 07.09.16.
 *
 * @author Moritz Köchig
 *         © mobile concepts GmbH 2016
 */
public class SamplePageAdapter extends CircularPagerAdapter<ContentContainer> {
    public SamplePageAdapter(FragmentManager fragmentManager, List<ContentContainer> contentContainers) {
        super(fragmentManager, contentContainers);
    }

    @Override
    protected Fragment getFragmentForItem(ContentContainer contentContainer, int index) {
        return SamplePageFragment.newInstance(contentContainer, index);
    }
}
