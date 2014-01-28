package com.salvadordalvik.something;

import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;

import com.android.volley.Response;
import com.salvadordalvik.fastlibrary.FastFragment;
import com.salvadordalvik.fastlibrary.list.FastAdapter;
import com.salvadordalvik.something.request.ThreadPageRequest;

import org.w3c.dom.Text;

/**
 * Created by matthewshepard on 1/19/14.
 */
public class ThreadViewFragment extends FastFragment {
    private ListView listView;
    private FastAdapter adapter;

    private int threadId, page, maxPage, forumId;
    private Spanned threadTitle;

    public ThreadViewFragment() {
        super(R.layout.ptr_generic_listview);
    }

    @Override
    public void viewCreated(View frag, Bundle savedInstanceState) {
        adapter = new FastAdapter(this, 1);
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
                if(!TextUtils.isEmpty(response.threadTitle)){
                    threadTitle = Html.fromHtml(response.threadTitle);
                }
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

    public Spanned getTitle() {
        return threadTitle;
    }
}
