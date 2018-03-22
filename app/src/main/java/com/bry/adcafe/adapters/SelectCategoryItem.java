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




    private void setImage2(){
        if(category.equals("farming land")){
//            Glide.with(mContext).load(R.drawable.farming_land1).override(400, 300).into(categoryImage);
        }else if(category.equals("fertilizer products")){

        }else if(category.equals("farming equipment")){

        }else if(category.equals("animal foods")){

        }else if(category.equals("agriculture stores")){

        }else if(category.equals("agriculture websites")){

        }else if(category.equals("agriculture apps")){

        }else if(category.equals("cars")){

        }else if(category.equals("car parts")){

        }else if(category.equals("motorcycles")){

        }else if(category.equals("automobile stores")){

        }else if(category.equals("automobile websites")){

        }else if(category.equals("automobile apps")){

        }else if(category.equals("loans")){

        }else if(category.equals("online banking")){

        }else if(category.equals("mobile banking")){

        }else if(category.equals("shows")){

        }else if(category.equals("movie services")){

        }else if(category.equals("events")){

        }else if(category.equals("concert-grounds")){

        }else if(category.equals("online entertainment")){

        }else if(category.equals("mobile entertainment")){

        }else if(category.equals("sports activities")){

        }else if(category.equals("gaming")){

        }else if(category.equals("foodstuffs")){

        }else if(category.equals("restaurants")){

        }else if(category.equals("alcoholic drinks")){

        }else if(category.equals("grocery stores")){

        }else if(category.equals("online groceries")){

        }else if(category.equals("grocery apps")){

        }else if(category.equals("property insurance")){

        }else if(category.equals("life insurance services")){

        }else if(category.equals("health insurance services")){

        }else if(category.equals("online insurance services")){

        }else if(category.equals("mobile insurance apps")){

        }else if(category.equals("household items")){

        }else if(category.equals("gym services")){

        }else if(category.equals("security services")){

        }else if(category.equals("interior design services")){

        }else if(category.equals("shopping malls")){

        }else if(category.equals("houses/real estate")){

        }else if(category.equals("sports equipment")){

        }else if(category.equals("medicine")){

        }else if(category.equals("clinics")){

        }else if(category.equals("vet services")){

        }else if(category.equals("medical websites")){

        }else if(category.equals("medical apps")){

        }else if(category.equals("official")){

        }else if(category.equals("casual")){

        }else if(category.equals("accessories")){

        }else if(category.equals("traditional wear")){

        }else if(category.equals("body products")){

        }else if(category.equals("clothing stores")){

        }else if(category.equals("clothing websites")){

        }else if(category.equals("clothing apps")){

        }else if(category.equals("sport clothing")){

        }else if(category.equals("")){

        }else if(category.equals("")){

        }else if(category.equals("")){

        }else if(category.equals("")){

        }else if(category.equals("")){

        }else if(category.equals("")){

        }else if(category.equals("")){

        }else if(category.equals("")){

        }else if(category.equals("")){

        }else if(category.equals("")){

        }else if(category.equals("")){

        }else if(category.equals("")){

        }else if(category.equals("")){

        }else if(category.equals("")){

        }else if(category.equals("")){

        }

    }

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
