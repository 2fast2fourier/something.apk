package com.salvadordalvik.something;

import android.app.Activity;
;
import android.app.ActionBar;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.MenuItem;
import android.view.View;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class MainActivity extends Activity implements SlidingPaneLayout.PanelSlideListener, SlidingMenu.OnCloseListener, SlidingMenu.OnOpenListener {
    private SlidingMenu slidingMenu;
    private SlidingPaneLayout slidingLayout;

    private ThreadListFragment threadList;
    private ThreadViewFragment threadView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configureSlidingMenu();
        configureActionbar();
        configureSlidingLayout();
        threadList = (ThreadListFragment) getFragmentManager().findFragmentById(R.id.threadlist_fragment);
        threadView = (ThreadViewFragment) getFragmentManager().findFragmentById(R.id.threadview_fragment);
    }

    private void configureSlidingMenu(){
        slidingMenu = new SlidingMenu(this, SlidingMenu.SLIDING_WINDOW);
        slidingMenu.setMenu(R.layout.ptr_generic_listview);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
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
        slidingLayout.setSliderFadeColor(Color.argb(0,0,0,0));
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

    public void showThread(int id) {
        showThread(id, 0);
    }

    public void showThread(int id, int page) {
        slidingLayout.closePane();
        threadView.loadThread(id, page);
    }
}
