package com.salvadordalvik.something;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.salvadordalvik.fastlibrary.FastFragment;
import com.salvadordalvik.fastlibrary.data.FastQueryTask;
import com.salvadordalvik.fastlibrary.list.FastAdapter;
import com.salvadordalvik.something.list.ForumItem;
import com.salvadordalvik.something.data.SomeDatabase;
import com.salvadordalvik.something.request.ForumListRequest;
import com.salvadordalvik.something.util.Constants;
import com.salvadordalvik.something.util.SomePreferences;

import java.util.List;

/**
 * Created by matthewshepard on 1/22/14.
 */
public class ForumListFragment extends FastFragment implements FastQueryTask.QueryResultCallback<ForumItem>,Response.ErrorListener, Response.Listener<Void> {
    private ListView forumList;
    private FastAdapter adapter;

    private int currentlySelectedForumId = 0;

    public static ForumListFragment newInstance(int currentForumId){
        ForumListFragment frag = new ForumListFragment();
        Bundle args = new Bundle();
        args.putInt("forum_id_hint", currentForumId);
        frag.setArguments(args);
        return frag;
    }

    public ForumListFragment() {
        super(R.layout.generic_listview);
    }

    @Override
    public void viewCreated(View frag, Bundle savedInstanceState) {
        currentlySelectedForumId = getArguments().getInt("forum_id_hint", 0);

        adapter = new FastAdapter(this, 3);
        forumList = (ListView) frag.findViewById(R.id.listview);
        forumList.setAdapter(adapter);
        forumList.setOnItemClickListener(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(SomePreferences.lastForumUpdate < System.currentTimeMillis() - 86400000){
            startRefresh();
        }
        refreshForumList();
        setTitle(getString(R.string.forum_title));
    }

    @Override
    public void refreshData(boolean pullToRefresh, boolean staleRefresh) {
        queueRequest(new ForumListRequest(this, this));
        refreshForumList();
    }

    private void refreshForumList(){
        new FastQueryTask<ForumItem>(SomeDatabase.getDatabase(), this).query(SomeDatabase.VIEW_FORUMS);
    }

    @Override
    public int[] findColumns(Cursor data) {
        return FastQueryTask.findColumnIndicies(data, ForumItem.DB_COLUMNS);
    }

    @Override
    public void queryResult(List<ForumItem> results) {
        adapter.clearList();
        adapter.addItems(new ForumItem(Constants.BOOKMARK_FORUMID, getString(R.string.bookmark_title), 0, false, false, false, currentlySelectedForumId));
        adapter.addItems(results);
    }

    @Override
    public ForumItem createItem(Cursor data, int[] columns) {
        return new ForumItem(data, true, columns, currentlySelectedForumId);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        refreshForumList();
    }

    @Override
    public void onResponse(Void response) {
        refreshForumList();
        SomePreferences.setLong(SomePreferences.LAST_FORUM_UPDATE_LONG, System.currentTimeMillis());
    }

    @Override
    protected void setTitle(CharSequence title) {
        Activity act = getActivity();
        if(act instanceof MainActivity){
            ((MainActivity)act).setTitle(title, this);
        }
    }
}
