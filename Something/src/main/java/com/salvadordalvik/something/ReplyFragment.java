package com.salvadordalvik.something;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.salvadordalvik.fastlibrary.FastFragment;
import com.salvadordalvik.something.request.ReplyDataRequest;

/**
 * Created by matthewshepard on 2/10/14.
 */
public class ReplyFragment extends FastFragment {
    private enum REPLY_TYPE {REPLY, QUOTE, EDIT};
    private EditText replyContent;

    private int threadId, postId;
    private REPLY_TYPE type;

    public ReplyFragment() {
        super(R.layout.reply_fragment, R.menu.post_reply);
    }

    @Override
    public void viewCreated(View frag, Bundle savedInstanceState) {
        replyContent = (EditText) frag.findViewById(R.id.reply_content);
    }

    @Override
    public void refreshData(boolean pullToRefresh, boolean staleRefresh) {
            queueRequest(new ReplyDataRequest(threadId, postId, replyListener, errorListener));
    }

    private Response.Listener<ReplyDataRequest.ReplyDataResponse> replyListener = new Response.Listener<ReplyDataRequest.ReplyDataResponse>() {
        @Override
        public void onResponse(ReplyDataRequest.ReplyDataResponse replyDataResponse) {

        }
    };

    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            //TODO display an aggressive retry/cancel screen
        }
    };
}
