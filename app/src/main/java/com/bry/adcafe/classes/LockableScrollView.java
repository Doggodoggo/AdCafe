package com.bry.adcafe.classes;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import com.bry.adcafe.Variables;

public class LockableScrollView extends ScrollView {
    private boolean mScrollable = true;
    private OnScrollChangedCallback mOnScrollChangedCallback;
    private OnTouchListener touchListener;
    private boolean startedOverScrollingY;


    public LockableScrollView(Context context) {
        super(context);
    }

    public LockableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LockableScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void setScrollingEnabled(boolean enabled) {
        mScrollable = enabled;
    }

    public boolean isScrollable() {
        return mScrollable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(mScrollable && touchListener!=null)touchListener.onTouch(this,ev);
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            return mScrollable && super.onTouchEvent(ev);
        } else {
            return super.onTouchEvent(ev);
        }
    }

    @Override
    protected void onScrollChanged(final int l, final int t, final int oldl, final int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if(mOnScrollChangedCallback != null) mOnScrollChangedCallback.onScroll(l, t, oldl, oldt);
        if (t < oldt) startedOverScrollingY = false;
    }

    @Override
    protected void onOverScrolled (int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        if (scrollY > 0 && clampedY && !startedOverScrollingY) {
            startedOverScrollingY = true;
            Variables.hasReachedBottomOfPage = true;
        }
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    }

    public OnScrollChangedCallback getOnScrollChangedCallback() {
        return mOnScrollChangedCallback;
    }

    public void setOnScrollChangedCallback(final OnScrollChangedCallback onScrollChangedCallback) {
        mOnScrollChangedCallback = onScrollChangedCallback;
    }

    public void setOnTouchEvent(final OnTouchListener customTouchListener){
        touchListener = customTouchListener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Don't do anything with intercepted touch events if
        // we are not scrollable
        return mScrollable && super.onInterceptTouchEvent(ev);
    }

    public interface OnScrollChangedCallback {
        void onScroll(int l, int t, int oldl, int oldt);
    }

    public interface OnTouchEventCallback{
        boolean onTouch(MotionEvent event);
    }
}
