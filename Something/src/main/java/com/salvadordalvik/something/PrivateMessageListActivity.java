package com.salvadordalvik.something;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

/**
 * Created by matthewshepard on 2/7/14.
 */
public class PrivateMessageListActivity extends SomeActivity implements SlidingMenu.OnClosedListener, SlidingMenu.OnOpenedListener {
    private SlidingMenu slidingMenu;
    private PrivateMessageListFragment listFragment;
    private PrivateMessageFragment messageFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.private_message_activity);
        configureSlidingMenu();
        listFragment = (PrivateMessageListFragment) getSupportFragmentManager().findFragmentById(R.id.pm_list_fragment);
        messageFragment = (PrivateMessageFragment) getSupportFragmentManager().findFragmentById(R.id.pm_fragment);
    }

    private void configureSlidingMenu(){
        slidingMenu = new SlidingMenu(this, SlidingMenu.SLIDING_CONTENT);
        slidingMenu.setMenu(R.layout.pm_list_activity);
        slidingMenu.setOnClosedListener(this);
        slidingMenu.setOnOpenedListener(this);
        slidingMenu.setMode(SlidingMenu.LEFT);
        updateSlidingMenuOffset();
        slidingMenu.showMenu();
    }

    private void updateSlidingMenuOffset(){
        DisplayMetrics met = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(met);
        if(met.widthPixels < getResources().getDimension(R.dimen.pm_list_width_cutoff)){
            slidingMenu.setBehindOffsetRes(R.dimen.pm_list_offset);
        }else{
            slidingMenu.setBehindWidthRes(R.dimen.pm_list_width);
        }
    }

    @Override
    public void onBackPressed() {
        if(slidingMenu.isMenuShowing()){
            super.onBackPressed();
        }else{
            slidingMenu.showMenu();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(slidingMenu.isMenuShowing()){
            listFragment.setMenuVisibility(true);
            messageFragment.setMenuVisibility(false);
        }else{
            listFragment.setMenuVisibility(false);
            messageFragment.setMenuVisibility(true);
        }
        slidingMenu.setSlidingEnabled(messageFragment.hasPMLoaded());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                slidingMenu.showMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateSlidingMenuOffset();
    }

    @Override
    public void onClosed() {
        if(messageFragment != null){
            messageFragment.onPaneRevealed();
            messageFragment.setMenuVisibility(true);
        }
        if(listFragment != null){
            listFragment.setMenuVisibility(false);
            listFragment.onPaneObscured();
        }
    }

    @Override
    public void onOpened() {
        if(listFragment != null){
            listFragment.setMenuVisibility(true);
            listFragment.onPaneRevealed();
        }
        if(messageFragment != null){
            messageFragment.onPaneObscured();
            messageFragment.setMenuVisibility(false);
        }
    }

    public void showPM(int id, String title) {
        messageFragment.showPM(id, title);
        slidingMenu.setSlidingEnabled(true);
        slidingMenu.showContent();
    }
}
