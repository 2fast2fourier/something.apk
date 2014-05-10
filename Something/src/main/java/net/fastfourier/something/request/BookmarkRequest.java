package net.fastfourier.something.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;

import org.jsoup.nodes.Document;

/**
 * Created by matthewshepard on 1/31/14.
 */
public class BookmarkRequest extends HTMLRequest<Boolean> {
    private boolean add;

    public BookmarkRequest(int threadId, boolean add, Response.Listener<Boolean> success, Response.ErrorListener error) {
        super("http://forums.somethingawful.com/bookmarkthreads.php", Request.Method.POST, success, error);
        this.add = add;
        addParam("threadid", threadId);
        addParam("action", add ? "add" : "remove");
    }

    @Override
    public Boolean parseHtmlResponse(Request<Boolean> request, NetworkResponse response, Document document) throws Exception {
        return add;
    }
}
