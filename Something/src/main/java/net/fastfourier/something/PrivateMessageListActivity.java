package net.fastfourier.something;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.MenuItem;
import android.view.View;

import net.fastfourier.something.widget.LockableSlidingPaneLayout;

/**
 * Created by matthewshepard on 2/7/14.
 */
public class PrivateMessageListActivity extends SomeActivity implements SlidingPaneLayout.PanelSlideListener {
    private LockableSlidingPaneLayout drawerLayout;
    private PrivateMessageListFragment listFragment;
    private PrivateMessageFragment messageFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.private_message_activity);
        drawerLayout = (LockableSlidingPaneLayout) findViewById(R.id.pm_drawer);
        drawerLayout.setPanelSlideListener(this);
        drawerLayout.setFocusableInTouchMode(false);
        drawerLayout.openPane();

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
        return drawerLayout == null || drawerLayout.isOpen();
    }

    private void lockDrawer(boolean lock){
        drawerLayout.setLocked(lock);
    }

    private void showMenu(){
        drawerLayout.openPane();
    }

    private void closeMenu(){
        drawerLayout.closePane();
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {

    }

    @Override
    public void onPanelOpened(View panel) {
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
    public void onPanelClosed(View panel) {
        if(messageFragment != null){
            messageFragment.onPaneRevealed();
            messageFragment.setMenuVisibility(true);
        }
        if(listFragment != null){
            listFragment.setMenuVisibility(false);
            listFragment.onPaneObscured();
        }
    }
}
