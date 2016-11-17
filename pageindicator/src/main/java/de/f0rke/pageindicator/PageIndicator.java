package de.f0rke.pageindicator;

import android.animation.ArgbEvaluator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static android.support.v4.view.ViewPager.SCROLL_STATE_DRAGGING;
import static android.support.v4.view.ViewPager.SCROLL_STATE_IDLE;
import static android.support.v4.view.ViewPager.SCROLL_STATE_SETTLING;

/**
 * Created by moritzkochig on 11.08.16.
 *
 * @author f0rke
 */

public class PageIndicator extends LinearLayout implements ViewPager.PageTransformer {

    //<editor-fold desc="### Constants ###">
    // Default colors
    @ColorRes
    public static final int DEFAULT_LIGHT_COLOR = R.color.page_indicator_light;
    @ColorRes
    public static final int DEFAULT_DARK_COLOR = R.color.page_indicator_dark;

    //Debug TAG
    private static final String TAG = "CgPageIndicator";
    //</editor-fold>


    //<editor-fold desc="### Properties ###">
    // #############################################################################################
    // #############################################################################################
    //
    //                                            Properties
    //
    // #############################################################################################
    // #############################################################################################

    private ViewPager viewPager;
    private ColorProvider colorProvider;
    private IconProvider iconProvider;
    private final int dotSize = 8;
    private CircularPagerAdapter circularAdapter;
    private AtomicBoolean isTransformer = new AtomicBoolean(false);
    private ViewPager.PageTransformer transformer;
    private ViewPager.OnPageChangeListener onPageChangeListener;
    //</editor-fold>


    //<editor-fold desc="### Inherited LinearLayout Constructors ###">
    // #############################################################################################
    // #############################################################################################
    //
    //                               Inherited LinearLayout Constructors
    //
    // #############################################################################################
    // #############################################################################################

    public PageIndicator(Context context) {
        super(context);
    }

    public PageIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PageIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PageIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    //</editor-fold>


    //<editor-fold desc="### Setup methods ###">
    // #############################################################################################
    // #############################################################################################
    //
    //                                          Setup methods
    //
    // #############################################################################################
    // #############################################################################################

    /**
     * Setup the page indicator with a standard FragmentViewPager
     *
     * @param pager     The corresponding ViewPager
     * @param cProvider The color Provider for the indicator views or null if you want to use the
     *                  default theme of light and dark gray
     * @param iProvider The icon Provider for the indicator views or null if you don't want to
     *                  use icons
     */
    public void setupWithViewPager(@NonNull ViewPager pager, @Nullable ColorProvider cProvider, @Nullable IconProvider iProvider) {
        this.viewPager = pager;
        this.setColorProviderForTheme(cProvider);
        this.iconProvider = iProvider != null ? iProvider : getDefaultIconProvider();
        this.setup();
    }

    /**
     * Setup the page indicator with a FragmentViewPager that will loop endlessly
     *
     * @param pager     The corresponding ViewPager
     * @param cProvider The color Provider for the indicator views or null if you want to use the
     *                  default theme of light and dark gray
     * @param iProvider The icon Provider for the indicator views or null if you don't want to
     *                  use icons
     * @param adapter   The circular fragment adapter to provide circular functionality
     */
    public void setupWithCircularViewPager(@NonNull ViewPager pager, @Nullable ColorProvider cProvider, @Nullable IconProvider iProvider, CircularPagerAdapter adapter) {
        this.circularAdapter = adapter;
        this.setupWithViewPager(pager, cProvider, iProvider);
    }

    /**
     * Internal method for setting the color provider and making sure it is not null
     *
     * @param provider the provider to set or null for choosing default color theme
     */
    private void setColorProviderForTheme(ColorProvider provider) {
        if (provider == null) {
            colorProvider = getDefaultColorProvider();
        } else {
            colorProvider = provider;
        }
    }

