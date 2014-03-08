package com.salvadordalvik.something;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.salvadordalvik.fastlibrary.alert.FastAlert;
import com.salvadordalvik.something.request.PMReplyDataRequest;
import com.salvadordalvik.something.request.ReplyDataRequest;
import com.salvadordalvik.something.request.ReplyPostRequest;

/**
 * Created by matthewshepard on 2/10/14.
 */
public class ReplyFragment extends SomeFragment implements DialogInterface.OnCancelListener, TextWatcher {
    public static final int TYPE_REPLY = 2;
    public static final int TYPE_QUOTE = 3;
    public static final int TYPE_EDIT = 4;
    public static final int TYPE_PM = 5;

    private ProgressDialog dialog = null;

    private EditText replyContent;
    private boolean replyEnabled = false;

    private int threadId, postId, pmId, replyType;

    private ReplyDataRequest.ReplyDataResponse replyData = null;

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
        replyContent.addTextChangedListener(this);
        startRefresh();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem reply = menu.findItem(R.id.menu_post_reply);
        if(reply != null){
            replyEnabled = replyData != null && replyContent.length() > 0;
            reply.setEnabled(replyEnabled);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_post_reply:
                postReply();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void postReply(){
        Editable content = replyContent.getText();
        if(replyData != null && content != null && content.length() > 0){
            replyData.replyMessage = content.toString().trim();
            queueRequest(new ReplyPostRequest(replyData, postingResult, postingErrorListener));
        }else{
            //this shouldn't happen, throw and log via bugsense
            throw new IllegalArgumentException("MISSING REPLY DATA");
        }
    }

    private Response.Listener<ReplyPostRequest.ReplyPostResult> postingResult = new Response.Listener<ReplyPostRequest.ReplyPostResult>() {
        @Override
        public void onResponse(ReplyPostRequest.ReplyPostResult response) {
            Activity activity = getActivity();
            if(activity != null){
                activity.setResult(response.jumpPostId, new Intent().putExtra("thread_id", response.jumpThreadId).putExtra("post_id", response.jumpPostId));
                activity.finish();
            }
        }
    };

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
            replyData = replyDataResponse;
            switch (replyType){
                case TYPE_REPLY:
                    setTitle("Reply: "+replyDataResponse.threadTitle);
                    break;
                case TYPE_QUOTE:
                    replyContent.setText(replyDataResponse.replyContent+"\n\n");
                    replyContent.setSelection(replyDataResponse.replyContent.length() + 2);
                    setTitle("Quote: "+replyDataResponse.threadTitle);
                    break;
                case TYPE_EDIT:
                    replyContent.setText(replyDataResponse.replyContent+"\n\n");
                    replyContent.setSelection(replyDataResponse.replyContent.length() + 2);
                    setTitle("Edit: "+replyDataResponse.threadTitle);
                    break;
            }
            dismissDialog();
            invalidateOptionsMenu();
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

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if(!replyEnabled || s.length() == 0){
            invalidateOptionsMenu();
        }
    }
}
