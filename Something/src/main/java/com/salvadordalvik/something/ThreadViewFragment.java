package com.salvadordalvik.something;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannedString;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.salvadordalvik.fastlibrary.FastFragment;
import com.salvadordalvik.fastlibrary.alert.FastAlert;
import com.salvadordalvik.fastlibrary.request.FastVolley;
import com.salvadordalvik.fastlibrary.util.FastUtils;
import com.salvadordalvik.something.request.BookmarkRequest;
import com.salvadordalvik.something.request.MarkLastReadRequest;
import com.salvadordalvik.something.request.ThreadPageRequest;
import com.salvadordalvik.something.util.Constants;
import com.salvadordalvik.something.util.SomePreferences;
import com.salvadordalvik.something.widget.PageSelectDialogFragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.DefaultHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnPullFromBottomListener;

/**
 * Created by matthewshepard on 1/19/14.
 */
public class ThreadViewFragment extends FastFragment implements PageSelectDialogFragment.PageSelectable, View.OnClickListener {
    private WebView threadView;

    private int threadId, page, maxPage;
    private CharSequence threadTitle;
    private String pageHtml, rawThreadTitle;
    private boolean bookmarked;

    private ImageView navPrev, navNext;
    private TextView navPageBar;
    private boolean disableNavLoading = false;

    public ThreadViewFragment() {
        super(R.layout.thread_pageview, R.menu.thread_view);
    }

    @Override
    public void viewCreated(View frag, Bundle savedInstanceState) {
        navPrev = (ImageView) frag.findViewById(R.id.threadview_prev);
        navNext = (ImageView) frag.findViewById(R.id.threadview_next);
        navPageBar = (TextView) frag.findViewById(R.id.threadview_page);
        navPageBar.setOnClickListener(this);
        navNext.setOnClickListener(this);
        navPrev.setOnClickListener(this);

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

        updateNavbar();
        loadSessionCookie();
    }

    @Override
    protected void setupPullToRefresh(PullToRefreshLayout ptr) {
        ActionBarPullToRefresh.from(getActivity()).allChildrenArePullable().options(generatePullToRefreshOptions()).listener(this).setup(ptr);
    }

    @Override
    protected Options generatePullToRefreshOptions() {
        return Options.create().scrollDistance(getScrollDistance()).build();
    }

    private float getScrollDistance(){
        return Math.max(Math.min(FastUtils.calculateScrollDistance(getActivity(), 2.5f), 0.666f), 0.333f);
    }

