package net.fastfourier.something.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import com.salvadordalvik.fastlibrary.request.PersistentCookieStore;
import net.fastfourier.something.R;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by matthewshepard on 1/17/14.
 */
public class SomePreferences {
    private static String TAG = "SomePreferences";
    /*
     * Define Preference key/cache pairs here:
     */
    //public static final String EXAMPLE_VARIABLE_NAME = "internal_variable_name";
    //public static int exampleVariable;

    private static PersistentCookieStore cookieStore;
    private static CookieManager cookieManager;

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
    public static String byobTheme = "byob";

    public static boolean loggedIn;

    public static boolean hidePreviouslyReadPosts = true;

    public static final String IMAGES_ENABLED_BOOL = "images_enabled";
    public static final String IMAGES_WIFI_BOOL = "images_wifi";
    public static boolean imagesEnabled = true;
    public static boolean imagesWifi = false;

    public static final String AVATARS_ENABLED_BOOL = "avatars_enabled";
    public static final String AVATARS_WIFI_BOOL = "avatars_wifi";
    public static boolean avatarsEnabled = true;
    public static boolean avatarsWifi = false;


    public static final String LAST_FORUM_UPDATE_LONG = "last_forum_update";
    public static long lastForumUpdate;

    public static final String POST_PER_PAGE_INT = "post_per_page";
    public static int threadPostPerPage;

    public static final String FONT_SIZE_STRING = "font_size_scaled_em";
    public static String fontSize;

    private synchronized static void updatePreferences(SharedPreferences newPrefs){
        //Update cached preferences here:
        //exampleVariable = newPrefs.getInt(EXAMPLE_VARIABLE_NAME, 0);

        favoriteForumId = newPrefs.getInt(THREADLIST_FAVORITE_FORUMID, DEFAULT_FAVORITE_FORUMID_INT);

        loggedIn = isLoggedIn();

        selectedTheme = newPrefs.getString(PRIMARY_THEME_STRING, "default");
        selectedSysTheme = newPrefs.getString(SYSTEM_THEME_STRING, "light");

        systemTheme = getSystemTheme(selectedSysTheme);

        threadPostPerPage = newPrefs.getInt(POST_PER_PAGE_INT, 40);

        fontSize = newPrefs.getString(FONT_SIZE_STRING, "1em");

        lastForumUpdate = newPrefs.getLong(LAST_FORUM_UPDATE_LONG, 0);

        imagesEnabled = newPrefs.getBoolean(IMAGES_ENABLED_BOOL, true);
        imagesWifi = newPrefs.getBoolean(IMAGES_WIFI_BOOL, false);
        avatarsEnabled = newPrefs.getBoolean(AVATARS_ENABLED_BOOL, true);
        avatarsWifi = newPrefs.getBoolean(AVATARS_WIFI_BOOL, false);
    }

    public synchronized static boolean shouldShowImages(Context context){
        if(imagesEnabled){
            return true;
        }else if(imagesWifi && context != null){
            ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return wifi == null || wifi.isConnected();
        }
        return false;
    }

    public synchronized static boolean shouldShowAvatars(Context context){
        if(avatarsEnabled){
            return true;
        }else if(avatarsWifi && context != null){
            ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            return wifi == null || wifi.isConnected();
        }
        return false;
    }


    private static SharedPreferences preferenceStore;

    public synchronized static void init(Context context){
        cookieStore = new PersistentCookieStore(context.getApplicationContext());
        cookieManager = new CookieManager(cookieStore, CookiePolicy.ACCEPT_ALL);
        CookieManager.setDefault(cookieManager);

        preferenceStore = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        updatePreferences(preferenceStore);
        preferenceStore.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                updatePreferences(sharedPreferences);
            }
        });
    }

    public synchronized static String getPreference(String key, String defaultValue) {
        return preferenceStore.getString(key, defaultValue);
    }

    public synchronized static void setString(String key, String value){
        preferenceStore.edit().putString(key, value).apply();
        updatePreferences(preferenceStore);
    }

    public synchronized static void setInt(String key, int value){
        preferenceStore.edit().putInt(key, value).apply();
        updatePreferences(preferenceStore);
    }

    public synchronized static void setLong(String key, long value){
        preferenceStore.edit().putLong(key, value).apply();
        updatePreferences(preferenceStore);
    }

    public synchronized static void setBoolean(String key, boolean value){
        preferenceStore.edit().putBoolean(key, value).apply();
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
        if("byob".equalsIgnoreCase(sysTheme)){
            return R.style.Something_BYOB;
        }
        return R.style.Something_Light;
    }

    public static void clearAuthentication() {
        Log.i(TAG, "Logging out");
        loggedIn = false;
        cookieStore.removeAll();
    }

    private static boolean isLoggedIn(){
        try {
            Map<String, List<String>> cookies = CookieManager.getDefault().get(URI.create(Constants.BASE_URL), new HashMap<String, List<String>>());
            List<String> cookieList = cookies.get("Cookie");
            if (cookieList != null){
                for (String cookie : cookieList) {
                    if(cookie.contains(Constants.COOKIE_USER_ID) && !cookie.contains("deleted")){
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean confirmLogin() {
        loggedIn = isLoggedIn();
        return loggedIn;
    }

    public static String getCookie(String name) {
        List<HttpCookie> cookies = cookieManager.getCookieStore().getCookies();
        for (HttpCookie cookie : cookies) {
            if (cookie.getDomain().contains(Constants.COOKIE_DOMAIN)) {
                if (cookie.getName().equals(name)) {
                    return String.format("%s=%s; domain=%s", name, cookie.getValue(), cookie.getDomain());
                }
            }
        }
        Log.w(TAG, "getCookie: could not find cookie " + name);
        return "";
    }
}
