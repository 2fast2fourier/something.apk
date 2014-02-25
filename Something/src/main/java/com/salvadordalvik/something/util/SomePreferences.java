package com.salvadordalvik.something.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

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
    private static final int DEFAULT_FAVORITE_FORUMID = Constants.BOOKMARK_FORUMID;
    public static int favoriteForumId;

    //TODO theme stuff
    public static boolean forceTheme = false;
    public static String selectedTheme = "dark";
    public static boolean amberYos = false;

    public static boolean loggedIn;
    public static final String LOGIN_COOKIE_STRING = "login_cookie_string";
    public static String cookieString;


    public static final String LAST_FORUM_UPDATE = "last_forum_update";
    public static long lastForumUpdate;

    //TODO postperpage
    public static int threadPostPerPage = 40;

    private synchronized static void updatePreferences(SharedPreferences newPrefs){
        //Update cached preferences here:
        //exampleVariable = newPrefs.getInt(EXAMPLE_VARIABLE_NAME, 0);

        favoriteForumId = newPrefs.getInt(THREADLIST_FAVORITE_FORUMID, DEFAULT_FAVORITE_FORUMID);

        cookieString = newPrefs.getString(LOGIN_COOKIE_STRING, null);
        loggedIn = !TextUtils.isEmpty(cookieString) && cookieString.contains("bbuserid");
        Log.e("cookie",cookieString);

        lastForumUpdate = newPrefs.getLong(LAST_FORUM_UPDATE, 0);
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
}
