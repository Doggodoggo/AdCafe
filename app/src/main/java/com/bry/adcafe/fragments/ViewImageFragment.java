package com.bry.adcafe.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.services.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by bryon on 20/11/2017.
 */

public class ViewImageFragment extends DialogFragment {
    private final String TAG = "ViewImageFragment";
    private Context mContext;
    private String mKey = "";
    private ImageView mImageView;
    private ImageButton mBackButton;
    private ImageButton mShareButton;
    private ImageButton mWebsiteLink;
    private ImageButton mDeleteButton;
    private ImageButton mDownloadButton;
    private Advert mAdvert;
    private String igsNein = "none";
    private Activity mActivity;


    public void setfragcontext(Context context){
        mContext = context;
    }

    public void setActivity(Activity activity){
        this.mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_view_image_dialog, container, false);

        mBackButton = (ImageButton) rootView.findViewById(R.id.backBtn);
        mShareButton = (ImageButton) rootView.findViewById(R.id.shareBtn);
        mWebsiteLink = (ImageButton) rootView.findViewById(R.id.Website);
        TextView websiteTextxx = (TextView) rootView.findViewById(R.id.websiteTextxx);
        mDeleteButton = (ImageButton) rootView.findViewById(R.id.Delete);
        mDownloadButton = rootView.findViewById(R.id.Download);
        mAdvert = Variables.adToBeViewed;
        if(mAdvert.getWebsiteLink().equals(igsNein)) {
            mWebsiteLink.setAlpha(0.4f);
            websiteTextxx.setAlpha(0.4f);
        }
        mImageView = (ImageView) rootView.findViewById(R.id.theAdImage);
        mImageView.setImageBitmap(mAdvert.getImageBitmap());

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ImageFragment","Setting ad to be shared.");
                Variables.adToBeShared = mAdvert;
                Intent intent = new Intent("SHARE");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        });
        mWebsiteLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mAdvert.getWebsiteLink().equals(igsNein)){
                    try {
                        String url = mAdvert.getWebsiteLink();
                        if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://" + url;
                        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(webIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(mContext, "There's something wrong with the link", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("ARE_YOU_SURE_INTENT");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                dismiss();
            }
        });

        mDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveImageDialog();
            }
        });

//        LocalBroadcastManager.getInstance(mContext).registerReceiver(new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                Intent intent2 = new Intent(mAdvert.getPushRefInAdminConsole());
//                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent2);
//                dismiss();
//            }
//        },new IntentFilter("DELETE_PINNED_AD"));

        return rootView;
    }

    private void saveImageDialog() {
        final Dialog d = new Dialog(this.mActivity);
        d.setTitle("Save to Device.");
        d.setContentView(R.layout.dialog991);
        Button b1 = d.findViewById(R.id.okBtn);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
                checkPermission();
            }
        });
        d.show();

    }

    private void checkPermission(){
        if (Build.VERSION.SDK_INT >= 23) {
            if (mContext.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                saveImageToDevice();
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(mActivity, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            saveImageToDevice();
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
            saveImageToDevice();
        }
    }

    private void saveImageToDevice2() {
        Advert mAdvert = Variables.adToBeViewed;
        Bitmap icon = mAdvert.getImageBitmap();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File f = new File(Environment.getRootDirectory()+ File.separator+"AdCafePins"+
                File.separator+mAdvert.getPushRefInAdminConsole()+".jpg");
        try {
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            Toast.makeText(mContext,"Image saved.",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(mContext,"Image save unsuccessful.",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }




    private void saveImageToDevice(){
        Bitmap imageToSave = mAdvert.getImageBitmap();
        String fileName = randomInt()+".jpg";
        File direct = new File(Environment.getExternalStorageDirectory() + "/AdCafePins");

        if (!direct.exists()) {
            File wallpaperDirectory = new File("/sdcard/AdCafePins/");
            wallpaperDirectory.mkdirs();
        }

        File file = new File(new File("/sdcard/AdCafePins/"), fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            Toast.makeText(mContext,"Image saved.",Toast.LENGTH_SHORT).show();
            setSavingImageName(fileName);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext,"Image save unsuccessful.",Toast.LENGTH_SHORT).show();
        }
    }

    public String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(8);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    public String randomInt(){
        Random rand = new Random();
        int max = 1000000000;
        int min = 1;
        int n = rand.nextInt(max) + min;
        return Integer.toString(n);
    }



    private void setSavingImageName(String image){
        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String dateInDays = Long.toString(mAdvert.getDateInDays());
        DatabaseReference mref = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(user).child(Constants.PINNED_AD_LIST).child(dateInDays).child(mAdvert.getPushId())
                .child("downloadImageName");
        mref.setValue(image);
    }


}
