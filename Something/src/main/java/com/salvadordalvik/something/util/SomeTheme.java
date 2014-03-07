package com.salvadordalvik.something.util;

import android.content.Context;
import android.graphics.Color;

import com.salvadordalvik.something.R;

/**
 * Created by matthewshepard on 3/2/14.
 */
public class SomeTheme {
    public static int bookmark_unread;
    public static int bookmark_normal;
    public static int bookmark_red;
    public static int bookmark_gold;

    public static int actionbar_yospos;
    public static int actionbar_amberpos;
    public static int actionbar_fyad;

    public static int getActionbarColorForForum(int forumId, int fallbackColor) {
        if(!SomePreferences.forceTheme){
            switch (forumId){
                case Constants.YOSPOS_FORUMID:
                    //TODO amberpos
                    return actionbar_yospos;
                case Constants.FYAD_FORUMID:
                    return actionbar_fyad;
            }
        }
        return fallbackColor;
    }

    public static void init(Context context){
        actionbar_yospos = context.getTheme().obtainStyledAttributes(R.style.Something_YOSPOS, new int[]{R.attr.statusBarBackground}).getColor(0, Color.GRAY);
        actionbar_fyad = context.getTheme().obtainStyledAttributes(R.style.Something_FYAD, new int[]{R.attr.statusBarBackground}).getColor(0, Color.GRAY);
        bookmark_unread = context.getResources().getColor(R.color.readcount_unbookmarked);
        bookmark_normal = context.getResources().getColor(R.color.readcount_normal);
        bookmark_red = context.getResources().getColor(R.color.readcount_red);
        bookmark_gold = context.getResources().getColor(R.color.readcount_gold);
    }
}
