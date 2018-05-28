package com.bry.adcafe.adapters;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.models.User;
import com.bry.adcafe.services.TimeManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.ViewHolder;
import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.LongClick;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by bryon on 05/09/2017.
 */

@NonReusable
@Layout(R.layout.saved_ads_list_item)
public class SavedAdsCard {
    private static final String TAG = "SavedAdsCard";
    @View(R.id.SavedImageView) private ImageView imageView;
    @View(R.id.savedErrorImageView) private ImageView errorImageView;
    @View(R.id.savedAdCardAvi) private AVLoadingIndicatorView mAvi;
    @View(R.id.sacard) private CardView mCardView;
    @View(R.id.selectedIcon) private ImageView mSelectedIcon;
//    @View(R.id.testText) private TextView testText;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private Advert mAdvert;
    public String mId;
    private ProgressDialog mAuthProgressDialog;
    private boolean hasMessageBeenSeen = false;
    private boolean onDoublePressed = false;
    private boolean isBeingShared = false;

    private long noOfDaysDate;
    private SavedAdsCard sac;
    private boolean mIsLastElement;
    private byte[] mImageBytes;
    private Bitmap bs;
    private boolean hasLoaded =false;
    private int index;

    private boolean isLoadingImageFromFirebase = false;
    private boolean hasSentIntentToViewAd = false;
    private PlaceHolderView parentPHView;


    public SavedAdsCard(int index,PlaceHolderView parentPHView,Advert advert, Context context, PlaceHolderView placeHolderView,String pinID,long noOfDays,boolean isLastElement) {
        this.mAdvert = advert;
        mContext = context;
        this.mPlaceHolderView = placeHolderView;
        this.noOfDaysDate = noOfDays;
        this.mIsLastElement = isLastElement;
        this.index = index;
        this.parentPHView = parentPHView;
    }

    @Resolve
    private void onResolved() {
        Log.e("SavedAdsCard","The date for pinning is: "+getDateFromDays(noOfDaysDate)+" the pos is: "+index);
        if(imageView.getDrawable()==null){
            new LongOperationFI().execute("");
        }
//        testText.setText(getDateFromDays(noOfDaysDate));
        loadListeners();
        sac = this;
        BAWhite();
    }

