package com.salvadordalvik.something.list;

import android.app.Activity;
import android.app.Fragment;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.salvadordalvik.fastlibrary.list.BaseFastItem;
import com.salvadordalvik.something.MainActivity;
import com.salvadordalvik.something.R;

/**
 * Created by matthewshepard on 1/17/14.
 */
public class ThreadItem extends BaseFastItem<ThreadItem.ThreadHolder> implements Parcelable{
    private String threadTitle, author, lastPost;
    private int unread, replies;

    public ThreadItem(int id, String title, int unreadCount, int replies, String author, String lastPost) {
        super(R.layout.thread_item, id, true);
        this.threadTitle = title;
        this.unread = unreadCount;
        this.replies = replies;
        this.author = author;
        this.lastPost = lastPost;
    }

    public ThreadItem(Parcel source) {
        super(R.layout.thread_item, source.readInt(), true);
        this.threadTitle = source.readString();
        this.unread = source.readInt();
        this.replies = source.readInt();
        this.author = source.readString();
        this.lastPost = source.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getId());
        dest.writeString(threadTitle);
        dest.writeInt(unread);
        dest.writeInt(replies);
        dest.writeString(author);
        dest.writeString(lastPost);
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
        if(unread >= 0){
            holder.subtext.setText("Pages: "+(replies/40+1)+" - Killed: "+lastPost);
        }else{
            holder.subtext.setText("Pages: "+(replies/40+1)+" - Author: "+author);
        }
    }

    @Override
    public void onItemClick(Activity act, Fragment fragment) {
        if(unread >= 0){
            ((MainActivity)act).showThread(id);
        }else{
            ((MainActivity)act).showThread(id, 1);
        }
    }

    @Override
    public ThreadHolder createViewHolder(View view) {
        return new ThreadHolder(view);
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

    private static final Parcelable.Creator<ThreadItem> CREATOR = new Parcelable.Creator<ThreadItem>(){

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
