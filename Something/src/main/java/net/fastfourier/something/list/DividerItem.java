package net.fastfourier.something.list;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.salvadordalvik.fastlibrary.list.BaseFastItem;
import com.salvadordalvik.fastlibrary.util.FastUtils;

/**
 * Created by matthewshepard on 1/27/14.
 */
public class DividerItem extends BaseFastItem<DividerItem.DividerHolder> {
    private int textRes;
    private String text;

    public DividerItem(int layoutId, int textRes, String text) {
        super(layoutId, FastUtils.getSimpleUID(), false);
        this.textRes = textRes;
        this.text = text;
    }

    @Override
    public DividerHolder createViewHolder(View view) {
        return new DividerHolder(view, textRes);
    }

    @Override
    public void updateViewFromHolder(View view, DividerHolder holder) {
        holder.text.setText(text);
    }

    @Override
    public boolean onItemClick(Activity act, Fragment fragment) {
        return false;
    }

    protected static class DividerHolder{
        private TextView text;

        public DividerHolder(View view, int textRes) {
            text = (TextView) view.findViewById(textRes);
        }
    }
}
