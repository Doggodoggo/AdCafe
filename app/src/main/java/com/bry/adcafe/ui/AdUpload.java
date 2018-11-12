package com.bry.adcafe.ui;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.fragments.FragmentModalBottomSheet;
import com.bry.adcafe.fragments.FragmentMpesaPayBottomsheet;
import com.bry.adcafe.fragments.FragmentMpesaPaymentInitiation;
import com.bry.adcafe.fragments.FragmentSelectPaymentOptionBottomSheet;
import com.bry.adcafe.fragments.SetAdvertiserLocation;
import com.bry.adcafe.fragments.SetAdvertiserTargetInfoFragment;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.models.AdvertiserLocation;
import com.bry.adcafe.models.TargetedUser;
import com.bry.adcafe.models.User;
import com.bry.adcafe.services.Payments;
import com.bry.adcafe.services.TimeManager;
import com.bry.adcafe.services.Utils;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.bry.adcafe.Variables.amountToPayForUpload;

public class AdUpload extends AppCompatActivity implements NumberPicker.OnValueChangeListener{
    public static final String TAG = AdUpload.class.getSimpleName();
    private static final int PICK_IMAGE_REQUEST = 234;
    private Uri mFilepath;

    private Context mContext;
    private ImageView mUploadButton;
    private ImageView mChoosingImage;
    private ImageView mProfileImageViewPreview;
    private CardView mCardviewForShowingPreviewOfAd;
    private LinearLayout mTopBarPreview;
    private AVLoadingIndicatorView mAvi;
    private TextView mLoadingTextView;
    private TextView mSelectText;
    private TextView mUploadText;
    private LinearLayout mNoConnection;
    private LinearLayout mBottomNavs;
    private TextView mNumberOfUsersChosenText;
    @Bind(R.id.numberOfUsersToAdvertiseToLayout) RelativeLayout mNumberOfUsersToAdvertiseTo;
    @Bind(R.id.categoryText)TextView mCategoryText;
    @Bind(R.id.helpIcon) ImageView mHelpIcon;
    @Bind(R.id.uploadLayout) RelativeLayout mUploadLayout;
    @Bind(R.id.progressBarUpload) ProgressBar mProgressBarUpload;



    private boolean mHasNumberBeenLoaded;
    private boolean mHasUserChosenAnImage;
    private boolean mHasNumberBeenChosen;
    private boolean mHasUserPayed;

    private List<Integer> clustersToUpLoadTo = new ArrayList<>();
    private List<Integer> failedClustersToUploadTo = new ArrayList<>();
    private int mNumberOfClusters = 1;
    private int mClusterTotal;
    private int mClusterToStartFrom;
    private int noOfChildrenInClusterToStartFrom;
    private int noOfChildrenInLatestCluster;

    private DatabaseReference mRef;
    private DatabaseReference mRef2;
    private DatabaseReference mRef3;
    private DatabaseReference mRef4;
    private DatabaseReference mRef5;
    private DatabaseReference mRef6;
    private DatabaseReference boolRef;
    private String date;

    private Bitmap bm;
    private int cycleCount = 0;
    private static TextView tv;
    static Dialog d;
    private ImageButton b;

    private boolean uploading = false;
    private String pushrefInAdminConsole;
    private String mLink = "none";
    private String mCategory;
    private ProgressDialog mAuthProgressDialog;
    private ProgressDialog mProgForPayments;
    private int numberOfClustersBeingUploadedTo = 0;

    private int mAmountToPayPerTargetedView;
    private int mAmountPlusOurShare;

    private boolean isWindowPaused = false;
    private DatabaseReference SKListener;
    private boolean isNeedToLoadLogin = false;

    private List<String> userIds = new ArrayList<>();
    private List<TargetedUser> UsersInCategory = new ArrayList<>();
    private List <String> knownTargetedUsers = new ArrayList<>();

    private String mPhoneNumber = "none";
    private final int REQUESTCODE = 3301;
    private boolean canShowDiscardMessageWhenLeaving = true;

