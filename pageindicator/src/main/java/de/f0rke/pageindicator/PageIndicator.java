package de.f0rke.pageindicator;

import android.animation.ArgbEvaluator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by moritzkochig on 11.08.16.
 *
 * @author Moritz Köchig
 *         © mobile concepts GmbH 2016
 */

public class PageIndicator extends LinearLayout implements ViewPager.PageTransformer {

    @ColorRes
    public static final int DEFAULT_LIGHT_COLOR = R.color.page_indicator_light;
    @ColorRes
    public static final int DEFAULT_DARK_COLOR = R.color.page_indicator_dark;

    private static final String TAG = "CgPageIndicator";

    private ViewPager viewPager;
    private ColorProvider colorProvider;
    private IconProvider iconProvider;
    private final int dotSize = 8;
    private CircularPagerAdapter circularAdapter;
    private AtomicBoolean isTransformer = new AtomicBoolean(false);
    private ViewPager.PageTransformer transformer;
    private ViewPager.OnPageChangeListener onPageChangeListener;

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

    public void setupWithViewPager(@NonNull ViewPager pager, @Nullable ColorProvider cProvider, @Nullable IconProvider iProvider) {
        this.viewPager = pager;
        setColorProviderForTheme(cProvider);
        this.iconProvider = iProvider != null ? iProvider : getDefaultIconProvider();
        setup();
    }

    public void setupWithCircularViewPager(@NonNull ViewPager pager, @Nullable ColorProvider cProvider, @Nullable IconProvider iProvider, CircularPagerAdapter adapter) {
        this.circularAdapter = adapter;
        setupWithViewPager(pager, cProvider, iProvider);
    }

    private void setColorProviderForTheme(ColorProvider provider) {
        if (provider == null) {
            colorProvider = getDefaultColorProvider();
        } else {
            colorProvider = provider;
        }
    }

    private ColorProvider getDefaultColorProvider() {
        return Theme.LIGHT;
    }

    private void setup() {
        removeAllViewsInLayout();
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        int itemCount = viewPager.getAdapter().getCount();
        for (int i = 0; i < itemCount; i++) {

            //setup group
            ViewGroup indicator = (ViewGroup) inflater.inflate(R.layout.single_indicator, this, false);
            indicator.setTag(R.id.POSITION, i);
            final int finalI = i;

            for (int j = 0; j < indicator.getChildCount(); j++) {
                indicator.getChildAt(j).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        viewPager.setCurrentItem(finalI, true);
                    }
                });
            }

            boolean active = (this.circularAdapter != null ? i == 1 : i == 0);
            if (this.circularAdapter == null || (i > 0 && i < this.circularAdapter.getCount() - 1)) {
                int color = active ? colorProvider.getActiveColor(i) : colorProvider.getInactiveColor(i);
                Integer icon = active ? iconProvider.getActiveIcon(i) : iconProvider.getInactiveIcon(i);

                setupIndicator(indicator, color, icon, iconProvider.getIconSize());
            } else {
                indicator.setVisibility(INVISIBLE);
            }

            //finish
            this.addView(indicator, i);
        }
        this.requestLayout();

        onPageChangeListener = new IndicatorPageChangeListener();

        if (circularAdapter != null) {
            CircularPagerHandler handler = new CircularPagerHandler(viewPager);
            handler.setOnPageChangeListener(onPageChangeListener);
            viewPager.addOnPageChangeListener(handler);
        } else {
            viewPager.addOnPageChangeListener(onPageChangeListener);
        }
        this.transformer = new IndicatorViewTransformer();
    }

    public enum Theme implements ColorProvider {
        DARK, LIGHT;

        @Override
        public int getActiveColor(int position) {
            if (this == DARK) {
                return DEFAULT_LIGHT_COLOR;
            } else {
                return DEFAULT_DARK_COLOR;
            }
        }

        @Override
        public int getInactiveColor(int position) {
            if (this == DARK) {
                return DEFAULT_DARK_COLOR;
            } else {
                return DEFAULT_LIGHT_COLOR;
            }
        }

    }

    @Override
    public void transformPage(View page, float position) {
        if (transformer != null) {
            transformer.transformPage(page, position);
        }
    }

    private void setupIndicator(ViewGroup indicator, int color, Integer icon, Integer iconSize) {
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

    private double getDensity() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return (double) metrics.density;
    }

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

    private class IndicatorViewTransformer implements ViewPager.PageTransformer {

        @Override
        public void transformPage(View page, float position) {

            isTransformer.compareAndSet(false, true);
            Object tag = page.getTag(R.id.POSITION);
            if (tag instanceof Integer) {
                int pageIndex = (int) tag;
                ViewGroup indicator = getVirtualIndicator(pageIndex);
                if (indicator == null) {
                    return;
                }
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

    private class IndicatorPageChangeListener implements ViewPager.OnPageChangeListener {

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
                    dot.setCardBackgroundColor(ContextCompat.getColor(getContext(), active ? colorProvider.getActiveColor(i) : colorProvider.getInactiveColor(i)));
                }
                requestLayout();
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    }
}


