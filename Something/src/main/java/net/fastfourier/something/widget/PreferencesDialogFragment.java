package net.fastfourier.something.widget;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.salvadordalvik.fastlibrary.FastDialogFragment;

import net.fastfourier.something.R;
import net.fastfourier.something.util.SomePreferences;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by matthewshepard on 2/1/14.
 */
public class PreferencesDialogFragment extends FastDialogFragment implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    private String[] themes, systemThemes, friendlyThemeNames;
    private int[] themeColors;
    private GridLayout primaryThemes;
    private TextView themeTitle;
    private boolean restartRequired = false;
    private int[] perPageOptions;

    public PreferencesDialogFragment() {
        super(R.layout.preference_dialog, R.string.preference_dialog_title);
    }

    private void loadThemeList(){
        try {
            String[] themeList = getResources().getAssets().list("css");
            themes = new String[themeList.length];
            systemThemes = new String[themeList.length];
            friendlyThemeNames = new String[themeList.length];
            themeColors = new int[themeList.length];
            for(int ix=0;ix<themeList.length;ix++){
                themes[ix] = themeList[ix].replace(".css", "");
                loadThemeIconColor(themeList[ix], ix);
                Log.e("theme", themeList[ix] +" - "+friendlyThemeNames[ix]+" - SysTheme: "+systemThemes[ix]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final static Pattern cssHeaderPattern = Pattern.compile("/\\*\\s*name:\\s*([\\w\\s]*)\\s*icon:\\s*([#0-9a-fA-F]+)\\s*systheme:\\s*([\\w\\s]*)\\*/");

    private void loadThemeIconColor(String themeName, int position){
        try {
            InputStreamReader in = new InputStreamReader(getResources().getAssets().open("css/"+themeName));
            String cssFile = CharStreams.toString(in);
            Matcher match = cssHeaderPattern.matcher(cssFile);
            if(match.find()){
                friendlyThemeNames[position] = match.group(1).trim();
                themeColors[position] = Color.parseColor(match.group(2).trim());
                systemThemes[position] = match.group(3).trim();
            }
            Closeables.close(in, true);
        } catch (IOException e) {
            e.printStackTrace();
            friendlyThemeNames[position] = "Unknown";
            themeColors[position] = Color.WHITE;
        }
    }

    @Override
    public void viewCreated(View frag, Bundle savedInstanceState) {
        loadThemeList();

        themeTitle = (TextView) frag.findViewById(R.id.preferences_theme);

        primaryThemes = (GridLayout) frag.findViewById(R.id.preference_theme_container);
        for(int ix=0;ix<themes.length;ix++){
            ImageView theme = new ImageView(getActivity());
            GradientDrawable selectedColor = (GradientDrawable) getResources().getDrawable(R.drawable.preference_theme_icon_checked);
            selectedColor.mutate();
            selectedColor.setColor(themeColors[ix]);
            theme.setImageDrawable(selectedColor);
            theme.setTag(ix);
            theme.setOnClickListener(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.setMargins(16, 16, 16, 16);
            primaryThemes.addView(theme, params);
        }

        updateThemeIcons();

        frag.findViewById(R.id.preference_apply).setOnClickListener(this);

        int currentSelected = 0;
        perPageOptions = getResources().getIntArray(R.array.post_per_page_items);
        String[] perPageStrings = new String[perPageOptions.length];
        for(int ix=0;ix<perPageOptions.length;ix++){
            perPageStrings[ix] = Integer.toString(perPageOptions[ix]);
            if(SomePreferences.threadPostPerPage == perPageOptions[ix]){
                currentSelected = ix;
            }
        }

        RadioGroup images = (RadioGroup) frag.findViewById(R.id.preferences_image_group);
        if(SomePreferences.imagesEnabled){
            RadioButton image = (RadioButton) frag.findViewById(R.id.preference_image_enabled);
            image.setChecked(true);
        }else if(SomePreferences.imagesWifi){
            RadioButton image = (RadioButton) frag.findViewById(R.id.preference_image_wifi);
            image.setChecked(true);
        }else{
            RadioButton image = (RadioButton) frag.findViewById(R.id.preference_image_disabled);
            image.setChecked(true);
        }
        images.setOnCheckedChangeListener(this);

        RadioGroup avatars = (RadioGroup) frag.findViewById(R.id.preferences_avatar_group);
        if(SomePreferences.avatarsEnabled){
            RadioButton image = (RadioButton) frag.findViewById(R.id.preference_avatar_enabled);
            image.setChecked(true);
        }else if(SomePreferences.avatarsWifi){
            RadioButton image = (RadioButton) frag.findViewById(R.id.preference_avatar_wifi);
            image.setChecked(true);
        }else{
            RadioButton image = (RadioButton) frag.findViewById(R.id.preference_avatar_disabled);
            image.setChecked(true);
        }
        avatars.setOnCheckedChangeListener(this);

        Spinner perPage = (Spinner) frag.findViewById(R.id.preferences_postperpage_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, perPageStrings);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        perPage.setAdapter(adapter);
        perPage.setSelection(currentSelected, false);
        perPage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SomePreferences.setInt(SomePreferences.POST_PER_PAGE_INT, perPageOptions[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                SomePreferences.setInt(SomePreferences.POST_PER_PAGE_INT, 40);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.preference_apply){
            dismiss();
        }else{
            int theme = (Integer) v.getTag();
            selectTheme(themes[theme], systemThemes[theme]);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if(restartRequired){
            getActivity().recreate();
        }
    }

    @Override
    public void refreshData(boolean pullToRefresh) {}

    private void selectTheme(String theme, String systemTheme){
        restartRequired = !SomePreferences.selectedTheme.equalsIgnoreCase(theme);
        SomePreferences.setTheme(theme, systemTheme);
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
                    themeTitle.setText("Theme: "+friendlyThemeNames[themeId]);
                }else{
                    GradientDrawable selectedColor = (GradientDrawable) getResources().getDrawable(R.drawable.preference_theme_icon);
                    selectedColor.mutate();
                    selectedColor.setColor(themeColors[themeId]);
                    theme.setImageDrawable(selectedColor);
                }
            }
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId){
            case R.id.preference_image_enabled:
                SomePreferences.setBoolean(SomePreferences.IMAGES_ENABLED_BOOL, true);
                SomePreferences.setBoolean(SomePreferences.IMAGES_WIFI_BOOL, false);
                break;
            case R.id.preference_image_wifi:
                SomePreferences.setBoolean(SomePreferences.IMAGES_ENABLED_BOOL, false);
                SomePreferences.setBoolean(SomePreferences.IMAGES_WIFI_BOOL, true);
                break;
            case R.id.preference_image_disabled:
                SomePreferences.setBoolean(SomePreferences.IMAGES_ENABLED_BOOL, false);
                SomePreferences.setBoolean(SomePreferences.IMAGES_WIFI_BOOL, false);
                break;
            case R.id.preference_avatar_enabled:
                SomePreferences.setBoolean(SomePreferences.AVATARS_ENABLED_BOOL, true);
                SomePreferences.setBoolean(SomePreferences.AVATARS_WIFI_BOOL, false);
                break;
            case R.id.preference_avatar_wifi:
                SomePreferences.setBoolean(SomePreferences.AVATARS_ENABLED_BOOL, false);
                SomePreferences.setBoolean(SomePreferences.AVATARS_WIFI_BOOL, true);
                break;
            case R.id.preference_avatar_disabled:
                SomePreferences.setBoolean(SomePreferences.AVATARS_ENABLED_BOOL, false);
                SomePreferences.setBoolean(SomePreferences.AVATARS_WIFI_BOOL, false);
                break;
        }
    }
}
