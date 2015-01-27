package net.fastfourier.something.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.TypedValue;

import net.fastfourier.something.R;

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
                case Constants.FYAD_DUMP_FORUMID:
                    return actionbar_fyad;
            }
        }
        return fallbackColor;
    }

    public static void init(Context context){
        actionbar_yospos = getThemeColor(context, R.style.Something_YOSPOS, R.attr.statusBarBackground, Color.BLACK);
        actionbar_fyad = getThemeColor(context, R.style.Something_FYAD, R.attr.statusBarBackground, Color.BLACK);
        bookmark_unread = context.getResources().getColor(R.color.readcount_unbookmarked);
        bookmark_normal = context.getResources().getColor(R.color.readcount_normal);
        bookmark_red = context.getResources().getColor(R.color.readcount_red);
        bookmark_gold = context.getResources().getColor(R.color.readcount_gold);
    }

    public static int getThemeColor(Context context, int colorAttr, int fallbackColor){
        TypedValue val = new TypedValue();
        if(context != null && context.getTheme() != null && context.getTheme().resolveAttribute(colorAttr, val, true)){
            return val.data;
        }
        return fallbackColor;
    };

    public static int getThemeResource(Context context, int resourceAttr, int fallbackRes){
        TypedValue val = new TypedValue();
        if(context != null && context.getTheme() != null && context.getTheme().resolveAttribute(resourceAttr, val, false)){
            return val.data;
        }
        return fallbackRes;
    };

    public static int getThemeColor(Context context, int styleRes, int resourceAttr, int fallbackColor){
        TypedArray array;
        if(context != null && context.getTheme() != null){
            array = context.getTheme().obtainStyledAttributes(styleRes, new int[]{resourceAttr});
            if(array != null){
                int color = array.getColor(0, fallbackColor);
                array.recycle();
                return color;
            }
        }
        return fallbackColor;
    };
}