    private BroadcastReceiver mMessageReceiverToLoadImages = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("SavedAdsCard--","Received broadcast to LoadAd image");
            setImageFromCardBroadcast();
        }
    };

    private void setImageFromCardBroadcast(){
        imageView.setImageBitmap(Variables.loadedSavedAdsList.get(mAdvert.getPushRefInAdminConsole()));
        mAvi.setVisibility(android.view.View.GONE);
    }

    private void loadListeners() {
        if(!hasLoaded) {
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverToUnregisterAllReceivers,
                    new IntentFilter("UNREGISTER"));
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForAddNewBlank,
                    new IntentFilter("ADD_BLANK" + noOfDaysDate + mAdvert.getPushId()));
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverToLoadImages,
                    new IntentFilter("SET_IMAGE" + noOfDaysDate));
            hasLoaded = true;
        }
    }

    private void doNothing() {

    }

    private void loadImageFromFirebaseFirst() {
        isLoadingImageFromFirebase = true;
        Log.d("SavedAdsCard","Loading the image from firebase first");
        DatabaseReference adRef2 = FirebaseDatabase.getInstance().getReference(Constants.PINNED_AD_POOL)
                .child(Long.toString(mAdvert.getDateInDays())).child(mAdvert.getPushRefInAdminConsole()).child("imageUrl");

        Log.d("SavedAdsCard","Query set up is --"+Constants.PINNED_AD_POOL+" : "+
                mAdvert.getDateInDays()+" : "+mAdvert.getPushRefInAdminConsole());

        adRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String image = dataSnapshot.getValue(String.class);
                    if(image!=null)Log.d("SavedAdsCard","String of image has been loaded from firebase");
                    mAdvert.setImageUrl(image);
                    Log.d("SavedAdsCard","Now running the setImage method");
                    setImage();
                }else{
                    isLoadingImageFromFirebase = false;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("SavedAdsCard","Something went wrong while loading image from firebase : "+databaseError.getDetails());
            }
        });
    }

    private void setImage() {
        try {
            Bitmap bm = decodeFromFirebaseBase64(mAdvert.getImageUrl());
            Log.d("SavedAdsCard---", "Image has been converted to bitmap.");
            bs = getResizedBitmap(bm, 300);
            mImageBytes = bitmapToByte(bs);
            if(!Variables.loadedSavedAdsList.containsKey(mAdvert.getPushRefInAdminConsole())){
                Variables.loadedSavedAdsList.put(mAdvert.getPushRefInAdminConsole(),bs);
            }
            mAdvert.setImageBitmap(bm);
            isLoadingImageFromFirebase = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    private void loadImage2(){
        Glide.with(mContext).load(mImageBytes).diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<byte[], GlideDrawable>() {
            @Override
            public boolean onException(Exception e, byte[] model, Target<GlideDrawable> target, boolean isFirstResource) {
                try{
                    mAvi.setVisibility(android.view.View.GONE);
                    errorImageView.setVisibility(android.view.View.VISIBLE);
                }catch (Exception e2){
                    e2.printStackTrace();
                }
                Log.d("SavedAdsCard---",e.getMessage());
                if(!isInternetAvailable()&& !hasMessageBeenSeen){
                    hasMessageBeenSeen = true;
                    Intent intent = new Intent(Constants.CONNECTION_OFFLINE);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                }
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, byte[] model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                try{
                    mAvi.setVisibility(android.view.View.GONE);
                    errorImageView.setVisibility(android.view.View.GONE);
                }catch (Exception e){
                    e.printStackTrace();
                }
                hasLoaded = true;
                return false;
            }
        }).into(imageView);
    }

    private void loadImage(){
//        mAvi.setVisibility(android.view.View.VISIBLE);
        try {
            Bitmap bm = decodeFromFirebaseBase64(mAdvert.getImageUrl());
            mAdvert.setImageBitmap(bm);
            Log.d("SavedAdsCard---","Image has been converted to bitmap and set in model instance.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Glide.with(mContext).load(bitmapToByte(getResizedBitmap(mAdvert.getImageBitmap(),170))).listener(new RequestListener<byte[], GlideDrawable>() {
            @Override
            public boolean onException(Exception e, byte[] model, Target<GlideDrawable> target, boolean isFirstResource) {
                try{
                    mAvi.setVisibility(android.view.View.GONE);
                    errorImageView.setVisibility(android.view.View.VISIBLE);
                }catch (Exception e2){
                    e2.printStackTrace();
                }
                if(mIsLastElement){
                    Intent intent = new Intent("DONE!!");
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                }
                Log.d("SavedAdsCard---",e.getMessage());
                if(!isInternetAvailable()&& !hasMessageBeenSeen){
                    hasMessageBeenSeen = true;
                    Intent intent = new Intent(Constants.CONNECTION_OFFLINE);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                }
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, byte[] model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                try{
                    mAvi.setVisibility(android.view.View.GONE);
                    errorImageView.setVisibility(android.view.View.GONE);
                }catch (Exception e){
                    e.printStackTrace();
                }
                if(mIsLastElement){
                    Intent intent = new Intent("DONE!!");
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                }
                return false;
            }
        }).into(imageView);
    }




    private BroadcastReceiver mMessageReceiverToUnregisterAllReceivers = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("ADVERT_CARD--","Received broadcast to Unregister all receivers");
            unregisterAllReceivers();
        }
    };

    private BroadcastReceiver mMessageReceiverForUnpin = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("SAVED_ADS--","Received broadcast to Unpin ad");
            unPin();
            removeReceiver();
        }
    };

    private BroadcastReceiver mMessageReceiverForUnpin2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("SAVED_ADS--","Received broadcast to Unpin ad");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    unPin();
                }
            }, 400);

            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
        }
    };

    private BroadcastReceiver mMessageReceiverForAddNewBlank = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("SAVED_ADS--","Received broadcast to ad blank item.");
            pushBlank();
