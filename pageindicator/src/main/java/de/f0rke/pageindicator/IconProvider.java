package de.f0rke.pageindicator;

import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;

/**
 * Created by moritzkochig on 07.09.16.
 *
 * @author Moritz Köchig
 *         © mobile concepts GmbH 2016
 */
public interface IconProvider {

    @Nullable
    @DrawableRes
    Integer getActiveIcon(int position);

    @Nullable
    @DrawableRes
    Integer getInactiveIcon(int position);

    Integer getIconSize();

    boolean doColorIcon();
}
