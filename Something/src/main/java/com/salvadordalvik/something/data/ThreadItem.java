package com.salvadordalvik.something.data;

import android.app.Activity;
import android.app.Fragment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.salvadordalvik.fastlibrary.list.BaseFastItem;
import com.salvadordalvik.something.MainActivity;
import com.salvadordalvik.something.R;

/**
 * Created by matthewshepard on 1/17/14.
 */
public class ThreadItem extends BaseFastItem<ThreadItem.ThreadHolder> {
    private String threadTitle;

    public ThreadItem(int id, String title) {
        super(R.layout.thread_item, id, 0, true);
        threadTitle = title;
    }

    @Override
    public void updateViewFromHolder(View view, ThreadHolder holder) {
        holder.title.setText(threadTitle);
    }

    @Override
    public void onItemClick(Activity act, Fragment fragment) {
        Toast.makeText(act, "Clicked thread: "+id, Toast.LENGTH_SHORT).show();
        if(act instanceof MainActivity){
            ((MainActivity)act).showThread(id);
        }else{
//            act.startActivity();
        }
    }

    @Override
    public ThreadHolder createViewHolder(View view) {
        return new ThreadHolder(view);
    }

    protected static class ThreadHolder{
        public TextView title;
        private ThreadHolder(View view){
            title = (TextView) view.findViewById(R.id.thread_item_title);
        }
    }
}
