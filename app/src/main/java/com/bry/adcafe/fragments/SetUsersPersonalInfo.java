package com.bry.adcafe.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.services.TimeManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;

public class SetUsersPersonalInfo extends DialogFragment {
    private final String TAG = "myMapFragment";
    private Context mContext;
    private Activity mActivity;
    private View mRootView;

    private LinearLayout mainLayout;
    private Button okBtn;

    private LinearLayout mainLaout2;
    private Button okBtn1point5;

    private LinearLayout genderLayout;
    private RadioButton radioButtonFemale;
    private RadioButton radioButtonMale;
    private Button skip;
    private Button okBtn2;

    private LinearLayout ageLayout;
    private ImageButton openCalendar;
    private TextView setAgeTextView;
    private Button skip2;
    private Button okBtn3;

    private LinearLayout locationLayout;
    private ImageButton openMapImg;
    private Button openMap;
    private Button skip3;
    private Button okBtn4;

    private LinearLayout concludeLayout;
    private Button okBtn5;

    public void setfragcontext(Context context) {
        mContext = context;
    }

    public void setActivity(Activity activity) {
        this.mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_set_users_personal_info, container, false);

        mainLayout = rootView.findViewById(R.id.mainLayout);
        okBtn = rootView.findViewById(R.id.okBtn);

        mainLaout2 = rootView.findViewById(R.id.mainLayout2);
        okBtn1point5 = rootView.findViewById(R.id.okBtn1point5);

        genderLayout = rootView.findViewById(R.id.genderLayout);
        radioButtonFemale = rootView.findViewById(R.id.radioButtonFemale);
        radioButtonMale = rootView.findViewById(R.id.radioButtonMale);
        skip = rootView.findViewById(R.id.skip);
        okBtn2 = rootView.findViewById(R.id.okBtn2);

        ageLayout = rootView.findViewById(R.id.ageLayout);
        openCalendar = rootView.findViewById(R.id.calendar);
        setAgeTextView = rootView.findViewById(R.id.setAgeText);
        skip2 = rootView.findViewById(R.id.skip2);
        okBtn3 = rootView.findViewById(R.id.okBtn3);

        locationLayout = rootView.findViewById(R.id.locationLayout);
        openMapImg = rootView.findViewById(R.id.openMapImg);
        openMap = rootView.findViewById(R.id.openMap);
        skip3 = rootView.findViewById(R.id.skip3);
        okBtn4 = rootView.findViewById(R.id.okBtn4);

        concludeLayout= rootView.findViewById(R.id.concludeLayout);
        okBtn5= rootView.findViewById(R.id.okBtn5);

        loadFirstView();

