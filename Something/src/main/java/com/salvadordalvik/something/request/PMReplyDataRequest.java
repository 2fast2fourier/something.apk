package com.salvadordalvik.something.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;

import org.jsoup.nodes.Document;

/**
 * Created by matthewshepard on 3/8/14.
 */
public class PMReplyDataRequest extends HTMLRequest<PMReplyDataRequest.PMReplyData> {
    public PMReplyDataRequest(int pmId, Response.Listener<PMReplyData> success, Response.ErrorListener error) {
        super("", Request.Method.GET, success, error);
    }

    @Override
    public PMReplyData parseHtmlResponse(NetworkResponse response, Document document) throws Exception {
        return new PMReplyData();
    }

    public static class PMReplyData {
        public String replyContent, username;
    }
}
