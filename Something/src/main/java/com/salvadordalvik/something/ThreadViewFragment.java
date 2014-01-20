package com.salvadordalvik.something;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.android.volley.Response;
import com.salvadordalvik.fastlibrary.FastFragment;
import com.salvadordalvik.fastlibrary.list.FastAdapter;
import com.salvadordalvik.something.request.ThreadPageRequest;

/**
 * Created by matthewshepard on 1/19/14.
 */
public class ThreadViewFragment extends FastFragment {
    private ListView listView;
    private FastAdapter adapter;

    private int threadId, page, maxPage, forumId;
    private String threadTitle;

    public ThreadViewFragment() {
        super(R.layout.ptr_generic_listview);
    }

    @Override
    public void viewCreated(View frag, Bundle savedInstanceState) {
        adapter = new FastAdapter(getActivity(), this, 1);
        listView = (ListView) frag.findViewById(R.id.ptr_listview);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(adapter);
    }

    @Override
    public void refreshData(boolean pullToRefresh, boolean staleRefresh) {
        queueRequest(new ThreadPageRequest(threadId, page, new Response.Listener<ThreadPageRequest.ThreadPage>() {
            @Override
            public void onResponse(ThreadPageRequest.ThreadPage response) {
                page = response.page;
                maxPage = response.maxPage;
                forumId = response.forumId;
                threadTitle = response.threadTitle;
                adapter.clearList();
                adapter.addItems(response.posts);
                getActivity().setTitle(threadTitle);
            }
        }, null));
    }

    public void loadThread(int threadId, int page){
        this.threadId = threadId;
        this.page = page;
        startRefresh();
    }
}
