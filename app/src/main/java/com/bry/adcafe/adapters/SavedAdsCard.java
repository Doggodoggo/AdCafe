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
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import com.bry.adcafe.services.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
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
import java.io.File;
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
    @View(R.id.SavedImageBack) private ImageView SavedImageBack;
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

    private MotionEvent mEvent;
    private Bitmap bl;


    public SavedAdsCard(int index,PlaceHolderView parentPHView,Advert advert, Context context,
                        PlaceHolderView placeHolderView,String pinID,long noOfDays,boolean isLastElement) {
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
        if(mAdvert.getDownloadImageName()!=null){
            Bitmap image = loadImageFromMemory(mAdvert.getDownloadImageName());
            if(image!=null){
                mAdvert.setImageBitmap(image);
                imageView.setImageBitmap(image);
                try{
                    mAvi.setVisibility(android.view.View.GONE);
                    errorImageView.setVisibility(android.view.View.GONE);
                }catch (Exception e){
                    e.printStackTrace();
                }
                hasLoaded = true;
            }else{
                if(imageView.getDrawable()==null){
                    new LongOperationFI().execute("");
                }
            }
        }else{
            if(imageView.getDrawable()==null){
                new LongOperationFI().execute("");
            }
        }
        loadListeners();
        sac = this;
        BAWhite();

        imageView.setOnTouchListener(new android.view.View.OnTouchListener() {
            @Override
            public boolean onTouch(android.view.View v, MotionEvent event) {
                Log.e(TAG,"registered on touch: "+event.getRawX());
                Variables.activeEvent = event;
                return false;
            }
        });
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
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForShowSelf,new IntentFilter("SHOW_SELF"));
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForResetStarValue,new IntentFilter("STAR"));
            hasLoaded = true;
        }
    }

    private void doNothing() {

    }





    private void loadImageFromFirebaseFirst() {
        isLoadingImageFromFirebase = true;
        if(Variables.loadedSavedAdsList.containsKey(mAdvert.getPushRefInAdminConsole())){
            mAdvert.setImageBitmap(Variables.loadedSavedAdsList.get(mAdvert.getPushRefInAdminConsole()));
            setImage2();
        }else{
            Log.d("SavedAdsCard", "Loading the image from firebase first");
            try{
                DatabaseReference adRef2 = FirebaseDatabase.getInstance().getReference(Constants.PINNED_AD_POOL)
                        .child(Long.toString(mAdvert.getDateInDays()))
                        .child(mAdvert.getPushRefInAdminConsole())
                        .child("imageUrl");

                Log.d("SavedAdsCard", "Query set up is --" + Constants.PINNED_AD_POOL + " : " +
                        mAdvert.getDateInDays() + " : " + mAdvert.getPushRefInAdminConsole());

                adRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String image = dataSnapshot.getValue(String.class);
                            if (image != null)
                                Log.d("SavedAdsCard", "String of image has been loaded from firebase");
                            try {
                                mAdvert.setImageUrl(image);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            Log.d("SavedAdsCard", "Now running the setImage method");
                            setImage();
                        } else {
                            isLoadingImageFromFirebase = false;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("SavedAdsCard", "Something went wrong while loading image from firebase : " + databaseError.getDetails());
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void setImage2() {
        try {
            Bitmap bm = mAdvert.getImageBitmap();
            Log.d("SavedAdsCard---", "Image has been converted to bitmap.");
            bs = getResizedBitmap(bm, 300);
            mImageBytes = bitmapToByte(bs);
            if(!Variables.loadedSavedAdsList.containsKey(mAdvert.getPushRefInAdminConsole())){
                Variables.loadedSavedAdsList.put(mAdvert.getPushRefInAdminConsole(),bs);
            }
            isLoadingImageFromFirebase = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setImage() {
        try {
            Bitmap bm;
            if(!mAdvert.getImageUrl().equals("")) bm = decodeFromFirebaseBase64(mAdvert.getImageUrl());
            else bm = mAdvert.getImageBitmap();
            Log.d("SavedAdsCard---", "Image has been converted to bitmap.");
            bs = getResizedBitmap(bm, 300);
            mImageBytes = bitmapToByte(bs);
            if(!Variables.loadedSavedAdsList.containsKey(mAdvert.getPushRefInAdminConsole())){
                Variables.loadedSavedAdsList.put(mAdvert.getPushRefInAdminConsole(),bm);
            }
            mAdvert.setImageBitmap(bm);
            isLoadingImageFromFirebase = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap fastblur(Bitmap sentBitmap, float scale, int radius) {

        int width = Math.round(sentBitmap.getWidth() * scale);
        int height = Math.round(sentBitmap.getHeight() * scale);
        sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false);

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
//        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

//        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
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
        } catch (Exception e) {
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
        try{
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForUnpin);
        }catch (Exception e){
            e.printStackTrace();
        }
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForAddNewBlank);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForShowSelf);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverToUnregisterAllReceivers);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForResetStarValue);
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
        int[] originalPos = new int[2];
        imageView.getLocationOnScreen(originalPos);
        int x = originalPos[0];
        int y = (originalPos[1]);

        Variables.xPos = x;
        Variables.yPos = y;

        Log.e(TAG,"the X pos is: "+Variables.xPos);
        Log.e(TAG,"the Y pos is: "+Variables.yPos);
        Log.e(TAG,"the index is: "+index);
        Log.e(TAG,"the minus value is: "+((index+1)*(Utils.dpToPx(50))));

        if(Variables.isSelectingMultipleItems) selectAdForUnpinning();
        else viewAd();
    }

    private BroadcastReceiver mMessageReceiverForShowSelf = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("SAVED_ADS--","Received broadcast to ad blank item.");
            imageView.setVisibility(android.view.View.VISIBLE);
        }
    };




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

    private BroadcastReceiver mMessageReceiverForResetStarValue = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            mAdvert.setStarred(!mAdvert.isUserStarred());
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
                    imageView.setVisibility(android.view.View.INVISIBLE);
                    Intent intent = new Intent("VIEW");
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                    setUpReceiver();
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
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
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




    private static Bitmap decodeFromFirebaseBase64(String image) {
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
            loadImageFromFirebaseFirst();
            while(isLoadingImageFromFirebase){
                doNothing();
            }
            return "executed";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(mImageBytes!=null){
                loadImage2();
                setBackgroundImageBlurrs();
            }
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
//        if(val == 0)imageView.setBackgroundColor(mContext.getResources().getColor(R.color.white));
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


    private Bitmap loadImageFromMemory(String imageName){
        File path = new File(Environment.getExternalStorageDirectory(),"/AdCafePins");
        try{
            return BitmapFactory.decodeFile(path.getPath()+"/"+imageName);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    private void setBackgroundImageBlurrs(){
        new LongOperationBL().execute("");
    }

    private class LongOperationBL extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            bl = fastblur(bs,0.7f,27);
            if(!Variables.blurrs.containsKey(mAdvert.getPushRefInAdminConsole())){
                Variables.blurrs.put(mAdvert.getPushRefInAdminConsole(),bl);
            }
            return "executed";
        }

        @Override
        protected void onPostExecute(String result) {
            SavedImageBack.setImageBitmap(bl);
            super.onPostExecute(result);
        }

    }

}

