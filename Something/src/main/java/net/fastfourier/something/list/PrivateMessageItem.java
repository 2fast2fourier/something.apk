package net.fastfourier.something.list;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.salvadordalvik.fastlibrary.list.BaseFastItem;
import net.fastfourier.something.PrivateMessageListActivity;
import net.fastfourier.something.R;

/**
 * Created by matthewshepard on 2/7/14.
 */
public class PrivateMessageItem extends BaseFastItem<PrivateMessageItem.PMHolder> {
    public static final int STATUS_NORMAL = 0;
    public static final int STATUS_NEW = 1;
    public static final int STATUS_REPLIED = 2;
    public static final int STATUS_FORWARDED = 3;

    private final String title, author, date;
    private final int folderId;
    private int status;

    public PrivateMessageItem(int id, String title, String author, String date, int status, int folderId) {
        super(R.layout.private_message_item, id);
        this.title = title;
        this.author = author;
        this.date = date;
        this.status = status;
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
        if(status == STATUS_NEW){
            holder.title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        }else{
            holder.title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        }
        switch (status){
            case STATUS_NORMAL:
                holder.subtext.setCompoundDrawablesWithIntrinsicBounds(R.drawable.pm_status_normal, 0, 0, 0);
                break;
            case STATUS_NEW:
                holder.subtext.setCompoundDrawablesWithIntrinsicBounds(R.drawable.pm_status_new, 0, 0, 0);
                break;
            case STATUS_REPLIED:
                holder.subtext.setCompoundDrawablesWithIntrinsicBounds(R.drawable.pm_status_replied, 0, 0, 0);
                break;
            case STATUS_FORWARDED:
                holder.subtext.setCompoundDrawablesWithIntrinsicBounds(R.drawable.pm_status_forwarded, 0, 0, 0);
                break;
        }
        holder.subtext.setCompoundDrawablePadding(4);
    }

    @Override
    public boolean onItemClick(Activity act, Fragment fragment) {
        if(act instanceof PrivateMessageListActivity){
            ((PrivateMessageListActivity)act).showPM(getId(), title);
        }else{
            act.startActivity(new Intent(act, PrivateMessageListActivity.class).putExtra("pm_id", getId()).putExtra("pm_title", title));
        }
        status = STATUS_NORMAL;
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
