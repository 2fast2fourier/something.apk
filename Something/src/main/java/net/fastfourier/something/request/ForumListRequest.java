package net.fastfourier.something.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import net.fastfourier.something.data.ForumProcessTask;

import org.jsoup.nodes.Document;

/**
 * Created by matthewshepard on 1/30/14.
 */
public class ForumListRequest extends HTMLRequest<Void> {
    public ForumListRequest(Response.Listener<Void> success, Response.ErrorListener error) {
        super("http://forums.somethingawful.com/forumdisplay.php", Request.Method.GET, success, error);
        //request GBS since that forumid will probably never change
        addParam("forumid", 1);
    }

    @Override
    public Void parseHtmlResponse(NetworkResponse response, Document document) throws Exception {
        ForumProcessTask.processForums(document);
        return null;
    }
}
