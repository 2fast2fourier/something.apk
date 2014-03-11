package net.fastfourier.something;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.salvadordalvik.fastlibrary.FastFragment;
import com.salvadordalvik.fastlibrary.util.FastUtils;

/**
 * Created by matthewshepard on 2/10/14.
 */
public class ReplyPreviewFragment extends FastFragment {
    public WebView replyView;

    public ReplyPreviewFragment() {
        super(R.layout.reply_preview_fragment, R.menu.post_reply);
    }

    @Override
    public void viewCreated(View frag, Bundle savedInstanceState) {
        replyView = (WebView) frag.findViewById(R.id.reply_webview);
        initWebview();
    }

    @Override
    public void refreshData(boolean pullToRefresh, boolean staleRefresh) {

    }

    private void initWebview() {
        replyView.getSettings().setJavaScriptEnabled(true);
        replyView.setWebChromeClient(chromeClient);
        replyView.setWebViewClient(webClient);
        replyView.addJavascriptInterface(new ReplyJavascriptInterface(), "listener");

        replyView.setBackgroundColor(Color.BLACK);

        registerForContextMenu(replyView);
    }

    public class ReplyJavascriptInterface {

        @JavascriptInterface
        public void onReplySubmit(String postContent){

        }

        @JavascriptInterface
        public void onReplyPreview(String postContent){

        }
    }

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
}
