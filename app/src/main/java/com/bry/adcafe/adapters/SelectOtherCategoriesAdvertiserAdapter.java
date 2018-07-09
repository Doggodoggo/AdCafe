package com.bry.adcafe.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

@NonReusable
@Layout(R.layout.select_other_category_advertiser_item)
public class SelectOtherCategoriesAdvertiserAdapter {
    @View(R.id.categoryCheckBok) private CheckBox mCategoryCheckBok;
    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private String mCategory;

    public SelectOtherCategoriesAdvertiserAdapter(Context context, PlaceHolderView PHView, String category){
        this.mContext = context;
        this.mPlaceHolderView = PHView;
        this.mCategory = category;
    }

    @Resolve
    private void onResolved(){
        mCategoryCheckBok.setText(mCategory);
        if(Variables.targetCategoryList.contains(mCategory)) mCategoryCheckBok.setChecked(true);
        mCategoryCheckBok.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(mCategoryCheckBok.isChecked()) Variables.targetCategoryList.add(mCategory);
                else Variables.targetCategoryList.remove(mCategory);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Constants.SET_MULTI_CATEGORY_DATA));
            }
        });
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForReset,
                new IntentFilter(Constants.RESET_CATEGORIES_SELECTED));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForRemoveReceivers,
                new IntentFilter(Constants.UNREGISTER_LISTENERS));
    }

    private BroadcastReceiver mMessageReceiverForReset = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mCategoryCheckBok.setChecked(false);
        }
    };

    private BroadcastReceiver mMessageReceiverForRemoveReceivers = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForReset);
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
        }
    };

}
