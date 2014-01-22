package com.salvadordalvik.something;

import android.app.Application;

import com.salvadordalvik.fastlibrary.request.FastVolley;
import com.salvadordalvik.something.data.SomeDatabase;
import com.salvadordalvik.something.util.OkHttpStack;
import com.salvadordalvik.something.util.SomePreferences;

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
    }
}
