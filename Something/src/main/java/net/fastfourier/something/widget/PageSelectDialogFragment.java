package net.fastfourier.something.widget;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.NumberPicker;

import com.salvadordalvik.fastlibrary.FastDialogFragment;

import net.fastfourier.something.R;

/**
 * Created by matthewshepard on 2/1/14.
 */
public class PageSelectDialogFragment extends FastDialogFragment implements View.OnClickListener {
    private int maxPage;

    private NumberPicker picker;

    public static interface PageSelectable{
        public void goToPage(int page);
    }

    public PageSelectDialogFragment() {
        super(R.layout.page_select_dialog, R.string.page_select_title);
    }

    public static PageSelectDialogFragment newInstance(int currentPage, int maxPage, PageSelectable targetFragment){
        PageSelectDialogFragment frag = new PageSelectDialogFragment();
        Bundle args = new Bundle();
        args.putInt("current", currentPage);
        args.putInt("max", maxPage);
        frag.setArguments(args);
        frag.setTargetFragment((Fragment) targetFragment, 0);
        return frag;
    };

    @Override
    public void viewCreated(View frag, Bundle savedInstanceState) {
        Bundle args = getArguments();
        int current = args.getInt("current");
        maxPage = args.getInt("max");

        picker = (NumberPicker) frag.findViewById(R.id.page_number_picker);
        picker.setMinValue(1);
        picker.setMaxValue(maxPage);
        picker.setValue(current);
        picker.setWrapSelectorWheel(false);
        frag.findViewById(R.id.page_select_button).setOnClickListener(this);
        frag.findViewById(R.id.page_select_first).setOnClickListener(this);
        frag.findViewById(R.id.page_select_last).setOnClickListener(this);
    }

    private void selectPage(int page) {
        Fragment fragment = getTargetFragment();
        if(fragment instanceof PageSelectable){
            ((PageSelectable) fragment).goToPage(page);
        }else{
            throw new RuntimeException("PageSelectDialogFragment has no Target PageSelectable Fragment!");
        }
        dismiss();
    }

    @Override
    public void refreshData(boolean pullToRefresh) {}

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.page_select_button:
                //NumberPicker won't update its internal value until the user finishes typing,
                //but it will update when it loses focus.
                //this is so fucking stupid
                picker.clearFocus();
                selectPage(picker.getValue());
                break;            case R.id.page_select_first:
                picker.setValue(1);
                break;
            case R.id.page_select_last:
                picker.setValue(maxPage);
                break;
        }
    }
}
