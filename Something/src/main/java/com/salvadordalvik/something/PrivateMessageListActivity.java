package com.salvadordalvik.something;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

/**
 * Created by matthewshepard on 2/7/14.
 */
public class PrivateMessageListActivity extends SomeActivity implements DrawerLayout.DrawerListener {
    private DrawerLayout slidingMenu;
    private PrivateMessageListFragment listFragment;
    private PrivateMessageFragment messageFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.private_message_activity);
        slidingMenu = (DrawerLayout) findViewById(R.id.main_drawer);
        configureSlidingMenu();
        listFragment = (PrivateMessageListFragment) getSupportFragmentManager().findFragmentById(R.id.pm_list_fragment);
        messageFragment = (PrivateMessageFragment) getSupportFragmentManager().findFragmentById(R.id.pm_fragment);
    }

    private void configureSlidingMenu(){
        slidingMenu.setFocusableInTouchMode(false);
        slidingMenu.setDrawerListener(this);
        slidingMenu.openDrawer(Gravity.LEFT);
    }

    private boolean isMenuShowing(){
        return slidingMenu.isDrawerOpen(Gravity.LEFT);
    }

    @Override
    public void onBackPressed() {
        if(isMenuShowing()){
            super.onBackPressed();
        }else{
            slidingMenu.openDrawer(Gravity.LEFT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isMenuShowing()){
            listFragment.setMenuVisibility(true);
            messageFragment.setMenuVisibility(false);
        }else{
            listFragment.setMenuVisibility(false);
            messageFragment.setMenuVisibility(true);
        }
        slidingMenu.setDrawerLockMode(messageFragment.hasPMLoaded() ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_OPEN, Gravity.LEFT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                slidingMenu.openDrawer(Gravity.LEFT);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showPM(int id, String title) {
        messageFragment.showPM(id, title);
        slidingMenu.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.LEFT);
        slidingMenu.closeDrawer(Gravity.LEFT);
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(View drawerView) {
        if(listFragment != null){
            listFragment.setMenuVisibility(true);
            listFragment.onPaneRevealed();
        }
        if(messageFragment != null){
            messageFragment.onPaneObscured();
            messageFragment.setMenuVisibility(false);
        }
    }

    @Override
    public void onDrawerClosed(View drawerView) {
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
    public void onDrawerStateChanged(int newState) {

    }

    public void showPMFolder(int folder) {
        slidingMenu.openDrawer(Gravity.LEFT);
        listFragment.showFolder(folder);
    }
}
