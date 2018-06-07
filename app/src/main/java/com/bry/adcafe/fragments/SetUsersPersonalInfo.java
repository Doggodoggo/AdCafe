package com.bry.adcafe.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.content.Context.MODE_PRIVATE;

public class SetUsersPersonalInfo extends DialogFragment {
    private final String TAG = "myMapFragment";
    private Context mContext;
    private Activity mActivity;
    private View mRootView;

    private LinearLayout mainLayout;
    private Button okBtn;

    private LinearLayout genderLayout;
    private RadioButton radioButtonFemale;
    private RadioButton radioButtonMale;
    private Button skip;
    private Button okBtn2;

    private LinearLayout ageLayout;
    private NumberPicker numberPickerAge;
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

        genderLayout = rootView.findViewById(R.id.genderLayout);
        radioButtonFemale = rootView.findViewById(R.id.radioButtonFemale);
        radioButtonMale = rootView.findViewById(R.id.radioButtonMale);
        skip = rootView.findViewById(R.id.skip);
        okBtn2 = rootView.findViewById(R.id.okBtn2);

        ageLayout = rootView.findViewById(R.id.ageLayout);
        numberPickerAge = rootView.findViewById(R.id.numberPickerAge);
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
                loadSecondView();
            }
        });
    }


    private void loadSecondView() {
        genderLayout.setVisibility(View.VISIBLE);
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
    }


    private void loadThirdView() {
        ageLayout.setVisibility(View.VISIBLE);
        numberPickerAge.setMaxValue(65);
        numberPickerAge.setMinValue(18);
        numberPickerAge.setWrapSelectorWheel(false);
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
                int age = numberPickerAge.getValue();
                setUsersAge(age);
                ageLayout.setVisibility(View.GONE);
                loadFouthView();
            }
        });
    }

    private void setUsersAge(int age) {
        SharedPreferences pref = mContext.getSharedPreferences(Constants.AGE, MODE_PRIVATE);
        pref.edit().clear().putInt(Constants.AGE, age).apply();

        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mref = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(user).child(Constants.AGE);
        mref.setValue(age);
    }



    private void loadFouthView() {
        locationLayout.setVisibility(View.VISIBLE);
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
    }

    private void loadFifthView() {
        concludeLayout.setVisibility(View.VISIBLE);
        okBtn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

}
