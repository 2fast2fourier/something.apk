package com.salvadordalvik.something;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.salvadordalvik.fastlibrary.FastFragment;
import com.salvadordalvik.fastlibrary.list.FastAdapter;
import com.salvadordalvik.something.request.ThreadListRequest;

/**
 * Created by matthewshepard on 1/16/14.
 */
public class ThreadListFragment extends FastFragment {
    private ListView threadList;
    private FastAdapter adapter;

    private int forumId = 219;

    public ThreadListFragment() {
        super(R.layout.ptr_generic_listview);
    }

    @Override
    public void viewCreated(View frag, Bundle savedInstanceState) {
        adapter = new FastAdapter(getActivity(), this, 1);
        threadList = (ListView) frag.findViewById(R.id.ptr_listview);
        threadList.setAdapter(adapter);
        threadList.setOnItemClickListener(adapter);
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
                adapter.clearList();
                adapter.addItems(response.threads);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "Failed to load!", Toast.LENGTH_LONG).show();
            }
        }));
    }

    public void showForum(int id) {
        threadList.setSelection(0);
        forumId = id;
        startRefresh();
    }
}
