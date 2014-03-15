package net.fastfourier.something.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;
import com.salvadordalvik.fastlibrary.util.FastUtils;

import net.fastfourier.something.MainActivity;
import net.fastfourier.something.PrivateMessageListActivity;

/**
 * Created by matthewshepard on 3/14/14.
 */
public class SomeURL {
    public enum TYPE {FORUM, THREAD, POST, PM_FOLDER, PM_MESSAGE, INDEX, EXTERNAL, UNKNOWN}

    private TYPE urlType;
    private int id = 0, page = 0, userId = 0;
    private String url;

    public SomeURL(String url) {
        this.url = url;
        if(url != null && url.length() > 0){
            try{
                Uri parsedUrl = Uri.parse(url);
                Log.e("SomeURL", "URL: "+url+" - "+parsedUrl.getHost()+" + "+parsedUrl.getPath()+" + "+parsedUrl.getLastPathSegment()+" + "+parsedUrl.getQuery()+" + "+parsedUrl.getFragment()+" - "+parsedUrl.getAuthority());
                if(parsedUrl.isRelative() || "forums.somethingawful.com".equalsIgnoreCase(parsedUrl.getHost())){
                    String lastPath = parsedUrl.getLastPathSegment();
                    if("showthread.php".equalsIgnoreCase(lastPath)){
                        String go = parsedUrl.getQueryParameter("goto");
                        if("post".equalsIgnoreCase(go)){
                            id = Integer.parseInt(parsedUrl.getQueryParameter("postid"));
                            urlType = TYPE.POST;
                        }else if("lastpost".equalsIgnoreCase(go)){
                            id = Integer.parseInt(parsedUrl.getQueryParameter("threadid"));
                            urlType = TYPE.THREAD;
                            page = -1;
                        }else if("newpost".equalsIgnoreCase(go)){
                            id = Integer.parseInt(parsedUrl.getQueryParameter("threadid"));
                            urlType = TYPE.THREAD;
                            page = 0;
                        }else{
                            id = Integer.parseInt(parsedUrl.getQueryParameter("threadid"));
                            urlType = TYPE.THREAD;
                            String pageNum = parsedUrl.getQueryParameter("pagenumber");
                            if(pageNum != null && pageNum.matches("\\d+")){
                                page = Integer.parseInt(pageNum);
                            }else{
                                page = 1;
                            }
                        }
                        String user = parsedUrl.getQueryParameter("pagenumber");
                        if(user != null && user.matches("\\d+")){
                            userId = Integer.parseInt(user);
                        }
                    }else if("forumdisplay.php".equalsIgnoreCase(lastPath)){
                        urlType = TYPE.FORUM;
                        id = Integer.parseInt(parsedUrl.getQueryParameter("forumid"));
                        String pageNum = parsedUrl.getQueryParameter("pagenumber");
                        if(pageNum != null && pageNum.matches("\\d+")){
                            page = Integer.parseInt(pageNum);
                        }else{
                            page = 1;
                        }
                    }else if("private.php".equalsIgnoreCase(lastPath)){
                        String folder = parsedUrl.getQueryParameter("folderid");
                        String pmid = parsedUrl.getQueryParameter("privatemessageid");
                        if(folder != null && folder.matches("\\d+")){
                            urlType = TYPE.PM_FOLDER;
                            id = Integer.parseInt(folder);
                        }else if(pmid != null && pmid.matches("\\d+")){
                            urlType = TYPE.PM_MESSAGE;
                            id = Integer.parseInt(pmid);
                        }else{
                            id = Constants.PM_FOLDER_INBOX;
                        }
                    }else if("usercp.php".equalsIgnoreCase(lastPath)){
                        urlType = TYPE.FORUM;
                        id = Constants.BOOKMARK_FORUMID;
                    }else if("index.php".equalsIgnoreCase(lastPath) || parsedUrl.getPath() == null || parsedUrl.getPath().length() < 2){
                        urlType = TYPE.INDEX;
                    }else{
                        urlType = TYPE.EXTERNAL;
                    }
                }else{
                    urlType = TYPE.EXTERNAL;
                }
            }catch (Exception e){
                BugSenseHandler.sendException(e);
                e.printStackTrace();
                urlType = TYPE.UNKNOWN;
            }
        }else{
            urlType = TYPE.UNKNOWN;
        }
    }

    @Override
    public String toString() {
        return url;
    }

    public TYPE getUrlType() {
        return urlType;
    }

    public int getId() {
        return id;
    }

    public int getPage() {
        return page;
    }

    public int getUserId() {
        return userId;
    }

    public String getUrl() {
        return url;
    }

    public static void handleUrl(Activity activity, String url) {
        new SomeURL(url).handleUrl(activity);
    }

    public void handleUrl(Activity activity) {
        switch (urlType){
            case THREAD:
                if(activity instanceof MainActivity){
                    ((MainActivity) activity).showThread(id, page);
                }else if(activity != null){
                    activity.startActivity(new Intent(activity, MainActivity.class).putExtra("thread_id", id).putExtra("thread_page", page));
                }
            case POST:
                if(activity instanceof MainActivity){
                    ((MainActivity) activity).showThread(id);
                }else if(activity != null){
                    activity.startActivity(new Intent(activity, MainActivity.class).putExtra("post_id", id));
                }
            case FORUM:
                if(activity instanceof MainActivity){
                    ((MainActivity) activity).showForum(id);
                }else if(activity != null){
                    activity.startActivity(new Intent(activity, MainActivity.class).putExtra("forum_id", id).putExtra("forum_page", page));
                }
                break;
            case INDEX:
                if(activity instanceof MainActivity){
                    ((MainActivity) activity).showForumList();
                }else if(activity != null){
                    activity.startActivity(new Intent(activity, MainActivity.class).putExtra("show_index", true));
                }
            case PM_FOLDER:
                if(activity instanceof PrivateMessageListActivity){
                    ((PrivateMessageListActivity) activity).showPMFolder(id);
                }else if(activity != null){
                    activity.startActivity(new Intent(activity, PrivateMessageListActivity.class).putExtra("pm_folder", id));
                }
                break;
            case PM_MESSAGE:
                if(activity instanceof PrivateMessageListActivity){
                    ((PrivateMessageListActivity) activity).showPM(id, "Private Message");
                }else if(activity != null){
                    activity.startActivity(new Intent(activity, PrivateMessageListActivity.class).putExtra("pm_id", id));
                }
                break;
            case EXTERNAL:
            case UNKNOWN:
            default:
                FastUtils.startUrlIntent(activity, url);
                break;
        }
    }
}
