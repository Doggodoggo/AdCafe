package com.bry.adcafe.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.models.AdvertiserLocation;
import com.bry.adcafe.models.myLatLng;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class ContactAdvertiserBottomsheet  extends BottomSheetDialogFragment {
    private Activity mActivity;
    private View mContentView;
    private Advert mAdvert;

    public void setActivity(Activity activity){
        this.mActivity = activity;
    }

    public void setAdvert(Advert ad){
        this.mAdvert = ad;
    }

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            switch (newState) {
                case BottomSheetBehavior.STATE_COLLAPSED:{
                    Log.d("BSB","collapsed") ;
                }
                case BottomSheetBehavior.STATE_SETTLING:{
                    Log.d("BSB","settling") ;
                }
                case BottomSheetBehavior.STATE_EXPANDED:{
                    Log.d("BSB","expanded") ;
                }
                case BottomSheetBehavior.STATE_HIDDEN: {
                    Log.d("BSB" , "hidden") ;
                    dismiss();
                }
                case BottomSheetBehavior.STATE_DRAGGING: {
                    Log.d("BSB","dragging") ;
                }
            }

        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            Log.d("BSB","sliding " + slideOffset ) ;
        }
    };

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View contentView = View.inflate(getContext(), R.layout.contact_advertiser_bottomsheet, null);
        dialog.setContentView(contentView);
        mContentView  = contentView;

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        params.setMargins(15,-15,15,15);
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if( behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
            ((BottomSheetBehavior) behavior).setHideable(false);
        }

        LinearLayout CallLayout = mContentView.findViewById(R.id.CallLayout);
        ImageButton callImg = mContentView.findViewById(R.id.phoneIcon);
        TextView callText = mContentView.findViewById(R.id.callTextView);

        LinearLayout websiteLayout = mContentView.findViewById(R.id.websiteLayout);
        ImageButton websiteIcom = mContentView.findViewById(R.id.websiteIcon);
        TextView visitWebsiteText = mContentView.findViewById(R.id.visitWebsiteText);

        LinearLayout LocationLayout = mContentView.findViewById(R.id.LocationLayout);
        ImageButton locationIcon = mContentView.findViewById(R.id.locationIcon);
        TextView locationText = mContentView.findViewById(R.id.locationText);


        if(!mAdvert.getAdvertiserPhoneNo().equals("none"))callText.setText("Call: "+mAdvert.getAdvertiserPhoneNo());

        if(mAdvert.getAdvertiserPhoneNo().equals("none")) CallLayout.setAlpha(0.6f);
        if(mAdvert.getWebsiteLink().equals("none")) websiteLayout.setAlpha(0.6f);
        if(mAdvert.getAdvertiserLocations().isEmpty()) LocationLayout.setAlpha(0.6f);

        CallLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPhone();
            }
        });

        websiteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openBrowser();
            }
        });

        LocationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMap();
            }
        });


        FrameLayout bottomSheet = dialog.getWindow().findViewById(android.support.design.R.id.design_bottom_sheet);
        bottomSheet.setBackgroundResource(R.drawable.dialog_bg);


    }

    private void openPhone(){
        if(mAdvert.getAdvertiserPhoneNo().equals("none")){
            Toast.makeText(mActivity,"They didn't add that.",Toast.LENGTH_SHORT).show();
        }else{
            Vibrator b = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
            b.vibrate(30);
            String number = mAdvert.getAdvertiserPhoneNo();
            String uri = "tel:" + number.trim();
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse(uri));
            startActivity(intent);
        }
    }

    private void openMap(){
        if(mAdvert.getAdvertiserLocations().isEmpty()){
            Toast.makeText(mActivity,"They didn't add that.",Toast.LENGTH_SHORT).show();
        }else{
            Vibrator b = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
            b.vibrate(30);
            AdvertiserLocation closestLocation = getClosestLocation();
            String thing = "geo:"+closestLocation.getMyLatLng().latitude+","+closestLocation.getMyLatLng().longitude;
            if(!closestLocation.getPlaceName().equals(""))thing = thing+"?q="+closestLocation.getPlaceName();
            Uri gmmIntentUri = Uri.parse(thing);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        }
    }

    private void openBrowser(){
        if(mAdvert.getWebsiteLink().equals("none")){
            Toast.makeText(mActivity,"They didn't add that.",Toast.LENGTH_SHORT).show();
        }else{
            Vibrator b = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
            b.vibrate(30);
            try {
                String url = Variables.getCurrentAdvert().getWebsiteLink();
                if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://" + url;
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(webIntent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(mActivity, "There's something wrong with the link", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private AdvertiserLocation getClosestLocation(){
        List<myLatLng> advertiserSetLong = new ArrayList<>();
        for(AdvertiserLocation loc:Variables.getCurrentAdvert().getAdvertiserLocations()){
            advertiserSetLong.add(loc.getMyLatLng());
        }
        if(Variables.usersLatLongs.isEmpty()){
            return Variables.getCurrentAdvert().getAdvertiserLocations().get(0);
        }else {
            myLatLng closestLatLng = Variables.getCurrentAdvert().getAdvertiserLocations().get(0).getMyLatLng();
            float closestDistance = 1000000000;
            for (LatLng latlngUser : Variables.usersLatLongs) {
                for(myLatLng latlngAdv: advertiserSetLong){
                    Location locAdv = new Location("");
                    locAdv.setLatitude(latlngAdv.latitude);
                    locAdv.setLongitude(latlngAdv.longitude);

                    Location locUser = new Location("");
                    locUser.setLatitude(latlngUser.latitude);
                    locUser.setLongitude(latlngUser.longitude);

                    float distance = Variables.distanceInMetersBetween2Points(locAdv,locUser);
                    if(distance<closestDistance){
                        closestDistance = distance;
                        closestLatLng = latlngAdv;
                    }
                }
            }
            for(AdvertiserLocation adLoc:Variables.getCurrentAdvert().getAdvertiserLocations()){
                if(adLoc.getMyLatLng().equals(closestLatLng)){
                    return adLoc;
                }
            }return Variables.getCurrentAdvert().getAdvertiserLocations().get(0);
        }
    }

}