    @Bind(R.id.swipeBackView2)View swipeBackView2;
    private boolean isSwipingForBack2 = false;
    private GestureDetector mSwipeBackDetector2;
    private int maxSideSwipeLength2 = 200;
    private int x_delta2;
    private List<Integer> SideSwipeRawList2 = new ArrayList<>();
    private boolean isSideScrolling2 = false;
    private int maxSideSwipeLength = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_upload);
        mContext = this.getApplicationContext();
        mHasUserChosenAnImage = false;
        mHasNumberBeenLoaded = false;
        ButterKnife.bind(this);

        mCategory = Variables.SelectedCategory;
        Log(TAG,"Category gotten from Variables class is : "+mCategory);
        mCategoryText.setText(mCategory);

        mAmountToPayPerTargetedView = Variables.amountToPayPerTargetedView;
        Variables.advertiserLocations.clear();
        mAmountPlusOurShare = Variables.getTotalPayForOneUserForAdvertiser(Variables.amountToPayPerTargetedView);
        Log(TAG,"Amount to pay per targeted user is : "+ mAmountToPayPerTargetedView);
        Log(TAG,"Amount to pay per targeted user is : "+ mAmountPlusOurShare);

        setUpViews();
        createProgressDialog();
        if (!TimeManager.isTimerOnline())TimeManager.setUpTimeManager("RESET_TIMER",mContext);
            startGetNumberOfClusters();
        setUpListeners();
        Variables.resetAdvertiserTargetingData();

        addTouchListenerForSwipeBack2();

    }

    private ChildEventListener chilForRefresh = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            if(!uploading){
                resetAndRestart();
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void loadListenerForRecreate() {
        mRef6 = FirebaseDatabase.getInstance().getReference(Constants.CLUSTER_TO_START_FROM)
                .child(Integer.toString(mAmountToPayPerTargetedView))
                .child(mCategory+"_cluster_to_start_from");
        mRef6.addChildEventListener(chilForRefresh);
    }

    private void resetAndRestart(){
//        if(!clustersToUpLoadTo.isEmpty())clustersToUpLoadTo.clear();
//        mHasNumberBeenChosen = false;
//        startGetNumberOfClusters();
    }


    @Override
    protected void onResume(){
        isWindowPaused = false;
        super.onResume();
        if (!TimeManager.isTimerOnline())TimeManager.setUpTimeManager("RESET_TIMER",mContext);
        if(isNeedToLoadLogin){
            Intent intent = new Intent(AdUpload.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else addListenerForChangeInSessionKey();

    }

    @Override
    protected void onPause(){
        if (!TimeManager.isTimerOnline())TimeManager.setUpTimeManager("RESET_TIMER",mContext);
        removeListenerForChangeInSessionKey();
        isWindowPaused = true;
        super.onPause();
    }


    private void startGetNumberOfClusters(){
        if(isOnline(mContext)){
            getNumberOfClusters();//in-turn triggers the getClusterToStartFrom method.
        }else{
            Snackbar.make(findViewById(R.id.adUploadCoordinatorLayout), R.string.UploadAdNoConnection,
                    Snackbar.LENGTH_LONG).show();
            mNoConnection.setVisibility(View.VISIBLE);
            mBottomNavs.setVisibility(View.GONE);
        }
    }

    private void setUpViews() {
        mBottomNavs = findViewById(R.id.bottomNavs);
        mUploadButton = findViewById(R.id.uploadIcon);
        mChoosingImage = findViewById(R.id.chooseImageIcon);
        mProfileImageViewPreview = findViewById(R.id.profileImageViewPreview);
        mCardviewForShowingPreviewOfAd = findViewById(R.id.cardviewForShowingPreviewOfAd);
        mTopBarPreview = findViewById(R.id.topBarPreview);
        mAvi = findViewById(R.id.AdUploadAvi);
        mLoadingTextView = findViewById(R.id.loadingText);
        mSelectText = findViewById(R.id.selectText);
        mUploadText = findViewById(R.id.uploadText);
        mNoConnection = findViewById(R.id.noConnectionMessage);
        tv = findViewById(R.id.numberOfUsersToAdvertiseTo);
        b = findViewById(R.id.chooseNumberButton);
        mNumberOfUsersChosenText = findViewById(R.id.chooseNumberText);
    }


    private void getNumberOfClusters() {
//        mAvi.setVisibility(View.VISIBLE);
        mProgressBarUpload.setVisibility(View.VISIBLE);
        mLoadingTextView.setVisibility(View.VISIBLE);
        setAllOtherViewsToBeGone();
        Log(TAG,"---Getting number of clusters.");
        //When restructuring to advertising to specific type of users,add .child("%AdvertCategory%");
        mRef = FirebaseDatabase.getInstance().getReference(Constants.CLUSTERS).child(Constants.CLUSTERS_LIST)
                .child(Integer.toString(mAmountToPayPerTargetedView)).child(mCategory);
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot clusterSnap:dataSnapshot.getChildren()){
                    for(DataSnapshot userIdSnap:clusterSnap.getChildren()){
                        String userId = userIdSnap.getValue(String.class);
                        Log(TAG,"Loaded user: "+userId);
                        userIds.add(userId);
                    }
                }
                long numberOfClusters;
                if(dataSnapshot.getChildrenCount() == 0){
                    numberOfClusters = 1;
                }else{
                    numberOfClusters = dataSnapshot.getChildrenCount();
                }
                mClusterTotal = (int)numberOfClusters;
                Log(TAG,"---Number of clusters gotten from firebase is-- "+mClusterTotal);
                getClusterToStartForm();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mProgressBarUpload.setVisibility(View.GONE);
                mNoConnection.setVisibility(View.VISIBLE);
                mBottomNavs.setVisibility(View.GONE);
            }
        });

    }

    private void getClusterToStartForm() {
        Log(TAG,"---getting cluster to start from");
        //When changing to specific clusters, this will need to change from this to .getReference("%AdvertCategory%_cluster_to_start_from");
        mRef2 = FirebaseDatabase.getInstance().getReference(Constants.CLUSTER_TO_START_FROM)
                .child(Integer.toString(mAmountToPayPerTargetedView)).child(mCategory+"_cluster_to_start_from");
        mRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int clusterGotten;
                if(dataSnapshot.hasChildren()){
                    clusterGotten = dataSnapshot.getValue(int.class);
                }else{
                    clusterGotten = 1;
                }
                mClusterToStartFrom = clusterGotten;
                Log(TAG,"---Cluster to start from is -- "+mClusterToStartFrom);
                loadClusterToStartFromChildrenNo();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
//            setAllOtherViewsToBeVisible();
                mNoConnection.setVisibility(View.VISIBLE);
                mBottomNavs.setVisibility(View.GONE);
//            mAvi.setVisibility(View.GONE);
                mProgressBarUpload.setVisibility(View.GONE);
                Log(TAG,"---Unable to connect to firebase at the moment. "+databaseError.getMessage());
                Snackbar.make(findViewById(R.id.SignUpCoordinatorLayout), R.string.cannotUploadFailedFirebase,
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void loadClusterToStartFromChildrenNo() {
        Log(TAG,"---Starting query for no of ads in cluster to start from.");
        //When changing to targeted advertising,this will need to have .child("%AdvertCategory%") between getNextDay and clusterToStartFrom child;
        DatabaseReference boolRef = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS)
                .child(getNextDay()).child(Integer.toString(mAmountToPayPerTargetedView))
                .child(mCategory).child(Integer.toString(mClusterToStartFrom));
        boolRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                noOfChildrenInClusterToStartFrom = (int)dataSnapshot.getChildrenCount();
                Log(TAG,"--Number of children in cluster to start from gotten from firebase is  -"+noOfChildrenInClusterToStartFrom);
                getNumberOfUploadedAdsInLatestCluster();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
//            setAllOtherViewsToBeVisible();
                mNoConnection.setVisibility(View.VISIBLE);
                mBottomNavs.setVisibility(View.GONE);
//            mAvi.setVisibility(View.GONE);
                mProgressBarUpload.setVisibility(View.GONE);
                Log(TAG,"---Unable to connect to firebase at the moment. "+databaseError.getMessage());
                Snackbar.make(findViewById(R.id.SignUpCoordinatorLayout), R.string.cannotUploadFailedFirebase,
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void getNumberOfUploadedAdsInLatestCluster(){
        Log(TAG,"---Starting query for no of ads in Latest cluster now.");
        //When changing to targeted advertising,this will need to have .child("%AdvertCategory%") between getNextDay and clusterTotal child;
        DatabaseReference boolRef = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS)
                .child(getNextDay()).child(Integer.toString(mAmountToPayPerTargetedView))
                .child(mCategory).child(Integer.toString(mClusterTotal));
        boolRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    noOfChildrenInLatestCluster = (int)dataSnapshot.getChildrenCount();
                }else{
                    noOfChildrenInLatestCluster = 0;
                }
                Log(TAG,"---the number of children gotten is: "+noOfChildrenInLatestCluster);
                loadAllUsersForCategory();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
//                setAllOtherViewsToBeVisible();
                mNoConnection.setVisibility(View.VISIBLE);
                mBottomNavs.setVisibility(View.GONE);
//                mAvi.setVisibility(View.GONE);
                mProgressBarUpload.setVisibility(View.GONE);
                Log(TAG,"---Unable to connect to firebase at the moment. "+databaseError.getMessage());
                Snackbar.make(findViewById(R.id.SignUpCoordinatorLayout), R.string.cannotUploadFailedFirebase,
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void loadAllUsersForCategory(){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(String uid:userIds){
                    DataSnapshot userSnap = dataSnapshot.child(uid);
                    String gender = "";
                    int birthday = 0;
                    int birthmonth = 0;
                    int birthyear = 0;
                    if(userSnap.child(Constants.GENDER).exists()) gender = userSnap.child(Constants.GENDER).getValue(String.class);
                    if(userSnap.child(Constants.DATE_OF_BIRTH).exists()) {
                        birthday = userSnap.child(Constants.DATE_OF_BIRTH).child("day").getValue(int.class);
                        birthmonth = userSnap.child(Constants.DATE_OF_BIRTH).child("month").getValue(int.class);
                        birthyear = userSnap.child(Constants.DATE_OF_BIRTH).child("year").getValue(int.class);
                    }
                    List<LatLng> userLocations = new ArrayList<>();
                    DataSnapshot locationSnap = userSnap.child(Constants.FIREBASE_USERS_LOCATIONS);
                    for(DataSnapshot location:locationSnap.getChildren()){
                        double lat = location.child("lat").getValue(double.class);
                        double lng = location.child("lng").getValue(double.class);
                        userLocations.add(new LatLng(lat,lng));
                    }
                    String deviceCategory = "";
                    if(userSnap.child(Constants.DEVICE_CATEGORY).exists()){
                        deviceCategory = userSnap.child(Constants.DEVICE_CATEGORY).getValue(String.class);
                    }
                    List<String> subs = new ArrayList<>();
                    for(DataSnapshot subSnap:userSnap.child(Constants.SUBSCRIPTION_lIST).getChildren()){
                        String subKey = subSnap.getKey();
                        subs.add(subKey);
                    }
                    int cluster = userSnap.child(Constants.SUBSCRIPTION_lIST).child(mCategory).getValue(int.class);
                    UsersInCategory.add(new TargetedUser(uid,gender,birthday,birthmonth,birthyear,
                            userLocations,cluster,deviceCategory,subs));
                }
                setAllOtherViewsToBeVisible();
                mProgressBarUpload.setVisibility(View.GONE);
                mLoadingTextView.setVisibility(View.GONE);
                OnClicks();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
//                setAllOtherViewsToBeVisible();
                mNoConnection.setVisibility(View.VISIBLE);
                mBottomNavs.setVisibility(View.GONE);
//                mAvi.setVisibility(View.GONE);
                mProgressBarUpload.setVisibility(View.GONE);
                Log(TAG,"---Unable to connect to firebase at the moment. "+databaseError.getMessage());
                Snackbar.make(findViewById(R.id.SignUpCoordinatorLayout), R.string.cannotUploadFailedFirebase, Snackbar.LENGTH_LONG).show();
            }
        });
    }



    @Override
    protected void onDestroy(){
        super.onDestroy();
//        if(mRef!=null) mRef.removeEventListener(val);
//        if(mRef2!=null) mRef2.removeEventListener(val2);
//        if(boolRef!=null) boolRef.removeEventListener(val3);
        if(mRef6!=null) mRef6.removeEventListener(chilForRefresh);
        removeListeners();
    }

    private void OnClicks(){
        if(findViewById(R.id.chooseImageIcon)!=null){
            findViewById(R.id.chooseImageIcon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chooseImage();
                }
            });
        }

        mHelpIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdUpload.this,TutorialAdvertisers.class);
                startActivity(intent);
            }
        });

        if(findViewById(R.id.WebsiteIcon)!=null){
            findViewById(R.id.WebsiteIcon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addWebsiteLinkPrompt();
                }
            });
        }

        if(findViewById(R.id.progressBarTimerExample)!=null){
            findViewById(R.id.progressBarTimerExample).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(FirebaseAuth.getInstance().getCurrentUser().getEmail().equals("bryonyoni@gmail.com")) {
                        if (isOnline(mContext)) {
                            if (bm != null) {
                                uploadImageAsAnnouncement();
                            } else {
                                Toast.makeText(mContext, "Please choose your image again.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(mContext, "Check your internet connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                    return false;
                }
            });
        }
        if(findViewById(R.id.uploadIcon)!=null){
            findViewById(R.id.uploadIcon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!mHasUserChosenAnImage){
                        Toast.makeText(mContext, R.string.pleaseChooseIcon,Toast.LENGTH_SHORT).show();
                    } else if(!mHasNumberBeenChosen){
                        Toast.makeText(mContext,"You may need to choose number of users to advertise to first!",Toast.LENGTH_LONG).show();
                    }else if(!isOnline(mContext)){
                        Snackbar.make(findViewById(R.id.adUploadCoordinatorLayout), R.string.UploadAdNoConnection,
                            Snackbar.LENGTH_LONG).show();
                    }else{
                        if(bm!=null && !uploading){
                            //This method will begin the process for uploading ad;
                            showDialogForPayments();
                        }else{
                            Toast.makeText(mContext,"Please choose your image again.",Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }

        if(findViewById(R.id.profileImageViewPreview)!=null){
            findViewById(R.id.profileImageViewPreview).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chooseImage();
                }
            });
        }

        if(findViewById(R.id.chooseNumberButton)!=null){
            findViewById(R.id.chooseNumberButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showNumberPicker();
                }
            });
        }
        if(findViewById(R.id.reupload)!=null){
            findViewById(R.id.reupload).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clustersToUpLoadTo.addAll(failedClustersToUploadTo);
                    failedClustersToUploadTo.clear();
                    if(bm!=null){
                        setAllOtherViewsToBeGone();
//                        mAvi.setVisibility(View.VISIBLE);
                        mProgressBarUpload.setVisibility(View.VISIBLE);
                        mLoadingTextView.setVisibility(View.VISIBLE);
                        mLoadingTextView.setText(R.string.uploadMessage);
                        setNewValueToStartFrom();

                        uploadImage(bm);
                    }else{
                        Toast.makeText(mContext,"Please choose your image again.",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        if(findViewById(R.id.targetIcon)!=null){
            findViewById(R.id.targetIcon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mHasNumberBeenChosen) loadUserTargetingPrompt();
                    else Toast.makeText(mContext,"First choose the number of people your targeting.",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setUpListeners(){
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForShowingSelectedBottomsheet,
                new IntentFilter("PROCEED_CARD_DETAILS_PART"));

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForStartPayments,
                new IntentFilter("START_PAYMENTS_INTENT"));

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForSetTargetInfo,
                new IntentFilter("IS_ADVERTISER_FILTERING"));
    }

    private void removeListeners(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverForShowingSelectedBottomsheet);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverForStartPayments);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverForSuccessfulMpesaPayments);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverForSetTargetInfo);
        removePaymentListeners();
    }



    //////This method will start the upload process.Call it when your done with the payments....
    private void startProcessForUpload(){
        canShowDiscardMessageWhenLeaving = false;
        mAuthProgressDialog.show();
        uploading = true;
        setNewValueToStartFrom();
        date = getNextDay();
        numberOfClustersBeingUploadedTo = clustersToUpLoadTo.size();
        recheckNoOfChildrenInClulsterToStartFrom();
    }

    private void addWebsiteLinkPrompt() {
        final Dialog d = new Dialog(AdUpload.this);
        d.setTitle("Add your contact info.");
        d.setContentView(R.layout.dialog5);

        Button b1 = d.findViewById(R.id.cancelBtn);
        Button b2 =  d.findViewById(R.id.buttonOk);

        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        final EditText websiteEditText = d.findViewById(R.id.websiteEditText);
        final EditText phoneNumber = d.findViewById(R.id.phoneNumber);
        final TextView locationsSet = d.findViewById(R.id.locationsSet);
        final ImageButton mapIcon = d.findViewById(R.id.mapIcon);

        if(mLink.equals("none")) websiteEditText.setText("");
        else websiteEditText.setText(mLink);

        if(mPhoneNumber.equals("none")) phoneNumber.setText("");
        else phoneNumber.setText(mPhoneNumber);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                d.dismiss();
            }
        });

        mapIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getFragmentManager();
                SetAdvertiserLocation mapFragment = new SetAdvertiserLocation();
                mapFragment.setMenuVisibility(false);
                mapFragment.show(fm,"Edit Your Location.");
                mapFragment.setfragcontext(mContext);
                mapFragment.setActivity(AdUpload.this);

                LocalBroadcastManager.getInstance(mContext).registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
                        if(Variables.advertiserLocations.isEmpty())locationsSet.setText("Click the map icon to add your locations.");
                        else if(Variables.advertiserLocations.size()==1) locationsSet.setText("Your 1 location has been set.");
                        else locationsSet.setText("Your "+Variables.advertiserLocations.size()+" locations have been set.");
                    }
                },new IntentFilter("UPDATE_TEXT_FOR_WHEN_LOCATION_IS_ADDED"));
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean canDismiss = true;
                findViewById(R.id.smallDot).setVisibility(View.INVISIBLE);
                if(!websiteEditText.getText().toString().trim().equals("")||
                    !phoneNumber.getText().toString().trim().equals("")||
                    !Variables.advertiserLocations.isEmpty()){
                    findViewById(R.id.smallDot).setVisibility(View.VISIBLE);
                }
                if(websiteEditText.getText().toString().trim().equals("")) {
                    mLink = "none";
                    Variables.incentiveForClick = 0;
                }else{
                    mLink = websiteEditText.getText().toString().trim();
                }
                if(phoneNumber.getText().toString().trim().equals("")){
                    mPhoneNumber = "none";
                }else{
                    if(phoneNumber.getText().toString().trim().length()<10){
                        phoneNumber.setError("That's not a real phone number");
                        mPhoneNumber = "none";
                        canDismiss = false;
                    }else{
                        try{
                            int vz = Integer.parseInt(phoneNumber.getText().toString().trim());
                        }catch (Exception e){
                            e.printStackTrace();
                            mPhoneNumber = "none";
                            phoneNumber.setError("That's not a real phone number");
                            canDismiss = false;
                        }
                        mPhoneNumber = phoneNumber.getText().toString().trim();
                    }
                }
                if(canDismiss){
                    if(!mLink.equals("") && !mLink.equals("none")){
                        showSetIncentiveView(d);
                    }else{
                        d.dismiss();
                    }
                }
            }
        });
        d.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                findViewById(R.id.smallDot).setVisibility(View.INVISIBLE);
                if(!websiteEditText.getText().toString().trim().equals("")|| !phoneNumber.getText().toString().trim().equals("")
                        || !Variables.advertiserLocations.isEmpty()){
                    findViewById(R.id.smallDot).setVisibility(View.VISIBLE);
                }
                if(websiteEditText.getText().toString().trim().equals("")) {
                    mLink = "none";
                    Variables.incentiveForClick = 0;
                }
                if(phoneNumber.getText().toString().trim().equals("")){
                    mPhoneNumber = "none";
                }

            }
        });
        d.show();
    }

    private void showSetIncentiveView(final Dialog d){
        final LinearLayout setContactDetailsLayout = d.findViewById(R.id.setContactDetailsLayout);
        setContactDetailsLayout.animate().setDuration(Constants.ANIMATION_DURATION).translationX(-200).alpha(0f)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                setContactDetailsLayout.setTranslationX(-200);
                setContactDetailsLayout.setAlpha(0f);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();

        final LinearLayout setIncentiveLayout = d.findViewById(R.id.setIncentiveLayout);
        setIncentiveLayout.setVisibility(View.VISIBLE);
        setIncentiveLayout.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        setIncentiveLayout.setTranslationX(0);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();

        final NumberPicker incentiveAmountPicker = d.findViewById(R.id.incentiveAmountPicker);
        incentiveAmountPicker.setMaxValue(100);
        incentiveAmountPicker.setMinValue(1);
        incentiveAmountPicker.setWrapSelectorWheel(false);
        incentiveAmountPicker.setOnValueChangedListener(this);
        incentiveAmountPicker.setValue(1);


        final Button buttonWebsiteIncentiveOk = d.findViewById(R.id.buttonWebsiteIncentiveOk);
        buttonWebsiteIncentiveOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Variables.incentiveForClick = incentiveAmountPicker.getValue();
                d.dismiss();
            }
        });

        final Button cancelWebsiteIncentiveBtn = d.findViewById(R.id.cancelWebsiteIncentiveBtn);
        cancelWebsiteIncentiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Variables.incentiveForClick!=0){
                    Variables.incentiveForClick = 0;
                    view.setAlpha(0.5f);
                    buttonWebsiteIncentiveOk.setText("SKIP");
                }
            }
        });

        incentiveAmountPicker.setOnScrollListener(new NumberPicker.OnScrollListener() {
            @Override
            public void onScrollStateChange(NumberPicker numberPicker, int i) {
                cancelWebsiteIncentiveBtn.setAlpha(1f);
                buttonWebsiteIncentiveOk.setText("SET");
            }
        });

        if(Variables.incentiveForClick!=0){
            cancelWebsiteIncentiveBtn.setAlpha(1f);
            buttonWebsiteIncentiveOk.setText("SET");
            incentiveAmountPicker.setValue((int)Variables.incentiveForClick);
        }else{
            Variables.incentiveForClick = 0;
            cancelWebsiteIncentiveBtn.setAlpha(0.5f);
            buttonWebsiteIncentiveOk.setText("SKIP");
        }

    }

    private void showDialogForPayments() {
//        startTestUpload();
        showMessageBeforeBottomsheet();
    }

    private void startTestUpload(){
        Variables.transactionID = "TRANS000000001";
        Variables.paymentOption = Constants.MPESA_OPTION;
        startProcessForUpload();
    }

    private BroadcastReceiver mMessageReceiverForShowingSelectedBottomsheet = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log(TAG, "Broadcast has been received show bottomsheet.");
            if(Variables.paymentOption.equals(Constants.MPESA_OPTION))showMpesaBottomSheetFragment();
            else if(Variables.paymentOption.equals(Constants.BANK_OPTION))showBottomSheetFragment();
        }
    };

    private BroadcastReceiver mMessageReceiverForStartPayments = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log(TAG, "Broadcast has been received show bottomsheet.");
            if(Variables.paymentOption.equals(Constants.MPESA_OPTION))startMpesaPayments();
