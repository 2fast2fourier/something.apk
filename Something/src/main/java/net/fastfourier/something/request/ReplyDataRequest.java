package net.fastfourier.something.request;

import android.content.ContentValues;
import android.database.Cursor;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.salvadordalvik.fastlibrary.util.FastDateUtils;

import net.fastfourier.something.ReplyFragment;

import org.joda.time.DateTime;
import org.jsoup.nodes.Document;

/**
 * Created by matthewshepard on 2/10/14.
 */
public class ReplyDataRequest extends HTMLRequest<ReplyDataRequest.ReplyDataResponse> {
    private int threadId, postId, type;

    public ReplyDataRequest(int threadId, int postId, int type, Response.Listener<ReplyDataResponse> success, Response.ErrorListener error) {
        super(type == ReplyFragment.TYPE_EDIT ? "http://forums.somethingawful.com/editpost.php" : "http://forums.somethingawful.com/newreply.php", Request.Method.GET, success, error);
        this.threadId = threadId;
        this.postId = postId;
        this.type = type;
        switch (type){
            case ReplyFragment.TYPE_REPLY:
                addParam("action", "newreply");
                addParam("threadid", threadId);
                break;
            case ReplyFragment.TYPE_QUOTE:
                addParam("action", "newreply");
                addParam("postid", postId);
                break;
            case ReplyFragment.TYPE_EDIT:
                addParam("action", "editpost");
                addParam("postid", postId);
                break;
            default:
                throw new IllegalArgumentException("Missing or incorrect arguments! "+threadId+" - "+postId+" - "+type);
        }
    }

    @Override
    public ReplyDataResponse parseHtmlResponse(Request<ReplyDataResponse> request, NetworkResponse response, Document document) throws Exception {
        String formKey = document.getElementsByAttributeValue("name", "formkey").val();
        String formCookie = document.getElementsByAttributeValue("name", "form_cookie").val();
        String replyContent = document.getElementsByAttributeValue("name", "message").text();
        String threadTitle = document.getElementsByClass("breadcrumbs").first().getElementsByTag("a").last().text();

        //options
        boolean signature = document.getElementsByAttributeValue("name", "signature").hasAttr("checked");
        boolean bookmark = document.getElementsByAttributeValue("name", "bookmark").hasAttr("checked");
        boolean emotes = document.getElementsByAttributeValue("name", "disablesmilies").hasAttr("checked");

        return new ReplyDataResponse(signature, bookmark, emotes, formKey, formCookie, unencodeHtml(replyContent), threadTitle, threadId, postId, type);
    }

    public static class ReplyDataResponse{
        public static final String[] COLUMNS = {
                "reply_signature",
                "reply_bookmark",
                "reply_emotes",
                "reply_formkey",
                "reply_formcookie",
                "reply_original_content",
                "reply_user_content",
                "reply_title",
                "reply_thread_id",
                "reply_post_id",
                "reply_type",
                "reply_saved_timestamp"
        };

        public final boolean signature, bookmark, emotes;
        public final String formKey, formCookie, originalContent, threadTitle;
        public final int threadId, postId, type;
        public String replyMessage, savedTimestamp;

        protected ReplyDataResponse(boolean signature, boolean bookmark, boolean emotes, String formKey, String formCookie, String replyContent, String threadTitle, int threadId, int postId, int type) {
            this.signature = signature;
            this.bookmark = bookmark;
            this.emotes = emotes;
            this.formKey = formKey;
            this.formCookie = formCookie;
            this.originalContent = replyContent;
            this.threadTitle = threadTitle;
            this.threadId = threadId;
            this.postId = postId;
            this.type = type;
        }

        public ReplyDataResponse(Cursor data){
            this.signature = data.getInt(data.getColumnIndex("reply_signature")) != 0;
            this.bookmark = data.getInt(data.getColumnIndex("reply_bookmark")) != 0;
            this.emotes = data.getInt(data.getColumnIndex("reply_emotes")) != 0;
            this.formKey = data.getString(data.getColumnIndex("reply_formkey"));
            this.formCookie = data.getString(data.getColumnIndex("reply_formcookie"));
            this.originalContent = data.getString(data.getColumnIndex("reply_original_content"));
            this.replyMessage = data.getString(data.getColumnIndex("reply_user_content"));
            this.threadTitle = data.getString(data.getColumnIndex("reply_title"));
            this.threadId = data.getInt(data.getColumnIndex("reply_thread_id"));
            this.postId = data.getInt(data.getColumnIndex("reply_post_id"));
            this.type = data.getInt(data.getColumnIndex("reply_type"));
            this.savedTimestamp = data.getString(data.getColumnIndex("reply_saved_timestamp"));
        }

        public ContentValues toContentValues(){
            ContentValues cv = new ContentValues();
            cv.put("reply_id", generateReplyUID(threadId, postId, type));
            cv.put("reply_thread_id", threadId);
            cv.put("reply_post_id", postId);
            cv.put("reply_type", type);
            cv.put("reply_original_content", originalContent);
            cv.put("reply_user_content", replyMessage);
            cv.put("reply_formcookie", formCookie);
            cv.put("reply_formkey", formKey);
            cv.put("reply_title", threadTitle);
            cv.put("reply_signature", signature);
            cv.put("reply_bookmark", bookmark);
            cv.put("reply_emotes", emotes);
            cv.put("reply_saved_timestamp", FastDateUtils.printSqliteTimestamp(DateTime.now()));
            return cv;
        }

        public static long generateReplyUID(int threadId, int postId, int type){
            return ((long)type) << 56 | ((long)postId) << 32 | threadId;
        }
    }
}