//            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
        }
    };




    private void unregisterAllReceivers(){
        try{LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForUnpin);
        }catch (Exception e){
            e.printStackTrace();
        }
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForAddNewBlank);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverToUnregisterAllReceivers);
    }

    @LongClick(R.id.SavedImageView)
    private void onLongClick(){
//        promptUserIfSureToUnpinAd();
        Variables.isSelectingMultipleItems = true;
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Constants.SHOW_DELETE_ICON));
        selectAdForUnpinning();
    }

    @Click(R.id.SavedImageView)
    private void onClick(){
        if(Variables.isSelectingMultipleItems) selectAdForUnpinning();
        else viewAd();
    }

    private void selectAdForUnpinning() {
        if(!Variables.UnpinAdsList.contains(mAdvert)){
            Variables.UnpinAdsList.add(mAdvert);
            mSelectedIcon.setVisibility(android.view.View.VISIBLE);
            setReceiver();
        }else{
            Variables.UnpinAdsList.remove(mAdvert);
            mSelectedIcon.setVisibility(android.view.View.INVISIBLE);
            removeListener();
            if(Variables.UnpinAdsList.isEmpty()){
                Variables.isSelectingMultipleItems = false;
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Constants.SHOW_DELETE_ICON));
            }
        }
        BAWhite();
    }


    private BroadcastReceiver mMessageReceiverForRemoveSelf = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("SAVED_ADS--","Received broadcast to remove self.");
            mPlaceHolderView.removeView(sac);
            unregisterAllReceivers();
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForRemoveRemoveSelfListener);
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
        }
    };

    private BroadcastReceiver mMessageReceiverForRemoveRemoveSelfListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("SAVED_ADS--","Received broadcast to remove self.");
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForRemoveSelf);
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
            try {
                mSelectedIcon.setVisibility(android.view.View.INVISIBLE);
                BAWhite();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    private void setReceiver() {
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForRemoveSelf,
                new IntentFilter(Constants.REMOVE_SELF_LISTENER));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForRemoveRemoveSelfListener,
                new IntentFilter(Constants.REMOVE_REMOVE_SELF_LISTENER));
    }

    private void removeListener() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForRemoveSelf);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForRemoveRemoveSelfListener);
    }


    private void promptUserIfSureToUnpinAd(){
        if(isNetworkConnected(mContext)){
            Variables.adToBeUnpinned = mAdvert;
            Variables.noOfDays = noOfDaysDate;
            Variables.placeHolderView = mPlaceHolderView;
            Variables.position = mPlaceHolderView.getViewResolverPosition(this);
            Intent intent = new Intent("ARE_YOU_SURE2");
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

            LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForUnpin2
                    ,new IntentFilter(mAdvert.getPushRefInAdminConsole()));
        }else{
            Toast.makeText(mContext,"You need an internet connection to unpin that.",Toast.LENGTH_SHORT).show();
        }

    }

    private void viewAd() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if(!isBeingShared && hasLoaded) {
                    Log.d("SavedAdsCard", "Setting the ad to be viewed.");
                    Variables.adToBeViewed = mAdvert;
                    Variables.adToBeUnpinned = mAdvert;
                    Variables.noOfDays = noOfDaysDate;
                    Variables.placeHolderView = mPlaceHolderView;
                    Variables.position = mPlaceHolderView.getViewResolverPosition(this);
//                    if(!hasSentIntentToViewAd){
//                        hasSentIntentToViewAd = true;
                        Intent intent = new Intent("VIEW");
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        setUpReceiver();
//                    }
                }
                isBeingShared = false;
            }
        }, 3);
    }

    private void setUpReceiver(){
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForUnpin
                ,new IntentFilter(mAdvert.getPushRefInAdminConsole()));
    }




    private void removeReceiver(){
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForUnpin);
        hasSentIntentToViewAd = false;
    }

    private void shareAd() {
        isBeingShared = true;
        Log.d("SavedAdsCard","Setting the ad to be shared.");
        Variables.adToBeShared = mAdvert;
        Intent intent = new Intent("SHARE");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

    }

    private boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            return !ipAddr.toString().equals("");
        } catch (Exception e) {
            return false;
        }

    }




    private void unPin(){
        String id;
        try{
            id = mAdvert.getPushId();
        }catch (Exception e){
            e.printStackTrace();
            id = Variables.adToBeUnpinned.getPushId();
        }
        Variables.VariablesHashOfAds.get(noOfDaysDate).remove(Variables.adToBeUnpinned);
        try{
            mPlaceHolderView.removeView(this);
        }catch (Exception e){
            e.printStackTrace();
            Variables.placeHolderView.removeView(this);
        }
        if(Variables.placeHolderView.getChildCount()==0){
            Intent intent2 = new Intent("CHECK_IF_IS_EMPTY"+noOfDaysDate);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent2);
        }
        unregisterAllReceivers();

        Intent intent = new Intent("EQUATE_PLACEHOLDER_VIEWS");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

        Log.d("SAVED_ADS_CARD--","Removing pinned ad"+id);
        String uid = User.getUid();
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.PINNED_AD_LIST).child(Long.toString(noOfDaysDate)).child(id);

        checkAndRemoveBannerIfNoOtherPiners();
        adRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshot.getRef().removeValue();
                Intent intent = new Intent(Constants.REMOVE_PINNED_AD);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Intent intent = new Intent(Constants.UNABLE_TO_REMOVE_PINNED_AD);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        });

    }

    private void checkAndRemoveBannerIfNoOtherPiners() {
        Advert ad = Variables.adToBeUnpinned;
        final DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.PINNED_AD_POOL)
                .child(Long.toString(ad.getDateInDays())).child(ad.getPushRefInAdminConsole()).child(Constants.NO_OF_TIMES_PINNED);

        final DatabaseReference adRef2 = FirebaseDatabase.getInstance().getReference(Constants.PINNED_AD_POOL)
                .child(Long.toString(ad.getDateInDays())).child(ad.getPushRefInAdminConsole()).child("imageUrl");

        adRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    int numberOfPins = dataSnapshot.getValue(int.class);
                    if(numberOfPins==1){
                        adRef.removeValue();
                        adRef2.removeValue();
                    }else{
                        adRef.setValue(numberOfPins-1);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private byte[] bitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] byteArray = baos.toByteArray();
        return byteArray;
    }




    private static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        Bitmap bitm = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
