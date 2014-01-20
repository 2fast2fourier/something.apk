package com.salvadordalvik.something.data;

import android.app.Activity;
import android.app.Fragment;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.salvadordalvik.fastlibrary.list.BaseFastItem;
import com.salvadordalvik.something.R;

/**
 * Created by matthewshepard on 1/19/14.
 */
public class PostItem extends BaseFastItem<PostItem.PostHolder> {
    private String author, avatar, avTitle, content, postDate;

    public PostItem(int id, String author, String avTitle, String avatar, String content, String postDate) {
        super(R.layout.post_item, id, 0, false);
        this.author = author;
        this.avatar = avatar;
        this.avTitle = avTitle;
        this.content = content;
        this.postDate = postDate;
    }

    @Override
    public PostHolder createViewHolder(View view) {
        return new PostHolder(view);
    }

    @Override
    public void updateViewFromHolder(View view, PostHolder holder) {
        holder.author.setText(author);
        holder.date.setText(postDate);
        holder.content.setText(Html.fromHtml(content));
    }

    @Override
    public void onItemClick(Activity act, Fragment fragment) {

    }

    protected static class PostHolder{
        public TextView author, date, content;
        private PostHolder(View view){
            author = (TextView) view.findViewById(R.id.post_author);
            date = (TextView) view.findViewById(R.id.post_date);
            content = (TextView) view.findViewById(R.id.post_content);
        }
    }
}
