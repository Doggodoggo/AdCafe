package com.bry.adcafe.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Vibrator;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.adapters.BlankItem;
import com.bry.adcafe.adapters.DateItem;
import com.bry.adcafe.adapters.SAContainer;
import com.bry.adcafe.adapters.SavedAdsCard;
import com.bry.adcafe.fragments.ContactAdvertiserBottomsheet;
import com.bry.adcafe.fragments.ExpandedImageFragment;
import com.bry.adcafe.fragments.SmartFragmentStatePagerAdapter;
import com.bry.adcafe.fragments.ViewImageFragment;
import com.bry.adcafe.models.AdCoin;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.models.AdvertiserLocation;
import com.bry.adcafe.models.CustomViewPager;
import com.bry.adcafe.models.MyPlaceHolderView;
import com.bry.adcafe.models.MyTime;
import com.bry.adcafe.models.ObservableWebView;
import com.bry.adcafe.models.User;
import com.bry.adcafe.models.WebClickData;
import com.bry.adcafe.models.myLatLng;
import com.bry.adcafe.services.DatabaseManager;
import com.bry.adcafe.services.TimeManager;
import com.bry.adcafe.services.Utils;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mindorks.placeholderview.PlaceHolderView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import com.wang.avi.AVLoadingIndicatorView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class Bookmarks extends AppCompatActivity{
    private static final String TAG = "Bookmarks";
    private Context mContext;
    @Bind(R.id.PlaceHolderView) public MyPlaceHolderView mPlaceHolderView;
    @Bind(R.id.PlaceHolderView2) public MyPlaceHolderView mPlaceHolderView2;
    @Bind(R.id.progressBarBookmarks) public ProgressBar mProgressBarBookmarks;

    private ChildEventListener mChildEventListener;
    private DatabaseReference mRef;

    private List<Advert> mSavedAds;
    private Runnable mViewRunnable;
    private AVLoadingIndicatorView mAvi;
    private TextView loadingText;
    private TextView noAdsText;
    private ProgressDialog mAuthProgressDialog;
    private ProgressDialog mProg;

    private int cycleCount = 0;
    private LinkedHashMap<Long,List> HashOfAds = new LinkedHashMap<>();
    private boolean isDone = false;
    private LongOperation Lo;
    private boolean isSharing = false;
    private int iterations = 0;
    private int numberOfAdsLoaded = 0;
    private boolean isLoadingAds = false;

    private int iterationsFofUnpined = 0;
    ViewImageFragment imageF;

    private boolean isWindowPaused = false;
    private DatabaseReference SKListener;
    private boolean isNeedToLoadLogin = false;

    private MyPagerAdapter adapterViewPager;
    private CustomViewPager vpPager;
    private boolean isViewPagerShowing = false;
    private List<Advert> mAllAdsList = new ArrayList<>();

    private int currentPagePosition = 0;


    private boolean isEnabled = true;
    private ObservableWebView myWebView;
    private ImageButton mReloadButton;
    private ImageButton backButton;
    private ProgressBar progressBar;
//    private ProgressBar progressBar2;

    private TextView websiteText;
    private ImageButton closeBtn;
    private CardView cardContainer;
    private ImageView collapseIcon;

    private TextView minifiedTextView;
    private LinearLayout rightButtonLayout;
    private LinearLayout backBtnLayout;

    private View swipeBackView;
    private View swipeBackViewBig;

    private boolean isProgressShowing = false;
    private int animationTime = 150;

    private RelativeLayout WebViewContainer;
    private int mAnimationTime = 350;
    private boolean isCardMinimized = true;

    private String PAGE = "https://www.oneplus.com/";
    private boolean isMinified = false;


    private final int collapsedMargin = 1100;
    private final int unCollapsedMargin = 1;
    private boolean mIsScrolling = false;

    private GestureDetector mDetector;
    private int _yDelta;
    private boolean isInTransition = false;
    private final int normalDuration = 350;
    private List<Integer> RawList = new ArrayList<>();

    private boolean isSwipingForBack = false;
    private GestureDetector mSwipeBackDetector;
    private int maxSideSwipeLength = 200;
    private int x_delta;
    private List<Integer> SideSwipeRawList = new ArrayList<>();
    private boolean isSideScrolling = false;

    private ProgressBar scrollProgress;
    private int currentScrollProgress = 0;
    private int SCROLL_AMOUNT_THRESHOLD = 180;

    private int pageHeight = 0;
    private int scrollSoFar = 0;

    private LinearLayout ContactSelectorContainer;
    private LinearLayout CallLayout;
    private LinearLayout websiteLayout;
    private LinearLayout LocationLayout;

    private boolean isConfirmOpenWebsiteLayout = false;
    private boolean isConfirmDialLayout = false;


    @Bind(R.id.blackBackgroundLayout) RelativeLayout blackBackgroundLayout;
    private boolean isBottomPartOpen = false;
    @Bind(R.id.scrollProgressLayout) RelativeLayout scrollProgressLayout;
    private boolean isUpdating = false;
    private boolean hasPageBeenOpened = false;

    @Bind(R.id.pageIconLayout) RelativeLayout pageIconLayout;
    @Bind(R.id.pageIcon) ImageView pageIcon;

    private int noOfStrikes = 0;

    private boolean scrollConfirmBoolean = false;
    private boolean didScrollChangeListenerSetScrollConfirmBoolean = false;

    private int DIALER_REQUEST_CODE = 1001;
    private int SHARE_IMAGE_REQUEST_CODE = 1525;
    private boolean isLoadingPrevious = false;

    @Bind(R.id.backIndicator) View backIndicator;
    @Bind(R.id.shareIndicator) View shareIndicator;
    private int Ydelta;


    private GestureDetector mNavsSwipeBackDetector;
    private List<Integer> vpagerYrawList= new ArrayList<>();
    private boolean isUpDownSwiping = false;

    private MotionEvent event;
    private boolean hasEventBeenSet = false;
    private boolean hasAdBeenSet = false;

    private boolean isSetUpSharing = false;
    @Bind(R.id.secureImage) ImageView secureImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);
        mContext = getApplicationContext();
        ButterKnife.bind(this);

        loadPlaceHolderViews();
        registerReceivers();
        createProgressDialog();

        if(isNetworkConnected(mContext)){
//            loadAdsFromFirebase2();
            showProg();
            new LongOperation().execute("");

        }else{
            Snackbar.make(findViewById(R.id.bookmarksCoordinatorLayout), R.string.connectionDropped,
                    Snackbar.LENGTH_INDEFINITE).show();
        }
        setDeleteIcon();
        Variables.isSelectingMultipleItems = false;
        Variables.UnpinAdsList.clear();


        setViews();
        collapseCard();
        isCardMinimized = true;
        addTouchListener();

        animateCollapse();
    }

    private void setDeleteIcon() {
        findViewById(R.id.deleteAllicon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!Variables.UnpinAdsList.isEmpty()){
                    showPromptForBeforeRemove();
                }
            }
        });
    }

    private void showPromptForBeforeRemove(){
        String message = "Do you really want to unpin these "+Variables.UnpinAdsList.size()+" items?";
        if(Variables.UnpinAdsList.size()==1)message = "Do you really want to unpin the "+Variables.UnpinAdsList.size()+" item?";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setTitle("Confirm Unpin.")
                .setCancelable(true)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        Variables.isSelectingMultipleItems = false;
                        TextView topText = findViewById(R.id.topText);
                        topText.animate().setDuration(140).translationX(0);
                        findViewById(R.id.deleteAllicon).setVisibility(View.GONE);
                        Variables.UnpinAdsList.clear();
                        LocalBroadcastManager.getInstance(mContext)
                               .sendBroadcast(new Intent(Constants.REMOVE_REMOVE_SELF_LISTENER));
                    }
                })
                .setPositiveButton("Yes.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startUnPinningAll();
                        mAuthProgressDialog.show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    private void startUnPinningAll() {
        String uid = User.getUid();
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Constants.REMOVE_SELF_LISTENER));

        for(final Advert ad:Variables.UnpinAdsList){
            Variables.VariablesHashOfAds.get(ad.getDateInDays()).remove(ad);

            final DatabaseReference adRef2 = FirebaseDatabase.getInstance().getReference(Constants.PINNED_AD_POOL)
                    .child(Long.toString(ad.getDateInDays())).child(ad.getPushRefInAdminConsole()).child(Constants.NO_OF_TIMES_PINNED);

            final DatabaseReference adRef3 = FirebaseDatabase.getInstance().getReference(Constants.PINNED_AD_POOL)
                    .child(Long.toString(ad.getDateInDays())).child(ad.getPushRefInAdminConsole()).child("imageUrl");

            adRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        int numberOfPins = dataSnapshot.getValue(int.class);
                        if(numberOfPins==1){
                            adRef2.removeValue();
                            adRef3.removeValue();
                        }else{
                            adRef2.setValue(numberOfPins-1);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                    .child(uid).child(Constants.PINNED_AD_LIST).child(Long.toString(ad.getDateInDays()))
                    .child(ad.getPushId());

            adRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Variables.UnpinAdsList.remove(ad);
                    mAllAdsList.remove(ad);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("CHECK_IF_HAS_CHILDREN"+ad.getDateInDays()));
                    if(Variables.UnpinAdsList.isEmpty()){
                        mAuthProgressDialog.hide();
                        Variables.isSelectingMultipleItems = false;
                        TextView topText = findViewById(R.id.topText);
                        topText.animate().setDuration(140).translationX(0);
                        findViewById(R.id.deleteAllicon).setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    @Override
    protected void onResume(){
        isWindowPaused = false;
        super.onResume();
        if(isNeedToLoadLogin){
            Intent intent = new Intent(Bookmarks.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else addListenerForChangeInSessionKey();

    }

    @Override
    protected void onPause(){
        removeListenerForChangeInSessionKey();
        isWindowPaused = true;
        super.onPause();
    }


    @Override
    protected void onDestroy(){
        mPlaceHolderView.removeAllViews();
        if(Lo!=null) Lo = null;
//        hideProg();
        unregisterAllReceivers();
        super.onDestroy();
    }

    private void registerReceivers(){
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForUnpinned,new IntentFilter(Constants.REMOVE_PINNED_AD));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForReceivingUnableToPinAd,new IntentFilter(Constants.UNABLE_TO_REMOVE_PINNED_AD));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForSharingAd,new IntentFilter("SHARE"));

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForViewingAd,new IntentFilter("VIEW"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForShowingAreYouSureText,new IntentFilter("ARE_YOU_SURE_INTENT"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForShowingAreYouSureText2,new IntentFilter("ARE_YOU_SURE2"));

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForContinue,new IntentFilter("DONE!!"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForEquatePHViews,new IntentFilter("EQUATE_PLACEHOLDER_VIEWS"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForAddView,new IntentFilter("ADD_VIEW_IN_ACTIVITY"));

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForShowNoAdsText,new IntentFilter("SHOW_NO_ADS_TEXT"));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForShowDeleteIcon,new IntentFilter(Constants.SHOW_DELETE_ICON));

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForShowContact,new IntentFilter("SHOW_CONTACT_OPTIONS"));
    }

    private void unregisterAllReceivers() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForUnpinned);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForReceivingUnableToPinAd);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForSharingAd);

        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForViewingAd);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForShowingAreYouSureText);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForShowingAreYouSureText2);

        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForContinue);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForEquatePHViews);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForAddView);

        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForShowNoAdsText);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForShowDeleteIcon);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForShowContact);

        sendBroadCastToUnregisterReceivers();
    }

    private void sendBroadCastToUnregisterReceivers(){
        Intent intent = new Intent("UNREGISTER");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }




    private void showProg(){
        isLoadingAds = true;
        mProg.show();
    }

    private void hideProg(){
        isLoadingAds = false;
        mProg.dismiss();
    }

    private void loadPlaceHolderViews() {
        mAvi = findViewById(R.id.avi);
        loadingText = findViewById(R.id.loadingPinnedAdsMessage);

        noAdsText = findViewById(R.id.noPins);

        Point windowSize = Utils.getDisplaySize(getWindowManager());
        int width = windowSize.x;
        Variables.width = width;
//        int spanCount = width/Utils.dpToPx(88);

        int spanCount = 4;
        GridLayoutManager glm = new GridLayoutManager(mContext,spanCount);
        mPlaceHolderView.getBuilder().setLayoutManager(glm);

//        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(mContext,R.dimen.item_offset);
//
//        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.item_offset);
//        SpacesItemDecoration sid = new SpacesItemDecoration(spacingInPixels);
//
//        GridSpacingItemDecoration gsid = new GridSpacingItemDecoration(spanCount,1,true);
//
//        mPlaceHolderView.addItemDecoration(sid);
    }





    public class ItemOffsetDecoration extends RecyclerView.ItemDecoration {

        private int mItemOffset;

        public ItemOffsetDecoration(int itemOffset) {
            mItemOffset = itemOffset;
        }

        public ItemOffsetDecoration(@NonNull Context context, @DimenRes int itemOffsetId) {
            this(context.getResources().getDimensionPixelSize(itemOffsetId));
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset);
        }
    }

    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;
        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;

            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildLayoutPosition(view) == 0) {
                outRect.top = space;
            } else {
                outRect.top = 0;
            }
        }
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }



    private BroadcastReceiver mMessageReceiverForShowDeleteIcon = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("SAVED_ADS--","Received broadcast to show icon.");
            if(Variables.isSelectingMultipleItems){
                TextView topText = findViewById(R.id.topText);
                topText.animate().setDuration(140).translationX(-30);
                findViewById(R.id.deleteAllicon).setVisibility(View.VISIBLE);
            }else{
                TextView topText = findViewById(R.id.topText);
                topText.animate().setDuration(140).translationX(0);
                findViewById(R.id.deleteAllicon).setVisibility(View.INVISIBLE);
            }
        }
    };

    private BroadcastReceiver mMessageReceiverForShowNoAdsText = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BOOKMARKS--","Received message for showing no ads text.");
            noAdsText.setVisibility(View.VISIBLE);
        }
    };

    private BroadcastReceiver mMessageReceiverForAddView = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BOOKMARKS--","Trying to add blank view from activity...");
            try{
                mPlaceHolderView.addViewAfter(mPlaceHolderView.getViewResolverPosition(Variables.adView),
                        new BlankItem(mContext,mPlaceHolderView,Variables.lastNoOfDays,"pineapples",false));
            }catch (Exception e){
                e.printStackTrace();
                refreshActivity();
            }

        }
    };

    private void refreshActivity() {
        sendBroadCastToUnregisterReceivers();
        mPlaceHolderView.removeAllViews();
        hideProg();
        mProg.setMessage("Reloading your pinns..");
        new LongOperation().execute("");
    }

    private BroadcastReceiver mMessageReceiverForEquatePHViews = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BOOKMARKS--","Equating placeholderview");
            Variables.placeHolderView = mPlaceHolderView;
        }
    };

    private BroadcastReceiver mMessageReceiverForUnpinned = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BOOKMARKS--","Message received to show toast for unpin action");
