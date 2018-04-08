package com.bry.adcafe.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bumptech.glide.Glide;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

/**
 * Created by bryon on 29/11/2017.
 */

@NonReusable
@Layout(R.layout.select_category_item)
public class SelectCategoryItem {
    @View(R.id.cat_name) private TextView categoryName;
    @View(R.id.cat_select) private CheckBox checkBox;
    @View(R.id.categoryCard)private CardView categoryCardView;
    @View(R.id.categoryImage) private ImageView categoryImage;
    @View(R.id.checkImage) private ImageView mCheckImage;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private String category;
    private boolean isChecked;
    private boolean isListenersSet = false;
    private String mCategoryGroup;
    private int imageId;

    public SelectCategoryItem(Context context, PlaceHolderView placeHV,String Category,boolean isChecked,String generalCategory){
        this.mContext = context;
        this.mPlaceHolderView = placeHV;
        this.category = Category;
        this.isChecked = isChecked;
        this.mCategoryGroup = generalCategory;
        setListeners();
    }

    @Resolve
    private void onResolved() {
        categoryName.setText(category);
        Log.d("SelectCategoryItem","checkbox for "+category+" should be : "
                +Variables.selectedCategoriesToSubscribeTo.contains(category));
        checkBox.setChecked(Variables.selectedCategoriesToSubscribeTo.contains(category));
        setCheckImage(Variables.selectedCategoriesToSubscribeTo.contains(category));
        isChecked = Variables.selectedCategoriesToSubscribeTo.contains(category);
        checkBox.setText(category);
        setListeners();
        try{
            setImage();
            if(Variables.selectedCategoriesToSubscribeTo.contains(category)) setBAndWhite();
            else removeBAWhite();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setListeners() {
        if(isListenersSet)removeListeners();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForAddCategory,
                new IntentFilter(Constants.ADD_CATEGORY_AUTOMATICALLY+mCategoryGroup));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForUnregisterReceiver,
                new IntentFilter(Constants.STOP_LISTENING));
        isListenersSet = true;
    }

    private void removeListeners(){
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForUnregisterReceiver);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForAddCategory);
        isListenersSet = false;
    }



    @Click(R.id.cat_select)
    private void onClick(){
        if(isChecked){
            checkBox.setChecked(false);
            setCheckImage(false);
            Variables.selectedCategoriesToSubscribeTo.remove(category);
            isChecked = false;
            removeBAWhite();
            Log.d("SelectCategoryItem - ","Removing category - "+category);

            Intent intent = new Intent("REMOVED_CATEGORY"+mCategoryGroup);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }else{
            checkBox.setChecked(true);
            setCheckImage(true);
            Variables.selectedCategoriesToSubscribeTo.add(category);
            isChecked = true;
            setBAndWhite();
            Log.d("SelectCategoryItem - ","Adding category - "+category);
        }
    }

    private void autoClick(){
        if(!isChecked){
            Variables.selectedCategoriesToSubscribeTo.add(category);
            isChecked = true;
            try{
                checkBox.setChecked(true);
                setCheckImage(true);
            }catch (Exception e){}
            try{
                if(Variables.selectedCategoriesToSubscribeTo.contains(category) && isChecked)setBAndWhite();
            }catch (Exception e){}
            Log.d("SelectCategoryItem - ","Auto-Adding category - "+category);
        }
    }



    private BroadcastReceiver mMessageReceiverForAddCategory = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           if(Variables.categoryGroupBeingAdded.equals(mCategoryGroup))autoClick();
        }
    };

    private BroadcastReceiver mMessageReceiverForUnregisterReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            removeListeners();
        }
    };




    private void setImage(){
        String filename;
        filename = category.replaceAll(" ","_");
        Log.d("SelectCategoryIntem","filename is: "+filename);
        Glide.with(mContext).load(getImage(filename)).override(100, 130).into(categoryImage);
        imageId = getImage(filename);
    }

    private void setBAndWhite(){
        if(Variables.selectedCategoriesToSubscribeTo.contains(category) && isChecked) {
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            categoryImage.setColorFilter(filter);
        }
    }

    private void removeBAWhite(){
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(1);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        categoryImage.setColorFilter(filter);
    }




    private int getImage(String imageName) {
        return mContext.getResources().getIdentifier(imageName, "drawable", mContext.getPackageName());
    }

    private void setCheckImage(boolean value){
        if(value) mCheckImage.setVisibility(android.view.View.VISIBLE);
        else mCheckImage.setVisibility(android.view.View.INVISIBLE);
    }

}
