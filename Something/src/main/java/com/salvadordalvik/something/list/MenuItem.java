package com.salvadordalvik.something.list;

import android.view.View;
import android.widget.TextView;

import com.salvadordalvik.fastlibrary.list.BaseFastItem;
import com.salvadordalvik.something.R;

/**
 * Created by matthewshepard on 1/27/14.
 */
public abstract class MenuItem extends BaseFastItem<MenuItem.MenuHolder> {
    private String title;

    public MenuItem(String title) {
        super(R.layout.menu_item);
        this.title = title;
    }

    @Override
    public void updateViewFromHolder(View view, MenuHolder holder) {
        holder.title.setText(title);
    }

    @Override
    public MenuHolder createViewHolder(View view) {
        return new MenuHolder(view);
    }

    protected static class MenuHolder{
        public TextView title;

        private MenuHolder(View view){
            this.title = (TextView) view.findViewById(R.id.menu_item_title);
        }
    }
}
