package net.fastfourier.something;

import android.app.Application;
import android.webkit.CookieSyncManager;

import com.salvadordalvik.fastlibrary.request.FastVolley;
import net.fastfourier.something.data.SomeDatabase;
import net.fastfourier.something.util.MustCache;
import net.fastfourier.something.util.OkHttpStack;
import net.fastfourier.something.util.SomePreferences;
import net.fastfourier.something.util.SomeTheme;

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
        CookieSyncManager.createInstance(this);
        SomeTheme.init(this);
    }
}
