package net.fastfourier.something.list;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;

import com.salvadordalvik.fastlibrary.list.BaseFastItem;
import net.fastfourier.something.PrivateMessageListFragment;
import net.fastfourier.something.R;

/**
 * Created by matthewshepard on 2/7/14.
 */
public class PrivateMessageFolderItem extends BaseFastItem<PrivateMessageFolderItem.PMFolderHolder> {
    private final String title;
    private boolean selected;

    public PrivateMessageFolderItem(int id, String title, boolean selected) {
        super(R.layout.private_message_folder, id);
        this.title = title;
        this.selected = selected;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
    }

    @Override
    public PMFolderHolder createViewHolder(View view) {
        return new PMFolderHolder(view);
    }

    @Override
    public void updateViewFromHolder(View view, PMFolderHolder holder) {
        holder.title.setText(title);
        view.setActivated(selected);
    }

    @Override
    public boolean onItemClick(Activity act, Fragment fragment) {
        ((PrivateMessageListFragment)fragment).showFolder(getId());
        selected = true;
        return true;
    }

    protected static class PMFolderHolder{
        private TextView title;

        public PMFolderHolder(View view) {
            title = (TextView) view.findViewById(R.id.pm_folder_title);
        }
    }
}