//            iterationsFofUnpined++;
//            if(iterationsFofUnpined==Variables.numberOfUnpinns){
                Snackbar.make(findViewById(R.id.bookmarksCoordinatorLayout), R.string.unpinned,
                        Snackbar.LENGTH_SHORT).show();
                mAuthProgressDialog.dismiss();
                iterationsFofUnpined = 0;
                Variables.numberOfUnpinns = 0;
//            }
        }
    };

    private BroadcastReceiver mMessageReceiverForReceivingUnableToPinAd = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BOOKMARKS--","Unable to unpin ad.");
            Snackbar.make(findViewById(R.id.bookmarksCoordinatorLayout), R.string.failedUnpinned,
                    Snackbar.LENGTH_SHORT).show();
            mAuthProgressDialog.dismiss();
        }
    };

    private BroadcastReceiver mMessageReceiverForSharingAd = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BOOKMARKS--","Received message to share ad.");
            isStoragePermissionGranted();
        }
    };

    private BroadcastReceiver mMessageReceiverForViewingAd = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BOOKMARKS--","Received message to view ad.");
//            loadAdFragment();

            showExpandedImage(mAllAdsList.indexOf(Variables.adToBeViewed));

        }
    };

    private BroadcastReceiver mMessageReceiverForShowingAreYouSureText = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BOOKMARKS--","Received message to prompt user if they're sure they want to delete.");
            promptUserIfTheyAreSureIfTheyWantToDeleteAd();
        }
    };

    private BroadcastReceiver mMessageReceiverForShowingAreYouSureText2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BOOKMARKS--","Received message to prompt user if they're sure they want to delete.");
            promptUserIfTheyAreSureIfTheyWantToDeleteAd2();
        }
    };

    private BroadcastReceiver mMessageReceiverForContinue = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BOOKMARKS--","Received message to continue");
