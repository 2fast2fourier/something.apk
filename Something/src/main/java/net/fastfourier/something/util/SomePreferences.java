package net.fastfourier.something.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import net.fastfourier.something.R;
import net.fastfourier.something.SomeApplication;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by matthewshepard on 1/17/14.
 */
public class SomePreferences {
    /*
     * Define Preference key/cache pairs here:
     */
    //public static final String EXAMPLE_VARIABLE_NAME = "internal_variable_name";
    //public static int exampleVariable;

    public static final String THREADLIST_FAVORITE_FORUMID = "threadlist_favorite_forumid";
    private static final int DEFAULT_FAVORITE_FORUMID_INT = Constants.BOOKMARK_FORUMID;
    public static int favoriteForumId;

    //TODO theme stuff
    private static final String PRIMARY_THEME_STRING = "primary_theme";
    private static final String SYSTEM_THEME_STRING = "system_theme";
    public static String selectedTheme;
    private static String selectedSysTheme;
    public static int systemTheme;
    public static boolean forceTheme = false;
    public static String yosTheme = "yospos";
    public static String fyadTheme = "fyad";

    public static boolean loggedIn;

    public static final String HIDE_PREVIOUS_POSTS_BOOL = "hide_previous_posts";
    public static boolean hidePreviouslyReadPosts = true;

    public static final String HIDE_ALL_IMAGES_BOOL = "hide_all_images";
    public static boolean hideAllImages = false;


    public static final String LAST_FORUM_UPDATE_LONG = "last_forum_update";
    public static long lastForumUpdate;

    //TODO postperpage
    public static int threadPostPerPage = 40;

    private synchronized static void updatePreferences(SharedPreferences newPrefs){
        //Update cached preferences here:
        //exampleVariable = newPrefs.getInt(EXAMPLE_VARIABLE_NAME, 0);

        favoriteForumId = newPrefs.getInt(THREADLIST_FAVORITE_FORUMID, DEFAULT_FAVORITE_FORUMID_INT);

        loggedIn = isLoggedIn();

        selectedTheme = newPrefs.getString(PRIMARY_THEME_STRING, "default");
        selectedSysTheme = newPrefs.getString(SYSTEM_THEME_STRING, "light");

        systemTheme = getSystemTheme(selectedSysTheme);

        lastForumUpdate = newPrefs.getLong(LAST_FORUM_UPDATE_LONG, 0);
    }


    private static SharedPreferences preferenceStore;

    public synchronized static void init(Context context){
        preferenceStore = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        preferenceStore.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                updatePreferences(sharedPreferences);
            }
        });
        updatePreferences(preferenceStore);
    }

    public synchronized static void setString(String key, String value){
        preferenceStore.edit().putString(key, value).commit();
        updatePreferences(preferenceStore);
    }

    public synchronized static void setInt(String key, int value){
        preferenceStore.edit().putInt(key, value).commit();
        updatePreferences(preferenceStore);
    }

    public synchronized static void setLong(String key, long value){
        preferenceStore.edit().putLong(key, value).commit();
        updatePreferences(preferenceStore);
    }

    public synchronized static void setBoolean(String key, boolean value){
        preferenceStore.edit().putBoolean(key, value).commit();
        updatePreferences(preferenceStore);
    }

    public static void setTheme(String theme, String systemTheme) {
        setString(PRIMARY_THEME_STRING, theme);
        setString(SYSTEM_THEME_STRING, systemTheme);
    }

    private static int getSystemTheme(String sysTheme){
        if("light".equalsIgnoreCase(sysTheme)){
            return R.style.Something_Light;
        }
        if("dark".equalsIgnoreCase(sysTheme)){
            return R.style.Something_Dark;
        }
        if("yospos".equalsIgnoreCase(sysTheme)){
            return R.style.Something_YOSPOS;
        }
        if("amberpos".equalsIgnoreCase(sysTheme)){
            return R.style.Something_AmberPOS;
        }
        if("fyad".equalsIgnoreCase(sysTheme)){
            return R.style.Something_FYAD;
        }
        return R.style.Something_Light;
    }

    public static void clearAuthentication() {
        loggedIn = false;
        SomeApplication.clearCookies();
    }

    private static boolean isLoggedIn(){
        try {
            Map<String, List<String>> cookies = CookieManager.getDefault().get(URI.create("https://forums.somethingawful.com"), new HashMap<String, List<String>>());
            List<String> cookieList = cookies.get("Cookie");
            if(cookieList != null){
                for(String cookie : cookieList){
                    if(cookie.contains("bbuserid")){
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        };
        return false;
    }

    public static boolean confirmLogin() {
        loggedIn = isLoggedIn();
        return loggedIn;
    }
}
