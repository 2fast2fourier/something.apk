package com.salvadordalvik.something.list;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.app.Fragment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.salvadordalvik.fastlibrary.list.BaseFastItem;
import com.salvadordalvik.something.MainActivity;
import com.salvadordalvik.something.R;

/**
 * Created by matthewshepard on 1/17/14.
 */
public class ThreadItem extends BaseFastItem<ThreadItem.ThreadHolder> implements Parcelable{
    private String threadTitle, author, lastPost;
    private int unread, replies;
    private boolean bookmarked, closed;

    public ThreadItem(int id, String title, int unreadCount, int replies, String author, String lastPost, boolean bookmarked, boolean closed) {
        super(R.layout.thread_item, id, true);
        this.threadTitle = title;
        this.unread = unreadCount;
        this.replies = replies;
        this.author = author;
        this.lastPost = lastPost;
        this.bookmarked = bookmarked;
        this.closed = closed;
    }

    public ThreadItem(Parcel source) {
        super(R.layout.thread_item, source.readInt(), true);
        this.threadTitle = source.readString();
        this.unread = source.readInt();
        this.replies = source.readInt();
        this.author = source.readString();
        this.lastPost = source.readString();
        this.bookmarked = source.readByte() > 0;
        this.closed = source.readByte() > 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getId());
        dest.writeString(threadTitle);
        dest.writeInt(unread);
        dest.writeInt(replies);
        dest.writeString(author);
        dest.writeString(lastPost);
        dest.writeInt(bookmarked ? 1 : 0);
        dest.writeInt(closed ? 1 : 0);
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
        if(bookmarked){
            unreadBackground.setColor(Color.argb(255,53,102,147));
        }else{
            unreadBackground.setColor(Color.argb(255,0,0,0));
        }
        holder.unread.setAlpha(unread > 0 ? 1.0f : 0.5f);
        if(unread >= 0){
            holder.subtext.setText(" "+(replies/40+1)+" - Killed: "+lastPost);
        }else{
            holder.subtext.setText(" "+(replies/40+1)+" - Author: "+author);
        }
        view.setAlpha(closed ? 0.5f : 1f);
    }

    @Override
    public boolean onItemClick(Activity act, Fragment fragment) {
        if(unread >= 0){
            ((MainActivity)act).showThread(id);
        }else{
            ((MainActivity)act).showThread(id, 1);
        }
        return false;
    }

    @Override
    public ThreadHolder createViewHolder(View view) {
        return new ThreadHolder(view);
    }

    public void updateUnreadCount(int currentPage, int maxPage, int perPage) {
        replies = Math.max(replies, pageToIndex(maxPage, perPage));
        unread = Math.max(0, replies - pageToIndex(currentPage+1, perPage) + 1);
    }

    private static int pageToIndex(int page, int perPage) {
        return (page-1)*perPage;
    }

    public boolean isBookmarked() {
        return bookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        this.bookmarked = bookmarked;
    }

    protected static class ThreadHolder{
        public TextView title, subtext, unread;
        private ThreadHolder(View view){
            title = (TextView) view.findViewById(R.id.thread_item_title);
            subtext = (TextView) view.findViewById(R.id.thread_item_subtext);
            unread = (TextView) view.findViewById(R.id.thread_item_unread);
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
