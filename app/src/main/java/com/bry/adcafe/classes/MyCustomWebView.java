package com.bry.adcafe.classes;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

import com.bry.adcafe.Variables;

public class MyCustomWebView extends WebView {
    private OnScrollChangedCallback mOnScrollChangedCallback;
    private OnTouchListener touchListener;
    private boolean startedOverScrollingY;



    public MyCustomWebView(final Context context)
    {
        super(context);
    }

    public MyCustomWebView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public MyCustomWebView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
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


    public OnScrollChangedCallback getOnScrollChangedCallback()
    {
        return mOnScrollChangedCallback;
    }

    public void setOnScrollChangedCallback(final OnScrollChangedCallback onScrollChangedCallback) {
        mOnScrollChangedCallback = onScrollChangedCallback;
    }

    public void setOnTouchEvent(final OnTouchListener customTouchListener){
        touchListener = customTouchListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(touchListener!=null)touchListener.onTouch(this,event);
        return super.onTouchEvent(event);
    }

    /**
     * Impliment in the activity/fragment/view that you want to listen to the webview
     */
    public interface OnScrollChangedCallback {
        void onScroll(int l, int t, int oldl, int oldt);
    }

    public interface OnTouchEventCallback{
        boolean onTouch(MotionEvent event);
    }
}
