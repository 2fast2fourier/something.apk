package net.fastfourier.something;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.MenuItem;

/**
 * Created by matthewshepard on 2/10/14.
 */
public class ReplyActivity extends SomeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reply_activity);
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
