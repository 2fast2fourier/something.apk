package net.fastfourier.something;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import net.fastfourier.something.request.PreviewRequest;
import net.fastfourier.something.util.Constants;
import net.fastfourier.something.util.SomeURL;

/**
 * Created by Timothy Miller on 2014/06/28.
 */
public class PreviewFragment extends SomeFragment implements Response.ErrorListener, Response.Listener<PreviewRequest.PreviewData> {

    private boolean ignorePageProgress = true;
    private WebView threadView;
    public String threadHtml;
    public PreviewFragment() {
        super(R.layout.reply_preview_fragment, R.menu.preview_view);
    }

    public CharSequence getTitle() {
        return null;
    }

    @Override
    public void viewCreated(View frag, Bundle savedInstanceState) {
        threadView = (WebView) frag.findViewById(R.id.preview_webview);
        initWebview();
        // TODO: Change back to loadDataWithBaseURL, as it won't load the CSS without it.
        threadView.loadData(this.threadHtml, "text/html", "utf-8");
    }

    @Override
    public void refreshData(boolean pullToRefresh, boolean staleRefresh) {

    }

    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getActivity().getIntent();
        threadHtml = intent.getStringExtra("threadHtml");
    }

    // TODO: This code is repeated in several places. We should move it under one helper class.
    @SuppressLint("SetJavaScriptEnabled")
    private void initWebview() {
        threadView.getSettings().setJavaScriptEnabled(true);
        threadView.setWebChromeClient(chromeClient);
        threadView.setWebViewClient(webClient);
        //threadView.addJavascriptInterface(new SomeJavascriptInterface(), "listener");

        TypedValue val = new TypedValue();
        if(getActivity().getTheme().resolveAttribute(R.attr.webviewBackgroundColor, val, true)){
            threadView.setBackgroundColor(val.data);
        }

        registerForContextMenu(threadView);
    }

    private WebChromeClient chromeClient = new WebChromeClient(){
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            Log.d("WebView", "Progress: " + newProgress);
            if(!ignorePageProgress){

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

            }
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
            Log.d("WebView", "onLoadResource: " + url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("WebView", "shouldOverrideUrlLoading: "+url);
            SomeURL.handleUrl(getActivity(), url);
            return true;
        }
    };

    @Override
    public void onResponse(PreviewRequest.PreviewData response) {
        threadView.loadDataWithBaseURL(Constants.BASE_URL, response.htmlData, "text/html", "utf-8", null);
    }
}