//            if(Variables.paymentOption.equals(Constants.MPESA_OPTION))startTestUpload();
            else if(Variables.paymentOption.equals(Constants.BANK_OPTION)) startBankPayments();
        }
    };




    private void startBankPayments(){
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.PAY_POOL);
        DatabaseReference pushRef = adRef.push();
        Variables.transactionID = "TRANS"+pushRef.getKey();

        String cardNumber = Variables.cardNumber;
        String expiration = Variables.expiration;
        String cvv = Variables.cvv;
        String postalCode = Variables.postalCode;
        int amount = (int)(amountToPayForUpload);
        String cardHolderName = Variables.cardHolderName;
        String cardHolderFirstName = Variables.cardHolderFirstName;
        String cardHolderLastName = Variables.cardHolderLastName;
        String expirationMonth = Variables.expirationMonth;
        String expirationYear = Variables.expirationYear;
        String cardHolderAdress = Variables.phoneNo;
        String email = Variables.cardHolderEmail;
        String state = Variables.cardHolderState;
        String FAILED_BANK_PAYMENTS = "FAILED_BANK_PAYMENTS";
        String SUCCESSFUL_BANK_PAYMENTS = "SUCCESSFUL_BANK_PAYMENTS";
        Log(TAG,"Expiration date: "+expiration);

