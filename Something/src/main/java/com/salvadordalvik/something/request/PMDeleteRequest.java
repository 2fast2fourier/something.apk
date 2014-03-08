package com.salvadordalvik.something.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;

import org.jsoup.nodes.Document;

/**
 * Created by matthewshepard on 3/8/14.
 */
public class PMDeleteRequest extends HTMLRequest {
    public PMDeleteRequest(int folderId, Response.Listener success, Response.ErrorListener error, int... pmIds) {
        super("http://forums.somethingawful.com/private.php", Request.Method.POST, success, error);
        for(int pmId : pmIds){
            addParam("privatemessage["+pmId+"]", "yes");
        }
        addParam("action", "dostuff");
        addParam("thisfolder", folderId);
        addParam("folderid", folderId);
        addParam("delete", "delete");
    }

    @Override
    public Object parseHtmlResponse(NetworkResponse response, Document document) throws Exception {
        return null;
    }
}