    private void initWebview() {
        threadView.getSettings().setJavaScriptEnabled(true);
        threadView.setWebChromeClient(chromeClient);
        threadView.setWebViewClient(webClient);
        threadView.addJavascriptInterface(new SomeJavascriptInterface(), "listener");

        TypedValue val = new TypedValue();
        if(getActivity().getTheme().resolveAttribute(R.attr.webviewBackgroundColor, val, true)){
            threadView.setBackgroundColor(val.data);
        }

        registerForContextMenu(threadView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        WebView.HitTestResult result = threadView.getHitTestResult();
        final String targetUrl = result.getExtra();

        switch (result.getType()){
            case WebView.HitTestResult.IMAGE_TYPE:
            case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
                menu.add(R.string.menu_save_image).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(getActivity(), "Image Saving not implemented yet!", Toast.LENGTH_LONG).show();
                        //TODO save image link
                        FastUtils.startUrlIntent(getActivity(), targetUrl);
                        return true;
                    }
                });
            case WebView.HitTestResult.SRC_ANCHOR_TYPE:
                menu.setHeaderTitle(result.getExtra());
                menu.add(R.string.menu_open_link).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        FastUtils.startUrlIntent(getActivity(), targetUrl);
                        return true;
                    }
                });
                menu.add(R.string.menu_copy_url).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        FastAlert.custom(getActivity(), getView(), getString(R.string.url_copied), null, R.drawable.ic_menu_link);
                        ClipboardManager clipman = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        clipman.setPrimaryClip(ClipData.newPlainText(threadTitle.toString(), targetUrl));
                        return true;
                    }
                });
                menu.add(R.string.menu_share_link).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        FastUtils.showSimpleShareChooser(getActivity(), threadTitle.toString(), targetUrl, getString(R.string.menu_share_link));
                        return true;
                    }
                });
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(((MainActivity)getActivity()).isFragmentFocused(this)){
            threadView.onResume();
            threadView.resumeTimers();
        }
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

    private void updateNavbar() {
        navPrev.setEnabled(page > 1 && !disableNavLoading);
        navPageBar.setText("Page "+page+"/"+maxPage);
        navNext.setImageResource(page < maxPage ? R.drawable.arrowright : R.drawable.ic_menu_load);
        navNext.setEnabled(!disableNavLoading || page == maxPage);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem bookmark = menu.findItem(R.id.menu_thread_bookmark);
        if(bookmark != null){
            bookmark.setIcon(bookmarked ? R.drawable.star : R.drawable.star_empty);
            bookmark.setTitle(bookmarked ? R.string.menu_thread_unbookmark : R.string.menu_thread_bookmark);
        }
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

    //Must be a common string, volley uses direct equality when comparing tags.
    private static final String THREAD_REQUEST_TAG = "thread_page_request";
    @Override
    public void refreshData(boolean pullToRefresh, boolean staleRefresh) {
        threadView.stopLoading();
        disableNavLoading = true;
        updateNavbar();
        FastVolley.cancelRequestByTag(THREAD_REQUEST_TAG);
        queueRequest(new ThreadPageRequest(threadId, page, pageListener, errorListener), THREAD_REQUEST_TAG);
        getHandler().postDelayed(enableNavigation, 1000);
    }

    private Runnable enableNavigation = new Runnable() {
        @Override
        public void run() {
            if(disableNavLoading){
                disableNavLoading = false;
                updateNavbar();
            }
        }
    };

    private Response.Listener<ThreadPageRequest.ThreadPage> pageListener = new Response.Listener<ThreadPageRequest.ThreadPage>() {
        @Override
        public void onResponse(ThreadPageRequest.ThreadPage response) {
            loadSessionCookie();
            disableNavLoading = false;
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
            if (act instanceof MainActivity) {
                MainActivity main = (MainActivity) act;
                main.onThreadPageLoaded(response.threadId);
            }
            setTitle(threadTitle);
            updateNavbar();
            invalidateOptionsMenu();
        }
    };

    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            disableNavLoading = false;
            updateNavbar();
        }
    };

    public void loadThread(int threadId, int page){
        this.threadId = threadId;
        this.page = page;
        this.maxPage = 0;
        this.bookmarked = false;
        this.threadTitle = new SpannedString(getString(R.string.thread_view_loading));
        setTitle(threadTitle);
        invalidateOptionsMenu();
        updateNavbar();
        startRefresh();
        threadView.loadUrl("about:blank");
    }

    private void loadSessionCookie(){
        if(SomePreferences.loggedIn){
            CookieManager cookieMstr = CookieManager.getInstance();
            Matcher cookieCutter = Pattern.compile("(\\w+)=(\\w+)").matcher(SomePreferences.cookieString);
            while(cookieCutter.find()){
                String name = cookieCutter.group(1);
                String value = cookieCutter.group(2);
                cookieMstr.setCookie("forums.somethingawful.com", name+"="+value+"; domain=forums.somethingawful.com");
            }
            CookieSyncManager.getInstance().sync();
        }
    }

    public void goToPage(int pageNum){
        if(pageNum <= maxPage && pageNum > 0){
            page = pageNum;
            updateNavbar();
            startRefresh();
        }
    }

    public boolean hasThreadLoaded() {
        return pageHtml != null;
    }

    public CharSequence getTitle() {
        return threadTitle;
    }

    private WebChromeClient chromeClient = new WebChromeClient(){
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            Log.d("WebView", "Progress: "+newProgress);
            setProgress(newProgress);
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
            setProgress(100);
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
                    displayPageSelect();
                    return true;
                }
            }
            FastUtils.startUrlIntent(getActivity(), url);
            return true;
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.threadview_prev:
                goToPage(page-1);
                break;
            case R.id.threadview_page:
                displayPageSelect();
                break;
            case R.id.threadview_next:
                if(page < maxPage){
                    goToPage(page+1);
                }else{
                    startRefresh();
                }
                break;
        }
    }

    private void displayPageSelect(){
        PageSelectDialogFragment.newInstance(page, maxPage, ThreadViewFragment.this).show(getFragmentManager(), "page_select");
    }

    public class SomeJavascriptInterface {

        @JavascriptInterface
        public void onQuoteClick(String postId){

        }

        @JavascriptInterface
        public void onEditClick(String postId){

        }

        @JavascriptInterface
        public void onMoreClick(String postId, String username, String userid){

        }

        @JavascriptInterface
        public void onLastReadClick(String postIndex){
            final int index = FastUtils.safeParseInt(postIndex, 0);
            if(index > 0){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FastAlert.process(getActivity(), getView(), getSafeString(R.string.mark_last_read_started));
                        queueRequest(new MarkLastReadRequest(threadId, index, new Response.Listener<ThreadPageRequest.ThreadPage>() {
                            @Override
                            public void onResponse(ThreadPageRequest.ThreadPage response) {
                                FastAlert.notice(getActivity(), getView(), getSafeString(R.string.mark_last_read_success));
                                pageListener.onResponse(response);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                FastAlert.error(getActivity(), getView(), getSafeString(R.string.mark_last_read_failure));
                            }
                        }
                        ));
                    }
                });
            }else{
                throw new RuntimeException("Invalid postIndex in onLastReadClick: "+postIndex);
            }
        }
    }

    @Override
    protected void setTitle(CharSequence title) {
        Activity act = getActivity();
        if(act instanceof MainActivity){
            ((MainActivity)act).setTitle(title, this);
        }
    }
}
