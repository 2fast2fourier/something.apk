package net.fastfourier.something.list;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.salvadordalvik.fastlibrary.list.BaseFastItem;

import net.fastfourier.something.R;

/**
 * Created by matthewshepard on 1/27/14.
 */
public abstract class ForumHeaderItem extends BaseFastItem<ForumHeaderItem.MenuHolder> implements View.OnClickListener {
    private String title;
    private boolean selected = false;

    public ForumHeaderItem(String title) {
        super(R.layout.forum_header_item);
        this.title = title;
    }

    @Override
    public void updateViewFromHolder(View view, MenuHolder holder) {
        holder.title.setText(title);
        holder.button.setOnClickListener(this);
        view.setActivated(selected);
    }

    public void setSelected(boolean selected){
        this.selected = selected;
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
