package com.salvadordalvik.something.request;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.salvadordalvik.something.data.ForumProcessTask;
import com.salvadordalvik.something.list.ThreadItem;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

/**
 * Created by matthewshepard on 1/17/14.
 */
public class ThreadListRequest extends HTMLRequest<ThreadListRequest.ThreadListResponse> {

    public ThreadListRequest(int forumId, Response.Listener<ThreadListResponse> success, Response.ErrorListener error) {
        super("http://forums.somethingawful.com/forumdisplay.php?=219", Request.Method.GET, success, error);
        addParam("forumid", forumId);
    }

    @Override
    public ThreadListResponse parseHtmlResponse(NetworkResponse response, Document document) throws Exception {
        ArrayList<ThreadItem> threads = new ArrayList<ThreadItem>();
        Element threadList = document.getElementById("forum");
        for(Element thread : threadList.getElementsByClass("thread")){
            int id;
            try{
                id=Integer.parseInt(thread.id().replaceAll("\\D", ""));
            }catch (NumberFormatException nfe){
                Log.e("ThreadListRequest", "Could not parse thread ID!");
                continue;
            }

            int unread = -1, replies;

            Element seen = thread.getElementsByClass("lastseen").first();
            if(seen != null){
                Element count = seen.getElementsByClass("count").first();
                if(count != null){
                    unread = stripParseInt(count.text());
                }else{
                    unread = 0;
                }
            }

            replies = stripParseInt(thread.getElementsByClass("replies").first().text());

            Elements authors = thread.getElementsByClass("author");

            String author, lastPost = null;
            author = authors.first().text();
            if(authors.size() > 1){
                lastPost = authors.last().text();
            }

            threads.add(new ThreadItem(id, getFirstTextByClass(thread, "thread_title", "Thread Title"), unread, replies, author, lastPost));
        }
        ForumProcessTask.execute(document);
        return new ThreadListResponse(threads);
    }

    public static class ThreadListResponse{
        public ArrayList<ThreadItem> threads;
        public ThreadListResponse(ArrayList<ThreadItem> threads){
            this.threads = threads;
        }
    }
}
