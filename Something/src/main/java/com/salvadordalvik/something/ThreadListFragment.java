package com.salvadordalvik.something;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spanned;
import android.text.SpannedString;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.salvadordalvik.fastlibrary.FastFragment;
import com.salvadordalvik.fastlibrary.alert.FastAlert;
import com.salvadordalvik.fastlibrary.data.FastQueryTask;
import com.salvadordalvik.fastlibrary.list.FastItem;
import com.salvadordalvik.fastlibrary.util.FastUtils;
import com.salvadordalvik.something.data.SomeDatabase;
import com.salvadordalvik.something.list.ForumItem;
import com.salvadordalvik.something.list.MenuItem;
import com.salvadordalvik.something.list.PagedAdapter;
import com.salvadordalvik.something.list.StubItem;
import com.salvadordalvik.something.list.ThreadItem;
import com.salvadordalvik.something.request.BookmarkRequest;
import com.salvadordalvik.something.request.ThreadListRequest;
import com.salvadordalvik.something.util.Constants;
import com.salvadordalvik.something.util.SomePreferences;
import com.salvadordalvik.something.widget.PageSelectDialogFragment;
import com.salvadordalvik.something.widget.PreferencesDialogFragment;

import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.DefaultHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.library.Options;

/**
 * Created by matthewshepard on 1/16/14.
 */
public class ThreadListFragment extends FastFragment implements FastQueryTask.QueryResultCallback<ForumItem>,PagedAdapter.PagedCallbacks, PageSelectDialogFragment.PageSelectable {
    private ListView threadList;
    private PagedAdapter adapter;

    private int forumId = -1;
    private boolean starred = false;
    private Spanned forumTitle;

