package com.salvadordalvik.something.list;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.salvadordalvik.fastlibrary.list.BaseFastItem;
import com.salvadordalvik.something.MainActivity;
import com.salvadordalvik.something.R;
import com.salvadordalvik.something.data.SomeDatabase;

/**
 * Created by matthewshepard on 1/22/14.
 */
public class ForumItem extends BaseFastItem<ForumItem.ForumHolder> {
    private Spanned title;
    private int parentId;
    private boolean starred, indent;

    public ForumItem(Cursor data, boolean indentSubforums) {
        super(R.layout.forum_item, data.getInt(data.getColumnIndex("forum_id")), true);
        title = Html.fromHtml(data.getString(data.getColumnIndex("forum_name")));
        parentId = data.getInt(data.getColumnIndex("parent_forum_id"));
        starred = !data.isNull(data.getColumnIndex("forum_starred"));
        indent = indentSubforums;
    }

    @Override
    public ForumHolder createViewHolder(View view) {
        return new ForumHolder(view);
    }

    @Override
    public void updateViewFromHolder(View view, ForumHolder holder) {
        holder.forumId = getId();
        holder.title.setText(title);
        if(parentId > 0 && indent){
            holder.indent.setVisibility(View.VISIBLE);
        }else{
            holder.indent.setVisibility(View.GONE);
        }
        holder.star.setImageResource(starred ? R.drawable.star : R.drawable.star_empty);
    }

    @Override
    public void onItemClick(Activity act, Fragment fragment) {
        ((MainActivity)act).showForum(id);
    }

    public Spanned getTitle() {
        return title;
    }

    protected static class ForumHolder implements View.OnClickListener {
        public TextView title;
        public ImageView indent, star;
        public int forumId = 0;
        private ForumHolder(View view){
            title = (TextView) view.findViewById(R.id.forum_item_title);
            indent = (ImageView) view.findViewById(R.id.forum_item_indent);
            star = (ImageView) view.findViewById(R.id.forum_item_star);
            star.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(forumId > 0){
                ContentValues cv = new ContentValues();
                cv.put("forum_id", forumId);
                if(SomeDatabase.getDatabase().insertRows(SomeDatabase.TABLE_STARRED_FORUM, SQLiteDatabase.CONFLICT_ABORT, cv) < 0){
                    SomeDatabase.getDatabase().deleteRows(SomeDatabase.TABLE_STARRED_FORUM, "forum_id=?", Integer.toString(forumId));
                    star.setImageResource(R.drawable.star_empty);
                }else{
                    star.setImageResource(R.drawable.star);
                }
            }
        }
    }
}
