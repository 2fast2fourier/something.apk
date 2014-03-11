package com.salvadordalvik.something.list;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.salvadordalvik.fastlibrary.list.BaseFastItem;
import com.salvadordalvik.something.PrivateMessageListActivity;
import com.salvadordalvik.something.R;

/**
 * Created by matthewshepard on 2/7/14.
 */
public class PrivateMessageItem extends BaseFastItem<PrivateMessageItem.PMHolder> {
    private final String title, author, date;
    private final int folderId;
    private boolean unread;

    public PrivateMessageItem(int id, String title, String author, String date, boolean unread, int folderId) {
        super(R.layout.private_message_item, id);
        this.title = title;
        this.author = author;
        this.date = date;
        this.unread = unread;
        this.folderId = folderId;
    }

    @Override
    public PMHolder createViewHolder(View view) {
        return new PMHolder(view);
    }

    @Override
    public void updateViewFromHolder(View view, PMHolder holder) {
        holder.title.setText(title);
        holder.subtext.setText(formatSubtext(author, folderId));
        holder.date.setText(date);
        if(unread){
            holder.title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }else{
            holder.title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        }
    }

    @Override
    public boolean onItemClick(Activity act, Fragment fragment) {
        if(act instanceof PrivateMessageListActivity){
            ((PrivateMessageListActivity)act).showPM(getId(), title);
        }else{
            act.startActivity(new Intent(act, PrivateMessageListActivity.class).putExtra("pm_id", getId()).putExtra("pm_title", title));
        }
        unread = false;
        return true;
    }

    protected static class PMHolder{
        private final TextView title, subtext, date;

        public PMHolder(View view) {
            title = (TextView) view.findViewById(R.id.pm_item_title);
            subtext = (TextView) view.findViewById(R.id.pm_item_author);
            date = (TextView) view.findViewById(R.id.pm_item_date);
        }
    }

    private static String formatSubtext(String user, int folderId){
        if(folderId < 0){
            return "To: "+user;
        }
        return "From: "+user;
    }
}
