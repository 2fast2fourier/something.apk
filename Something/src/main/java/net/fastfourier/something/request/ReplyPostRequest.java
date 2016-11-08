package net.fastfourier.something.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import net.fastfourier.something.ReplyFragment;
import net.fastfourier.something.util.Constants;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by matthewshepard on 3/8/14.
 */
public class ReplyPostRequest extends HTMLRequest<ReplyPostRequest.ReplyPostResult>{
    public ReplyPostRequest(ReplyDataRequest.ReplyDataResponse reply, Response.Listener<ReplyPostResult> success, Response.ErrorListener error) {
        super(ReplyFragment.TYPE_EDIT == reply.type ? Constants.BASE_URL + "editpost.php" : Constants.BASE_URL + "newreply.php", Request.Method.POST, success, error);
        switch (reply.type){
            case ReplyFragment.TYPE_QUOTE:
            case ReplyFragment.TYPE_REPLY:
                addParam("action", "postreply");
                addParam("threadid", reply.threadId);
                break;
            case ReplyFragment.TYPE_EDIT:
                addParam("action", "updatepost");
                addParam("postid", reply.postId);
                break;
        }
        addParam("formkey", reply.formKey);
        addParam("form_cookie", reply.formCookie);
        addParam("message", encodeHtml(reply.replyMessage));
        addParam("parseurl", "yes");
        if(reply.bookmark){
            addParam("bookmark", "yes");
        }
        if(reply.signature){
            addParam("signature", "yes");
        }
        if(reply.emotes){
            addParam("disablesmilies", "yes");
        }
    }

    @Override
    public ReplyPostResult parseHtmlResponse(Request<ReplyPostResult> request, NetworkResponse response, Document document) throws Exception {
        int postId = 0, threadId = 0;
        Element redirect = document.getElementsByAttributeValue("http-equiv","Refresh").first();
        if(redirect != null){
            String redir = redirect.attr("content");
            Matcher post = Pattern.compile("postid=(\\d+)").matcher(redir);
            if(post.find()){
                postId = Integer.parseInt(post.group(1));
            }
            Matcher thread = Pattern.compile("threadid=(\\d+)").matcher(redir);
            if(thread.find()){
                threadId = Integer.parseInt(thread.group(1));
            }
        }
        return new ReplyPostResult(postId, threadId);
    }

    public static class ReplyPostResult {
        public int jumpThreadId;
        public int jumpPostId;

        public ReplyPostResult(int jumpPostId, int jumpThreadId) {
            this.jumpPostId = jumpPostId;
            this.jumpThreadId = jumpThreadId;
        }
    }
}
