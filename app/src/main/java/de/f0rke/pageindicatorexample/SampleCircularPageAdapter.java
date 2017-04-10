package de.f0rke.pageindicatorexample;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.List;

import de.f0rke.pageindicator.CircularPagerAdapter;

/**
 * Created by moritzkochig on 07.09.16.
 *
 * @author f0rke
 */
public class SampleCircularPageAdapter extends CircularPagerAdapter<SampleContentContainer> {
    public SampleCircularPageAdapter(FragmentManager fragmentManager, List<SampleContentContainer> sampleContentContainers) {
        super(fragmentManager, sampleContentContainers);
    }

    @Override
    protected Fragment getFragmentForItem(SampleContentContainer sampleContentContainer, int index) {
        return SamplePageFragment.newInstance(sampleContentContainer, index);
    }
}
