package net.fastfourier.something;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import net.fastfourier.something.request.PMReplyDataRequest;
import net.fastfourier.something.request.PMSendRequest;
import net.fastfourier.something.request.ReplyDataRequest;
import net.fastfourier.something.request.ReplyPostRequest;
import net.fastfourier.something.request.SomeError;

/**
 * Created by matthewshepard on 2/10/14.
 */
public class ReplyFragment extends SomeFragment implements DialogInterface.OnCancelListener, TextWatcher {
    private enum BBCODE {BOLD, ITALICS, UNDERLINE, STRIKEOUT, URL, VIDEO, IMAGE, QUOTE, SPOILER, CODE}
    public static final int TYPE_REPLY = 2;
    public static final int TYPE_QUOTE = 3;
    public static final int TYPE_EDIT = 4;
    public static final int TYPE_PM = 5;

    public static final int NEW_PM = -1;

    private ProgressDialog dialog = null;

    private EditText replyContent, replyTitle, replyUsername;
    private boolean replyEnabled = false;

    private int threadId, postId, pmId, replyType;
    private String pmUsername;

    private ReplyDataRequest.ReplyDataResponse replyData = null;
    private PMReplyDataRequest.PMReplyData pmReplyData = null;

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
        pmUsername = intent.getStringExtra("pm_username");
        switch (replyType){
            case TYPE_REPLY:
                if(threadId == 0 || postId != 0 || pmId != 0){
                    throw new IllegalArgumentException("ID MISMATCH");
                }
                setTitle(getSafeString(R.string.reply_title_reply));
                break;
            case TYPE_QUOTE:
                if(threadId == 0 || postId == 0 || pmId != 0){
                    throw new IllegalArgumentException("ID MISMATCH");
                }
                setTitle(getSafeString(R.string.reply_title_reply));
                break;
            case TYPE_EDIT:
                if(threadId != 0 || postId == 0 || pmId != 0){
                    throw new IllegalArgumentException("ID MISMATCH");
                }
                setTitle(getSafeString(R.string.reply_title_edit));
                break;
            case TYPE_PM:
                if(threadId != 0 || postId != 0 || pmId == 0){
                    throw new IllegalArgumentException("ID MISMATCH");
                }
                setTitle(getSafeString(R.string.reply_title_pm));
                break;
            default:
                throw new IllegalArgumentException("INVALID REPLY TYPE");
        }
    }

    @Override
    public void viewCreated(View frag, Bundle savedInstanceState) {
        replyContent = (EditText) frag.findViewById(R.id.reply_content);
        replyTitle = (EditText) frag.findViewById(R.id.reply_title);
        replyUsername = (EditText) frag.findViewById(R.id.reply_username);
        replyContent.addTextChangedListener(this);
        if(replyType != TYPE_PM){
            replyUsername.setVisibility(View.GONE);
            replyTitle.setVisibility(View.GONE);
        }else{
            replyUsername.setVisibility(View.VISIBLE);
            replyTitle.setVisibility(View.VISIBLE);
            replyUsername.addTextChangedListener(this);
            replyTitle.addTextChangedListener(this);
        }
        startRefresh();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem reply = menu.findItem(R.id.menu_post_reply);
        if(reply != null){
            if(replyType == TYPE_PM){
                replyEnabled = pmReplyData != null
                        && replyContent.getText() != null
                        && replyContent.getText().toString().trim().length() > 0
                        && replyTitle.getText() != null
                        && replyTitle.getText().toString().trim().length() > 0
                        && replyUsername.getText() != null
                        && replyUsername.getText().toString().trim().length() > 0;
            }else{
                replyEnabled = replyData != null
                        && replyContent.getText() != null
                        && replyContent.getText().toString().trim().length() > 0;
            }
            reply.setEnabled(replyEnabled);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_post_reply:
                confirmReply();
                return true;
            case R.id.bbcode_bold:
                insertBBCode(BBCODE.BOLD);
                return true;
            case R.id.bbcode_italics:
                insertBBCode(BBCODE.ITALICS);
                return true;
            case R.id.bbcode_underline:
                insertBBCode(BBCODE.UNDERLINE);
                return true;
            case R.id.bbcode_strikeout:
                insertBBCode(BBCODE.STRIKEOUT);
                return true;
            case R.id.bbcode_url:
                insertBBCode(BBCODE.URL);
                return true;
            case R.id.bbcode_video:
                insertBBCode(BBCODE.VIDEO);
                return true;
            case R.id.bbcode_image:
                insertBBCode(BBCODE.IMAGE);
                return true;
            case R.id.bbcode_quote:
                insertBBCode(BBCODE.QUOTE);
                return true;
            case R.id.bbcode_spoiler:
                insertBBCode(BBCODE.SPOILER);
                return true;
            case R.id.bbcode_code:
                insertBBCode(BBCODE.CODE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void insertBBCode(BBCODE code){
        insertBBCode(code, -1, -1);
    }

    public void insertBBCode(BBCODE code, int selectionStart, int selectionEnd){
        if(selectionStart < 0){
            //update selection values
            selectionStart = replyContent.getSelectionStart();
            selectionEnd = replyContent.getSelectionEnd();
        }
        boolean highlighted = selectionStart != selectionEnd;
        String startTag = null;
        String endTag = null;
        switch(code){
            case BOLD:
                startTag = "[b]";
                endTag = "[/b]";
                break;
            case ITALICS:
                startTag = "[i]";
                endTag = "[/i]";
                break;
            case UNDERLINE:
                startTag = "[u]";
                endTag = "[/u]";
                break;
            case STRIKEOUT:
                startTag = "[s]";
                endTag = "[/s]";
                break;
            case URL:
                String link = null;
                ClipboardManager cb = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = cb.getPrimaryClip();
                if(clip != null && clip.getItemCount() > 0){
                    CharSequence clipText = clip.getItemAt(0).getText();
                    if(clipText != null){
                        String text = clipText.toString();
                        if(text.startsWith("http://") || text.startsWith("https://")){
                            link = text;
                        }
                    }
                }
                if(link != null){
                    startTag = "[url="+link+"]";
                }else{
                    startTag = "[url]";
                }
                endTag = "[/url]";
                break;
            case QUOTE:
                startTag = "[quote]";
                endTag = "[/quote]";
                break;
            case IMAGE:
                startTag = "[img]";
                endTag = "[/img]";
                break;
            case VIDEO:
                startTag = "[video]";
                endTag = "[/video]";
                break;
            case SPOILER:
                startTag = "[spoiler]";
                endTag = "[/spoiler]";
                break;
            case CODE:
                startTag = "[code]";
                endTag = "[/code]";
                break;
        }
        if(replyContent.getEditableText() != null){
            if(highlighted){
                replyContent.getEditableText().insert(selectionStart, startTag);
                replyContent.getEditableText().insert(selectionEnd+startTag.length(), endTag);
                replyContent.setSelection(selectionStart+startTag.length());
            }else{
                replyContent.getEditableText().insert(selectionStart, startTag+endTag);
                replyContent.setSelection(selectionStart+startTag.length());
            }
        }
    }

    @Override
    public void refreshData(boolean pullToRefresh, boolean staleRefresh) {
        dismissDialog();
        dialog = ProgressDialog.show(getActivity(), getString(R.string.post_loading), getString(R.string.please_wait), true, false, this);
        if(replyType == TYPE_PM){
            queueRequest(new PMReplyDataRequest(pmId, pmReplyListener, loadingErrorListener));
        }else{
            queueRequest(new ReplyDataRequest(threadId, postId, replyType, replyListener, loadingErrorListener));
        }
    }

    private void confirmReply(){
        if(getActivity() != null){
            int title, confirm;
            String message;
            switch (replyType){
                case TYPE_REPLY:
                    confirm = R.string.confirm_reply;
                    title = R.string.reply_confirm_title_reply;
                    message = getSafeString(R.string.reply_confirm_message_reply) +"\n"+ replyData.threadTitle;
                    break;
                case TYPE_QUOTE:
                    confirm = R.string.confirm_quote;
                    title = R.string.reply_confirm_title_quote;
                    message = getSafeString(R.string.reply_confirm_message_quote) +"\n"+ replyData.threadTitle;
                    break;
                case TYPE_EDIT:
                    confirm = R.string.confirm_edit;
                    title = R.string.reply_confirm_title_edit;
                    message = getSafeString(R.string.reply_confirm_message_edit) +"\n"+ replyData.threadTitle;
                    break;
                case TYPE_PM:
                    confirm = R.string.confirm_pm;
                    title = R.string.reply_confirm_title_pm;
                    message = getSafeString(R.string.reply_confirm_message_pm) +"\n"+replyUsername.getText();
                    break;
                default:
                    throw new IllegalArgumentException("INVALID REPLY TYPE");
            }
            new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(confirm,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    postReply();
                                }
                            }
                    )
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }

    private void postReply(){
        Editable content = replyContent.getText();
        switch (replyType){
            case TYPE_REPLY:
            case TYPE_QUOTE:
            case TYPE_EDIT:
                if(replyData != null && content != null && content.length() > 0){
                    replyData.replyMessage = content.toString().trim();
                    Log.e("ReplyFragment", replyData.replyMessage);
                    queueRequest(new ReplyPostRequest(replyData, postingResult, postingErrorListener));
                    dialog = ProgressDialog.show(getActivity(), getSafeString(R.string.posting_title), getSafeString(R.string.posting_message), true, false, this);
                }else{
                    //this shouldn't happen, throw and log via bugsense
                    throw new IllegalArgumentException("MISSING REPLY DATA");
                }
                break;
            case TYPE_PM:
                Editable title = replyTitle.getText();
                Editable username = replyUsername.getText();
                if(pmReplyData != null && title != null && username != null && content != null && content.length() > 0){
                    pmReplyData.replyMessage = content.toString().trim();
                    pmReplyData.replyUsername = username.toString().trim();
                    pmReplyData.replyTitle = title.toString().trim();
                    queueRequest(new PMSendRequest(pmReplyData, pmSendResult, postingErrorListener));
                    dialog = ProgressDialog.show(getActivity(), getSafeString(R.string.sending_title), getSafeString(R.string.posting_message), true, false, this);
                }else{
                    //this shouldn't happen, throw and log via bugsense
                    throw new IllegalArgumentException("MISSING REPLY DATA");
                }
                break;
        }
    }

    private Response.Listener<PMSendRequest.PMSendResult> pmSendResult = new Response.Listener<PMSendRequest.PMSendResult>() {
        @Override
        public void onResponse(PMSendRequest.PMSendResult response) {
            dismissDialog();
            Activity activity = getActivity();
            if(activity != null){
                activity.setResult(TYPE_PM);
                activity.finish();
            }
        }
    };

    private Response.Listener<ReplyPostRequest.ReplyPostResult> postingResult = new Response.Listener<ReplyPostRequest.ReplyPostResult>() {
        @Override
        public void onResponse(ReplyPostRequest.ReplyPostResult response) {
            dismissDialog();
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
            pmReplyData = replyDataResponse;
            if(replyDataResponse.replyContent != null && replyDataResponse.replyContent.length() > 0){
                replyContent.setText("\n\n"+replyDataResponse.replyContent);
                replyContent.setSelection(0);
            }
            if(!TextUtils.isEmpty(replyDataResponse.replyUsername)){
                replyUsername.setText(replyDataResponse.replyUsername);
            }else if(!TextUtils.isEmpty(pmUsername)){
                replyUsername.setText(pmUsername);
            }
            if(replyDataResponse.replyTitle != null){
                replyTitle.setText(replyDataResponse.replyTitle);
            }
            if(replyUsername.length() > 0 && replyTitle.length() > 0){
                replyContent.requestFocusFromTouch();
            }else if(replyUsername.length() > 0){
                replyTitle.requestFocusFromTouch();
            }
            dismissDialog();
            invalidateOptionsMenu();
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
                    setTitle("Reply: "+replyDataResponse.threadTitle);
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
                if(volleyError instanceof SomeError){
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.post_loading_failed)
                            .setMessage(volleyError.getMessage())
                            .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (getActivity() != null) {
                                        getActivity().finish();
                                    }
                                }
                            })
                            .show();
                }else{
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.post_loading_failed)
                            .setMessage(R.string.posting_failed_message)
                            .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startRefresh();
                                }
                            })
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (getActivity() != null) {
                                        getActivity().finish();
                                    }
                                }
                            })
                            .show();
                }
            }
        }
    };

    private Response.ErrorListener postingErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            dismissDialog();
            if(getActivity() != null){
                if(volleyError instanceof SomeError){
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.posting_failed_title)
                            .setMessage(volleyError.getMessage())
                            .setPositiveButton(R.string.button_ok, null)
                            .show();
                }else{
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.posting_failed_title)
                            .setMessage(R.string.posting_failed_message)
                            .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    postReply();
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                }
            }
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
        invalidateOptionsMenu();
    }
}
