package com.bry.adcafe.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.classes.MyPlaceHolderView;
import com.bry.adcafe.services.TimeManager;
import com.bry.adcafe.services.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by bryon on 10/01/2018.
 */

@NonReusable
@Layout(R.layout.saved_ads_list_container)
public class SAContainer {
    private final String TAG = "SAContainer";
    @View(R.id.dayText) private TextView dateTextView;
    @View(R.id.PHViewForSpecificDay) private MyPlaceHolderView PHViewForSpecificDay;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private List<Advert> adList;
    private long noOfDays;
    private boolean hasLoaded = false;
    private SAContainer ths;
    private DatabaseReference dbRef;
    private int cycles = 0;
    private int backgroundCycles = 0;

    private List<Advert> adList2 = new ArrayList<>();
    private int pos;
    private MotionEvent event;
    private GestureDetector expandGestureDetector;

    public SAContainer(List<Advert> adlist, Context context, PlaceHolderView placeHolderView, long noOfDayss, int pos) {
        this.adList = adlist;
        mContext = context;
        mPlaceHolderView = placeHolderView;
        noOfDays = noOfDayss;
        this.pos = pos;
    }

    @Resolve
    private void onResolved() {
        dateTextView.setText(getDateFromDays(noOfDays));
        if(!hasLoaded){
            addAdsIntoViews();
        }
        ths = this;
    }



    private void addAdsIntoViews() {
        int width = Variables.width;
        int calculatedSpanCount = width/ Utils.dpToPx(87);
        int spanCount = 4;

        GridLayoutManager glm = new GridLayoutManager(mContext,calculatedSpanCount);
        glm.supportsPredictiveItemAnimations();
        PHViewForSpecificDay.setLayoutManager(glm);

        PHViewForSpecificDay.setNestedScrollingEnabled(false);
        int idForDayList = pos;
        Log.w("SAContainer","The id for days list is: "+idForDayList);
        for (Advert ad: adList) {
            int pos = adList.indexOf(ad);
            PHViewForSpecificDay.addView(pos,new SavedAdsCard(idForDayList,mPlaceHolderView,ad,mContext,PHViewForSpecificDay,ad.getPushId(),noOfDays,false));
        }
        hasLoaded = true;
        loadListeners();

    }

    @Resolve
    public int getItemCount() {
        return adList.size();
    }

    private void loadListeners() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef =  FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.PINNED_AD_LIST).child(Long.toString(noOfDays));
        dbRef.addChildEventListener(chil);

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverToUnregisterAllReceivers,
                new IntentFilter("UNREGISTER"));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverToCheckIfIsEmpty,
                new IntentFilter("CHECK_IF_IS_EMPTY"+noOfDays));

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverToCheckIfHasChildren,
                new IntentFilter("CHECK_IF_HAS_CHILDREN"+noOfDays));

    }



    private BroadcastReceiver mMessageReceiverToUnregisterAllReceivers = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Received broadcast to Unregister all receivers");
            unregisterAllReceivers();
        }
    };

    private BroadcastReceiver mMessageReceiverToCheckIfIsEmpty = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Received broadcast to check if is empty.");
//            checkIfIsEmpty();
        }
    };

    private BroadcastReceiver mMessageReceiverToCheckIfHasChildren = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Received broadcast to check if has children.");
//            checkIfHasChildren2();
        }
    };




    private void checkIfIsEmpty() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Removing self since date has no children");
                mPlaceHolderView.removeView(ths);
                unregisterAllReceivers();
            }
        }, 300);

    }

    private ChildEventListener chil = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.d(TAG,"OnChildRemoved listener has been called.");
