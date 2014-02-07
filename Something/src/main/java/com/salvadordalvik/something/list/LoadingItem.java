package com.salvadordalvik.something.list;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.salvadordalvik.fastlibrary.list.BaseFastItem;
import com.salvadordalvik.fastlibrary.util.FastUtils;
import com.salvadordalvik.something.R;

/**
 * Created by matthewshepard on 2/2/14.
 */
public class LoadingItem extends BaseFastItem<LoadingItem.LoadingHolder> {
    private String message;

    public LoadingItem(String message) {
        super(R.layout.loading_item, FastUtils.getSimpleUID(), false);
        this.message = message;
    }

    @Override
    public LoadingHolder createViewHolder(View view) {
        return new LoadingHolder(view);
    }

    @Override
    public void updateViewFromHolder(View view, LoadingHolder holder) {
        holder.message.setText(message);
    }

    @Override
    public boolean onItemClick(Activity act, Fragment fragment) {
        return false;
    }

    protected static class LoadingHolder{
        public TextView message;
        public LoadingHolder(View view) {
            message = (TextView) view.findViewById(R.id.loading_item_message);
        }
    }
}
