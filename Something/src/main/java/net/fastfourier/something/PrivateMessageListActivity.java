package net.fastfourier.something;

import android.app.ActionBar;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

/**
 * Created by matthewshepard on 2/7/14.
 */
public class PrivateMessageListActivity extends SomeActivity implements SlidingMenu.OnCloseListener, SlidingMenu.OnOpenListener {
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

        ActionBar bar = getActionBar();
        if(bar != null){
            bar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void configureSlidingMenu(){
        slidingMenu = new SlidingMenu(this, SlidingMenu.SLIDING_CONTENT);
        slidingMenu.setMenu(R.layout.pm_container);
        slidingMenu.setOnCloseListener(this);
        slidingMenu.setOnOpenListener(this);
        slidingMenu.setMode(SlidingMenu.LEFT);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        slidingMenu.setTouchModeBehind(SlidingMenu.TOUCHMODE_MARGIN);
        slidingMenu.setFadeEnabled(false);
        slidingMenu.setBehindScrollScale(0f);
        updateSlidingMenuSize();
        slidingMenu.showMenu();
    }

    private void updateSlidingMenuSize(){
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
        slidingMenu.setSlidingEnabled(messageFragment.isPMLoaded());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                if(slidingMenu.isMenuShowing()){
                    finish();
                }else{
                    slidingMenu.showMenu();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showPM(int id, String title) {
        messageFragment.showPM(id, title);
        listFragment.highlightPM(id);
        slidingMenu.setSlidingEnabled(true);
        slidingMenu.showContent();
    }

    public void showPMFolder(int folder) {
        slidingMenu.showMenu();
        listFragment.showFolder(folder);
    }

    @Override
    public void onClose() {
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
    public void onOpen() {
        if(listFragment != null){
            listFragment.setMenuVisibility(true);
            listFragment.onPaneRevealed();
        }
        if(messageFragment != null){
            messageFragment.onPaneObscured();
            messageFragment.setMenuVisibility(false);
        }
    }

    public int getSelectedPMId() {
        return messageFragment != null ? messageFragment.getPmId() : 0;
    }
}
