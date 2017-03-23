package de.f0rke.pageindicator;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by moritzkochig on 07.09.16.
 *
 * @author f0rke
 */
public abstract class CircularPagerAdapter<Item> extends FragmentStatePagerAdapter {
    protected List<Item> mItems;

    public CircularPagerAdapter(final FragmentManager fragmentManager, final List<Item> items) {
        super(fragmentManager);
        mItems = items;
    }

    public void replaceData(List<Item> items) {
        setList(items);
        notifyDataSetChanged();
    }

    protected void setList(List<Item> items) {
        mItems = checkNotNull(items);
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
