package net.fastfourier.something.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;

import net.fastfourier.something.ReplyFragment;
import net.fastfourier.something.util.Constants;
import net.fastfourier.something.util.MustCache;
import net.fastfourier.something.util.SomePreferences;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.HashMap;

/**
 * Created by Timothy on 2014/06/28.
 */
public class PreviewRequest extends HTMLRequest<PreviewRequest.PreviewData> {
    private String previewTitle = "Preview";

    public PreviewRequest(ReplyDataRequest.ReplyDataResponse reply, Response.Listener<PreviewData> success, Response.ErrorListener error) {
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
        // Add params for preview
        addParam("submit", "Submit Reply");
        addParam("preview", "Preview Reply");
    }

    @Override
    public PreviewData parseHtmlResponse(Request<PreviewData> request, NetworkResponse response, Document document) throws Exception {
        StringBuilder previewHtml = new StringBuilder();
        parsePreview(document, previewHtml);
        return new PreviewData(previewHtml.toString());
    }

    public static class PreviewData{
        public final String htmlData;

        public PreviewData(String previewHtml) {
            htmlData = previewHtml;
        }
    }

    private static void parsePreview(Document doc, StringBuilder html) {
        // Get the preview post from the redirect.
        Element post = doc.getElementsByClass("postbody").first();
        if(post != null)
        {
            HashMap<String, String> postData = new HashMap<String, String>();
            String postContent = post.html();
            postData.put("theme", SomePreferences.selectedTheme);
            postData.put("postcontent", postContent);
            MustCache.applyPreviewTemplate(html, postData);
        }
        else{
            throw new RuntimeException("Preview data not found!");
        }
    }
}
