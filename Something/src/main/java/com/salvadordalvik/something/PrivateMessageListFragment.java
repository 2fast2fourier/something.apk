package com.salvadordalvik.something;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.salvadordalvik.fastlibrary.FastFragment;
import com.salvadordalvik.fastlibrary.alert.FastAlert;
import com.salvadordalvik.fastlibrary.list.SectionFastAdapter;
import com.salvadordalvik.something.request.PrivateMessageListRequest;

/**
 * Created by matthewshepard on 2/7/14.
 */
public class PrivateMessageListFragment extends SomeFragment implements Response.ErrorListener, Response.Listener<PrivateMessageListRequest.PMListResult> {
    private ListView pmList;
    private SectionFastAdapter adapter;

    private int folderId = 0;

    public PrivateMessageListFragment() {
        super(R.layout.generic_listview);
    }

    @Override
    public void viewCreated(View frag, Bundle savedInstanceState) {
        pmList = (ListView) frag.findViewById(R.id.listview);
        adapter = new SectionFastAdapter(this, 2);
        pmList.setAdapter(adapter);
        pmList.setOnItemClickListener(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        startRefreshIfStale();
    }

    @Override
    public void refreshData(boolean pullToRefresh, boolean staleRefresh) {
        queueRequest(new PrivateMessageListRequest(folderId, false, this, this));
    }

    @Override
    public void onResponse(PrivateMessageListRequest.PMListResult pmListResult) {
        adapter.replaceSection(0, pmListResult.folders);
        adapter.replaceSection(1, pmListResult.messages);
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        FastAlert.error(getActivity(), getView(), getSafeString(R.string.loading_failed));
    }

    public void showFolder(int id) {
        folderId = id;
        startRefresh();
    }

    public void onPaneObscured() {

    }

    public void onPaneRevealed() {

    }
}
