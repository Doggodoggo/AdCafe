package com.bry.adcafe.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.adapters.SelectOtherCategoriesAdvertiserAdapter;
import com.mindorks.placeholderview.PlaceHolderView;

public class SelectOtherCategoriesAdvertiser extends DialogFragment {
    private Context mContext;
    private String mKey = "";
    private Button resetBtn;
    private Button okBtn;
    private PlaceHolderView PlaceHolderViewCategories;
    private String mCategory;



    public void setfragcontext(Context context){
        mContext = context;
    }

    public void setCategory(String category){
        this.mCategory = category;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_set_other_categories_advertiser, container, false);

        resetBtn = rootView.findViewById(R.id.resetBtn);
        okBtn = rootView.findViewById(R.id.okBtn);
        PlaceHolderViewCategories = rootView.findViewById(R.id.PlaceHolderViewCategories);
        PlaceHolderViewCategories.setItemViewCacheSize(Variables.allCategories.size());

        for(String category: Variables.allCategories){
            if(!category.equals(mCategory)) {
                PlaceHolderViewCategories.addView(new SelectOtherCategoriesAdvertiserAdapter(mContext, PlaceHolderViewCategories, category));
            }
        }

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Variables.targetCategoryList.clear();
                Toast.makeText(mContext,"All extra categories removed.",Toast.LENGTH_SHORT).show();
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("IS_ADVERTISER_FILTERING"));
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Constants.RESET_CATEGORIES_SELECTED));
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Constants.SET_MULTI_CATEGORY_DATA));
            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Variables.targetCategoryList.isEmpty()){
                    Toast.makeText(mContext,"Choose at least one!",Toast.LENGTH_SHORT).show();
                }else{
                    dismiss();
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Constants.SET_MULTI_CATEGORY_DATA));
                }
            }
        });

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        return rootView;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Constants.UNREGISTER_LISTENERS));
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("IS_ADVERTISER_FILTERING"));

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
