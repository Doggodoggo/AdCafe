package com.bry.adcafe.adapters;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.TextView;

import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bryon on 19/03/2018.
 */

@NonReusable
@Layout(R.layout.select_category_container)
public class SubscriptionManagerContainer {
    @View(R.id.MainCategoryTitle) private TextView mainCategoryNameView;
    @View(R.id.PHViewForGeneralCategory) private PlaceHolderView mPHVewForGeneralCategoryView;
    @View(R.id.newText) private TextView newText;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private String mCategoryName;
    private List<String> mSubCategories;

    public SubscriptionManagerContainer(Context context,PlaceHolderView placeHolderView,String CategoryName, List<String> subCategories){
        this.mContext = context;
        this.mPlaceHolderView = placeHolderView;
        this.mCategoryName = CategoryName;
        this.mSubCategories = subCategories;
    }


    @Resolve
    private void onResolved() {
        mainCategoryNameView.setText(mCategoryName+".");
        if(mCategoryName.equals("mens-fasion"))mainCategoryNameView.setText("mens-fashion.");
        if(mCategoryName.equals("womens-fasion"))mainCategoryNameView.setText("womens-fashion.");

        List<String> myNewSubCategories = new ArrayList<>();
        for(String subCategory:mSubCategories){
            if(Variables.newCategoryList.contains(subCategory)){
                myNewSubCategories.add(subCategory);
//                Variables.newCategories.remove(subCategory);
            }
        }
        if(!myNewSubCategories.isEmpty()){
            newText.setText(myNewSubCategories.size()+" new!");
            newText.setVisibility(android.view.View.VISIBLE);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mPHVewForGeneralCategoryView.setLayoutManager(layoutManager);
        for(String subCategory: mSubCategories){
            mPHVewForGeneralCategoryView.addView(new SubscriptionManagerItem(mContext,mPHVewForGeneralCategoryView,
                    subCategory, Variables.Subscriptions.containsKey(subCategory)));
        }

    }
}
