package net.fastfourier.something.widget;

import android.content.Context;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by matthewshepard on 3/20/14.
 */
public class LockableSlidingPaneLayout extends SlidingPaneLayout {
    public LockableSlidingPaneLayout(Context context) {
        super(context);
    }

    public LockableSlidingPaneLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LockableSlidingPaneLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private boolean locked = false;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return !locked && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return !locked && super.onTouchEvent(ev);
    }

    public void setLocked(boolean locked){
        this.locked = locked;
    }
}
