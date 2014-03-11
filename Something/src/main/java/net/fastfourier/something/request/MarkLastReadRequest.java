package net.fastfourier.something.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import net.fastfourier.something.util.SomePreferences;

import org.jsoup.nodes.Document;

/**
 * Created by matthewshepard on 2/2/14.
 */
public class MarkLastReadRequest extends HTMLRequest<ThreadPageRequest.ThreadPage> {
    public MarkLastReadRequest(int threadId, int postIndex, Response.Listener success, Response.ErrorListener error) {
        super("http://forums.somethingawful.com/showthread.php", Request.Method.GET, success, error);
        addParam("action", "setseen");
        addParam("threadid", threadId);
        addParam("index", postIndex);
    }

    @Override
    public ThreadPageRequest.ThreadPage parseHtmlResponse(NetworkResponse response, Document document) throws Exception {
        return ThreadPageRequest.processThreadPage(document, SomePreferences.hideAllImages, SomePreferences.hidePreviouslyReadPosts);
    }
}