//            startLoadAdsIntoViews();
        }
    };

    private BroadcastReceiver mMessageReceiverForShowContact = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BOOKMARKS--","Received message to show bottom sheet");
            showBottomSheetForContact();
        }
    };

    private void showBottomSheetForContact(){
        if(Variables.adToBeViewed.didAdvertiserSetContactInfo()) {
//            ContactAdvertiserBottomsheet fragmentModalBottomSheet = new ContactAdvertiserBottomsheet();
//            fragmentModalBottomSheet.setActivity(Bookmarks.this);
//            fragmentModalBottomSheet.setAdvert(Variables.adToBeViewed);
//            fragmentModalBottomSheet.show(getSupportFragmentManager(), "BottomSheet Fragment");
            openBottomPart();
        }
//        Variables.adToBeViewed.setWebsiteLink("www.oneplus.com");
//        openBottomPart();
    }




    private void promptUserIfTheyAreSureIfTheyWantToDeleteAd2() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you really want to unpin that?")
                .setTitle("Confirm Unpin.")
                .setCancelable(true)
                .setPositiveButton("Yes.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Variables.adToBeUnpinned.getPushRefInAdminConsole());
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        mAuthProgressDialog.show();
                    }
                })
                .setNegativeButton("No!!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    private void promptUserIfTheyAreSureIfTheyWantToDeleteAd() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to unpin that?")
                .setTitle("Confirm Unpin.")
                .setCancelable(true)
                .setPositiveButton("Yes.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        Intent intent = new Intent("DELETE_PINNED_AD");
//                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        Intent intent2 = new Intent(Variables.adToBeViewed.getPushRefInAdminConsole());
                        mAllAdsList.remove(Variables.adToBeViewed);
                        Variables.loadedSavedAdsList.remove(Variables.adToBeViewed.getPushRefInAdminConsole());
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent2);
                        Variables.placeHolderView = mPlaceHolderView;
                        mAuthProgressDialog.show();
                    }
                })
                .setNegativeButton("No!!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    private void loadAdFragment() {
        FragmentManager fm = getFragmentManager();
        ViewImageFragment imageFragment = new ViewImageFragment();
        imageFragment.setMenuVisibility(false);
        imageFragment.show(fm, "View image.");
        imageFragment.setfragcontext(mContext);
        imageFragment.setActivity(this);
        imageF = imageFragment;
    }

    private void createProgressDialog(){
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle(R.string.app_name);
        mAuthProgressDialog.setMessage("Updating your preferences...");
        mAuthProgressDialog.setCancelable(false);

        mProg = new ProgressDialog(this,R.style.AppCompatAlertDialogStyle);
        mProg.setMessage("Loading your Pins...");
        mProg.setTitle(R.string.app_name);
        mProg.setCancelable(true);
        mProg.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
    }

    @Override
    public void onBackPressed(){
        if(!isCardMinimized){
            updatePosition();
            expandContactSelector();
        }else if(isConfirmOpenWebsiteLayout){
            closeConfirmWebsiteLayout();
        }else if(isConfirmDialLayout){
            closeConfirmDialLayout();
        }else if(isBottomPartOpen){
            closeBottomPart();
        } else {
            if (isLoadingAds) {
                hideProg();
                finish();
            } else {
                if (Variables.isSelectingMultipleItems) {
                    Variables.UnpinAdsList.clear();
                    LocalBroadcastManager.getInstance(mContext)
                            .sendBroadcast(new Intent(Constants.REMOVE_REMOVE_SELF_LISTENER));
                    Variables.isSelectingMultipleItems = false;
                    TextView topText = findViewById(R.id.topText);
                    topText.animate().setDuration(140).translationX(0);
                    findViewById(R.id.deleteAllicon).setVisibility(View.GONE);
                } else {
                    if (isViewPagerShowing) {
                        hideExpandedImage();
                    } else {
                        super.onBackPressed();
                    }
                }
            }
        }
    }





    private void loadAdsFromFirebase(){
        if(!mSavedAds.isEmpty()){
            mSavedAds.clear();
        }
        String uid = User.getUid();
        Query query = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid).child(Constants.PINNED_AD_LIST);
        DatabaseReference mRef = query.getRef();
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snap: dataSnapshot.getChildren()){
                    Advert advert = snap.getValue(Advert.class);
                    mSavedAds.add(advert);
                    Log.d("BOOKMARKS"," --Loaded ads from firebase.--"+advert.getPushId());
                }
                loadBookmarkedAdsIntoCards();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("UTILS","Failed to load ads from firebase.");
            }
        });
    }

    private void loadAdsFromFirebase2(){
        String uid = User.getUid();
        Query query = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.PINNED_AD_LIST);
        DatabaseReference mRef = query.getRef();

        mRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    for (final DataSnapshot snap: dataSnapshot.getChildren()){
                        final long noOfDays = Long.parseLong(snap.getKey());
                        final List<Advert> AdList = new ArrayList<>();

                        for(DataSnapshot adSnap: snap.getChildren()){
                            Advert advert = adSnap.getValue(Advert.class);

                            if(adSnap.child("contactdata").exists()){
                                advert.setAdvertiserPhoneNo(adSnap.child("contactdata").child(Constants.ADVERTISER_PHONE_NO)
                                        .getValue(String.class));
                                if(adSnap.child("contactdata").child(Constants.ADVERTISER_LOCATION).exists()){
                                    List<AdvertiserLocation> advertisersLoc = new ArrayList<>();
                                    for(DataSnapshot locSnap:adSnap.child("contactdata").child(Constants.ADVERTISER_LOCATION).getChildren()){
                                        String name = "";
                                        if(locSnap.child("name").exists())name = locSnap.child("name").getValue(String.class);
                                        double lat = locSnap.child("lat").getValue(double.class);
                                        double lng = locSnap.child("lng").getValue(double.class);
                                        advertisersLoc.add(new AdvertiserLocation(new myLatLng(lat,lng),name));
                                    }
                                    advert.setAdvertiserLocations(advertisersLoc);
                                }
                            }

                            AdList.add(advert);
                            mAllAdsList.add(advert);
                            Log.d("BOOKMARKS"," --Loaded ads from firebase.--"+advert.getPushId());
                        }
                        HashOfAds.put(noOfDays,AdList);
                        Variables.VariablesHashOfAds.put(noOfDays,AdList);
                        Log.d(TAG,"Added ads for day : "+noOfDays+" to hashmap.Adlist size is : "+AdList.size());
                    }
                    isDone = true;
                }
                isDone = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Bookmarks","Failed to load ads from firebase.");
            }
        });
    }

    private void loadImagesForEachAdFirst(){
        for(List <Advert>list: HashOfAds.values()){
            numberOfAdsLoaded+=list.size();
        }
        for (List <Advert>list: HashOfAds.values()){
            for(final Advert ad: list){
                ad.getDateInDays();
                DatabaseReference adRef2 = FirebaseDatabase.getInstance().getReference(Constants.PINNED_AD_POOL)
                        .child(Long.toString(ad.getDateInDays())).child(ad.getPushRefInAdminConsole());
                adRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                       String img = dataSnapshot.getValue(String.class);
                       ad.setImageUrl(img);
                       iterations++;
                       if(iterations==numberOfAdsLoaded){
                           startLoadAdsIntoViews();
                       }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    private void startLoadAdsIntoViews(){
//        mPlaceHolderView2.setItemViewCacheSize(HashOfAds.size());
        if(HashOfAds.isEmpty()){
            Log.d(TAG,"No ads have been loaded, perhaps user doesn't have any pinned ads");
            Toast.makeText(mContext,"You do not have any pinned ads.",Toast.LENGTH_SHORT).show();
            noAdsText.setVisibility(View.VISIBLE);
//            mAvi.setVisibility(View.GONE);
            mProgressBarBookmarks.setVisibility(View.GONE);
            loadingText.setVisibility(View.GONE);
            hideProg();
        }else{
            mPlaceHolderView2.setItemViewCacheSize(HashOfAds.size());
            Log.d(TAG,"Ads have been loaded.");
            if(cycleCount+1<=HashOfAds.size()) {
                Long days = getDaysFromHash(cycleCount);
                List<Advert> adList = HashOfAds.get(days);
                Log.d(TAG,"Loading ads : "+days);
                loadDaysAdsIntoViews2(adList, days);
            }else{
                Log.d(TAG,"Cycle-count plus one is not less than hash of ads size.");
                hideProg();
                cycleCount=0;
            }
            setTouchListenerForExpandImage();
        }
    }



    private void loadDaysAdsIntoViews(List<Advert> adList, long noOfDays) {
        if(mPlaceHolderView == null) loadPlaceHolderViews();
        Variables.daysArray.add(noOfDays);
        mPlaceHolderView.addView(new DateItem(mContext,mPlaceHolderView,noOfDays,getDateFromDays(noOfDays)));
        mPlaceHolderView.addView(new BlankItem(mContext,mPlaceHolderView,noOfDays,"",false));
        mPlaceHolderView.addView(new BlankItem(mContext,mPlaceHolderView,noOfDays,"",false));
        mPlaceHolderView.addView(new BlankItem(mContext,mPlaceHolderView,noOfDays,"",false));
        Log.d(TAG,"Adlist size for "+noOfDays+" is: "+adList.size());
        for(int i = 0; i<adList.size();i++){
            boolean islst = false;
            if(i+1==adList.size()) islst = true;
//            mPlaceHolderView.addView(new SavedAdsCard(adList.get(i),mContext,mPlaceHolderView,adList.get(i).getPushId(),noOfDays,islst));
            Log.d(TAG,"Loaded ad : "+adList.get(i).getPushId()+"; isLast item is : "+islst);
        }
        for(int i = 0;i<getNumber(adList.size());i++){
            boolean islst = false;
            if(i+1==getNumber(adList.size())) islst = false;
            mPlaceHolderView.addView(new BlankItem(mContext,mPlaceHolderView,noOfDays,"pineapples",islst));
            Log.d(TAG,"Loaded a blank item for :"+getDateFromDays(noOfDays)+"; isLast item is : "+islst);
        }
        cycleCount++;
        startLoadAdsIntoViews();
    }

    private void loadDaysAdsIntoViews2(List<Advert> adList, long noOfDays){
        int pos = getPositionOf(noOfDays);

        mPlaceHolderView2.addView(pos,new SAContainer(adList,mContext,mPlaceHolderView2,noOfDays,pos));
//        mPlaceHolderView2.addView(new SAContainer(adList,mContext,mPlaceHolderView2,noOfDays));
        cycleCount++;
        startLoadAdsIntoViews();
    }

    private void loadBookmarkedAdsIntoCards() {
        if(mPlaceHolderView == null){
            loadPlaceHolderViews();
        }
        if(mSavedAds!=null && mSavedAds.size()>0){
            for(int i = 0; i<mSavedAds.size();i++){
//                mPlaceHolderView.addView(new SavedAdsCard(mSavedAds.get(i),mContext,mPlaceHolderView,mSavedAds.get(i).getPushId()));
            }
        }else{
            Toast.makeText(mContext,"You do not have any pinned ads.",Toast.LENGTH_LONG).show();
            noAdsText.setVisibility(View.VISIBLE);
        }
        mSavedAds.clear();
//        mAvi.setVisibility(View.GONE);
        mProgressBarBookmarks.setVisibility(View.GONE);
        loadingText.setVisibility(View.GONE);

    }

    private boolean isNetworkConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }




    private void shareImage(Bitmap icon){
        if(!isSharing){
            isSharing = true;
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(30);
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/jpeg");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            icon.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            File f = new File(Environment.getExternalStorageDirectory() + File.separator + "temporary_file.jpg");
            try {
                f.createNewFile();
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(bytes.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
//        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/temporary_file.jpg"));
            share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///"+Environment.getExternalStorageDirectory().getPath()+"/temporary_file.jpg"));
            startActivity(Intent.createChooser(share, "Share Image"));
            isSharing = false;
        }
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                shareImage(Variables.loadedSavedAdsList.get(Variables.adToBeShared.getPushRefInAdminConsole()));
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            shareImage(Variables.loadedSavedAdsList.get(Variables.adToBeShared.getPushRefInAdminConsole()));
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1){
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
                //resume tasks needing this permission
                shareImage(Variables.loadedSavedAdsList.get(Variables.adToBeShared.getPushRefInAdminConsole()));
            }
        }else if(requestCode == 11){
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
                //resume tasks needing this permission
//                imageF.saveImageToDevice();
                saveImageToDevice();
            }
        }else if (requestCode==DIALER_REQUEST_CODE){
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED) openDialerAndCall();
        }

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

        Log.d("Splash","Date gotten is : "+dayOfMonth+" "+monthName+" "+year);

        Calendar cal2 = Calendar.getInstance();
        int year2 = cal2.get(Calendar.YEAR);
        String yearName;

        if(year == year2){
            Log.d(TAG,"Ad was pined this year...");
            yearName = "";
        }else if(year2 == year+1){
            Log.d(TAG,"Ad was pined last year...");
            yearName =", "+Integer.toString(year);
        }else{
            yearName =", "+ Integer.toString(year);
        }

        return dayOfMonth+" "+monthName+yearName;
    }

    public String getMonthName_Abbr(int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month);
        SimpleDateFormat month_date = new SimpleDateFormat("MMM");
        String month_name = month_date.format(cal.getTime());
        return month_name;
    }




    private int getNumber(int size){
        int newSize = size;
        int number = 0;
        while (newSize%4!=0){
            newSize++;
            number++;
        }

        return number;
    }

    private Long getDaysFromHash(int pos){
        LinkedHashMap map = HashOfAds;
        Long Sub = (new ArrayList<Long>(map.keySet())).get(pos);
        Log.d(TAG, "Date gotten from getDaysFromHash method is :" + Sub);
        return Sub;
    }

    private class LongOperation extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            try{
                loadAdsFromFirebase2();
                while(!isDone){
                    executeStuff();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return "executed";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d(TAG,"Starting to load ads into views from onPostExecute");
            cycleCount=0;
            startLoadAdsIntoViews();
//            hideProg();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(!Variables.daysArray.isEmpty())Variables.daysArray.clear();
        }
    }

    private void executeStuff() {

    }



    private int getPositionOf(Long noOfDaysDate) {
        LinkedHashMap map = HashOfAds;
        List<Long> indexes = new ArrayList<Long>(map.keySet());
        return indexes.indexOf(noOfDaysDate);
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
            Intent intent = new Intent(Bookmarks.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else{
            isNeedToLoadLogin = true;
        }

    }



    private void showExpandedImage(int positionToStart){
        isViewPagerShowing = true;
        vpPager =  findViewById(R.id.viewPager);
        animateExpand();
//        vpPager.setVisibility(View.VISIBLE);
        setBackViewsToBeInvisible();

        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        adapterViewPager.setfragcontext(mContext);
        adapterViewPager.setActivity(Bookmarks.this);
        adapterViewPager.setViewingAdsList(mAllAdsList);
        vpPager.setAdapter(adapterViewPager);
        vpPager.setCurrentItem(positionToStart);
        setBottomActionListeners();
        currentPagePosition = positionToStart;
        vpPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            // This method will be invoked when a new page becomes selected.
            @Override
            public void onPageSelected(int position) {
                currentPagePosition = position;
                try{
                    Variables.adToBeViewed = mAllAdsList.get(currentPagePosition);
                    if(!Variables.adToBeViewed.didAdvertiserSetContactInfo()) {
                        findViewById(R.id.Website).setAlpha(0.4f);
                        findViewById(R.id.websiteTextxx).setAlpha(0.4f);
                    }
                    TextView tv = findViewById(R.id.dateView);
                    tv.setText(getDateFromDays(Variables.adToBeViewed.getDateInDays()));
                }catch (Exception e){
                    e.printStackTrace();
                }
//                Toast.makeText(Bookmarks.this, "Selected page position: " + position, Toast.LENGTH_SHORT).show();
            }

            // This method will be invoked when the current page is scrolled
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Code goes here
            }

            // Called when the scroll state changes:
            // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
            @Override
            public void onPageScrollStateChanged(int state) {
                // Code goes here
            }
        });
        try{
            Variables.adToBeViewed = mAllAdsList.get(positionToStart);
            if(!Variables.adToBeViewed.didAdvertiserSetContactInfo()) {
                findViewById(R.id.Website).setAlpha(0.4f);
                findViewById(R.id.websiteTextxx).setAlpha(0.4f);
            }
            TextView tv = findViewById(R.id.dateView);
            tv.setText(getDateFromDays(Variables.adToBeViewed.getDateInDays()));
        }catch (Exception e){
            e.printStackTrace();
        }
        addTouchListenerForVPagerNavs();
        View adTouchDetector = findViewById(R.id.adTouchDetector);
        adTouchDetector.setVisibility(View.GONE);
    }

    private void hideExpandedImage(){
        isViewPagerShowing = false;
        adapterViewPager = null;
        ViewPager vpPager =  findViewById(R.id.viewPager);
//        vpPager.setVisibility(View.GONE);
        animateCollapse();
        findViewById(R.id.swipeUpDownDetector).setVisibility(View.GONE);
//        vpPager.removeAllViews();
        setBackViewsToBeNormal();

    }

    private void animateCollapse(){
        vpPager = findViewById(R.id.viewPager);
        int leftMargin = getScreenWidth();
        int topMargin = Utils.dpToPx(100);
        int bottomMargin = Utils.dpToPx(100);
        int rightMargin = -getScreenWidth();

        final CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) vpPager.getLayoutParams();

        ValueAnimator animatorRight;
        ValueAnimator animatorLeft;
        ValueAnimator animatorBot;
        ValueAnimator animatorTop;

        animatorRight = ValueAnimator.ofInt(0);
        animatorLeft = ValueAnimator.ofInt(0);
        animatorTop = ValueAnimator.ofInt(0,400);
        animatorBot = ValueAnimator.ofInt(0,-400);


        animatorRight.setInterpolator(new LinearOutSlowInInterpolator());
        animatorBot.setInterpolator(new LinearOutSlowInInterpolator());
        animatorLeft.setInterpolator(new LinearOutSlowInInterpolator());
        animatorTop.setInterpolator(new LinearOutSlowInInterpolator());

        animatorRight.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.rightMargin = (Integer) valueAnimator.getAnimatedValue();
                vpPager.requestLayout();
            }
        });

        animatorLeft.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.leftMargin = (Integer) valueAnimator.getAnimatedValue();
                vpPager.requestLayout();
            }
        });

        animatorTop.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.topMargin = (Integer) valueAnimator.getAnimatedValue();
                vpPager.requestLayout();
            }
        });

        animatorBot.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.bottomMargin = (Integer) valueAnimator.getAnimatedValue();
                vpPager.requestLayout();
            }
        });


        animatorBot.setDuration(mAnimationTime);
        animatorTop.setDuration(mAnimationTime);
        animatorLeft.setDuration(mAnimationTime);
        animatorRight.setDuration(mAnimationTime);

        animatorBot.start();
        animatorTop.start();
        animatorLeft.start();
        animatorRight.start();

        vpPager.animate().alpha(0f).setDuration(250).setInterpolator(new LinearOutSlowInInterpolator()).start();

        animatorRight.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                vpPager.setVisibility(View.GONE);
                vpPager.setAlpha(1f);
                vpPager.removeAllViews();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        hasEventBeenSet = false;
        hasAdBeenSet = false;

        RelativeLayout topNavButtons = findViewById(R.id.topNavButtons);
        topNavButtons.animate().alpha(0f).translationY(Utils.dpToPx(50)).setDuration(mAnimationTime)
                .setInterpolator(new LinearOutSlowInInterpolator()).start();

        LinearLayout bottomNavButtons = findViewById(R.id.bottomNavButtons);
        bottomNavButtons.animate().alpha(0f).translationY(Utils.dpToPx(50)).setDuration(mAnimationTime)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                findViewById(R.id.bottomNavButtons).setVisibility(View.GONE);
                findViewById(R.id.topNavButtons).setVisibility(View.GONE);

                findViewById(R.id.bottomNavButtons).setAlpha(0f);
                findViewById(R.id.topNavButtons).setAlpha(0f);

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();

        isSetUpSharing = false;
        findViewById(R.id.isSharingProgress).setVisibility(View.INVISIBLE);
    }

    private void animateExpand(){
        vpPager = findViewById(R.id.viewPager);

        int leftMargin = 0;
        int topMargin = getScreenHeight()/2;
        int bottomMargin = -getScreenHeight()/2;
        int rightMargin = 0;

        if(Variables.activeEvent!=null) {
            Log.e(TAG,"Raw values  getRawX: "+Variables.activeEvent.getRawX()+" getRawY: "+Variables.activeEvent.getRawY());
            leftMargin = (int) (Variables.activeEvent.getRawX());
            topMargin = (int) (Variables.activeEvent.getRawY());
            bottomMargin = getScreenHeight() - (topMargin +45);
            rightMargin = getScreenWidth() - (leftMargin +45);
        }else Log.e(TAG,"active event is null");

        final CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) vpPager.getLayoutParams();

        params.leftMargin = leftMargin;
        params.topMargin = topMargin;
        params.rightMargin = rightMargin;
        params.bottomMargin = bottomMargin;
        vpPager.setLayoutParams(params);

        vpPager.setVisibility(View.VISIBLE);

        ValueAnimator animatorRight;
        ValueAnimator animatorLeft;
        ValueAnimator animatorBot;
        ValueAnimator animatorTop;

        animatorRight = ValueAnimator.ofInt(rightMargin,0);
        animatorLeft = ValueAnimator.ofInt(leftMargin,0);
        animatorTop = ValueAnimator.ofInt(topMargin,0);
        animatorBot = ValueAnimator.ofInt(bottomMargin,0);


