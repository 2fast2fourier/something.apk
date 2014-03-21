package net.fastfourier.something.list;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.app.Fragment;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.salvadordalvik.fastlibrary.list.BaseFastItem;
import com.salvadordalvik.fastlibrary.request.FastVolley;
import net.fastfourier.something.MainActivity;

import net.fastfourier.something.R;
import net.fastfourier.something.util.SomePreferences;
import net.fastfourier.something.util.SomeTheme;

/**
 * Created by matthewshepard on 1/17/14.
 */
public class ThreadItem extends BaseFastItem<ThreadItem.ThreadHolder> implements Parcelable{
    private String threadTitle, author, lastPost, tagUrl;
    private int unread, replies, bookmark;
    private boolean closed, selected;

    public ThreadItem(int id, String title, int unreadCount, int replies, String author, String lastPost, int bookmarked, boolean closed, String tagUrl) {
        super(R.layout.thread_item, id, true);
        this.threadTitle = title;
        this.unread = unreadCount;
        this.replies = replies;
        this.author = author;
        this.lastPost = lastPost;
        this.bookmark = bookmarked;
        this.closed = closed;
        this.tagUrl = tagUrl;
        this.selected = false;
    }

    public ThreadItem(Parcel source) {
        super(R.layout.thread_item, source.readInt(), true);
        this.threadTitle = source.readString();
        this.unread = source.readInt();
        this.replies = source.readInt();
        this.author = source.readString();
        this.lastPost = source.readString();
        this.bookmark = source.readInt();
        this.closed = source.readInt() > 0;
        this.tagUrl = source.readString();
        this.selected = false;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getId());
        dest.writeString(threadTitle);
        dest.writeInt(unread);
        dest.writeInt(replies);
        dest.writeString(author);
        dest.writeString(lastPost);
        dest.writeInt(bookmark);
        dest.writeInt(closed ? 1 : 0);
        dest.writeString(tagUrl);
    }

    @Override
    public void updateViewFromHolder(View view, ThreadHolder holder) {
        holder.title.setText(threadTitle);
        if(unread >= 0){
            holder.unread.setVisibility(View.VISIBLE);
            holder.unread.setText(Integer.toString(unread));
        }else{
            holder.unread.setVisibility(View.GONE);
        }
        GradientDrawable unreadBackground = (GradientDrawable) holder.unread.getBackground();
        switch (bookmark){
            case 0:
                unreadBackground.setColor(SomeTheme.bookmark_unread);
                break;
            case 1:
                unreadBackground.setColor(SomeTheme.bookmark_normal);
                break;
            case 2:
                unreadBackground.setColor(SomeTheme.bookmark_red);
                break;
            case 3:
                unreadBackground.setColor(SomeTheme.bookmark_gold);
                break;
        }
        holder.unread.setAlpha(unread > 0 ? 1.0f : 0.5f);
        if(unread >= 0){
            holder.subtext.setText(" "+(replies/SomePreferences.threadPostPerPage+1)+" - Killed By: "+lastPost);
        }else{
            holder.subtext.setText(" "+(replies/SomePreferences.threadPostPerPage+1)+" - OP: "+author);
        }
        if(!TextUtils.isEmpty(tagUrl)){
            holder.tagImage.setImageUrl(tagUrl, FastVolley.getImageLoader());
            holder.tagImage.setVisibility(View.VISIBLE);
        }else{
            holder.tagImage.setVisibility(View.GONE);
        }

        view.setAlpha(closed ? 0.5f : 1f);
        view.setActivated(selected);
    }

    @Override
    public boolean onItemClick(Activity act, Fragment fragment) {
        act.startActivity(
                new Intent(act, MainActivity.class)
                        .putExtra("thread_id", id)
                        .putExtra("thread_page", unread >= 0 ? 0 : 1)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        return false;
    }

    @Override
    public ThreadHolder createViewHolder(View view) {
        return new ThreadHolder(view);
    }

    public void updateUnreadCount(int currentPage, int maxPage, int perPage) {
        if(unread < 0){
            replies = Math.max(replies, pageToIndex(maxPage, perPage));
            unread = Math.max(0, replies - pageToIndex(currentPage+1, perPage) + 1);
        }else{
            replies = Math.max(replies, pageToIndex(maxPage, perPage));
            unread = Math.min(unread, Math.max(0, replies - pageToIndex(currentPage+1, perPage) + 1));
        }
    }

    private static int pageToIndex(int page, int perPage) {
        return (page-1)*perPage;
    }

    public boolean isBookmarked() {
        return bookmark > 0;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmark = bookmarked ? 1 : 0;
    }

    public String getTitle() {
        return threadTitle;
    }

    public void markUnread() {
        unread = -1;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
    }

    protected static class ThreadHolder{
        public TextView title, subtext, unread;
        public NetworkImageView tagImage;
        private ThreadHolder(View view){
            title = (TextView) view.findViewById(R.id.thread_item_title);
            subtext = (TextView) view.findViewById(R.id.thread_item_subtext);
            unread = (TextView) view.findViewById(R.id.thread_item_unread);
            tagImage = (NetworkImageView) view.findViewById(R.id.thread_item_tagimg);
        }
    }

    //PARCEL

    public static final Parcelable.Creator<ThreadItem> CREATOR = new Parcelable.Creator<ThreadItem>(){

        @Override
        public ThreadItem createFromParcel(Parcel source) {
            return new ThreadItem(source);
        }

        @Override
        public ThreadItem[] newArray(int size) {
            return new ThreadItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
