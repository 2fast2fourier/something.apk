package net.fastfourier.something;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.SpannedString;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.webkit.ConsoleMessage;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.salvadordalvik.fastlibrary.alert.FastAlert;
import com.salvadordalvik.fastlibrary.request.FastVolley;
import com.salvadordalvik.fastlibrary.util.FastUtils;

import net.fastfourier.something.request.BookmarkRequest;
import net.fastfourier.something.request.MarkLastReadRequest;
import net.fastfourier.something.request.ThreadPageRequest;
import net.fastfourier.something.util.Constants;
import net.fastfourier.something.util.SomeTheme;
import net.fastfourier.something.util.SomeURL;
import net.fastfourier.something.widget.PageSelectDialogFragment;

import java.util.LinkedList;

import fr.castorflex.android.smoothprogressbar.SmoothProgressDrawable;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.HeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshFromBottomListener;
import uk.co.senab.actionbarpulltorefresh.library.sdk.Compat;

/**
 * Created by matthewshepard on 1/19/14.
 */
public class ThreadViewFragment extends SomeFragment implements PageSelectDialogFragment.PageSelectable, View.OnClickListener, OnRefreshFromBottomListener {
    private static final int REQUEST_REPLY = 101;
    private static final int REQUEST_NEW_PM = 102;
    private WebView threadView;

    private View pfbContainer;
    private TextView pfbTitle;
    private ProgressBar pfbProgressbar;

    private int threadId = 0;
    private long postId = 0;
    private int userId = 0;
    private int page;
    private int maxPage;
    private int forumId;
    private CharSequence threadTitle;
    private String pageHtml, rawThreadTitle;
    private boolean bookmarked, canReply;
    private String downloadUrl;

    /**
     * This backstack is used to push-pop thread states when navigating into threads view link from another thread,
     * allowing back-button to take the user back to the original thread.
     * See loadThreadState, saveThreadState, overrideBackPressed, and the various loadThread functions to see this in action.
     */
    private LinkedList<Bundle> threadBackstack = new LinkedList<Bundle>();

    /**
     * Basic navbar, navNext is next-page if page<maxPage and refresh otherwise.
     * See updateNavBar() for more.
     * Navbar buttons are locked for a second after navigation, see enableNavigation runnable.
     */
    private ImageView navPrev, navNext;
    private TextView navPageBar;
    private boolean disableNavLoading = false;

    /**
     * ignorePageProgress is a hack to prevent chromeClient progress callbacks when we don't want them,
     * mostly so we don't get progressbar interruptions in some parts of the refresh process.
     */
    private boolean ignorePageProgress = false;

