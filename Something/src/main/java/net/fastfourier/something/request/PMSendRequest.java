package net.fastfourier.something.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;

import org.jsoup.nodes.Document;

/**
 * Created by matthewshepard on 3/8/14.
 */
public class PMSendRequest extends HTMLRequest<PMSendRequest.PMSendResult> {
    public PMSendRequest(PMReplyDataRequest.PMReplyData reply, Response.Listener<PMSendResult> success, Response.ErrorListener error) {
        super("http://forums.somethingawful.com/private.php", Request.Method.POST, success, error);
        addParam("action", "dosend");
        if(reply.replyToPMId > 0){
            addParam("prevmessageid", reply.replyToPMId);
        }
        addParam("touser", reply.replyUsername);
        addParam("title", reply.replyTitle);
        addParam("message", reply.replyMessage);
        addParam("parseurl", "yes");
        addParam("savecopy", "yes");
        addParam("iconid", 0);
    }

    @Override
    public PMSendResult parseHtmlResponse(NetworkResponse response, Document document) throws Exception {
        return new PMSendResult();
    }

    public static class PMSendResult {
    }
}
