package com.bry.adcafe.models;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.mindorks.placeholderview.PlaceHolderView;

public class MyPlaceHolderView extends PlaceHolderView {
    private OnTouchListener touchListener;

    public MyPlaceHolderView(Context context) {
        super(context);
    }

    public MyPlaceHolderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyPlaceHolderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(touchListener!=null)touchListener.onTouch(this,ev);
        return super.onTouchEvent(ev);
    }

    public void setOnTouchEvent(final OnTouchListener customTouchListener){
        touchListener = customTouchListener;
    }


}
