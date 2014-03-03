package com.salvadordalvik.something;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.TypedValue;

import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.salvadordalvik.something.util.SomePreferences;

/**
 * Created by matthewshepard on 3/1/14.
 */
public class SomeActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(SomePreferences.systemTheme);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        configureActionbar();
    }

    private void configureActionbar(){
        ActionBar bar = getActionBar();
        bar.setHomeButtonEnabled(true);
        bar.setDisplayShowHomeEnabled(false);

        SystemBarTintManager tint = new SystemBarTintManager(this);
        TypedValue tintColor = new TypedValue();
        if(getTheme().resolveAttribute(R.attr.statusBarBackground, tintColor, true)){
            tint.setStatusBarTintEnabled(true);
            tint.setTintColor(tintColor.data);
        }else{
            tint.setStatusBarTintEnabled(false);
        }
    }
}