//        animatorRight.setInterpolator(new LinearOutSlowInInterpolator());
//        animatorBot.setInterpolator(new LinearOutSlowInInterpolator());
//        animatorLeft.setInterpolator(new LinearOutSlowInInterpolator());
//        animatorTop.setInterpolator(new LinearOutSlowInInterpolator());

        animatorRight.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.rightMargin = (Integer) valueAnimator.getAnimatedValue();
                vpPager.requestLayout();
            }
        });

        animatorLeft.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.leftMargin = (Integer) valueAnimator.getAnimatedValue();
                vpPager.requestLayout();
            }
        });

        animatorTop.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.topMargin = (Integer) valueAnimator.getAnimatedValue();
                vpPager.requestLayout();
            }
        });

        animatorBot.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.bottomMargin = (Integer) valueAnimator.getAnimatedValue();
                vpPager.requestLayout();
            }
        });


//        animatorBot.setDuration(mAnimationTime);
//        animatorTop.setDuration(mAnimationTime);
//        animatorLeft.setDuration(mAnimationTime);
//        animatorRight.setDuration(mAnimationTime);

        animatorBot.setDuration(mAnimationTime-110);
        animatorTop.setDuration(mAnimationTime-110);
        animatorLeft.setDuration(mAnimationTime-100);
        animatorRight.setDuration(mAnimationTime-100);

        animatorBot.start();
        animatorTop.start();
        animatorLeft.start();
        animatorRight.start();

        animatorRight.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

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

        RelativeLayout topNavButtons = findViewById(R.id.topNavButtons);
        topNavButtons.animate().alpha(1f).translationY(0).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator()).start();

        LinearLayout bottomNavButtons = findViewById(R.id.bottomNavButtons);
        bottomNavButtons.animate().alpha(1f).translationY(0).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        findViewById(R.id.bottomNavButtons).setVisibility(View.VISIBLE);
                        findViewById(R.id.topNavButtons).setVisibility(View.VISIBLE);

                        findViewById(R.id.bottomNavButtons).setAlpha(1f);
                        findViewById(R.id.topNavButtons).setAlpha(1f);

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

    }

    private int getScreenWidth(){
        Display display = getWindowManager(). getDefaultDisplay();
        Point size = new Point();
        display. getSize(size);
        return size. x;
    }

    private int getScreenHeight(){
        Display display = getWindowManager(). getDefaultDisplay();
        Point size = new Point();
        display. getSize(size);
        return size. y;
    }




    private void setBackViewsToBeInvisible() {
        mPlaceHolderView.setAlpha(0.1f);
        mPlaceHolderView2.setAlpha(0.1f);
        findViewById(R.id.AdArchivesLayout).setAlpha(0f);
        findViewById(R.id.topNavButtons).setVisibility(View.VISIBLE);
    }

    private void setBackViewsToBeNormal(){
        mPlaceHolderView.setAlpha(1f);
        mPlaceHolderView2.setAlpha(1f);
        findViewById(R.id.AdArchivesLayout).setAlpha(1f);
//        findViewById(R.id.bottomNavButtons).setVisibility(View.GONE);
//        findViewById(R.id.topNavButtons).setVisibility(View.GONE);
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {
        private List<Advert> viewingList;
        private Activity mActivity;
        private Context mContext;

        public MyPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }


        public void setfragcontext(Context context){
            mContext = context;
        }

        public void setActivity(Activity activity){
            this.mActivity = activity;
        }

        public void setViewingAdsList(List<Advert> adsLists){
            viewingList = adsLists;
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return viewingList.size();
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            ExpandedImageFragment frag = new ExpandedImageFragment();
            frag.setActivity(mActivity);
            frag.setfragcontext(mContext);
            frag.setAdvert(viewingList.get(position));
            frag.setPosition(position);
            return frag;
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }

    }

    public void setBottomActionListeners(){
        findViewById(R.id.bottomNavButtons).setVisibility(View.VISIBLE);

        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isEnabled)onBackPressed();
            }
        });
        findViewById(R.id.shareBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Variables.adToBeShared = mAllAdsList.get(currentPagePosition);
                if(Variables.loadedSavedAdsList.get(Variables.adToBeShared.getPushRefInAdminConsole())!=null) {
                    if(isEnabled){
//                            findViewById(R.id.isSharingProgress).setVisibility(View.VISIBLE);
                            isStoragePermissionGranted();

                    }
                }
            }
        });
        findViewById(R.id.Website).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isEnabled)showBottomSheetForContact();
            }
        });
        findViewById(R.id.Delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isEnabled) {
                    Variables.adToBeViewed = mAllAdsList.get(currentPagePosition);
                    hideExpandedImage();
                    promptUserIfTheyAreSureIfTheyWantToDeleteAd();
                }
            }
        });
        findViewById(R.id.Download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Variables.loadedSavedAdsList.get(Variables.adToBeViewed.getPushRefInAdminConsole())!=null){
                    if(isEnabled)saveImageDialog();
                }
            }
        });

    }



    private void saveImageDialog() {
        final Dialog d = new Dialog(Bookmarks.this);
        d.setTitle("Save to Device.");
        d.setContentView(R.layout.dialog991);
        Button b1 = d.findViewById(R.id.okBtn);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
                checkZePermission();
            }
        });
        d.show();

    }

    private void checkZePermission(){
        if (Build.VERSION.SDK_INT >= 23) {
            if (mContext.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                saveImageToDevice();
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(Bookmarks.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 11);
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            saveImageToDevice();
        }
    }

    public void saveImageToDevice(){
        Bitmap imageToSave = Variables.loadedSavedAdsList.get(Variables.adToBeViewed.getPushRefInAdminConsole());
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

    public String randomInt(){
        Random rand = new Random();
        int max = 1000000000;
        int min = 1;
        int n = rand.nextInt(max) + min;
        return Integer.toString(n);
    }

    private void setSavingImageName(String image){
        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String dateInDays = Long.toString(Variables.adToBeViewed.getDateInDays());
        DatabaseReference mref = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(user).child(Constants.PINNED_AD_LIST).child(dateInDays).child(Variables.adToBeViewed.getPushId())
                .child("downloadImageName");
        mref.setValue(image);
    }



    private void setViews(){
        WebViewContainer = findViewById(R.id.WebViewContainer);
        cardContainer = findViewById(R.id.cardContainer);
        myWebView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progressBar);
        mReloadButton = findViewById(R.id.reloadBtn);
        backButton = findViewById(R.id.backBtnX);
        websiteText = findViewById(R.id.websiteText);
        closeBtn = findViewById(R.id.closeBtn);
        collapseIcon = findViewById(R.id.collapseIcon);
        minifiedTextView = findViewById(R.id.minifiedTextView);
        rightButtonLayout = findViewById(R.id.rightButtonLayout);
        backBtnLayout = findViewById(R.id.backBtnLayout);
        swipeBackView = findViewById(R.id.swipeBackView);
        scrollProgress = findViewById(R.id.scrollProgress);

        ContactSelectorContainer = findViewById(R.id.ContactSelectorContainer);
        CallLayout = findViewById(R.id.CallLayout);
        websiteLayout = findViewById(R.id.websiteLayout);
        LocationLayout = findViewById(R.id.LocationLayout);
    }

    private void updatePosition(){
        if(isCardMinimized){
            expandCard();
            isCardMinimized = false;
        }else{
            collapseCard();
            isCardMinimized = true;
        }
    }

    private void expandCard(){
        showWebViews();
        WebViewContainer.setVisibility(View.VISIBLE);
        final CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) WebViewContainer.getLayoutParams();

        ValueAnimator animatorRight;
        ValueAnimator animatorLeft;
        ValueAnimator animatorBot;
        ValueAnimator animatorTop;

        animatorRight = ValueAnimator.ofInt(150,50,0);
        animatorLeft = ValueAnimator.ofInt(150,50,0);

        animatorTop = ValueAnimator.ofInt(500,350,0);
        animatorBot = ValueAnimator.ofInt(500,350,0);


        animatorRight.setInterpolator(new LinearOutSlowInInterpolator());
        animatorBot.setInterpolator(new LinearOutSlowInInterpolator());
        animatorLeft.setInterpolator(new LinearOutSlowInInterpolator());
        animatorTop.setInterpolator(new LinearOutSlowInInterpolator());

        animatorRight.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.rightMargin = (Integer) valueAnimator.getAnimatedValue();
                WebViewContainer.requestLayout();
            }
        });

        animatorLeft.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.leftMargin = (Integer) valueAnimator.getAnimatedValue();
                WebViewContainer.requestLayout();
            }
        });

        animatorTop.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.topMargin = (Integer) valueAnimator.getAnimatedValue();
                WebViewContainer.requestLayout();
            }
        });

        animatorBot.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.bottomMargin = (Integer) valueAnimator.getAnimatedValue();
                WebViewContainer.requestLayout();
            }
        });


        animatorBot.setDuration(mAnimationTime);
        animatorTop.setDuration(mAnimationTime);
        animatorLeft.setDuration(mAnimationTime);
        animatorRight.setDuration(mAnimationTime);

        animatorBot.start();
        animatorTop.start();
        animatorLeft.start();
        animatorRight.start();

        animatorRight.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                RunAnimationEndListeners();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

    }

    private void collapseCard(){
        hideWebViews();
        final CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) WebViewContainer.getLayoutParams();

        ValueAnimator animatorRight;
        ValueAnimator animatorLeft;
        ValueAnimator animatorBot;
        ValueAnimator animatorTop;

        animatorRight = ValueAnimator.ofInt(0,250,250);
        animatorLeft = ValueAnimator.ofInt(0,250,250);
        animatorTop = ValueAnimator.ofInt(0,350,450);
        animatorBot = ValueAnimator.ofInt(0,350,350);

        animatorRight.setInterpolator(new LinearOutSlowInInterpolator());
        animatorBot.setInterpolator(new LinearOutSlowInInterpolator());
        animatorLeft.setInterpolator(new LinearOutSlowInInterpolator());
        animatorTop.setInterpolator(new LinearOutSlowInInterpolator());

        animatorRight.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.rightMargin = (Integer) valueAnimator.getAnimatedValue();
                WebViewContainer.requestLayout();
            }
        });

        animatorLeft.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.leftMargin = (Integer) valueAnimator.getAnimatedValue();
                WebViewContainer.requestLayout();
            }
        });

        animatorTop.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.topMargin = (Integer) valueAnimator.getAnimatedValue();
                WebViewContainer.requestLayout();
            }
        });

        animatorBot.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.bottomMargin = (Integer) valueAnimator.getAnimatedValue();
                WebViewContainer.requestLayout();
            }
        });


        animatorBot.setDuration(mAnimationTime);
        animatorTop.setDuration(mAnimationTime);
        animatorLeft.setDuration(mAnimationTime+100);
        animatorRight.setDuration(mAnimationTime+100);

        animatorBot.start();
        animatorTop.start();
        animatorLeft.start();
        animatorRight.start();

        WebViewContainer.animate().alpha(0f).setDuration(mAnimationTime).start();
        animatorTop.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                OnCollapseCard();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

    }

    private void OnCollapseCard(){
        WebViewContainer.setVisibility(View.GONE);
        WebViewContainer.setAlpha(1f);
        if(hasPageBeenOpened){
            hasPageBeenOpened = false;
            if (Build.VERSION.SDK_INT < 18) {
                myWebView.clearView();
                myWebView.clearHistory();
            } else {
                myWebView.loadUrl("about:blank");
                myWebView.clearHistory();
            }
        }
    }

    private void RunAnimationEndListeners() {
        setUpWebView();
        setUpScrollProgress();
    }



    @SuppressLint("SetJavaScriptEnabled")
    private void setUpWebView(){
        WebSettings webSettings = myWebView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        myWebView.loadUrl(PAGE);
        hasPageBeenOpened = true;
        Variables.hasReachedBottomOfPage = false;
        noOfStrikes = 0;
        scrollConfirmBoolean = false;

        myWebView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                try {
                    updateToSpecificUrl(url);
                    UpdateButtonsAndAll();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

        });
        myWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int progress) {
                if(!isProgressShowing) showProgressBar();
                updateProgress(progress);

                // Return the app name after finish loading
                if(progress == 100){
                    hideProgress();
                    UpdateButtonsAndAll();
                    updateUrl();

                    String currentUrl = myWebView.getUrl();
                    Log.e("MainAct: ","current url"+currentUrl);
                    if(!currentUrl.contains("https://")){
                        secureImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_not_secure));
                    }else{
                        secureImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_secure));
                    }
                    secureImage.setVisibility(View.VISIBLE);
                    secureImage.animate().translationX(0).alpha(1f).setDuration(animationTime).setInterpolator(new LinearOutSlowInInterpolator())
                            .setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    secureImage.setTranslationX(0);
                                    secureImage.setAlpha(1f);
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

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                super.onReceivedIcon(view, icon);
                pageIcon.setImageBitmap(icon);
            }
        });

        addClickListeners();
        updateUrl();
        UpdateButtonsAndAll();
        setPapeHeight();
    }

    private void setPapeHeight(){
        ViewTreeObserver viewTreeObserver  = myWebView.getViewTreeObserver();

        viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                int height = myWebView.getMeasuredHeight();
                if( height != 0 ){
                    pageHeight = height;
                    myWebView.getViewTreeObserver().removeOnPreDrawListener(this);
                }
                return false;
            }
        });
    }

    private void addClickListeners(){
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(myWebView.canGoBack()){
                    String url = myWebView.copyBackForwardList().getItemAtIndex(myWebView.copyBackForwardList().getSize()-1).getUrl();
                    if(!url.contains("about:blank")) {
                        updateToSpecificUrl(url);
                        UpdateButtonsAndAll();
                        myWebView.goBack();

                        Animation pulse = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.pulse);
                        backButton.startAnimation(pulse);
                    }

                }

            }
        });

        mReloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myWebView.reload();
                UpdateButtonsAndAll();
                updateUrl();
            }
        });


        websiteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isMinified)minifyTheTopPart();
            }
        });

        minifiedTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unMinifyTheTopPart();
            }
        });

        addTouchListenerForSwipeBack();
    }

    private void updateUrl(){
        websiteText.setText(getMyVersionOfTheUrl());
        minifiedTextView.setText(getMyVersionOfTheUrl());
    }



    private String getMyVersionOfTheUrl(){
        String newUrl;
        if(myWebView.getUrl().length()>50){
            String s = myWebView.getUrl().substring(0, Math.min(99, myWebView.getUrl().length() - 1));
            newUrl = getTrimmedUrl(s) +"...";
        }else{
            String url = myWebView.getUrl();
            newUrl = getTrimmedUrl(url);
        }
        return newUrl;
    }

    private void updateToSpecificUrl(String url){
        String newUrl;
        if(url.length()>50){
            String s = url.substring(0, Math.min(99, url.length() - 1));
            newUrl = getTrimmedUrl(s) +"...";
        }else{
            newUrl = getTrimmedUrl(url);
        }
        websiteText.setText(newUrl);
        minifiedTextView.setText(newUrl);
    }

    private String getTrimmedUrl(String s){
        String newUrl = s;
        if(s.contains("https://www.")) {
            newUrl = s.replace("https://www.", "");
        }else if(s.contains("http://www.")){
            newUrl = s.replace("http://www.", "");
        }else if(s.contains("https://")){
            newUrl = s.replace("https://", "");
        }else if(s.contains("http://")){
            newUrl = s.replace("http://", "");
        }
        if(newUrl.charAt(newUrl.length() - 1) == '/'){
            newUrl = newUrl.substring(0, newUrl.length() - 1);
        }
        return newUrl;
    }

    private void UpdateButtonsAndAll(){
        if(myWebView.canGoBack()){
            backButton.setAlpha(1f);
        }else{
            backButton.setAlpha(0.3f);
        }
    }

    private void showProgressBar(){
        isProgressShowing = true;
        progressBar.setVisibility(View.VISIBLE);

        Drawable progressDrawable = progressBar.getProgressDrawable().mutate();
        progressDrawable.setColorFilter(getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
        progressBar.setProgressDrawable(progressDrawable);
        progressBar.setProgress(1);

    }




    private void hideProgress(){
        isProgressShowing = false;
        progressBar.setVisibility(View.INVISIBLE);
        progressBar.setProgress(0);

    }

    private void updateProgress(int prog){
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", prog);
        progressAnimator.setDuration(animationTime);
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.start();

    }

    private void hideWebViews(){
        websiteText.setText("");
        minifiedTextView.setText("");

        myWebView.setVisibility(View.INVISIBLE);
        backButton.setVisibility(View.INVISIBLE);
        mReloadButton.setVisibility(View.INVISIBLE);
//        closeBtn.setVisibility(View.INVISIBLE);
        collapseIcon.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
//        progressBar2.setVisibility(View.INVISIBLE);
        swipeBackView.setVisibility(View.GONE);
        scrollProgressLayout.setVisibility(View.GONE);
    }

    private void showWebViews(){
        myWebView.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.VISIBLE);
        mReloadButton.setVisibility(View.VISIBLE);
//        closeBtn.setVisibility(View.VISIBLE);
        collapseIcon.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        swipeBackView.setVisibility(View.VISIBLE);

        scrollProgressLayout.setVisibility(View.VISIBLE);
    }




    private void minifyTheTopPart(){
        isMinified = true;
        minifiedTextView.setVisibility(View.VISIBLE);
        rightButtonLayout.setVisibility(View.GONE);

        backBtnLayout.animate().translationY(-100).setDuration(normalDuration).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                backBtnLayout.setVisibility(View.GONE);
                backBtnLayout.setTranslationY(0);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        final CardView.LayoutParams params = (CardView.LayoutParams) minifiedTextView.getLayoutParams();

        ValueAnimator animatorTop;
        animatorTop = ValueAnimator.ofInt(Utils.dpToPx(58),Utils.dpToPx(45),Utils.dpToPx(29));
        animatorTop.setInterpolator(new LinearOutSlowInInterpolator());
        animatorTop.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.height = (Integer) valueAnimator.getAnimatedValue();
                minifiedTextView.requestLayout();
            }
        });
        animatorTop.setDuration(normalDuration).start();
