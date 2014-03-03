package com.salvadordalvik.something;

import android.app.ActionBar;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;

import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.salvadordalvik.something.util.SomePreferences;

/**
 * Created by matthewshepard on 3/1/14.
 */
public class SomeActivity extends FragmentActivity {
    private LayerDrawable actionbarBackgroundList;
    private ColorDrawable actionbarColor;
    private SystemBarTintManager tint;

    private int defaultActionbarColor, currentActionbarColor;

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

        tint = new SystemBarTintManager(this);
        TypedValue tintColor = new TypedValue();
        if(getTheme().resolveAttribute(R.attr.statusBarBackground, tintColor, true)){
            tint.setStatusBarTintEnabled(true);
            tint.setTintColor(tintColor.data);
            defaultActionbarColor = tintColor.data;
            currentActionbarColor = tintColor.data;
        }else{
            tint.setStatusBarTintEnabled(false);
        }

        TypedValue actionbarBackground = new TypedValue();
        if(getTheme().resolveAttribute(R.attr.actionbarBackgroundLayerList, actionbarBackground, false)){
            actionbarBackgroundList = (LayerDrawable) getResources().getDrawable(actionbarBackground.data);
            actionbarBackgroundList.mutate();
            actionbarColor = (ColorDrawable) actionbarBackgroundList.findDrawableByLayerId(R.id.actionbar_background_color);
            actionbarColor.mutate();
            bar.setBackgroundDrawable(actionbarBackgroundList);
        }

    }

    protected void setActionbarColor(int color){
        if(actionbarColor != null && color != currentActionbarColor){
            actionbarColor.setColor(color);
            currentActionbarColor = color;
            if(tint.isStatusBarTintEnabled()){
                tint.setTintColor(color);
            }
        }
    }

    protected void setActionbarColorToDefault(){
        if(actionbarColor != null && currentActionbarColor != defaultActionbarColor){
            actionbarColor.setColor(defaultActionbarColor);
            currentActionbarColor = defaultActionbarColor;
            if(tint.isStatusBarTintEnabled()){
                tint.setTintColor(defaultActionbarColor);
            }
        }
    }

    protected int getActionbarDefaultColor(){
        return defaultActionbarColor;
    }

    protected int getCurrentActionbarColor(){
        return currentActionbarColor;
    }
}
