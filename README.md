# Android-PageIndicator (under construction)
##Synopsis
A library providing a PageIndicator View for ViewPager on Android. The Basic features are
- Dot views or Icons per Indicator. Can be mixed for example to show an icon for the current displayed pager page and dots for the other pages.
- Individual colors per indicator depending on active/inactive status. Can be distinguished by page index.
- Optional animating active page indicator parallel to scrolling the pages. This is compatible with mixed dots/icons indicators and their colors.
- Supporting circular looping ViewPagers by providing an abstract adapter class.

## Code Example

`PageIndicator` inherits from LinearLayout, so you can simlpy add it in your Layout XML file like this
```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <android.support.v4.view.ViewPager
        android:id="@+id/pager"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

    <de.f0rke.pageindicator.PageIndicator
        android:id="@+id/indicator"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true" />
</RelativeLayout>
```

The simplies usage of `PageIndicator` is to set it up with any `android.support.v4.view.ViewPager`, standard colors and no icons:
```java
ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
PageIndicator pageIndicator = (PageIndicator) rootView.findViewById(R.id.indicator);
pageIndicator.setupWithViewPager(viewPager, PageIndicator.Theme.LIGHT, null);
```
Pretty straight forward like the `android.support.design.widget.TabLayout` from Android Design libraries.

__IMPORTANT:__ 
Whatever `Fragment` you implement to display within a `ViewPager`, it needs to add a Tag flagged with the `R.id.POSITION` key (Defined by the library) attached ro it's root view. Otherwise the PageIndicator is not able to retrieve the pages index at setup and later on and will crash(Propper Exceptions will be added in a later version).

### Adding some magic

These Code snippets are taken from the Example App. It uses a `CircularPageAdapter` implementation to achieve circular scrolling of the ViewPager. It also handles animating the indicators from page to page with a mixture of dots and icons. I guess it shows somehow the capabilities of this library.
In your MainActivity you could use the Indicator like this
```java
ViewPager pager = (ViewPager) findViewById(R.id.pager);
List<ContentContainer> contentList = new ArrayList<SampleContentContainer>() {{
    add(new SampleContentContainer("Page One"));
    add(new SampleContentContainer("Page Two"));
    add(new SampleContentContainer("Page Three"));
}};
SamplePageAdapter adapter = new SamplePageAdapter(getSupportFragmentManager(), contentList);
PageIndicator indicator = (PageIndicator) findViewById(R.id.indicator);
pager.setAdapter(adapter);
pager.setPageTransformer(false, indicator);
indicator.setupWithCircularViewPager(pager, PageIndicator.Theme.LIGHT, null, adapter);
```

With a Adapter implemetation at least looking somewhat like this
```java
public class SamplePageAdapter extends CircularPagerAdapter<SampleContentContainer> {
    public SamplePageAdapter(FragmentManager fragmentManager, List<SampleContentContainer> contentContainers) {
        super(fragmentManager, contentContainers);
    }

    @Override
    protected Fragment getFragmentForItem(SampleContentContainer contentContainer, int index) {
        return SamplePageFragment.newInstance(contentContainer, index);
    }
}
```


## Motivation

This Project was created becaus in many App I was contributing or coding myself, an independent customizable PageIndicator was missing. I wanted a library that can be used across multiple projects and did not find a suitable existing one. So as a developer I started to implement my own one which by time became a little more complex than expected. So here we are.

## Installation
The library is available by import via maven.

Add the bintray repository to the project's root .gradle file:
```gradle
allprojects {
    repositories {
        .
        .
        .
        maven { url "https://dl.bintray.com/f0rke/pageindicator" }
    }
}
```

Add this to the dependencies of the `build.gradle` file of the module you want to use the `PageIndicator`.
```gradle
dependencies {
  .
  .
  .
  compile 'de.f0rke.pageindicator:pageindicator:1.0.2' 
}
``` 

If you don't want to use jCenter import, you can download and import the pageindicator module yourself.

## License
```
Copyright 2016 Moritz Köchig

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