        mRootView = rootView;
        return rootView;
    }

    private void loadFirstView() {
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainLayout.setVisibility(View.GONE);
                loadFirstView2();
            }
        });
    }

    private void loadFirstView2(){
        mainLaout2.setVisibility(View.VISIBLE);
        mainLaout2.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0)
                .setInterpolator(new FastOutSlowInInterpolator());
        okBtn1point5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainLaout2.setVisibility(View.GONE);
                loadSecondView();
            }
        });
    }


    private void loadSecondView() {
        genderLayout.setVisibility(View.VISIBLE);
        genderLayout.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0)
                .setInterpolator(new FastOutSlowInInterpolator());
        SharedPreferences prefs2 = mContext.getSharedPreferences(Constants.GENDER, MODE_PRIVATE);
        String gender = prefs2.getString(Constants.GENDER, "NULL");
        if(!gender.equals("NULL")&&gender.equals(Constants.MALE)) radioButtonMale.setChecked(true);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                genderLayout.setVisibility(View.GONE);
                loadThirdView();
            }
        });
        okBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(radioButtonFemale.isChecked()){
                    setGender(Constants.FEMALE);
                }else{
                    setGender(Constants.MALE);
                }
                genderLayout.setVisibility(View.GONE);
                loadThirdView();
            }
        });
    }

    private void setGender(String gender){
        SharedPreferences pref = mContext.getSharedPreferences(Constants.GENDER, MODE_PRIVATE);
        pref.edit().clear().putString(Constants.GENDER, gender).apply();

        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mref = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(user).child(Constants.GENDER);
        mref.setValue(gender);

        setConsentToTarget(true);
    }


    private void loadThirdView() {
        ageLayout.setVisibility(View.VISIBLE);
        ageLayout.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0)
                .setInterpolator(new FastOutSlowInInterpolator());
        SharedPreferences pref = mContext.getSharedPreferences(Constants.DATE_OF_BIRTH, MODE_PRIVATE);
        if(pref.getInt("year",0)!=0) {
            int day = pref.getInt("day", 0);
            int month = pref.getInt("month", 0);
            int year = pref.getInt("year", 0);
            setAgeTextView.setVisibility(View.VISIBLE);
            setAgeTextView.setText(String.format("Set birthday date: %d %s,%d", day, getMonthName_Abbr(month), year));
        }

        openCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });

        skip2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ageLayout.setVisibility(View.GONE);
                loadFouthView();
            }
        });
        okBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ageLayout.setVisibility(View.GONE);
                loadFouthView();
            }
        });
    }



    private void loadFouthView() {
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
        skip3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationLayout.setVisibility(View.GONE);
                loadFifthView();
            }
        });
        okBtn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationLayout.setVisibility(View.GONE);
                loadFifthView();
            }
        });
    }


    private void openMapSelector() {
        FragmentManager fm = getFragmentManager();
        myMapFragment mapFragment = new myMapFragment();
        mapFragment.setMenuVisibility(false);
        mapFragment.show(fm,"Edit User Location.");
        mapFragment.setfragcontext(mContext);
        mapFragment.setActivity(mActivity);
//        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("SHOW_MAP"));
    }

    private void loadFifthView() {
        concludeLayout.setVisibility(View.VISIBLE);
        concludeLayout.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0)
                .setInterpolator(new FastOutSlowInInterpolator());
        okBtn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    private void setConsentToTarget(Boolean bol){
        SharedPreferences pref = mContext.getSharedPreferences(Constants.CONSENT_TO_TARGET, MODE_PRIVATE);
        pref.edit().clear().putBoolean(Constants.CONSENT_TO_TARGET, bol).apply();

        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mref = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(user).child(Constants.CONSENT_TO_TARGET);
        mref.setValue(bol);
    }


    private String getMonthName_Abbr(int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month);
        SimpleDateFormat month_date = new SimpleDateFormat("MMM");
        return month_date.format(cal.getTime());
    }


    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        Context context;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int year = Integer.parseInt(TimeManager.getYear());
            int month = TimeManager.getMonthVal();
            int day = Integer.parseInt(TimeManager.getDay());
            context = getActivity().getApplicationContext();
            SharedPreferences pref = context.getSharedPreferences(Constants.DATE_OF_BIRTH, MODE_PRIVATE);
            if(pref.getInt("year",0)!=0) {
                day = pref.getInt("day", 0);
                month = pref.getInt("month", 0);
                year = pref.getInt("year", 0);
            }
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            setUsersAge(day,month,year);
        }

        private void setUsersAge(int birthDay,int birthMonth,int birthYear) {
            SharedPreferences pref = context.getSharedPreferences(Constants.DATE_OF_BIRTH, MODE_PRIVATE);
            pref.edit().putInt("day", birthDay).putInt("month",birthMonth).putInt("year",birthYear).apply();

            String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference mref = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                    .child(user).child(Constants.DATE_OF_BIRTH);
            mref.child("day").setValue(birthDay);
            mref.child("month").setValue(birthMonth);
            mref.child("year").setValue(birthYear);

            Toast.makeText(context,"Birthday set",Toast.LENGTH_SHORT).show();
        }

    }

}
