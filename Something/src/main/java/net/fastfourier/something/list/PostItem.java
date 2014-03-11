package net.fastfourier.something.list;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.salvadordalvik.fastlibrary.list.BaseFastItem;
import net.fastfourier.something.R;

import org.xml.sax.XMLReader;

/**
 * Created by matthewshepard on 1/19/14.
 */
public class PostItem extends BaseFastItem<PostItem.PostHolder> implements Html.ImageGetter, Html.TagHandler {
    private String author, avatar, avTitle, postDate;
    private Spanned content;

    public PostItem(int id, String author, String avTitle, String avatar, String content, String postDate) {
        super(R.layout.post_item, id, false);
        this.author = author;
        this.avatar = avatar;
        this.avTitle = avTitle;
        this.postDate = postDate;
        this.content = Html.fromHtml(content, this, this);
    }

    @Override
    public PostHolder createViewHolder(View view) {
        return new PostHolder(view);
    }

    @Override
    public void updateViewFromHolder(View view, PostHolder holder) {
        holder.author.setText(author);
        holder.date.setText(postDate);
        holder.content.setText(content);
    }

    @Override
    public boolean onItemClick(Activity act, Fragment fragment) {
        return false;
    }

    @Override
    public Drawable getDrawable(String source) {
        Log.e("PostItem", "Get Image: " + source);
        return null;
    }

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
//        Log.e("PostItem", "Unknown Tag: " + tag);
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
