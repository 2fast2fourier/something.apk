package net.fastfourier.something.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;

import org.jsoup.nodes.Document;

/**
 * Created by matthewshepard on 3/10/14.
 */
public class MarkUnreadRequest extends HTMLRequest<Void>{
    public MarkUnreadRequest(int threadId, Response.Listener<Void> success, Response.ErrorListener error) {
        super("http://forums.somethingawful.com/showthread.php", Request.Method.POST, success, error);
        addParam("action", "resetseen");
        addParam("threadid", threadId);
    }

    @Override
    public Void parseHtmlResponse(Request<Void> request, NetworkResponse response, Document document) throws Exception {
        return null;
    }
}
