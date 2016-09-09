package de.f0rke.pageindicator;

import android.support.annotation.ColorRes;

/**
 * Created by moritzkochig on 07.09.16.
 *
 * @author f0rke
 */
public interface ColorProvider {

    /**
     * Provide the color for the Indicator at {@param position} in active state
     *
     * @param position The index of the page, which the relevant indicator represents
     * @return Color to use for active state
     */
    @ColorRes
    int getActiveColor(int position);

    /**
     * Provide the color for the Indicator at {@param position} in inactive state
     *
     * @param position The index of the page, which is represented by the relevant indicator
     * @return Color to use for inactive state
     */
    @ColorRes
    int getInactiveColor(int position);
}
