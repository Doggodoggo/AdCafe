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
    private ProgressBar progressBarProcessImage;
    private Bitmap backBl;
    private Bitmap bm;
    private ImageView AdImageBackground;


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
        AdImageBackground = view.findViewById(R.id.theAdImageBackground);
        progressBarViewImage = view.findViewById(R.id.progressBarViewImage);
        progressBarProcessImage = view.findViewById(R.id.progressBarProcessImage);
        setImageInView();

        return view;
    }

    private void setImageInView() {
        if(Variables.loadedSavedAdsList.containsKey(mAdvert.getPushRefInAdminConsole())){
            bm = Variables.loadedSavedAdsList.get(mAdvert.getPushRefInAdminConsole());
            mAdImageView.setImageBitmap(bm);
            progressBarProcessImage.setVisibility(View.VISIBLE);
            new LongOperationBL().execute("");
        }else{
            bm = loadImageFromMemory(mAdvert.getDownloadImageName());
            if(bm!=null) {
                mAdvert.setImageBitmap(bm);
                if(!Variables.loadedSavedAdsList.containsKey(mAdvert.getPushRefInAdminConsole())){
                    Variables.loadedSavedAdsList.put(mAdvert.getPushRefInAdminConsole(),bm);
                }
                mAdImageView.setImageBitmap(bm);
                progressBarProcessImage.setVisibility(View.VISIBLE);
                new LongOperationBL().execute("");
            }else{
                progressBarViewImage.setVisibility(View.VISIBLE);
                loadImageFromFirebase();
            }
        }
    }

    private class LongOperationBL extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            if(Variables.blurrs.containsKey(mAdvert.getPushRefInAdminConsole())){
               backBl = Variables.blurrs.get(mAdvert.getPushRefInAdminConsole());
            }else{
                backBl = fastblur(bm,0.7f,27);
                Variables.blurrs.put(mAdvert.getPushRefInAdminConsole(),backBl);
            }
            return "executed";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBarProcessImage.setVisibility(View.INVISIBLE);
            AdImageBackground.setImageBitmap(backBl);
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
                AdImageBackground.setImageBitmap(backBl);
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

            if(bm!=null){
                if(Variables.blurrs.containsKey(mAdvert.getPushRefInAdminConsole())){
                    backBl = Variables.blurrs.get(mAdvert.getPushRefInAdminConsole());
                }else{
                    backBl = fastblur(bm,0.7f,27);
                    Variables.blurrs.put(mAdvert.getPushRefInAdminConsole(),backBl);
                }
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
