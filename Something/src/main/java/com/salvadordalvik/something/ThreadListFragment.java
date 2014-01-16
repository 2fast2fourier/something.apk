package com.salvadordalvik.something;

import android.os.Bundle;
import android.view.View;

import com.salvadordalvik.fastlibrary.FastFragment;

/**
 * Created by matthewshepard on 1/16/14.
 */
public class ThreadListFragment extends FastFragment {
    public ThreadListFragment() {
        super(R.layout.threadlist_fragment);
    }

    @Override
    public void viewCreated(View frag, Bundle savedInstanceState) {

    }

    @Override
    public void refreshData(boolean pullToRefresh, boolean staleRefresh) {

    }
}
