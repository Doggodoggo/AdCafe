package com.bry.adcafe.fragments;

import android.animation.Animator;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
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
import com.bry.adcafe.services.Utils;
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

    private LinearLayout mainLayout2;
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

    private LinearLayout deviceCategoryLayout;
    private ImageButton backBtn4;
    private TextView deviceCategoryUserCount;
    private RadioGroup selectDeviceRadioGroup;
    private RadioButton radioButtonLowEnd;
    private RadioButton radioButtonMidRange;
    private RadioButton radioButtonHighEnd;
    private Button skip4;
    private Button okBtn6;

    private LinearLayout categoriesLayout;
    private ImageButton backBtn5;
    private TextView multiCategoryExplanation;
    private TextView categoriesUserCount;
    private TextView categoriesNumber;
    private ImageButton setCategoryImg;
    private Button okBtn7;
    private Button skip5;


    private LinearLayout concludeLayout;
    private ImageButton backBtn3;
    private TextView confirmGender;
    private TextView confirmAge;
    private TextView confirmLocations;
    private TextView usersToBeReached;
    private TextView confirmDeviceCategory;
    private TextView confirmMultiCategory;
    private Button okBtn5;

    private List<TargetedUser> targetedUserData;
    private float NEG = -50;
    private String mCategory;

    private boolean hasSetSomeInfo = false;
    private final String skp = "SKIP.";
    private final String clr = "CLEAR.";
    private final String set = "SET.";


    public void setfragcontext(Context context) {
        mContext = context;
    }

    public void setActivity(Activity activity) {
        this.mActivity = activity;
    }

    public void setCategory(String cat){
        this.mCategory = cat;
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

        mainLayout2 = rootView.findViewById(R.id.mainLayout2);
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

        deviceCategoryLayout = rootView.findViewById(R.id.deviceCategoryLayout);
        backBtn4 = rootView.findViewById(R.id.backBtn4);
        deviceCategoryUserCount = rootView.findViewById(R.id.deviceCategoryUserCount);
        selectDeviceRadioGroup = rootView.findViewById(R.id.selectDeviceRadioGroup);
        radioButtonLowEnd = rootView.findViewById(R.id.radioButtonLowEnd);
        radioButtonMidRange = rootView.findViewById(R.id.radioButtonMidRange);
        radioButtonHighEnd = rootView.findViewById(R.id.radioButtonHighEnd);
        skip4 = rootView.findViewById(R.id.skip4);
        okBtn6 = rootView.findViewById(R.id.okBtn6);

        categoriesLayout = rootView.findViewById(R.id.categoriesLayout);
        backBtn5 = rootView.findViewById(R.id.backBtn5);
        multiCategoryExplanation = rootView.findViewById(R.id.multiCategoryExplanation);
        categoriesUserCount = rootView.findViewById(R.id.categoriesUserCount);
        categoriesNumber = rootView.findViewById(R.id.categoriesNumber);
        setCategoryImg = rootView.findViewById(R.id.setCategoryImg);
        okBtn7 = rootView.findViewById(R.id.okBtn7);
        skip5 = rootView.findViewById(R.id.skip5);


        concludeLayout= rootView.findViewById(R.id.concludeLayout);
        backBtn3 = rootView.findViewById(R.id.backBtn3);
        confirmGender = rootView.findViewById(R.id.confirmGender);
        confirmAge = rootView.findViewById(R.id.confirmAge);
        confirmLocations = rootView.findViewById(R.id.confirmLocations);
        confirmDeviceCategory = rootView.findViewById(R.id.confirmDeviceCategory);
        confirmMultiCategory = rootView.findViewById(R.id.confirmMultiCategory);
        usersToBeReached = rootView.findViewById(R.id.usersToBeReached);
        okBtn5= rootView.findViewById(R.id.okBtn5);



        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForSetLocations
                ,new IntentFilter(Constants.SET_LOCATION_DATA));

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForSetCategories,
                new IntentFilter(Constants.SET_MULTI_CATEGORY_DATA));

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        loadFirstView();
        return rootView;
    }

    private void loadFirstView() {
        if(Variables.genderTarget.equals("") && Variables.ageGroupTarget == null
                && Variables.locationTarget.isEmpty()) Variables.isOnlyTargetingKnownUsers = false;
        if(!Variables.isTargetingDataSet()) resetBtn.setAlpha(0.5f);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainLayout.animate().translationX(NEG).alpha(0f).setDuration(Constants.ANIMATION_DURATION)
                        .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mainLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
                loadFirstView2();
            }
        });
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Variables.isTargetingDataSet()) resetData();
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
        mainLayout2.setVisibility(View.VISIBLE);
        mainLayout2.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0)
                .setInterpolator(new LinearOutSlowInInterpolator());
        okBtn1point5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainLayout2.animate().translationX(NEG).alpha(0f).setDuration(Constants.ANIMATION_DURATION)
                        .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mainLayout2.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
                loadGenderView();
            }
        });
    }



    private void loadGenderView(){
        hasSetSomeInfo = false;
        genderLayout.setVisibility(View.VISIBLE);
        genderLayout.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0).alpha(1f)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                genderLayout.setTranslationX(0);
                genderLayout.setAlpha(1f);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        if(Variables.genderTarget.equals(Constants.MALE)){
            hasSetSomeInfo = true;
            radioButtonMale.setChecked(true);
        }
        else if(Variables.genderTarget.equals(Constants.FEMALE)){
            hasSetSomeInfo = true;
            radioButtonFemale.setChecked(true);
        }
        else hasSetSomeInfo = false;
        genderUserCount.setText(Html.fromHtml("Users that will be reached: <b>"+getNumberOfUsersAfterFiltering()
                +" Users.</b>"));
        if(hasSetSomeInfo){
            skip.setAlpha(1f);
            okBtn2.setText(set);
        }else{
            skip.setAlpha(0.5f);
            okBtn2.setText(skp);
        }
        selectGenderRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(radioButtonFemale.isChecked()){
                    hasSetSomeInfo = true;
                    skip.setAlpha(1f);
                    okBtn2.setText(set);
                    Variables.genderTarget = Constants.FEMALE;
                }else if(radioButtonMale.isChecked()){
                    hasSetSomeInfo = true;
                    skip.setAlpha(1f);
                    okBtn2.setText(set);
                    Variables.genderTarget = Constants.MALE;
                }
                genderUserCount.setText(Html.fromHtml("Users that will be reached: <b>"+
                        getNumberOfUsersAfterFiltering()+" User(s).</b>"));
            }
        });
        okBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!radioButtonMale.isChecked() && !radioButtonFemale.isChecked()){
                    Variables.genderTarget = "";
                    genderLayout.animate().translationX(NEG).alpha(0f).setDuration(Constants.ANIMATION_DURATION)
                            .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            genderLayout.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                    loadAgeGroupView();
                }else{
                    if(radioButtonFemale.isChecked()){
                        Variables.genderTarget = Constants.FEMALE;
                    }else if(radioButtonMale.isChecked()){
                        Variables.genderTarget = Constants.MALE;
                    }
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("IS_ADVERTISER_FILTERING"));
                    genderLayout.animate().translationX(NEG).alpha(0f).setDuration(Constants.ANIMATION_DURATION)
                            .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            genderLayout.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                    loadAgeGroupView();
                }
            }
        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasSetSomeInfo){
                    hasSetSomeInfo = false;
                    Variables.genderTarget = "";
                    selectGenderRadioGroup.clearCheck();
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("IS_ADVERTISER_FILTERING"));
                    skip.setAlpha(0.5f);
                    okBtn2.setText(skp);
                    genderUserCount.setText(Html.fromHtml("Users that will be reached: <b>"+
                            getNumberOfUsersAfterFiltering()+" User(s).</b>"));
                }
            }
        });
    }

    private void loadAgeGroupView() {
        hasSetSomeInfo = false;
        ageLayout.setVisibility(View.VISIBLE);
        ageLayout.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0).alpha(1f)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                ageLayout.setVisibility(View.VISIBLE);
                ageLayout.setTranslationX(0);
                ageLayout.setAlpha(1f);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        try{
            ageUserCount.setText(Html.fromHtml("Users that will be reached: <b>"+getNumberOfUsersAfterFiltering()
                    +" User(s).</b>"));
            if(Variables.ageGroupTarget!=null){
                hasSetSomeInfo = true;
                setAgeTextView.setText(String.format("Age Group set is: from %d to %d", Variables.ageGroupTarget.getStartingAge(), Variables.ageGroupTarget.getFinishAge()));
            }else{
                hasSetSomeInfo = false;
            }
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
                if(numberPicker2.getValue()<=numberPicker1.getValue()){
                    numberPicker2.setValue(numberPicker1.getValue()+1);
                }
                Variables.ageGroupTarget = new AgeGroup(numberPicker1.getValue(),numberPicker2.getValue());
                ageUserCount.setText(Html.fromHtml("Users that will be reached: <b>"+getNumberOfUsersAfterFiltering()
                        +" User(s).</b>"));
                setAgeTextView.setText(String.format("Age Group set is: from %d to %d", Variables.ageGroupTarget.getStartingAge(), Variables.ageGroupTarget.getFinishAge()));
                skip2.setAlpha(1f);
                okBtn3.setText(set);
                hasSetSomeInfo = true;
            }
        });

        numberPicker2.setOnScrollListener(new NumberPicker.OnScrollListener() {
            @Override
            public void onScrollStateChange(NumberPicker numberPicker, int i) {
                if(numberPicker2.getValue()<=numberPicker1.getValue()){
                    numberPicker1.setValue(numberPicker2.getValue()-1);
                }
                Variables.ageGroupTarget = new AgeGroup(numberPicker1.getValue(),numberPicker2.getValue());
                ageUserCount.setText(Html.fromHtml("Users that will be reached: <b>"+getNumberOfUsersAfterFiltering()
                        +" User(s).</b>"));
                setAgeTextView.setText(String.format("Age Group set is: from %d to %d", Variables.ageGroupTarget.getStartingAge(), Variables.ageGroupTarget.getFinishAge()));
                skip2.setAlpha(1f);
                okBtn3.setText(set);
                hasSetSomeInfo = true;
            }
        });
        if(Variables.ageGroupTarget!=null){
            numberPicker1.setValue(Variables.ageGroupTarget.getStartingAge());
            numberPicker2.setValue(Variables.ageGroupTarget.getFinishAge());
            skip2.setAlpha(1f);
            okBtn3.setText(set);
            hasSetSomeInfo = true;
        }else{
            skip2.setAlpha(0.5f);
            okBtn3.setText(skp);
            hasSetSomeInfo = false;
        }

        backBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ageLayout.animate().translationX(Utils.dpToPx(400)).setDuration(Constants.ANIMATION_DURATION)
                        .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        ageLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
                loadGenderView();
            }
        });

        skip2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasSetSomeInfo){
                    hasSetSomeInfo = false;
                    Variables.ageGroupTarget = null;
                    numberPicker1.setValue(18);
                    numberPicker2.setValue(19);
                    skip2.setAlpha(0.5f);
                    okBtn3.setText(skp);
                    setAgeTextView.setText("No age group set.");
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("IS_ADVERTISER_FILTERING"));
                    ageUserCount.setText(Html.fromHtml("Users that will be reached: <b>"+getNumberOfUsersAfterFiltering()
                            +" User(s).</b>"));
                }
            }
        });
        okBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!hasSetSomeInfo){
                    ageLayout.animate().translationX(NEG).alpha(0f).setDuration(Constants.ANIMATION_DURATION)
                            .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            ageLayout.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                    Variables.ageGroupTarget = null;
                    loadMapLayout();
                }else{
                    ageLayout.animate().translationX(NEG).alpha(0f).setDuration(Constants.ANIMATION_DURATION)
                            .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            ageLayout.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                    Variables.ageGroupTarget = new AgeGroup(numberPicker1.getValue(), numberPicker2.getValue());
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("IS_ADVERTISER_FILTERING"));
                    loadMapLayout();
                }
            }
        });

    }



    private void loadMapLayout() {
        hasSetSomeInfo = false;
        locationLayout.setVisibility(View.VISIBLE);
        locationLayout.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0).alpha(1f)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                locationLayout.setVisibility(View.VISIBLE);
                locationLayout.setTranslationX(0);
                locationLayout.setAlpha(1f);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
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
            hasSetSomeInfo = false;
            skip3.setAlpha(0.5f);
            okBtn4.setText(skp);
        }else{
            locationsNumber.setText(Html.fromHtml("Locations set: <b>"+ Variables.locationTarget.size()+" Locations.</b>"));
            hasSetSomeInfo = true;
            skip3.setAlpha(1f);
            okBtn4.setText(set);
        }

        backBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationLayout.animate().translationX(Utils.dpToPx(400)).setDuration(Constants.ANIMATION_DURATION)
                        .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        locationLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
                loadAgeGroupView();
            }
        });
        skip3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasSetSomeInfo){
                    hasSetSomeInfo = false;
                    Variables.locationTarget.clear();
                    skip3.setAlpha(0.5f);
                    okBtn4.setText(skp);
                    locationsNumber.setText(Html.fromHtml("Locations set:<b> None.</b>"));
                    locationUserCount.setText(Html.fromHtml("Users that will be reached: <b>"+
                            getNumberOfUsersAfterFiltering()+" Users.</b>"));
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("IS_ADVERTISER_FILTERING"));
                }
            }
        });
        okBtn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationLayout.animate().translationX(NEG).alpha(0f).setDuration(Constants.ANIMATION_DURATION)
                        .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        locationLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
                loadDeviceCategoryRangeSelector();
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
                    getNumberOfUsersAfterFiltering()+" User(s).</b>"));
            if(Variables.locationTarget.isEmpty()){
                hasSetSomeInfo = false;
                skip3.setAlpha(0.5f);
                okBtn4.setText(skp);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("IS_ADVERTISER_FILTERING"));
                locationsNumber.setText(Html.fromHtml("Locations set:<b> None.</b>"));
            }else{
                hasSetSomeInfo = true;
                skip3.setAlpha(1f);
                okBtn4.setText(set);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("IS_ADVERTISER_FILTERING"));
                locationsNumber.setText(Html.fromHtml("Locations set: <b>"+ Variables.locationTarget.size()+" Locations.</b>"));
            }
        }
    };

    private void loadDeviceCategoryRangeSelector(){
        hasSetSomeInfo = false;
        deviceCategoryLayout.setVisibility(View.VISIBLE);
        deviceCategoryLayout.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0).alpha(1f)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                deviceCategoryLayout.setAlpha(1f);
                deviceCategoryLayout.setTranslationX(0);
                deviceCategoryLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        if(Variables.deviceRangeCategory.equals(Constants.HIGH_END_DEVICE)){
            hasSetSomeInfo = true;
            skip4.setAlpha(1f);
            okBtn6.setText(set);
            radioButtonHighEnd.setChecked(true);
        }
        else if(Variables.deviceRangeCategory.equals(Constants.MID_RANGE_DEVICE)){
            hasSetSomeInfo = true;
            skip4.setAlpha(1f);
            okBtn6.setText(set);
            radioButtonMidRange.setChecked(true);
        }
        else if(Variables.deviceRangeCategory.equals(Constants.LOW_END_DEVICE)){
            hasSetSomeInfo = true;
            skip4.setAlpha(1f);
            okBtn6.setText(set);
            radioButtonLowEnd.setChecked(true);
        }else{
            skip4.setAlpha(0.5f);
            okBtn6.setText(skp);
            hasSetSomeInfo = false;
        }

        deviceCategoryUserCount.setText(Html.fromHtml("Users that will be reached: <b>"+getNumberOfUsersAfterFiltering()
                +" User(s).</b>"));
        selectDeviceRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(radioButtonHighEnd.isChecked()){
                    Variables.deviceRangeCategory = Constants.HIGH_END_DEVICE;
                    hasSetSomeInfo = true;
                    skip4.setAlpha(1f);
                    okBtn6.setText(set);
                }else if(radioButtonMidRange.isChecked()){
                    Variables.deviceRangeCategory = Constants.MID_RANGE_DEVICE;
                    hasSetSomeInfo = true;
                    skip4.setAlpha(1f);
                    okBtn6.setText(set);
                }else if(radioButtonLowEnd.isChecked()){
                    Variables.deviceRangeCategory = Constants.LOW_END_DEVICE;
                    hasSetSomeInfo = true;
                    skip4.setAlpha(1f);
                    okBtn6.setText(set);
                }
                deviceCategoryUserCount.setText(Html.fromHtml("Users that will be reached: <b>"+ getNumberOfUsersAfterFiltering()+" Users.</b>"));
            }
        });

        backBtn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deviceCategoryLayout.animate().translationX(Utils.dpToPx(400)).setDuration(Constants.ANIMATION_DURATION)
                        .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        deviceCategoryLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
                loadMapLayout();
            }
        });

        skip4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasSetSomeInfo){
                    hasSetSomeInfo = false;
                    skip4.setAlpha(0.5f);
                    okBtn6.setText(skp);
                    Variables.deviceRangeCategory = "";
                    deviceCategoryUserCount.setText(Html.fromHtml("Users that will be reached: <b>"+ getNumberOfUsersAfterFiltering()+" Users.</b>"));
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("IS_ADVERTISER_FILTERING"));
                    selectDeviceRadioGroup.clearCheck();
                }
            }
        });

        okBtn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!radioButtonHighEnd.isChecked() && !radioButtonMidRange.isChecked()&& !radioButtonLowEnd.isChecked()){
                    Variables.deviceRangeCategory = "";
                    deviceCategoryLayout.animate().translationX(NEG).alpha(0f).setDuration(Constants.ANIMATION_DURATION)
                            .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            deviceCategoryLayout.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                    loadSelectOtherCategories();
                }else{
                    if(radioButtonHighEnd.isChecked()){
                        Variables.deviceRangeCategory = Constants.HIGH_END_DEVICE;
                    }else if(radioButtonMidRange.isChecked()){
                        Variables.deviceRangeCategory = Constants.MID_RANGE_DEVICE;
                    }else if(radioButtonLowEnd.isChecked()){
                        Variables.deviceRangeCategory = Constants.LOW_END_DEVICE;
                    }
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("IS_ADVERTISER_FILTERING"));
                    deviceCategoryLayout.animate().translationX(NEG).alpha(0f).setDuration(Constants.ANIMATION_DURATION)
                            .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            deviceCategoryLayout.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                    loadSelectOtherCategories();
                }
            }
        });
    }

    private void loadSelectOtherCategories(){
        hasSetSomeInfo = false;
        categoriesLayout.setVisibility(View.VISIBLE);
        categoriesLayout.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0).alpha(1f)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                categoriesLayout.setVisibility(View.VISIBLE);
                categoriesLayout.setAlpha(1f);
                categoriesLayout.setTranslationX(0);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        categoriesUserCount.setText(Html.fromHtml("Users that will be reached: <b>"+
                getNumberOfUsersAfterFiltering()+" User(s).</b>"));
        if(Variables.targetCategoryList.isEmpty()){
            hasSetSomeInfo = false;
            skip5.setAlpha(0.5f);
            okBtn7.setText(skp);
            categoriesNumber.setText(Html.fromHtml("Extra categories set:<b> None.</b>"));
        }else{
            hasSetSomeInfo = true;
            skip5.setAlpha(1f);
            okBtn7.setText(set);
            categoriesNumber.setText(Html.fromHtml("Extra categories set: <b>"+
                    Variables.targetCategoryList.size()+" Location(s).</b>"));
        }

        backBtn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoriesLayout.animate().translationX(Utils.dpToPx(400)).setDuration(Constants.ANIMATION_DURATION)
                        .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        categoriesLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
                loadDeviceCategoryRangeSelector();

            }
        });

        setCategoryImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadCategoryPicker();
            }
        });

        skip5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasSetSomeInfo){
                    hasSetSomeInfo = false;
                    skip5.setAlpha(0.5f);
                    okBtn7.setText(skp);
                    Variables.targetCategoryList.clear();
                    categoriesNumber.setText(Html.fromHtml("Extra categories set:<b> None.</b>"));
                    categoriesUserCount.setText(Html.fromHtml("Users that will be reached: <b>"+
                            getNumberOfUsersAfterFiltering()+" User(s).</b>"));
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("IS_ADVERTISER_FILTERING"));
                }
            }
        });

        okBtn7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoriesLayout.animate().translationX(NEG).alpha(0f).setDuration(Constants.ANIMATION_DURATION)
                        .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        categoriesLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
                loadConfirmDetailsView();
            }
        });
    }

    private void loadCategoryPicker(){
        FragmentManager fm = getFragmentManager();
        SelectOtherCategoriesAdvertiser cat = new SelectOtherCategoriesAdvertiser();
        cat.setMenuVisibility(false);
        cat.show(fm,"Add Other Categories.");
        cat.setfragcontext(mContext);
        cat.setCategory(mCategory);
    }

    private BroadcastReceiver mMessageReceiverForSetCategories = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            categoriesUserCount.setText(Html.fromHtml("Users that will be reached: <b>"+
                    getNumberOfUsersAfterFiltering()+" User(s).</b>"));
            if(Variables.targetCategoryList.isEmpty()){
                hasSetSomeInfo = false;
                skip5.setAlpha(0.5f);
                okBtn7.setText(skp);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("IS_ADVERTISER_FILTERING"));
                categoriesNumber.setText(Html.fromHtml("Extra categories set:<b> None.</b>"));
            }else{
                hasSetSomeInfo = true;
                skip5.setAlpha(1f);
                okBtn7.setText(set);
                if(Variables.targetCategoryList.size()==1){
                    categoriesNumber.setText(Html.fromHtml("Extra categories set: <b>"+
                            Variables.targetCategoryList.size()+" Category.</b>"));
                }else{
                    categoriesNumber.setText(Html.fromHtml("Extra categories set: <b>"+
                            Variables.targetCategoryList.size()+" Categories.</b>"));
                }
            }
        }
    };



    private void loadConfirmDetailsView(){
        concludeLayout.setVisibility(View.VISIBLE);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("IS_ADVERTISER_FILTERING"));
        concludeLayout.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0).alpha(1f)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                concludeLayout.setAlpha(1f);
                concludeLayout.setTranslationX(0);
                concludeLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        if(Variables.genderTarget!=null){
            if(Variables.genderTarget.equals(""))confirmGender.setText("No preferred Gender/Sex set.");
            else confirmGender.setText(Html.fromHtml("Set gender/sex target: <b>"+Variables.genderTarget+".</b>"));
        }else{
            confirmGender.setText("No preferred Gender/Sex set.");
        }
        if(Variables.ageGroupTarget!=null){
            confirmAge.setText(Html.fromHtml("Age Group set is: from <b>"+Variables.ageGroupTarget.getStartingAge()
                    +" to "+Variables.ageGroupTarget.getFinishAge()+".</b>"));
        }else{
         confirmAge.setText("No preferred Age Group set.");
        }
        if(!Variables.locationTarget.isEmpty()){
            if(Variables.locationTarget.size()==1) confirmLocations.setText(Html.fromHtml("Locations set: <b>1 Location.</b>"));
            else confirmLocations.setText(Html.fromHtml("Locations set: <b>"+Variables.locationTarget.size()+" Locations.</b>"));
        }else{
            confirmLocations.setText("No preferred Locations set.");
        }
        if(!Variables.deviceRangeCategory.equals("")){
            if(Variables.deviceRangeCategory.equals(Constants.HIGH_END_DEVICE))
                confirmDeviceCategory.setText(Html.fromHtml("<b>High-end</b> device users targeted."));
            else if(Variables.deviceRangeCategory.equals(Constants.MID_RANGE_DEVICE))
                confirmDeviceCategory.setText(Html.fromHtml("<b>Mid-range</b> device users targeted."));
            else if(Variables.deviceRangeCategory.equals(Constants.LOW_END_DEVICE))
                confirmDeviceCategory.setText(Html.fromHtml("<b>Low-end</b> device users targeted."));
        }else{
            confirmDeviceCategory.setText("No preferred device category set.");
        }

        if(Variables.targetCategoryList.isEmpty()){
            confirmMultiCategory.setText("No extra categories set.");
        }else{
            if(Variables.targetCategoryList.size()==1){
                confirmMultiCategory.setText(Html.fromHtml("Extra category set: <b>"+
                        Variables.targetCategoryList.size()+" category.</b>"));
            }else{
                confirmMultiCategory.setText(Html.fromHtml("Extra categories set: <b>"+
                        Variables.targetCategoryList.size()+" categies.</b>"));
            }
        }

        long noOfUsersThatWillBeReached = getNumberOfUsersAfterFiltering();
        usersToBeReached.setText(Html.fromHtml("Users that will be reached: <b>"+noOfUsersThatWillBeReached+" User(s).</b>"));

        backBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                concludeLayout.animate().translationX(Utils.dpToPx(400)).setDuration(Constants.ANIMATION_DURATION)
                        .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        concludeLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
                loadSelectOtherCategories();
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
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForSetCategories);
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
        if(!Variables.deviceRangeCategory.equals("")){
            for(TargetedUser user:targetedUserData){
                if(!user.getDeviceCategory().equals(Variables.deviceRangeCategory)){
                    if(usersQualified.contains(user)) usersQualified.remove(user);
                }
            }
        }
        if(!Variables.targetCategoryList.isEmpty()){
            for(TargetedUser user: targetedUserData){
                for(String subscription:Variables.targetCategoryList){
                    if(!user.getSubscriptions().contains(subscription)){
                        if(usersQualified.contains(user)) usersQualified.remove(user);
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
        if(!Variables.deviceRangeCategory.equals("")){
            for(TargetedUser user:targetedUserData){
                if(!user.getDeviceCategory().equals(Variables.deviceRangeCategory)){
                    if(usersQualified.contains(user)) usersQualified.remove(user);
                }
            }
        }

        if(!Variables.targetCategoryList.isEmpty()){
            for(TargetedUser user: targetedUserData){
                for(String subscription:Variables.targetCategoryList){
                    if(!user.getSubscriptions().contains(subscription)){
                        if(usersQualified.contains(user)) usersQualified.remove(user);
                    }
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
