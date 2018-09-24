package com.bry.adcafe.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.Advert;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;

public class ExpandedImageFragment extends Fragment {
    private final String TAG = "ExpandedImageFragment";
    private Context mContext;
    private String mKey = "";
    private int mPage;
    private Advert mAdvert;
    private String igsNein = "none";
    private Activity mActivity;

    private ImageView mAdImageView;
    private ProgressBar progressBarViewImage;


    public void setfragcontext(Context context){
        mContext = context;
    }

    public void setActivity(Activity activity){
        this.mActivity = activity;
    }

    public void setPosition(int pos){
        mPage = pos;
    }

    public void setAdvert(Advert ad){
        this.mAdvert = ad;
    }




    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.expanded_image_fragment, container, false);
        RelativeLayout imageRel = view.findViewById(R.id.imageRel);
//        imageRel.animate().setDuration(Constants.ANIMATION_DURATION).translationY(0)
//                .setInterpolator(new FastOutSlowInInterpolator());

        mAdImageView = view.findViewById(R.id.theAdImage);
        progressBarViewImage = view.findViewById(R.id.progressBarViewImage);
        setImageInView();

        return view;
    }

    private void setImageInView() {
        if(Variables.loadedSavedAdsList.containsKey(mAdvert.getPushRefInAdminConsole())){
            Bitmap bm = Variables.loadedSavedAdsList.get(mAdvert.getPushRefInAdminConsole());
            mAdImageView.setImageBitmap(bm);
        }else{
            Bitmap image = loadImageFromMemory(mAdvert.getDownloadImageName());
            if(image!=null) {
                mAdvert.setImageBitmap(image);
                if(!Variables.loadedSavedAdsList.containsKey(mAdvert.getPushRefInAdminConsole())){
                    Variables.loadedSavedAdsList.put(mAdvert.getPushRefInAdminConsole(),image);
                }
                mAdImageView.setImageBitmap(image);
            }else{
                progressBarViewImage.setVisibility(View.VISIBLE);
                loadImageFromFirebase();
            }
        }
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

    private void loadImageFromFirebase() {
        Log.d(TAG, "Loading the image from firebase first");
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.PINNED_AD_POOL)
                .child(Long.toString(mAdvert.getDateInDays())).child(mAdvert.getPushRefInAdminConsole()).child("imageUrl");

        adRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String image = dataSnapshot.getValue(String.class);
                    mAdvert.setImageUrl(image);
                    startSetImageFromThread();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void startSetImageFromThread(){
        Log.d(TAG,"Starting background execution of processing image from firebase");
        new LongOperationFI().execute("");
    }

    private class LongOperationFI extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            setImage();
            return "executed";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(mAdvert.getImageBitmap()!=null){
                progressBarViewImage.setVisibility(View.INVISIBLE);
                mAdImageView.setImageBitmap(mAdvert.getImageBitmap());
            }
        }

        @Override
        protected void onPreExecute() {
            progressBarViewImage.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }
    }

    private void setImage() {
        try {
            Bitmap bm = null;
            if(!mAdvert.getImageUrl().equals("")) bm = decodeFromFirebaseBase64(mAdvert.getImageUrl());
            Log.d("SavedAdsCard---", "Image has been converted to bitmap.");
//            Bitmap bs = getResizedBitmap(bm, 300);
//            mImageBytes = bitmapToByte(bs);
            if(!Variables.loadedSavedAdsList.containsKey(mAdvert.getPushRefInAdminConsole())){
                Variables.loadedSavedAdsList.put(mAdvert.getPushRefInAdminConsole(),bm);
            }
            mAdvert.setImageBitmap(bm);
            mAdvert.setImageUrl("");
        } catch (Exception e) {
            e.printStackTrace();
        }
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




}