    public ThreadViewFragment() {
        super(R.layout.thread_pageview, R.menu.thread_view);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void viewCreated(View frag, Bundle savedInstanceState) {
        navPrev = (ImageView) frag.findViewById(R.id.threadview_prev);
        navNext = (ImageView) frag.findViewById(R.id.threadview_next);
        navPageBar = (TextView) frag.findViewById(R.id.threadview_page);
        navPageBar.setOnClickListener(this);
        navNext.setOnClickListener(this);
        navPrev.setOnClickListener(this);

        pfbContainer = frag.findViewById(R.id.threadview_pullfrombottom);
        pfbTitle = (TextView) frag.findViewById(R.id.threadview_pullfrombottom_title);
        pfbProgressbar = (ProgressBar) frag.findViewById(R.id.threadview_pullfrombottom_progress);

        threadView = (WebView) frag.findViewById(R.id.ptr_webview);
        initWebview();

        updateNavbar();
        CookieSyncManager cookieMan = CookieSyncManager.getInstance();
        if(cookieMan != null){
            cookieMan.sync();
        }

        if(savedInstanceState != null && savedInstanceState.containsKey("thread_html")){
            loadThreadState(savedInstanceState);
        }else{
            Intent intent = getActivity().getIntent();
            threadId = intent.getIntExtra("thread_id", 0);
            page = intent.getIntExtra("thread_page", 0);
            postId = intent.getLongExtra("post_id", 0);
            if(threadId > 0 || postId > 0){
                setTitle("Loading...");
                startRefresh();
            }
        }
    }

    @Override
    protected void setupPullToRefresh(PullToRefreshLayout ptr) {
        ActionBarPullToRefresh.from(getActivity()).allChildrenArePullable().options(generatePullToRefreshOptions()).bottomListener(this).setup(ptr);
    }

    @Override
    protected Options generatePullToRefreshOptions() {
        return Options.create().scrollDistance(getScrollDistance()).headerTransformer(header).refreshOnUp(true).build();
    }

    private float getScrollDistance(){
        return Math.max(Math.min(FastUtils.calculateScrollDistance(getActivity(), 2f), 0.666f), 0.333f);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebview() {
        threadView.getSettings().setJavaScriptEnabled(true);
        threadView.setWebChromeClient(chromeClient);
        threadView.setWebViewClient(webClient);
        threadView.addJavascriptInterface(new SomeJavascriptInterface(), "listener");

        threadView.setBackgroundColor(SomeTheme.getThemeColor(getActivity(), R.attr.webviewBackgroundColor, Color.BLACK));

        registerForContextMenu(threadView);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        WebView.HitTestResult result = threadView.getHitTestResult();
        final String targetUrl = result.getExtra();
        final ThreadViewFragment that = this;

        switch (result.getType()){
            case WebView.HitTestResult.IMAGE_TYPE:
            case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
                menu.add(R.string.menu_save_image).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (Build.VERSION.SDK_INT >= 23) {
                            Log.e("ThreadViewFragment", "Checking SDK");
                            if (ContextCompat.checkSelfPermission(that.getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                Log.e("ThreadViewFragment", "Need write permissions");
                                downloadUrl = targetUrl;
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Constants.REQUEST_WRITE_PERMISSIONS);
                                return false;
                            }
                        }
                        enqueueDownload(targetUrl);
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

    public void enqueueDownload(String targetUrl) {
        Uri image = Uri.parse(targetUrl);
        DownloadManager.Request request = new DownloadManager.Request(image);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, image.getLastPathSegment());
        request.allowScanningByMediaScanner();
        DownloadManager dlMngr = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        dlMngr.enqueue(request);
        Toast.makeText(getActivity(), "Image saved to Downloads folder.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case Constants.REQUEST_WRITE_PERMISSIONS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (downloadUrl != null) {
                        enqueueDownload(downloadUrl);
                    }
                }
                else {
                    Toast.makeText(getActivity(), R.string.no_permission_download, Toast.LENGTH_LONG).show();
                }
                downloadUrl = null;
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(((MainActivity)getActivity()).isFragmentFocused(this)){
            threadView.onResume();
            threadView.resumeTimers();
            updateActionbarColor(forumId);
            setTitle(threadTitle);
        }
        CookieSyncManager cookieMan = CookieSyncManager.getInstance();
        if(cookieMan != null){
            cookieMan.startSync();
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
            updateActionbarColor(forumId);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        threadView.pauseTimers();
        threadView.onPause();
        CookieSyncManager cookieMan = CookieSyncManager.getInstance();
        if(cookieMan != null){
            cookieMan.stopSync();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(pageHtml != null){
            saveThreadState(outState);
        }
    }

    /**
     * Saves current thread state for app lifecycle and thread navigation push/pop.
     * Must mirror loadThreadState implementation.
     * @param outState Bundle to store all data needed to restore current thread state.
     * @return outState passthrough
     */
    private Bundle saveThreadState(Bundle outState){
        outState.putString("thread_html", pageHtml);
        outState.putInt("thread_id", threadId);
        outState.putInt("thread_page", page);
        outState.putInt("thread_maxpage", maxPage);
        outState.putString("thread_title", rawThreadTitle);
        outState.putLong("post_id", postId);
        outState.putInt("thread_forum_id", forumId);
        outState.putBoolean("thread_bookmarked", bookmarked);
        outState.putBoolean("thread_canreply", canReply);
        return outState;
    }

    /**
     * Restores state saved in saveThreadState, used when restoring state after app lifecycle changes
     * or deep navigation into threads via link (and back button).
     * @param inState
     */
    private void loadThreadState(Bundle inState){
        threadId = inState.getInt("thread_id", 0);
        pageHtml = inState.getString("thread_html");
        page = inState.getInt("thread_page", 1);
        maxPage = inState.getInt("thread_maxpage", 1);
        postId = inState.getLong("post_id", 0);
        forumId = inState.getInt("thread_forum_id", 0);
        bookmarked = inState.getBoolean("thread_bookmarked");
        canReply = inState.getBoolean("thread_canreply");
        rawThreadTitle = inState.getString("thread_title");
        if(!TextUtils.isEmpty(rawThreadTitle)){
            threadTitle = Html.fromHtml(rawThreadTitle);
            setTitle(threadTitle);
        }
        updateNavbar();
        threadView.loadDataWithBaseURL(Constants.BASE_URL, pageHtml, "text/html", "utf-8", null);
    }

    /**
     * Updates current navbar state.
     * Note: navNext acts as next button when `page < maxPage`, but as refresh otherwise.
     */
    private void updateNavbar() {
        navPrev.setEnabled(page > 1 && !disableNavLoading);
        navPageBar.setText("Page "+Math.max(page, 0)+"/"+maxPage);
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
        MenuItem reply = menu.findItem(R.id.menu_thread_reply);
        if(reply != null){
            reply.setEnabled(canReply);
            reply.setTitle(canReply ? R.string.menu_thread_reply : R.string.menu_thread_reply_closed);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_thread_reply:
                startActivityForResult(
                        new Intent(getActivity(), ReplyActivity.class)
                                .putExtra("thread_id", threadId)
                                .putExtra("reply_type", ReplyFragment.TYPE_REPLY),
                        REQUEST_REPLY
                );
                return true;
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

    /**
     * Begins thread page request, loading thread posts.
     * If the threadID is not known but a postID exists, will do a redirect to that postId.
     * Otherwise if threadId is known but page isn't specified will do a 'nextpost' request.
     * If threadId and page is specified, requests that page.
     * @param pullToRefresh Whether request was triggered by PTR.
     * @param staleRefresh Whether request triggered by stale timeout. (Not used here in ThreadView)
     */
    @Override
    public void refreshData(boolean pullToRefresh, boolean staleRefresh) {
        threadView.stopLoading();
        disableNavLoading = true;
        updateNavbar();
        FastVolley.cancelRequestByTag(THREAD_REQUEST_TAG);
        if(threadId > 0){
            if(userId != 0) {
                queueRequest(new ThreadPageRequest(getActivity(), threadId, page, userId, pageListener, errorListener), THREAD_REQUEST_TAG);
            }
            else {
                queueRequest(new ThreadPageRequest(getActivity(), threadId, page, pageListener, errorListener), THREAD_REQUEST_TAG);
            }
        }else if(postId > 0){
            queueRequest(new ThreadPageRequest(getActivity(), postId, pageListener, errorListener));
        }
        getHandler().postDelayed(enableNavigation, 1000);
    }

    /**
     * Runnable trigger that re-enables navigation.
     * Navbar buttons are disabled for a second after the initial trigger, to prevent double-tap.
     * This runnable is queued for approx 1 second, at which point it re-enables navbar and refreshes.
     */
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
            ignorePageProgress = false;
            disableNavLoading = false;
            threadId = response.threadId;
            postId = 0;
            page = response.pageNum;
            maxPage = response.maxPageNum;
            if (!TextUtils.isEmpty(response.threadTitle)) {
                threadTitle = Html.fromHtml(response.threadTitle);
            }
            threadView.loadDataWithBaseURL(Constants.BASE_URL, response.pageHtml, "text/html", "utf-8", null);
            pageHtml = response.pageHtml;
            rawThreadTitle = response.threadTitle;
            bookmarked = response.bookmarked;
            canReply = response.canReply;

            //post update to threadlist/activity
            SomeApplication.bus.post(response);

            setTitle(threadTitle);
            updateNavbar();
            invalidateOptionsMenu();
            forumId = response.forumId;
            updateActionbarColor(response.forumId);
        }
    };

    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            disableNavLoading = false;
            updateNavbar();
        }
    };

    /**
     * Trigger page load of specific thread. Called by thread list or url links.
     * See startRefresh() and ./request/ThreadPageRequest for volley implementation.
     * @param threadId Thread ID for requested thread. (Required)
     * @param page Page number to load, Optional, if -1 will go to last post, if 0 will go to newest unread post.
     * @param userId (Optional) UserId for filtering.
     * @param fromUrl True if request was sent by internal URL request. Used to decide if we should push current state into backstack.
     */
    public void loadThread(int threadId, int page, int userId, boolean fromUrl) {
        if(fromUrl && isThreadLoaded()){
            threadBackstack.push(saveThreadState(new Bundle()));
        }else{
            threadBackstack.clear();
        }
        this.ignorePageProgress = true;
        this.threadId = threadId;
        this.page = page;
        this.userId = userId;
        this.maxPage = 0;
        this.forumId = 0;
        this.bookmarked = false;
        this.threadTitle = new SpannedString(getString(R.string.thread_view_loading));
        setTitle(threadTitle);
        invalidateOptionsMenu();
        updateNavbar();
        startRefresh();
        threadView.loadUrl("about:blank");
    }


    /**
     * Trigger page load of specific thread. Called by thread list or url links.
     * See startRefresh() and ./request/ThreadPageRequest for volley implementation.
     * @param threadId Thread ID for requested thread. (Required)
     * @param page Page number to load, Optional, if -1 will go to last post, if 0 will go to newest unread post.
     * @param fromUrl True if request was sent by internal URL request. Used to decide if we should push current state into backstack.
     */
    public void loadThread(int threadId, int page, boolean fromUrl){
        loadThread(threadId,page,0,fromUrl);
    }


    /**
     * Trigger page load of specific thread by redirecting from a postID. Called by thread list or url links.
     * See startRefresh() and ./request/ThreadPageRequest for volley implementation.
     * @param postId Post ID to redirect to. (Required)
     * @param fromUrl True if request was sent by internal URL request. Used to decide if we should push current state into backstack.
     */
    public void loadPost(long postId, boolean fromUrl){
        if(fromUrl && isThreadLoaded()){
            threadBackstack.push(saveThreadState(new Bundle()));
        }else{
            threadBackstack.clear();
        }
        this.ignorePageProgress = true;
        this.postId = postId;
        this.threadId = 0;
        this.page = 0;
        this.maxPage = 0;
        this.forumId = 0;
        this.bookmarked = false;
        this.threadTitle = new SpannedString(getString(R.string.thread_view_loading));
        setTitle(threadTitle);
        invalidateOptionsMenu();
        updateNavbar();
        startRefresh();
        threadView.loadUrl("about:blank");
    }

    /**
     * Called when user presses back button.
     * If we have backstack elements, consumes event and triggers pop.
     * @return True if we are consuming back-press, false allows event to continue. (Event may be intercepted elsewhere or will bubble up to system)
     */
    public boolean overrideBackPressed() {
        if(threadBackstack.peek() != null){
            loadThreadState(threadBackstack.pop());
            return true;
        }else{
            return false;
        }
    }

    /**
     * Triggers page navigation.
     * @param pageNum Target page number, must be valid page between 1 and current max.
     */
    public void goToPage(int pageNum){
        if(pageNum <= maxPage && pageNum > 0){
            page = pageNum;
            updateNavbar();
            startRefresh();
        }
    }

    /**
     * Checks to see if page HTML has loaded, used to enable/disable specific UI elements.
     * Does not indicate if WebView has caught up and fully rendered, it may still be rendering.
     * @return True if page request has returned properly and we have a valid thread or post id.
     */
    public boolean isThreadLoaded() {
        return pageHtml != null || threadId > 0 || postId > 0;
    }

    /**
     * Current thread title.
     * @return Current thread title, HTML entities already converted.
     */
    public CharSequence getTitle() {
        return threadTitle;
    }

    private WebChromeClient chromeClient = new WebChromeClient(){

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            Log.d("WebView", "Progress: "+newProgress);
            //ignorePageProgress is a hack to keep the chromeClient progress events from triggering
            //the loading bar when the webview is doing a page-clear or other event the user doesn't care about
            if(!ignorePageProgress){
                setProgress(newProgress);
            }
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
            //ignorePageProgress is a hack to keep the chromeClient progress events from triggering
            //the loading bar when the webview is doing a page-clear or other event the user doesn't care about
            if(!ignorePageProgress){
                setProgress(100);
            }
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            Log.d("WebView", "onLoadResource: " + url);
        }

        /**
         * Handle custom URLs and events here.
         * 'something://something-X' urls are simple event triggers.
         * Additionally, any real SA urls are parsed and handled here.
         * Other links are sent to the OS url handler, for browser or external apps to handle.
         * @param view
         * @param url
         * @return
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
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
            SomeURL.handleUrl(getActivity(), url);
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

    /**
     * Displays page select dialog. Once page is selected, this fragment will call 'goToPage()' in this fragment with the results.
     */
    private void displayPageSelect(){
        PageSelectDialogFragment.newInstance(page, maxPage, ThreadViewFragment.this).show(getFragmentManager(), "page_select");
    }

    @Override
    public void onRefreshStartedFromBottom(View view) {
        if(page < maxPage){
            goToPage(page+1);
        }else{
            setRefreshAnimation(true);
            //request unread page, if no new unread messages appear it will only return "lastpost", otherwise will return PTI for new unread post
            queueRequest(new ThreadPageRequest(getActivity(), threadId, 0, pageListener, errorListener), THREAD_REQUEST_TAG);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_REPLY){
            if(resultCode > 0){
                loadPost(resultCode, false);
            }else if(data != null && data.getIntExtra("thread_id", 0) > 0){
                loadThread(data.getIntExtra("thread_id", 0), 0, false);
            }
        }else if(requestCode == REQUEST_NEW_PM && resultCode > 0){
            FastAlert.notice(this, R.string.reply_sent_pm);
        }
    }

    public int getThreadId() {
        return threadId;
    }

    public class SomeJavascriptInterface {

        @JavascriptInterface
        public void onQuoteClick(String postId){
            startActivityForResult(
                    new Intent(getActivity(), ReplyActivity.class)
                            .putExtra("thread_id", threadId)
                            .putExtra("post_id", Integer.parseInt(postId))
                            .putExtra("reply_type", ReplyFragment.TYPE_QUOTE),
                    REQUEST_REPLY
            );
        }

        @JavascriptInterface
        public void onEditClick(String postId){
            startActivityForResult(
                    new Intent(getActivity(), ReplyActivity.class)
                            .putExtra("post_id", Integer.parseInt(postId))
                            .putExtra("reply_type", ReplyFragment.TYPE_EDIT),
                    REQUEST_REPLY
            );
        }

        @JavascriptInterface
        public void onMoreClick(final String postId, final String username, final String userId){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showMoreDialog(postId, username, Integer.parseInt(userId));
                }
            });
        }

