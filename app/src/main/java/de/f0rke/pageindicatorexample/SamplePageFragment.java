package de.f0rke.pageindicatorexample;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by moritzkochig on 07.09.16.
 *
 * @author f0rke
 */
public class SamplePageFragment extends Fragment {

    private static final String CONTENT_KEY = "content";
    private static final String PAGE_INDEX_KEY = "pageIndex";
    private ContentContainer content;
    private int pageIndex;

    public static SamplePageFragment newInstance(ContentContainer content, int pageIndex) {

        Bundle args = new Bundle();
        args.putParcelable(CONTENT_KEY, content);
        args.putInt(PAGE_INDEX_KEY, pageIndex);

        SamplePageFragment fragment = new SamplePageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @CallSuper
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle;
        if (savedInstanceState != null) {
            bundle = savedInstanceState;
        } else {
            bundle = getArguments();
        }
        retrieveDataFromBundle(bundle);
    }

    @Override
    @CallSuper
    public void onSaveInstanceState(Bundle outState) {
        this.writeDataToBundle(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    @CallSuper
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            retrieveDataFromBundle(savedInstanceState);
        }
    }

    @CallSuper
    private void retrieveDataFromBundle(Bundle bundle) {
        this.content = bundle.getParcelable(CONTENT_KEY);
        this.pageIndex = bundle.getInt(PAGE_INDEX_KEY);
    }

    @CallSuper
    private Bundle writeDataToBundle(@Nullable Bundle bundle) {
        Bundle b;
        if (bundle != null) {
            b = bundle;
        } else {
            b = new Bundle();
        }
        b.putParcelable(CONTENT_KEY, content);
        b.putInt(PAGE_INDEX_KEY, pageIndex);
        return b;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sample_page, container, false);
        rootView.setTag(R.id.POSITION, pageIndex);
        ((TextView) rootView.findViewById(R.id.sample_text)).setText(content.getText());
        return rootView;
    }
}