    public ThreadListFragment() {
        super(R.layout.generic_listview, R.menu.thread_list);
        adapter = new PagedAdapter(this, 3, 6, this);

        adapter.addItems(0, new MenuItem("Forums") {
            @Override
            public boolean onItemClick(Activity act, Fragment fragment) {
                ((MainActivity) act).showForumList(forumId);
                return false;
            }

            @Override
            public void onButtonClick(View view) {
                ((MainActivity)getActivity()).showForum(Constants.BOOKMARK_FORUMID);
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if(savedInstanceState != null && savedInstanceState.containsKey("forum_id")){
            forumId = savedInstanceState.getInt("forum_id", SomePreferences.favoriteForumId);
        }else if(forumId < 1){
            if(args != null && args.containsKey("forum_id")){
                forumId = args.getInt("forum_id", SomePreferences.favoriteForumId);
            }else{
                forumId = SomePreferences.favoriteForumId;
            }
        }
    }

    @Override
    protected Options generatePullToRefreshOptions() {
        return Options.create().scrollDistance(getScrollDistance()).build();
    }

    private float getScrollDistance(){
        Log.e("thread", "scroll distance: "+FastUtils.calculateScrollDistance(getActivity(), 2.5f));
        return Math.max(Math.min(FastUtils.calculateScrollDistance(getActivity(), 2.5f), 0.666f), 0.333f);
    }

    @Override
    public void viewCreated(View frag, Bundle savedInstanceState) {
        threadList = (ListView) frag.findViewById(R.id.listview);
        threadList.setAdapter(adapter);
        threadList.setOnItemClickListener(adapter);
        threadList.setOnScrollListener(adapter);

        registerForContextMenu(threadList);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!startRefreshIfStale()){
            updateStarredForums();
            updateForumTitle();
        }
    }

    public void onPaneObscured() {
    }

    public void onPaneRevealed() {
        if(isResumed()){
            startRefreshIfStale();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("forum_id", forumId);
    }

    @Override
    public void refreshData(boolean pullToRefresh, boolean staleRefresh) {
        loadPage(1, true);
        updateStarredForums();
        updateForumTitle();
        if(pullToRefresh){
            adapter.clearPagesAfter(1);
        }
    }

    private void loadPage(int page, boolean scrollTo){
        setRefreshAnimation(true);
        queueRequest(new ThreadListRequest(forumId, page, scrollTo, forumResponse, errorResponse));
    }

    private Response.Listener<ThreadListRequest.ThreadListResponse> forumResponse = new Response.Listener<ThreadListRequest.ThreadListResponse>() {
        @Override
        public void onResponse(ThreadListRequest.ThreadListResponse response) {
            adapter.setPageContent(response.page, response.threads);
            adapter.setMaxPage(response.maxPage);
            if(response.scrollTo){
                if(response.page == 1){
                    scrollToThreads();
                }else{
                    scrollToThreads(response.page);
                }
            }
            updateForumTitle();
        }
    };

    private Response.ErrorListener errorResponse =  new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            FastAlert.error(getActivity(), getView(), getSafeString(R.string.loading_failed));
            adapter.loadingPageFailed();
        }
    };

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        FastItem item = adapter.getItem(info.position);
        if(item instanceof ThreadItem){
            final ThreadItem thread = (ThreadItem) item;
            menu.add(thread.isBookmarked() ? R.string.menu_thread_unbookmark : R.string.menu_thread_bookmark).setOnMenuItemClickListener(new android.view.MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(android.view.MenuItem item) {
                    //TODO better integrate this request into fragment, display true indeterminate message.
                    queueRequest(new BookmarkRequest(thread.getId(), !thread.isBookmarked(), null, null));
                    thread.setBookmarked(!thread.isBookmarked());
                    adapter.notifyDataSetChanged();
                    FastAlert.process(getActivity(), getView(), getSafeString(R.string.bookmarking_thread_started));
                    return true;
                }
            });
        }
    }

    private void updateStarredForums(){
        new FastQueryTask<ForumItem>(SomeDatabase.getDatabase(), this).query(SomeDatabase.VIEW_STARRED_FORUMS, null);
    }

    private void updateForumTitle() {
        if(forumId == Constants.BOOKMARK_FORUMID){
            forumTitle = new SpannedString(getSafeString(R.string.bookmark_title));
            setTitle(forumTitle);
        }else{
            new FastQueryTask<ForumItem>(SomeDatabase.getDatabase(), new FastQueryTask.QueryResultCallback<ForumItem>() {
                @Override
                public int[] findColumns(Cursor data) {
                    return FastQueryTask.findColumnIndicies(data, ForumItem.DB_COLUMNS);
                }

                @Override
                public void queryResult(List<ForumItem> results) {
                    Activity act = getActivity();
                    if(results.size() > 0 && act != null){
                        ForumItem forum = results.get(0);
                        forumTitle = forum.getTitle();
                        starred = forum.isStarred();
                        setTitle(forumTitle);
                        invalidateOptionsMenu();
                    }
                }

                @Override
                public ForumItem createItem(Cursor data, int[] columns) {
                    return new ForumItem(data, false, columns, forumId);
                }
            }).query(SomeDatabase.VIEW_FORUMS, null, "forum_id=?", Integer.toString(forumId));
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        android.view.MenuItem pin = menu.findItem(R.id.menu_forum_pin);
        pin.setChecked(forumId == SomePreferences.favoriteForumId);
        pin.setTitle(forumId == Constants.BOOKMARK_FORUMID ? R.string.menu_forum_pin_bookmarks : R.string.menu_forum_pin);

        android.view.MenuItem star = menu.findItem(R.id.menu_forum_star);
        star.setVisible(forumId != Constants.BOOKMARK_FORUMID);
        star.setChecked(starred);

        android.view.MenuItem home = menu.findItem(R.id.menu_forum_home);
        home.setVisible(forumId == SomePreferences.favoriteForumId && forumId != Constants.BOOKMARK_FORUMID);
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_forum_pin:
                SomePreferences.setInt(SomePreferences.THREADLIST_FAVORITE_FORUMID, forumId);
                invalidateOptionsMenu();
                return true;
            case R.id.menu_forum_star:
                starred = ForumItem.toggleStar(forumId);
                invalidateOptionsMenu();
                updateStarredForums();
                return true;
            case R.id.menu_forum_home:
                if(SomePreferences.favoriteForumId == forumId){
                    showForum(Constants.BOOKMARK_FORUMID);
                }else{
                    showForum(SomePreferences.favoriteForumId);
                }
                return true;
            case R.id.menu_preferences:
                new PreferencesDialogFragment().show(getFragmentManager(), "preferences");
                return true;
            case R.id.menu_logout:
                SomePreferences.setString(SomePreferences.LOGIN_COOKIE_STRING, null);
                startActivity(new Intent(getActivity(), LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                getActivity().finish();
                return true;
            case R.id.menu_private_messages:
                startActivity(new Intent(getActivity(), PrivateMessageListActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void scrollToThreads(){
        int threadOffset = adapter.getSectionOffset(2);
        if(threadOffset < adapter.getCount()){
            threadList.setSelection(threadOffset);
        }
    }

    private void scrollToThreads(int page){
        int threadOffset = adapter.getPageOffset(page);
        if(threadOffset < adapter.getCount()){
            threadList.setSelection(threadOffset);
        }
    }

    public void showForum(int id) {
        threadList.setSelection(1);
        adapter.clearPages();
        forumId = id;
        startRefresh();
        invalidateOptionsMenu();
    }

    @Override
    public int[] findColumns(Cursor data) {
        return FastQueryTask.findColumnIndicies(data, ForumItem.DB_COLUMNS);
    }

    @Override
    public void queryResult(List<ForumItem> results) {
        adapter.clearSection(1);
        adapter.addItems(1, results);
    }

    @Override
    public ForumItem createItem(Cursor data, int[] columnIndex) {
        return new ForumItem(data, false, columnIndex, forumId);
    }

    public Spanned getTitle(){
        return forumTitle;
    }

    public void onThreadPageLoaded(int threadId) {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void showPageSelectDialog(int page, int maxPage) {
        if(maxPage > 1){
            PageSelectDialogFragment.newInstance(page, maxPage, this).show(getFragmentManager(), "page_select");
        }

    }

    @Override
    public void refreshPage(int page) {
        loadPage(page, false);
    }

    @Override
    public void scrollToTop() {
        scrollToThreads();
    }

    @Override
    public void goToPage(int page) {
        loadPage(page, true);
    }

    @Override
    protected void setTitle(CharSequence title) {
        Activity act = getActivity();
        if(act instanceof MainActivity){
            ((MainActivity)act).setTitle(title, this);
        }
    }
}
