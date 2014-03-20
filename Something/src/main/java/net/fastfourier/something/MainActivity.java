package net.fastfourier.something;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SlidingPaneLayout;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import com.bugsense.trace.BugSenseHandler;

import net.fastfourier.something.util.SomePreferences;
import net.fastfourier.something.util.SomeTheme;
import net.fastfourier.something.widget.LockableSlidingPaneLayout;
import net.fastfourier.something.widget.MarginDrawerLayout;

public class MainActivity extends SomeActivity implements SlidingPaneLayout.PanelSlideListener {
    private LockableSlidingPaneLayout drawerLayout;

    private ThreadListFragment threadList;
    private ThreadViewFragment threadView;
    private ForumListFragment forumList;

    private int[] startColor = new int[3], endColor = new int[3];
    private float[] currentColor = new float[3];
    private boolean sliderSettled = true, interpActionbarColor = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(this, "cd75dfa8");
        setContentView(R.layout.activity_main);
        drawerLayout = (LockableSlidingPaneLayout) findViewById(R.id.main_drawer);
        drawerLayout.setPanelSlideListener(this);
        drawerLayout.setFocusableInTouchMode(false);
        drawerLayout.setSliderFadeColor(Color.argb(96,0,0,0));
        if(savedInstanceState != null && !savedInstanceState.getBoolean("menu_open")){
            closeMenu();
        }else{
            showMenu();
        }
        setProgressBarVisibility(false);
        threadView = (ThreadViewFragment) getSupportFragmentManager().findFragmentById(R.id.threadview_fragment);
        Fragment threads = getSupportFragmentManager().findFragmentByTag("thread_list");
        if(threads instanceof ThreadListFragment){
            threadList = (ThreadListFragment) threads;
        }else{
            threadList = new ThreadListFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.sliding_container, threadList, "thread_list").commit();
        }
        if(!SomePreferences.loggedIn){
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.hasExtra("thread_id")){
            showThread(intent.getIntExtra("thread_id", 0), intent.getIntExtra("thread_page", 1), intent.getBooleanExtra("from_url", false));
        }else if(intent.hasExtra("post_id")){
            showPost(intent.getLongExtra("post_id", 0), intent.getBooleanExtra("from_url", false));
        }else if(intent.hasExtra("forum_id")){
            showForum(intent.getIntExtra("forum_id", intent.getIntExtra("forum_page", 1)));
        }else if(intent.getBooleanExtra("show_index", false)){
            showForumList();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        BugSenseHandler.startSession(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isMenuShowing()){
            threadList.setMenuVisibility(true);
            threadView.setMenuVisibility(false);
            if(forumList != null){
                setTitle(R.string.forum_title);
            }else{
                Spanned title = threadList.getTitle();
                if(title != null && title.length() > 0){
                    setTitle(title);
                }
            }
        }else{
            threadList.setMenuVisibility(false);
            threadView.setMenuVisibility(true);
            CharSequence title = threadView.getTitle();
            if(title != null && title.length() > 0){
                setTitle(title);
            }
        }
        lockDrawer(!threadView.isThreadLoaded());
        setDisplayUp(!isMenuShowing() || forumList != null);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        BugSenseHandler.closeSession(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("menu_open", isMenuShowing());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                if(isMenuShowing()){
                    if(forumList != null){
                        hideForumsList();
                        return true;
                    }
                }else{
                    if(!threadView.overrideBackPressed()){
                        showMenu();
                    }
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(!isMenuShowing()){
            if(!threadView.overrideBackPressed()){
                showMenu();
            }
        }else if(getSupportFragmentManager().getBackStackEntryCount() > 0){
            hideForumsList();
        }else{
            super.onBackPressed();
        }
    }

    public void showThread(int id, boolean fromUrl) {
        showThread(id, 0, fromUrl);
    }

    public void showThread(int id, int page, boolean fromUrl) {
        lockDrawer(false);
        closeMenu();
        threadView.loadThread(id, page, fromUrl);
        threadList.highlightThread(id);
    }

    public void showPost(long id, boolean fromUrl) {
        lockDrawer(false);
        closeMenu();
        threadView.loadPost(id, fromUrl);
        threadList.highlightThread(0);
    }

    public void showForum(int id) {
        showMenu();
        FragmentManager fragMan = getSupportFragmentManager();
        if(fragMan.getBackStackEntryCount() > 0){
            hideForumsList();
        }
        threadList.showForum(id);
    }

    public void hideForumsList(){
        getSupportFragmentManager().popBackStackImmediate();
        forumList = null;
        setDisplayUp(false);
        setTitle(threadList.getTitle());
    }

    public void showForumList(){
        showForumList(getCurrentForumId());
    }

    private int getCurrentForumId() {
        return threadList != null ? threadList.getForumId() : 0;
    }

    public void showForumList(int currentForumId){
        showMenu();
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        forumList = ForumListFragment.newInstance(currentForumId);
        trans.replace(R.id.sliding_container, forumList, "forum_list");
        trans.addToBackStack("open_forum_list");
        trans.commit();

        setDisplayUp(true);
    }

    public void onThreadPageLoaded(int threadId) {
        lockDrawer(false);
        threadList.onThreadPageLoaded(threadId);
    }

    public void setTitle(CharSequence title, Fragment requestor){
        if(!TextUtils.isEmpty(title) && isFragmentFocused(requestor)){
            setTitle(title);
        }
    }

    public boolean isFragmentFocused(Fragment fragment){
        if(isMenuShowing()){
            return forumList == fragment || (forumList == null && fragment == threadList);
        }else{
            return fragment == threadView;
        }
    }

    private static int interpColor(int[] start, int[] end, float[] current, float percent){
        current[0] = start[0]+((end[0]-start[0])*percent);
        current[1] = start[1]+((end[1]-start[1])*percent);
        current[2] = start[2]+((end[2]-start[2])*percent);
        return Color.rgb((int) current[0], (int) current[1], (int) current[2]);
    }
      
    private static void colorToRGB(int color, int[] rgb){
        rgb[0] = Color.red(color);
        rgb[1] = Color.green(color);
        rgb[2] = Color.blue(color);
    }

    private void onSlide(float slideOffset){
        if(sliderSettled && threadView != null){
            int start, end;
            start = SomeTheme.getActionbarColorForForum(threadView.getForumId(), getActionbarDefaultColor());
            end = getActionbarDefaultColor();
            colorToRGB(start, startColor);
            colorToRGB(end, endColor);
            interpActionbarColor = start != end;
            sliderSettled = false;
        }
        if(interpActionbarColor){
            setActionbarColor(interpColor(startColor, endColor, currentColor, slideOffset));
        }
    }

    public int getCurrentThreadId() {
        return threadView != null ? threadView.getThreadId() : 0;
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
        onSlide(slideOffset);
    }

    @Override
    public void onPanelOpened(View panel) {
        if(threadList != null){
            Spanned title = threadList.getTitle();
            if(title != null && title.length() > 0){
                setTitle(title);
            }
            threadList.setMenuVisibility(true);
            threadList.onPaneRevealed();
        }
        if(threadView != null){
            threadView.onPaneObscured();
            threadView.setMenuVisibility(false);
        }
        setActionbarColorToDefault();

        interpActionbarColor = false;
        sliderSettled = true;
        setDisplayUp(forumList != null);
    }

    @Override
    public void onPanelClosed(View panel) {
        if(threadView != null){
            CharSequence title = threadView.getTitle();
            if(title != null && title.length() > 0){
                setTitle(title);
            }
            threadView.onPaneRevealed();
            threadView.setMenuVisibility(true);
        }
        if(threadList != null){
            threadList.setMenuVisibility(false);
            threadList.onPaneObscured();
        }

        interpActionbarColor = false;
        sliderSettled = true;

        setDisplayUp(true);
    }

    private void setDisplayUp(boolean display){
        ActionBar ab = getActionBar();
        if(ab != null){
            ab.setDisplayHomeAsUpEnabled(display);
        }
    }
}