//            checkIfHasChildren();
            checkIfHasChildren2();
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void checkIfHasChildren2() {
        Log.d(TAG,"Checking if variable hash of ads for "+noOfDays+" date is empty.");
        Log.d(TAG,"It has got "+Variables.VariablesHashOfAds.get(noOfDays).size()+" items");
        if(Variables.VariablesHashOfAds.get(noOfDays).isEmpty()){
            Log.d(TAG,"it is.");
            removeThisView();
        }
    }

    private void checkIfHasChildren() {
        Long days;
        try {
            days = noOfDays;
            String test = Long.toString(days);
        }catch (Exception e){
            e.printStackTrace();
            days = Variables.noOfDays;
            noOfDays = Variables.noOfDays;
        }
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Query query =  FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.PINNED_AD_LIST).child(Long.toString(days));
        DatabaseReference dbRef = query.getRef();
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    removeThisView();
                    Log.d("DateItem -- ", "Removing date since no ads are in date :" + getDateFromDays(noOfDays));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }




    private void removeThisView() {
        unregisterAllReceivers();
        Variables.VariablesHashOfAds.remove(noOfDays);
        if(Variables.VariablesHashOfAds.isEmpty()){
            Intent intent2 = new Intent("SHOW_NO_ADS_TEXT");
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent2);
        }
        try{
            mPlaceHolderView.removeView(ths);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void unregisterAllReceivers() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverToUnregisterAllReceivers);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverToCheckIfIsEmpty);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverToCheckIfHasChildren);
        if(dbRef!=null) dbRef.removeEventListener(chil);
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


    private void loadAdImagesFirst(){
        addAdsIntoViews();
        for(final Advert ad :adList){
            Log.d("SavedAdsCard","Loading the image from firebase first");
            DatabaseReference adRef2 = FirebaseDatabase.getInstance().getReference(Constants.PINNED_AD_POOL)
                    .child(Long.toString(ad.getDateInDays())).child(ad.getPushRefInAdminConsole()).child("imageUrl");

            Log.d("SavedAdsCard","Query set up is --"+Constants.PINNED_AD_POOL+" : "+
                    ad.getDateInDays()+" : "+ad.getPushRefInAdminConsole());

            adRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        String image = dataSnapshot.getValue(String.class);
                        if(image!=null)Log.d("SavedAdsContainer","String of image has been loaded from firebase");
                        ad.setImageUrl(image);
                        adList2.add(ad);
                        Log.d("SavedAdsContainer","set image for ad:"+ad.getPushRefInAdminConsole());
                    }
                    cycles++;
                    if(cycles==adList.size()-1){
                        backgroundCycles = 0;
                        startSettingImageBitmaps();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("SavedAdsCard","Something went wrong while loading image from firebase : "+databaseError.getDetails());
                }
            });
        }
    }

    private void startSettingImageBitmaps() {
        new LongOperationFI().execute("yqlURL");
    }

    private static Bitmap decodeFromFirebaseBase64(String image){
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

    private class LongOperationFI extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            Advert ad = adList.get(backgroundCycles);
            if(ad.getImageUrl()!=null) {
                try {
                    Bitmap bm = decodeFromFirebaseBase64(ad.getImageUrl());
                    Log.d("SavedAdsCard---", "Image has been converted to bitmap.");
                    Bitmap bs = getResizedBitmap(bm, 300);
//                    if (!Variables.loadedSavedAdsList.containsKey(ad.getPushRefInAdminConsole())) {
//                        Variables.loadedSavedAdsList.put(ad.getPushRefInAdminConsole(), bs);
//                    }
                    Variables.loadedSavedAdsList.put(ad.getPushRefInAdminConsole(), bs);
                    ad.setImageBitmap(bm);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return "executed";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("SAContainer","Done converting all the image urls to bitmaps.");
            backgroundCycles++;
            if(backgroundCycles==adList.size()){
                startLoadingTheAds();
            }else startSettingImageBitmaps();
        }

        @Override
        protected void onPreExecute() {
          Log.d("SAContainer","Preparing to convert background image.");
        }
    }

    private void startLoadingTheAds() {
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("SET_IMAGE"+noOfDays));
    }


}
