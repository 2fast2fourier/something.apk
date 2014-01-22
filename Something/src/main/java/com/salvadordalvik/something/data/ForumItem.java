package com.salvadordalvik.something.data;

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import com.salvadordalvik.fastlibrary.list.BaseFastItem;
import com.salvadordalvik.something.MainActivity;
import com.salvadordalvik.something.R;

/**
 * Created by matthewshepard on 1/22/14.
 */
public class ForumItem extends BaseFastItem<ForumItem.ForumHolder> {
    private Spanned title;

    public ForumItem(int id, String title) {
        super(R.layout.thread_item, id, 0, true);
        this.title = Html.fromHtml(title);
    }

    public ForumItem(Cursor data) {
        super(R.layout.thread_item, data.getInt(data.getColumnIndex("forum_id")), 0, true);
        title = Html.fromHtml(data.getString(data.getColumnIndex("forum_name")));
    }

    @Override
    public ForumHolder createViewHolder(View view) {
        return new ForumHolder(view);
    }

    @Override
    public void updateViewFromHolder(View view, ForumHolder holder) {
        holder.title.setText(title);
    }

    @Override
    public void onItemClick(Activity act, Fragment fragment) {
        ((MainActivity)act).showForum(id);
    }

    protected static class ForumHolder{
        public TextView title;
        private ForumHolder(View view){
            title = (TextView) view.findViewById(R.id.thread_item_title);
        }
    }
}
