package com.salvadordalvik.something;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.salvadordalvik.fastlibrary.FastFragment;
import com.salvadordalvik.fastlibrary.list.SectionFastAdapter;
import com.salvadordalvik.something.data.PostItem;
import com.salvadordalvik.something.request.ThreadListRequest;

/**
 * Created by matthewshepard on 1/16/14.
 */
public class ThreadListFragment extends FastFragment {
    private ListView threadList;
    private SectionFastAdapter adapter;

    private int forumId = 219;

    public ThreadListFragment() {
        super(R.layout.ptr_generic_listview);
    }

    @Override
    public void viewCreated(View frag, Bundle savedInstanceState) {
        adapter = new SectionFastAdapter(getActivity(), this, 2);
        threadList = (ListView) frag.findViewById(R.id.ptr_listview);
        threadList.setAdapter(adapter);
        threadList.setOnItemClickListener(adapter);

        adapter.addItems(0, new PostItem(9, "Forums", "TEST", null, "FORUMS LIST HERE", "Yup"));
        adapter.addItems(1, new PostItem(8, "Divider", "TEST", null, "DIVIDER HERE", "Yup"));
    }

    @Override
    public void onResume() {
        super.onResume();
        startRefreshIfStale();
    }

    @Override
    public void refreshData(boolean pullToRefresh, boolean staleRefresh) {
        queueRequest(new ThreadListRequest(forumId, new Response.Listener<ThreadListRequest.ThreadListResponse>() {
            @Override
            public void onResponse(ThreadListRequest.ThreadListResponse response) {
                adapter.clearSection(2);
                adapter.addItems(2, response.threads);
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
        int threadOffset = adapter.getSectionOffset(1);
        if(threadOffset < adapter.getCount()){
            threadList.setSelection(threadOffset);
        }
    }

    public void showForum(int id) {
        threadList.setSelection(0);
        forumId = id;
        startRefresh();
    }
}
