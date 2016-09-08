package de.f0rke.pageindicator;

import android.support.annotation.ColorRes;

/**
 * Created by moritzkochig on 07.09.16.
 *
 * @author Moritz Köchig
 *         © mobile concepts GmbH 2016
 */
public interface ColorProvider {

    @ColorRes
    int getActiveColor(int position);

    @ColorRes
    int getInactiveColor(int position);
}