//        if(FirebaseAuth.getInstance().getCurrentUser().getEmail().equals("biglebowski@gmail.com")) amount = 80;

        Payments payments = new Payments(mContext,SUCCESSFUL_BANK_PAYMENTS,FAILED_BANK_PAYMENTS);
        payments.startCardPayment(Variables.transactionID,Variables.transactionID,amount,Variables.phoneNo,email,cvv,cardNumber,expirationMonth,
                expirationYear,cardHolderAdress,state,Constants.country,postalCode,state,cardHolderFirstName,cardHolderLastName);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForSuccessfulPayment
                ,new IntentFilter(SUCCESSFUL_BANK_PAYMENTS));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForfailedPayment,
                new IntentFilter(FAILED_BANK_PAYMENTS));
        mProgForPayments.show();
//        startProcessForUpload();
    }

    private void startMpesaPayments(){
        double amount = amountToPayForUpload;
//        if(FirebaseAuth.getInstance().getCurrentUser().getEmail().equals("biglebowski@gmail.com")) amount = 300;
        String phoneNo = Variables.phoneNo;

        FragmentManager fm = getFragmentManager();
        FragmentMpesaPaymentInitiation fragmentMpesaPaymentInitiation = new FragmentMpesaPaymentInitiation();
        fragmentMpesaPaymentInitiation.setMenuVisibility(false);
        fragmentMpesaPaymentInitiation.setContext(mContext);
        fragmentMpesaPaymentInitiation.setDetails(amount,phoneNo);
        fragmentMpesaPaymentInitiation.show(fm, "Mpesa pay.");

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForSuccessfulMpesaPayments,
                new IntentFilter("FINISHED_MPESA_PAYMENTS"));
    }

    private BroadcastReceiver mMessageReceiverForSuccessfulMpesaPayments = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log(TAG, "Broadcast has been received that Mpesa payment is successful.");
            mProgForPayments.hide();
            startProcessForUpload();
            addPaymentTotalsInFirebase();
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
        }
    };

    private void addPaymentTotalsInFirebase() {
        final double amount = Variables.amountToPayForUpload;
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference(Constants.ADMIN_MONEY);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long amm = 0;
                if(dataSnapshot.exists()) amm = dataSnapshot.getValue(long.class);
                long newAmm = amm+=amount;
                DatabaseReference mewRef = FirebaseDatabase.getInstance().getReference(Constants.ADMIN_MONEY);
                mewRef.setValue(newAmm);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private BroadcastReceiver mMessageReceiverForSuccessfulPayment = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log(TAG, "Broadcast has been received that payment is successful.");
            mProgForPayments.hide();
            startProcessForUpload();
            addPaymentTotalsInFirebase();
            removePaymentListeners();
        }
    };

    private BroadcastReceiver mMessageReceiverForfailedPayment = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log(TAG, "Broadcast has been received that payments has failed.");
            mProgForPayments.hide();
            showFailedCardPayments();
            removePaymentListeners();
        }
    };

    private void removePaymentListeners(){
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForSuccessfulPayment);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForfailedPayment);
    }

    private void showMessageBeforeBottomsheet(){
        FragmentSelectPaymentOptionBottomSheet fragmentModalBottomSheet = new FragmentSelectPaymentOptionBottomSheet();
        fragmentModalBottomSheet.setActivity(AdUpload.this);
        if(Variables.genderTarget.equals("") &&
                Variables.ageGroupTarget== null &&
                Variables.locationTarget.isEmpty()&&
                Variables.deviceRangeCategory.equals("")&&
                Variables.targetCategoryList.isEmpty()&&
                clustersToUpLoadTo.size()==1 &&
                UsersInCategory.size() < Constants.NUMBER_OF_USERS_PER_CLUSTER &&
                getTargetedUsersByCluster(clustersToUpLoadTo.get(0)).size() < Constants.NUMBER_OF_USERS_PER_CLUSTER){
            int number = getTargetedUsersByCluster(clustersToUpLoadTo.get(0)).size();
            fragmentModalBottomSheet.setTargetOptionData(true,number,mCategory);
        }
        fragmentModalBottomSheet.show(getSupportFragmentManager(),"BottomSheet Fragment");
    }

    private void showBottomSheetFragment(){
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        FragmentModalBottomSheet fragmentModalBottomSheet = new FragmentModalBottomSheet();

        fragmentModalBottomSheet.setDetails((mNumberOfClusters*Constants.NUMBER_OF_USERS_PER_CLUSTER),
                mAmountPlusOurShare, TimeManager.getNextDayPlus(), mCategory,userEmail,Variables.userName);
        fragmentModalBottomSheet.setActivity(AdUpload.this);

        fragmentModalBottomSheet.show(getSupportFragmentManager(),"BottomSheet Fragment");
    }

    private void showMpesaBottomSheetFragment(){
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        FragmentMpesaPayBottomsheet fragmentModalBottomSheet = new FragmentMpesaPayBottomsheet();

        List<TargetedUser> usersTargetedForSelectedClusters = new ArrayList<>(getNumberOfUsersAfterFiltering());
        knownTargetedUsers = new ArrayList<>();
        for(TargetedUser user: usersTargetedForSelectedClusters){
            knownTargetedUsers.add(user.getUserId());
        }
        fragmentModalBottomSheet.setDetails((mNumberOfClusters*Constants.NUMBER_OF_USERS_PER_CLUSTER),
                mAmountPlusOurShare, TimeManager.getNextDayPlus(), mCategory,userEmail,Variables.userName,
                usersTargetedForSelectedClusters);
        fragmentModalBottomSheet.setActivity(AdUpload.this);

        fragmentModalBottomSheet.show(getSupportFragmentManager(),"BottomSheet Fragment");
    }




    private void showFailedCardPayments(){
        final Dialog d = new Dialog(AdUpload.this);
        d.setTitle("Payment failed");
        d.setContentView(R.layout.dialog93);
        Button b2 =  d.findViewById(R.id.okBtn);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }



    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        Log.i(TAG,"----value is"+" "+newVal);

    }

    public void showNumberPicker() {
        final Dialog d = new Dialog(AdUpload.this);
        d.setTitle("Targeted people no.");
        d.setContentView(R.layout.dialog);
        Button b1 = d.findViewById(R.id.chooseNumberButton);
        Button b2 = d.findViewById(R.id.cancelChooseNumberButton);
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        final NumberPicker np = d.findViewById(R.id.numberPicker1);
        np.setMaxValue(mClusterTotal);
        np.setMinValue(1);
        np.setWrapSelectorWheel(false);
        np.setOnValueChangedListener(this);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv.setText(String.valueOf(np.getValue()*Constants.NUMBER_OF_USERS_PER_CLUSTER));
                mHasNumberBeenChosen = true;
                if(mHasUserChosenAnImage)mUploadLayout.setVisibility(View.VISIBLE);
                mNumberOfClusters = np.getValue();
//                addToClusterListToUploadTo(mNumberOfClusters);
                mNumberOfUsersToAdvertiseTo.setVisibility(View.VISIBLE);
                addToClusterListToUploadTo(mNumberOfClusters);
                d.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();

    }

    private void chooseImage() {
        Log(TAG,"Starting intent for picking an image.");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Log(TAG, "---Data gotten from activity is ok.");
            if (data.getData() != null) {
                mFilepath = data.getData();
                try {
                    mCardviewForShowingPreviewOfAd.setVisibility(View.VISIBLE);
                    mTopBarPreview.setVisibility(View.VISIBLE);
                    mHasUserChosenAnImage = true;

                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mFilepath);
                    bm = getResizedBitmap(bitmap,1300);
//                    byte[] myByte = bitmapToByte(bm);
//                    Glide.with(mContext).load(myByte)
//                            .override(400, 300)
//                            .into(mProfileImageViewPreview);
                    mProfileImageViewPreview.setImageBitmap(bm);
                    mProfileImageViewPreview.setBackground(null);

                    if(mHasNumberBeenChosen)mUploadLayout.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log(TAG, "---Unable to get and set image. " + e.getMessage());
                }
            } else {
                Log(TAG, "---Unable to work on the result code for some reason.");
                Toast.makeText(mContext,"the data.getData method returns null for some reason...",Toast.LENGTH_SHORT).show();
            }
        }

    }

    private byte[] bitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] byteArray = baos.toByteArray();
        return byteArray;
    }

    private String encodeBitmapForFirebaseStorage(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,80,baos);
        String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        return imageEncoded;
    }

    private void uploadImageAsAnnouncement(){
        if(FirebaseAuth.getInstance().getCurrentUser().getEmail().equals("bryonyoni@gmail.com")){
            Toast.makeText(mContext,"Uploading announcement to firebase",Toast.LENGTH_SHORT).show();
            String encodedImageToUpload = encodeBitmapForFirebaseStorage(bm);

            DatabaseReference dba = FirebaseDatabase.getInstance().getReference(Constants.ANNOUNCEMENTS).child(getNextDay());
            DatabaseReference pushRef = dba.push();
            String key = pushRef.getKey();

            Advert announcement = new Advert(encodedImageToUpload);
            announcement.setNumberOfTimesSeen(0);
            announcement.setNumberOfUsersToReach(1000);
            announcement.setPushRefInAdminConsole(key);
            announcement.setUserEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
            announcement.setWebsiteLink(mLink);
            announcement.setHasBeenReimbursed(false);
            announcement.setDateInDays(getDateInDays());
            announcement.setCategory("technology");
            announcement.setPushId(key);

            pushRef.setValue(announcement).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(mContext,"Announcement Uploaded.",Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(mContext,"Announcement has failed to upload.",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void recheckNoOfChildrenInClulsterToStartFrom(){
        DatabaseReference boolRef = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS)
                .child(getNextDay()).child(Integer.toString(mAmountToPayPerTargetedView))
                .child(mCategory).child(Integer.toString(mClusterToStartFrom));
        boolRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                noOfChildrenInClusterToStartFrom = (int)dataSnapshot.getChildrenCount();
                recheckNoOfChildrenInLatestClusters();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void recheckNoOfChildrenInLatestClusters(){
        DatabaseReference boolRef = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS)
                .child(getNextDay()).child(Integer.toString(mAmountToPayPerTargetedView))
                .child(mCategory).child(Integer.toString(mClusterTotal));
        boolRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    noOfChildrenInLatestCluster = (int)dataSnapshot.getChildrenCount();
                }else{
                    noOfChildrenInLatestCluster = 0;
                }
                uploadImageToManagerConsole();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }




    private void uploadImageToManagerConsole() {
        String encodedImageToUpload = encodeBitmapForFirebaseStorage(bm);
//        addToClusterListToUploadTo(mNumberOfClusters);
        Log(TAG, "Uploading Ad to AdminConsole.");
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE).child(getNextDay());
        DatabaseReference pushRef = adRef.push();
        String pushId = pushRef.getKey();

        DatabaseReference adRef2 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(User.getUid()).child(Constants.UPLOADED_AD_LIST).child(getNextDay());
        DatabaseReference pushRef2 = adRef2.push();
        pushRef2.setValue(pushId);

        Advert advert = new Advert(encodedImageToUpload);
        advert.setNumberOfTimesSeen(0);
        advert.setPaymentReference(Variables.transactionID);
        advert.setPaymentMethod(Variables.paymentOption);
        advert.setAdvertiserUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
        advert.setNumberOfUsersToReach(mNumberOfClusters*Constants.NUMBER_OF_USERS_PER_CLUSTER);
        if(Variables.isTargetingDataSet())advert.setNumberOfUsersToReach(knownTargetedUsers.size());
        advert.setPushRefInAdminConsole(pushId);
        advert.setUserEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        advert.setWebsiteLink(mLink);
        advert.setWebClickNumber(0);
        advert.setWebClickIncentive(Variables.incentiveForClick);
        advert.setPayoutReimbursalAmount(0);
        advert.setAmountToPayPerTargetedView(mAmountPlusOurShare);
        advert.setHasBeenReimbursed(false);
        advert.setDateInDays(getDateInDays());
        advert.setCategory(mCategory);

        pushrefInAdminConsole = pushId;
        pushRef.setValue(advert).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
               uploadImage(bm);
            }
        });
        advert.setImageUrl("");
        DatabaseReference dbrefh = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(User.getUid()).child(Constants.UPLOAD_HISTORY).child(Long.toString(-(getDateInDays()+1)))
                .child(pushId);
        dbrefh.setValue(advert);

        DatabaseReference dbMyRef = FirebaseDatabase.getInstance().getReference(Constants.HISTORY_UPLOADS)
                .child(TimeManager.getNextDayYear()).child(TimeManager.getNextDayMonth()).child(TimeManager.getNextDayDay())
                .child(pushId);
        dbMyRef.setValue(advert);

        DatabaseReference dbrefTrans = FirebaseDatabase.getInstance().getReference(Constants.TRANSACTIONS)
                .child(Constants.PAYMENTS).child(Constants.MPESA_PAYMENTS)
                .child(TimeManager.getNextDayYear()).child(TimeManager.getNextDayMonth()).child(TimeManager.getNextDayDay())
                .child(pushId);
        Variables.transactionObject.setTimeOfDay(TimeManager.getTime());
        Variables.transactionObject.setDate(TimeManager.getDate());
        Variables.transactionObject.setPayingPhoneNumber(Variables.phoneNo);
        Variables.transactionObject.setPushrefInAdminConsole(pushId);
        Variables.transactionObject.setUploaderId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        dbrefTrans.setValue(Variables.transactionObject);

        if(Variables.isTargetingDataSet()) {
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child(Constants.TARGET_USER_DATA)
                    .child(getNextDay()).child(pushId);

            Gson gson = new Gson();
            String userListString = gson.toJson(knownTargetedUsers);
            myRef.setValue(userListString);
        }
    }

    private void createProgressDialog(){
        mAuthProgressDialog = new ProgressDialog(this,R.style.AppCompatAlertDialogStyle);
        mAuthProgressDialog.setTitle(R.string.app_name);
        mAuthProgressDialog.setMessage("Uploading your ad... ");
        mAuthProgressDialog.setCancelable(false);
        mAuthProgressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
        mAuthProgressDialog.setIndeterminate(true);

        mProgForPayments = new ProgressDialog(this,R.style.AppCompatAlertDialogStyle);
        mProgForPayments.setTitle(R.string.app_name);
        mProgForPayments.setMessage("This should take a few seconds... ");
        mProgForPayments.setCancelable(false);
        mProgForPayments.setProgress(ProgressDialog.STYLE_SPINNER);
        mProgForPayments.setIndeterminate(true);
    }

    private void uploadImage(final Bitmap bm) {
//        String encodedImageToUpload = encodeBitmapForFirebaseStorage(bm);
        uploading = true;
        try{
            if(mRef6!=null) mRef6.removeEventListener(chilForRefresh);
        }catch (Exception e){
            e.printStackTrace();
        }
        if(clustersToUpLoadTo.size()>10){
            for(int i = 0; i < 10; i++){
                String pushId;
                final Integer clusterNumber = clustersToUpLoadTo.get(i);
                if(clusterNumber<mClusterToStartFrom){
                    //push id is set to +2 to avoid setting the same push ID twice.
                    pushId = Integer.toString(noOfChildrenInClusterToStartFrom+2);
                }else{
                    if(clusterNumber==mClusterTotal){
                        //The latest cluster may have fewer children than other cluster, thus should be handled differently.
                        pushId = Integer.toString(noOfChildrenInLatestCluster+1);
                    }else{
                        pushId = Integer.toString(noOfChildrenInClusterToStartFrom+1);
                    }
                }
                Log(TAG,"---Uploading encoded image to cluster -"+clusterNumber+" now...");
                Log(TAG,"---The custom push id is ---"+pushId);
                //When configuring for targeted advertising based on user prefs, this will change by add ing .child("%AdvertCategory%") between
                //date and cluster number.
                mRef3 = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS).child(date)
                        .child(Integer.toString(mAmountToPayPerTargetedView))
                        .child(mCategory)
                        .child(Integer.toString(clusterNumber)).child(pushId);
//                Advert advert = new Advert(encodedImageToUpload);
                Advert advert = new Advert();
                advert.setPushId(pushId);
                advert.setWebsiteLink(mLink);
                advert.setCategory(mCategory);
                advert.setAdvertiserPhoneNo("");
                advert.setFlagged(false);
                advert.setWebClickIncentive(Variables.incentiveForClick);
                advert.setWebClickNumber(0);
                advert.setAdvertiserUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                advert.setDateInDays(getDateInDays());
                advert.setAdminFlagged(false);
                advert.setPushRefInAdminConsole(pushrefInAdminConsole);
                setClusterInAdminAdvert(clusterNumber,Integer.parseInt(pushId));
                mRef3.setValue(advert).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        cycleCount++;
                        clustersToUpLoadTo.remove(clusterNumber);
//                        mAuthProgressDialog.setProgress((clustersToUpLoadTo.size()/numberOfClustersBeingUploadedTo)*100);
//                        int percentage = (clustersToUpLoadTo.size()/numberOfClustersBeingUploadedTo)*100;
//                        String message ="Uploading your ad... "+percentage+"%";
//                        mAuthProgressDialog.setMessage(message);
                        if(clustersToUpLoadTo.isEmpty()){
                            if(!failedClustersToUploadTo.isEmpty()){
                                checkAndNotifyAnyFailed();
                            }else{
//                                mAvi.setVisibility(View.GONE);
//                                mLoadingTextView.setVisibility(View.GONE);
//                                setAllOtherViewsToBeVisible();
                                mAuthProgressDialog.dismiss();
                                Log(TAG,"---Ad has been successfully uploaded to one of the clusters in firebase");
//                                setHasPayedInFirebaseToFalse();
                                cycleCount = 1;
                                uploading = false;
                                startDashboardActivity();
                            }
                        }else{
                            if(cycleCount == 10){
                                cycleCount = 0;
                                uploadImage(bm);
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        failedClustersToUploadTo.add(clusterNumber);
                        cycleCount++;
                        clustersToUpLoadTo.remove(clusterNumber);
                        if(clustersToUpLoadTo.isEmpty()){
                            checkAndNotifyAnyFailed();
                        }
                    }
                });
                if(Variables.isTargetingDataSet()){
                    if(Variables.genderTarget!=null){
                        if(!Variables.genderTarget.equals("")) mRef3.child("targetdata").child("gender").setValue(Variables.genderTarget);
                    }
                    if(Variables.ageGroupTarget!=null){
                        mRef3.child("targetdata").child("agegroup").setValue(Variables.ageGroupTarget);
                    }
                    if(!Variables.locationTarget.isEmpty()){
                        for(LatLng latl:Variables.locationTarget){
                            DatabaseReference pushRef = mRef3.child("targetdata").child("locations").push();
                            pushRef.child("lat").setValue(latl.latitude);
                            pushRef.child("lng").setValue(latl.longitude);
                        }
                    }
                    if(!Variables.deviceRangeCategory.equals("")){
                        mRef3.child("targetdata").child("devicerange").setValue(Variables.deviceRangeCategory);
                    }
                    if(!Variables.targetCategoryList.isEmpty()){
                        for(String category:Variables.targetCategoryList){
                            DatabaseReference pushRef = mRef3.child("targetdata").child("categorylist").push();
                            pushRef.setValue(category);
                        }
                    }
                }
                if(!mPhoneNumber.equals("none") || !Variables.advertiserLocations.isEmpty()){
                    if(!mPhoneNumber.equals("none") || !mPhoneNumber.equals("")){
                        mRef3.child("contactdata").child(Constants.ADVERTISER_PHONE_NO)
                                .setValue(mPhoneNumber);
                    }
                    if(!Variables.advertiserLocations.isEmpty()){
                        for(AdvertiserLocation adLoc: Variables.advertiserLocations){
                            DatabaseReference pushRef = mRef3.child("contactdata").child(Constants.ADVERTISER_LOCATION).push();
                            if(!adLoc.getPlaceName().equals(""))pushRef.child("name").setValue(adLoc.getPlaceName());
                            pushRef.child("lat").setValue(adLoc.getMyLatLng().latitude);
                            pushRef.child("lng").setValue(adLoc.getMyLatLng().longitude);
                        }
                    }
                }
            }
        }else{
            for(final Integer number : clustersToUpLoadTo){
                Log(TAG,"---Uploading encoded image to cluster -"+number+" now...");
                String pushId;
                if(number<mClusterToStartFrom){
                    //push id is set to +2 to avoid setting the same push ID twice.
                    pushId = Integer.toString(noOfChildrenInClusterToStartFrom+2);
                }else{
                    if(number==mClusterTotal){
                        //The latest cluster may have fewer children than other cluster, thus should be handled differently.
                        pushId = Integer.toString(noOfChildrenInLatestCluster+1);
                    }else{
                        pushId = Integer.toString(noOfChildrenInClusterToStartFrom+1);
                    }
                }
                Log(TAG,"---The custom push id is ---"+pushId);
                //When configuring for targeted advertising based on user prefs, this will change by add ing .child("%AdvertCategory%") between
                //date and cluster number.
                mRef3 = FirebaseDatabase.getInstance().getReference(Constants.ADVERTS).child(date)
                        .child(Integer.toString(mAmountToPayPerTargetedView))
                        .child(mCategory)
                        .child(Integer.toString(number)).child(pushId);
//                Advert advert = new Advert(encodedImageToUpload);
                Advert advert = new Advert();
                advert.setPushId(pushId);
                advert.setWebsiteLink(mLink);
                advert.setCategory(mCategory);
                advert.setAdvertiserPhoneNo("");
                advert.setFlagged(false);
                advert.setWebClickIncentive(Variables.incentiveForClick);
                advert.setWebClickNumber(0);
                advert.setAdvertiserUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                advert.setDateInDays(getDateInDays());
                advert.setAdminFlagged(false);
                advert.setPushRefInAdminConsole(pushrefInAdminConsole);
                setClusterInAdminAdvert(number,Integer.parseInt(pushId));
                mRef3.setValue(advert).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        cycleCount++;
                        clustersToUpLoadTo.remove(number);
//                        mAuthProgressDialog.setProgress((clustersToUpLoadTo.size()/numberOfClustersBeingUploadedTo)*100);
//                        int percentage = (clustersToUpLoadTo.size()/numberOfClustersBeingUploadedTo)*100;
//                        String message ="Uploading your ad... "+percentage+"%";
//                        mAuthProgressDialog.setMessage(message);
                        if(clustersToUpLoadTo.isEmpty()){
                            if(!failedClustersToUploadTo.isEmpty()){
                                checkAndNotifyAnyFailed();
                            }else{
//                                mAvi.setVisibility(View.GONE);
//                                mLoadingTextView.setVisibility(View.GONE);
//                                setAllOtherViewsToBeVisible();
                                mAuthProgressDialog.dismiss();
                                Log(TAG,"---Ad has been successfully uploaded to one of the clusters in firebase");
//                                setHasPayedInFirebaseToFalse();
                                cycleCount = 1;
                                uploading = false;
                                startDashboardActivity();
                            }

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        failedClustersToUploadTo.add(number);
                        cycleCount++;
                        clustersToUpLoadTo.remove(number);
                        if(clustersToUpLoadTo.isEmpty()){
                            checkAndNotifyAnyFailed();
                        }
                    }
                });
                if(Variables.isTargetingDataSet()){
                    if(Variables.genderTarget!=null){
                        if(!Variables.genderTarget.equals("")) mRef3.child("targetdata").child("gender").setValue(Variables.genderTarget);
                    }
                    if(Variables.ageGroupTarget!=null){
                        mRef3.child("targetdata").child("agegroup").setValue(Variables.ageGroupTarget);
                    }
                    if(!Variables.locationTarget.isEmpty()){
                        for(LatLng latl:Variables.locationTarget){
                            DatabaseReference pushRef = mRef3.child("targetdata").child("locations").push();
                            pushRef.child("lat").setValue(latl.latitude);
                            pushRef.child("lng").setValue(latl.longitude);
                        }
                    }
                    if(!Variables.deviceRangeCategory.equals("")){
                        mRef3.child("targetdata").child("devicerange").setValue(Variables.deviceRangeCategory);
                    }
                    if(!Variables.targetCategoryList.isEmpty()){
                        for(String category:Variables.targetCategoryList){
                            DatabaseReference pushRef = mRef3.child("targetdata").child("categorylist").push();
                            pushRef.setValue(category);
                        }
                    }
                }
                if(!mPhoneNumber.equals("none") || !Variables.advertiserLocations.isEmpty()){
                    if(!mPhoneNumber.equals("none") || !mPhoneNumber.equals("")){
                        mRef3.child("contactdata").child(Constants.ADVERTISER_PHONE_NO)
                                .setValue(mPhoneNumber);
                    }
                    if(!Variables.advertiserLocations.isEmpty()){
                        for(AdvertiserLocation adLoc: Variables.advertiserLocations){
                            DatabaseReference pushRef = mRef3.child("contactdata").child(Constants.ADVERTISER_LOCATION).push();
                            if(!adLoc.getPlaceName().equals(""))pushRef.child("name").setValue(adLoc.getPlaceName());
                            pushRef.child("lat").setValue(adLoc.getMyLatLng().latitude);
                            pushRef.child("lng").setValue(adLoc.getMyLatLng().longitude);
                        }
                    }
                }
            }
        }


    }



    private void setClusterInAdminAdvert(int cluster,int pushId){
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.ADS_FOR_CONSOLE)
                .child(getNextDay())
                .child(pushrefInAdminConsole)
                .child("clustersToUpLoadTo")
                .child(Integer.toString(cluster));
        adRef.setValue(pushId);
    }

    private void checkAndNotifyAnyFailed() {
        if(!clustersToUpLoadTo.isEmpty()){
//           Toast.makeText(mContext,"Upload process is incomplete.",Toast.LENGTH_LONG).show();
//            mAvi.setVisibility(View.GONE);
            mProgressBarUpload.setVisibility(View.GONE);
            mLoadingTextView.setVisibility(View.GONE);
            setAllOtherViewsToBeVisible();
            findViewById(R.id.reupload).setVisibility(View.VISIBLE);

            final Dialog d = new Dialog(AdUpload.this);
            d.setTitle("Upload incomplete");
            d.setContentView(R.layout.dialog2);
            Button b1 = d.findViewById(R.id.buttonOk);
            b1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    d.dismiss();
                }
            });
            d.show();
        }else{
            findViewById(R.id.reupload).setVisibility(View.GONE);
        }
    }

    private void startDashboardActivity() {
        final Dialog d = new Dialog(AdUpload.this);
        d.setTitle("Upload complete");
        d.setContentView(R.layout.dialog4);
        Button b2 =  d.findViewById(R.id.buttonOk);
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
                finish();
            }
        });
        d.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                finish();
            }
        });
        d.show();

    }



    private void setNewValueToStartFrom() {
        mRef4 = FirebaseDatabase.getInstance().getReference(Constants.CLUSTER_TO_START_FROM).child(Integer.toString(mAmountToPayPerTargetedView))
                .child(mCategory+"_cluster_to_start_from");
        if(mClusterToStartFrom + mNumberOfClusters > mClusterTotal){
            mRef4.setValue((mClusterToStartFrom + mNumberOfClusters)-mClusterTotal);
        }else{
            mRef4.setValue(mClusterToStartFrom + mNumberOfClusters);
        }

    }

    private void addToClusterListToUploadTo(int number){
        clustersToUpLoadTo.clear();
        Log(TAG,"The number of total clusters is : "+mClusterTotal);
        Log(TAG,"The cluster to start from is : "+mClusterToStartFrom);
        Log(TAG,"Number of clusters to upload to is : "+mNumberOfClusters);

        for(int i = 0; i < number; i++){
            if(mClusterToStartFrom+i>mClusterTotal){
                clustersToUpLoadTo.add(mClusterToStartFrom+i-(mClusterTotal));
                Log(TAG,"Limit has been exceeded.setting cluster to upload to : "+(mClusterToStartFrom+i-(mClusterTotal)));
            }else{
                clustersToUpLoadTo.add(mClusterToStartFrom+i);
                Log(TAG,"Adding cluster to list normally : "+(mClusterToStartFrom+i));
            }
        }

    }

    private void setHasPayedInFirebaseToFalse() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        mRef5 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants. HAS_USER_MADE_PAMENTS);
        mRef5.setValue(false);
    }



    private void setAllOtherViewsToBeGone(){
        mChoosingImage.setVisibility(View.GONE);
        mUploadButton.setVisibility(View.GONE);
        mNumberOfUsersChosenText.setVisibility(View.GONE);
        b.setVisibility(View.GONE);
        mCardviewForShowingPreviewOfAd.setVisibility(View.GONE);
        mSelectText.setVisibility(View.GONE);
        mUploadText.setVisibility(View.GONE);
        mTopBarPreview.setVisibility(View.GONE);
        mNumberOfUsersToAdvertiseTo.setVisibility(View.INVISIBLE);
        mHelpIcon.setVisibility(View.GONE);
    }

    private void setAllOtherViewsToBeVisible(){
        mChoosingImage.setVisibility(View.VISIBLE);
        mUploadButton.setVisibility(View.VISIBLE);
        mNumberOfUsersChosenText.setVisibility(View.VISIBLE);
        b.setVisibility(View.VISIBLE);
        mCardviewForShowingPreviewOfAd.setVisibility(View.VISIBLE);
        mSelectText.setVisibility(View.VISIBLE);
        mUploadText.setVisibility(View.VISIBLE);
        mTopBarPreview.setVisibility(View.VISIBLE);
        mHelpIcon.setVisibility(View.VISIBLE);
    }

    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

    private String getDate(){
        return TimeManager.getDate();
    }




    private String getNextDay(){
        return TimeManager.getNextDay();

    }

    @Override
    public void onBackPressed(){
        if(uploading){
            final Dialog d = new Dialog(AdUpload.this);
            d.setTitle("Upload incomplete");
            d.setContentView(R.layout.dialog3);
            Button b1 = d.findViewById(R.id.buttonYes);
            Button b2 = d.findViewById(R.id.buttonNo);
            b1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startDashboardActivity();
                    d.dismiss();
                }
            });
            b2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    d.dismiss();
                }
            });
            d.show();
        }else{
//            super.onBackPressed();
            if(canShowDiscardMessageWhenLeaving){
                showDialogForDiscardingData();
            }else finish();
        }

    }

    private Long getDateInDays(){
        return TimeManager.getDateInDays();
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



    private void Log(String tag,String message){
        try{
            String user = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            if(user.equals(Constants.ADMIN_ACC)) Log.d(tag,message);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void loadUserTargetingPrompt(){
        FragmentManager fm = getFragmentManager();
        SetAdvertiserTargetInfoFragment cpvFragment = new SetAdvertiserTargetInfoFragment();
        cpvFragment.setMenuVisibility(false);
        cpvFragment.show(fm,"Filter Users.");

        List<TargetedUser> usersTargetedForSelectedClusters = new ArrayList<>(getNumberOfUsersAfterFilteringByCategories());
        cpvFragment.setUsersThatCanBeReached(usersTargetedForSelectedClusters);
        cpvFragment.setfragcontext(mContext);
        cpvFragment.setCategory(mCategory);
        cpvFragment.setActivity(this);
    }

    private BroadcastReceiver mMessageReceiverForSetTargetInfo = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log(TAG,"Broadcast received for show or hide target dot. IsTargeting is: "+Variables.isTargetingDataSet());
            if(Variables.isTargetingDataSet())findViewById(R.id.smallDot2).setVisibility(View.VISIBLE);
            else findViewById(R.id.smallDot2).setVisibility(View.INVISIBLE);
        }
    };

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            if(Variables.isTargetingDataSet())findViewById(R.id.smallDot2).setVisibility(View.VISIBLE);
            else findViewById(R.id.smallDot2).setVisibility(View.INVISIBLE);
        }
    }



    ChildEventListener chil = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            if(dataSnapshot.getKey().equals(Constants.BOI_IS_DA_KEY)){
                String firebasekey = dataSnapshot.getValue(String.class);
                if(!firebasekey.equals(getSessionKey())){
                    PerformShutdown();
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    public void addListenerForChangeInSessionKey(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference FirstCheckref = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.BOI_IS_DA_KEY);
        FirstCheckref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    String firebasekey = dataSnapshot.getValue(String.class);
                    if (!firebasekey.equals(getSessionKey())) {
                        PerformShutdown();
                    }else{
                        nowReallyAddLisenerForChangeInSessionKey();
                    }
                }else{
                    PerformShutdown();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void nowReallyAddLisenerForChangeInSessionKey(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SKListener = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_CHILD_USERS)
                .child(uid);
        SKListener.addChildEventListener(chil);
    }

    public void removeListenerForChangeInSessionKey(){
        if(SKListener!=null){
            SKListener.removeEventListener(chil);
        }
    }

    public String getSessionKey(){
        SharedPreferences prefs2 = getSharedPreferences(Constants.BOI_IS_DA_KEY, MODE_PRIVATE);
        String sk = prefs2.getString(Constants.BOI_IS_DA_KEY, "NULL");
        Log.d(TAG, "Loading session key from shared prefs - " + sk);
        return sk;
    }



    public void PerformShutdown(){
        if (FirebaseAuth.getInstance() != null) {
            FirebaseAuth.getInstance().signOut();
        }
        Variables.resetAllValues();
        if(!isWindowPaused){
            Intent intent = new Intent(AdUpload.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else{
            isNeedToLoadLogin = true;
        }

    }

    public List<TargetedUser> getTargetedUsersByCluster(int cluster){
        List<TargetedUser> specificUsers = new ArrayList<>();

        for(TargetedUser user:UsersInCategory){
            if(user.getClusterId()==cluster){
                specificUsers.add(user);
            }
        }
        return specificUsers;
    }


    private List<TargetedUser> getNumberOfUsersAfterFilteringByCategories(){
        List<TargetedUser> usersQualified = new ArrayList<>(UsersInCategory);

        for(TargetedUser user : UsersInCategory){
            if(!clustersToUpLoadTo.contains(user.getClusterId())){
                if(usersQualified.contains(user)) usersQualified.remove(user);
            }
        }

        return usersQualified;
    }

    private List<TargetedUser> getNumberOfUsersAfterFiltering(){
        List<TargetedUser> usersQualified = new ArrayList<>(UsersInCategory);

        for(TargetedUser user : UsersInCategory){
            if(!clustersToUpLoadTo.contains(user.getClusterId())){
                if(usersQualified.contains(user)) usersQualified.remove(user);
            }
        }

        if(!Variables.genderTarget.equals("")){
            for(TargetedUser user: UsersInCategory){
                if(!user.getGender().equals(Variables.genderTarget)){
                    if(usersQualified.contains(user)) usersQualified.remove(user);
                }
            }
        }
        if(Variables.ageGroupTarget!=null){
            for(TargetedUser user:UsersInCategory){
                if(user.getBirthday()!=0 || user.getBirthYear()!=0) {
                    Integer userAge = getAge(user.getBirthYear(), user.getBirthMonth(), user.getBirthday());
                    if(userAge < Variables.ageGroupTarget.getStartingAge() || userAge > Variables.ageGroupTarget.getFinishAge()){
                        if(usersQualified.contains(user)) usersQualified.remove(user);
                    }
                }else{
                    if(usersQualified.contains(user)) usersQualified.remove(user);
                }
            }
        }
        if(!Variables.locationTarget.isEmpty()){
            for(TargetedUser user:UsersInCategory){
                if(locationContained(user.getUserLocations())==0){
                    if(usersQualified.contains(user)) usersQualified.remove(user);
                }
            }
        }
        if(!Variables.deviceRangeCategory.equals("")){
            for(TargetedUser user:UsersInCategory){
                if(!user.getDeviceCategory().equals(Variables.deviceRangeCategory)){
                    if(usersQualified.contains(user)) usersQualified.remove(user);
                }
            }
        }
        if(!Variables.targetCategoryList.isEmpty()){
            for(TargetedUser user: UsersInCategory){
                for(String subscription:Variables.targetCategoryList){
                    if(!user.getSubscriptions().contains(subscription)){
                        if(usersQualified.contains(user)) usersQualified.remove(user);
                    }
                }
            }
        }
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


    private void showDialogForTargetUsersBeforePayment(){
        if(!Variables.isTargetingDataSet() && clustersToUpLoadTo.size()==1 && getTargetedUsersByCluster(clustersToUpLoadTo.get(0)).size()<100){
            int number = getTargetedUsersByCluster(clustersToUpLoadTo.get(0)).size();
            final Dialog d = new Dialog(AdUpload.this);
            d.setTitle("Targeted people no.");
            d.setContentView(R.layout.dialog_only_specific_or_all);
            TextView message = d.findViewById(R.id.usersInfo);
            final RadioButton yes = d.findViewById(R.id.radioButtonYes);
            final RadioButton no = d.findViewById(R.id.radioButtonNo);
            Button ok = d.findViewById(R.id.okBtn);

            yes.setText("Yes, Pay only for the "+number+" users.");
            message.setText(String.format("As of now, the users who are interested in %s are %d. You can pay for these users only, however, if they increase, the new users will not see your advert.", mCategory, number));

            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(yes.isChecked()) Variables.isOnlyTargetingKnownUsers = true;
                    showDialogForPayments();
                    d.dismiss();
                }
            });
            d.show();
        }else{
            showDialogForPayments();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUESTCODE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG,"Sending message for location button thingy to true");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("SET_THAT_MY_LOCATION_BUTTON_THINGY"));
            }
        }
    }


    private void showDialogForDiscardingData(){
        final Dialog d = new Dialog(AdUpload.this);
        d.setTitle("Discard Info");
        d.setContentView(R.layout.dialog_discard_changes_in_adupload);
        Button ok = d.findViewById(R.id.okBtn);

        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                d.dismiss();
                finish();
            }
        });

        d.show();
    }


    private void addTouchListenerForSwipeBack2() {
        mSwipeBackDetector2 = new GestureDetector(this, new MySwipebackGestureListener2());
        swipeBackView2.setOnTouchListener(touchListener2);
    }

    View.OnTouchListener touchListener2 = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (mSwipeBackDetector2.onTouchEvent(motionEvent)) {
                return true;
            }
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if (isSideScrolling2) {
                    Log.d("touchListener", " onTouch ACTION_UP");
                    isSideScrolling2 = false;

                    int RightScore = 0;
                    int LeftScore = 0;
                    if (SideSwipeRawList2.size() > 15) {
                        for (int i = SideSwipeRawList2.size() - 1; i > SideSwipeRawList2.size() - 15; i--) {
                            int num1 = SideSwipeRawList2.get(i);
                            int numBefore1 = SideSwipeRawList2.get(i - 1);

                            if (numBefore1 > num1) LeftScore++;
                            else RightScore++;
                        }
                    } else {
                        for (int i = SideSwipeRawList2.size() - 1; i > 0; i--) {
                            int num1 = SideSwipeRawList2.get(i);
                            int numBefore1 = SideSwipeRawList2.get(i - 1);

                            if (numBefore1 > num1) LeftScore++;
                            else RightScore++;
                        }
                    }
                    if (RightScore > LeftScore) {
//                        onBackPressed();
                    }
                    hideNupdateSideSwipeThing2();
                    SideSwipeRawList2.clear();
                }
                ;
            }

            return false;
        }
    };

    class MySwipebackGestureListener2 extends GestureDetector.SimpleOnGestureListener {
        int origX = 0;
        int origY = 0;


        @Override
        public boolean onDown(MotionEvent event) {
            Log.d("TAG", "onDown: ");
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            Log.d("TAG", "onDown: event.getRawX(): " + event.getRawX() + " event.getRawY()" + event.getRawY());
            CoordinatorLayout.LayoutParams lParams = (CoordinatorLayout.LayoutParams) swipeBackView2.getLayoutParams();
            x_delta2 = X - lParams.leftMargin;

            origX = lParams.leftMargin;
            origY = lParams.topMargin;


            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.i("TAG", "onSingleTapConfirmed: ");
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.i("TAG", "onLongPress: ");
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.i("TAG", "onDoubleTap: ");
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            final int Y = (int) e2.getRawY();
            final int X = (int) e2.getRawX();
            if ((X - x_delta2) < maxSideSwipeLength) {

            } else if ((X - x_delta2) > maxSideSwipeLength) {

            } else {
            }
            showNupdateSideSwipeThing2(X - x_delta2);

            Log.d("TAG", "the e2.getAction()= " + e2.getAction() + " and the MotionEvent.ACTION_CANCEL= " + MotionEvent.ACTION_CANCEL);
            SideSwipeRawList2.add(X);

            isSideScrolling2 = true;
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            int RightScore = 0;
            int LeftScore = 0;
            if (SideSwipeRawList2.size() > 15) {
                for (int i = SideSwipeRawList2.size() - 1; i > SideSwipeRawList2.size() - 15; i--) {
                    int num1 = SideSwipeRawList2.get(i);
                    int numBefore1 = SideSwipeRawList2.get(i - 1);

                    if (numBefore1 > num1) LeftScore++;
                    else RightScore++;
                }
            } else {
                for (int i = SideSwipeRawList2.size() - 1; i > 0; i--) {
                    int num1 = SideSwipeRawList2.get(i);
                    int numBefore1 = SideSwipeRawList2.get(i - 1);

                    if (numBefore1 > num1) LeftScore++;
                    else RightScore++;
                }
            }
            if (RightScore > LeftScore) {
                onBackPressed();
            }
            SideSwipeRawList2.clear();
            return false;

        }
    }

    private void showNupdateSideSwipeThing2(int pos) {
        int trans = (int) ((pos - Utils.dpToPx(10)) * 0.9);
//        RelativeLayout isGoingBackIndicator = findViewById(R.id.isGoingBackIndicator);
//        isGoingBackIndicator.setTranslationX(trans);

        View v = findViewById(R.id.swipeBackViewIndicator2);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) v.getLayoutParams();
        params.height = trans;
        v.setLayoutParams(params);

        LinearLayout bottomNavs = findViewById(R.id.bottomNavs);
        bottomNavs.setTranslationX((int)(trans*0.05));

        CardView cardviewForShowingPreviewOfAd = findViewById(R.id.cardviewForShowingPreviewOfAd);
        cardviewForShowingPreviewOfAd.setTranslationX((int)(trans*0.05));

        LinearLayout topBarPreview2 = findViewById(R.id.topBarPreview2);
        topBarPreview2.setTranslationX((int)(trans*0.05));

        LinearLayout topBarPreview = findViewById(R.id.topBarPreview);
        topBarPreview.setTranslationX((int)(trans*0.05));
    }

    private void hideNupdateSideSwipeThing2() {
        int myDurat = 200;
//        RelativeLayout isGoingBackIndicator = findViewById(R.id.isGoingBackIndicator);
//        isGoingBackIndicator.animate().setDuration(mAnimationTime).translationX(Utils.dpToPx(-40)).start();

        final View v = findViewById(R.id.swipeBackViewIndicator2);
        final CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) v.getLayoutParams();

        ValueAnimator animatorTop;
        animatorTop = ValueAnimator.ofInt(params.height, 0);
        animatorTop.setInterpolator(new LinearOutSlowInInterpolator());
        animatorTop.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.height = (Integer) valueAnimator.getAnimatedValue();
                v.requestLayout();
            }
        });
        animatorTop.setDuration(myDurat).start();



        final LinearLayout bottomNavs = findViewById(R.id.bottomNavs);

        final CardView cardviewForShowingPreviewOfAd = findViewById(R.id.cardviewForShowingPreviewOfAd);
        cardviewForShowingPreviewOfAd.animate().setDuration(myDurat).setInterpolator(new LinearOutSlowInInterpolator()).translationX(0).start();

        final LinearLayout topBarPreview2 = findViewById(R.id.topBarPreview2);
        topBarPreview2.animate().setDuration(myDurat).setInterpolator(new LinearOutSlowInInterpolator()).translationX(0).start();

        final LinearLayout topBarPreview = findViewById(R.id.topBarPreview);
        topBarPreview.animate().setDuration(myDurat).setInterpolator(new LinearOutSlowInInterpolator()).translationX(0).start();

        bottomNavs.animate().setDuration(myDurat).setInterpolator(new LinearOutSlowInInterpolator()).translationX(0)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        bottomNavs.setTranslationX(0);
                        cardviewForShowingPreviewOfAd.setTranslationX(0);
                        topBarPreview2.setTranslationX(0);
                        topBarPreview.setTranslationX(0);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }


}
