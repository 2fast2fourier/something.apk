package net.fastfourier.something.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.bugsense.trace.BugSenseHandler;
import net.fastfourier.something.list.PrivateMessageFolderItem;
import net.fastfourier.something.list.PrivateMessageItem;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by matthewshepard on 2/7/14.
 */
public class PrivateMessageListRequest extends HTMLRequest<PrivateMessageListRequest.PMListResult> {
    private static final Pattern pmIdPattern = Pattern.compile("privatemessageid=(\\d+)");

    private int folderId;

    public PrivateMessageListRequest(int folderId, boolean showAll, Response.Listener<PMListResult> success, Response.ErrorListener error) {
        super("http://forums.somethingawful.com/private.php", Request.Method.GET, success, error);
        this.folderId = folderId;
        addParam("folderid", folderId);
        if(showAll){
            addParam("showall", "1");
        }
    }

    @Override
    public PMListResult parseHtmlResponse(NetworkResponse response, Document document) throws Exception {
        ArrayList<PrivateMessageFolderItem> pmFolders = new ArrayList<PrivateMessageFolderItem>();
        ArrayList<PrivateMessageItem> pm = new ArrayList<PrivateMessageItem>();
        Elements messages = document.getElementsByTag("tbody").first().getElementsByTag("tr");
        for(Element message : messages){
            try{
                Element name = message.getElementsByClass("title").first();
                if(name != null){
                    Matcher idMatch = pmIdPattern.matcher(name.getElementsByTag("a").attr("href"));
                    if(idMatch.find()){
                        int id = Integer.parseInt(idMatch.group(1));
                        String author = message.getElementsByClass("sender").text();
                        String title = message.getElementsByClass("title").text();
                        String date = message.getElementsByClass("date").text();

                        int status = PrivateMessageItem.STATUS_NORMAL;
                        Element statusDiv = message.getElementsByClass("status").first();
                        if(statusDiv != null){
                            String statusImg = statusDiv.getElementsByTag("img").attr("src");
                            if(statusImg.contains("pmreplied")){
                                status = PrivateMessageItem.STATUS_REPLIED;
                            }else if(statusImg.contains("newpm")){
                                status = PrivateMessageItem.STATUS_NEW;
                            }else if(statusImg.contains("pmforwarded")){
                                status = PrivateMessageItem.STATUS_FORWARDED;
                            }else{
                                status = PrivateMessageItem.STATUS_NORMAL;
                            }
                        }

                        pm.add(new PrivateMessageItem(id, title, author, date, status, folderId));
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                BugSenseHandler.sendException(e);
            }
        }

        Elements folders = document.getElementsByClass("folder").first().getElementsByTag("option");
        for(Element folder : folders){
            pmFolders.add(new PrivateMessageFolderItem(Integer.parseInt(folder.attr("value")), folder.text(), folder.hasAttr("selected")));
        }

        return new PMListResult(pm, pmFolders);
    }

    public static class PMListResult{
        public final ArrayList<PrivateMessageItem> messages;
        public final ArrayList<PrivateMessageFolderItem> folders;

        private PMListResult(ArrayList<PrivateMessageItem> pm, ArrayList<PrivateMessageFolderItem> pmFolders) {
            this.messages = pm;
            this.folders = pmFolders;
        }
    }
}
