package net.fastfourier.something.list;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.salvadordalvik.fastlibrary.list.BaseFastItem;
import net.fastfourier.something.MainActivity;
import net.fastfourier.something.R;
import net.fastfourier.something.data.SomeDatabase;
import net.fastfourier.something.util.SomeTheme;

/**
 * Created by matthewshepard on 1/22/14.
 */
public class ForumItem extends BaseFastItem<ForumItem.ForumHolder> {
    public static final String[] DB_COLUMNS = {"forum_id", "forum_name", "parent_forum_id", "forum_starred", "category"};
    private final Spanned title;
    private final int parentId;
    private final boolean showStar;
    private boolean selected, starred;
    private final String category;

    public ForumItem(Cursor data, boolean indentSubforums, int[] columns, int selectedForumId) {
        super(data.getInt(columns[2]) > 0 && indentSubforums ? R.layout.subforum_item : R.layout.forum_item, data.getInt(columns[0]), true);
        this.title = Html.fromHtml(data.getString(columns[1]));
        this.parentId = data.getInt(columns[2]);
        this.starred = !data.isNull(columns[3]);
        this.selected = id == selectedForumId;
        this.showStar = true;
        this.category = data.getString(columns[4]);
    }

    public ForumItem(int id, String title, int parentId, boolean showStar, boolean starred, boolean indentSubforums, int selectedForumId) {
        super(R.layout.forum_item, id, true);
        this.title = Html.fromHtml(title);
        this.parentId = parentId;
        this.starred = starred;
        this.selected = id == selectedForumId;
        this.showStar = showStar;
        this.category = "";
    }

    @Override
    public ForumHolder createViewHolder(View view) {
        return new ForumHolder(view);
    }

    @Override
    public void updateViewFromHolder(View view, ForumHolder holder) {
        holder.forum = this;
        holder.title.setText(title);
        if(showStar){
            TypedValue star = new TypedValue();
            if(view.getContext().getTheme().resolveAttribute(starred ? R.attr.inlineStarIcon : R.attr.inlineEmptyStarIcon, star, false)){
                holder.star.setVisibility(View.VISIBLE);
                holder.star.setImageResource(star.data);
            }
        }else{
            holder.star.setVisibility(View.GONE);
        }
        view.setActivated(selected);
    }

    @Override
    public boolean onItemClick(Activity act, Fragment fragment) {
        act.startActivity(new Intent(act, MainActivity.class).putExtra("forum_id", id).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        return false;
    }

    public Spanned getTitle() {
        return title;
    }

    public boolean isStarred() {
        return starred;
    }

    public String getCategory() {
        return category;
    }

    private boolean toggleStar(){
        starred = toggleStar(getId());
        return starred;
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

    protected static class ForumHolder implements View.OnClickListener {
        public TextView title;
        public ImageView star;
        public ForumItem forum;
        private ForumHolder(View view){
            title = (TextView) view.findViewById(R.id.forum_item_title);
            star = (ImageView) view.findViewById(R.id.forum_item_star);
            star.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(forum != null && forum.getId() > 0){
                star.setImageResource(
                        SomeTheme.getThemeResource(
                                v.getContext(),
                                forum.toggleStar() ? R.attr.inlineStarIcon : R.attr.inlineEmptyStarIcon,
                                R.drawable.star)
                );
            }
        }
    }
}
