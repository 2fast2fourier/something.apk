package net.fastfourier.something.request;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;

import net.fastfourier.something.ReplyFragment;

import org.jsoup.nodes.Document;

/**
 * Created by matthewshepard on 3/8/14.
 */
public class PMReplyDataRequest extends HTMLRequest<PMReplyDataRequest.PMReplyData> {
    public PMReplyDataRequest(int pmId, Response.Listener<PMReplyData> success, Response.ErrorListener error) {
        super("http://forums.somethingawful.com/private.php", Request.Method.GET, success, error);
        addParam("action", "newmessage");
        if(pmId > 0){
            addParam("privatemessageid", pmId);
        }
    }

    @Override
    public PMReplyData parseHtmlResponse(NetworkResponse response, Document document) throws Exception {
        int pmId = 0;
        String id = document.getElementsByAttributeValue("name", "prevmessageid").val();
        if(id != null && id.matches("\\d+")){
            pmId = Integer.parseInt(id);
        }
        String username = document.getElementsByAttributeValue("name", "touser").val();
        String replyContent = document.getElementsByAttributeValue("name", "message").text();
        String title = document.getElementsByAttributeValue("name", "title").val();
        return new PMReplyData(pmId, unencodeHtml(replyContent), username, unencodeHtml(title));
    }

    public static class PMReplyData {
        public final int replyToPMId;
        public final String replyContent;

        public String replyMessage, replyUsername, replyTitle;

        public PMReplyData(int pmId, String replyContent, String username, String title) {
            this.replyToPMId = pmId;
            this.replyContent = replyContent;
            this.replyUsername = username;
            this.replyTitle = title;
        }



        public PMReplyData(Cursor data){
            this.replyTitle = data.getString(data.getColumnIndex("reply_title"));
            this.replyContent = data.getString(data.getColumnIndex("reply_original_content"));
            this.replyMessage = data.getString(data.getColumnIndex("reply_user_content"));
            this.replyUsername = data.getString(data.getColumnIndex("reply_username"));
            this.replyToPMId = data.getInt(data.getColumnIndex("reply_post_id"));
        }

        public ContentValues toContentValues(){
            ContentValues cv = new ContentValues();
            cv.put("reply_id", generateReplyUID(replyUsername.hashCode(), replyToPMId, ReplyFragment.TYPE_PM));
            cv.put("reply_post_id", replyToPMId);
            cv.put("reply_type", ReplyFragment.TYPE_PM);
            cv.put("reply_original_content", replyContent);
            cv.put("reply_user_content", replyMessage);
            cv.put("reply_username", replyUsername);
            cv.put("reply_title", replyTitle);
            return cv;
        }

        private static long generateReplyUID(int threadId, int postId, int type){
            return ((long)type) << 56 | ((long)postId) << 32 | threadId;
        }
    }
}
