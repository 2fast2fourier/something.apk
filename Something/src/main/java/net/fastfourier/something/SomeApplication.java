package net.fastfourier.something;

import android.app.Application;
import android.webkit.CookieSyncManager;

import com.salvadordalvik.fastlibrary.request.FastVolley;
import com.salvadordalvik.fastlibrary.request.PersistentCookieStore;

import net.fastfourier.something.data.SomeDatabase;
import net.fastfourier.something.util.MustCache;
import net.fastfourier.something.util.OkHttpStack;
import net.fastfourier.something.util.SomePreferences;
import net.fastfourier.something.util.SomeTheme;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

/**
 * Created by matthewshepard on 1/17/14.
 */
public class SomeApplication extends Application {

    private static PersistentCookieStore cookieStore;

    @Override
    public void onCreate() {
        super.onCreate();
        SomeDatabase.init(this);
        cookieStore = new PersistentCookieStore(this);
        CookieHandler.setDefault(new CookieManager(cookieStore, CookiePolicy.ACCEPT_ALL));
        FastVolley.init(this, new OkHttpStack());
        SomePreferences.init(this);
        MustCache.init(this);
        CookieSyncManager.createInstance(this);
        SomeTheme.init(this);
    }

    public static void clearCookies() {
        if(cookieStore != null){
            cookieStore.removeAll();
        }
    }
}
