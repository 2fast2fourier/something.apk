package com.salvadordalvik.something;

import android.app.Application;
import android.webkit.CookieSyncManager;

import com.salvadordalvik.fastlibrary.request.FastVolley;
import com.salvadordalvik.something.data.SomeDatabase;
import com.salvadordalvik.something.util.MustCache;
import com.salvadordalvik.something.util.OkHttpStack;
import com.salvadordalvik.something.util.SomePreferences;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by matthewshepard on 1/17/14.
 */
public class SomeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SomeDatabase.init(this);
        FastVolley.init(this, new OkHttpStack());
        SomePreferences.init(this);
        MustCache.init(this);
    }
}