//        Bitmap newBm = getResizedBitmap(bitm,00);
        return bitm;
    }

    private static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }



    private boolean isNetworkConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    private void pushBlank(){
        try{
            mPlaceHolderView.addViewAfter(sac,new BlankItem(mContext,mPlaceHolderView,noOfDaysDate,"pineapples",false));
        }catch (Exception e){
            e.printStackTrace();
            try{
                mPlaceHolderView = Variables.placeHolderView;
                mPlaceHolderView.addViewAfter(sac,new BlankItem(mContext,mPlaceHolderView,noOfDaysDate,"pineapples",false));
            }catch (Exception e2){
                e2.printStackTrace();
                try{
                    mPlaceHolderView = Variables.placeHolderView;
                    mPlaceHolderView.addViewAfter(mPlaceHolderView.getViewResolverPosition(sac),
                            new BlankItem(mContext,mPlaceHolderView,noOfDaysDate,"pineapples",false));
                }catch (Exception e3){
                    e3.printStackTrace();
                    Variables.adView = sac;
                    Variables.lastNoOfDays = noOfDaysDate;
                    Intent intent = new Intent("ADD_VIEW_IN_ACTIVITY");
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                }
            }
        }
    }

    private class LongOperationFI extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            if(mAdvert.getImageBitmap()==null) loadImageFromFirebaseFirst();
            else isLoadingImageFromFirebase = false;
            while(isLoadingImageFromFirebase){
                doNothing();
            }
            return "executed";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(mImageBytes!=null) loadImage2();
            else{
                mAvi.setVisibility(android.view.View.GONE);
                errorImageView.setVisibility(android.view.View.VISIBLE);
            }
        }

        @Override
        protected void onPreExecute() {
            if(imageView.getDrawable()!=null){
                imageView.setImageBitmap(null);
            }
            mAvi.setVisibility(android.view.View.VISIBLE);
            super.onPreExecute();
        }
    }

    private void BAWhite(){
        int val = 1;
        if(Variables.UnpinAdsList.contains(mAdvert)) val = 0;
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(val);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        imageView.setColorFilter(filter);
    }



    private String getDateFromDays(long days){
        long currentTimeInMills = -days*(1000*60*60*24);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentTimeInMills);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        int monthOfYear = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

//        String monthName = new DateFormatSymbols().getMonths()[monthOfYear];
        String monthName = getMonthName_Abbr(monthOfYear);

        Log.d(TAG,"Date gotten is : "+dayOfMonth+" "+monthName+" "+year);

        Calendar cal2 = Calendar.getInstance();
        int year2 = cal2.get(Calendar.YEAR);
        int day2 = cal2.get(Calendar.DAY_OF_MONTH);
        int month2 = cal2.get(Calendar.MONTH);

        String yearName;
        String dayWord;

        if(year == year2){
            Log.d(TAG,"Ad was pined this year...");
            yearName = "";
        }else if(year2 == year+1){
            Log.d(TAG,"Ad was pined last year...");
            yearName =", "+Integer.toString(year);
        }else{
            yearName =", "+ Integer.toString(year);
        }

        String ret = dayOfMonth+" "+monthName+yearName;

        if((-days) == getDateInDays()-1){
            ret = "Yesterday";
        }

        if((-days) == getDateInDays()){
            ret = "Today";
        }

        return ret;
    }

    private long getDateInDays(){
        return TimeManager.getDateInDays();
    }

    private String getMonthName_Abbr(int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month);
        SimpleDateFormat month_date = new SimpleDateFormat("MMM");
        String month_name = month_date.format(cal.getTime());
        return month_name;
    }

}

