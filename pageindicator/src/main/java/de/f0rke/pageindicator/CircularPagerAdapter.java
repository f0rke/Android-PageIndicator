package de.f0rke.pageindicator;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

/**
 * Created by moritzkochig on 07.09.16.
 *
 * @author f0rke
 */
public abstract class CircularPagerAdapter<Item> extends FragmentStatePagerAdapter {
    private List<Item> mItems;

    public CircularPagerAdapter(final FragmentManager fragmentManager, final List<Item> items) {
        super(fragmentManager);
        mItems = items;
    }

    protected abstract Fragment getFragmentForItem(final Item item, int index);

    @Override
    public Fragment getItem(final int position) {
        final int itemsSize = mItems.size();
        int index;
        if (position == 0) {
            index = itemsSize - 1;
        } else if (position == itemsSize + 1) {
            index = 0;
        } else {
            index = position - 1;
        }
        return getFragmentForItem(mItems.get(index), position);
    }

    @Override
    public int getCount() {
        final int itemsSize = mItems.size();
        return itemsSize > 1 ? itemsSize + 2 : itemsSize;
    }

    public int getCountWithoutFakePages() {
        return mItems.size();
    }
}
