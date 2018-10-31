package com.bry.adcafe.models;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CustomViewPager extends ViewPager {
    private boolean enabled;
    private OnTouchListener touchListener;

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.enabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.enabled) {
            if(touchListener!=null)touchListener.onTouch(this,event);
            return super.onTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.enabled) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }

    public void setOnTouchEvent(final OnTouchListener customTouchListener){
        touchListener = customTouchListener;
    }

    public void setPagingEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
