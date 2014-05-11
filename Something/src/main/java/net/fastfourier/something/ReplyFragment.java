package net.fastfourier.something;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.salvadordalvik.fastlibrary.data.FastQueryTask;
import com.salvadordalvik.fastlibrary.util.FastDateUtils;

import net.fastfourier.something.data.SomeDatabase;
import net.fastfourier.something.request.PMReplyDataRequest;
import net.fastfourier.something.request.PMSendRequest;
import net.fastfourier.something.request.ReplyDataRequest;
import net.fastfourier.something.request.ReplyPostRequest;
import net.fastfourier.something.request.SomeError;

import java.util.List;

/**
 * Created by matthewshepard on 2/10/14.
 */
public class ReplyFragment extends SomeFragment implements DialogInterface.OnCancelListener, TextWatcher, ActionMode.Callback {

    private static final int DRAFT_PREVIEW_LENGTH = 100;
    private enum BBCODE {BOLD, ITALICS, UNDERLINE, STRIKEOUT, URL, VIDEO, IMAGE, QUOTE, SPOILER, CODE}

    public static final int TYPE_REPLY = 2;
    public static final int TYPE_QUOTE = 3;
    public static final int TYPE_EDIT = 4;
    public static final int TYPE_PM = 5;

    public static final int NEW_PM = -1;

    private ProgressDialog dialog = null;

    private EditText replyContent, replyTitle, replyUsername;

    private ActionMode selectionMode;