//        minifiedTextView.setTranslationX(-Utils.dpToPx(60));
        minifiedTextView.animate().setDuration(normalDuration).translationX(0).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        minifiedTextView.setTranslationX(0);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();


        final CardView.LayoutParams params2 = (CardView.LayoutParams) progressBar.getLayoutParams();

        ValueAnimator animatorTop2;
        animatorTop2 = ValueAnimator.ofInt(Utils.dpToPx(58),Utils.dpToPx(45),Utils.dpToPx(29));
        animatorTop2.setInterpolator(new LinearOutSlowInInterpolator());
        animatorTop2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params2.topMargin = (Integer) valueAnimator.getAnimatedValue();
                progressBar.requestLayout();
            }
        });
        animatorTop2.setDuration(normalDuration).start();


        final CardView.LayoutParams params3 = (CardView.LayoutParams) scrollProgressLayout.getLayoutParams();

        ValueAnimator animatorTop3;
        animatorTop3 = ValueAnimator.ofInt(Utils.dpToPx(19),Utils.dpToPx(16),Utils.dpToPx(14),Utils.dpToPx(5));
        animatorTop3.setInterpolator(new LinearOutSlowInInterpolator());
        animatorTop3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params3.topMargin = (Integer) valueAnimator.getAnimatedValue();
                scrollProgressLayout.requestLayout();
            }
        });
        animatorTop3.setDuration(normalDuration).start();

        ValueAnimator animator4;
        animator4 = ValueAnimator.ofInt(Utils.dpToPx(60),Utils.dpToPx(40),Utils.dpToPx(20),Utils.dpToPx(10));
        animator4.setInterpolator(new LinearOutSlowInInterpolator());
        animator4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params3.rightMargin = (Integer) valueAnimator.getAnimatedValue();
                scrollProgressLayout.requestLayout();
            }
        });
        animator4.setStartDelay(20);
        animator4.setDuration(normalDuration+100).start();

        myWebView.animate().translationY(Utils.dpToPx(29)).setDuration(normalDuration).start();

        pageIconLayout.setVisibility(View.VISIBLE);
        final CardView.LayoutParams params4 = (CardView.LayoutParams) pageIconLayout.getLayoutParams();
        ValueAnimator animator5;
        animator5 = ValueAnimator.ofInt(Utils.dpToPx(19),Utils.dpToPx(14),Utils.dpToPx(9),Utils.dpToPx(3));
        animator5.setInterpolator(new LinearOutSlowInInterpolator());
        animator5.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params4.topMargin = (Integer) valueAnimator.getAnimatedValue();
                pageIconLayout.requestLayout();
            }
        });
        animator5.setDuration(normalDuration).start();
        pageIconLayout.animate().alpha(1f).setInterpolator(new LinearInterpolator()).setDuration(normalDuration)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        pageIconLayout.setAlpha(1f);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();

    }

    private void unMinifyTheTopPart(){
        isMinified = false;
        minifiedTextView.setVisibility(View.INVISIBLE);
        rightButtonLayout.setVisibility(View.VISIBLE);
        backBtnLayout.setVisibility(View.VISIBLE);
//        progressBar2.setVisibility(View.INVISIBLE);
//        if(isProgressShowing)progressBar.setVisibility(View.VISIBLE);
        final CardView.LayoutParams params = (CardView.LayoutParams) backBtnLayout.getLayoutParams();

        ValueAnimator animatorTop;
        animatorTop = ValueAnimator.ofInt(Utils.dpToPx(29),Utils.dpToPx(40),Utils.dpToPx(50),Utils.dpToPx(58));
        animatorTop.setInterpolator(new LinearOutSlowInInterpolator());
        animatorTop.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params.height = (Integer) valueAnimator.getAnimatedValue();
                backBtnLayout.requestLayout();
            }
        });
//        websiteText.setTranslationX(Utils.dpToPx(70));
        websiteText.animate().setDuration(normalDuration).translationX(0).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        websiteLayout.setTranslationX(0);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();


        final CardView.LayoutParams params2 = (CardView.LayoutParams) rightButtonLayout.getLayoutParams();
        ValueAnimator animatorTop2;
        animatorTop2 = ValueAnimator.ofInt(Utils.dpToPx(29),Utils.dpToPx(40),Utils.dpToPx(50),Utils.dpToPx(58));
        animatorTop2.setInterpolator(new LinearOutSlowInInterpolator());
        animatorTop2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params2.height = (Integer) valueAnimator.getAnimatedValue();
                rightButtonLayout.requestLayout();
            }
        });

//        Runnable r = new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        };

        animatorTop.setDuration(normalDuration).start();
        animatorTop2.setDuration(normalDuration).start();

        final CardView.LayoutParams params3 = (CardView.LayoutParams) progressBar.getLayoutParams();

        ValueAnimator animatorTop3;
        animatorTop3 = ValueAnimator.ofInt(Utils.dpToPx(29),Utils.dpToPx(40),Utils.dpToPx(50),Utils.dpToPx(58));
        animatorTop3.setInterpolator(new LinearOutSlowInInterpolator());
        animatorTop3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params3.topMargin = (Integer) valueAnimator.getAnimatedValue();
                progressBar.requestLayout();
            }
        });
        animatorTop3.setDuration(normalDuration).start();

