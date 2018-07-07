package com.bry.adcafe.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.AgeGroup;
import com.bry.adcafe.models.TargetedUser;
import com.bry.adcafe.services.TimeManager;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SetAdvertiserTargetInfoFragment extends DialogFragment {
    private final String TAG = "AdvTargetInfoFrag";
    private Context mContext;
    private Activity mActivity;

    private LinearLayout mainLayout;
    private Button resetBtn;
    private Button okBtn;

    private LinearLayout mainLaout2;
    private Button okBtn1point5;

    private LinearLayout genderLayout;
    private RadioButton radioButtonFemale;
    private RadioButton radioButtonMale;
    private TextView genderUserCount;
    private RadioGroup selectGenderRadioGroup;
    private Button skip;
    private Button okBtn2;

    private LinearLayout ageLayout;
    private ImageButton backBtn1;
    private NumberPicker numberPicker1;
    private NumberPicker numberPicker2;
    private TextView ageUserCount;
    private TextView setAgeTextView;
    private Button skip2;
    private Button okBtn3;

    private LinearLayout locationLayout;
    private ImageButton backBtn2;
    private TextView locationUserCount;
    private TextView locationsNumber;
    private ImageButton openMapImg;
    private Button openMap;
    private Button skip3;
    private Button okBtn4;

    private LinearLayout concludeLayout;
    private ImageButton backBtn3;
    private TextView confirmGender;
    private TextView confirmAge;
    private TextView confirmLocations;
    private TextView usersToBeReached;
    private Button okBtn5;

    private List<TargetedUser> targetedUserData;
    private float NEG = -800f;


    public void setfragcontext(Context context) {
        mContext = context;
    }

    public void setActivity(Activity activity) {
        this.mActivity = activity;
    }

    public void setUsersThatCanBeReached(List<TargetedUser> users){
        this.targetedUserData = users;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.set_advertiser_target_info, container, false);

        mainLayout = rootView.findViewById(R.id.mainLayout);
        resetBtn = rootView.findViewById(R.id.resetBtn);
        okBtn = rootView.findViewById(R.id.okBtn);

        mainLaout2 = rootView.findViewById(R.id.mainLayout2);
        okBtn1point5 = rootView.findViewById(R.id.okBtn1point5);

        genderLayout = rootView.findViewById(R.id.genderLayout);
        selectGenderRadioGroup = rootView.findViewById(R.id.selectGenderRadioGroup);
        radioButtonFemale = rootView.findViewById(R.id.radioButtonFemale);
        radioButtonMale = rootView.findViewById(R.id.radioButtonMale);
        genderUserCount = rootView.findViewById(R.id.genderUserCount);
        skip = rootView.findViewById(R.id.skip);
        okBtn2 = rootView.findViewById(R.id.okBtn2);

        ageLayout = rootView.findViewById(R.id.ageLayout);
        backBtn1 = rootView.findViewById(R.id.backBtn1);
        numberPicker1 = rootView.findViewById(R.id.numberPicker1);
        numberPicker2 = rootView.findViewById(R.id.numberPicker2);
        ageUserCount = rootView.findViewById(R.id.ageUserCount);
        setAgeTextView = rootView.findViewById(R.id.setAgeText);
        skip2 = rootView.findViewById(R.id.skip2);
        okBtn3 = rootView.findViewById(R.id.okBtn3);

        locationLayout = rootView.findViewById(R.id.locationLayout);
        backBtn2 = rootView.findViewById(R.id.backBtn2);
        locationUserCount = rootView.findViewById(R.id.locationUserCount);
        locationsNumber = rootView.findViewById(R.id.locationsNumber);
        openMapImg = rootView.findViewById(R.id.openMapImg);
        openMap = rootView.findViewById(R.id.openMap);
        skip3 = rootView.findViewById(R.id.skip3);
        okBtn4 = rootView.findViewById(R.id.okBtn4);

        concludeLayout= rootView.findViewById(R.id.concludeLayout);
        backBtn3 = rootView.findViewById(R.id.backBtn3);
        confirmGender = rootView.findViewById(R.id.confirmGender);
        confirmAge = rootView.findViewById(R.id.confirmAge);
        confirmLocations = rootView.findViewById(R.id.confirmLocations);
        okBtn5= rootView.findViewById(R.id.okBtn5);

        usersToBeReached = rootView.findViewById(R.id.usersToBeReached);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForSetLocations
                ,new IntentFilter(Constants.SET_LOCATION_DATA));

        loadFirstView();
        return rootView;
    }

    private void loadFirstView() {
        if(!Variables.isTargeting) resetBtn.setAlpha(0.5f);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainLayout.setVisibility(View.GONE);
                loadFirstView2();
            }
        });
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Variables.isTargeting) resetData();
            }
        });
    }

    private void resetData(){
        Variables.resetAdvertiserTargetingData();
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("IS_ADVERTISER_FILTERING"));
        Toast.makeText(mContext,"Data cleared.",Toast.LENGTH_SHORT).show();
        resetBtn.setAlpha(0.5f);
    }

    private void loadFirstView2(){
        mainLaout2.setVisibility(View.VISIBLE);
        mainLaout2.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0)
                .setInterpolator(new FastOutSlowInInterpolator());
        okBtn1point5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainLaout2.setVisibility(View.GONE);
                loadGenderView();
            }
        });
    }



    private void loadGenderView(){
        genderLayout.setVisibility(View.VISIBLE);
        genderLayout.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0)
                .setInterpolator(new FastOutSlowInInterpolator());
        if(Variables.genderTarget.equals(Constants.MALE)) radioButtonMale.setChecked(true);
        else if(Variables.genderTarget.equals(Constants.FEMALE)) radioButtonFemale.setChecked(true);
        genderUserCount.setText(Html.fromHtml("Users that will be reached: <b>"+getNumberOfUsersAfterFiltering()
                +" Users.</b>"));
        selectGenderRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(radioButtonFemale.isChecked()){
                    Variables.genderTarget = Constants.FEMALE;
                }else if(radioButtonMale.isChecked()){
                    Variables.genderTarget = Constants.MALE;
                }
                genderUserCount.setText(Html.fromHtml("Users that will be reached: <b>"+
                        getNumberOfUsersAfterFiltering()+" Users.</b>"));
            }
        });
        okBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!radioButtonMale.isChecked() && !radioButtonFemale.isChecked()){
                    Toast.makeText(mContext,"Pick one!",Toast.LENGTH_SHORT).show();
                }else{
                    if(radioButtonFemale.isChecked()){
                        Variables.genderTarget = Constants.FEMALE;
                        Variables.isTargeting = true;
                    }else if(radioButtonMale.isChecked()){
                        Variables.genderTarget = Constants.MALE;
                        Variables.isTargeting = true;
                    }
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("IS_ADVERTISER_FILTERING"));
                    genderLayout.setVisibility(View.GONE);
                    genderLayout.setTranslationX(NEG);
                    loadAgeGroupView();
                }
            }
        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Variables.genderTarget = "";
                if(Variables.genderTarget.equals("")&&Variables.ageGroupTarget==null&&Variables.locationTarget.isEmpty()){
                    Variables.isTargeting = false;
                }
                genderLayout.setVisibility(View.GONE);
                genderLayout.setTranslationX(NEG);
                loadAgeGroupView();
            }
        });
    }

    private void loadAgeGroupView() {
        ageLayout.setVisibility(View.VISIBLE);
        ageLayout.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0)
                .setInterpolator(new FastOutSlowInInterpolator());
        try{
            ageUserCount.setText(Html.fromHtml("Users that will be reached: <b>"+getNumberOfUsersAfterFiltering()
                    +" Users.</b>"));
            if(Variables.ageGroupTarget!=null) setAgeTextView.setText(String.format("Age Group set is: from %d to %d",
                    Variables.ageGroupTarget.getStartingAge(), Variables.ageGroupTarget.getFinishAge()));
        }catch (Exception e){
            e.printStackTrace();
        }
        numberPicker1.setWrapSelectorWheel(false);
        numberPicker2.setWrapSelectorWheel(false);
        numberPicker1.setMinValue(18);
        numberPicker2.setMinValue(19);
        numberPicker1.setMaxValue(64);
        numberPicker2.setMaxValue(65);

        numberPicker1.setOnScrollListener(new NumberPicker.OnScrollListener() {
            @Override
            public void onScrollStateChange(NumberPicker numberPicker, int i) {
                Variables.ageGroupTarget = new AgeGroup(numberPicker1.getValue(),numberPicker2.getValue());
                ageUserCount.setText(Html.fromHtml("Users that will be reached: <b>"+getNumberOfUsersAfterFiltering()
                        +" Users.</b>"));
            }
        });

        numberPicker2.setOnScrollListener(new NumberPicker.OnScrollListener() {
            @Override
            public void onScrollStateChange(NumberPicker numberPicker, int i) {
                Variables.ageGroupTarget = new AgeGroup(numberPicker1.getValue(),numberPicker2.getValue());
                ageUserCount.setText(Html.fromHtml("Users that will be reached: <b>"+getNumberOfUsersAfterFiltering()
                        +" Users.</b>"));
            }
        });
        if(Variables.ageGroupTarget!=null){
            numberPicker1.setValue(Variables.ageGroupTarget.getStartingAge());
            numberPicker2.setValue(Variables.ageGroupTarget.getFinishAge());
        }

        backBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ageLayout.setTranslationX(600);
                ageLayout.setVisibility(View.GONE);
                loadGenderView();
            }
        });

        skip2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ageLayout.setVisibility(View.GONE);
                ageLayout.setTranslationX(NEG);
                Variables.ageGroupTarget = null;
                loadMapLayout();
            }
        });
        okBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ageLayout.setVisibility(View.GONE);
                ageLayout.setTranslationX(NEG);
                Variables.ageGroupTarget = new AgeGroup(numberPicker1.getValue(),numberPicker2.getValue());
                Variables.isTargeting = true;
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("IS_ADVERTISER_FILTERING"));
                loadMapLayout();
            }
        });

    }



    private void loadMapLayout() {
        locationLayout.setVisibility(View.VISIBLE);
        locationLayout.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0)
                .setInterpolator(new FastOutSlowInInterpolator());
        openMapImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMapSelector();
            }
        });
        openMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMapImg.performClick();
            }
        });
        locationUserCount.setText(Html.fromHtml("Users that will be reached: <b>"+
                getNumberOfUsersAfterFiltering()+" Users.</b>"));
        if(Variables.locationTarget.isEmpty()){
            locationsNumber.setText(Html.fromHtml("Locations set:<b> None.</b>"));
        }else{
            locationsNumber.setText(Html.fromHtml("Locations set: <b>"+ Variables.locationTarget.size()+" Locations.</b>"));
        }

        backBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationLayout.setVisibility(View.GONE);
                locationLayout.setTranslationX(600);
                loadAgeGroupView();
            }
        });
        skip3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationLayout.setVisibility(View.GONE);
                locationLayout.setTranslationX(NEG);
                loadConfirmDetailsView();
            }
        });
        okBtn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationLayout.setVisibility(View.GONE);
                locationLayout.setTranslationX(NEG);
                loadConfirmDetailsView();
            }
        });
    }

    private void openMapSelector(){
        FragmentManager fm = getFragmentManager();
        AdvertiserMapFragment mapFragment = new AdvertiserMapFragment();
        mapFragment.setMenuVisibility(false);
        mapFragment.show(fm,"Edit Target Location.");
        mapFragment.setfragcontext(mContext);
        mapFragment.setUsersThatCanBeReached(getTargetedUserDataAfterFiltering());
        mapFragment.setActivity(mActivity);
    }

    private BroadcastReceiver mMessageReceiverForSetLocations = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            locationUserCount.setText(Html.fromHtml("Users that will be reached: <b>"+
                    getNumberOfUsersAfterFiltering()+" Users.</b>"));
            if(Variables.locationTarget.isEmpty()){
                locationsNumber.setText(Html.fromHtml("Locations set:<b> None.</b>"));
            }else{
                locationsNumber.setText(Html.fromHtml("Locations set: <b>"+ Variables.locationTarget.size()+" Locations.</b>"));
            }
        }
    };



    private void loadConfirmDetailsView(){
        concludeLayout.setVisibility(View.VISIBLE);
        concludeLayout.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0)
                .setInterpolator(new FastOutSlowInInterpolator());
        if(Variables.genderTarget!=null){
            if(Variables.genderTarget.equals(""))confirmGender.setText("No preferred Gender/Sex set.");
            else confirmGender.setText(String.format("Set gender/sex target: %s.", Variables.genderTarget));
        }else{
            confirmGender.setText("No preferred Gender/Sex set.");
        }
        if(Variables.ageGroupTarget!=null){
            confirmAge.setText(String.format("Age Group set is: from %d to %d", Variables.ageGroupTarget.getStartingAge(), Variables.ageGroupTarget.getFinishAge()));
        }else{
         confirmAge.setText("No preferred Age Group set.");
        }
        if(!Variables.locationTarget.isEmpty()){
            if(Variables.locationTarget.size()==1) confirmLocations.setText("Locations set: 1 Location.");
            else confirmLocations.setText("Locations set: "+Variables.locationTarget.size()+" Locations.");
        }else{
            confirmLocations.setText("No preferred Locations set.");
        }

        long noOfUsersThatWillBeReached = getNumberOfUsersAfterFiltering();
        usersToBeReached.setText("Users that will be reached: "+noOfUsersThatWillBeReached+" Users");

        backBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                concludeLayout.setVisibility(View.GONE);
                concludeLayout.setTranslationX(600);
                loadMapLayout();
            }
        });
        okBtn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    @Override
    public void dismiss(){
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("IS_ADVERTISER_FILTERING"));
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForSetLocations);
        super.dismiss();
    }


    private long getNumberOfUsersAfterFiltering(){
        List<TargetedUser> usersQualified = new ArrayList<>(targetedUserData);
        Log.d(TAG,"No of users in targetedUserdDataList: "+usersQualified.size());
        for(TargetedUser user: usersQualified){
            Log.d(TAG,"User id: "+user.getUserId());
            Log.d(TAG,"User birthday: "+user.getBirthday()+":"+user.getBirthMonth()+":"+user.getBirthYear());
            Log.d(TAG,"User gender: "+user.getGender());
            Log.d(TAG,"Cluster id: "+user.getClusterId());
        }
        if(!Variables.genderTarget.equals("")){
            for(TargetedUser user: targetedUserData){
                if(!user.getGender().equals(Variables.genderTarget)){
                    if(usersQualified.contains(user)){
                        Log.d(TAG,"Removing user "+user.getUserId()+" since, gender required is "+Variables.genderTarget+
                        " while users gender is: "+user.getGender());
                        usersQualified.remove(user);
                    }
                }
            }
        }
        if(Variables.ageGroupTarget!=null){
            for(TargetedUser user:targetedUserData){
                if(user.getBirthday()!=0 || user.getBirthYear()!=0) {
                    Integer userAge = getAge(user.getBirthYear(), user.getBirthMonth(), user.getBirthday());
                    Log.d(TAG,"User age: "+userAge);
                    if(userAge < Variables.ageGroupTarget.getStartingAge() || userAge > Variables.ageGroupTarget.getFinishAge()){
                        if(usersQualified.contains(user)) usersQualified.remove(user);
                    }
                }else{
                    if(usersQualified.contains(user)) usersQualified.remove(user);
                }
            }
        }
        if(!Variables.locationTarget.isEmpty()){
            for(TargetedUser user:targetedUserData){
                if(locationContained(user.getUserLocations())==0){
                    if(usersQualified.contains(user)) {
                        usersQualified.remove(user);
                        Log.d(TAG, "Removing user: " + user.getUserId() + " since they are not situated near any location");
                    }
                }
            }
        }
        return (long)usersQualified.size();
    }

    private List<TargetedUser> getTargetedUserDataAfterFiltering(){
        List<TargetedUser> usersQualified = new ArrayList<>(targetedUserData);
        Log.d(TAG,"No of users in targetedUserdDataList: "+usersQualified.size());
        for(TargetedUser user: usersQualified){
            Log.d(TAG,"User id: "+user.getUserId());
            Log.d(TAG,"User birthday: "+user.getBirthday()+":"+user.getBirthMonth()+":"+user.getBirthYear());
            Log.d(TAG,"User gender: "+user.getGender());
            Log.d(TAG,"Cluster id: "+user.getClusterId());
        }
        if(!Variables.genderTarget.equals("")){
            for(TargetedUser user: targetedUserData){
                if(!user.getGender().equals(Variables.genderTarget)){
                    if(usersQualified.contains(user)){
                        Log.d(TAG,"Removing user "+user.getUserId()+" since, gender required is "+Variables.genderTarget+
                                " while users gender is: "+user.getGender());
                        usersQualified.remove(user);
                    }
                }
            }
        }
        if(Variables.ageGroupTarget!=null){
            for(TargetedUser user:targetedUserData){
                if(user.getBirthday()!=0 || user.getBirthYear()!=0) {
                    Integer userAge = getAge(user.getBirthYear(), user.getBirthMonth(), user.getBirthday());
                    Log.d(TAG,"User age: "+userAge);
                    if(userAge < Variables.ageGroupTarget.getStartingAge() || userAge > Variables.ageGroupTarget.getFinishAge()){
                        if(usersQualified.contains(user)) usersQualified.remove(user);
                    }
                }else{
                    if(usersQualified.contains(user)) usersQualified.remove(user);
                }
            }
        }
//        if(!Variables.locationTarget.isEmpty()){
//            for(TargetedUser user:targetedUserData){
//                if(locationContained(user.getUserLocations())==0){
//                    if(usersQualified.contains(user)) {
//                        usersQualified.remove(user);
//                        Log.d(TAG, "Removing user: " + user.getUserId() + " since they are not situated near any location");
//                    }
//                }
//            }
//        }
        return usersQualified;
    }

    private Integer getAge(int year, int month, int day){
        Calendar dob = Calendar.getInstance();
        Calendar today = TimeManager.getCal();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }
        return age;
    }

    private int locationContained(List<LatLng> checkLocalLst){
        int locations = 0;
        for(LatLng latlngUser : checkLocalLst){
            for(LatLng latlngAdv: Variables.locationTarget){
                Location locAdv = new Location("");
                locAdv.setLatitude(latlngAdv.latitude);
                locAdv.setLongitude(latlngAdv.longitude);

                Location locUser = new Location("");
                locUser.setLatitude(latlngUser.latitude);
                locUser.setLongitude(latlngUser.longitude);
                float distance = Variables.distanceInMetersBetween2Points(locAdv,locUser);
                if(distance<=Constants.MAX_DISTANCE_IN_METERS) locations++;
            }
        }
        return locations;
    }

}