    /**
     * Initializes the child views using the count of the ViewPagers adapter content count,
     */
    private void setup() {

        // Remove all child views for the case the setup method gets called again for resetting the
        // PageIndicators content
        removeAllViewsInLayout();

        // Get an inflater to inflate the indicator views
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Determine how many pages need an indicator
        int itemCount = viewPager.getAdapter().getCount();
        for (int i = 0; i < itemCount; i++) {

            // Setup indicator viewgroup (icon and dot)
            ViewGroup indicator = (ViewGroup) inflater.inflate(R.layout.single_indicator, this, false);

            // Link indicator with page index
            indicator.setTag(R.id.POSITION, i);
            final int finalI = i;

            // Register all child views for onClick events
            for (int j = 0; j < indicator.getChildCount(); j++) {
                indicator.getChildAt(j).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewPager.setCurrentItem(finalI, true);
                    }
                });
            }

            // Determine if the current indicator is the active one and set it up correspondingly
            boolean active = (this.circularAdapter != null ? i == 1 : i == 0);
            if (this.circularAdapter == null || (i > 0 && i < this.circularAdapter.getCount() - 1)) {
                int color = active ? colorProvider.getActiveColor(i) : colorProvider.getInactiveColor(i);
                Integer icon = active ? iconProvider.getActiveIcon(i) : iconProvider.getInactiveIcon(i);
                setupIndicator(indicator, color, icon, iconProvider.getIconSize());
            } else {
                //If this is a circular pager indicator, then hide first and last view
                indicator.setVisibility(INVISIBLE);
            }

            // Finished setting up the indicator view, now adding
            this.addView(indicator, i);
        }
        this.requestLayout();

        // Add page listeners for each case of not circular and circular
        onPageChangeListener = new IndicatorPageChangeListener();
        if (circularAdapter != null) {
            CircularPagerHandler handler = new CircularPagerHandler(viewPager);
            handler.setOnPageChangeListener(onPageChangeListener);
            viewPager.addOnPageChangeListener(handler);
        } else {
            viewPager.addOnPageChangeListener(onPageChangeListener);
        }

        // Preparing the page transformer for the case this PageIndicator is added to the pager
        // as transformer later
        this.transformer = new IndicatorViewTransformer();
    }

    /**
     * Apply the final values to the single indicator
     *
     * @param indicator The indicator to apply the parameters to
     * @param color     The color to set
     * @param icon      The icon to show
     * @param iconSize  The size of the icon to show
     */
    private void setupIndicator(ViewGroup indicator, @ColorInt int color, @DrawableRes Integer icon, Integer iconSize) {
        boolean dotVisible = icon == null;
        boolean iconVisible = !dotVisible;

        //setup dot
        CardView dotView = (CardView) indicator.findViewById(R.id.indicator_dot_8dp);
        dotView.setVisibility(dotVisible ? VISIBLE : INVISIBLE);
        dotView.setCardBackgroundColor(color);

        //setup icon
        ImageView iconView = (ImageView) indicator.findViewById(R.id.indicator_icon);
        iconSize = (/*iconVisible &&*/ iconSize != null) ? iconSize : dotSize;
        RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(iconSize, iconSize);
        if (iconProvider.doColorIcon()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                iconView.setImageTintList(new ColorStateList(new int[][]{new int[]{}}, new int[]{color}));
            } else {
                iconView.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            }
        }
        if (iconVisible) {
            iconView.setVisibility(VISIBLE);
            iconView.setImageResource(icon);
        } else {
            iconView.setVisibility(INVISIBLE);
        }
        iconView.setLayoutParams(iconParams);
    }
    //</editor-fold>


    //<editor-fold desc="### Proxy methods ###">
    // #############################################################################################
    // #############################################################################################
    //
    //                                         Proxy methods
    //
    // #############################################################################################
    // #############################################################################################

    /**
     * Provide a default colorProvider
     *
     * @return A Provider with the light default {@link Theme}
     */
    private ColorProvider getDefaultColorProvider() {
        return Theme.LIGHT;
    }

    @Override
    /**
     * Only forwards the parameters to the inner transformer
     */
    public void transformPage(View page, float position) {
        if (transformer != null) {
            transformer.transformPage(page, position);
        }
    }

    /**
     * Overriding the onMeasure method to make sure the page indicator stays always at the same
     * height although it's measured height would differ while animating page transformations
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //If the pager displays icons, we don't want to resize the views height but keep it always the same as max
        if (iconProvider != null) {
            Integer expandedIconSize = iconProvider.getIconSize();
            if (expandedIconSize != null) {
                int height;
                height = (int) (expandedIconSize * getDensity() + this.getPaddingBottom() + this.getPaddingTop());
                setMeasuredDimension(widthMeasureSpec, height);
            }
        }
    }

    /**
     * Simple helper function
     *
     * @return the Devices display density
     */
    private double getDensity() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return (double) metrics.density;
    }

    /**
     * Provides a default icon Provider
     *
     * @return A provider which provides null values
     */
    private IconProvider getDefaultIconProvider() {
        return new IconProvider() {
            @Override
            public Integer getActiveIcon(int position) {
                return null;
            }

            @Override
            public Integer getInactiveIcon(int position) {
                return null;
            }

            @Override
            public Integer getIconSize() {
                return null;
            }

            @Override
            public boolean doColorIcon() {
                return false;
            }
        };
    }
    //</editor-fold>


    //<editor-fold desc="### Auto Play ###">
    // #############################################################################################
    // #############################################################################################
    //
    //                                          Auto Play
    //
    // #############################################################################################
    // #############################################################################################
    private Timer timer = new Timer();
    private AutoPlayTimerTask task = null;
    private long autoScrollInterval = 0;
    private boolean directionForward = true;
    private Handler autoPlayHandler = null;
    private final AtomicReference<AutoPlayState> currentAutoPlayState = new AtomicReference<>(AutoPlayState.NOT_INITIALIZED);
    private final AtomicReference<AutoPlayLogLevel> autoPlayDebugMode = new AtomicReference<>(AutoPlayLogLevel.NONE);

    /* TODO:
     * Detect view moving to background to not swipe then. Also don't operate on dead Views if
     * the developer created memory leaks when not properly reusing or destroying views during
     * view state changes
     */
    private final AtomicBoolean keepPlayingInBackground = new AtomicBoolean(false);

    public enum AutoPlayState {
        NOT_INITIALIZED, INITIALIZED, PLAYING, PAUSED, STOPPED
    }

    public enum AutoPlayLogLevel {
        NONE(0), LOW(1), HIGH(2);

        final int value;

        AutoPlayLogLevel(int i) {
            this.value = i;
        }
    }

    public void initializeAutoPlay(long interval, boolean directionForward) {
        this.autoPlayHandler = new Handler(Looper.getMainLooper());
        this.timer = new Timer();
        this.autoScrollInterval = interval;
        this.directionForward = directionForward;
        this.currentAutoPlayState.set(AutoPlayState.INITIALIZED);
    }

    public void setAutoPlayDebugMode(AutoPlayLogLevel level) {
        this.autoPlayDebugMode.set(level);
    }

    public void startAutoPlay() {
        if (!new ArrayList<AutoPlayState>() {{
            add(AutoPlayState.NOT_INITIALIZED);
            add(AutoPlayState.PLAYING);
        }}.contains(currentAutoPlayState.get())) {
            AutoPlayLogLevel logLevel = this.autoPlayDebugMode.get();
            AutoPlayState state = this.currentAutoPlayState.get();
            if (state == AutoPlayState.PAUSED) {
                if (logLevel == AutoPlayLogLevel.HIGH) {
                    Log.d(TAG, "Resuming AutoPlay");
                }
            } else {
                if (logLevel.value > AutoPlayLogLevel.NONE.value) {
                    Log.d(TAG, "Starting AutoPlay");
                }
            }
            if (task == null) {
                task = new AutoPlayTimerTask();
            }
            timer.schedule(task, autoScrollInterval, autoScrollInterval);
            this.currentAutoPlayState.set(AutoPlayState.PLAYING);
        }
    }

    public void stopAutoPlay() {
        stopAutoPlay(false);
    }

    private void stopAutoPlay(boolean automatic) {
        if (task != null) {
            task.cancel();
            task = null;
        }
        AutoPlayLogLevel logLevel = this.autoPlayDebugMode.get();
        if (automatic) {
            if (logLevel == AutoPlayLogLevel.HIGH) {
                Log.d(TAG, "Pausing AutoPlay due interaction");
            }
            this.currentAutoPlayState.set(AutoPlayState.PAUSED);
        } else {
            if (logLevel.value > AutoPlayLogLevel.NONE.value) {
                Log.d(TAG, "Stopping AutoPlay");
            }
            this.currentAutoPlayState.set(AutoPlayState.STOPPED);
        }
    }

    public void disableAutoPlay() {
        timer.cancel();
        autoScrollInterval = 0;
        directionForward = true;
        task = null;
        timer = new Timer();
        autoPlayHandler = null;

        if (this.autoPlayDebugMode.get().value > AutoPlayLogLevel.NONE.value) {
            Log.d(TAG, "Disabled AutoPlay. To restart it needs to be re-initialized.");
        }
        this.currentAutoPlayState.set(AutoPlayState.NOT_INITIALIZED);
    }

    private class AutoPlayTimerTask extends TimerTask {
        @Override
        public void run() {
            AutoPlayLogLevel logLevel = autoPlayDebugMode.get();
            if (autoPlayHandler != null && viewPager != null) {
                autoPlayHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (viewPager.getVisibility() == View.VISIBLE) {
                            viewPager.setCurrentItem(viewPager.getCurrentItem() + (directionForward ? 1 : -1), true);
                        }
                    }
                });
                if (logLevel == AutoPlayLogLevel.HIGH) {
                    Log.d(TAG, "AutoPlay performed");
                }
            } else {
                if (viewPager == null) {
                    if (logLevel.value > AutoPlayLogLevel.NONE.value) {
                        Log.e(TAG, "Trying to perform AutoPlay but viewPager instance has gone. -> disabling AutoPlay");
                    }
                } else {
                    if (logLevel.value > AutoPlayLogLevel.NONE.value) {
                        Log.e(TAG, "Trying to perform AutoPlay thus Handler instance is null. -> disabling AutoPlay");
                    }
                }
                disableAutoPlay();
            }
        }
    }
    //</editor-fold>


    //<editor-fold desc="### Encapsulated inner classes ###">
    // #############################################################################################
    // #############################################################################################
    //
    //                                  Encapsulated inner classes
    //
    // #############################################################################################
    // #############################################################################################

    /**
     * This Transformer animates indicator color, cross fading dot and icon within indicator view,
     * shrinking and inflating icon size and supports circular swiping
     */
    private class IndicatorViewTransformer implements ViewPager.PageTransformer {

        @Override
        /**
         * Transformers main method
         */
        public void transformPage(View page, float position) {

            // Set the transformer flag
            isTransformer.compareAndSet(false, true);

            // Get the pageIndex of the current fragment
            Object tag = page.getTag(R.id.POSITION);
            if (tag instanceof Integer) {
                int pageIndex = (int) tag;

                // Get the indicator view to manipulate or return if the current page is a fake
                // page for circular paging
                ViewGroup indicator = getVirtualIndicator(pageIndex);
                if (indicator == null) {
                    return;
                }

                // Translate the indicator index if there are fake pages
                int indicatorIndex;
                if (circularAdapter != null) {
                    if (pageIndex > 0 && pageIndex < circularAdapter.getCount() - 1) {
                        indicatorIndex = pageIndex - 1;
                    } else {
                        return;
                    }
                } else {
                    indicatorIndex = pageIndex;
                }

                // Get calculated new values and apply them to indicator view
                float virtualPosition = getVirtualPosition(position);
                float absPosition = Math.abs(virtualPosition);
                Integer icon = absPosition <= 0.5f ? iconProvider.getActiveIcon(indicatorIndex) : iconProvider.getInactiveIcon(indicatorIndex);
                float alpha = getAlpha(absPosition, indicatorIndex);
                indicator.setAlpha(alpha);
                int iconSize = getIconSize(alpha, absPosition, indicatorIndex);
                int color = getColor(absPosition, indicatorIndex);
                setupIndicator(indicator, color, icon, iconSize);

            } else {
                Log.e(TAG, "Not able to retrieve page index from Pager View");
            }
        }

        // ################################# Helper methods ########################################

        /**
         * Calculates the color for the given position and page indicator index
         *
         * @param absPosition The absolute value of the relative position of the page within
         *                    the ViewPager
         * @param index       The index of the indicator view within the PageIndicator
         * @return The calculated color considering colorProvider and scrollPosition
         */
        @ColorInt
        private int getColor(final float absPosition, final int index) {
            int color;
            if (absPosition == 0f) {
                color = colorProvider.getActiveColor(index);
            } else if (absPosition > 0.0f && absPosition < 1.0f) {
                color = (Integer) new ArgbEvaluator().evaluate(absPosition, colorProvider.getActiveColor(index), colorProvider.getInactiveColor(index));
            } else {
                color = colorProvider.getInactiveColor(index);
            }
            return color;
        }

        /**
         * Calculates the icon size for the absolute position and indicator index considering
         * the alpha value of the view
         *
         * @param alpha       alpha value of the indicator
         * @param absPosition absolute value of the relative position in ViewPager
         * @param index       indicator view index
         * @return The calculated IconSize
         */
        private int getIconSize(final float alpha, final float absPosition, int index) {
            int expandedIconSize = (int) ((iconProvider.getIconSize() != null ? iconProvider.getIconSize() : dotSize) * getDensity());
            int iconSize;
            boolean hasIcon = iconProvider.getActiveIcon(index) != null || iconProvider.getInactiveIcon(index) != null;

            if (hasIcon)
                if (absPosition == 0.0f) {
                    iconSize = expandedIconSize;
                } else if (absPosition > 0.5f && absPosition <= 1f) {
                    iconSize = (int) ((expandedIconSize + (dotSize * getDensity())) - (int) (expandedIconSize * alpha));
                    if (iconSize > expandedIconSize) {
                        iconSize = expandedIconSize;
                    }
                } else if (absPosition > 0f && absPosition < 0.5f) {
                    iconSize = (int) ((expandedIconSize) * alpha);
                    if (iconSize > expandedIconSize) {
                        iconSize = expandedIconSize;
                    }
                } else if (absPosition == 0.5f) {
                    iconSize = (int) ((expandedIconSize + (dotSize * getDensity())) * alpha);
                    iconSize = iconSize == 0f ? (int) ((expandedIconSize + (dotSize * getDensity())) / 2) : iconSize;
                } else {
                    iconSize = (int) ((dotSize * getDensity()));
                }
            else {
                iconSize = dotSize;
            }
            return iconSize;
        }

        /**
         * Calculates the alpha value which should be applied to the indicator view at the given
         * index with the relative position
         *
         * @param absPosition Absolute value of the relative position in ViewPager
         * @param index       Indicator view index
         * @return The calculated alpha value
         */
        private float getAlpha(final float absPosition, final int index) {

            boolean hasIcon = iconProvider.getActiveIcon(index) != null || iconProvider.getInactiveIcon(index) != null;
            float alpha;

            if (hasIcon) {
                if (absPosition == 0.0f) {
                    alpha = 1f;
                } else if (absPosition > 0.5f && absPosition <= 1f) {
                    alpha = (absPosition - 0.5f) * 2;
                } else if (absPosition > 0f && absPosition < 0.5f) {
                    alpha = (0.5f - absPosition) * 2;
                } else if (absPosition == 0.5f) {
                    alpha = (0.5f - absPosition) * 2;
                } else {
                    alpha = 1f;
                }
            } else {
                alpha = 1f;
            }
            return alpha;
        }

        /**
         * Gets the visible (to manipulate) Indicator view for the given index
         *
         * @param index page index
         * @return The corresponding indicator or null if fake indicator for circular pager
         */
        private ViewGroup getVirtualIndicator(int index) {
            if (circularAdapter != null) {
                //Don't animate last and first indicator if circular viewpager
                //Manipulate index instead to animate
                if (index == 0 || index == circularAdapter.getCount() - 1) {
                    return null;
                }
            }
            return (ViewGroup) getChildAt(index);
        }

        /**
         * Get the virtual to calculate the other values with. This is needed if the PageIndicator
         * is indicator for a circular pager to avoid animating fake pages.
         *
         * @param actualPosition The actual position of the page
         * @return The virtual position of the page to use for calculating the animating values
         */
        private float getVirtualPosition(float actualPosition) {
            float virtualPosition;

            if (circularAdapter != null) {
                if (actualPosition > (circularAdapter.getCountWithoutFakePages() - 1)) {
                    //first swiping left: need to animate last
                    virtualPosition = 1 - (actualPosition % (circularAdapter.getCountWithoutFakePages() - 1));
                } else if (actualPosition < -(circularAdapter.getCountWithoutFakePages() - 1)) {
                    //last swiping right: need to animate first
                    virtualPosition = 1 + actualPosition % (circularAdapter.getCountWithoutFakePages() - 1);
                } else {
                    virtualPosition = actualPosition;
                }
            } else {
                virtualPosition = actualPosition;
            }
            return virtualPosition;
        }
    }

    /**
     * TODO: write documentation
     */
    private class IndicatorPageChangeListener implements ViewPager.OnPageChangeListener {

        /**
         * Not used by this implementation.
         */
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            if (!isTransformer.get()) {
                for (int i = 0; i < getChildCount(); i++) {
                    ViewGroup indicator = (ViewGroup) getChildAt(i);
                    boolean active = position == (int) indicator.getTag(R.id.POSITION);

                    setupIndicator(indicator,
                            active ? colorProvider.getActiveColor(i) : colorProvider.getInactiveColor(i),
                            active ? iconProvider.getActiveIcon(i) : iconProvider.getInactiveIcon(i),
                            128);

                    CardView dot = (CardView) indicator.findViewById(R.id.indicator_dot_8dp);
                    dot.setCardBackgroundColor(active ? colorProvider.getActiveColor(i) : colorProvider.getInactiveColor(i));
                }
                requestLayout();
            }
        }

        /**
         * Not used by this implementation.
         */
        @Override
        public void onPageScrollStateChanged(int state) {
            switch (state) {
                case SCROLL_STATE_IDLE:
                    if (currentAutoPlayState.get() == AutoPlayState.PAUSED) {
                        startAutoPlay();
                    }
                    break;
                case SCROLL_STATE_DRAGGING:
                case SCROLL_STATE_SETTLING:
                    if (!new ArrayList<AutoPlayState>() {{
                        add(AutoPlayState.NOT_INITIALIZED);
                        add(AutoPlayState.PAUSED);
                        add(AutoPlayState.STOPPED);
                    }}.contains(currentAutoPlayState.get())) {
                        stopAutoPlay(true);
                    }
                    break;
            }
        }
    }

    /**
     * This enum implements two simple grayscale ColorProviders
     */
    public enum Theme implements ColorProvider {
        DARK, LIGHT;

        @Override
        public int getActiveColor(int position) {
            if (this == DARK) {
                return 0xFFDCDCDC;
            } else {
                return 0xFF5C5C5C;
            }
        }

        @Override
        public int getInactiveColor(int position) {
            if (this == DARK) {
                return 0xFF5C5C5C;
            } else {
                return 0xFFDCDCDC;
            }
        }
    }
    //</editor-fold>
}


