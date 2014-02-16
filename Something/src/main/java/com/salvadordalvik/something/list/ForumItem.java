package com.salvadordalvik.something.list;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v4.app.Fragment;
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
    public static final String[] DB_COLUMNS = {"forum_id", "forum_name", "parent_forum_id", "forum_starred"};
    private final Spanned title;
    private final int parentId;
    private final boolean starred, indent, selected, showStar;

    public ForumItem(Cursor data, boolean indentSubforums, int[] columns, int selectedForumId) {
        super(R.layout.forum_item, data.getInt(columns[0]), true);
        this.title = Html.fromHtml(data.getString(columns[1]));
        this.parentId = data.getInt(columns[2]);
        this.starred = !data.isNull(columns[3]);
        this.indent = indentSubforums;
        this.selected = id == selectedForumId;
        this.showStar = true;
    }

    public ForumItem(int id, String title, int parentId, boolean showStar, boolean starred, boolean indentSubforums, int selectedForumId) {
        super(R.layout.forum_item, id, true);
        this.title = Html.fromHtml(title);
        this.parentId = parentId;
        this.starred = starred;
        this.indent = indentSubforums;
        this.selected = id == selectedForumId;
        this.showStar = showStar;
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
        if(showStar){
            holder.star.setVisibility(View.VISIBLE);
            holder.star.setImageResource(starred ? R.drawable.star : R.drawable.star_empty);
        }else{
            holder.star.setVisibility(View.GONE);
        }
        if(selected){
            view.setBackgroundColor(Color.rgb(11,11,11));
        }else{
            view.setBackgroundResource(0);
        }
    }

    @Override
    public boolean onItemClick(Activity act, Fragment fragment) {
        ((MainActivity)act).showForum(id);
        return false;
    }

    public Spanned getTitle() {
        return title;
    }

    public boolean isStarred() {
        return starred;
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
                if(toggleStar(forumId)){
                    star.setImageResource(R.drawable.star);
                }else{
                    star.setImageResource(R.drawable.star_empty);
                }
            }
        }
    }

    public static boolean toggleStar(int forumId){
        ContentValues cv = new ContentValues();
        cv.put("forum_id", forumId);
        if(SomeDatabase.getDatabase().deleteRows(SomeDatabase.TABLE_STARRED_FORUM, "forum_id=?", Integer.toString(forumId)) > 0){
            return false;
        }else{
            SomeDatabase.getDatabase().insertRows(SomeDatabase.TABLE_STARRED_FORUM, SQLiteDatabase.CONFLICT_IGNORE, cv);
            return true;
        }
    }
}
