package com.salvadordalvik.something;

import android.app.Activity;
;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class MainActivity extends Activity implements SlidingPaneLayout.PanelSlideListener, SlidingMenu.OnCloseListener, SlidingMenu.OnOpenListener {
    private SlidingMenu slidingMenu;
    private SlidingPaneLayout slidingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configureSlidingMenu();
        configureActionbar();
        configureSlidingLayout();
    }

    private void configureSlidingMenu(){
        slidingMenu = new SlidingMenu(this, SlidingMenu.SLIDING_WINDOW);
        slidingMenu.setMenu(R.layout.threadlist_fragment);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        slidingMenu.setTouchModeBehind(SlidingMenu.TOUCHMODE_FULLSCREEN);
        slidingMenu.setBehindOffset(100);
        slidingMenu.setOnOpenListener(this);
        slidingMenu.setOnCloseListener(this);
    }

    private void configureActionbar(){
        ActionBar bar = getActionBar();
        bar.setHomeButtonEnabled(true);
    }

    private void configureSlidingLayout(){
        slidingLayout = (SlidingPaneLayout) findViewById(R.id.sliding_layout);
        slidingLayout.setPanelSlideListener(this);
        slidingLayout.openPane();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                if(slidingLayout.isOpen()){
                    slidingMenu.toggle();
                }else{
                    slidingLayout.openPane();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(!slidingLayout.isOpen()){
            slidingLayout.openPane();
        }else if(slidingMenu.isMenuShowing()){
            slidingMenu.showContent();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public void onPanelSlide(View view, float v) {
        slidingMenu.setSlidingEnabled(v > 0.5f);
    }

    @Override
    public void onPanelOpened(View view) {

    }

    @Override
    public void onPanelClosed(View view) {

    }

    @Override
    public void onOpen() {

    }

    @Override
    public void onClose() {

    }
}
