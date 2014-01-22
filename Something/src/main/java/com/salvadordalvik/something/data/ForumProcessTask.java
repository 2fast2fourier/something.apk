package com.salvadordalvik.something.data;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by matthewshepard on 1/21/14.
 */
public class ForumProcessTask implements Runnable {
    private static Pattern forumNameParser = Pattern.compile("(-*)\\s*([^-]+)");

    private Document page;

    private ForumProcessTask(Document page){
        this.page = page;
    }

    @Override
    public void run() {
        ArrayList<ContentValues> forumList = new ArrayList<ContentValues>();
        String categoryName = "UNKNOWN";
        Element forumSelect = page.getElementsByAttributeValue("name", "forumid").first();
        if(forumSelect == null){
            Log.e("ForumProcess", "Could not find forum selector.");
            return;
        }
        Elements forums = forumSelect.getElementsByTag("option");
        for(Element forum : forums){
            int forumId = parseForumId(forum);
            String rawName = forum.text().trim();
            if(forumId > 0 && rawName.length() > 0){
                Matcher findForum = forumNameParser.matcher(rawName);
                if(findForum.find()){
                    String indent = findForum.group(1).trim();
                    String forumName = findForum.group(2).trim();
                    if(indent.length() > 0){
                        ContentValues cv = new ContentValues();
                        cv.put("forum_id", forumId);
                        cv.put("forum_name", forumName);
                        cv.put("category", categoryName);
                        //TODO parent forums
                        cv.put("parent_forum_id", 0);

                        forumList.add(cv);
                    }else{
                        categoryName = findForum.group(2);
                    }
                }
            }
        }
        if(forumList.size() > 0){
            SomeDatabase db = SomeDatabase.getDatabase();
            db.deleteRows(SomeDatabase.TABLE_FORUM, null);
            db.insertRows(SomeDatabase.TABLE_FORUM, SQLiteDatabase.CONFLICT_REPLACE, forumList);
        }
    }

    public static void execute(Document document){
        new Thread(new ForumProcessTask(document)).start();
    }

    public static int parseForumId(Element forum){
        String rawId = forum.attr("value").replaceAll("[^\\-\\d]", "");
        if(rawId.length() > 0){
            return Integer.parseInt(rawId);
        }
        return 0;
    }
}
