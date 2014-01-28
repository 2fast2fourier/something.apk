package com.salvadordalvik.something;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.salvadordalvik.fastlibrary.FastFragment;
import com.salvadordalvik.fastlibrary.data.FastQueryTask;
import com.salvadordalvik.fastlibrary.list.FastAdapter;
import com.salvadordalvik.something.list.ForumItem;
import com.salvadordalvik.something.data.SomeDatabase;

import java.util.List;

/**
 * Created by matthewshepard on 1/22/14.
 */
public class ForumListFragment extends FastFragment implements FastQueryTask.QueryResultCallback<ForumItem> {
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
        refreshData(false, false);
    }

    @Override
    public void refreshData(boolean pullToRefresh, boolean staleRefresh) {
        new FastQueryTask<ForumItem>(SomeDatabase.getDatabase(), this).query(SomeDatabase.VIEW_FORUMS);
    }

    @Override
    public void queryResult(List<ForumItem> results) {
        adapter.clearList();
        adapter.addItems(results);
    }

    @Override
    public ForumItem createItem(Cursor data) {
        return new ForumItem(data, true);
    }
}
