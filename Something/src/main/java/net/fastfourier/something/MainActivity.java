package net.fastfourier.something;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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

import java.util.List;

public class MainActivity extends SomeActivity implements SlidingPaneLayout.PanelSlideListener, FragmentManager.OnBackStackChangedListener {
    private LockableSlidingPaneLayout drawerLayout;

    private ThreadListFragment threadList;
    private ThreadViewFragment threadView;

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
        if(savedInstanceState != null && savedInstanceState.containsKey("menu_open") && !savedInstanceState.getBoolean("menu_open")){
            closeMenu();
        }else{
            showMenu();
        }
        setProgressBarVisibility(false);

        getSupportFragmentManager().addOnBackStackChangedListener(this);
        threadView = (ThreadViewFragment) getSupportFragmentManager().findFragmentById(R.id.threadview_fragment);
        Fragment threads = getSupportFragmentManager().findFragmentByTag("thread_list");
        if(threads instanceof ThreadListFragment){
            threadList = (ThreadListFragment) threads;
        }else{
            threadList = ThreadListFragment.newInstance(SomePreferences.favoriteForumId);
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
        lockDrawer(!threadView.isThreadLoaded());
        updateActionbar();
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
                    if(canPopMenuStack()){
                        popMenuStack();
                        return true;
                    }
                }else{
                    if(!threadView.overrideBackPressed()){
                        showMenu();
                    }
                    return true;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(!isMenuShowing()){
            if(!threadView.overrideBackPressed()){
                showMenu();
            }
        }else if(canPopMenuStack()){
            popMenuStack();
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
        if(threadList != null && threadList.isAdded()){
            threadList.highlightThread(id);
        }
    }

    public void showPost(long id, boolean fromUrl) {
        lockDrawer(false);
        closeMenu();
        threadView.loadPost(id, fromUrl);
        if(threadList != null && threadList.isAdded()){
            threadList.highlightThread(0);
        }
    }

    public void showForum(int id) {
        FragmentManager fragman = getSupportFragmentManager();
        showMenu();
        if(id == SomePreferences.favoriteForumId){
            if(fragman.getBackStackEntryCount() > 0){
                fragman.popBackStackImmediate(fragman.getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
            threadList = ThreadListFragment.newInstance(id);
            fragman.beginTransaction().replace(R.id.sliding_container, threadList, "thread_list").commit();
        }else{
            threadList = ThreadListFragment.newInstance(id);
            fragman.beginTransaction().replace(R.id.sliding_container, threadList, "thread_list").addToBackStack("open_forum").commit();
        }
    }

    private boolean canPopMenuStack(){
        return getSupportFragmentManager().getBackStackEntryCount() > 0;
    }

    private void popMenuStack(){
        FragmentManager fragman = getSupportFragmentManager();
        fragman.popBackStackImmediate();
    }

    private int getCurrentForumId() {
        return threadList != null ? threadList.getForumId() : 0;
    }

    public void showForumList(){
        showForumList(getCurrentForumId());
    }

    public void showForumList(int currentForumId){
        showMenu();
        if(!getSupportFragmentManager().popBackStackImmediate("forum_index", 0)){
            FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
            trans.replace(R.id.sliding_container, ForumListFragment.newInstance(currentForumId), "forum_list");
            trans.addToBackStack("forum_index");
            trans.commit();
        }
    }

    public void onThreadPageLoaded(int threadId) {
        lockDrawer(false);
        if(threadList != null){
            threadList.onThreadPageLoaded(threadId);
        }
    }

    public void setTitle(CharSequence title, Fragment requestor){
        if(!TextUtils.isEmpty(title) && isFragmentFocused(requestor)){
            setTitle(title);
        }
    }

    public boolean isFragmentFocused(Fragment fragment){
        if(isMenuShowing()){
            return fragment instanceof ForumListFragment || fragment instanceof ThreadListFragment;
        }else{
            return fragment instanceof ThreadViewFragment;
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
        if(threadList != null && threadList.isAdded()){
            threadList.onPaneRevealed();
        }
        if(threadView != null){
            threadView.onPaneObscured();
        }
        setActionbarColorToDefault();

        interpActionbarColor = false;
        sliderSettled = true;

        updateActionbar();
    }

    @Override
    public void onPanelClosed(View panel) {
        if(threadView != null){
            threadView.onPaneRevealed();
        }
        if(threadList != null && threadList.isAdded()){
            threadList.onPaneObscured();
        }

        interpActionbarColor = false;
        sliderSettled = true;

        updateActionbar();
    }

    private void updateActionbar(){
        setDisplayUp(!isMenuShowing() || canPopMenuStack());
        List<Fragment> frags = getSupportFragmentManager().getFragments();
        if(frags != null){
            for(Fragment frag : frags){
                if(frag instanceof SomeFragment && frag.isAdded()){
                    if(isFragmentFocused(frag)){
                        CharSequence title = ((SomeFragment) frag).getTitle();
                        if(title != null && title.length() > 0){
                            setTitle(title);
                        }
                        frag.setMenuVisibility(true);
                    }else{
                        frag.setMenuVisibility(false);
                    }
                }
            }
        }
    }

    private void setDisplayUp(boolean display){
        ActionBar ab = getActionBar();
        if(ab != null){
            ab.setDisplayHomeAsUpEnabled(display);
        }
    }

    @Override
    public void onBackStackChanged() {
        Fragment threads = getSupportFragmentManager().findFragmentByTag("thread_list");
        if(threads instanceof ThreadListFragment){
            threadList = (ThreadListFragment) threads;
        }
        updateActionbar();
    }
}
