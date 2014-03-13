package net.fastfourier.something;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spanned;
import android.text.SpannedString;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.salvadordalvik.fastlibrary.alert.FastAlert;
import com.salvadordalvik.fastlibrary.data.FastQueryTask;
import com.salvadordalvik.fastlibrary.list.FastItem;
import com.salvadordalvik.fastlibrary.util.FastUtils;
import net.fastfourier.something.data.SomeDatabase;
import net.fastfourier.something.list.ForumHeaderItem;
import net.fastfourier.something.list.ForumItem;
import net.fastfourier.something.list.PagedAdapter;
import net.fastfourier.something.list.ThreadItem;
import net.fastfourier.something.request.BookmarkRequest;
import net.fastfourier.something.request.MarkUnreadRequest;
import net.fastfourier.something.request.ThreadListRequest;
import net.fastfourier.something.util.Constants;
import net.fastfourier.something.util.SomePreferences;
import net.fastfourier.something.widget.PageSelectDialogFragment;
import net.fastfourier.something.widget.PreferencesDialogFragment;

import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.Options;

/**
 * Created by matthewshepard on 1/16/14.
 */
public class ThreadListFragment extends SomeFragment implements FastQueryTask.QueryResultCallback<ForumItem>,PagedAdapter.PagedCallbacks, PageSelectDialogFragment.PageSelectable, AdapterView.OnItemLongClickListener, View.OnClickListener {
    private ListView threadList;
    private PagedAdapter adapter;

    private int forumId = -1;
    private boolean starred = false;
    private Spanned forumTitle;

    private int unreadPMCount = 0;

    private ForumHeaderItem header;

    public ThreadListFragment() {
        super(R.layout.generic_listview, R.menu.thread_list);
        adapter = new PagedAdapter(this, 3, 6, this);

        header = new ForumHeaderItem("Forums") {
            @Override
            public boolean onItemClick(Activity act, Fragment fragment) {
                ((MainActivity) act).showForumList(forumId);
                return false;
            }

            @Override
            public void onButtonClick(View view) {
                ((MainActivity)getActivity()).showForum(Constants.BOOKMARK_FORUMID);
            }
        };

        adapter.addItems(0, header);
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
        header.setSelected(forumId == Constants.BOOKMARK_FORUMID);
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
        threadList.setOnItemLongClickListener(this);
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
            Activity activity = getActivity();
            if(activity instanceof MainActivity){
                highlightThread(((MainActivity)activity).getCurrentThreadId());
            }

            //this will only be non-zero in bookmarks
            unreadPMCount = response.unreadPMCount;
            invalidateOptionsMenu();
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
    public void onRefreshCompleted() {
        super.onRefreshCompleted();
    }

    private void showThreadDialog(final int threadId, final String threadTitle, final boolean bookmarked, final ThreadItem item){
        final String threadUrl = "http://forums.somethingawful.com/showthread.php?threadid=" + threadId;
        new AlertDialog.Builder(getActivity())
                .setTitle(threadTitle)
                .setItems(bookmarked ? R.array.thread_context_actions_bookmarked : R.array.thread_context_actions_normal, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            //See R.array.thread_context_actions_normal for item list
                            case 0://First Page
                                ((MainActivity) getActivity()).showThread(threadId, 1);
                                break;
                            case 1://Last Page
                                ((MainActivity) getActivity()).showThread(threadId, -1);
                                break;
                            case 2://Bookmark/Unbookmark
                                queueRequest(new BookmarkRequest(threadId, !bookmarked, null, null));
                                item.setBookmarked(!bookmarked);
                                adapter.notifyDataSetChanged();
                                FastAlert.notice(ThreadListFragment.this, bookmarked ? R.string.bookmarking_thread_started_removing : R.string.bookmarking_thread_started, R.drawable.ic_menu_bookmark);
                                break;
                            case 3://Mark Unread
                                queueRequest(new MarkUnreadRequest(threadId, null, null));
                                FastAlert.notice(ThreadListFragment.this, R.string.marked_unread);
                                item.markUnread();
                                adapter.notifyDataSetChanged();
                                break;
                            case 4://Share link
                                FastUtils.showSimpleShareChooser(getActivity(), threadTitle, threadUrl, getSafeString(R.string.share_url_title));
                                break;
                            case 5://Copy Link
                                FastUtils.copyToClipboard(getActivity(), threadTitle, threadUrl);
                                FastAlert.notice(ThreadListFragment.this, R.string.link_copied, R.drawable.ic_menu_link);
                                break;
                        }
                    }
                })
                .show();
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

        android.view.MenuItem pm = menu.findItem(R.id.menu_private_messages);
        View pmView = pm.getActionView();
        View notification = pmView.findViewById(R.id.pm_count);
        if(unreadPMCount > 0){
            notification.setVisibility(View.VISIBLE);
            ((TextView)notification).setText(Integer.toString(unreadPMCount));
        }else{
            notification.setVisibility(View.GONE);
        }
        pmView.setOnClickListener(this);
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
                showPMs();
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
        header.setSelected(id == Constants.BOOKMARK_FORUMID);
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

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        FastItem item = adapter.getItem(position);
        if(item instanceof ThreadItem){
            ThreadItem thread = (ThreadItem) item;
            showThreadDialog(thread.getId(), thread.getTitle(), thread.isBookmarked(), thread);
            return true;
        }
        return false;
    }

    public void highlightThread(int id) {
        for(FastItem item : adapter.getAllItems()){
            if(item instanceof ThreadItem){
                ((ThreadItem) item).setSelected(item.getId() == id);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.pm_notification_button:
                showPMs();
                break;
        }
    }

    private void showPMs() {
        startActivity(new Intent(getActivity(), PrivateMessageListActivity.class));
    }
}
