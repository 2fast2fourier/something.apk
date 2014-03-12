package net.fastfourier.something;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.bugsense.trace.BugSenseHandler;
import com.jeremyfeinstein.slidingmenu.lib.CustomViewAbove;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import net.fastfourier.something.util.SomePreferences;
import net.fastfourier.something.util.SomeTheme;

public class MainActivity extends SomeActivity implements CustomViewAbove.OnPageChangeListener, SlidingMenu.OnCloseListener, SlidingMenu.OnOpenListener {
    private SlidingMenu slidingMenu;

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
        configureSlidingMenu();
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

    private void configureSlidingMenu(){
        slidingMenu = new SlidingMenu(this, SlidingMenu.SLIDING_CONTENT);
        slidingMenu.setMenu(R.layout.menu_container);
        slidingMenu.setOnPageChangeListener(this);
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
        if(met.widthPixels < getResources().getDimension(R.dimen.nav_list_width_cutoff)){
            slidingMenu.setBehindOffsetRes(R.dimen.nav_list_offset);
        }else{
            slidingMenu.setBehindWidthRes(R.dimen.nav_list_width);
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
        if(slidingMenu.isMenuShowing()){
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
        slidingMenu.setSlidingEnabled(threadView.isThreadLoaded());
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                if(slidingMenu.isMenuShowing()){
                    if(getSupportFragmentManager().getBackStackEntryCount() > 0){
                        getSupportFragmentManager().popBackStack();
                        return true;
                    }
                }else{
                    slidingMenu.showMenu();
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(!slidingMenu.isMenuShowing()){
            slidingMenu.showMenu();
        }else if(getSupportFragmentManager().getBackStackEntryCount() > 0){
            getSupportFragmentManager().popBackStack();
            forumList = null;
        }else{
            super.onBackPressed();
        }
    }

    public void showThread(int id) {
        showThread(id, 0);
    }

    public void showThread(int id, int page) {
        slidingMenu.setSlidingEnabled(true);
        slidingMenu.showContent();
        threadView.loadThread(id, page);
        threadList.highlightThread(id);
    }

    public void showForum(int id) {
        slidingMenu.showMenu();
        FragmentManager fragMan = getSupportFragmentManager();
        if(fragMan.getBackStackEntryCount() > 0){
            fragMan.popBackStackImmediate();
            forumList = null;
        }
        threadList.showForum(id);
    }

    public void showForumList(int currentForumId){
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        forumList = ForumListFragment.newInstance(currentForumId);
        trans.replace(R.id.sliding_container, forumList, "forum_list");
        trans.addToBackStack("open_forum_list");
        trans.commit();
    }

    public void onThreadPageLoaded(int threadId) {
        slidingMenu.setSlidingEnabled(true);
        threadList.onThreadPageLoaded(threadId);
    }

    public void setTitle(CharSequence title, Fragment requestor){
        if(!TextUtils.isEmpty(title) && isFragmentFocused(requestor)){
            setTitle(title);
        }
    }

    public boolean isFragmentFocused(Fragment fragment){
        if(slidingMenu != null && slidingMenu.isMenuShowing()){
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

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        onSlide(Math.abs(positionOffset));
    }

    @Override
    public void onPageSelected(int position) {

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

    @Override
    public void onClose() {
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
    }

    @Override
    public void onOpen() {
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
    }

    public int getCurrentThreadId() {
        return threadView != null ? threadView.getThreadId() : 0;
    }
}
