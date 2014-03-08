package com.salvadordalvik.something;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.salvadordalvik.fastlibrary.FastFragment;
import com.salvadordalvik.fastlibrary.alert.FastAlert;
import com.salvadordalvik.something.request.PMReplyDataRequest;
import com.salvadordalvik.something.request.ReplyDataRequest;

/**
 * Created by matthewshepard on 2/10/14.
 */
public class ReplyFragment extends SomeFragment implements DialogInterface.OnCancelListener {
    public static final int TYPE_REPLY = 2;
    public static final int TYPE_QUOTE = 3;
    public static final int TYPE_EDIT = 4;
    public static final int TYPE_PM = 5;

    private ProgressDialog dialog = null;

    private EditText replyContent;

    private int threadId, postId, pmId, replyType;

    public ReplyFragment() {
        super(R.layout.reply_fragment, R.menu.post_reply);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getActivity().getIntent();
        threadId = intent.getIntExtra("thread_id", 0);
        postId = intent.getIntExtra("post_id", 0);
        pmId = intent.getIntExtra("pm_id", 0);
        replyType = intent.getIntExtra("reply_type", 0);
        switch (replyType){
            case TYPE_REPLY:
                if(threadId == 0 || postId != 0 || pmId != 0){
                    throw new IllegalArgumentException("ID MISMATCH");
                }
                break;
            case TYPE_QUOTE:
                if(threadId == 0 || postId == 0 || pmId != 0){
                    throw new IllegalArgumentException("ID MISMATCH");
                }
                break;
            case TYPE_EDIT:
                if(threadId != 0 || postId == 0 || pmId != 0){
                    throw new IllegalArgumentException("ID MISMATCH");
                }
                break;
            case TYPE_PM:
                if(threadId != 0 || postId != 0 || pmId == 0){
                    throw new IllegalArgumentException("ID MISMATCH");
                }
                break;
        }
    }

    @Override
    public void viewCreated(View frag, Bundle savedInstanceState) {
        replyContent = (EditText) frag.findViewById(R.id.reply_content);

        startRefresh();
    }

    @Override
    public void refreshData(boolean pullToRefresh, boolean staleRefresh) {
        dismissDialog();
        dialog = ProgressDialog.show(getActivity(), getString(R.string.post_loading), getString(R.string.please_wait), true, true, this);
        if(replyType == TYPE_PM){
            queueRequest(new PMReplyDataRequest(pmId, pmReplyListener, loadingErrorListener));
        }else{
            queueRequest(new ReplyDataRequest(threadId, postId, replyType, replyListener, loadingErrorListener));
        }
    }

    private Response.Listener<PMReplyDataRequest.PMReplyData> pmReplyListener = new Response.Listener<PMReplyDataRequest.PMReplyData>() {
        @Override
        public void onResponse(PMReplyDataRequest.PMReplyData replyDataResponse) {
            replyContent.setText("\n\n"+replyDataResponse.replyContent);
            replyContent.setSelection(0);
            dismissDialog();
        }
    };

    private void dismissDialog(){
        if(dialog != null){
            dialog.dismiss();
            dialog = null;
        }
    }

    private Response.Listener<ReplyDataRequest.ReplyDataResponse> replyListener = new Response.Listener<ReplyDataRequest.ReplyDataResponse>() {
        @Override
        public void onResponse(ReplyDataRequest.ReplyDataResponse replyDataResponse) {
            replyContent.setText(replyDataResponse.replyContent+"\n\n");
            replyContent.setSelection(replyDataResponse.replyContent.length() + 2);
            switch (replyType){
                case TYPE_REPLY:
                    setTitle("Reply: "+replyDataResponse.threadTitle);
                    break;
                case TYPE_QUOTE:
                    setTitle("Quote: "+replyDataResponse.threadTitle);
                    break;
                case TYPE_EDIT:
                    setTitle("Edit: "+replyDataResponse.threadTitle);
                    break;
            }
            dismissDialog();
        }
    };

    private Response.ErrorListener loadingErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            dismissDialog();
            if(getActivity() != null){
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.post_loading_failed)
                        .setMessage(R.string.post_loading_failed_message)
                        .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startRefresh();
                            }
                        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                    }
                }).show();
            }
        }
    };

    private Response.ErrorListener postingErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            FastAlert.error(ReplyFragment.this, R.string.posting_failed_title);
            //TODO display an aggressive retry/cancel screen
        }
    };

    @Override
    public void onCancel(DialogInterface dialog) {
        if(this.dialog == dialog){
            this.dialog = null;
        }
    }
}