        @JavascriptInterface
        public void onLastReadClick(String postIndex){
            final int index = FastUtils.safeParseInt(postIndex, 0);
            if(index > 0){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FastAlert.process(getActivity(), getView(), getSafeString(R.string.mark_last_read_started));
                        queueRequest(new MarkLastReadRequest(getActivity(), threadId, index, new Response.Listener<ThreadPageRequest.ThreadPage>() {
                            @Override
                            public void onResponse(ThreadPageRequest.ThreadPage response) {
                                Activity activity = getActivity();
                                if(activity instanceof MainActivity && ((MainActivity) activity).isFragmentFocused(ThreadViewFragment.this)){
                                    FastAlert.notice(activity, getView(), getSafeString(R.string.mark_last_read_success));
                                }
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

    private void showMoreDialog(final String postId, final String username, final int userId){
        if(this.userId ==0) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.post_more_title)
                    .setItems(R.array.more_actions_normal, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                //See R.array.more_actions_normal for item list
                                case 0://PM
                                    startActivityForResult(
                                            new Intent(getActivity(), ReplyActivity.class)
                                                    .putExtra("pm_id", ReplyFragment.NEW_PM)
                                                    .putExtra("pm_username", username)
                                                    .putExtra("reply_type", ReplyFragment.TYPE_PM),
                                            REQUEST_NEW_PM
                                    );
                                    break;
                                case 1://Filter posts
                                    getActivity().startActivity(
                                            new Intent(getActivity(), MainActivity.class)
                                                    .putExtra("thread_id", threadId)
                                                    .putExtra("thread_page", 1)
                                                    .putExtra("user_id", userId)
                                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    );
                                    break;
                                case 2://Share link
                                    String url = "http://forums.somethingawful.com/showthread.php?goto=post&postid=" + postId + "#post" + postId;
                                    FastUtils.showSimpleShareChooser(getActivity(), threadTitle.toString(), url, getSafeString(R.string.share_url_title));
                                    break;
                                case 3://Copy Link
                                    String postUrl = "http://forums.somethingawful.com/showthread.php?goto=post&postid=" + postId + "#post" + postId;
                                    FastUtils.copyToClipboard(getActivity(), threadTitle.toString(), postUrl);
                                    FastAlert.notice(ThreadViewFragment.this, R.string.link_copied, R.drawable.ic_menu_link);
                                    break;
                            }
                        }
                    })
                    .show();
        }
        else {
            new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.post_more_title)
                    .setItems(R.array.more_actions_filtered, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                //See R.array.more_actions_normal for item list
                                case 0://PM
                                    startActivityForResult(
                                            new Intent(getActivity(), ReplyActivity.class)
                                                    .putExtra("pm_id", ReplyFragment.NEW_PM)
                                                    .putExtra("pm_username", username)
                                                    .putExtra("reply_type", ReplyFragment.TYPE_PM),
                                            REQUEST_NEW_PM
                                    );
                                    break;
                                case 1://Filter posts
                                    getActivity().startActivity(
                                            new Intent(getActivity(), MainActivity.class)
                                                    .putExtra("thread_id", threadId)
                                                    .putExtra("thread_page", 1)
                                                    .putExtra("user_id", 0)
                                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                    );
                                    break;
                                case 2://Share link
                                    String url = "http://forums.somethingawful.com/showthread.php?goto=post&postid=" + postId + "#post" + postId;
                                    FastUtils.showSimpleShareChooser(getActivity(), threadTitle.toString(), url, getSafeString(R.string.share_url_title));
                                    break;
                                case 3://Copy Link
                                    String postUrl = "http://forums.somethingawful.com/showthread.php?goto=post&postid=" + postId + "#post" + postId;
                                    FastUtils.copyToClipboard(getActivity(), threadTitle.toString(), postUrl);
                                    FastAlert.notice(ThreadViewFragment.this, R.string.link_copied, R.drawable.ic_menu_link);
                                    break;
                            }
                        }
                    })
                    .show();
        }
    }

    @Override
    protected void setTitle(CharSequence title) {
        Activity act = getActivity();
        if(act instanceof MainActivity){
            ((MainActivity)act).setTitle(title, this);
        }
    }

    /**
     * Hacky support for dynamic actionbar color changes based on theme/specialty theme.
     * @param forumId
     */
    protected void updateActionbarColor(int forumId){
        Activity act = getActivity();
        if(forumId > 0 && act instanceof SomeActivity){
            SomeActivity sact = (SomeActivity) act;
            sact.setActionbarColor(SomeTheme.getActionbarColorForForum(forumId, sact.getActionbarDefaultColor()));
        }
    }

    public int getForumId() {
        return forumId;
    }

    /**
     * Header class for custom extended ActionbarPullToRefresh.
     * This portion only updates the view system, custom PTR library implements pull-from-bottom.
     * See onRefreshStartedFromBottom() for event callback.
     */
    private HeaderTransformer header = new HeaderTransformer(){
        private int nextPageColor, refreshColor;

        private final Interpolator mInterpolator = new AccelerateInterpolator();

        @Override
        public boolean showHeaderView() {
            boolean changed = pfbContainer.getVisibility() != View.VISIBLE;

            if(changed){
                updateHeaderState();
                pfbContainer.setVisibility(View.VISIBLE);
                AnimatorSet animSet = new AnimatorSet();
                ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(pfbContainer, "alpha", 0f, 1f);
                animSet.play(alphaAnim);
                animSet.setDuration(200);
                animSet.start();
            }

            return changed;
        }

        @Override
        public boolean hideHeaderView() {
            boolean changed = pfbContainer.getVisibility() != View.GONE;

            if(changed){
                Animator animator;
                if (pfbContainer.getAlpha() >= 0.5f) {
                    // If the content layout is showing, translate and fade out
                    animator = new AnimatorSet();
                    ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(pfbContainer, "alpha", 1f, 0f);
                    ((AnimatorSet) animator).play(alphaAnim);
                } else {
                    // If the content layout isn't showing (minimized), just fade out
                    animator = ObjectAnimator.ofFloat(pfbContainer, "alpha", 1f, 0f);
                }
                animator.setDuration(200);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        onReset();
                    }
                });
                animator.start();
            }

            return changed;
        }

        @Override
        public void onViewCreated(Activity activity, View headerView) {
            int[] ptrColorArray = getResources().getIntArray(SomeTheme.getThemeResource(getActivity(), R.attr.progressBarColorArray, R.array.sbp_colors_light));
            nextPageColor = SomeTheme.getThemeColor(getActivity(), R.attr.progressBarColor, Color.rgb(20,146,204));
            refreshColor = SomeTheme.getThemeColor(getActivity(), R.attr.progressBarColorRefresh, Color.rgb(190, 190, 190));

            final int strokeWidth = getResources().getDimensionPixelSize(R.dimen.pull_to_refresh_stroke);

            if(ptrColorArray != null){
                pfbProgressbar.setIndeterminateDrawable(
                        new SmoothProgressDrawable.Builder(getActivity())
                                .colors(ptrColorArray)
                                .sectionsCount(6)
                                .separatorLength(0)
                                .strokeWidth(strokeWidth)
                                .build()
                );
            }else{
                pfbProgressbar.setIndeterminateDrawable(
                        new SmoothProgressDrawable.Builder(getActivity())
                                .color(nextPageColor)
                                .sectionsCount(6)
                                .separatorLength(0)
                                .strokeWidth(strokeWidth)
                                .build());
            }
            updateHeaderState();
        }

        private void updateHeaderState(){
            ShapeDrawable shape = new ShapeDrawable();
            shape.setShape(new RectShape());
            shape.getPaint().setColor(page < maxPage ? nextPageColor : refreshColor);
            ClipDrawable clipDrawable = new ClipDrawable(shape, Gravity.CENTER, ClipDrawable.HORIZONTAL);
            pfbProgressbar.setProgressDrawable(clipDrawable);

            pfbTitle.setText(page < maxPage ? R.string.pull_bottom_nextpage : R.string.pull_to_refresh_pull_label);
        }

        @Override
        public void onReset() {
            pfbContainer.setVisibility(View.GONE);
            pfbProgressbar.setProgress(0);
            pfbProgressbar.setIndeterminate(false);
            Compat.setAlpha(pfbContainer, 1f);
            updateHeaderState();
        }

        @Override
        public void onPulled(float percentagePulled, boolean mCurrentPullIsUp) {
            final float progress = mInterpolator.getInterpolation(percentagePulled);
            pfbProgressbar.setProgress(Math.round(pfbProgressbar.getMax() * progress));
        }

        @Override
        public void onRefreshStarted() {
            pfbTitle.setText(R.string.thread_view_loading);
            pfbProgressbar.setIndeterminate(true);
        }

        @Override
        public void onReleaseToRefresh(boolean mCurrentPullIsUp) {
            if(page == maxPage){
                pfbTitle.setText(R.string.pull_bottom_release_refresh);
            }else{
                pfbTitle.setText(R.string.pull_bottom_release_nexpage);
            }
            pfbProgressbar.setProgress(pfbProgressbar.getMax());
        }

        @Override
        public void onRefreshMinimized() {
        }

        @Override
        public void onConfigurationChanged(Activity activity, Configuration newConfig) {
        }
    };
}
