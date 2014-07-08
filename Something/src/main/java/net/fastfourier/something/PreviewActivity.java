package net.fastfourier.something;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.MenuItem;

/**
 * Created by Timothy on 2014/06/28.
 */
public class PreviewActivity extends SomeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reply_preview_activity);
        ActionBar ab = getActionBar();
        if(ab != null){
            ab.setHomeButtonEnabled(true);
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
