package com.salvadordalvik.something.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.salvadordalvik.something.ReplyFragment;

import org.jsoup.nodes.Document;

/**
 * Created by matthewshepard on 2/10/14.
 */
public class ReplyDataRequest extends HTMLRequest<ReplyDataRequest.ReplyDataResponse> {


    public ReplyDataRequest(int threadId, int postId, int type, Response.Listener<ReplyDataResponse> success, Response.ErrorListener error) {
        super(type == ReplyFragment.TYPE_EDIT ? "http://forums.somethingawful.com/editpost.php" : "http://forums.somethingawful.com/newreply.php", Request.Method.GET, success, error);
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
    public ReplyDataResponse parseHtmlResponse(NetworkResponse response, Document document) throws Exception {
        String formKey = document.getElementsByAttributeValue("name", "formkey").val();
        String formCookie = document.getElementsByAttributeValue("name", "form_cookie").val();
        String replyContent = document.getElementsByAttributeValue("name", "message").text();
        String threadTitle = document.getElementsByClass("breadcrumbs").first().getElementsByTag("a").last().text();

        //options
        boolean signature = document.getElementsByAttributeValue("name", "signature").hasAttr("checked");
        boolean bookmark = document.getElementsByAttributeValue("name", "bookmark").hasAttr("checked");
        boolean emotes = document.getElementsByAttributeValue("name", "disablesmilies").hasAttr("checked");

        return new ReplyDataResponse(signature, bookmark, emotes, formKey, formCookie, replyContent, threadTitle);
    }

    public static class ReplyDataResponse{
        public final boolean signature, bookmark, emotes;
        public final String formKey, formCookie, replyContent, threadTitle;

        protected ReplyDataResponse(boolean signature, boolean bookmark, boolean emotes, String formKey, String formCookie, String replyContent, String threadTitle) {
            this.signature = signature;
            this.bookmark = bookmark;
            this.emotes = emotes;
            this.formKey = formKey;
            this.formCookie = formCookie;
            this.replyContent = replyContent;
            this.threadTitle = threadTitle;
        }
    }
}
