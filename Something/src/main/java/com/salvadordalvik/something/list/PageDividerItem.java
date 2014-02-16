package com.salvadordalvik.something.list;

import android.app.Activity;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.salvadordalvik.fastlibrary.list.BaseFastItem;
import com.salvadordalvik.fastlibrary.util.FastUtils;
import com.salvadordalvik.something.R;

/**
 * Created by matthewshepard on 2/2/14.
 */
public class PageDividerItem extends BaseFastItem<PageDividerItem.DividerHolder> {
    private final int page;
    private final PagedAdapter.PagedCallbacks delegate;
    private final PagedAdapter parent;

    public PageDividerItem(int page, PagedAdapter.PagedCallbacks delegate, PagedAdapter parent) {
        super(R.layout.page_divider_item, FastUtils.getSimpleUID(), false);
        this.page = page;
        this.delegate = delegate;
        this.parent = parent;
    }

    @Override
    public DividerHolder createViewHolder(View view) {
        return new DividerHolder(view);
    }

    @Override
    public void updateViewFromHolder(View view, DividerHolder holder) {
        holder.delegate = delegate;
        holder.pageNumber = page;
        holder.maxPage = parent.getMaxPage();
        holder.page.setText("Page "+page+"/"+parent.getMaxPage()+(holder.maxPage > 1 ? "..." : ""));
        if(page != parent.getLoadingPage()){
            holder.refresh.clearAnimation();
        }
        holder.jump.setVisibility(page > 1 ? View.VISIBLE : View.INVISIBLE);
        if(page == 1){
            view.setBackgroundResource(R.drawable.page_divider_background);
        }else{
            view.setBackgroundColor(Color.BLACK);
        }
    }

    @Override
    public boolean onItemClick(Activity act, Fragment fragment) {
        return false;
    }

    protected static class DividerHolder implements View.OnClickListener{
        public PagedAdapter.PagedCallbacks delegate;
        public int pageNumber, maxPage;
        public TextView page;
        public ImageView refresh, jump;
        private DividerHolder(View view){
            page = (TextView) view.findViewById(R.id.page_divider_page);
            page.setOnClickListener(this);
            refresh = (ImageView) view.findViewById(R.id.page_divider_refresh);
            refresh.setOnClickListener(this);
            jump = (ImageView) view.findViewById(R.id.page_divider_scroll);
            jump.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.page_divider_page:
                    delegate.showPageSelectDialog(pageNumber, maxPage);
                    break;
                case R.id.page_divider_refresh:
                    delegate.refreshPage(pageNumber);
                    RotateAnimation rot = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    rot.setRepeatMode(Animation.RESTART);
                    rot.setRepeatCount(Animation.INFINITE);
                    v.startAnimation(rot);
                    break;
                case R.id.page_divider_scroll:
                    delegate.scrollToTop();
                    break;
            }
        }
    }
}
