package net.fastfourier.something.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import net.fastfourier.something.util.MustCache;
import net.fastfourier.something.util.SomePreferences;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.HashMap;

/**
 * Created by matthewshepard on 2/12/14.
 */
public class PrivateMessageRequest extends HTMLRequest<PrivateMessageRequest.PMData> {
    private String pmTitle;

    public PrivateMessageRequest(int pmId, String pmTitle, Response.Listener<PMData> success, Response.ErrorListener error) {
        super("http://forums.somethingawful.com/private.php", Request.Method.GET, success, error);
        addParam("action", "show");
        addParam("privatemessageid", pmId);
        this.pmTitle = pmTitle;
    }

    @Override
    public PMData parseHtmlResponse(NetworkResponse response, Document document) throws Exception {

        StringBuilder pmHtml = new StringBuilder();
        parsePM(document, pmHtml, pmTitle);

        return new PMData(pmHtml.toString());
    }

    public static class PMData{
        public final String htmlData;

        public PMData(String pmHtml) {
            htmlData = pmHtml;
        }
    }

    private static void parsePM(Document doc, StringBuilder html, String pmTitle){
        Element post = doc.getElementsByClass("post").first();
        if(post != null){
            HashMap<String, String> postData = new HashMap<String, String>();
            String author = post.getElementsByClass("author").text();
            Element title = post.getElementsByClass("title").first();
            String avTitle = title.text();
            String avatarUrl = title.getElementsByTag("img").attr("src");
            String postContent = post.getElementsByClass("postbody").html();
            String postDate = post.getElementsByClass("postdate").text().replaceAll("[#?]", "").trim();

            postData.put("username", author);
            postData.put("avatarText", avTitle);
            postData.put("avatarURL", avatarUrl);
            postData.put("postcontent", postContent);
            postData.put("postDate", postDate);
            //Passing title through from fragment because parsing it out of the breadcrumb is a pita
            postData.put("pmtitle", pmTitle);

            //TODO parse/fill out
            postData.put("mod", null);
            postData.put("admin", null);

            HashMap<String, String> headerArgs = new HashMap<String, String>();
            headerArgs.put("theme", SomePreferences.selectedTheme);
            headerArgs.put("jumpToPostId", "0");
            headerArgs.put("fontSize", Integer.toString(SomePreferences.fontSize));
            headerArgs.put("previouslyRead", null);
            MustCache.applyHeaderTemplate(html, headerArgs);

            MustCache.applyPMTemplate(html, postData);

            MustCache.applyFooterTemplate(html, null);
        }else{
            throw new RuntimeException("Reply data not found!");
        }
    }
}
