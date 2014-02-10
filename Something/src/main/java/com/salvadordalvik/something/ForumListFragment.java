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
import com.salvadordalvik.something.util.SomePreferences;

import java.util.List;

/**
 * Created by matthewshepard on 1/22/14.
 */
public class ForumListFragment extends FastFragment implements FastQueryTask.QueryResultCallback<ForumItem>,Response.ErrorListener, Response.Listener<Void> {
    private ListView forumList;
    private FastAdapter adapter;

    public ForumListFragment() {
        super(R.layout.ptr_generic_listview);
    }

    @Override
    public void viewCreated(View frag, Bundle savedInstanceState) {
        adapter = new FastAdapter(this, 1);
        forumList = (ListView) frag.findViewById(R.id.ptr_listview);
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
        adapter.addItems(results);
    }

    @Override
    public ForumItem createItem(Cursor data, int[] columns) {
        return new ForumItem(data, true, columns);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        refreshForumList();
    }

    @Override
    public void onResponse(Void response) {
        refreshForumList();
        SomePreferences.setLong(SomePreferences.LAST_FORUM_UPDATE, System.currentTimeMillis());
    }

    @Override
    protected void setTitle(CharSequence title) {
        Activity act = getActivity();
        if(act instanceof MainActivity){
            ((MainActivity)act).setTitle(title, this);
        }
    }
}
