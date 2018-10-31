package com.bry.adcafe.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.services.DatabaseManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by bryon on 29/11/2017.
 */

@NonReusable
@Layout(R.layout.select_category_item)
public class SubscriptionManagerItem {
    @View(R.id.cat_name) private TextView categoryName;
    @View(R.id.cat_details) private TextView categoryDetails;
    @View(R.id.cat_select) private CheckBox checkBox;
    @View(R.id.categoryImage) private ImageView categoryImage;
    @View(R.id.checkImage) private ImageView mCheckImage;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private String category;
    private boolean isChecked;
    private boolean isSubscribing;
    private boolean areListenersActive;
    private int imageId;

    public SubscriptionManagerItem(Context context, PlaceHolderView placeHV,String Category,boolean isChecked){
        this.mContext = context;
        this.mPlaceHolderView = placeHV;
        this.category = Category;
        this.isChecked = isChecked;
    }

    @Resolve
    private void onResolved() {
        Variables.Subscriptions.containsKey(category);

        categoryName.setText(category);
        checkBox.setText(category);
        checkBox.setChecked(Variables.Subscriptions.containsKey(category));
        setCheckImage(Variables.Subscriptions.containsKey(category));
        isChecked = Variables.Subscriptions.containsKey(category);

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForUnregisterAllReceivers,
                new IntentFilter("UNREGISTER_ALL"));
        try{
            setImage();
            if(isChecked) setBAndWhite();
            else removeBAWhite();
        }catch (Exception e){
            e.printStackTrace();
        }

        categoryImage.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View view) {
                try {
                    checkBox.performClick();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    @Click(R.id.cat_select)
    private void onClick(){
        if(isChecked){
            if(isOnline(mContext)) {
                if(Variables.Subscriptions.size()>1){
                    if(!category.equals(Variables.getCurrentAdvert().getCategory())) {
                        Log("SubscriptionManagerItem","The category being viewed is not being removed");
                        Log("SubscriptionManagerItem","The category being viewed is "+
                                getSubscriptionValue(Variables.getCurrentSubscriptionIndex())
                                +" while the categories being removed is "+category);
                        removeSubscription();
                    } else {
                        checkBox.setChecked(true);
                        Toast.makeText(mContext, "You cannot remove that because your currently viewing content of it.", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(mContext,"You have to have at least one category!",Toast.LENGTH_LONG).show();
                    checkBox.setChecked(true);
                }
            } else Toast.makeText(mContext, "You might need an internet connection to un-subscribe.", Toast.LENGTH_SHORT).show();
        }else{
            if(isOnline(mContext)) {
                addSubscription();
            } else Toast.makeText(mContext, "You might need an internet connection to subscribe.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addSubscription() {
        Variables.areYouSureText = "Are you sure you want to subscribe to "+category+"?";
        Intent intent = new Intent(Constants.CONFIRM_START);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        isSubscribing = true;
        setUpTransactionListeners();
        setBAndWhite();
    }

    private void removeSubscription() {
        Variables.areYouSureText = "Are you sure you want to un-subscribe from "+category+"?";
        Intent intent = new Intent(Constants.CONFIRM_START);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        isSubscribing = false;
        setUpTransactionListeners();
        removeBAWhite();
    }

    private BroadcastReceiver mMessageReceiverForAllClear = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(isSubscribing){
                DatabaseManager dbm = new DatabaseManager();
                dbm.setContext(mContext);
                dbm.subscribeUserToSpecificCategory(category);
                Log("SelectCategoryItem - ", "Adding category - " + category);
            }else{
                int SubscriptionClusterID = Variables.Subscriptions.get(category);
                DatabaseManager dbm = new DatabaseManager();
                dbm.setContext(mContext);
                dbm.unSubscribeUserFormAdvertCategory(category,SubscriptionClusterID);
                Log("SelectCategoryItem - ", "Removing category - " + category);
            }

        }
    };

    private BroadcastReceiver mMessageReceiverForFinishedSubscribing = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkBox.setChecked(true);
            setCheckImage(true);
            isChecked = true;
            removeTransactionListeners();
        }
    };

    private BroadcastReceiver mMessageReceiverForFinishedUnSubscribing = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkBox.setChecked(false);
            setCheckImage(false);
            isChecked = false;
            removeTransactionListeners();
        }
    };

    private BroadcastReceiver mMessageReceiverForUnregisterAllReceivers = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          if(areListenersActive)removeTransactionListeners();
          LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
        }
    };

    private BroadcastReceiver mMessageReceiverForUserCanceled = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            removeTransactionListeners();
            checkBox.setChecked(isChecked);
            setCheckImage(isChecked);
            if(isChecked)setBAndWhite();
            else removeBAWhite();
        }
    };

    private void setUpTransactionListeners(){
        areListenersActive = true;
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForAllClear,
                new IntentFilter(Constants.ALL_CLEAR)); //this receives broadcast for all clear.

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForFinishedSubscribing,
                new IntentFilter(Constants.SET_UP_USERS_SUBSCRIPTION_LIST)); //this receives broadcast for finishing adding subscription.

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForFinishedUnSubscribing,
                new IntentFilter(Constants.FINISHED_UNSUBSCRIBING)); //this receives broadcast for finishing unsubscribing user.

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForUserCanceled,
                new IntentFilter(Constants.CANCELLED));

    }

    private void removeTransactionListeners(){
        areListenersActive = false;
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForAllClear);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForFinishedSubscribing);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForFinishedUnSubscribing);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForUserCanceled);

    }

    private boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

    private String getSubscriptionValue(int index) {
        LinkedHashMap map = Variables.Subscriptions;
        String Sub = (new ArrayList<String>(map.keySet())).get(index);
        Log("SubscriptionManagerItem", "Subscription gotten from getCurrent Subscription method is :" + Sub);
        return Sub;
    }

    private int getPositionOf(String subscription) {
        LinkedHashMap map = Variables.Subscriptions;
        List<String> indexes = new ArrayList<String>(map.keySet());
        return indexes.indexOf(subscription);
    }


    private void setImage(){
        String filename;
        filename = category.replaceAll(" ","_");
        Log("SelectCategoryIntem","filename is: "+filename);

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
        imageId = getImage(filename);
    }

    private void setBAndWhite(){
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(1);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        categoryImage.setColorFilter(filter);
    }

    private void removeBAWhite(){
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
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

    private void Log(String tag,String message){
        try{
            String user = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            if(user.equals("bryonyoni@gmail.com")) Log.d(tag,message);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
