package com.bry.adcafe.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

import java.util.List;

/**
 * Created by bryon on 19/03/2018.
 */

@NonReusable
@Layout(R.layout.select_category_container)
public class SelectCategoryContainer {
    @View(R.id.MainCategoryTitle) private TextView mainCategoryNameView;
    @View(R.id.PHViewForGeneralCategory) private PlaceHolderView mPHVewForGeneralCategoryView;
    @View(R.id.selectAllBtn) private Button mSelectAllBtn;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private String mCategoryGroup;
    private List<String> mSubCategories;
    private boolean isListenersSet = false;

    public SelectCategoryContainer(Context context,PlaceHolderView placeHolderView,String CategoryName, List<String> subCategories){
        this.mContext = context;
        this.mPlaceHolderView = placeHolderView;
        this.mCategoryGroup = CategoryName;
        this.mSubCategories = subCategories;
    }

    @Resolve
    private void onResolved() {
        mainCategoryNameView.setText(mCategoryGroup+".");
        if(mCategoryGroup.equals("mens-fasion"))mainCategoryNameView.setText("mens-fashion.");
        if(mCategoryGroup.equals("womens-fasion"))mainCategoryNameView.setText("womens-fashion.");


        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mPHVewForGeneralCategoryView.setLayoutManager(layoutManager);

        for(String subCategory: mSubCategories){
            mPHVewForGeneralCategoryView.addView(new SelectCategoryItem(mContext,mPHVewForGeneralCategoryView,
                    subCategory,false, mCategoryGroup));
        }
        mSelectAllBtn.setVisibility(android.view.View.VISIBLE);
        setListeners();
    }

    private void setListeners() {
        if(isListenersSet)removeListeners();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForUnregisterReceiver,
                new IntentFilter(Constants.STOP_LISTENING));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForRemovedCategory,
                new IntentFilter("REMOVED_CATEGORY"+mCategoryGroup));
        isListenersSet = true;
    }

    private void removeListeners() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForUnregisterReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForRemovedCategory);
        isListenersSet = false;
    }

    @Click(R.id.selectAllBtn)
    private void onClick(){
        Variables.categoryGroupBeingAdded = mCategoryGroup;
        Log.d("SelectCategoryContainer","Category group being added is : "+ mCategoryGroup);
        Intent intent = new Intent(Constants.ADD_CATEGORY_AUTOMATICALLY+ mCategoryGroup);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(intent));
        mSelectAllBtn.setAlpha(0.4f);
    }

    private BroadcastReceiver mMessageReceiverForRemovedCategory = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mSelectAllBtn.setAlpha(1.0f);
        }
    };

    private BroadcastReceiver mMessageReceiverForUnregisterReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            removeListeners();
        }
    };
}
