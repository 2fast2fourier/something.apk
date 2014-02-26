package com.salvadordalvik.something.widget;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.salvadordalvik.fastlibrary.FastDialogFragment;
import com.salvadordalvik.something.R;
import com.salvadordalvik.something.util.SomePreferences;

/**
 * Created by matthewshepard on 2/1/14.
 */
public class PreferencesDialogFragment extends FastDialogFragment implements View.OnClickListener {
    private String[] themes = {"default", "dark", "yospos", "amberpos", "fyad"};
    private int[] themeColors = {Color.WHITE, Color.GRAY, Color.rgb(87, 255, 87), Color.rgb(232, 188, 68), Color.rgb(255, 174, 255)};
    private LinearLayout primaryThemes;

    public PreferencesDialogFragment() {
        super(R.layout.preference_dialog, R.string.preference_dialog_title);
    }

    @Override
    public void viewCreated(View frag, Bundle savedInstanceState) {
        primaryThemes = (LinearLayout) frag.findViewById(R.id.preference_theme_container);
        for(int ix=0;ix<themes.length;ix++){
            ImageView theme = new ImageView(getActivity());
            GradientDrawable selectedColor = (GradientDrawable) getResources().getDrawable(R.drawable.preference_theme_icon_checked);
            selectedColor.mutate();
            selectedColor.setColor(themeColors[ix]);
            theme.setImageDrawable(selectedColor);
            theme.setTag(ix);
            theme.setOnClickListener(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(16, 0, 16, 0);
            primaryThemes.addView(theme, params);
        }

        updateThemeIcons();
    }

    @Override
    public void onClick(View v) {
        selectTheme(themes[(Integer) v.getTag()]);
    }

    @Override
    public void refreshData(boolean pullToRefresh) {}

    private void selectTheme(String theme){
        SomePreferences.setTheme(theme);
        updateThemeIcons();
    }

    private void updateThemeIcons() {
        int count = primaryThemes.getChildCount();
        for(int ix=0; ix<count; ix++){
            View child = primaryThemes.getChildAt(ix);
            if(child instanceof ImageView){
                int themeId = (Integer) child.getTag();
                ImageView theme = (ImageView) child;
                if(themes[themeId].equals(SomePreferences.selectedTheme)){
                    GradientDrawable selectedColor = (GradientDrawable) getResources().getDrawable(R.drawable.preference_theme_icon_checked);
                    selectedColor.mutate();
                    selectedColor.setColor(themeColors[themeId]);
                    theme.setImageDrawable(selectedColor);
                }else{
                    GradientDrawable selectedColor = (GradientDrawable) getResources().getDrawable(R.drawable.preference_theme_icon);
                    selectedColor.mutate();
                    selectedColor.setColor(themeColors[themeId]);
                    theme.setImageDrawable(selectedColor);
                }
            }
        }
    }
}
