package com.salvadordalvik.something;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SlidingPaneLayout;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.salvadordalvik.fastlibrary.widget.ToggleSlidingPaneLayout;
import com.salvadordalvik.something.util.SomePreferences;

public class MainActivity extends FragmentActivity implements SlidingPaneLayout.PanelSlideListener {
    private ToggleSlidingPaneLayout slidingLayout;

    private ThreadListFragment threadList;
    private ThreadViewFragment threadView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_main);
        setProgressBarVisibility(false);
        threadView = (ThreadViewFragment) getSupportFragmentManager().findFragmentById(R.id.threadview_fragment);
        Fragment threads = getSupportFragmentManager().findFragmentByTag("thread_list");
        if(threads instanceof ThreadListFragment){
            threadList = (ThreadListFragment) threads;
        }else{
            threadList = new ThreadListFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.list_container, threadList, "thread_list").commit();
        }
        configureActionbar();
        configureSlidingLayout();

        if(!SomePreferences.loggedIn){
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }
    }

    private void configureActionbar(){
        ActionBar bar = getActionBar();
        bar.setHomeButtonEnabled(true);
    }

    private void configureSlidingLayout(){
        slidingLayout = (ToggleSlidingPaneLayout) findViewById(R.id.sliding_layout);
        slidingLayout.setSliderFadeColor(Color.argb(0,0,0,0));
        slidingLayout.setShadowResource(R.drawable.right_divider);
        slidingLayout.setPanelSlideListener(this);
        slidingLayout.openPane();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(slidingLayout.isOpen()){
            threadList.setMenuVisibility(true);
            threadView.setMenuVisibility(false);
        }else{
            threadList.setMenuVisibility(false);
            threadView.setMenuVisibility(true);
        }
        slidingLayout.setTouchSlidable(threadView.hasThreadLoaded());
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                slidingLayout.openPane();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(!slidingLayout.isOpen()){
            slidingLayout.openPane();
        }else if(getFragmentManager().getBackStackEntryCount() > 0){
            getFragmentManager().popBackStack();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public void onPanelSlide(View view, float v) {

    }

    @Override
    public void onPanelOpened(View view) {
        Spanned title = threadList.getTitle();
        if(title != null && title.length() > 0){
            setTitle(title);
        }
        threadView.onPaneObscured();
        threadView.setMenuVisibility(false);
        threadList.setMenuVisibility(true);
    }

    @Override
    public void onPanelClosed(View view) {
        Spanned title = threadView.getTitle();
        if(title != null && title.length() > 0){
            setTitle(title);
        }
        threadView.onPaneRevealed();
        threadView.setMenuVisibility(true);
        threadList.setMenuVisibility(false);
    }

    public void showThread(int id) {
        showThread(id, 0);
    }

    public void showThread(int id, int page) {
        slidingLayout.setTouchSlidable(true);
        slidingLayout.closePane();
        threadView.loadThread(id, page);
    }

    public void showForum(int id) {
        slidingLayout.openPane();
        if(getFragmentManager().getBackStackEntryCount() > 0){
            getFragmentManager().popBackStackImmediate();
        }
        threadList.showForum(id);
    }

    public void showForumList(){
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.replace(R.id.list_container, new ForumListFragment(), "forum_list");
        trans.addToBackStack("open_forum_list");
        trans.commit();
    }

    public void onThreadPageLoaded(int threadId, int unreadDiff) {
        slidingLayout.setTouchSlidable(true);
        threadList.onThreadPageLoaded(threadId, unreadDiff);
    }
}
