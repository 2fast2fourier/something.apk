package net.fastfourier.something.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;

import org.jsoup.nodes.Document;

/**
 * Created by matthewshepard on 3/8/14.
 */
public class PMReplyDataRequest extends HTMLRequest<PMReplyDataRequest.PMReplyData> {
    public PMReplyDataRequest(int pmId, Response.Listener<PMReplyData> success, Response.ErrorListener error) {
        super("http://forums.somethingawful.com/private.php", Request.Method.GET, success, error);
        addParam("action", "newmessage");
        if(pmId > 0){
            addParam("privatemessageid", pmId);
        }
    }

    @Override
    public PMReplyData parseHtmlResponse(NetworkResponse response, Document document) throws Exception {
        int pmId = 0;
        String id = document.getElementsByAttributeValue("name", "prevmessageid").val();
        if(id != null && id.matches("\\d+")){
            pmId = Integer.parseInt(id);
        }
        String username = document.getElementsByAttributeValue("name", "touser").val();
        String replyContent = document.getElementsByAttributeValue("name", "message").text();
        String title = document.getElementsByAttributeValue("name", "title").val();
        return new PMReplyData(pmId, unencodeHtml(replyContent), username, unencodeHtml(title));
    }

    public static class PMReplyData {
        public final int replyToPMId;
        public final String replyContent;

        public String replyMessage, replyUsername, replyTitle;

        public PMReplyData(int pmId, String replyContent, String username, String title) {
            this.replyToPMId = pmId;
            this.replyContent = replyContent;
            this.replyUsername = username;
            this.replyTitle = title;
        }
    }
}