//        new Handler().postDelayed(r, normalDuration);


        final CardView.LayoutParams params4 = (CardView.LayoutParams) scrollProgressLayout.getLayoutParams();

        ValueAnimator animatorTop4;
        animatorTop4 = ValueAnimator.ofInt(Utils.dpToPx(10),Utils.dpToPx(14),Utils.dpToPx(18),Utils.dpToPx(19));
        animatorTop4.setInterpolator(new LinearOutSlowInInterpolator());
        animatorTop4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params4.topMargin = (Integer) valueAnimator.getAnimatedValue();
                scrollProgressLayout.requestLayout();
            }
        });

        animatorTop4.setDuration(normalDuration+100).start();

        ValueAnimator animator4;
        animator4 = ValueAnimator.ofInt(Utils.dpToPx(20),Utils.dpToPx(40),Utils.dpToPx(60),Utils.dpToPx(80));
        animator4.setInterpolator(new LinearOutSlowInInterpolator());
        animator4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params4.rightMargin = (Integer) valueAnimator.getAnimatedValue();
                scrollProgressLayout.requestLayout();
            }
        });
        animator4.setDuration(normalDuration).start();

        myWebView.animate().translationY(Utils.dpToPx(58)).setDuration(normalDuration).start();

        final CardView.LayoutParams params5 = (CardView.LayoutParams) pageIconLayout.getLayoutParams();
        ValueAnimator animator5;
        animator5 = ValueAnimator.ofInt(Utils.dpToPx(5),Utils.dpToPx(14),Utils.dpToPx(16),Utils.dpToPx(20));
        animator5.setInterpolator(new LinearOutSlowInInterpolator());
        animator5.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                params5.topMargin = (Integer) valueAnimator.getAnimatedValue();
                pageIconLayout.requestLayout();
            }
        });
        animator5.setDuration(normalDuration).start();
        pageIconLayout.animate().alpha(0f).setInterpolator(new LinearInterpolator()).setDuration(normalDuration-100)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        pageIconLayout.setAlpha(0f);
                        pageIconLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();

        mReloadButton.setTranslationX(Utils.dpToPx(40));
        mReloadButton.animate().translationX(0).setInterpolator(new LinearOutSlowInInterpolator()).setDuration(normalDuration)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        mReloadButton.setTranslationX(0);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addTouchListener() {
        mDetector = new GestureDetector(this, new Bookmarks.MyGestureListener());
        myWebView.setOnTouchEvent(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (mDetector.onTouchEvent(motionEvent)) {
                    return true;
                }
                if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(mIsScrolling) {
                        Log.d("touchListener"," onTouch ACTION_UP");
                        mIsScrolling  = false;

                        int UpScore = 0;
                        int DownScore = 0;
                        if(RawList.size()>15){
                            for(int i= RawList.size()-1 ;i>15;i--){
                                int num1 = RawList.get(i);
                                int numBefore1 = RawList.get(i - 1);

                                if(numBefore1>num1) DownScore++;
                                else UpScore++;
                            }
                        }else{
                            for(int i= RawList.size()-1 ;i>0;i--){
                                int num1 = RawList.get(i);
                                int numBefore1 = RawList.get(i - 1);

                                if(numBefore1>num1) DownScore++;
                                else UpScore++;
                            }
                        }

                        if(RawList.size()>0) {
                            int scrollAmm = Math.abs((RawList.get(RawList.size() - 1)) - (RawList.get(0)));
                            Log.d("MainAct", "scrollAmm: " + scrollAmm);
                            if(scrollAmm>SCROLL_AMOUNT_THRESHOLD && !isProgressShowing){
                                if(DownScore>UpScore){
                                    if(scrollConfirmBoolean){
                                        if(didScrollChangeListenerSetScrollConfirmBoolean){
                                            updateScrollProgress();
                                        }else{
                                            scrollConfirmBoolean = false;
                                        }
                                    }else{
                                        scrollConfirmBoolean = true;
                                        didScrollChangeListenerSetScrollConfirmBoolean = false;
                                    }
                                }
                                else if(UpScore>DownScore){
                                    if(Variables.hasReachedBottomOfPage){
                                        if(scrollConfirmBoolean){
                                            if(didScrollChangeListenerSetScrollConfirmBoolean){
                                                updateScrollProgress();
                                            }else{
                                                scrollConfirmBoolean = false;
                                            }
                                        }else{
                                            scrollConfirmBoolean = true;
                                            didScrollChangeListenerSetScrollConfirmBoolean = false;
                                        }
                                    }else{
                                        TellUserToScrollDown();
                                    }
                                }
                            }
                        }
                        RawList.clear();
                    };
                }
                return false;
            }
        });

        myWebView.setOnScrollChangedCallback(new ObservableWebView.OnScrollChangedCallback(){
            public void onScroll(int l, int t, int oldl, int oldt){
                if(t> oldt){
                    if(scrollConfirmBoolean){
                        if(!didScrollChangeListenerSetScrollConfirmBoolean){
                            updateScrollProgress();
                        }else{
                            scrollConfirmBoolean = false;
                        }
                    }else{
                        scrollConfirmBoolean = true;
                        didScrollChangeListenerSetScrollConfirmBoolean = true;
                    }
                } else if(t< oldt){
                    if(scrollConfirmBoolean){
                        if(!didScrollChangeListenerSetScrollConfirmBoolean){
                            updateScrollProgress();
                        }else{
                            scrollConfirmBoolean = false;
                        }
                    }else{
                        scrollConfirmBoolean = true;
                        didScrollChangeListenerSetScrollConfirmBoolean = true;
                    }
                }
                scrollSoFar = t;
            }
        });

    }




    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        int origX = 0;
        int origY = 0;


        @Override
        public boolean onDown(MotionEvent event) {
            Log.d("TAG","onDown: ");
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            Log.d("TAG","onDown: event.getRawX(): "+event.getRawX()+" event.getRawY()"+event.getRawY());
            CardView.LayoutParams lParams = (CardView.LayoutParams) myWebView.getLayoutParams();
            _yDelta = Y - lParams.topMargin;

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
            if((Y-_yDelta)<unCollapsedMargin){
//                updateUpPosition(unCollapsedMargin,0);
            }else if((Y-_yDelta)>collapsedMargin){
//                updateUpPosition(collapsedMargin,0);
            }else{
//                updateUpPosition((Y - _yDelta),0);
            }
            Log.d("TAG","the e2.getAction()= "+e2.getAction()+" and the MotionEvent.ACTION_CANCEL= " +MotionEvent.ACTION_CANCEL);
            RawList.add(Y);
            if(RawList.size()==5)calculateGeneralDirectionThenUpdateTopView();

            mIsScrolling = true;
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            int UpScore = 0;
            int DownScore = 0;
            if(RawList.size()>15){
                for(int i= RawList.size()-1 ;i>15;i--){
                    int num1 = RawList.get(i);
                    int numBefore1 = RawList.get(i - 1);

                    if(numBefore1>num1) DownScore++;
                    else UpScore++;
                }
            }else{
                for(int i= RawList.size()-1 ;i>0;i--){
                    int num1 = RawList.get(i);
                    int numBefore1 = RawList.get(i - 1);

                    if(numBefore1>num1) DownScore++;
                    else UpScore++;
                }
            }
            if(UpScore>DownScore){
                if(isMinified)unMinifyTheTopPart();
            }else{
                if(!isMinified)minifyTheTopPart();
            }
            if(RawList.size()>0) {
                int scrollAmm = Math.abs(RawList.get(RawList.size() - 1) - RawList.get(0));
                Log.d("MainAct", "scrollAmm: " + scrollAmm);
                Log.d("mainacc","velocityY: "+ velocityY);
                if (scrollAmm > SCROLL_AMOUNT_THRESHOLD && !isProgressShowing && Math.abs(velocityY)<2600){
                    if(DownScore>UpScore){
                        if(scrollConfirmBoolean){
                            if(didScrollChangeListenerSetScrollConfirmBoolean){
                                updateScrollProgress();
                            }else{
                                scrollConfirmBoolean = false;
                            }
                        }else{
                            scrollConfirmBoolean = true;
                            didScrollChangeListenerSetScrollConfirmBoolean = false;
                        }
                    }
                    else if(UpScore>DownScore){
                        if(Variables.hasReachedBottomOfPage){
                            if(scrollConfirmBoolean){
                                if(didScrollChangeListenerSetScrollConfirmBoolean){
                                    updateScrollProgress();
                                }else{
                                    scrollConfirmBoolean = false;
                                }
                            }else{
                                scrollConfirmBoolean = true;
                                didScrollChangeListenerSetScrollConfirmBoolean = false;
                            }
                        }else{
                            TellUserToScrollDown();
                        }
                    }
                }
            }
            RawList.clear();
            return false;

        }
    }

    private void TellUserToScrollDown(){
//        Toast.makeText(mContext,"Please scroll down",Toast.LENGTH_SHORT).show();
    }

    private void calculateGeneralDirectionThenUpdateTopView(){
        int UpScore = 0;
        int DownScore = 0;
        if(RawList.size()>15){
            for(int i= RawList.size()-1 ; i>=RawList.size()-15 ; i--){
                int num1 = RawList.get(i);
                int numBefore1 = RawList.get(i - 1);

                if(numBefore1>num1) DownScore++;
                else UpScore++;
            }
            if(UpScore>DownScore){
                if(isMinified)unMinifyTheTopPart();
            }else{
                if(!isMinified)minifyTheTopPart();
            }
        }
    }

    private void collapseTopPartBy(int distance){
        int minimumHeightLevel = Utils.dpToPx(29);
        if(!isMinified){
            isMinified = true;
            minifiedTextView.setVisibility(View.VISIBLE);
        }

        final CardView.LayoutParams params = (CardView.LayoutParams) progressBar.getLayoutParams();
        int currentPos = params.topMargin;

        if(currentPos<minimumHeightLevel) {
//            params.topMargin = (currentPos - distance);
//            progressBar.setLayoutParams(params);

//            final CardView.LayoutParams params2 = (CardView.LayoutParams) minifiedTextView.getLayoutParams();
//            int currentHeight = params2.height;
//            params2.height = currentHeight-distance;
//            minifiedTextView.setLayoutParams(params2);

            ValueAnimator animatorTop;
            animatorTop = ValueAnimator.ofInt(currentPos-distance);
            animatorTop.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    params.topMargin = (Integer) valueAnimator.getAnimatedValue();
                    progressBar.requestLayout();
                }
            });
            animatorTop.setDuration(1).start();

            float perc = (currentPos-Utils.dpToPx(29))/Utils.dpToPx(29);

            final CardView.LayoutParams params3 = (CardView.LayoutParams) rightButtonLayout.getLayoutParams();
            int currentHeight3 = params3.height;
//            params3.height = currentHeight3-distance;
//            rightButtonLayout.setLayoutParams(params3);

            ValueAnimator animatorTop2;
            animatorTop2 = ValueAnimator.ofInt(currentHeight3-distance);
            animatorTop2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    params3.height = (Integer) valueAnimator.getAnimatedValue();
                    rightButtonLayout.requestLayout();
                }
            });
            animatorTop2.setDuration(1).start();

            final CardView.LayoutParams params4 = (CardView.LayoutParams) backBtnLayout.getLayoutParams();
            int currentHeight4 = params4.height;
