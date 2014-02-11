package com.salvadordalvik.something.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;

import org.jsoup.nodes.Document;

/**
 * Created by matthewshepard on 2/10/14.
 */
public class ReplyDataRequest extends HTMLRequest<ReplyDataRequest.ReplyDataResponse> {


    public ReplyDataRequest(int threadId, int postId, Response.Listener<ReplyDataResponse> success, Response.ErrorListener error) {
        super("http://forums.somethingawful.com/newreply.php", Request.Method.GET, success, error);
        addParam("action", "newreply");
        if(threadId > 0){
            addParam("threadid", threadId);
        }
        if(postId > 0){
            addParam("postid", postId);
        }
    }

    @Override
    public ReplyDataResponse parseHtmlResponse(NetworkResponse response, Document document) throws Exception {
        String formKey = document.getElementsByAttributeValue("name", "formkey").val();
        String formCookie = document.getElementsByAttributeValue("name", "form_cookie").val();
        String replyContent = document.getElementsByAttributeValue("name", "message").text();
        //options
        boolean signature = document.getElementsByAttributeValue("name", "signature").hasAttr("checked");
        boolean bookmark = document.getElementsByAttributeValue("name", "bookmark").hasAttr("checked");
        boolean emotes = document.getElementsByAttributeValue("name", "disablesmilies").hasAttr("checked");

        return new ReplyDataResponse(signature, bookmark, emotes, formKey, formCookie, replyContent);
    }

    public static class ReplyDataResponse{
        public final boolean signature, bookmark, emotes;
        public final String formKey, formCookie, replyContent;

        protected ReplyDataResponse(boolean signature, boolean bookmark, boolean emotes, String formKey, String formCookie, String replyContent) {
            this.signature = signature;
            this.bookmark = bookmark;
            this.emotes = emotes;
            this.formKey = formKey;
            this.formCookie = formCookie;
            this.replyContent = replyContent;
        }
    }
}
