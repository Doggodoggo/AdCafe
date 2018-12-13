package com.bry.adcafe.classes;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
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
        Log.d("MyPlaceHolderView","touch event detected");
//        Variables.activeEvent = ev;
        if(touchListener!=null)touchListener.onTouch(MyPlaceHolderView.this,ev);
        return super.onTouchEvent(ev);
    }



    public void setMyOnTouchEvent(final OnTouchListener customTouchListener){
        touchListener = customTouchListener;
    }


}