//            params4.height = currentHeight4-distance;
//            backBtnLayout.setLayoutParams(params4);

            ValueAnimator animatorTop4;
            animatorTop4 = ValueAnimator.ofInt(currentHeight4-distance);
            animatorTop4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    params4.height = (Integer) valueAnimator.getAnimatedValue();
                    backBtnLayout.requestLayout();
                }
            });
            animatorTop4.setDuration(1).start();

            rightButtonLayout.setAlpha(perc);
            backBtnLayout.setAlpha(perc);
            minifiedTextView.setAlpha(1-perc);

        }else{
            Log.d("MainActivity","Current position lower than minimumHeight level currentPos: "+currentPos+" minimumHeightLevel"+minimumHeightLevel);
            rightButtonLayout.setVisibility(View.INVISIBLE);
            backBtnLayout.setVisibility(View.INVISIBLE);
            minifiedTextView.setAlpha(1f);
        }
    }

    private void expandTopPartBy(int distance){
        int maximumHeightLevel = Utils.dpToPx(58);
        if(isMinified){
            isMinified = false;
            rightButtonLayout.setVisibility(View.VISIBLE);
            backBtnLayout.setVisibility(View.VISIBLE);

        }

        final CardView.LayoutParams params = (CardView.LayoutParams) progressBar.getLayoutParams();
        int currentPos = params.topMargin;

        if(currentPos<maximumHeightLevel) {
            params.topMargin = (currentPos + distance);
            progressBar.setLayoutParams(params);

            final CardView.LayoutParams params2 = (CardView.LayoutParams) minifiedTextView.getLayoutParams();
            int currentHeight = params2.height;
            params2.height = (currentHeight + distance);
            minifiedTextView.setLayoutParams(params2);

            float perc = (currentPos-Utils.dpToPx(29))/Utils.dpToPx(29);

            final CardView.LayoutParams params3 = (CardView.LayoutParams) rightButtonLayout.getLayoutParams();
            int currentHeight3 = params3.height;
            params3.height = currentHeight3+distance;
            rightButtonLayout.setLayoutParams(params3);

            final CardView.LayoutParams params4 = (CardView.LayoutParams) backBtnLayout.getLayoutParams();
            int currentHeight4 = params2.height;
            params4.height = currentHeight4+distance;
            backBtnLayout.setLayoutParams(params4);

            rightButtonLayout.setAlpha(perc);
            backBtnLayout.setAlpha(perc);
            minifiedTextView.setAlpha(1-perc);
        }else{
            rightButtonLayout.setAlpha(1f);
            backBtnLayout.setAlpha(1f);
            minifiedTextView.setAlpha(0f);
        }
    }





    private void addTouchListenerForSwipeBack(){
        mSwipeBackDetector = new GestureDetector(this, new Bookmarks.MySwipebackGestureListener());
        swipeBackView.setOnTouchListener(touchListener);
    }

    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (mSwipeBackDetector.onTouchEvent(motionEvent)) {
                return true;
            }
            if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if(isSideScrolling) {
                    Log.d("touchListener"," onTouch ACTION_UP");
                    isSideScrolling  = false;

                    int RightScore = 0;
                    int LeftScore = 0;
                    if(SideSwipeRawList.size()>15){
                        for(int i= SideSwipeRawList.size()-1 ;i>SideSwipeRawList.size()-15;i--){
                            int num1 = SideSwipeRawList.get(i);
                            int numBefore1 = SideSwipeRawList.get(i - 1);

                            if(numBefore1>num1) LeftScore++;
                            else RightScore++;
                        }
                    }else{
                        for(int i= SideSwipeRawList.size()-1 ;i>0;i--){
                            int num1 = SideSwipeRawList.get(i);
                            int numBefore1 = SideSwipeRawList.get(i - 1);

                            if(numBefore1>num1) LeftScore++;
                            else RightScore++;
                        }
                    }
                    if(RightScore>LeftScore){
                        backButton.performClick();
                        isLoadingPrevious = true;

                    }
                    hideNupdateSideSwipeThing();
                    SideSwipeRawList.clear();
                };
            }

            return false;
        }
    };




    class MySwipebackGestureListener extends GestureDetector.SimpleOnGestureListener {
        int origX = 0;
        int origY = 0;


        @Override
        public boolean onDown(MotionEvent event) {
            Log.d("TAG","onDown: ");
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            Log.d("TAG","onDown: event.getRawX(): "+event.getRawX()+" event.getRawY()"+event.getRawY());
            CardView.LayoutParams lParams = (CardView.LayoutParams) swipeBackView.getLayoutParams();
            x_delta = X - lParams.leftMargin;

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
            if((X-x_delta)<maxSideSwipeLength){

            }else if((X-x_delta)>maxSideSwipeLength){

            }else{
            }
            showNupdateSideSwipeThing(X-x_delta);

            Log.d("TAG","the e2.getAction()= "+e2.getAction()+" and the MotionEvent.ACTION_CANCEL= " +MotionEvent.ACTION_CANCEL);
            SideSwipeRawList.add(X);

            isSideScrolling = true;
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            int RightScore = 0;
            int LeftScore = 0;
            if(SideSwipeRawList.size()>15){
                for(int i= SideSwipeRawList.size()-1 ;i>SideSwipeRawList.size()-15;i--){
                    int num1 = SideSwipeRawList.get(i);
                    int numBefore1 = SideSwipeRawList.get(i - 1);

                    if(numBefore1>num1) LeftScore++;
                    else RightScore++;
                }
            }else{
                for(int i= SideSwipeRawList.size()-1 ;i>0;i--){
                    int num1 = SideSwipeRawList.get(i);
                    int numBefore1 = SideSwipeRawList.get(i - 1);

                    if(numBefore1>num1) LeftScore++;
                    else RightScore++;
                }
            }
            if(RightScore>LeftScore){

                backButton.performClick();
                isLoadingPrevious = true;

            }
            hideNupdateSideSwipeThing();
            SideSwipeRawList.clear();
            return false;

        }
    }

    private void showNupdateSideSwipeThing(int pos){
        int trans = (int)((pos-Utils.dpToPx(10))*0.9);
        RelativeLayout isGoingBackIndicator = findViewById(R.id.isGoingBackIndicator);

        View v = findViewById(R.id.swipeBackViewIndicator);
        CardView.LayoutParams params = (CardView.LayoutParams) v.getLayoutParams();
        params.height = trans;
        v.setLayoutParams(params);
    }

    private void hideNupdateSideSwipeThing(){
        RelativeLayout isGoingBackIndicator = findViewById(R.id.isGoingBackIndicator);

        final View v = findViewById(R.id.swipeBackViewIndicator);
        final CardView.LayoutParams params = (CardView.LayoutParams) v.getLayoutParams();

        if(isLoadingPrevious){
            isLoadingPrevious = false;
            ValueAnimator animatorTop;
            animatorTop = ValueAnimator.ofInt(params.height,params.height+Utils.dpToPx(5000));
            animatorTop.setInterpolator(new LinearOutSlowInInterpolator());
            animatorTop.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    params.height = (Integer) valueAnimator.getAnimatedValue();
                    v.requestLayout();
                }
            });
            animatorTop.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    v.setVisibility(View.GONE);
                    params.height = 0;
                    v.setLayoutParams(params);
                    v.setAlpha(1f);
                    v.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            animatorTop.setDuration(normalDuration).start();
            v.animate().alpha(0f).setDuration(normalDuration).setInterpolator(new LinearInterpolator()).start();
        }else{
            ValueAnimator animatorTop;
            animatorTop = ValueAnimator.ofInt(params.height,0);
            animatorTop.setInterpolator(new LinearOutSlowInInterpolator());
            animatorTop.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    params.height = (Integer) valueAnimator.getAnimatedValue();
                    v.requestLayout();
                }
            });
            animatorTop.setDuration(normalDuration).start();
        }


    }



    //here
    private void setUpScrollProgress(){
        scrollProgressLayout.setVisibility(View.VISIBLE);
        scrollProgress.setVisibility(View.VISIBLE);
        if(canScrollForIncentive()){
            scrollProgress.setProgress(0);
            currentScrollProgress = 0;

            final TextView textViewIncentiveAmount = findViewById(R.id.textViewIncentiveAmount);
            textViewIncentiveAmount.setAlpha(1f);
            textViewIncentiveAmount.setTranslationX(0);
            textViewIncentiveAmount.setVisibility(View.VISIBLE);

            final RelativeLayout doneImageLayout = findViewById(R.id.doneImageLayout);
            doneImageLayout.setAlpha(0f);
            doneImageLayout.setTranslationX(Utils.dpToPx(30));
        }else{
            currentScrollProgress = 100;
            scrollProgress.setProgress(100);

            final TextView textViewIncentiveAmount = findViewById(R.id.textViewIncentiveAmount);
            textViewIncentiveAmount.setAlpha(0f);
            textViewIncentiveAmount.setTranslationX(-Utils.dpToPx(30));
            textViewIncentiveAmount.setVisibility(View.GONE);

            final RelativeLayout doneImageLayout = findViewById(R.id.doneImageLayout);
            doneImageLayout.setAlpha(1f);
            doneImageLayout.setTranslationX(0);
            doneImageLayout.setVisibility(View.VISIBLE);
        }
    }

    //jk its actually here
    private void updateScrollProgress(){
        scrollConfirmBoolean = false;
        if(currentScrollProgress<100 && canScrollForIncentive() && !isUpdating) {
            isUpdating = true;
            currentScrollProgress += 20;
            ObjectAnimator progressAnimator = ObjectAnimator.ofInt(scrollProgress, "progress", currentScrollProgress);
            progressAnimator.setDuration(animationTime);
            progressAnimator.setInterpolator(new LinearInterpolator());
            progressAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    isUpdating = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            progressAnimator.start();
        }

    }

    private boolean canScrollForIncentive(){
        return false;
    }




    //Adds incentive to users money total

    private void removeMessage(){
        final RelativeLayout incentiveAddedMessage = findViewById(R.id.incentiveAddedMessage);
        incentiveAddedMessage.animate().alpha(0f).translationY(Utils.dpToPx(50)).setDuration(animationTime).setStartDelay(1900)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                incentiveAddedMessage.setVisibility(View.GONE);
                incentiveAddedMessage.setAlpha(0f);
                incentiveAddedMessage.setTranslationY(Utils.dpToPx(50));
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();
    }




    //This is where everything starts
    private void openBottomPart(){
        isBottomPartOpen = true;
        disableAllViews();
        RelativeLayout blackBackgroundLayout = findViewById(R.id.blackBackgroundLayout);
        blackBackgroundLayout.setVisibility(View.VISIBLE);
        final View blackView = findViewById(R.id.blackBackgroundView);
        blackView.setAlpha(0f);
        blackView.animate().setDuration(mAnimationTime).alpha(0.7f).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        blackView.setAlpha(0.7f);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();

        ContactSelectorContainer.setVisibility(View.VISIBLE);
        expandContactSelector();

        setContactSelectorClickListeners();
        setRelevantData();
        moveSwipeViewUpwards();
    }

    private void setContactSelectorClickListeners(){
        final Advert ad = Variables.adToBeViewed;
        final View blackView = findViewById(R.id.blackBackgroundView);

        websiteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!ad.getWebsiteLink().equals("none")) {
                    if (!isConfirmDialLayout){
//                        updatePosition();
//                        ContactSelectorContainer.animate().translationY(Utils.dpToPx(160)).setDuration(mAnimationTime)
//                                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
//                            @Override
//                            public void onAnimationStart(Animator animator) {
//
//                            }
//
//                            @Override
//                            public void onAnimationEnd(Animator animator) {
//                                ContactSelectorContainer.setTranslationY(Utils.dpToPx(160));
//                            }
//
//                            @Override
//                            public void onAnimationCancel(Animator animator) {
//
//                            }
//
//                            @Override
//                            public void onAnimationRepeat(Animator animator) {
//
//                            }
//                        }).start();
                        openConfirmWebsiteLayout();
                    }
                }else{
                    Toast.makeText(mContext, "They didn't add that.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        CallLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!ad.getAdvertiserPhoneNo().equals("none")) {
                    if (!isConfirmOpenWebsiteLayout) {
//                        openDialerAndCall();
//                        ContactSelectorContainer.animate().translationY(Utils.dpToPx(160)).setDuration(mAnimationTime)
//                                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
//                            @Override
//                            public void onAnimationStart(Animator animator) {
//
//                            }
//
//                            @Override
//                            public void onAnimationEnd(Animator animator) {
//                                ContactSelectorContainer.setTranslationY(Utils.dpToPx(160));
//                            }
//
//                            @Override
//                            public void onAnimationCancel(Animator animator) {
//
//                            }
//
//                            @Override
//                            public void onAnimationRepeat(Animator animator) {
//
//                            }
//                        }).start();
                        openConfirmDialLayout();
                    } else {
                        Log.e(TAG, "Cannot open ConfirmCall Layout, isConfirmOpenWebsiteLayout is true!!");
                    }
                }else{
                    Toast.makeText(mContext, "They didn't add that.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        LocationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!ad.getAdvertiserLocations().isEmpty()){
                    openLocation();
                }else{
                    Toast.makeText(mContext, "They didn't add that.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.phoneIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CallLayout.performClick();
            }
        });

        findViewById(R.id.websiteIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                websiteLayout.performClick();
            }
        });

        findViewById(R.id.locationIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationLayout.performClick();
            }
        });


        blackView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(isCardMinimized) closeBottomPart();
            }
        });
    }

    private void setRelevantData() {
        Advert ad = Variables.adToBeViewed;

        TextView callText = findViewById(R.id.callTextView);
        TextView phoneNoText = findViewById(R.id.phoneNoText);
        TextView websiteNameText = findViewById(R.id.websiteNameText);
        TextView websiteIncentiveText = findViewById(R.id.websiteIncentiveText);
        TextView incentiveTextView = findViewById(R.id.incentiveTextView);

        if(!ad.getAdvertiserPhoneNo().equals("none")){
            callText.setText(ad.getAdvertiserPhoneNo());
            phoneNoText.setText(ad.getAdvertiserPhoneNo());
        }
        if(!ad.getWebsiteLink().equals("none")){
            String url = Variables.adToBeViewed.getWebsiteLink();
            if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://" + url;
            websiteNameText.setText(url);
            PAGE = url;
        }

        if(ad.getAdvertiserPhoneNo().equals("none")) CallLayout.setAlpha(0.6f);
        if(ad.getWebsiteLink().equals("none")) websiteLayout.setAlpha(0.6f);
        if(ad.getAdvertiserLocations().isEmpty()) LocationLayout.setAlpha(0.6f);

        if(ad.didAdvertiserAddIncentive()){
            if(canScrollForIncentive()){
                websiteIncentiveText.setText("");
                incentiveTextView.setText("Online.");
            }else{
                websiteIncentiveText.setText("");
                incentiveTextView.setText("Online.");
            }

        }else{
            websiteIncentiveText.setText("");
            incentiveTextView.setText("Online.");
        }

    }

    private void collapseContactSelector(){
        ContactSelectorContainer.animate().translationY(Utils.dpToPx(-155)).setDuration(mAnimationTime)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                ContactSelectorContainer.setTranslationY(Utils.dpToPx(-155));
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();
    }

    private void expandContactSelector(){
        ContactSelectorContainer.animate().translationY(Utils.dpToPx(0)).setDuration(mAnimationTime)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                ContactSelectorContainer.setVisibility(View.VISIBLE);
                ContactSelectorContainer.setTranslationY(0);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();
        setContactSelectorClickListeners();
    }




    private void openConfirmWebsiteLayout(){
        isConfirmOpenWebsiteLayout = true;
        final RelativeLayout ConfirmVisitWebsiteContainer = findViewById(R.id.ConfirmVisitWebsiteContainer);
        ConfirmVisitWebsiteContainer.setVisibility(View.VISIBLE);


        ContactSelectorContainer.animate().translationY(Utils.dpToPx(-160)).setDuration(mAnimationTime)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                ContactSelectorContainer.setTranslationY(Utils.dpToPx(-160));
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();

        ConfirmVisitWebsiteContainer.animate().translationY(Utils.dpToPx(0)).setDuration(mAnimationTime)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                ConfirmVisitWebsiteContainer.setVisibility(View.VISIBLE);
                ConfirmVisitWebsiteContainer.setTranslationY(0);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();

        TextView websiteIncentiveText = findViewById(R.id.websiteIncentiveText);
        Advert ad = Variables.adToBeViewed;
        if(ad.didAdvertiserAddIncentive()) {
            if (canScrollForIncentive()) {
                websiteIncentiveText.setText("");
            } else {
                websiteIncentiveText.setText("Click visit to open their website.");
            }
        }else{
            websiteIncentiveText.setText("Click visit to open their website.");
        }

        TextView websiteNameText = findViewById(R.id.websiteNameText);
        websiteNameText.setText(PAGE);

        CardView okVisitBtn = findViewById(R.id.okVisitBtn);
        okVisitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePosition();
                ConfirmVisitWebsiteContainer.animate().translationY(Utils.dpToPx(200)).setDuration(mAnimationTime)
                        .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        ConfirmVisitWebsiteContainer.setVisibility(View.VISIBLE);
                        ConfirmVisitWebsiteContainer.setTranslationY(Utils.dpToPx(200));
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();
                isConfirmOpenWebsiteLayout = false;

                ContactSelectorContainer.animate().translationY(Utils.dpToPx(160)).setDuration(mAnimationTime)
                        .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        ContactSelectorContainer.setTranslationY(Utils.dpToPx(160));
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();
            }
        });

    }

    private void closeConfirmWebsiteLayout(){
        isConfirmOpenWebsiteLayout = false;
        final RelativeLayout ConfirmVisitWebsiteContainer = findViewById(R.id.ConfirmVisitWebsiteContainer);

        ConfirmVisitWebsiteContainer.animate().translationY(Utils.dpToPx(200)).setDuration(mAnimationTime)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                ConfirmVisitWebsiteContainer.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();

        expandContactSelector();
    }





    private void openConfirmDialLayout(){
        isConfirmDialLayout = true;
        final RelativeLayout ConfirmCallContainer = findViewById(R.id.ConfirmCallContainer);
        ConfirmCallContainer.setVisibility(View.VISIBLE);
        collapseContactSelector();

        TextView phoneNoText = findViewById(R.id.phoneNoText);
        phoneNoText.setText(Variables.adToBeViewed.getAdvertiserPhoneNo());

        ConfirmCallContainer.animate().translationY(Utils.dpToPx(0)).setDuration(mAnimationTime)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                ConfirmCallContainer.setTranslationY(0);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();

        CardView okDialBtn = findViewById(R.id.okDialBtn);
        okDialBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialerAndCall();
            }
        });
    }

    private void closeConfirmDialLayout(){
        isConfirmDialLayout = false;
        final RelativeLayout ConfirmCallContainer = findViewById(R.id.ConfirmCallContainer);
        ConfirmCallContainer.animate().translationY(Utils.dpToPx(200)).setDuration(mAnimationTime)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                ConfirmCallContainer.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();

        expandContactSelector();
    }

    private void openDialerAndCall() {
        Intent intent = new Intent(Intent.ACTION_CALL);
        Advert ad = Variables.adToBeViewed;
        String number = ad.getAdvertiserPhoneNo();
        intent.setData(Uri.parse("tel:" + number.trim()));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CALL_PHONE}, DIALER_REQUEST_CODE);
            return;
        }
        startActivity(intent);
    }




    private void openLocation(){
        Advert ad = Variables.adToBeViewed;
        if(ad.getAdvertiserLocations().isEmpty()){
            Toast.makeText(mContext,"They didn't add that.",Toast.LENGTH_SHORT).show();
        }else{
            Vibrator b = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
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

    private AdvertiserLocation getClosestLocation(){
        List<myLatLng> advertiserSetLong = new ArrayList<>();
        for(AdvertiserLocation loc:Variables.adToBeViewed.getAdvertiserLocations()){
            advertiserSetLong.add(loc.getMyLatLng());
        }
        if(Variables.usersLatLongs.isEmpty()){
            return Variables.adToBeViewed.getAdvertiserLocations().get(0);
        }else {
            myLatLng closestLatLng = Variables.adToBeViewed.getAdvertiserLocations().get(0).getMyLatLng();
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
            for(AdvertiserLocation adLoc:Variables.adToBeViewed.getAdvertiserLocations()){
                if(adLoc.getMyLatLng().equals(closestLatLng)){
                    return adLoc;
                }
            }return Variables.adToBeViewed.getAdvertiserLocations().get(0);
        }
    }

    private void closeBottomPart() {
        if(isConfirmOpenWebsiteLayout){
            isConfirmOpenWebsiteLayout = false;
            final RelativeLayout ConfirmVisitWebsiteContainer = findViewById(R.id.ConfirmVisitWebsiteContainer);
            ConfirmVisitWebsiteContainer.setVisibility(View.GONE);
            ConfirmVisitWebsiteContainer.setTranslationY(Utils.dpToPx(200));
        }if(isConfirmDialLayout){
            isConfirmDialLayout = false;
            final RelativeLayout ConfirmCallContainer = findViewById(R.id.ConfirmCallContainer);
            ConfirmCallContainer.setVisibility(View.GONE);
            ConfirmCallContainer.setTranslationY(Utils.dpToPx(200));
        }
        final RelativeLayout blackBackgroundLayout = findViewById(R.id.blackBackgroundLayout);
        blackBackgroundLayout.animate().alpha(0f).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        blackBackgroundLayout.setVisibility(View.GONE);
                        blackBackgroundLayout.setAlpha(1f);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                }).start();

        ContactSelectorContainer.animate().translationY(Utils.dpToPx(200)).setDuration(mAnimationTime)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                ContactSelectorContainer.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        }).start();
        isBottomPartOpen = false;
        enableAllViews();
        unMoveSwipeViewUpwards();
    }




    private void moveSwipeViewUpwards(){
        final int pos = Utils.dpToPx(-50);


    }

    private void unMoveSwipeViewUpwards(){
        final int pos = 0;

    }


    public void disableAllViews(){
        isEnabled = false;
        vpPager.setPagingEnabled(false);
    }

    public void enableAllViews(){
        isEnabled = true;
        vpPager.setPagingEnabled(true);
    }


    private void addTouchListenerForVPagerNavs(){
        mNavsSwipeBackDetector = new GestureDetector(this, new Bookmarks.ViewPagerSwipeGestureListener());
        vpPager.setOnTouchEvent(touchListenerForVPagerNavs);
    }

    View.OnTouchListener touchListenerForVPagerNavs = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (mNavsSwipeBackDetector.onTouchEvent(motionEvent)) {
                return true;
            }
            if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                if(isUpDownSwiping) {
                    isUpDownSwiping  = false;
                    int UpScore = 0;
                    int DownScore = 0;
                    for(int i= vpagerYrawList.size()-1 ;i>0;i--){
                        int num1 = vpagerYrawList.get(i);
                        int numBefore1 = vpagerYrawList.get(i - 1);

                        if(numBefore1>num1) DownScore++;
                        else UpScore++;
                    }
//                    if(Math.abs(UpScore-DownScore)>10) {
//                        if (UpScore > DownScore) {
//                            findViewById(R.id.shareBtn).performClick();
//                        } else {
//                            findViewById(R.id.backBtn).performClick();
//                        }
//                    }
//                    hideNupdatePositionOfLinesUnderViewPagerNavButtons();
                    hideNupdatePositionOfLinesUnderViewPagerNavButtons();
                    vpagerYrawList.clear();
                }
            }

            return false;
        }
    };



    class ViewPagerSwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        int origX = 0;
        int origY = 0;


        @Override
        public boolean onDown(MotionEvent event) {
            Log.d("TAG","onDown: ");
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            Log.d("TAG","onDown: event.getRawX(): "+event.getRawX()+" event.getRawY()"+event.getRawY());
            CoordinatorLayout.LayoutParams lParams = (CoordinatorLayout.LayoutParams) vpPager.getLayoutParams();
            Ydelta = Y - lParams.topMargin;

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

            updatePositionOfLinesUnderViewPagerNavButtons(Y-Ydelta);

            Log.d("TAG","the e2.getAction()= "+e2.getAction()+" and the MotionEvent.ACTION_CANCEL= " +MotionEvent.ACTION_CANCEL);
            vpagerYrawList.add(X);

            isUpDownSwiping = true;
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            int UpScore = 0;
            int DownScore = 0;

            for(int i= vpagerYrawList.size()-1 ;i>0;i--){
                int num1 = vpagerYrawList.get(i);
                int numBefore1 = vpagerYrawList.get(i - 1);

                if(numBefore1>num1) DownScore++;
                else UpScore++;
            }

            if(Math.abs(velocityX)<Math.abs(velocityY)) {
                if (velocityY > 0) {
                    if (Math.abs(velocityY) > 1500) findViewById(R.id.backBtn).performClick();
                } else {
                    if (Math.abs(velocityY) > 1500) findViewById(R.id.shareBtn).performClick();
                }
            }
                hideNupdatePositionOfLinesUnderViewPagerNavButtons();
            vpagerYrawList.clear();
            return false;

        }
    }

    private void updatePositionOfLinesUnderViewPagerNavButtons(int newPos){
        int pos = (int)(0.2*newPos);
        vpPager.setTranslationY(pos);

        if(newPos>0){
            View v = findViewById(R.id.backIndicator);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
            params.width = Math.abs(pos);
            if(Math.abs(pos)<Utils.dpToPx(42)) v.setLayoutParams(params);
        }else if(newPos==0){
            View v = findViewById(R.id.backIndicator);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
            params.width = 0;
            v.setLayoutParams(params);

            View v2 = findViewById(R.id.shareIndicator);
            RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) v2.getLayoutParams();
            params2.width = 0;
            v2.setLayoutParams(params2);
        }
        else{
            View v = findViewById(R.id.shareIndicator);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
            params.width = Math.abs(pos);
            if(Math.abs(pos)<Utils.dpToPx(24)) v.setLayoutParams(params);
        }
    }

    private void hideNupdatePositionOfLinesUnderViewPagerNavButtons(){
        int trans = (int)vpPager.getTranslationY();
        vpPager.animate().setInterpolator(new LinearOutSlowInInterpolator()).setDuration(mAnimationTime).translationY(0)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        vpPager.setTranslationY(0);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        if(trans>0) {
            final View v = findViewById(R.id.backIndicator);
            final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
            ValueAnimator animatorTop;
            animatorTop = ValueAnimator.ofInt(params.width, 0);
            animatorTop.setInterpolator(new LinearOutSlowInInterpolator());
            animatorTop.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    params.width = (Integer) valueAnimator.getAnimatedValue();
                    v.requestLayout();
                }
            });
            animatorTop.setDuration(normalDuration).start();
        }else {
            final View v2 = findViewById(R.id.shareIndicator);
            final RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) v2.getLayoutParams();
            ValueAnimator animatorTop2;
            animatorTop2 = ValueAnimator.ofInt(Utils.dpToPx(24), 0);
            animatorTop2.setInterpolator(new LinearOutSlowInInterpolator());
            animatorTop2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    params2.width = (Integer) valueAnimator.getAnimatedValue();
                    v2.requestLayout();
                }
            });
            animatorTop2.setDuration(normalDuration).start();
        }

    }




    private void setTouchListenerForExpandImage(){
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.d(TAG,"window focus change detected");
        isSetUpSharing = false;
        findViewById(R.id.isSharingProgress).setVisibility(View.INVISIBLE);
    }


}
