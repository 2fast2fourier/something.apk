package com.salvadordalvik.something.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by matthewshepard on 1/17/14.
 */
public class SomePreferences {
    /*
     * Define Preference key/cache pairs here:
     */
    //public static final String EXAMPLE_VARIABLE_NAME = "internal_variable_name";
    //public static int exampleVariable;

    private synchronized static void updatePreferences(SharedPreferences newPrefs){
        //Update cached preferences here:
        //exampleVariable = newPrefs.getInt(EXAMPLE_VARIABLE_NAME, 0);

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
