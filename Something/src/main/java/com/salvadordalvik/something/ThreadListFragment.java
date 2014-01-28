package com.salvadordalvik.something;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.salvadordalvik.fastlibrary.FastFragment;
import com.salvadordalvik.fastlibrary.list.SectionFastAdapter;
import com.salvadordalvik.something.list.MenuItem;
import com.salvadordalvik.something.list.StubItem;
import com.salvadordalvik.something.list.ThreadItem;
import com.salvadordalvik.something.request.ThreadListRequest;

import java.util.ArrayList;

/**
 * Created by matthewshepard on 1/16/14.
 */
public class ThreadListFragment extends FastFragment {
    private ListView threadList;
    private SectionFastAdapter adapter;

    private ArrayList<ThreadItem> threadData;

    private static final int THREAD_SECTION = 3;

    private int forumId = 219;
    private int page = 1;
    private int maxPage = 1;

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
        startRefreshIfStale();
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
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "Failed to load!", Toast.LENGTH_LONG).show();
            }
        }));
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
}
