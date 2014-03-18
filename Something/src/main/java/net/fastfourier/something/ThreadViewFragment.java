package net.fastfourier.something;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
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
import android.webkit.CookieManager;
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
import net.fastfourier.something.util.SomePreferences;
import net.fastfourier.something.util.SomeTheme;
import net.fastfourier.something.util.SomeURL;
import net.fastfourier.something.widget.PageSelectDialogFragment;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private int page;
    private int maxPage;
    private int forumId;
    private CharSequence threadTitle;
    private String pageHtml, rawThreadTitle;
    private boolean bookmarked;

    private LinkedList<Bundle> threadBackstack = new LinkedList<Bundle>();

    private ImageView navPrev, navNext;
    private TextView navPageBar;
    private boolean disableNavLoading = false;

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
        return Options.create().scrollDistance(getScrollDistance()).headerTransformer(header).build();
    }

    private float getScrollDistance(){
        return Math.max(Math.min(FastUtils.calculateScrollDistance(getActivity(), 2.5f), 0.666f), 0.333f);
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
            updateActionbarColor(forumId);
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

    private Bundle saveThreadState(Bundle outState){
        outState.putString("thread_html", pageHtml);
        outState.putInt("thread_id", threadId);
        outState.putInt("thread_page", page);
        outState.putInt("thread_maxpage", maxPage);
        outState.putString("thread_title", rawThreadTitle);
        outState.putLong("post_id", postId);
        outState.putInt("thread_forum_id", forumId);
        outState.putBoolean("thread_bookmarked", bookmarked);
        return outState;
    }

    private void loadThreadState(Bundle inState){
        threadId = inState.getInt("thread_id", 0);
        pageHtml = inState.getString("thread_html");
        page = inState.getInt("thread_page", 1);
        maxPage = inState.getInt("thread_maxpage", 1);
        postId = inState.getLong("post_id", 0);
        forumId = inState.getInt("thread_forum_id", 0);
        bookmarked = inState.getBoolean("thread_bookmarked");
        rawThreadTitle = inState.getString("thread_title");
        if(!TextUtils.isEmpty(rawThreadTitle)){
            threadTitle = Html.fromHtml(rawThreadTitle);
            setTitle(threadTitle);
        }

        threadView.loadDataWithBaseURL(Constants.BASE_URL, pageHtml, "text/html", "utf-8", null);
    }

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
    @Override
    public void refreshData(boolean pullToRefresh, boolean staleRefresh) {
        threadView.stopLoading();
        disableNavLoading = true;
        updateNavbar();
        FastVolley.cancelRequestByTag(THREAD_REQUEST_TAG);
        if(threadId > 0){
            queueRequest(new ThreadPageRequest(threadId, page, pageListener, errorListener), THREAD_REQUEST_TAG);
        }else if(postId > 0){
            queueRequest(new ThreadPageRequest(postId, pageListener, errorListener));
        }
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
            Activity act = getActivity();
            if (act instanceof MainActivity) {
                MainActivity main = (MainActivity) act;
                main.onThreadPageLoaded(response.threadId);
            }
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

    public void loadThread(int threadId, int page, boolean fromUrl){
        if(fromUrl && isThreadLoaded()){
            threadBackstack.push(saveThreadState(new Bundle()));
        }else{
            threadBackstack.clear();
        }
        this.ignorePageProgress = true;
        this.threadId = threadId;
        this.page = page;
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

    public boolean overrideBackPressed() {
        if(threadBackstack.peek() != null){
            loadThreadState(threadBackstack.pop());
            return true;
        }else{
            return false;
        }
    }

    public void goToPage(int pageNum){
        if(pageNum <= maxPage && pageNum > 0){
            page = pageNum;
            updateNavbar();
            startRefresh();
        }
    }

    public boolean isThreadLoaded() {
        return pageHtml != null || threadId > 0 || postId > 0;
    }

    public CharSequence getTitle() {
        return threadTitle;
    }

    private WebChromeClient chromeClient = new WebChromeClient(){

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            Log.d("WebView", "Progress: "+newProgress);
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
            if(!ignorePageProgress){
                setProgress(100);
            }
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            Log.d("WebView", "onLoadResource: " + url);
        }

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

    private void displayPageSelect(){
        PageSelectDialogFragment.newInstance(page, maxPage, ThreadViewFragment.this).show(getFragmentManager(), "page_select");
    }

    @Override
    public void onRefreshStartedFromBottom(View view) {
        if(page < maxPage){
            goToPage(page+1);
        }else{
            startRefresh(false, true);
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
        public void onMoreClick(String postId, String username, String userid){
            showMoreDialog(postId, username, userid);
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

    private void showMoreDialog(final String postId, final String username, final String userid){
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
                                //TODO not implemented yet
                                FastAlert.error(ThreadViewFragment.this, "NOT IMPLEMENTED YET");
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
                            case 4://Profile
                                //TODO not implemented yet
                                FastAlert.error(ThreadViewFragment.this, "NOT IMPLEMENTED YET");
                                break;
                            case 5://Rapsheet
                                //TODO not implemented yet
                                FastAlert.error(ThreadViewFragment.this, "NOT IMPLEMENTED YET");
                                break;
                        }
                    }
                })
                .show();
    }

    @Override
    protected void setTitle(CharSequence title) {
        Activity act = getActivity();
        if(act instanceof MainActivity){
            ((MainActivity)act).setTitle(title, this);
        }
    }

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
            pfbTitle.setText(R.string.pull_bottom_release_nexpage);
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
