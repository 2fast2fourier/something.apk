package com.salvadordalvik.something;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.salvadordalvik.fastlibrary.FastFragment;
import com.salvadordalvik.fastlibrary.list.FastAdapter;
import com.salvadordalvik.fastlibrary.util.FastUtils;
import com.salvadordalvik.something.request.ThreadPageRequest;
import com.salvadordalvik.something.util.Constants;

import org.w3c.dom.Text;

/**
 * Created by matthewshepard on 1/19/14.
 */
public class ThreadViewFragment extends FastFragment {
    private WebView threadView;

    private int threadId, page, maxPage, forumId;
    private Spanned threadTitle;

    public ThreadViewFragment() {
        super(R.layout.ptr_generic_webview);
    }

    @Override
    public void viewCreated(View frag, Bundle savedInstanceState) {
        threadView = (WebView) frag.findViewById(R.id.ptr_webview);
        initWebview();
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
    public void refreshData(boolean pullToRefresh, boolean staleRefresh) {
        queueRequest(new ThreadPageRequest(threadId, page, new Response.Listener<ThreadPageRequest.ThreadPage>() {
            @Override
            public void onResponse(ThreadPageRequest.ThreadPage response) {
                page = response.pageNum;
                maxPage = response.maxPageNum;
                forumId = response.forumId;
                if(!TextUtils.isEmpty(response.threadTitle)){
                    threadTitle = Html.fromHtml(response.threadTitle);
                }
                threadView.loadDataWithBaseURL(Constants.BASE_URL, response.pageHtml, "text/html", "utf-8", null);
                getActivity().setTitle(threadTitle);
            }
        }, null));
    }

    public void loadThread(int threadId, int page){
        this.threadId = threadId;
        this.page = page;
        startRefresh();
    }

    protected void goToPage(int pageNum){
        if(pageNum <= maxPage && pageNum > 0){
            page = pageNum;
            startRefresh();
        }
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
