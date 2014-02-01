package com.salvadordalvik.something;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.salvadordalvik.fastlibrary.FastFragment;
import com.salvadordalvik.fastlibrary.alert.FastAlert;
import com.salvadordalvik.fastlibrary.util.FastUtils;
import com.salvadordalvik.something.request.BookmarkRequest;
import com.salvadordalvik.something.request.ThreadPageRequest;
import com.salvadordalvik.something.util.Constants;

/**
 * Created by matthewshepard on 1/19/14.
 */
public class ThreadViewFragment extends FastFragment {
    private WebView threadView;

    private int threadId, page, maxPage;
    private Spanned threadTitle;
    private String pageHtml, rawThreadTitle;
    private boolean bookmarked;

    public ThreadViewFragment() {
        super(R.layout.ptr_generic_webview, R.menu.thread_view);
    }

    @Override
    public void viewCreated(View frag, Bundle savedInstanceState) {
        threadView = (WebView) frag.findViewById(R.id.ptr_webview);
        initWebview();

        if(savedInstanceState != null && savedInstanceState.containsKey("thread_html")){
            threadId = savedInstanceState.getInt("thread_id");
            pageHtml = savedInstanceState.getString("thread_html");
            page = savedInstanceState.getInt("thread_page", 1);
            maxPage = savedInstanceState.getInt("thread_maxpage", 1);
            rawThreadTitle = savedInstanceState.getString("thread_title");
            if(!TextUtils.isEmpty(rawThreadTitle)){
                threadTitle = Html.fromHtml(rawThreadTitle);
                setTitle(threadTitle);
            }

            threadView.loadDataWithBaseURL(Constants.BASE_URL, pageHtml, "text/html", "utf-8", null);
        }
    }

    private void initWebview() {
        threadView.getSettings().setJavaScriptEnabled(true);
        threadView.setWebChromeClient(chromeClient);
        threadView.setWebViewClient(webClient);

        threadView.setBackgroundColor(Color.BLACK);
    }

    @Override
    public void onResume() {
        super.onResume();
        threadView.onResume();
        threadView.resumeTimers();
    }

    public void onPaneObscured() {
        if(isResumed()){
            threadView.pauseTimers();
            threadView.onPause();
        }
    }

    public void onPaneRevealed() {
        if(isResumed()){
            threadView.onResume();
            threadView.resumeTimers();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        threadView.pauseTimers();
        threadView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(pageHtml != null){
            outState.putString("thread_html", pageHtml);
            outState.putInt("thread_id", threadId);
            outState.putInt("thread_page", page);
            outState.putInt("thread_maxpage", maxPage);
            outState.putString("thread_title", rawThreadTitle);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_thread_bookmark).setIcon(bookmarked ? R.drawable.star : R.drawable.star_empty);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_thread_bookmark:
                FastAlert.process(getActivity(), getView(), getString(bookmarked ? R.string.bookmarking_thread_started_removing : R.string.bookmarking_thread_started));
                queueRequest(new BookmarkRequest(threadId, !bookmarked, new Response.Listener<Boolean>() {
                    @Override
                    public void onResponse(Boolean response) {
                        bookmarked = response;
                        invalidateOptionsMenu();
                        FastAlert.notice(getActivity(), getView(), getString(response ? R.string.bookmarking_thread_success_add : R.string.bookmarking_thread_success_remove));
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        FastAlert.error(getActivity(), getView(), getString(R.string.bookmarking_thread_failure));
                    }
                }));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void refreshData(boolean pullToRefresh, boolean staleRefresh) {
        queueRequest(new ThreadPageRequest(threadId, page, new Response.Listener<ThreadPageRequest.ThreadPage>() {
            @Override
            public void onResponse(ThreadPageRequest.ThreadPage response) {
                page = response.pageNum;
                maxPage = response.maxPageNum;
                if (!TextUtils.isEmpty(response.threadTitle)) {
                    threadTitle = Html.fromHtml(response.threadTitle);
                }
                threadView.loadDataWithBaseURL(Constants.BASE_URL, response.pageHtml, "text/html", "utf-8", null);
                pageHtml = response.pageHtml;
                rawThreadTitle = response.threadTitle;
                bookmarked = response.bookmarked;
                Activity act = getActivity();
                if (act != null) {
                    act.setTitle(threadTitle);
                    ((MainActivity) act).onThreadPageLoaded(response.threadId, response.unreadDiff);
                }
                invalidateOptionsMenu();
            }
        }, null));
    }

    public void loadThread(int threadId, int page){
        this.threadId = threadId;
        this.page = page;
        this.bookmarked = false;
        setTitle(getString(R.string.thread_view_loading));
        invalidateOptionsMenu();
        startRefresh();
    }

    protected void goToPage(int pageNum){
        if(pageNum <= maxPage && pageNum > 0){
            page = pageNum;
            startRefresh();
        }
    }

    public boolean hasThreadLoaded() {
        return pageHtml != null;
    }

    public Spanned getTitle() {
        return threadTitle;
    }

    private WebChromeClient chromeClient = new WebChromeClient(){
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            Log.d("WebView", "Progress: "+newProgress);
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Log.e("WebChromeClient", consoleMessage.lineNumber()+" - "+consoleMessage.messageLevel()+" - "+consoleMessage.message());
            return true;
        }
    };

    private WebViewClient webClient = new WebViewClient(){
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.d("WebView", "onPageStarted: " + url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.d("WebView", "onPageFinished: " + url);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            Log.d("WebView", "onLoadResource: " + url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("WebView", "shouldOverrideUrlLoading: "+url);
            if(url != null && url.startsWith("something")){
                if(url.contains("something://something-next")){
                    goToPage(page+1);
                    return true;
                }else if(url.contains("something://something-prev")){
                    goToPage(page-1);
                    return true;
                }else if(url.contains("something://something-pageselect")){
                    Toast.makeText(getActivity(), "Not implemented yet.", Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
            FastUtils.startUrlIntent(getActivity(), url);
            return true;
        }
    };
}
