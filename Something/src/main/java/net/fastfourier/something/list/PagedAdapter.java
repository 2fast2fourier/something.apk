package net.fastfourier.something.list;

import android.support.v4.app.Fragment;
import android.widget.AbsListView;

import com.salvadordalvik.fastlibrary.list.FastItem;
import com.salvadordalvik.fastlibrary.list.SectionFastAdapter;

import java.util.List;

/**
 * Created by matthewshepard on 2/2/14.
 */
public class PagedAdapter extends SectionFastAdapter implements AbsListView.OnScrollListener{
    private int lastPage, loadingPage, maxPage;
    private final int firstPageOffset;
    private final PagedCallbacks callback;

    public int getLoadingPage() {
        return loadingPage;
    }

    public interface PagedCallbacks{
        public void showPageSelectDialog(int page, int maxPage);
        public void refreshPage(int page);
        public void scrollToTop();
    }

    public PagedAdapter(Fragment fragment, int reservedSectionCount, int maxTypeCount, PagedCallbacks callbacks) {
        super(fragment, maxTypeCount);
        this.firstPageOffset = reservedSectionCount;
        this.callback = callbacks;
        this.lastPage = 0;
        this.loadingPage = -1;
    }

    private int getPageSection(int page){
        return page+firstPageOffset;
    }

    public void setPageContent(int page, List<? extends FastItem> items){
        while(page-1 > lastPage){
            lastPage++;
            replaceSection(getPageSection(lastPage), new PageDividerItem(lastPage, callback, this));
        }
        if(page > lastPage){
            lastPage = page;
            loadingPage = -1;
        }
        if(loadingPage == page){
            loadingPage = -1;
        }
        replaceSection(getPageSection(page), new PageDividerItem(page, callback, this));
        addItems(getPageSection(page), items);
    }

    public void setMaxPage(int maxPage){
        this.maxPage = maxPage;
    }

    public int getPageOffset(int page){
        return getSectionOffset(getPageSection(page));
    }

    public void clearPages() {
        for(int ix=1;ix<=lastPage;ix++){
            clearSection(getPageSection(ix));
        }
        lastPage = 0;
        maxPage = 0;
        loadingPage = -1;
    }

    public void clearPagesAfter(int page) {
        for(int ix=page+1;ix<=maxPage;ix++){
            clearSection(getPageSection(ix));
        }
        lastPage = page;
    }

    public int getMaxPage() {
        return maxPage;
    }

    public void loadingPageFailed(){
        loadingPage = -1;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(firstVisibleItem + visibleItemCount + 10 > totalItemCount && totalItemCount > 20 && loadingPage < lastPage && lastPage < maxPage){
            loadingPage = lastPage+1;
            replaceSection(getPageSection(loadingPage), new LoadingItem("Loading Page "+loadingPage));
            callback.refreshPage(loadingPage);
        }
    }
}
