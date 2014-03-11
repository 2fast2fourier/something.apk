package net.fastfourier.something.util;

import android.util.Log;

import com.android.volley.toolbox.HurlStack;
import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by matthewshepard on 1/19/14.
 */
public class OkHttpStack extends HurlStack{
    private final OkHttpClient client;

    public OkHttpStack() {
        this(new OkHttpClient());
    }

    public OkHttpStack(OkHttpClient client) {
        if (client == null) {
            throw new NullPointerException("Client must not be null.");
        }
        this.client = client;
    }

    @Override protected HttpURLConnection createConnection(URL url) throws IOException {
        return client.open(url);
    }

    public static boolean saveCookies(){
        try {
            Map<String, List<String>> cookies = CookieManager.getDefault().get(URI.create("https://forums.somethingawful.com"), new HashMap<String, List<String>>());
            List<String> cookieList = cookies.get("Cookie");
            for(String cookie : cookieList){
                if(cookie.contains("bbuserid")){
                    SomePreferences.setString(SomePreferences.LOGIN_COOKIE_STRING, cookie);
                    Log.e("SaveCookies", SomePreferences.cookieString);
                    CookieManager.setDefault(null);
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        };
        return false;
    }
}
