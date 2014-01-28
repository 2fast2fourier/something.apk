package com.salvadordalvik.something;

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Spanned;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.salvadordalvik.fastlibrary.FastFragment;
import com.salvadordalvik.fastlibrary.data.FastQueryTask;
import com.salvadordalvik.fastlibrary.list.SectionFastAdapter;
import com.salvadordalvik.something.data.SomeDatabase;
import com.salvadordalvik.something.list.ForumItem;
import com.salvadordalvik.something.list.MenuItem;
import com.salvadordalvik.something.list.StubItem;
import com.salvadordalvik.something.list.ThreadItem;
import com.salvadordalvik.something.request.ThreadListRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matthewshepard on 1/16/14.
 */
public class ThreadListFragment extends FastFragment implements FastQueryTask.QueryResultCallback<ForumItem> {
    private ListView threadList;
    private SectionFastAdapter adapter;

    private ArrayList<ThreadItem> threadData;

    private static final int THREAD_SECTION = 3;

    private int forumId = 219;
    private int page = 1;
    private int maxPage = 1;
    private Spanned forumTitle;

    public ThreadListFragment() {
        super(R.layout.ptr_generic_listview);
        adapter = new SectionFastAdapter(this, 4);

        adapter.addItems(0, new MenuItem("Forums") {
            @Override
            public void onItemClick(Activity act, Fragment fragment) {
                ((MainActivity)act).showForumList();
            }
        });
        adapter.addItems(2, new StubItem(R.layout.divider_item));
    }

    @Override
    public void viewCreated(View frag, Bundle savedInstanceState) {
        threadList = (ListView) frag.findViewById(R.id.ptr_listview);
        threadList.setAdapter(adapter);
        threadList.setOnItemClickListener(adapter);

        if(savedInstanceState != null){
            forumId = savedInstanceState.getInt("forum_id", forumId);
            page = savedInstanceState.getInt("forum_page", 1);
            threadData = savedInstanceState.getParcelableArrayList("thread_list");
            if(threadData != null){
                adapter.addItems(THREAD_SECTION, threadData);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!startRefreshIfStale()){
            updateStarredForums();
            updateForumTitle();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("forum_id", forumId);
        outState.putInt("forum_page", page);
        if(threadData != null){
            outState.putParcelableArrayList("thread_list", threadData);
        }
    }

    @Override
    public void refreshData(boolean pullToRefresh, boolean staleRefresh) {
        queueRequest(new ThreadListRequest(forumId, new Response.Listener<ThreadListRequest.ThreadListResponse>() {
            @Override
            public void onResponse(ThreadListRequest.ThreadListResponse response) {
                adapter.clearSection(THREAD_SECTION);
                adapter.addItems(THREAD_SECTION, response.threads);
                threadData = response.threads;
//                maxPage = response.maxPage;
                scrollToThreads();
                updateForumTitle();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "Failed to load!", Toast.LENGTH_LONG).show();
            }
        }));
        updateStarredForums();
        updateForumTitle();
    }
    

    private void updateStarredForums(){
        new FastQueryTask<ForumItem>(SomeDatabase.getDatabase(), this).query(SomeDatabase.VIEW_STARRED_FORUMS);
    }

    private void updateForumTitle() {
        new FastQueryTask<ForumItem>(SomeDatabase.getDatabase(), new FastQueryTask.QueryResultCallback<ForumItem>() {
            @Override
            public void queryResult(List<ForumItem> results) {
                Activity act = getActivity();
                if(results.size() > 0 && act != null){
                    forumTitle = results.get(0).getTitle();
                    act.setTitle(forumTitle);
                }
            }

            @Override
            public ForumItem createItem(Cursor data) {
                return new ForumItem(data, false);
            }
        }).query(SomeDatabase.VIEW_FORUMS, null, "forum_id=?", Integer.toString(forumId));
    }

    private void scrollToThreads(){
        int threadOffset = adapter.getSectionOffset(2);
        if(threadOffset < adapter.getCount()){
            threadList.setSelection(threadOffset);
        }
    }

    public void showForum(int id) {
        threadList.setSelection(1);
        adapter.clearSection(THREAD_SECTION);
        forumId = id;
        startRefresh();
    }

    @Override
    public void queryResult(List<ForumItem> results) {
        adapter.clearSection(1);
        adapter.addItems(1, results);
    }

    @Override
    public ForumItem createItem(Cursor data) {
        return new ForumItem(data, false);
    }

    public Spanned getTitle(){
        return forumTitle;
    }
}
