package net.fastfourier.something;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import net.fastfourier.something.widget.MarginDrawerLayout;

/**
 * Created by matthewshepard on 2/7/14.
 */
public class PrivateMessageListActivity extends SomeActivity implements MarginDrawerLayout.DrawerListener {
    private MarginDrawerLayout drawerLayout;
    private PrivateMessageListFragment listFragment;
    private PrivateMessageFragment messageFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.private_message_activity);
        drawerLayout = (MarginDrawerLayout) findViewById(R.id.pm_drawer);
        drawerLayout.setDrawerListener(this);
        drawerLayout.setFocusableInTouchMode(false);
        drawerLayout.openDrawer(Gravity.LEFT);

        listFragment = (PrivateMessageListFragment) getSupportFragmentManager().findFragmentById(R.id.pm_list_fragment);
        messageFragment = (PrivateMessageFragment) getSupportFragmentManager().findFragmentById(R.id.pm_fragment);

        ActionBar bar = getActionBar();
        if(bar != null){
            bar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        if(isMenuShowing()){
            super.onBackPressed();
        }else{
            showMenu();
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
        lockDrawer(!messageFragment.isPMLoaded());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                if(isMenuShowing()){
                    finish();
                }else{
                    showMenu();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showPM(int id, String title) {
        messageFragment.showPM(id, title);
        listFragment.highlightPM(id);
        lockDrawer(false);
        closeMenu();
    }

    public void showPMFolder(int folder) {
        showMenu();
        listFragment.showFolder(folder);
    }

    public int getSelectedPMId() {
        return messageFragment != null ? messageFragment.getPmId() : 0;
    }


    private boolean isMenuShowing(){
        return drawerLayout == null || drawerLayout.isDrawerOpen(Gravity.LEFT);
    }

    private void lockDrawer(boolean lock){
        drawerLayout.setDrawerLockMode(lock ? DrawerLayout.LOCK_MODE_LOCKED_OPEN : DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.LEFT);
    }

    private void showMenu(){
        drawerLayout.openDrawer(Gravity.LEFT);
    }

    private void closeMenu(){
        drawerLayout.closeDrawer(Gravity.LEFT);
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
}