    private int threadId, postId, pmId, replyType;
    private String pmUsername;
    private boolean sentReply = false;

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
        replyContent.setCustomSelectionActionModeCallback(this);
        startRefresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(shouldSaveDraft()){
            if(replyType == TYPE_PM){
                if(preparePMData()){
                    Log.e("ReplyFragment", "save draft "+pmId);
                    SomeDatabase.getDatabase().insertRows(SomeDatabase.TABLE_SAVED_DRAFT, SQLiteDatabase.CONFLICT_REPLACE, pmReplyData.toContentValues());
                }
            }else{
                if(prepareReplyData()){
                    SomeDatabase.getDatabase().insertRows(SomeDatabase.TABLE_SAVED_DRAFT, SQLiteDatabase.CONFLICT_REPLACE, replyData.toContentValues());
                }
            }
        }
    }

    private boolean shouldSaveDraft() {
        if(sentReply){
            return false;
        }
        Log.e("ReplyFragment", "shouldSaveDraft "+pmId);
        if(replyType == TYPE_PM){
            return pmReplyData != null && replyContent.length() > 0 && !replyContent.getText().toString().trim().equalsIgnoreCase(pmReplyData.replyContent.trim());
        }else{
            return replyData != null && replyContent.length() > 0 && !replyContent.getText().toString().trim().equalsIgnoreCase(replyData.originalContent.trim());
        }
    }

    private void discardDraft(){
        switch (replyType){
            case TYPE_EDIT:
                SomeDatabase.getDatabase().deleteRows(SomeDatabase.TABLE_SAVED_DRAFT, "reply_post_id=? AND reply_type=?", Long.toString(postId), Long.toString(TYPE_EDIT));
                break;
            case TYPE_REPLY:
            case TYPE_QUOTE:
                SomeDatabase.getDatabase().deleteRows(SomeDatabase.TABLE_SAVED_DRAFT, "reply_thread_id=? AND reply_type!=?", Long.toString(threadId), Long.toString(TYPE_EDIT));
                break;
            case TYPE_PM:
                SomeDatabase.getDatabase().deleteRows(SomeDatabase.TABLE_SAVED_DRAFT, "reply_post_id=? AND reply_type=?", Long.toString(pmId), Long.toString(TYPE_PM));
                break;
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem reply = menu.findItem(R.id.menu_post_reply);
        if(reply != null){
            boolean replyEnabled;
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
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        selectionMode = mode;
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        getActivity().getMenuInflater().inflate(R.menu.bbcode_block, menu);
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if(item.getItemId() == R.id.bbcode){
            showBBCodeMenu();
            return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        selectionMode = null;
    }

    private void showBBCodeMenu() {
        //we have to dismiss the actionbar, we can't put a submenu in the context actionbar without it auto-closing.
        //https://code.google.com/p/android/issues/detail?id=23381
        //dismissing the context actionbar kills the selection, so save and reselect.
        final int selectionStart = replyContent.getSelectionStart();
        final int selectionEnd = replyContent.getSelectionEnd();
        if(selectionMode != null){
            selectionMode.finish();
            selectionMode = null;
        }
        replyContent.setSelection(selectionStart, selectionEnd);
        new AlertDialog.Builder(getActivity()).setTitle(R.string.bbcode).setItems(R.array.bbcode_items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        insertBBCode(BBCODE.BOLD, selectionStart, selectionEnd);
                        break;
                    case 1:
                        insertBBCode(BBCODE.ITALICS, selectionStart, selectionEnd);
                        break;
                    case 2:
                        insertBBCode(BBCODE.UNDERLINE, selectionStart, selectionEnd);
                        break;
                    case 3:
                        insertBBCode(BBCODE.STRIKEOUT, selectionStart, selectionEnd);
                        break;
                    case 4:
                        insertBBCode(BBCODE.IMAGE, selectionStart, selectionEnd);
                        break;
                    case 5:
                        insertBBCode(BBCODE.URL, selectionStart, selectionEnd);
                        break;
                    case 6:
                        insertBBCode(BBCODE.VIDEO, selectionStart, selectionEnd);
                        break;
                    case 7:
                        insertBBCode(BBCODE.QUOTE, selectionStart, selectionEnd);
                        break;
                    case 8:
                        insertBBCode(BBCODE.SPOILER, selectionStart, selectionEnd);
                        break;
                    case 9:
                        insertBBCode(BBCODE.CODE, selectionStart, selectionEnd);
                        break;
                }
            }
        }).show();
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
        switch (replyType){
            case TYPE_REPLY:
            case TYPE_QUOTE:
            case TYPE_EDIT:
                if(prepareReplyData()){
                    queueRequest(new ReplyPostRequest(replyData, postingResult, postingErrorListener));
                    dialog = ProgressDialog.show(getActivity(), getSafeString(R.string.posting_title), getSafeString(R.string.posting_message), true, false, this);
                }else{
                    //this shouldn't happen, throw and log via bugsense
                    throw new IllegalArgumentException("MISSING REPLY DATA");
                }
                break;
            case TYPE_PM:
                if(preparePMData()){
                    queueRequest(new PMSendRequest(pmReplyData, pmSendResult, postingErrorListener));
                    dialog = ProgressDialog.show(getActivity(), getSafeString(R.string.sending_title), getSafeString(R.string.posting_message), true, false, this);
                }else{
                    //this shouldn't happen, throw and log via bugsense
                    throw new IllegalArgumentException("MISSING PM REPLY DATA");
                }
                break;
        }
    }

    private boolean prepareReplyData(){
        Editable content = replyContent.getText();
        if(replyData != null && content != null && content.length() > 0) {
            replyData.replyMessage = content.toString().trim();
            return true;
        }
        return false;
    }

    private boolean preparePMData(){
        Editable content = replyContent.getText();
        Editable title = replyTitle.getText();
        Editable username = replyUsername.getText();
        if(pmReplyData != null && title != null && username != null && content != null && content.length() > 0) {
            pmReplyData.replyMessage = content.toString().trim();
            pmReplyData.replyUsername = username.toString().trim();
            pmReplyData.replyTitle = title.toString().trim();
            return true;
        }
        return false;
    }

    private Response.Listener<PMSendRequest.PMSendResult> pmSendResult = new Response.Listener<PMSendRequest.PMSendResult>() {
        @Override
        public void onResponse(PMSendRequest.PMSendResult response) {
            sentReply = true;
            discardDraft();
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
            sentReply = true;
            discardDraft();
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
            querySavedPM();
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
                    replyContent.setText(replyDataResponse.originalContent +"\n\n");
                    replyContent.setSelection(replyDataResponse.originalContent.length() + 2);
                    setTitle("Reply: "+replyDataResponse.threadTitle);
                    break;
                case TYPE_EDIT:
                    replyContent.setText(replyDataResponse.originalContent +"\n\n");
                    replyContent.setSelection(replyDataResponse.originalContent.length() + 2);
                    setTitle("Edit: "+replyDataResponse.threadTitle);
                    break;
            }
            dismissDialog();
            invalidateOptionsMenu();
            if(replyType == TYPE_EDIT){
                querySavedEdit();
            }else{
                querySavedDrafts();
            }
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

    private void querySavedDrafts(){
        new FastQueryTask<ReplyDataRequest.ReplyDataResponse>(SomeDatabase.getDatabase(),
            new FastQueryTask.QueryResultCallback<ReplyDataRequest.ReplyDataResponse>() {
                @Override
                public int[] findColumns(Cursor data) {
                    return FastQueryTask.findColumnIndicies(data, ReplyDataRequest.ReplyDataResponse.COLUMNS);
                }

                @Override
                public void queryResult(List<ReplyDataRequest.ReplyDataResponse> results) {
                    if(results.size() > 0 && getActivity() != null){
                        final ReplyDataRequest.ReplyDataResponse draft = results.get(0);
                        StringBuilder message = new StringBuilder("You have a saved reply:<br/><br/><i>");
                        if(draft.replyMessage.length() > DRAFT_PREVIEW_LENGTH){
                            message.append(draft.replyMessage.substring(0, DRAFT_PREVIEW_LENGTH).replaceAll("\\n","<br/>"));
                            message.append("...");
                        }else{
                            message.append(draft.replyMessage.replaceAll("\\n","<br/>"));
                        }
                        message.append("</i>");
                        if(!TextUtils.isEmpty(draft.savedTimestamp)){
                            message.append("<br/><br/>Saved ");
                            message.append(FastDateUtils.shortRecentDate(draft.savedTimestamp));
                            message.append(" ago");
                        }
                        new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.reply_draft_title_reply))
                                .setMessage(Html.fromHtml(message.toString()))
                                .setPositiveButton(replyType == TYPE_QUOTE ? "Multiquote" : "Keep", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (replyType == TYPE_QUOTE) {
                                            replyContent.setText(draft.replyMessage+"\n"+replyData.originalContent+"\n\n");
                                        } else if (replyType == TYPE_REPLY) {
                                            replyContent.setText(draft.replyMessage.trim()+"\n\n");
                                        }
                                        replyContent.setSelection(replyContent.length());
                                    }
                                })
                                .setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        discardDraft();
                                    }
                                })
                                .show();
                    }
                }

                @Override
                public ReplyDataRequest.ReplyDataResponse createItem(Cursor data, int[] columns) {
                    return new ReplyDataRequest.ReplyDataResponse(data);
                }
            })
        .query(SomeDatabase.TABLE_SAVED_DRAFT, "reply_saved_timestamp DESC", "reply_thread_id=? AND reply_type!=?", Long.toString(threadId), Long.toString(TYPE_EDIT));
    }

    private void querySavedEdit(){
        new FastQueryTask<ReplyDataRequest.ReplyDataResponse>(SomeDatabase.getDatabase(),
            new FastQueryTask.QueryResultCallback<ReplyDataRequest.ReplyDataResponse>() {
                @Override
                public int[] findColumns(Cursor data) {
                    return FastQueryTask.findColumnIndicies(data, ReplyDataRequest.ReplyDataResponse.COLUMNS);
                }

                @Override
                public void queryResult(List<ReplyDataRequest.ReplyDataResponse> results) {
                    if(results.size() > 0 && getActivity() != null){
                        final ReplyDataRequest.ReplyDataResponse draft = results.get(0);
                        StringBuilder message = new StringBuilder("You have a saved edit:<br/><br/><i>");
                        if(draft.replyMessage.length() > DRAFT_PREVIEW_LENGTH){
                            message.append(draft.replyMessage.substring(0, DRAFT_PREVIEW_LENGTH).replaceAll("\\n","<br/>"));
                            message.append("...");
                        }else{
                            message.append(draft.replyMessage.replaceAll("\\n","<br/>"));
                        }
                        message.append("</i>");
                        if(!TextUtils.isEmpty(draft.savedTimestamp)){
                            message.append("<br/><br/>Saved ");
                            message.append(FastDateUtils.shortRecentDate(draft.savedTimestamp));
                            message.append(" ago");
                        }
                        new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.reply_draft_title_edit))
                            .setMessage(Html.fromHtml(message.toString()))
                            .setPositiveButton("Keep", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    replyContent.setText(draft.replyMessage);
                                    replyContent.setSelection(draft.replyMessage.length());
                                }
                            })
                            .setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    discardDraft();
                                }
                            })
                            .show();
                    }
                }

                @Override
                public ReplyDataRequest.ReplyDataResponse createItem(Cursor data, int[] columns) {
                    return new ReplyDataRequest.ReplyDataResponse(data);
                }
            })
            .query(SomeDatabase.TABLE_SAVED_DRAFT, "reply_saved_timestamp DESC", "reply_post_id=? AND reply_type=?", Long.toString(postId), Long.toString(replyType));
    }

    private void querySavedPM(){
        Log.e("ReplyFragment", "querySavedPM "+pmId);
        new FastQueryTask<PMReplyDataRequest.PMReplyData>(SomeDatabase.getDatabase(),
            new FastQueryTask.QueryResultCallback<PMReplyDataRequest.PMReplyData>() {
                @Override
                public int[] findColumns(Cursor data) {
                    return new int[0];
                }

                @Override
                public void queryResult(List<PMReplyDataRequest.PMReplyData> results) {
                    if(results.size() > 0 && getActivity() != null){
                        final PMReplyDataRequest.PMReplyData draft = results.get(0);
                        StringBuilder message = new StringBuilder("You have a saved message:<br/><br/><i>");
                        if(draft.replyMessage.length() > DRAFT_PREVIEW_LENGTH){
                            message.append(draft.replyMessage.substring(0, DRAFT_PREVIEW_LENGTH).replaceAll("\\n","<br/>"));
                            message.append("...");
                        }else{
                            message.append(draft.replyMessage.replaceAll("\\n","<br/>"));
                        }
                        message.append("</i>");
                        if(!TextUtils.isEmpty(draft.savedTimestamp)){
                            message.append("<br/><br/>Saved ");
                            message.append(FastDateUtils.shortRecentDate(draft.savedTimestamp));
                            message.append(" ago");
                        }
                        new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.reply_draft_title_pm))
                                .setMessage(Html.fromHtml(message.toString()))
                                .setPositiveButton("Keep", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        replyContent.setText(draft.replyMessage);
                                        replyContent.setSelection(draft.replyMessage.length());
                                        replyTitle.setText(draft.replyTitle);
                                        replyUsername.setText(draft.replyUsername);
                                    }
                                })
                                .setNegativeButton("Discard", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        discardDraft();
                                    }
                                })
                                .show();
                    }
                }

                @Override
                public PMReplyDataRequest.PMReplyData createItem(Cursor data, int[] columns) {
                    Log.e("ReplyFragment", "createItem "+pmId);
                    return new PMReplyDataRequest.PMReplyData(data);
                }
            })
            .query(SomeDatabase.TABLE_SAVED_DRAFT, "reply_saved_timestamp DESC", "reply_post_id=? AND reply_type=?", Long.toString(pmId), Long.toString(TYPE_PM));
    }

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

    @Override
    public CharSequence getTitle() {
        return "Reply";
    }
}
