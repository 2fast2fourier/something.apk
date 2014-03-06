package com.salvadordalvik.something;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
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
import com.salvadordalvik.something.request.PrivateMessageRequest;
import com.salvadordalvik.something.util.Constants;

/**
 * Created by matthewshepard on 2/12/14.
 */
public class PrivateMessageFragment extends SomeFragment implements Response.ErrorListener, Response.Listener<PrivateMessageRequest.PMData> {
    private WebView webview;

    private int pmId = 0;
    private String pmTitle;

    public PrivateMessageFragment() {
        super(R.layout.generic_webview, R.menu.pm_reply);
    }

    @Override
    public void viewCreated(View frag, Bundle savedInstanceState) {
        Intent intent = getActivity().getIntent();
        pmId = intent.getIntExtra("pm_id", 0);
        pmTitle = intent.getStringExtra("pm_title");

        webview = (WebView) frag.findViewById(R.id.webview);
        initWebview();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(pmId > 0){
            startRefresh();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        webview.onResume();
        webview.resumeTimers();
    }

    public void onPaneRevealed() {
        if(isResumed()){
            webview.onResume();
            webview.resumeTimers();
        }
    }

    public void onPaneObscured() {
        if(isResumed()){
            webview.pauseTimers();
            webview.onPause();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        webview.pauseTimers();
        webview.onPause();
    }

    @Override
    public void refreshData(boolean pullToRefresh, boolean staleRefresh) {
        queueRequest(new PrivateMessageRequest(pmId, pmTitle, this, this));
    }

    public void showPM(int pmId, String pmTitle){
        this.pmId = pmId;
        this.pmTitle = pmTitle;
        startRefresh();
    }

    private void initWebview() {
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebChromeClient(chromeClient);
        webview.setWebViewClient(webClient);

        TypedValue val = new TypedValue();
        if(getActivity().getTheme().resolveAttribute(R.attr.webviewBackgroundColor, val, true)){
            webview.setBackgroundColor(val.data);
        }

        registerForContextMenu(webview);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        WebView.HitTestResult result = webview.getHitTestResult();
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
                        FastAlert.custom(getActivity(), getView(), getSafeString(R.string.url_copied), null, R.drawable.ic_menu_link);
                        ClipboardManager clipman = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        clipman.setPrimaryClip(ClipData.newPlainText(webview.toString(), targetUrl));
                        return true;
                    }
                });
                menu.add(R.string.menu_share_link).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        FastUtils.showSimpleShareChooser(getActivity(), webview.toString(), targetUrl, getSafeString(R.string.menu_share_link));
                        return true;
                    }
                });
                break;
        }
    }

//    public class ReplyJavascriptInterface {
//
//        @JavascriptInterface
//        public void onReplySubmit(String postContent){
//
//        }
//
//        @JavascriptInterface
//        public void onReplyPreview(String postContent){
//
//        }
//    }

    private WebChromeClient chromeClient = new WebChromeClient(){
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            Log.d("WebView", "Progress: " + newProgress);
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
            FastUtils.startUrlIntent(getActivity(), url);
            return true;
        }
    };

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        FastAlert.error(getActivity(), getView(), getSafeString(R.string.loading_failed));
    }

    @Override
    public void onResponse(PrivateMessageRequest.PMData pmData) {
        webview.loadDataWithBaseURL(Constants.BASE_URL, pmData.htmlData, "text/html", "utf-8", null);
    }

    public boolean hasPMLoaded() {
        return pmId > 0;
    }
}
