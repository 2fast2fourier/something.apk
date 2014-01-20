package com.salvadordalvik.something.request;

import android.text.TextUtils;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.salvadordalvik.fastlibrary.util.FastUtils;
import com.salvadordalvik.something.data.PostItem;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

/**
 * Created by matthewshepard on 1/19/14.
 */
public class ThreadPageRequest extends HTMLRequest<ThreadPageRequest.ThreadPage> {

    public ThreadPageRequest(int threadId, int page, Response.Listener<ThreadPage> success, Response.ErrorListener error) {
        super("http://forums.somethingawful.com/showthread.php", Request.Method.GET, success, error);
        addParam("threadid", threadId);
        if(page > 0){
            addParam("pagenumber", page);
        }else{
            addParam("goto", "newpost");
        }
    }

    @Override
    public ThreadPage parseHtmlResponse(NetworkResponse response, Document document) throws Exception {
        ArrayList<PostItem> posts = new ArrayList<PostItem>();

        parsePosts(document, posts);

        int currentPage, maxPage = 1, threadId, forumId;

        Element pages = document.getElementsByClass("pages").first();
        currentPage = FastUtils.safeParseInt(pages.getElementsByAttribute("selected").attr("value"), 1);
        Element lastPage = pages.getElementsByTag("option").last();
        if(lastPage != null){
            maxPage = FastUtils.safeParseInt(lastPage.attr("value"), 1);
        }

        String threadTitle = document.getElementsByClass("bclast").text();

        Element body = document.getElementsByTag("body").first();
        forumId = Integer.parseInt(body.attr("data-forum"));
        threadId = Integer.parseInt(body.attr("data-thread"));

        return new ThreadPage(posts, currentPage, maxPage, threadId, forumId, threadTitle);
    }

    public static class ThreadPage{
        public ArrayList<PostItem> posts;
        public int page, maxPage, threadId, forumId;
        public String threadTitle;

        private ThreadPage(ArrayList<PostItem> posts, int page, int maxPage, int threadId, int forumId, String threadTitle){
            this.posts = posts;
            this.page = page;
            this.maxPage = maxPage;
            this.threadId = threadId;
            this.forumId = forumId;
            this.threadTitle = threadTitle;
        }
    }

    private static void parsePosts(Document doc, ArrayList<PostItem> postArray){
        Elements posts = doc.getElementsByClass("post");
        for(Element post : posts){
            String rawId = post.id().replaceAll("\\D", "");
            if(!TextUtils.isEmpty(rawId)){
                int id = Integer.parseInt(rawId);
                String author = post.getElementsByClass("author").text();
                Element title = post.getElementsByClass("title").first();
                String avTitle = title.text();
                String avatar = title.getElementsByTag("img").attr("src");
                String postContent = post.getElementsByClass("postbody").html();
                String postDate = post.getElementsByClass("postdate").text();

                postArray.add(new PostItem(id, author, avTitle, avatar, postContent, postDate));
            }
        }
    }
}
