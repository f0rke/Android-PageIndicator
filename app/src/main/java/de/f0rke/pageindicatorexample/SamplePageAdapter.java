package de.f0rke.pageindicatorexample;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

/**
 * Created by moritzkochig on 4/10/17.
 *
 * @author Moritz Köchig
 *         © mobile concepts GmbH 2016
 */

public class SamplePageAdapter extends FragmentStatePagerAdapter {

    private List<SampleContentContainer> list;

    public SamplePageAdapter(FragmentManager fm, List<SampleContentContainer> sampleContentContainers) {
        super(fm);
        this.list = sampleContentContainers;
    }

    @Override
    public Fragment getItem(int position) {
        return SamplePageFragment.newInstance(list.get(position), position);
    }

    @Override
    public int getCount() {
        return list.size();
    }
}
