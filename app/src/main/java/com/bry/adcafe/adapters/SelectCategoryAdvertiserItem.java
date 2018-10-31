package com.bry.adcafe.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.ui.AdUpload;
import com.bry.adcafe.ui.SelectCategoryAdvertiser;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

/**
 * Created by bryon on 01/12/2017.
 */

@NonReusable
@Layout(R.layout.select_category_advertiser_list_item)
public class SelectCategoryAdvertiserItem {
    @View(R.id.cat_name) private TextView categoryName;
    @View(R.id.cat_details) private TextView categoryDetails;
    @View(R.id.categoryView) private LinearLayout categoryView;
    @View(R.id.categoryCard) private CardView categoryCardView;
    @View(R.id.categoryImage) private ImageView categoryImage;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private String category;


    public SelectCategoryAdvertiserItem(Context context, PlaceHolderView placeHV, String Category) {
        this.mContext = context;
        this.mPlaceHolderView = placeHV;
        this.category = Category;
    }

    @Resolve
    private void onResolve(){
        categoryName.setText(category);
        try{
            setImage();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Click(R.id.categoryImage)
    private void onClick(){
        Variables.SelectedCategory = category;
        Intent intent = new Intent("SELECTED_CATEGORY");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void setImage(){
        String filename;
        filename = category.replaceAll(" ","_");
        Log.d("SelectCategoryIntem","filename is: "+filename);

//        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), getImage(filename));
//        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(mContext.getResources(),
//                Bitmap.createScaledBitmap(bitmap,100,100,false));
//        roundedBitmapDrawable.setCircular(true);
//        categoryImage.setImageDrawable(roundedBitmapDrawable);

        Glide.with(mContext).load(getImage(filename)).asBitmap().centerCrop().into(new BitmapImageViewTarget(categoryImage) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(mContext.getResources(),resource);
//                                Bitmap.createScaledBitmap(resource,100,100,false));
                circularBitmapDrawable.setCircular(true);
                categoryImage.setImageDrawable(circularBitmapDrawable);
            }
        });

//        Glide.with(mContext).load(getImage(filename)).apply(RequestOptions.circleCropTransform()).into(categoryImage);
    }

    private int getImage(String imageName) {
        return mContext.getResources().getIdentifier(imageName, "drawable", mContext.getPackageName());
    }

}
