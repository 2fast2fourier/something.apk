package com.salvadordalvik.something.list;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

import com.salvadordalvik.fastlibrary.list.BaseFastItem;
import com.salvadordalvik.fastlibrary.util.FastUtils;

/**
 * Created by matthewshepard on 1/27/14.
 */
public class StubItem extends BaseFastItem<StubItem.StubHolder> {


    public StubItem(int layoutId) {
        super(layoutId, FastUtils.getSimpleUID(), false);
    }

    @Override
    public StubHolder createViewHolder(View view) {
        return new StubHolder();
    }

    @Override
    public void updateViewFromHolder(View view, StubHolder holder) {

    }

    @Override
    public void onItemClick(Activity act, Fragment fragment) {

    }

    protected static class StubHolder{

    }
}
