package com.salvadordalvik.something.list;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.salvadordalvik.fastlibrary.list.BaseFastItem;
import com.salvadordalvik.something.R;

/**
 * Created by matthewshepard on 1/27/14.
 */
public abstract class MenuItem extends BaseFastItem<MenuItem.MenuHolder> implements View.OnClickListener {
    private String title;
    private int iconRes;

    public MenuItem(String title, int iconDrawableRes) {
        super(R.layout.menu_item);
        this.title = title;
        this.iconRes = iconDrawableRes;
    }

    @Override
    public void updateViewFromHolder(View view, MenuHolder holder) {
        holder.title.setText(title);
        holder.button.setOnClickListener(this);
        holder.button.setImageResource(iconRes);
    }

    public abstract void onButtonClick(View view);

    @Override
    public void onClick(View view) {
        onButtonClick(view);
    }

    @Override
    public MenuHolder createViewHolder(View view) {
        return new MenuHolder(view);
    }

    protected static class MenuHolder{
        public TextView title;
        public ImageView button;

        private MenuHolder(View view){
            this.title = (TextView) view.findViewById(R.id.menu_item_title);
            this.button = (ImageView) view.findViewById(R.id.menu_item_bookmarks);
        }
    }
}
