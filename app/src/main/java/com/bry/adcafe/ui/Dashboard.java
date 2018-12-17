package com.bry.adcafe.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.adapters.FirstMessageItem;
import com.bry.adcafe.adapters.MessageItem;
import com.bry.adcafe.fragments.ChangeCPVFragment;
import com.bry.adcafe.fragments.FragmentUserPayoutBottomSheet;
import com.bry.adcafe.fragments.SetUsersPersonalInfo;
import com.bry.adcafe.fragments.myMapFragment;
import com.bry.adcafe.models.AdCoin;
import com.bry.adcafe.classes.LockableScrollView;
import com.bry.adcafe.models.Message;
import com.bry.adcafe.models.PayoutResponse;
import com.bry.adcafe.models.User;
import com.bry.adcafe.services.DatabaseManager;
import com.bry.adcafe.services.Payments;
import com.bry.adcafe.services.SliderPrefManager;
import com.bry.adcafe.services.TimeManager;
import com.bry.adcafe.services.Utils;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mindorks.placeholderview.PlaceHolderView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Dashboard extends AppCompatActivity {
    private final String TAG = "Dashboard";
    private TextView mTotalAdsSeenToday;
    private TextView mTotalAdsSeenAllTime;
    private ImageView mInfoImageView;
    private CardView mUploadAnAdIcon;
    private TextView mAmmountNumber;
    protected String mKey = "";
    private SliderPrefManager myPrefManager;
    private Button mUploadedAdsStats;
    private Context mContext;
    private static int NOTIFICATION_ID2 = 1880;

    @Bind(R.id.NotificationBtn) public ImageButton mNotfBtn;
    @Bind(R.id.dotForNotification) public View mDotForNotf;
    @Bind(R.id.ChangeCPVBtn) public ImageButton mCPVBtn;
    @Bind(R.id.targetedBtn) public ImageButton targetedBtn;
    @Bind(R.id.payoutBtn) public ImageButton payoutBtn;
    @Bind(R.id.findOutCard) public CardView findOutCard;

    public Context miniContext;
    private ProgressDialog mProgForPayments;
    private String joke;

    private Activity thisActivity;
    private DatabaseReference dbListener;
    private DatabaseReference dbListenerForMoney;
    private boolean isMakingPayout = false;

    private boolean isWindowPaused = false;
    private DatabaseReference SKListener;
    private boolean isNeedToLoadLogin = false;

    private final int REQUESTCODE = 3301;
    private static final int PICK_IMAGE_REQUEST = 234;
    private final int BOI = 35;
    private final int BOI2 = 37;


    @Bind(R.id.feedChatView) RelativeLayout mFeedChatView;
    @Bind(R.id.collapseFeedChatButton) ImageButton mCollapseFeedChatButton;
    private final int collapsedMargin = Utils.dpToPx(700);
    private final int unCollapsedMargin = Utils.dpToPx(1);
    private boolean isCardCollapsed = true;
    private boolean mIsScrolling = false;
    private int _yDelta;
    private boolean isInTransition = false;
    private final int normalDuration = 320;
    private List<Integer> RawList = new ArrayList<>();

    @Bind(R.id.ScrollView) LockableScrollView mScrollView;
    @Bind(R.id.feedChatEditText) EditText mFeedChatEditText;
    @Bind(R.id.addImageBtn) ImageButton mAddImageBtn;
    @Bind(R.id.sendBtn) ImageButton mSendBtn;
    @Bind(R.id.chatsPlaceHolderView) PlaceHolderView mChatsPlaceHolderView;

    private int numberOfItemsAdded = 0;
    private Uri mFilepath;
    private String mPath;
    private Bitmap imageBitmap;

    private int numberOfNewMessages = 0;

    @Bind(R.id.swipeBackView)View swipeBackView;
    private boolean isSwipingForBack = false;
    private GestureDetector mSwipeBackDetector;
    private int maxSideSwipeLength = 200;
    private int x_delta;
    private List<Integer> SideSwipeRawList = new ArrayList<>();
    private boolean isSideScrolling = false;

    @Bind(R.id.linLyt) LinearLayout linLyt;
    @Bind(R.id.settingsIcon) ImageButton settingsIcon;
    @Bind(R.id.settingsContainer) RelativeLayout settingsContainer;
    private int mAnimationTime = 300;
    @Bind(R.id.TweaksBlackBack) RelativeLayout TweaksBlackBack;
    @Bind(R.id.TweaksLayout) RelativeLayout TweaksLayout;
    private boolean isSettingsCardExpanded = false;
    private GestureDetector MyTouchBackGestureListener;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Variables.isDashboardActivityOnline = true;
        mContext = this.getApplicationContext();
        miniContext = mContext;
        thisActivity = this;
        ButterKnife.bind(this);

        loadViews();
        setValues();
        setClickListeners();
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        try{
            notificationManager.cancel(NOTIFICATION_ID2);
        }catch (Exception e){
            e.printStackTrace();
        }
        setListeners();
        createProgressDialog();
        setGestureListener();
        addListenerForPaymentSession();
        addListenerForChangeInPayoutTotals();

        fastCollapseTheFeedChatView();
        NEG = -(getScreenWidth()-Utils.dpToPx(10));

        addTouchListenerForSwipeBack();
        SetOnTopScrollListener();
/*        TODO
            Fix the SetOnTopScrollListener skipping bug
*/
    }

    @Override
    protected void onStart(){
        super.onStart();
        if(!Variables.isMainActivityOnline){
            loadUserDataFromSharedPrefs();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        new DatabaseManager().loadUsersPassword();
        if (!TimeManager.isTimerOnline())TimeManager.setUpTimeManager("RESET_TIMER",mContext);
        setValues();
        isWindowPaused = false;
        if(isNeedToLoadLogin){
            Intent intent = new Intent(Dashboard.this, LoginActivity.class);
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

    private void setListeners(){
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForShowingPrompt,
                new IntentFilter("SHOW_PROMPT"));

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForStartPayout,
                new IntentFilter("START_PAYOUT"));

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForShowMap,
                new IntentFilter("SHOW_MAP"));

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForListeningForNewMessages,
                new IntentFilter(Constants.NEW_MESSAGE_NOTIFIER_INTENT));
    }

    private void removeListeners(){
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForShowingPrompt);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverForStartPayout);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverForSuccessfulPayout);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForFailedPayout);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForShowMap);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForListeningForNewMessages);
    }

    private BroadcastReceiver mMessageReceiverForShowingPrompt = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            promptUserAboutChanges();
        }
    };


    private void setClickListeners() {
        mInfoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isSettingsCardExpanded) {
                    Intent intent = new Intent(Dashboard.this, TutorialUsers.class);
                    startActivity(intent);
                    Variables.isStartFromLogin = false;
                    Variables.isInfo = true;
                    finish();
                }
            }
        });

        mUploadAnAdIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isSettingsCardExpanded) {
                    myPrefManager = new SliderPrefManager(getApplicationContext());
                    if (myPrefManager.isFirstTimeLaunchForAdvertisers()) {
                        Intent intent = new Intent(Dashboard.this, TutorialAdvertisers.class);
                        startActivity(intent);
                    } else {
                        if (TimeManager.isAlmostMidNight()) {
                            Toast.makeText(mContext, "Please wait for 1 minute.", Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intent = new Intent(Dashboard.this, SelectCategoryAdvertiser.class);
                            startActivity(intent);
                        }
                    }
                }
            }
        });

        findOutCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isSettingsCardExpanded) {
                    if (TimeManager.isAlmostMidNight()) {
                        Toast.makeText(mContext, "Please wait for 1 minute.", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(Dashboard.this, AdStats.class);
                        startActivity(intent);
                    }
                }
            }
        });

        findViewById(R.id.uploadedAdsStats).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });

        findViewById(R.id.FeedbackBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                FragmentManager fm = getFragmentManager();
//                Log("DASHBOARD","Setting up fragment");
//                FeedbackFragment reportDialogFragment = new FeedbackFragment();
//                reportDialogFragment.setMenuVisibility(false);
//                reportDialogFragment.show(fm, "Feedback.");
//                reportDialogFragment.setfragContext(mContext);
                if(!isSettingsCardExpanded) {
                    showFeedChatView();
                }
            }
        });

        findViewById(R.id.FeedbackBtn).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(isOnline(mContext)) showPromptForJoke();
                return false;
            }
        });

        findViewById(R.id.subscriptionsImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isSettingsCardExpanded) {
                    Intent intent = new Intent(Dashboard.this, SubscriptionManager.class);
                    startActivity(intent);
                }
            }
        });

        mNotfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptUserAboutNotifications();
            }
        });

        mCPVBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptUserAboutChangingPrice();
            }
        });

        findViewById(R.id.UserDataBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadUserDataDialog();
            }
        });

        targetedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptUserToStopTargeting();
            }
        });

        payoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isSettingsCardExpanded) {
                    if (Variables.getTotalReimbursementAmount() < 1) {
                        promptUserForUnableToPayout();
                    } else {
                        if (isOnline(mContext)) {
                            if (!TimeManager.isTimerOnline())
                                TimeManager.setUpTimeManager("RESET_TIMER", mContext);
                            if (!isMakingPayout) promptUserAboutPayout();
                        } else
                            Toast.makeText(mContext, "You need internet connection first.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

//        shareAppBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                promptUserToShareApp();
//            }
//        });
        settingsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isSettingsCardExpanded) {
                    expandSettings();
                }
            }
        });

    }

    @Override
    protected void onStop(){
        super.onStop();
//        finish();
    }

    @Override
    protected void onDestroy(){
        Variables.isDashboardActivityOnline = false;
        removeLisenerForPaymentSession();
        removeListenerForChangeInPayoutTotals();
        super.onDestroy();
        removeListeners();
    }

    private void loadViews() {
        mTotalAdsSeenAllTime = findViewById(R.id.AdsSeenAllTimeNumber);
        mTotalAdsSeenToday = findViewById(R.id.AdsSeenTodayNumber);
        mInfoImageView = findViewById(R.id.helpIcon);
        mUploadAnAdIcon = findViewById(R.id.uploadAnAdIcon);
        mAmmountNumber = findViewById(R.id.ammountNumber);
        mUploadedAdsStats = findViewById(R.id.uploadedAdsStats);
    }

    private void setValues() {
        NumberFormat format = NumberFormat.getInstance(Locale.US);

        int todaysTotals;
        int monthsTotals;
        int reimbursementTotals;
        if(Variables.getMonthAdTotals(mKey) ==0) {
            SharedPreferences prefs = getSharedPreferences("TodayTotals", MODE_PRIVATE);
            todaysTotals = prefs.getInt("TodaysTotals", 0);

            SharedPreferences prefs2 = getSharedPreferences("MonthTotals", MODE_PRIVATE);
            monthsTotals = prefs2.getInt("MonthsTotals", 0);

            SharedPreferences prefs3 = getSharedPreferences("ReimbursementTotals", MODE_PRIVATE);
            reimbursementTotals = prefs3.getInt(Constants.REIMBURSEMENT_TOTALS, 0);
        }else{
            todaysTotals = Variables.getAdTotal(mKey);
            monthsTotals = Variables.getMonthAdTotals(mKey);
            reimbursementTotals = Variables.getTotalReimbursementAmount();
        }
        mTotalAdsSeenToday.setText(format.format(todaysTotals));
        if(monthsTotals<10000){//10 K
            int size = 50;
            mTotalAdsSeenAllTime.setTextSize(size);
            mTotalAdsSeenToday.setTextSize(size);
        }else if(monthsTotals<1000000){//1 Mil
            int size = (40);
            mTotalAdsSeenAllTime.setTextSize(size);
            mTotalAdsSeenToday.setTextSize(size);
        }else if(monthsTotals<10000000){//10 Mil
            int size = (30);
            mTotalAdsSeenAllTime.setTextSize(size);
            mTotalAdsSeenToday.setTextSize(size);
        }else if(monthsTotals<100000000){//100 Mil
            int size = (25);
            mTotalAdsSeenAllTime.setTextSize(size);
            mTotalAdsSeenToday.setTextSize(size);
        }else if(monthsTotals<1000000000){//1 Bil
            int size = (20);
            mTotalAdsSeenAllTime.setTextSize(size);
            mTotalAdsSeenToday.setTextSize(size);
        }else{//above 1 Bil (if that's even possible... which it isn't... because Integers can't be that big... because magic...)
            int size = (15);
            mTotalAdsSeenAllTime.setTextSize(size);
            mTotalAdsSeenToday.setTextSize(size);
        }
        if(reimbursementTotals>1000000){
            int size = 20;
            mAmmountNumber.setTextSize(size);
        }
        mTotalAdsSeenAllTime.setText(format.format(monthsTotals));
        mAmmountNumber.setText(format.format(reimbursementTotals));

        if(Variables.doesUserWantNotifications)mDotForNotf.setVisibility(View.VISIBLE);

        SharedPreferences pref = mContext.getSharedPreferences(Constants.CONSENT_TO_TARGET, MODE_PRIVATE);
        final boolean canUseData = pref.getBoolean(Constants.CONSENT_TO_TARGET,false);
        if(!canUseData) findViewById(R.id.dotForTargeted).setVisibility(View.VISIBLE);
    }


    private int y_deltaBoi;
    private boolean isDownSwipingBoi = false;
    private GestureDetector swipeTopGestureDetector;
    private boolean isAtTopOfPage = true;
    private void SetOnTopScrollListener(){
        swipeTopGestureDetector = new GestureDetector(this, new MySwipebackMainGestureListener());
        mScrollView.setOnScrollChangedCallback(new LockableScrollView.OnScrollChangedCallback() {
            @Override
            public void onScroll(int l, int t, int oldl, int oldt) {
                Log.d(TAG,"T: "+t);
                isAtTopOfPage = t<=1;
            }
        });

        mScrollView.setOnTouchEvent(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (swipeTopGestureDetector.onTouchEvent(event)) {
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (isDownSwipingBoi) {
                        isDownSwipingBoi = false;
                        restoreScrollView();
                    }
                }
                return false;
            }
        });
        restoreScrollView();
    }

    private int prevPos = 0;
    class MySwipebackMainGestureListener extends GestureDetector.SimpleOnGestureListener {
        int origX = 0;
        int origY = 0;


        @Override
        public boolean onDown(MotionEvent event) {
//            Log.d(TAG, "onDown: ");
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            Log.e(TAG, "onDown: event.getRawX(): " + event.getRawX() + " event.getRawY()" + event.getRawY());

            CoordinatorLayout.LayoutParams lParams = (CoordinatorLayout.LayoutParams) mScrollView.getLayoutParams();
            y_deltaBoi = Y - lParams.topMargin;

            origX = lParams.leftMargin;
            origY = lParams.topMargin;


            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            final int Y = (int) e2.getRawY();
            final int X = (int) e2.getRawX();

            if(isAtTopOfPage){
                onTouchScrollView((double)(Y - y_deltaBoi));
            }else restoreScrollView();
            isDownSwipingBoi = true;

            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG,"velocityY-"+velocityY);
            if(isAtTopOfPage) {
                if (velocityY>0 && Math.abs(velocityY) > Math.abs(velocityX) && Math.abs(velocityY) > 2800 && Math.abs(velocityY) < 8000 ) {
                    onBackPressed();
                }else{
                    restoreScrollView();
                }
            }else restoreScrollView();
            isDownSwipingBoi = false;
            return false;

        }

    }

    private boolean isScrollPosChanged = false;
    private void onTouchScrollView(double pos){
        if(!isScrollPosChanged)isScrollPosChanged = true;
        if(pos-10>0)pos-=10;
        double newPos = (pos/10);
        RelativeLayout linLyt = findViewById(R.id.allElLay);
        if(newPos<Utils.dpToPx(30))
            linLyt.setTranslationY((float)newPos);
    }

    private void restoreScrollView(){
        if(isScrollPosChanged) {
            final float pos = 0;
            final RelativeLayout linLyt = findViewById(R.id.allElLay);
            linLyt.animate().translationY(pos).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            linLyt.setTranslationY(pos);
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
    public void onBackPressed(){
        if(isShowingPromptAboutChangingPrice){
            unShowPromptUserAboutChangingPrice();
        }else {
            if (isPromptingUserToStopTargeting) {
                hidePromptForStopTargeting();
            } else {
                if (isLoadedUserDataDialog) {
                    hideUserDataDialog();
                } else {
                    if (isNotificationsOpen) {
                        hidePromptAboutNotif(brr);
                    } else {
                        if (isSettingsCardExpanded) {
                            collapseSettingsCard();
                        } else {
                            if (!isCardCollapsed) {
                                mCollapseFeedChatButton.performClick();
                            } else if (!Variables.isMainActivityOnline) {
                                Intent intent = new Intent(Dashboard.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                super.onBackPressed();
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isNotificationsOpen = false;
    private BroadcastReceiver brr;
    private void promptUserAboutNotifications(){
        isNotificationsOpen = true;
        addTouchListenerForSwipeUpToGoBack();
        final LinearLayout mainLayout = findViewById(R.id.NotificationsLayout);
        mainLayout.setVisibility(View.VISIBLE);
        mainLayout.animate().translationY(0).alpha(1f).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mainLayout.setTranslationY(0);
//                        mainLayout.setScaleY(1f);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        String message;
        if(Variables.doesUserWantNotifications)
            message = "Do you wish to put off daily alerts about new ads?";
        else message = "Do you wish to put back on daily notifications?";
        Button b1 = findViewById(R.id.continueBtn);
        Button b2 = findViewById(R.id.cancelBtn);
        if(Variables.doesUserWantNotifications)b1.setText("PUT OFF.");
        else b1.setText("PUT ON.");
        TextView t = findViewById(R.id.explanation);
        final ImageButton imgBtn = findViewById(R.id.pickTimeIcon);
        final TextView timeTxt = findViewById(R.id.setTimeText);
        t.setText(message);

        String hour = Integer.toString(Variables.preferredHourOfNotf);
        String minute = Integer.toString(Variables.preferredMinuteOfNotf);
        if(Variables.preferredHourOfNotf<10) hour = "0"+hour;
        if(Variables.preferredMinuteOfNotf<10) minute = "0"+minute;
        timeTxt.setText(String.format("Set time : %s:%s", hour, minute));

        final BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String hour = Integer.toString(Variables.preferredHourOfNotf);
                String minute = Integer.toString(Variables.preferredMinuteOfNotf);
                if(Variables.preferredHourOfNotf<10) hour = "0"+hour;
                if(Variables.preferredMinuteOfNotf<10) minute = "0"+minute;
                timeTxt.setText(String.format("Set time : %s:%s", hour, minute));
                setUsersPreferredNotificationTime();
            }
        };
        brr = br;
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean newValue = !Variables.doesUserWantNotifications;
                setUsersPreferedNotfStatus(newValue);
                hidePromptAboutNotif(br);
            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                hidePromptAboutNotif(br);
            }
        });
        findViewById(R.id.pickTimeExp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgBtn.performClick();
            }
        });
        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getSupportFragmentManager(), "timePicker");
                getSupportFragmentManager().executePendingTransactions();
                newFragment.getDialog().setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        unKindaCollapseNotifAll();
                    }
                });
//                newFragment.setCancelable(false);
                kindaCollapseNotifAll();
            }
        });
        LocalBroadcastManager.getInstance(mContext).registerReceiver(br,new IntentFilter("UPDATE_CHOSEN_TIME"));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
                unKindaCollapseNotifAll();
            }
        },new IntentFilter("unKindaCollapseNotifAll"));
    }

    private void setUsersPreferredNotificationTime(){
        SharedPreferences pref = mContext.getSharedPreferences(Constants.PREFERRED_NOTF_HOUR,MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.putInt(Constants.PREFERRED_NOTF_HOUR,Variables.preferredHourOfNotf);
        Log("DashBoard","Set the users preferred noification hour to : "+Variables.preferredHourOfNotf);
        editor.apply();

        SharedPreferences pref7 = mContext.getSharedPreferences(Constants.PREFERRED_NOTF_MIN,MODE_PRIVATE);
        SharedPreferences.Editor editor7 = pref7.edit();
        editor7.clear();
        editor7.putInt(Constants.PREFERRED_NOTF_MIN,Variables.preferredMinuteOfNotf);
        Log("DashBoard","Set the users preferred noification minute to : "+Variables.preferredMinuteOfNotf);
        editor7.apply();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference adRef11 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.PREFERRED_NOTF_HOUR);
        adRef11.setValue(Variables.preferredHourOfNotf);

        DatabaseReference adRef12 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.PREFERRED_NOTF_MIN);
        adRef12.setValue(Variables.preferredMinuteOfNotf);

        Toast.makeText(mContext,"Done!",Toast.LENGTH_SHORT).show();
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener, DialogInterface.OnDismissListener  {
        private Context mContext = getContext();

        public void setContext(Context context){
            mContext = context;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, Variables.preferredHourOfNotf, Variables.preferredMinuteOfNotf,
                    DateFormat.is24HourFormat(getActivity()));
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            Variables.preferredHourOfNotf = hourOfDay;
            Variables.preferredMinuteOfNotf = minute;

            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("UPDATE_CHOSEN_TIME"));
        }

        @Override
        public void onDismiss(final DialogInterface dialog){
            try{
                super.onDismiss(dialog);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("unKindaCollapseNotifAll"));
            }catch (Exception e){
                e.printStackTrace();
            }
            final Activity activity = getActivity();
            if (activity instanceof DialogInterface.OnDismissListener) {
                ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
            }
        }
    }

    private void hidePromptAboutNotif(BroadcastReceiver br){
        isNotificationsOpen = false;
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(br);

        final LinearLayout NotificationsLayout = findViewById(R.id.NotificationsLayout);
        NotificationsLayout.setVisibility(View.VISIBLE);
        final int trans = -Utils.dpToPx(250);
        NotificationsLayout.animate().alpha(0f).translationY(trans).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        NotificationsLayout.setAlpha(0f);
                        NotificationsLayout.setTranslationY(trans);
                        NotificationsLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        unKindaCollapseSettings();
        removeTouchListenerForSwipeUpToGoBack();
    }

    private void promptUserAboutNotifications2(){
        String message;
        if(Variables.doesUserWantNotifications)
            message = "Do you wish to put off daily alerts about new ads?";
        else message = "Do you wish to put back on daily notifications?";

        final Dialog d = new Dialog(this);
        d.setTitle("Notifications.");
        d.setContentView(R.layout.dialog8);
        Button b1 = d.findViewById(R.id.continueBtn);
        Button b2 = d.findViewById(R.id.cancelBtn);
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        if(Variables.doesUserWantNotifications)b1.setText("PUT OFF.");
        else b1.setText("PUT ON.");
        TextView t = d.findViewById(R.id.explanation);
        ImageButton imgBtn = d.findViewById(R.id.pickTimeIcon);
        final TextView timeTxt = d.findViewById(R.id.setTimeText);
        t.setText(message);

        String hour = Integer.toString(Variables.preferredHourOfNotf);
        String minute = Integer.toString(Variables.preferredMinuteOfNotf);
        if(Variables.preferredHourOfNotf<10) hour = "0"+hour;
        if(Variables.preferredMinuteOfNotf<10) minute = "0"+minute;
        timeTxt.setText(String.format("Set time : %s:%s", hour, minute));

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean newValue = !Variables.doesUserWantNotifications;
                setUsersPreferedNotfStatus(newValue);
                d.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                d.cancel();
            }
        });
        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getSupportFragmentManager(), "timePicker");
            }
        });
        final BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String hour = Integer.toString(Variables.preferredHourOfNotf);
                String minute = Integer.toString(Variables.preferredMinuteOfNotf);
                if(Variables.preferredHourOfNotf<10) hour = "0"+hour;
                if(Variables.preferredMinuteOfNotf<10) minute = "0"+minute;
                timeTxt.setText(String.format("Set time : %s:%s", hour, minute));

                setUsersPreferredNotificationTime();
            }
        };
        LocalBroadcastManager.getInstance(mContext).registerReceiver(br,new IntentFilter("UPDATE_CHOSEN_TIME"));
        d.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                LocalBroadcastManager.getInstance(mContext).unregisterReceiver(br);
            }
        });
        d.show();
    }

    private void promptUserAboutChangingPrice2(){
        FragmentManager fm = getFragmentManager();
        ChangeCPVFragment cpvFragment = new ChangeCPVFragment();
        cpvFragment.setMenuVisibility(false);
        cpvFragment.show(fm,"Change cpv fragment");
        cpvFragment.setContext(mContext);
    }

    private Button cancelBtnCpV;private Button continueBtnCPV;private Button cancelBtn2;private Button changeBtn;
    private LinearLayout changingCPVMainLayout;private LinearLayout chooseAmountLayout;

    private boolean isShowingPromptAboutChangingPrice = false;
    private boolean isShowing_changingCPVMainLayout = false;
    private boolean isShowing_chooseAmountLayout = false;
    private void promptUserAboutChangingPrice(){
        isShowingPromptAboutChangingPrice = true;
        addTouchListenerForSwipeUpToGoBack();
        final RelativeLayout changeCPVLayout = findViewById(R.id.changeCPVLayout);
        changeCPVLayout.setVisibility(View.VISIBLE);
        isShowing_changingCPVMainLayout = true;
        changeCPVLayout.animate().alpha(1f).translationY(0).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                changeCPVLayout.setAlpha(1f);
                changeCPVLayout.setTranslationY(0);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();

        cancelBtnCpV = findViewById(R.id.cancelBtnCpV);
        continueBtnCPV = findViewById(R.id.continueBtnCPV);
        cancelBtn2 = findViewById(R.id.cancelButton);
        changeBtn = findViewById(R.id. submitButton);
        changingCPVMainLayout = findViewById(R.id.changingCPVMainLayout);
        chooseAmountLayout = findViewById(R.id.chooseAmountLayout);
        TextView currentCpv = findViewById(R.id.cuurentCPV);
        currentCpv.setText(String.format("Current charge : %dKsh.", Variables.constantAmountPerView));

        changingCPVMainLayout.setVisibility(View.VISIBLE);
        changingCPVMainLayout.setAlpha(1f);
        boolean hasChangedPrev =  mContext.getSharedPreferences(Constants.IS_CHANGING_CPV, MODE_PRIVATE)
                .getBoolean(Constants.IS_CHANGING_CPV, false);
        if(hasChangedPrev){
            int hasChangedPrevValue = mContext.getSharedPreferences(Constants.NEW_CPV, MODE_PRIVATE)
                    .getInt(Constants.NEW_CPV, Variables.constantAmountPerView);
            TextView newCPV = findViewById(R.id.newCPV);
            newCPV.setVisibility(View.VISIBLE);
            newCPV.setText(String.format("New set charge : %dKsh.", hasChangedPrevValue));
        }
        cancelBtnCpV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unShowPromptUserAboutChangingPrice();
            }
        });
        cancelBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unShowPromptUserAboutChangingPrice();
            }
        });
        continueBtnCPV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changingCPVMainLayout.setVisibility(View.VISIBLE);
                changingCPVMainLayout.setTranslationX(0);
                changingCPVMainLayout.animate().setDuration(mAnimationTime).translationX(NEG).alpha(0f)
                        .setInterpolator(new LinearOutSlowInInterpolator())
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                changingCPVMainLayout.setVisibility(View.GONE);

                                isShowing_changingCPVMainLayout = false;
                                isShowing_chooseAmountLayout = true;
                                changingCPVMainLayout.setTranslationX(NEG);
                            }
                        });
                chooseAmountLayout.setVisibility(View.VISIBLE);
                chooseAmountLayout.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0).alpha(1f)
                        .setInterpolator(new LinearOutSlowInInterpolator())
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                addTouchListenerForGoingNext_chooseAmountLayout();
                            }
                        });
            }
        });
        changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cpv;
                RadioButton button3 = findViewById(R.id.radioButton1);
                RadioButton button5 = findViewById(R.id.radioButton3);
                RadioButton button8 = findViewById(R.id.radioButton6);
                if(button3.isChecked()) cpv = 1;
                else if(button5.isChecked()) cpv = 3;
                else cpv = 6;
                if(isNetworkConnected(mContext)) makeChanges(cpv);
                else Toast.makeText(mContext,"You need an internet connection to make that change.",Toast.LENGTH_SHORT).show();
            }
        });

        addTouchListenerForGoingNext_changingCPVMainLayout();
    }

    private int x_bottomSheet_changingCPVMainLayout;
    private boolean isContinueSwiping_changingCPVMainLayout = false;
    private GestureDetector continueGt_changingCPVMainLayout;
    private void addTouchListenerForGoingNext_changingCPVMainLayout(){
        continueGt_changingCPVMainLayout = new GestureDetector(this, new MySwipeContinueGestureListener_changingCPVMainLayout());
    }
    private void onTouch_changingCPVMainLayout(MotionEvent event){
        continueGt_changingCPVMainLayout.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (isContinueSwiping_changingCPVMainLayout) {
                isContinueSwiping_changingCPVMainLayout = false;
                restoreOnBottomsheetContinue_changingCPVMainLayout();
            }
        }
    }
    class MySwipeContinueGestureListener_changingCPVMainLayout extends GestureDetector.SimpleOnGestureListener {
        int origX = 0;
        int origY = 0;


        @Override
        public boolean onDown(MotionEvent event) {
//            Log.d(TAG, "onDown: ");
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) changingCPVMainLayout.getLayoutParams();
            x_bottomSheet_changingCPVMainLayout = X - lParams.leftMargin;

            origX = lParams.leftMargin;
            origY = lParams.topMargin;


            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            final int Y = (int) e2.getRawY();
            final int X = (int) e2.getRawX();

            Log.d(TAG, "the scroll pos= " + ((double)(X-x_bottomSheet_changingCPVMainLayout)));

            animateContinueBottomsheetDialog_changingCPVMainLayout((double)(X - x_bottomSheet_changingCPVMainLayout));
            isContinueSwiping_changingCPVMainLayout = true;

            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(velocityX) > Math.abs(velocityY) && Math.abs(velocityX)>1400 && Math.abs(velocityX)<8000) {
                continueBtnCPV.performClick();
            }else{
                restoreOnBottomsheetContinue_changingCPVMainLayout();
            }
            isContinueSwiping_changingCPVMainLayout = false;
            return false;

        }
    }
    private void animateContinueBottomsheetDialog_changingCPVMainLayout(double pos){
        double newPos = (pos/10);
        changingCPVMainLayout.setVisibility(View.VISIBLE);
        chooseAmountLayout.setVisibility(View.VISIBLE);

        changingCPVMainLayout.setTranslationX(((float)newPos));
        chooseAmountLayout.setTranslationX(Utils.dpToPx(350)+(float)newPos);

        final float alph = 0f;
        float alphPos = (((float)((pos/10)))/100f);
        final float TweaksContainerAlph = 1f+alphPos;
        changingCPVMainLayout.setAlpha(TweaksContainerAlph);

        final float TweaksLayoutAlph = alph-alphPos;
        chooseAmountLayout.setAlpha(TweaksLayoutAlph);
    }
    private void restoreOnBottomsheetContinue_changingCPVMainLayout(){
        final float newPos = 0;
        final float alph2 = 1f;
        changingCPVMainLayout.animate().translationX(newPos).alpha(alph2).setInterpolator(new LinearOutSlowInInterpolator()).setDuration(mAnimationTime).start();

        final float trans = Utils.dpToPx(350);
        final float alph = 0f;
        chooseAmountLayout.animate().translationX(trans).alpha(alph).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        chooseAmountLayout.setTranslationX(trans);
                        chooseAmountLayout.setAlpha(alph);

                        changingCPVMainLayout.setTranslationX(newPos);
                        changingCPVMainLayout.setAlpha(alph2);
                        changingCPVMainLayout.setVisibility(View.VISIBLE);
                        chooseAmountLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }


    private int x_bottomSheet_chooseAmountLayout;
    private boolean isContinueSwiping_chooseAmountLayout = false;
    private GestureDetector continueGt_chooseAmountLayout;
    private void addTouchListenerForGoingNext_chooseAmountLayout(){
        continueGt_chooseAmountLayout = new GestureDetector(this, new MySwipeContinueGestureListener_chooseAmountLayout());
    }
    private void onTouch_chooseAmountLayout(MotionEvent event){
        continueGt_chooseAmountLayout.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (isContinueSwiping_chooseAmountLayout) {
                isContinueSwiping_chooseAmountLayout = false;
                restoreOnBottomsheetContinue_chooseAmountLayout();
            }
        }
    }
    class MySwipeContinueGestureListener_chooseAmountLayout extends GestureDetector.SimpleOnGestureListener {
        int origX = 0;
        int origY = 0;


        @Override
        public boolean onDown(MotionEvent event) {
//            Log.d(TAG, "onDown: ");
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) chooseAmountLayout.getLayoutParams();
            x_bottomSheet_chooseAmountLayout = X - lParams.leftMargin;

            origX = lParams.leftMargin;
            origY = lParams.topMargin;


            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            final int Y = (int) e2.getRawY();
            final int X = (int) e2.getRawX();

            Log.d(TAG, "the scroll pos= " + ((double)(X-x_bottomSheet_chooseAmountLayout)));

            animateContinueBottomsheetDialog_chooseAmountLayout((double)(X - x_bottomSheet_chooseAmountLayout));
            isContinueSwiping_chooseAmountLayout = true;

            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            restoreOnBottomsheetContinue_chooseAmountLayout();
            if (Math.abs(velocityX) > Math.abs(velocityY) && Math.abs(velocityX)>1400 && Math.abs(velocityX)<8000) {
//                continueBtnCPV.performClick();
            }else{
                restoreOnBottomsheetContinue_chooseAmountLayout();
            }
            isContinueSwiping_chooseAmountLayout = false;
            return false;

        }
    }
    private void animateContinueBottomsheetDialog_chooseAmountLayout(double pos){
        double newPos = (pos/10);
        changingCPVMainLayout.setVisibility(View.VISIBLE);
        chooseAmountLayout.setVisibility(View.VISIBLE);

        changingCPVMainLayout.setTranslationX(NEG+((float)newPos));
        chooseAmountLayout.setTranslationX((float)newPos);

        final float alph = 0f;
        float alphPos = (((float)((pos/10)))/100f);
        final float TweaksContainerAlph = 1f+alphPos;
//        chooseAmountLayout.setAlpha(TweaksContainerAlph);

        final float TweaksLayoutAlph = alph-alphPos;
        changingCPVMainLayout.setAlpha(0.4f-TweaksLayoutAlph);
    }
    private void restoreOnBottomsheetContinue_chooseAmountLayout(){
        final float newPos = 0;
        final float alph2 = 1f;
        chooseAmountLayout.animate().translationX(newPos).alpha(alph2).setInterpolator(new LinearOutSlowInInterpolator()).setDuration(mAnimationTime).start();

        final float trans = NEG;
        final float alph = 0.4f;
        changingCPVMainLayout.animate().translationX(trans).alpha(alph).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        changingCPVMainLayout.setTranslationX(trans);
                        changingCPVMainLayout.setAlpha(alph);

                        chooseAmountLayout.setTranslationX(newPos);
                        chooseAmountLayout.setAlpha(alph2);
                        chooseAmountLayout.setVisibility(View.VISIBLE);
                        changingCPVMainLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }



    private void onclicks(){
        cancelBtnCpV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unShowPromptUserAboutChangingPrice();
            }
        });
        cancelBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unShowPromptUserAboutChangingPrice();
            }
        });
        continueBtnCPV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changingCPVMainLayout.setVisibility(View.VISIBLE);
                changingCPVMainLayout.setTranslationX(0);
                changingCPVMainLayout.animate().setDuration(mAnimationTime).translationX(-200).alpha(0f)
                        .setInterpolator(new LinearOutSlowInInterpolator())
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                changingCPVMainLayout.setVisibility(View.GONE);
                                changingCPVMainLayout.setTranslationX(1);
                            }
                        });
                chooseAmountLayout.setVisibility(View.VISIBLE);
                chooseAmountLayout.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0).alpha(1f)
                        .setInterpolator(new LinearOutSlowInInterpolator())
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                            }
                        });
            }
        });
        changeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cpv;
                RadioButton button3 = findViewById(R.id.radioButton1);
                RadioButton button5 = findViewById(R.id.radioButton3);
                RadioButton button8 = findViewById(R.id.radioButton6);
                if(button3.isChecked()) cpv = 1;
                else if(button5.isChecked()) cpv = 3;
                else cpv = 6;
                if(isNetworkConnected(mContext)) makeChanges(cpv);
                else Toast.makeText(mContext,"You need an internet connection to make that change.",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeChanges(int newCpv) {
        SharedPreferences prefs2 = mContext.getSharedPreferences(Constants.IS_CHANGING_CPV, MODE_PRIVATE);
        boolean hasChangedPrev = prefs2.getBoolean(Constants.IS_CHANGING_CPV, false);

        SharedPreferences prefs = mContext.getSharedPreferences(Constants.NEW_CPV, MODE_PRIVATE);
        int hasChangedPrevValue = prefs.getInt(Constants.NEW_CPV, Variables.constantAmountPerView);

        if(hasChangedPrev){
            new DatabaseManager().setBooleanForResetSubscriptions(newCpv, mContext);
            Intent intent = new Intent("SHOW_PROMPT");
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            unShowPromptUserAboutChangingPrice();
        }else{
            if (newCpv != Variables.constantAmountPerView) {
                new DatabaseManager().setBooleanForResetSubscriptions(newCpv, mContext);
                Intent intent = new Intent("SHOW_PROMPT");
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                unShowPromptUserAboutChangingPrice();
            } else {
                if(isShowingPromptAboutChangingPrice)Toast.makeText(mContext, "That's already your current charge.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isNetworkConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }

    private void unShowPromptUserAboutChangingPrice(){
        isShowingPromptAboutChangingPrice = false;
        isShowing_chooseAmountLayout = false;
        final RelativeLayout changeCPVLayout = findViewById(R.id.changeCPVLayout);
        changeCPVLayout.setVisibility(View.VISIBLE);
        final int trans = -Utils.dpToPx(250);
        changeCPVLayout.animate().alpha(0f).translationY(trans).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        changeCPVLayout.setAlpha(0f);
                        changeCPVLayout.setTranslationY(trans);
                        resetCPVViews();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
        unKindaCollapseSettings();
        removeTouchListenerForSwipeUpToGoBack();
    }

    private void resetCPVViews() {
        changingCPVMainLayout.setVisibility(View.VISIBLE);
        changingCPVMainLayout.setTranslationX(0);

        chooseAmountLayout.setVisibility(View.GONE);
        chooseAmountLayout.setTranslationX(Utils.dpToPx(350));
    }




    private void promptUserIfSureToLogout2(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("AdCafé");
        builder.setMessage("Are you sure you want to log out?")
                .setCancelable(true)
                .setPositiveButton("Yes.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logoutUser();
                    }
                })
                .setNegativeButton("No!!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    private void promptUserIfSureToLogout(){
        final Dialog d = new Dialog(this);
        d.setTitle("Logout.");
        d.setContentView(R.layout.dialog9);
        Button b1 = d.findViewById(R.id.continueBtn);
        Button b2 = d.findViewById(R.id.cancelBtn);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                d.cancel();
            }
        });
        d.show();
    }

    private void promptUserAboutPayout(){
        int reimbursementTotals;

        if(Variables.getMonthAdTotals(mKey) ==0) {
            SharedPreferences prefs3 = getSharedPreferences("ReimbursementTotals", MODE_PRIVATE);
            reimbursementTotals = prefs3.getInt(Constants.REIMBURSEMENT_TOTALS, 0);
        }else reimbursementTotals = Variables.getTotalReimbursementAmount();

        FragmentUserPayoutBottomSheet fragmentModalBottomSheet = new FragmentUserPayoutBottomSheet();
        fragmentModalBottomSheet.setActivity(Dashboard.this);
        fragmentModalBottomSheet.setDetails(reimbursementTotals,Variables.getPassword());
        fragmentModalBottomSheet.show(getSupportFragmentManager(),"BottomSheet Fragment");

        new DatabaseManager().setIsMakingPayoutInFirebase(true);
    }

    private void promptUserToShareApp(){
        Vibrator s = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        s.vibrate(50);
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getText(R.string.shareText2));
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.shareText)));
    }




    public void promptUserAboutChanges(){
        boolean hasChangedPrev =  mContext.getSharedPreferences(Constants.IS_CHANGING_CPV, MODE_PRIVATE)
                .getBoolean(Constants.IS_CHANGING_CPV, false);
        String message = "Your changes will take effect starting tomorrow.";
        if(!hasChangedPrev)message = "Your changes have been set.";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("AdCafé");
        builder.setMessage(message)
                .setCancelable(true)
                .setPositiveButton("Cool.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void logoutUser() {
        if (FirebaseAuth.getInstance() != null) {
            FirebaseAuth.getInstance().signOut();
        }
        clearUserDataFromSharedPreferences();
        Variables.resetAllValues();
        Intent intent = new Intent(Dashboard.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setUsersPreferedNotfStatus(boolean value){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();

        SharedPreferences pref7 = mContext.getSharedPreferences(Constants.PREFERRED_NOTIF,MODE_PRIVATE);
        SharedPreferences.Editor editor7 = pref7.edit();
        editor7.clear();
        editor7.putBoolean(Constants.PREFERRED_NOTIF,Variables.doesUserWantNotifications);
        Log("DashBoard","Set the users preference for seing notifications to : "+Variables.doesUserWantNotifications);
        editor7.apply();

        DatabaseReference adRef11 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.PREFERRED_NOTIF);
        adRef11.setValue(value);

        Variables.doesUserWantNotifications = value;

        if(Variables.doesUserWantNotifications)mDotForNotf.setVisibility(View.VISIBLE);
        else mDotForNotf.setVisibility(View.INVISIBLE);

        Toast.makeText(mContext,"Done!",Toast.LENGTH_SHORT).show();
    }






    private BroadcastReceiver mMessageReceiverForStartPayout = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log("Dashboard", "Broadcast has been received to start payout.");
            startPayout();
        }
    };

    private BroadcastReceiver mMessageReceiverForResetPasswod = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log("Dashboard", "Broadcast has been received reset password.");
//            showDialogForResetPassword();
        }
    };


    //Payout api implementation comes here...
    private void startPayout(){
        int reimbursementTotals;
        new DatabaseManager().setIsMakingPayoutInFirebase(true);
        if(Variables.getMonthAdTotals(mKey) ==0) {
            SharedPreferences prefs3 = getSharedPreferences("ReimbursementTotals", MODE_PRIVATE);
            reimbursementTotals = prefs3.getInt(Constants.REIMBURSEMENT_TOTALS, 0);

        }else reimbursementTotals = Variables.getTotalReimbursementAmount();

        if(Variables.isUsingNewCoinValueSystem){
            if(Variables.legitCoins!=null){
                if(!Variables.legitCoins.isEmpty()){
                    reimbursementTotals = getTotalValueFromEachCoin(Variables.legitCoins);
                }
            }
        }

        String payoutPhoneNumber = Variables.phoneNo;
        int payoutAmount = reimbursementTotals;
        String PAYOUT_SUCCESSFUL = "PAYOUT_SUCCESSFUL";
        String PAYOUT_FAILED = "PAYOUT_FAILED";

        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.PAY_POOL);
        DatabaseReference pushRef = adRef.push();
        Variables.transactionID = "TRANS"+pushRef.getKey();

        String newPhoneNo = "254"+payoutPhoneNumber.substring(1);
        Log("Dashboard","new Phone no is: "+newPhoneNo);


        if(FirebaseAuth.getInstance().getCurrentUser().getEmail().equals(Constants.ADMIN_ACC)) payoutAmount = 10;
        String StringPayoutAmount = Integer.toString(payoutAmount);

        Payments mps = new Payments(mContext,PAYOUT_SUCCESSFUL,PAYOUT_FAILED);
        mps.MpesaMakePayouts(StringPayoutAmount,newPhoneNo,mContext);


//        payments.makePayouts(Variables.transactionID,payoutPhoneNumber,payoutAmount);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForSuccessfulPayout,
                new IntentFilter(PAYOUT_SUCCESSFUL));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForFailedPayout,
                new IntentFilter(PAYOUT_FAILED));
        mProgForPayments.show();
    }

    private BroadcastReceiver mMessageReceiverForSuccessfulPayout = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log("Dashboard", "Broadcast has been received that payout is finished.");
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
            resetUserMoneyTotals();
        }
    };

    private BroadcastReceiver mMessageReceiverForFailedPayout = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log("Dashboard", "Broadcast has been received that payout has failed.");
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
            showFailedPayoutsView();
            mProgForPayments.dismiss();
            new DatabaseManager().setIsMakingPayoutInFirebase(false);
        }
    };

    private void showFailedPayoutsView() {
        final Dialog d = new Dialog(this);
        d.setTitle("Failed Payout.");
        d.setContentView(R.layout.dialog94);
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        Button b1 = d.findViewById(R.id.okBtn);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }

    private void promptUserForUnableToPayout(){
        final Dialog d = new Dialog(this);
        d.setTitle("Unable to make payout.");
        d.setContentView(R.layout.dialog992);
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        Button b1 = d.findViewById(R.id.okBtn);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }



    //call this when the payout process has occurred...
    private void resetUserMoneyTotals(){
        int amount = Variables.getTotalReimbursementAmount();
        setPayoutReceiptInFireBase(amount);
        removeAdminAmm(amount);
//        addAmountForAdmin(amount);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference adRef9 = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.REIMBURSEMENT_TOTALS);
        adRef9.setValue(0);

        new DatabaseManager().clearAdsSeenSoFarInFirebase();
        Variables.setTotalReimbursementAmount(0);

        SharedPreferences pref3 = mContext.getSharedPreferences("ReimbursementTotals",MODE_PRIVATE);
        SharedPreferences.Editor editor3 = pref3.edit();
        editor3.clear();
        editor3.putInt(Constants.REIMBURSEMENT_TOTALS,Variables.getTotalReimbursementAmount());
        Log("Dashboard","Setting the Reimbursement totals in shared preferences - "+Integer.toString(Variables.getTotalReimbursementAmount()));
        editor3.apply();

        setValues();
        mProgForPayments.dismiss();
        showSuccessfulPayoutPrompt();
        clearUsersCoinList();
    }

    private void clearUsersCoinList() {
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.USERS_COIN_LIST, MODE_PRIVATE);
        prefs.edit().clear().apply();

        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mref = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.USERS_COIN_LIST);
        mref.setValue(null);

        Variables.isUsingNewCoinValueSystem = true;
        Variables.legitCoins = null;
        setUserAsUsingNewValueSystem();
    }

    private void setUserAsUsingNewValueSystem(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid)
                .child(Constants.COIN_VALUE_SYSTEM);
        userRef.setValue(true);
    }

    private void showSuccessfulPayoutPrompt() {
        Log.e("Dashboard","Showing Payout");
        final Dialog d = new Dialog(this);
        d.setTitle("Successful Payout.");
        d.setContentView(R.layout.dialog95);
        Button b1 = d.findViewById(R.id.okBtn);
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
        new DatabaseManager().setIsMakingPayoutInFirebase(false);
    }

    private void setPayoutReceiptInFireBase(int amount) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.REIMBURSEMENT_HISTORY).child(Long.toString(-TimeManager.getDateInDays()));
        DatabaseReference pushRef = adRef.push();
        String pushId= pushRef.getKey();

        PayoutResponse myResp = Variables.payoutObject;
        myResp.setDateInDays(TimeManager.getDateInDays());
        myResp.setDate(TimeManager.getDate());
        myResp.setTime(TimeManager.getTime());
        myResp.setPhoneNo(Variables.phoneNo);
        myResp.setUserId(uid);
        myResp.setAmount(amount);
        myResp.setPushID(pushId);

        pushRef.setValue(myResp);

        DatabaseReference dbrefTrans = FirebaseDatabase.getInstance().getReference(Constants.TRANSACTIONS)
                .child(Constants.PAYOUTS).child(Constants.MPESA_PAYOUTS)
                .child(TimeManager.getYear()).child(TimeManager.getMonth()).child(TimeManager.getDay())
                .child(pushId);
        dbrefTrans.setValue(myResp);
    }

    private void clearUserDataFromSharedPreferences(){
        SharedPreferences pref = getApplicationContext().getSharedPreferences("TodayTotals", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();

        SharedPreferences pref2 = getApplicationContext().getSharedPreferences("MonthTotals", MODE_PRIVATE);
        SharedPreferences.Editor editor2 = pref2.edit();
        editor2.clear();
        editor2.apply();

        SharedPreferences pref7 = getApplicationContext().getSharedPreferences("ReimbursementTotals", MODE_PRIVATE);
        SharedPreferences.Editor editor7 = pref7.edit();
        editor7.clear();
        editor7.apply();

        SharedPreferences pref8 = getApplicationContext().getSharedPreferences(Constants.CONSTANT_AMMOUNT_PER_VIEW,MODE_PRIVATE);
        SharedPreferences.Editor editor8 = pref8.edit();
        editor8.clear();
        editor8.apply();

        SharedPreferences pref4 = mContext.getSharedPreferences("UID", MODE_PRIVATE);
        SharedPreferences.Editor editor4 = pref4.edit();
        editor4.clear();
        editor4.apply();

        SharedPreferences pref5 = mContext.getSharedPreferences("CurrentSubIndex", MODE_PRIVATE);
        SharedPreferences.Editor editor5 = pref5.edit();
        editor5.clear();
        editor5.apply();

        SharedPreferences pref6 = mContext.getSharedPreferences("CurrentAdInSubscription", MODE_PRIVATE);
        SharedPreferences.Editor editor6 = pref6.edit();
        editor6.clear();
        editor6.apply();
    }




    private void showDialogForResetPassword(){
        final Dialog d = new Dialog(Dashboard.this);
        d.setTitle("Password reset.");
        d.setContentView(R.layout.dialog92);
        Button b2 =  d.findViewById(R.id.continueBtn);
        Button b1 = d.findViewById(R.id.cancelBtn);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        d.show();
    }

    private boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

    private void createProgressDialog(){
        mProgForPayments = new ProgressDialog(this,R.style.AppCompatAlertDialogStyle);
        mProgForPayments.setTitle(R.string.app_name);
        mProgForPayments.setMessage("This should take a few seconds... ");
        mProgForPayments.setCancelable(false);
        mProgForPayments.setProgress(ProgressDialog.STYLE_SPINNER);
        mProgForPayments.setIndeterminate(true);
    }

    private void showPromptForJoke(){
        final Dialog d = new Dialog(Dashboard.this);
        d.setTitle("Socks are under-appreciated.");
        d.setContentView(R.layout.dialog96);
        final LinearLayout loadingLayout = d.findViewById(R.id.loadingLayout);
        final LinearLayout mainLayout = d.findViewById(R.id.mainLayout);

        final TextView jokeHeader = d.findViewById(R.id.jokeHeader);
        final TextView jokePart = d.findViewById(R.id.jokePart);
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                jokePart.setText(joke);
                jokeHeader.setText(""+ Html.fromHtml("&#128516;") );
                loadingLayout.setVisibility(View.GONE);
                mainLayout.setVisibility(View.VISIBLE);
                LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
            }
        },new IntentFilter("JOKE_INTENT"));
        d.show();

        OkHttpClient client = new OkHttpClient();
        String url1 = "https://icanhazdadjoke.com/";
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url1).newBuilder();
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .addHeader("Accept", "application/json")
                .url(url)
                .build();
        Callback cb = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
                Log("Dashboard","Response gotten"+ response.toString());
                try{
                    String jsonData = response.body().string();
                    if (response.isSuccessful()) {
                        JSONObject jokeJSON = new JSONObject(jsonData);
                        joke = jokeJSON.getString("joke");
                        Log("Dashboard","The joke : "+joke);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("JOKE_INTENT"));
                    }
                }catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        Call call = client.newCall(request);
        call.enqueue(cb);

    }
    
    

    private void Log(String tag,String message){
        try{
            String user = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            if(user.equals(Constants.ADMIN_ACC)) Log.d(tag,message);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void setGestureListener(){
        LinearLayout dashboardCoordinator = findViewById(R.id.linLyt);
    }

    private void removeAdminAmm(final long amount){
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.TOTAL_ALL_TIME_ADS);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    long amm = dataSnapshot.getValue(long.class);
                    long newAmm = amm -= amount;
                    dbRef.setValue(newAmm);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        final DatabaseReference dbRef2 = FirebaseDatabase.getInstance().getReference(Constants.ADMIN_MONEY);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    long amm = dataSnapshot.getValue(long.class);
                    long newAmm = amm -= amount;
                    dbRef2.setValue(newAmm);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

    private void loadUserDataDialog2(){
        FragmentManager fm = getFragmentManager();
        SetUsersPersonalInfo cpvFragment = new SetUsersPersonalInfo();
        cpvFragment.setMenuVisibility(false);
        cpvFragment.show(fm,"Edit User Data.");
        cpvFragment.setfragcontext(mContext);
        cpvFragment.setActivity(thisActivity);
    }

    private boolean isLoadedUserDataDialog = false;
    private LinearLayout mainLayout;private Button okBtn;private LinearLayout mainLaout2;private Button okBtn1point5;
    private LinearLayout genderLayout;private RadioButton radioButtonFemale;private RadioButton radioButtonMale;private Button skip;
    private Button okBtn2;
    private LinearLayout ageLayout;private ImageButton backBtn1;private ImageButton openCalendar;private TextView setAgeTextView;
    private Button skip2;private Button okBtn3;
    private LinearLayout locationLayout;private ImageButton backBtn2;private TextView locationNumberText;private ImageButton openMapImg;
    private Button openMap;private Button skip3;private Button okBtn4;
    private LinearLayout concludeLayout;private Button okBtn5;private float NEG;
    private int durat = Constants.ANIMATION_DURATION;

    private void loadUserDataDialog(){
        isLoadedUserDataDialog = true;
        addTouchListenerForSwipeUpToGoBack();
        mainLayout = findViewById(R.id.mainLayoutPC);
        okBtn = findViewById(R.id.okBtnPers);

        mainLaout2 = findViewById(R.id.mainLayout2);
        okBtn1point5 = findViewById(R.id.okBtn1point5);

        genderLayout = findViewById(R.id.genderLayout);
        radioButtonFemale = findViewById(R.id.radioButtonFemale);
        radioButtonMale = findViewById(R.id.radioButtonMale);
        skip = findViewById(R.id.skip);
        okBtn2 = findViewById(R.id.okBtn2);

        ageLayout = findViewById(R.id.ageLayout);
        backBtn1 = findViewById(R.id.backBtn1);
        openCalendar = findViewById(R.id.calendar);
        setAgeTextView = findViewById(R.id.setAgeText);
        skip2 = findViewById(R.id.skip2);
        okBtn3 = findViewById(R.id.okBtn3);

        locationLayout = findViewById(R.id.locationLayout);
        locationNumberText = findViewById(R.id.locationNumberText);
        backBtn2 = findViewById(R.id.backBtn2);
        openMapImg = findViewById(R.id.openMapImg);
        openMap = findViewById(R.id.openMap);
        skip3 = findViewById(R.id.skip3);
        okBtn4 = findViewById(R.id.okBtn4);

        concludeLayout= findViewById(R.id.concludeLayout);
        okBtn5= findViewById(R.id.okBtn5);

        concludeLayout.setVisibility(View.GONE);
        locationLayout.setVisibility(View.GONE);
        ageLayout.setVisibility(View.GONE);
        genderLayout.setVisibility(View.GONE);
        mainLaout2.setVisibility(View.GONE);
        mainLayout.setVisibility(View.VISIBLE);

        final RelativeLayout personalisedContentLayout = findViewById(R.id.personalisedContentLayout);
        personalisedContentLayout.setVisibility(View.VISIBLE);
        mainLayout.setAlpha(1f);
        personalisedContentLayout.animate().alpha(1f).translationY(0).setInterpolator(new LinearOutSlowInInterpolator()).setDuration(mAnimationTime)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        personalisedContentLayout.setTranslationY(0);
                        personalisedContentLayout.setAlpha(1f);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        resetViews();
        loadFirstView();
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForSetLocations,
                new IntentFilter("SET_USER_PERSONAL_LOCATIONS"));
    }



    private void loadFirstView() {
        isShowing_mainLayout = true;
        mainLayout.setVisibility(View.VISIBLE);
        mainLayout.setAlpha(1f);
        mainLayout.setTranslationX(0);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainLayout.animate().setDuration(durat).translationX(NEG).alpha(0f)
                        .setInterpolator(new LinearOutSlowInInterpolator())
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                mainLayout.setTranslationX(NEG);
                                mainLayout.setVisibility(View.GONE);
                                mainLayout.setAlpha(0f);
                            }
                        });
                loadFirstView2();
                isShowing_mainLayout = false;
            }
        });
        addTouchListenerForGoingNext_mainLayout();
    }

    private int x_bottomSheet_mainLayout;
    private boolean isShowing_mainLayout = false;
    private boolean isContinueSwiping_mainLayout = false;
    private GestureDetector continueGt_mainLayout;
    private void addTouchListenerForGoingNext_mainLayout(){
        continueGt_mainLayout = new GestureDetector(this, new MySwipeContinueGestureListener_mainLayout());
    }
    private void onTouch_mainLayout(MotionEvent event){
        continueGt_mainLayout.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (isContinueSwiping_mainLayout) {
                isContinueSwiping_mainLayout = false;
                restoreOnBottomsheetContinue_mainLayout();
            }
        }
    }
    class MySwipeContinueGestureListener_mainLayout extends GestureDetector.SimpleOnGestureListener {
        int origX = 0;
        int origY = 0;


        @Override
        public boolean onDown(MotionEvent event) {
//            Log.d(TAG, "onDown: ");
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) mainLayout.getLayoutParams();
            x_bottomSheet_mainLayout = X - lParams.leftMargin;

            origX = lParams.leftMargin;
            origY = lParams.topMargin;


            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            final int Y = (int) e2.getRawY();
            final int X = (int) e2.getRawX();

            Log.d(TAG, "the scroll pos= " + ((double)(X-x_bottomSheet_mainLayout)));

            animateContinueBottomsheetDialog_mainLayout((double)(X - x_bottomSheet_mainLayout));
            isContinueSwiping_mainLayout = true;

            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(velocityX) > Math.abs(velocityY) && Math.abs(velocityX)>1400 && Math.abs(velocityX)<8000 && velocityX<0) {
                okBtn.performClick();
            }else{
                restoreOnBottomsheetContinue_mainLayout();
            }
            isContinueSwiping_mainLayout = false;
            return false;

        }
    }
    private void animateContinueBottomsheetDialog_mainLayout(double pos){
        double newPos = (pos/10);
        mainLaout2.setVisibility(View.VISIBLE);
        mainLayout.setVisibility(View.VISIBLE);

        mainLayout.setTranslationX(((float)newPos));
        mainLaout2.setTranslationX(Utils.dpToPx(350)+(float)newPos);

        final float alph = 0f;
        float alphPos = (((float)((pos/10)))/100f);
        final float TweaksContainerAlph = 1f+alphPos;
        mainLayout.setAlpha(TweaksContainerAlph);

        final float TweaksLayoutAlph = alph+alphPos;
        mainLaout2.setAlpha(0.6f);
    }
    private void restoreOnBottomsheetContinue_mainLayout(){
        final float newPos = 0;
        final float alph2 = 1f;
        mainLayout.animate().translationX(newPos).alpha(alph2).setInterpolator(new LinearOutSlowInInterpolator()).setDuration(mAnimationTime).start();

        final float trans = Utils.dpToPx(350);
        final float alph = 0f;
        mainLaout2.animate().translationX(trans).alpha(alph).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mainLaout2.setTranslationX(trans);
                        mainLaout2.setAlpha(alph);

                        mainLayout.setTranslationX(newPos);
                        mainLayout.setAlpha(alph2);

                        mainLayout.setVisibility(View.VISIBLE);
                        mainLaout2.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }



    private void loadFirstView2(){
        isShowing_mainLaout2 = true;
        mainLaout2.setVisibility(View.VISIBLE);
        mainLaout2.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0).alpha(1f)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mainLaout2.setTranslationX(0);
                mainLaout2.setAlpha(1f);
                mainLayout.setVisibility(View.VISIBLE);
                super.onAnimationEnd(animation);
            }
        });
        okBtn1point5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainLaout2.animate().setDuration(durat).translationX(NEG).alpha(0f)
                        .setInterpolator(new LinearOutSlowInInterpolator())
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mainLaout2.setTranslationX(NEG);
                                mainLaout2.setVisibility(View.GONE);
                                mainLaout2.setAlpha(0f);
                                super.onAnimationEnd(animation);
                            }
                        });
                loadSetGenderView();
                isShowing_mainLaout2 = false;
            }
        });
        addTouchListenerForGoingNext_mainLaout2();
    }

    private int x_bottomSheet_mainLaout2;
    private boolean isContinueSwiping_mainLaout2 = false;
    private boolean isShowing_mainLaout2 = false;
    private GestureDetector continueGt_mainLaout2;
    private void addTouchListenerForGoingNext_mainLaout2(){
        continueGt_mainLaout2 = new GestureDetector(this, new MySwipeContinueGestureListener_mainLaout2());
    }
    private void onTouch_mainLaout2(MotionEvent event){
        continueGt_mainLaout2.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (isContinueSwiping_mainLaout2) {
                isContinueSwiping_mainLaout2 = false;
                restoreOnBottomsheetContinue_mainLaout2();
            }
        }
    }
    class MySwipeContinueGestureListener_mainLaout2 extends GestureDetector.SimpleOnGestureListener {
        int origX = 0;
        int origY = 0;


        @Override
        public boolean onDown(MotionEvent event) {
//            Log.d(TAG, "onDown: ");
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) mainLaout2.getLayoutParams();
            x_bottomSheet_mainLaout2 = X - lParams.leftMargin;

            origX = lParams.leftMargin;
            origY = lParams.topMargin;


            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            final int Y = (int) e2.getRawY();
            final int X = (int) e2.getRawX();

            Log.d(TAG, "the scroll pos= " + ((double)(X-x_bottomSheet_mainLaout2)));

            animateContinueBottomsheetDialog_mainLaout2((double)(X - x_bottomSheet_mainLaout2));
            isContinueSwiping_mainLaout2 = true;

            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(velocityX) > Math.abs(velocityY) && Math.abs(velocityX)>1400 && Math.abs(velocityX)<8000 && velocityX<0) {
                okBtn1point5.performClick();
            }else{
                restoreOnBottomsheetContinue_mainLaout2();
            }
            isContinueSwiping_mainLaout2 = false;
            return false;

        }
    }
    private void animateContinueBottomsheetDialog_mainLaout2(double pos){
        double newPos = (pos/10);
        mainLayout.setVisibility(View.VISIBLE);
        genderLayout.setVisibility(View.VISIBLE);
        mainLaout2.setVisibility(View.VISIBLE);

        mainLaout2.setTranslationX(((float)newPos));
        genderLayout.setTranslationX(Utils.dpToPx(350)+(float)newPos);
        mainLayout.setTranslationX(NEG+(float)newPos);

        final float alph = 0f;
        float alphPos = (((float)((pos/10)))/100f);
        final float TweaksContainerAlph = 1f+alphPos;
        mainLaout2.setAlpha(TweaksContainerAlph);

        final float TweaksLayoutAlph = alph-alphPos;
        genderLayout.setAlpha(0.6f);
        mainLayout.setAlpha(0.4f-TweaksLayoutAlph);
    }
    private void restoreOnBottomsheetContinue_mainLaout2(){
        final float newPos = 0;
        final float alph2 = 1f;
        mainLaout2.animate().translationX(newPos).alpha(alph2).setInterpolator(new LinearOutSlowInInterpolator()).setDuration(mAnimationTime).start();

        final float trans = Utils.dpToPx(350);
        final float alph = 0.4f;
        mainLayout.animate().translationX(NEG).alpha(alph).setDuration(mAnimationTime).start();
        genderLayout.animate().translationX(trans).alpha(alph).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        genderLayout.setTranslationX(trans);
                        genderLayout.setAlpha(alph);

                        mainLaout2.setTranslationX(newPos);
                        mainLaout2.setAlpha(alph2);

                        mainLayout.setTranslationX(NEG);
                        mainLayout.setAlpha(alph);
                        mainLayout.setVisibility(View.GONE);

                        mainLaout2.setVisibility(View.VISIBLE);
                        genderLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }



    private void loadSetGenderView() {
        isShowing_genderLayout = true;
        genderLayout.setVisibility(View.VISIBLE);
        genderLayout.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0).alpha(1f)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                genderLayout.setTranslationX(0);
                genderLayout.setAlpha(1f);
                genderLayout.setVisibility(View.VISIBLE);
                super.onAnimationEnd(animation);
            }
        });
        SharedPreferences prefs2 = mContext.getSharedPreferences(Constants.GENDER, MODE_PRIVATE);
        final String gender = prefs2.getString(Constants.GENDER, "NULL");
        if(!gender.equals("NULL")&&gender.equals(Constants.MALE)) radioButtonMale.setChecked(true);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                genderLayout.setVisibility(View.GONE);
                genderLayout.animate().translationX(NEG).alpha(0f).setDuration(durat)
                        .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        genderLayout.setTranslationX(NEG);
                        genderLayout.setAlpha(0f);
                        genderLayout.setVisibility(View.GONE);
                        super.onAnimationEnd(animation);
                    }
                });
                loadSetBirthdayView();
                isShowing_genderLayout = false;
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
//                genderLayout.setVisibility(View.GONE);
                genderLayout.animate().translationX(NEG).alpha(0f).setDuration(durat)
                        .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        genderLayout.setTranslationX(NEG);
                        genderLayout.setVisibility(View.GONE);
                        genderLayout.setAlpha(1f);
                        super.onAnimationEnd(animation);
                    }
                });
                loadSetBirthdayView();
                isShowing_genderLayout = false;
            }
        });
        addTouchListenerForGoingNext_genderLayout();
    }

    private int x_bottomSheet_genderLayout;
    private boolean isContinueSwiping_genderLayout = false;
    private boolean isShowing_genderLayout = false;
    private GestureDetector continueGt_genderLayout;
    private void addTouchListenerForGoingNext_genderLayout(){
        continueGt_genderLayout = new GestureDetector(this, new MySwipeContinueGestureListener_genderLayout());
    }
    private void onTouch_genderLayout(MotionEvent event){
        continueGt_genderLayout.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (isContinueSwiping_genderLayout) {
                isContinueSwiping_genderLayout = false;
                restoreOnBottomsheetContinue_genderLayout();
            }
        }
    }
    class MySwipeContinueGestureListener_genderLayout extends GestureDetector.SimpleOnGestureListener {
        int origX = 0;
        int origY = 0;


        @Override
        public boolean onDown(MotionEvent event) {
//            Log.d(TAG, "onDown: ");
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) genderLayout.getLayoutParams();
            x_bottomSheet_genderLayout = X - lParams.leftMargin;

            origX = lParams.leftMargin;
            origY = lParams.topMargin;


            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            final int Y = (int) e2.getRawY();
            final int X = (int) e2.getRawX();

            Log.d(TAG, "the scroll pos= " + ((double)(X-x_bottomSheet_genderLayout)));

            animateContinueBottomsheetDialog_genderLayout((double)(X - x_bottomSheet_genderLayout));
            isContinueSwiping_genderLayout = true;

            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(velocityX) > Math.abs(velocityY) && Math.abs(velocityX)>1400 && Math.abs(velocityX)<8000 && velocityX<0) {
                skip.performClick();
            }else{
                restoreOnBottomsheetContinue_genderLayout();
            }
            isContinueSwiping_genderLayout = false;
            return false;

        }
    }
    private void animateContinueBottomsheetDialog_genderLayout(double pos){
        double newPos = (pos/10);
        mainLaout2.setVisibility(View.VISIBLE);
        ageLayout.setVisibility(View.VISIBLE);
        genderLayout.setVisibility(View.VISIBLE);

        genderLayout.setTranslationX(((float)newPos));
        ageLayout.setTranslationX(Utils.dpToPx(350)+(float)newPos);
        mainLaout2.setTranslationX(NEG+(float)newPos);

        final float alph = 0f;
        float alphPos = (((float)((pos/10)))/100f);
        final float TweaksContainerAlph = 1f+alphPos;
        genderLayout.setAlpha(TweaksContainerAlph);

        final float TweaksLayoutAlph = alph-alphPos;
        ageLayout.setAlpha(0.6f);
        mainLaout2.setAlpha(0.4f-TweaksLayoutAlph);
    }
    private void restoreOnBottomsheetContinue_genderLayout(){
        final float newPos = 0;
        final float alph2 = 1f;
        genderLayout.animate().translationX(newPos).alpha(alph2).setInterpolator(new LinearOutSlowInInterpolator()).setDuration(mAnimationTime).start();

        final float trans = Utils.dpToPx(350);
        final float alph = 0.4f;
        mainLaout2.animate().translationX(NEG).alpha(alph).setDuration(mAnimationTime).start();
        ageLayout.animate().translationX(trans).alpha(alph).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ageLayout.setTranslationX(trans);
                        ageLayout.setAlpha(alph);

                        genderLayout.setTranslationX(newPos);
                        genderLayout.setAlpha(alph2);

                        mainLaout2.setTranslationX(NEG);
                        mainLaout2.setAlpha(alph);
                        mainLaout2.setVisibility(View.GONE);

                        genderLayout.setVisibility(View.VISIBLE);
                        ageLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }

    private void setGender(String gender){
        SharedPreferences pref = mContext.getSharedPreferences(Constants.GENDER, MODE_PRIVATE);
        pref.edit().clear().putString(Constants.GENDER, gender).apply();

        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mref = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(user).child(Constants.GENDER);
        mref.setValue(gender);

        setConsentToTargetPC(true);
    }




    private void loadSetBirthdayView() {
        isShowing_ageLayout = true;
        ageLayout.setVisibility(View.VISIBLE);
        ageLayout.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0).alpha(1f)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
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
        SharedPreferences pref = mContext.getSharedPreferences(Constants.DATE_OF_BIRTH, MODE_PRIVATE);
        if(pref.getInt("year",0)!=0) {
            int day = pref.getInt("day", 0);
            int month = pref.getInt("month", 0);
            int year = pref.getInt("year", 0);
            setAgeTextView.setVisibility(View.VISIBLE);
            setAgeTextView.setText(String.format("Set birthday date: %d %s,%d", day, getMonthName_Abbr(month), year));
        }

        findViewById(R.id.calendarExp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCalendar.performClick();
            }
        });

        openCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                android.app.DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");
                kindaCollapseAll();
                BroadcastReceiver mMessageReceiverForClosedCalendar = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        unKindaCollapseAll();
                    }
                };

                LocalBroadcastManager.getInstance(mContext).registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
                        unKindaCollapseAll();
                    }
                },new IntentFilter("cal"));
            }
        });

        backBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ageLayout.setVisibility(View.GONE);
                ageLayout.animate().translationX(Utils.dpToPx(350)).setDuration(Constants.ANIMATION_DURATION)
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
                loadSetGenderView();
                isShowing_ageLayout = false;
            }
        });

        skip2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ageLayout.setVisibility(View.GONE);
                ageLayout.animate().translationX(NEG).alpha(0f).setDuration(durat)
                        .setInterpolator(new LinearOutSlowInInterpolator())
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                ageLayout.setVisibility(View.GONE);
                                ageLayout.setAlpha(0f);
                                ageLayout.setTranslationX(NEG);
                                super.onAnimationEnd(animation);
                            }
                        });
                loadSetLocationsView();
                isShowing_ageLayout = false;
            }
        });
        okBtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                ageLayout.setVisibility(View.GONE);
                ageLayout.animate().translationX(NEG).alpha(0f).setDuration(durat)
                        .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        ageLayout.setTranslationX(NEG);
                        ageLayout.setVisibility(View.GONE);
                        ageLayout.setAlpha(1f);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
                loadSetLocationsView();
                isShowing_ageLayout = false;
            }
        });
        addTouchListenerForGoingNext_ageLayout();
    }

    private int x_bottomSheet_ageLayout;
    private boolean isContinueSwiping_ageLayout = false;
    private boolean isShowing_ageLayout = false;
    private GestureDetector continueGt_ageLayout;
    private void addTouchListenerForGoingNext_ageLayout(){
        continueGt_ageLayout = new GestureDetector(this, new MySwipeContinueGestureListener_ageLayout());
    }
    private void onTouch_ageLayout(MotionEvent event){
        continueGt_ageLayout.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (isContinueSwiping_ageLayout) {
                isContinueSwiping_ageLayout = false;
                restoreOnBottomsheetContinue_ageLayout();
            }
        }
    }
    class MySwipeContinueGestureListener_ageLayout extends GestureDetector.SimpleOnGestureListener {
        int origX = 0;
        int origY = 0;


        @Override
        public boolean onDown(MotionEvent event) {
//            Log.d(TAG, "onDown: ");
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) ageLayout.getLayoutParams();
            x_bottomSheet_ageLayout = X - lParams.leftMargin;

            origX = lParams.leftMargin;
            origY = lParams.topMargin;


            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            final int Y = (int) e2.getRawY();
            final int X = (int) e2.getRawX();

            Log.d(TAG, "the scroll pos= " + ((double)(X-x_bottomSheet_ageLayout)));

            animateContinueBottomsheetDialog_ageLayout((double)(X - x_bottomSheet_ageLayout));
            isContinueSwiping_ageLayout = true;

            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(velocityX) > Math.abs(velocityY) && Math.abs(velocityX)>1400 && Math.abs(velocityX)<8000) {
                if(velocityX<0) okBtn3.performClick();
                else backBtn1.performClick();
            }else{
                restoreOnBottomsheetContinue_ageLayout();
            }
            isContinueSwiping_ageLayout = false;
            return false;

        }
    }
    private void animateContinueBottomsheetDialog_ageLayout(double pos){
        double newPos = (pos/10);

        genderLayout.setVisibility(View.VISIBLE);
        ageLayout.setVisibility(View.VISIBLE);
        locationLayout.setVisibility(View.VISIBLE);

        genderLayout.setTranslationX(NEG+(float)newPos);
        ageLayout.setTranslationX(((float)newPos));
        locationLayout.setTranslationX(Utils.dpToPx(350)+(float)newPos);

        final float alph = 0f;
        float alphPos = (((float)((pos/10)))/100f);
        final float TweaksLayoutAlph = alph-alphPos;
        final float TweaksContainerAlph = 1f+alphPos;

        genderLayout.setAlpha(0.4f-TweaksLayoutAlph);
        ageLayout.setAlpha(TweaksContainerAlph);
        locationLayout.setAlpha(0.6f);
    }
    private void restoreOnBottomsheetContinue_ageLayout(){
        genderLayout.animate().translationX(NEG).alpha(0).setInterpolator(new LinearOutSlowInInterpolator()).setDuration(mAnimationTime).start();

        final float newPos = 0;
        final float alph2 = 1f;
        ageLayout.animate().translationX(newPos).alpha(alph2).setInterpolator(new LinearOutSlowInInterpolator()).setDuration(mAnimationTime).start();

        final float trans = Utils.dpToPx(350);
        final float alph = 0f;
        locationLayout.animate().translationX(trans).alpha(alph).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        genderLayout.setTranslationX(NEG);
                        genderLayout.setAlpha(0);

                        locationLayout.setTranslationX(trans);
                        locationLayout.setAlpha(alph);

                        ageLayout.setTranslationX(newPos);
                        ageLayout.setAlpha(alph2);

                        genderLayout.setVisibility(View.GONE);
                        ageLayout.setVisibility(View.VISIBLE);
                        locationLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }



    private void loadSetLocationsView() {
        isShowing_locationLayout = true;
        locationLayout.setVisibility(View.VISIBLE);
//        locationLayout.setTranslationX(Utils.dpToPx(400));
        locationLayout.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0).alpha(1f)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                locationLayout.setAlpha(1f);
                locationLayout.setTranslationX(0);
                locationLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        if(Variables.usersLatLongs.isEmpty()){
            locationNumberText.setText(Html.fromHtml("Locations set:<b> None.</b>"));
        }else{
            locationNumberText.setText(Html.fromHtml("Locations set: <b>"+ Variables.usersLatLongs.size()+" Locations.</b>"));
        }
        findViewById(R.id.locationExp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMapImg.performClick();
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

        backBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isShowing_locationLayout = false;
//                locationLayout.setVisibility(View.GONE);
                locationLayout.animate().translationX(Utils.dpToPx(350)).setDuration(durat)
                        .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        locationLayout.setVisibility(View.GONE);
                        locationLayout.setAlpha(1f);
                        locationLayout.setTranslationX(NEG);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
                loadSetBirthdayView();
            }
        });

        skip3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isShowing_locationLayout = false;
//                locationLayout.setVisibility(View.GONE);
                locationLayout.animate().translationX(NEG).alpha(0f).setDuration(durat)
                        .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        locationLayout.setAlpha(0f);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                loadFifthView();
            }
        });
        okBtn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isShowing_locationLayout = false;
//                locationLayout.setVisibility(View.GONE);
                locationLayout.animate().translationX(NEG).alpha(0f).setDuration(durat)
                        .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        locationLayout.setVisibility(View.GONE);
                        locationLayout.setTranslationX(NEG);
                        locationLayout.setAlpha(1f);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                loadFifthView();
            }
        });
        addTouchListenerForGoingNext_locationLayout();
    }

    private int x_bottomSheet_locationLayout;
    private boolean isContinueSwiping_locationLayout = false;
    private boolean isShowing_locationLayout = false;
    private GestureDetector continueGt_locationLayout;
    private void addTouchListenerForGoingNext_locationLayout(){
        continueGt_locationLayout = new GestureDetector(this, new MySwipeContinueGestureListener_locationLayout());
    }
    private void onTouch_locationLayout(MotionEvent event){
        continueGt_locationLayout.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (isContinueSwiping_locationLayout) {
                isContinueSwiping_locationLayout = false;
                restoreOnBottomsheetContinue_locationLayout();
            }
        }
    }
    class MySwipeContinueGestureListener_locationLayout extends GestureDetector.SimpleOnGestureListener {
        int origX = 0;
        int origY = 0;


        @Override
        public boolean onDown(MotionEvent event) {
//            Log.d(TAG, "onDown: ");
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) locationLayout.getLayoutParams();
            x_bottomSheet_locationLayout = X - lParams.leftMargin;

            origX = lParams.leftMargin;
            origY = lParams.topMargin;


            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            final int Y = (int) e2.getRawY();
            final int X = (int) e2.getRawX();

            Log.d(TAG, "the scroll pos= " + ((double)(X-x_bottomSheet_locationLayout)));

            animateContinueBottomsheetDialog_locationLayout((double)(X - x_bottomSheet_locationLayout));
            isContinueSwiping_locationLayout = true;

            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(velocityX) > Math.abs(velocityY) && Math.abs(velocityX)>1400 && Math.abs(velocityX)<8000) {
                if(velocityX<0) skip3.performClick();
                else backBtn2.performClick();
            }else{
                restoreOnBottomsheetContinue_locationLayout();
            }
            isContinueSwiping_locationLayout = false;
            return false;

        }
    }
    private void animateContinueBottomsheetDialog_locationLayout(double pos){
        double newPos = (pos/10);

        ageLayout.setVisibility(View.VISIBLE);
        locationLayout.setVisibility(View.VISIBLE);
        concludeLayout.setVisibility(View.VISIBLE);

        ageLayout.setTranslationX(NEG+(float)newPos);
        locationLayout.setTranslationX(((float)newPos));
        concludeLayout.setTranslationX(Utils.dpToPx(350)+(float)newPos);

        final float alph = 0f;
        float alphPos = (((float)((pos/10)))/100f);
        final float TweaksLayoutAlph = alph-alphPos;
        final float TweaksContainerAlph = 1f+alphPos;

        ageLayout.setAlpha(0.4f-TweaksLayoutAlph);
        locationLayout.setAlpha(TweaksContainerAlph);
        concludeLayout.setAlpha(0.6f);
    }
    private void restoreOnBottomsheetContinue_locationLayout(){
        ageLayout.animate().translationX(NEG).alpha(0.4f).setInterpolator(new LinearOutSlowInInterpolator()).setDuration(mAnimationTime).start();

        final float newPos = 0;
        final float alph2 = 1f;
        locationLayout.animate().translationX(newPos).alpha(alph2).setInterpolator(new LinearOutSlowInInterpolator()).setDuration(mAnimationTime).start();

        final float trans = Utils.dpToPx(350);
        final float alph = 0.4f;
        concludeLayout.animate().translationX(trans).alpha(alph).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ageLayout.setTranslationX(NEG);
                        ageLayout.setAlpha(0);

                        locationLayout.setTranslationX(newPos);
                        locationLayout.setAlpha(alph2);

                        concludeLayout.setTranslationX(trans);
                        concludeLayout.setAlpha(alph);

                        ageLayout.setVisibility(View.GONE);
                        locationLayout.setVisibility(View.VISIBLE);
                        concludeLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }

    private BroadcastReceiver mMessageReceiverForSetLocations = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(Variables.usersLatLongs.isEmpty()){
                locationNumberText.setText(Html.fromHtml("Locations set:<b> None.</b>"));
            }else{
                locationNumberText.setText(Html.fromHtml("Locations set: <b>"+ Variables.usersLatLongs.size()+" Locations.</b>"));
            }
            unKindaCollapseLocationAll();
        }
    };

    private void openMapSelector() {
        kindaCollapseLocationAll();
        FragmentManager fm = getFragmentManager();
        myMapFragment mapFragment = new myMapFragment();
        mapFragment.setMenuVisibility(false);
        mapFragment.show(fm,"Edit User Location.");
        mapFragment.setfragcontext(mContext);
        mapFragment.setActivity(Dashboard.this);
//        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("SHOW_MAP"));
    }

    private void loadFifthView() {
        isShowing_concludeLayout = true;
        concludeLayout.setVisibility(View.VISIBLE);
        concludeLayout.animate().setDuration(Constants.ANIMATION_DURATION).translationX(0).alpha(1f)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                concludeLayout.setTranslationX(0);
                concludeLayout.setAlpha(1f);
                super.onAnimationEnd(animation);
            }
        });
        okBtn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideUserDataDialog();
                isShowing_concludeLayout = false;
            }
        });
        addTouchListenerForGoingNext_concludeLayout();
    }

    private int x_bottomSheet_concludeLayout;
    private boolean isContinueSwiping_concludeLayout = false;
    private boolean isShowing_concludeLayout = false;
    private GestureDetector continueGt_concludeLayout;
    private void addTouchListenerForGoingNext_concludeLayout(){
        continueGt_concludeLayout = new GestureDetector(this, new MySwipeContinueGestureListener_concludeLayout());
    }
    private void onTouch_concludeLayout(MotionEvent event){
        continueGt_concludeLayout.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (isContinueSwiping_concludeLayout) {
                isContinueSwiping_concludeLayout = false;
                restoreOnBottomsheetContinue_concludeLayout();
            }
        }
    }
    class MySwipeContinueGestureListener_concludeLayout extends GestureDetector.SimpleOnGestureListener {
        int origX = 0;
        int origY = 0;


        @Override
        public boolean onDown(MotionEvent event) {
//            Log.d(TAG, "onDown: ");
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) concludeLayout.getLayoutParams();
            x_bottomSheet_concludeLayout = X - lParams.leftMargin;

            origX = lParams.leftMargin;
            origY = lParams.topMargin;


            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            final int Y = (int) e2.getRawY();
            final int X = (int) e2.getRawX();

            Log.d(TAG, "the scroll pos= " + ((double)(X-x_bottomSheet_concludeLayout)));

            animateContinueBottomsheetDialog_concludeLayout((double)(X - x_bottomSheet_concludeLayout));
            isContinueSwiping_concludeLayout = true;

            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            restoreOnBottomsheetContinue_concludeLayout();
            if (Math.abs(velocityX) > Math.abs(velocityY) && Math.abs(velocityX)>1400 && Math.abs(velocityX)<8000) {
//                continueBtnCPV.performClick();
            }else{
                restoreOnBottomsheetContinue_concludeLayout();
            }
            isContinueSwiping_concludeLayout = false;
            return false;

        }
    }
    private void animateContinueBottomsheetDialog_concludeLayout(double pos){
        double newPos = (pos/10);
        locationLayout.setVisibility(View.VISIBLE);
        concludeLayout.setVisibility(View.VISIBLE);

        locationLayout.setTranslationX(NEG+((float)newPos));
        concludeLayout.setTranslationX((float)newPos);

        final float alph = 0f;
        float alphPos = (((float)((pos/10)))/100f);
        final float TweaksContainerAlph = 1f+alphPos;
//        chooseAmountLayout.setAlpha(TweaksContainerAlph);

        final float TweaksLayoutAlph = alph-alphPos;
//        changingCPVMainLayout.setAlpha(TweaksLayoutAlph);
        locationLayout.setAlpha(0.4f-TweaksLayoutAlph);
    }
    private void restoreOnBottomsheetContinue_concludeLayout(){
        final float newPos = 0;
        final float alph2 = 1f;
        concludeLayout.animate().translationX(newPos).alpha(alph2).setInterpolator(new LinearOutSlowInInterpolator()).setDuration(mAnimationTime).start();

        final float trans = NEG;
        final float alph = 0.4f;
        locationLayout.animate().translationX(trans).alpha(alph).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        locationLayout.setTranslationX(trans);
                        locationLayout.setAlpha(alph);

                        concludeLayout.setTranslationX(newPos);
                        concludeLayout.setAlpha(alph2);
                        concludeLayout.setVisibility(View.VISIBLE);

                        locationLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }


    private void setConsentToTargetPC(Boolean bol){
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


    public static class DatePickerFragment extends android.app.DialogFragment implements DatePickerDialog.OnDateSetListener {
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

        @Override
        public void onDestroy(){
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("cal"));
            super.onDestroy();
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
//            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("cal"));
        }

    }

    public void kindaCollapseAll(){
        final float trans = Utils.dpToPx(300);
        TweaksLayout.animate().translationY(trans).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        TweaksLayout.setTranslationY(trans);
                        TweaksLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        final float trans2 = Utils.dpToPx(230);
        ageLayout.animate().translationY(trans2).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ageLayout.setTranslationY(trans2);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }


    public void unKindaCollapseAll(){
        final float trans = Utils.dpToPx(230);
        TweaksLayout.animate().translationY(trans).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        TweaksLayout.setTranslationY(trans);
                        TweaksLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        final float trans2 = 0;
        ageLayout.animate().translationY(trans2).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ageLayout.setTranslationY(trans2);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }

    public void kindaCollapseLocationAll(){
        final float trans = Utils.dpToPx(300);
        TweaksLayout.animate().translationY(trans).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        TweaksLayout.setTranslationY(trans);
                        TweaksLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        final float trans2 = Utils.dpToPx(230);
        locationLayout.animate().translationY(trans2).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        locationLayout.setTranslationY(trans2);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }

    public void unKindaCollapseLocationAll(){
        final float trans = Utils.dpToPx(230);
        TweaksLayout.animate().translationY(trans).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        TweaksLayout.setTranslationY(trans);
                        TweaksLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        final float trans2 = 0;
        locationLayout.animate().translationY(trans2).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        locationLayout.setTranslationY(trans2);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }

    public void kindaCollapseNotifAll(){
        final LinearLayout mainLayout = findViewById(R.id.NotificationsLayout);
        final float trans = Utils.dpToPx(300);
        TweaksLayout.animate().translationY(trans).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        TweaksLayout.setTranslationY(trans);
                        TweaksLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        final float trans2 = Utils.dpToPx(200);
        mainLayout.animate().translationY(trans2).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mainLayout.setTranslationY(trans2);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }

    public void unKindaCollapseNotifAll(){
        final LinearLayout mainLayout = findViewById(R.id.NotificationsLayout);
        final float trans = Utils.dpToPx(230);
        TweaksLayout.animate().translationY(trans).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        TweaksLayout.setTranslationY(trans);
                        TweaksLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        final float trans2 = 0;
        mainLayout.animate().translationY(trans2).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mainLayout.setTranslationY(trans2);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }



    private void hideUserDataDialog(){
        isLoadedUserDataDialog = false;
        final RelativeLayout personalisedContentLayout = findViewById(R.id.personalisedContentLayout);
        personalisedContentLayout.animate().alpha(0f).translationY(-Utils.dpToPx(250)).setInterpolator(new LinearOutSlowInInterpolator()).setDuration(mAnimationTime)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        personalisedContentLayout.setTranslationY(-Utils.dpToPx(250));
                        personalisedContentLayout.setAlpha(0f);
                        personalisedContentLayout.setVisibility(View.GONE);
                        resetViews();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForSetLocations);

        unKindaCollapseSettings();
        removeTouchListenerForSwipeUpToGoBack();
    }

    private void resetViews(){
        concludeLayout.setVisibility(View.GONE);
        concludeLayout.setAlpha(1f);
        isShowing_concludeLayout = false;
        isContinueSwiping_concludeLayout = false;
        concludeLayout.setTranslationX(Utils.dpToPx(350));

        locationLayout.setVisibility(View.GONE);
        locationLayout.setAlpha(1f);
        isShowing_locationLayout = false;
        isContinueSwiping_locationLayout = false;
        locationLayout.setTranslationX(Utils.dpToPx(350));

        ageLayout.setVisibility(View.GONE);
        ageLayout.setAlpha(1f);
        isShowing_ageLayout = false;
        isContinueSwiping_ageLayout = false;
        ageLayout.setTranslationX(Utils.dpToPx(350));

        genderLayout.setVisibility(View.GONE);
        genderLayout.setAlpha(1f);
        isShowing_genderLayout = false;
        isContinueSwiping_genderLayout = false;
        genderLayout.setTranslationX(Utils.dpToPx(350));

        mainLaout2.setVisibility(View.GONE);
        mainLaout2.setAlpha(1f);
        isShowing_mainLaout2 = false;
        isContinueSwiping_mainLaout2 = false;
        mainLaout2.setTranslationX(Utils.dpToPx(350));

        mainLayout.setVisibility(View.VISIBLE);
        mainLayout.setAlpha(1f);
        isShowing_mainLayout = false;
        isContinueSwiping_mainLayout = false;
        mainLayout.setTranslationX(0);
    }





    private BroadcastReceiver mMessageReceiverForShowMap = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log("Dashboard", "Broadcast has been received show map.");
            loadMap();
        }
    };

    private void loadMap(){
        FragmentManager fm = getFragmentManager();
        myMapFragment mapFragment = new myMapFragment();
        mapFragment.setMenuVisibility(false);
        mapFragment.show(fm,"Edit User Data.");
        mapFragment.setfragcontext(mContext);
        mapFragment.setActivity(thisActivity);
    }

    private void promptUserToStopTargeting2(){
        final Dialog d = new Dialog(this);
        d.setTitle("Targeting.");
        d.setContentView(R.layout.targeted_dialog);
        Button setButton = d.findViewById(R.id.stopBtn);
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        TextView targetingPermissionText = d.findViewById(R.id.targetingPermissionText);
        TextView targetNoteLess = d.findViewById(R.id.targetNoteLess);
        SharedPreferences pref = mContext.getSharedPreferences(Constants.CONSENT_TO_TARGET, MODE_PRIVATE);
        final boolean canUseData = pref.getBoolean(Constants.CONSENT_TO_TARGET,false);
        if(!canUseData){
            setButton.setText("START.");
            targetingPermissionText.setText(R.string.targetingPermissonOn);
            targetNoteLess.setText("This will result in more content.");
        }
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setConsentToTarget(!canUseData);
                d.dismiss();
            }
        });
        d.show();
    }

    private boolean isPromptingUserToStopTargeting = false;
    private void promptUserToStopTargeting(){
        isPromptingUserToStopTargeting = true;
        addTouchListenerForSwipeUpToGoBack();
        final LinearLayout targetingLayout = findViewById(R.id.targetingLayout);
        targetingLayout.setVisibility(View.VISIBLE);
        targetingLayout.animate().translationY(0).alpha(1).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        targetingLayout.setTranslationY(0);
                        targetingLayout.setAlpha(1f);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        Button setButton = findViewById(R.id.stopBtn);
        TextView targetingPermissionText = findViewById(R.id.targetingPermissionText);
        TextView targetNoteLess = findViewById(R.id.targetNoteLess);
        SharedPreferences pref = mContext.getSharedPreferences(Constants.CONSENT_TO_TARGET, MODE_PRIVATE);
        final boolean canUseData = pref.getBoolean(Constants.CONSENT_TO_TARGET,false);
        if(!canUseData){
            setButton.setText("START.");
            targetingPermissionText.setText(R.string.targetingPermissonOn);
            targetNoteLess.setText("This will result in more content.");
        }
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setConsentToTarget(!canUseData);
                hidePromptForStopTargeting();
            }
        });
    }

    private void hidePromptForStopTargeting(){
        isPromptingUserToStopTargeting = false;
        removeTouchListenerForSwipeUpToGoBack();
        final LinearLayout targetingLayout = findViewById(R.id.targetingLayout);
        final int trans = -Utils.dpToPx(250);
        targetingLayout.animate().translationY(trans).alpha(0).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        targetingLayout.setTranslationY(trans);
                        targetingLayout.setAlpha(0f);
                        targetingLayout.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
        unKindaCollapseSettings();
    }



    private void setConsentToTarget(Boolean bol){
        SharedPreferences pref = mContext.getSharedPreferences(Constants.CONSENT_TO_TARGET, MODE_PRIVATE);
        pref.edit().clear().putBoolean(Constants.CONSENT_TO_TARGET, bol).apply();

        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mref = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(user).child(Constants.CONSENT_TO_TARGET);
        mref.setValue(bol);
        Toast.makeText(mContext,"Your preference has been set",Toast.LENGTH_SHORT).show();
        if(bol)findViewById(R.id.dotForTargeted).setVisibility(View.VISIBLE);
        else findViewById(R.id.dotForTargeted).setVisibility(View.INVISIBLE);
    }



    ChildEventListener chil = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            if(dataSnapshot.getKey().equals(Constants.IS_MAKING_PAYOUT)){
                isMakingPayout = dataSnapshot.getValue(boolean.class);
                CardView payoutCard = findViewById(R.id.payoutCard);
                if(isMakingPayout)payoutCard.setAlpha(0.4f);
                else payoutCard.setAlpha(1.0f);

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

    private void addListenerForPaymentSession(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbListener = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS).child(uid);
        dbListener.addChildEventListener(chil);
    }

    private void removeLisenerForPaymentSession(){
        if(dbListener!=null) dbListener.removeEventListener(chil);
    }



    ChildEventListener chil2 = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            if(dataSnapshot.getKey().equals(Constants.REIMBURSEMENT_TOTALS)){
                int reimbursementAmount = dataSnapshot.getValue(int.class);
                if(reimbursementAmount==0) {
                    Variables.setTotalReimbursementAmount(reimbursementAmount);

                    SharedPreferences pref7 = getApplicationContext().getSharedPreferences("ReimbursementTotals", MODE_PRIVATE);
                    SharedPreferences.Editor editor7 = pref7.edit();
                    editor7.clear();
                    editor7.putInt(Constants.REIMBURSEMENT_TOTALS, Variables.getTotalReimbursementAmount());
                    Log("Dashboard", "Setting the Reimbursement totals in shared " +
                            "preferences - " + Integer.toString(Variables.getTotalReimbursementAmount()));
                    editor7.apply();
                    setValues();
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

    private void addListenerForChangeInPayoutTotals(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbListenerForMoney = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_CHILD_USERS)
                .child(uid);
        dbListenerForMoney.addChildEventListener(chil2);
    }

    private void removeListenerForChangeInPayoutTotals(){
        if(dbListenerForMoney!=null) dbListenerForMoney.removeEventListener(chil2);
    }




    ChildEventListener chil3 = new ChildEventListener() {
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
        SKListener.addChildEventListener(chil3);
    }

    public void removeListenerForChangeInSessionKey(){
        if(SKListener!=null){
            SKListener.removeEventListener(chil3);
        }
    }

    public String getSessionKey(){
        SharedPreferences prefs2 = getSharedPreferences(Constants.BOI_IS_DA_KEY, MODE_PRIVATE);
        String sk = prefs2.getString(Constants.BOI_IS_DA_KEY, "NULL");
        Log.d("Dashboard", "Loading session key from shared prefs - " + sk);
        return sk;
    }



    public void PerformShutdown(){
        if (FirebaseAuth.getInstance() != null) {
            FirebaseAuth.getInstance().signOut();
        }
        Variables.resetAllValues();
        if(!isWindowPaused){
            Intent intent = new Intent(Dashboard.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else{
            isNeedToLoadLogin = true;
        }

    }


    private void loadUserDataFromSharedPrefs() {
        loadSubsFromSharedPrefs();
        loadMarkersInSharedPrefs();
        Log(TAG, "Loading user data from shared preferences first...");
        SharedPreferences prefs = getSharedPreferences("TodayTotals", MODE_PRIVATE);
        int number = prefs.getInt("TodaysTotals", 0);
        Log(TAG, "AD TOTAL NUMBER GOTTEN FROM SHARED PREFERENCES IS - " + number);
        Variables.setAdTotal(number, mKey);

        SharedPreferences prefs2 = getSharedPreferences("MonthTotals", MODE_PRIVATE);
        int number2 = prefs2.getInt("MonthsTotals", 0);
        Log(TAG, "MONTH AD TOTAL NUMBER GOTTEN FROM SHARED PREFERENCES IS - " + number2);
        Variables.setMonthAdTotals(mKey, number2);

        SharedPreferences prefs7 = getSharedPreferences("ReimbursementTotals", MODE_PRIVATE);
        int number7 = prefs7.getInt(Constants.REIMBURSEMENT_TOTALS, 0);
        Log(TAG, "REIMBURSEMENT TOTAL NUMBER GOTTEN FROM SHARED PREFERENCES IS - " + number7);
        Variables.setTotalReimbursementAmount(number7);

        SharedPreferences prefs8 = getSharedPreferences(Constants.CONSTANT_AMMOUNT_PER_VIEW,MODE_PRIVATE);
        int number8 = prefs8.getInt(Constants.CONSTANT_AMMOUNT_PER_VIEW,3);
        Log(TAG,"CONSTANT AMOUNT GOTTEN PER AD FROM SHARED PREFERENCES IS -   "+number8);
        Variables.constantAmountPerView = number8;

        SharedPreferences pref9 = getSharedPreferences(Constants.PREFERRED_NOTF_HOUR,MODE_PRIVATE);
        Variables.preferredHourOfNotf = pref9.getInt(Constants.PREFERRED_NOTF_HOUR,7);

        SharedPreferences pref10 = getSharedPreferences(Constants.PREFERRED_NOTF_MIN,MODE_PRIVATE);
        Variables.preferredMinuteOfNotf = pref10.getInt(Constants.PREFERRED_NOTF_MIN,30);

        SharedPreferences prefs4 = getSharedPreferences("UID", MODE_PRIVATE);
        String uid = prefs4.getString("Uid", "");
        Log(TAG, "UID NUMBER GOTTEN FROM SHARED PREFERENCES IS - " + uid);
        User.setUid(uid);

        SharedPreferences prefs5 = getSharedPreferences("CurrentSubIndex", MODE_PRIVATE);
        int currentSubIndex = prefs5.getInt("CurrentSubIndex",0);
        Log(TAG, "CURRENT SUBSCRIPTION INDEX NUMBER GOTTEN FROM SHARED PREFERENCES IS - " + currentSubIndex);
        Variables.setCurrentSubscriptionIndex(currentSubIndex);

        SharedPreferences prefs6 = getSharedPreferences("CurrentAdInSubscription", MODE_PRIVATE);
        int currentAdInSubscription = prefs6.getInt("CurrentAdInSubscription",0);
        Log(TAG,"CURRENT AD IN SUBSCRIPTION GOTTEN FROM SHARED PREFERENCES IS : "+currentAdInSubscription);
        Variables.setCurrentAdInSubscription(currentAdInSubscription);

        loadSeenAdsInSharedPrefs();
    }

    private void loadSeenAdsInSharedPrefs(){
        if(!Variables.adsSeenSoFar.isEmpty())Variables.adsSeenSoFar.clear();
        Gson gson = new Gson();
        SharedPreferences prefs = getSharedPreferences("AdsSeenSoFar", MODE_PRIVATE);
        String storedHashMapString = prefs.getString("AdsSeenSoFarHashString", "nil");

        java.lang.reflect.Type type = new TypeToken<LinkedHashMap<String,String>>(){}.getType();
        Variables.adsSeenSoFar = gson.fromJson(storedHashMapString, type);
    }

    private void loadMarkersInSharedPrefs(){
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.USER_MARKERS,MODE_PRIVATE);
        Variables.usersLatLongs.clear();
        int size = sharedPreferences.getInt(Constants.USER_MARKERS_SIZE, 0);
        for(int i = 0; i < size; i++){
            double lat = (double) sharedPreferences.getFloat("lat"+i,0);
            double longit = (double) sharedPreferences.getFloat("long"+i,0);
            LatLng latLng = new LatLng(lat,longit);
            Variables.usersLatLongs.add(latLng);
        }
    }

    private void loadSubsFromSharedPrefs() {
        if(!Variables.Subscriptions.isEmpty())Variables.Subscriptions.clear();
        Gson gson = new Gson();
        SharedPreferences prefs = getSharedPreferences("Subscriptions", MODE_PRIVATE);
        String storedHashMapString = prefs.getString("hashString", "nil");

        java.lang.reflect.Type type = new TypeToken<LinkedHashMap<String,Integer>>(){}.getType();
        Variables.Subscriptions = gson.fromJson(storedHashMapString, type);
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
        if(requestCode==BOI){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();
            }
        }
        if(requestCode==BOI2){
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                showFeedChatView();
            }
        }
    }

    public void fastCollapseTheFeedChatView(){
        mFeedChatView.setVisibility(View.VISIBLE);
        isCardCollapsed = true;
        updateUpPosition(collapsedMargin,0);
    }

    public void showFeedChatView(){
        if(checkPermissionForLoadMessages()){
            isCardCollapsed = false;
            mScrollView.setScrollingEnabled(false);
            updateUpPosition(unCollapsedMargin,normalDuration);
            loadMessages();
            setFeedChatClickListeners();
        }
    }

    private void setFeedChatClickListeners(){
        mAddImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        mCollapseFeedChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideFeedChatView();
            }
        });

        mCollapseFeedChatButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                if(uid.equals("bryonyoni@gmail.com")){
                    mCollapseFeedChatButton.performClick();
                    SharedPreferences prefs = mContext.getSharedPreferences(Constants.FIREBASE_MESSAGES, MODE_PRIVATE);
                    prefs.edit().clear().apply();
                }

                try{

                }catch (Exception e){
                    e.printStackTrace();
                }
                return false;
            }
        });
    }

    private void loadMessages() {
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(Dashboard.this);
        linearLayoutManager.setReverseLayout(true);
        mChatsPlaceHolderView.setLayoutManager(linearLayoutManager);

        List<Message> myMessages = loadSavedMessages();
        addFirstMessageIfEmpty();
        for(Message message:myMessages){
            addMessageToList(message,false);
        }
        mChatsPlaceHolderView.setItemViewCacheSize(myMessages.size());
        removeNewMessageBubble();
    }

    private void sendMessage() {
        String messageText = mFeedChatEditText.getText().toString().trim();
        if(messageText.equals("")){
            mFeedChatEditText.setError("You can't send nothing!");
        }else if(!isOnline(mContext)){
            Toast.makeText(mContext,"You need an internet connection to send that.",Toast.LENGTH_SHORT).show();
        }else{
            mFeedChatEditText.setText("");
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            final Message message = new Message();
            message.setMessageString(messageText);
            message.setMessageType(Constants.TEXT_MESSAGE);
            message.setSenderId(uid);
            message.setIsUsersMessage(true);
            message.setHasBeenSent(false);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    addMessageToList(message,true);
                }
            }, 400);

        }
    }






    private void selectImage() {
        if(checkPermissionsForSendImage()){
            Log.d(TAG,"Starting intent for picking an image.");
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Log.d(TAG, "---Data gotten from activity is ok.");
            if (data.getData() != null) {
                mFilepath = data.getData();
                mPath = mFilepath.getPath();
                Log.e(TAG,"mPath - "+mPath);
                Log.e(TAG,"mFilePath - "+mFilepath.toString());
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mFilepath);
                    imageBitmap = getResizedBitmap(bitmap,1000);
                    sendImageMessage(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "---Unable to get and set image. " + e.getMessage());
                }
            } else {
                Log.e(TAG, "---Unable to work on the result code for some reason.");
                Toast.makeText(mContext,"the data.getData method returns null for some reason...", Toast.LENGTH_SHORT).show();
            }
        }

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

    private void sendImageMessage(Bitmap image){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final Message message = new Message();
        message.setMessageType(Constants.IMAGE_MESSAGE);
        message.setSenderId(uid);
        message.setImageBitmap(image);
        message.setMessageUri(mPath);
        message.setIsUsersMessage(true);
        message.setHasBeenSent(false);

        addMessageToList(message,false);
    }

    private boolean checkPermissionsForSendImage(){
        if (Build.VERSION.SDK_INT >= 23) {
            if (mContext.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(Dashboard.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, BOI);
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }

        return false;
    }

    private boolean checkPermissionForLoadMessages(){
        if (Build.VERSION.SDK_INT >= 23) {
            if (mContext.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(Dashboard.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, BOI2);
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }

        return false;
    }




    private void addMessageToList(Message message,boolean isNewMessage){
        mChatsPlaceHolderView.addView(0,new MessageItem(mContext,mChatsPlaceHolderView,message,isNewMessage));
        mChatsPlaceHolderView.scrollToPosition(0);
        numberOfItemsAdded++;
    }

    private void addFirstMessageIfEmpty(){
        if(loadSavedMessages().isEmpty()){
            mChatsPlaceHolderView.addView(0,new FirstMessageItem(mContext,mChatsPlaceHolderView));
            mChatsPlaceHolderView.scrollToPosition(0);
        }
    }

    private void hideFeedChatView(){
        isCardCollapsed = true;
        numberOfItemsAdded = 0;
        mChatsPlaceHolderView.removeAllViews();
        mScrollView.setScrollingEnabled(true);
        updateUpPosition(collapsedMargin,normalDuration);
    }

    private void updateUpPosition(int y_pos,int duration){
        mChatsPlaceHolderView.setVisibility(View.INVISIBLE);
        findViewById(R.id.theBottomView).setVisibility(View.INVISIBLE);


        if(!isInTransition){
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mFeedChatView.getLayoutParams();
            layoutParams.bottomMargin = -(unCollapsedMargin);
            layoutParams.topMargin = (unCollapsedMargin);
            mFeedChatView.setLayoutParams(layoutParams);
            mFeedChatView.setTranslationY(200);
            isInTransition = true;
        }


        Log.d("TAG","updateUpPosition y_pos: "+y_pos);
        if(y_pos==collapsedMargin){
            mFeedChatView.setRotationY(0);
            mFeedChatView.animate().setDuration(300).rotationY(4).setInterpolator(new LinearOutSlowInInterpolator())
                    .setListener(new Animator.AnimatorListener() {
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
        }else{
            mFeedChatView.setRotationY(4);
            mFeedChatView.animate().setDuration(300).rotationY(0).setInterpolator(new LinearOutSlowInInterpolator());
        }

        mFeedChatView.animate().setDuration(duration).translationY(y_pos)
                .setInterpolator(new LinearOutSlowInInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if(mFeedChatView.getTranslationY()==collapsedMargin){
                    CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mFeedChatView.getLayoutParams();
                    layoutParams.bottomMargin = -collapsedMargin;
                    layoutParams.topMargin = collapsedMargin;
                    mFeedChatView.setLayoutParams(layoutParams);
                    mFeedChatView.setTranslationY(unCollapsedMargin);
                    isInTransition = false;
                }
                mChatsPlaceHolderView.setVisibility(View.VISIBLE);
                findViewById(R.id.theBottomView).setVisibility(View.VISIBLE);


                mChatsPlaceHolderView.setTranslationY(50);
                mChatsPlaceHolderView.animate().setDuration(100).translationY(0).setInterpolator(new LinearOutSlowInInterpolator());

                findViewById(R.id.theBottomView).setTranslationY(50);
                findViewById(R.id.theBottomView).animate().setDuration(100).translationY(0).setInterpolator(new LinearOutSlowInInterpolator());

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        int rounded = (((y_pos/1100) + 99) / 100 );
        Log.d("TAG","rounded: "+rounded);

    }

    private List<Message> loadSavedMessages(){
        List<Message> myLocalMessages = new ArrayList<>();

        Gson gson = new Gson();
        SharedPreferences prefs = mContext.getSharedPreferences(Constants.FIREBASE_MESSAGES, MODE_PRIVATE);
        String storedHashMapString = prefs.getString(Constants.FIREBASE_MESSAGES, "nil");

        if(!storedHashMapString.equals("nil")) {
            java.lang.reflect.Type type = new TypeToken<List<Message>>() {}.getType();
            myLocalMessages = gson.fromJson(storedHashMapString, type);
        }

        Log.d(TAG,"Loaded "+myLocalMessages.size()+" stored messages from shared preferences");
        return myLocalMessages;
    }

    BroadcastReceiver mMessageReceiverForListeningForNewMessages = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!isCardCollapsed){
                mChatsPlaceHolderView.removeAllViews();
                loadMessages();
                numberOfNewMessages++;
            }else{
                setNewMessageBubble();
            }
        }
    };

    private void setNewMessageBubble() {
        findViewById(R.id.newMessagesView).setVisibility(View.VISIBLE);
    }

    private void removeNewMessageBubble(){
        findViewById(R.id.newMessagesView).setVisibility(View.INVISIBLE);
    }


    private int getTotalValueFromEachCoin(List<AdCoin> legitCoinsList){
        int amount = 0;
        for(AdCoin coin: legitCoinsList){
            amount+=coin.getValue();
        }
        return amount;
    }




    private void addTouchListenerForSwipeBack() {
        mSwipeBackDetector = new GestureDetector(this, new MySwipebackGestureListener());
        swipeBackView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (mSwipeBackDetector.onTouchEvent(motionEvent)) {
                    return true;
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (isSideScrolling) {
                        Log.d("touchListener", " onTouch ACTION_UP");
                        isSideScrolling = false;

                        int RightScore = 0;
                        int LeftScore = 0;
                        if (SideSwipeRawList.size() > 15) {
                            for (int i = SideSwipeRawList.size() - 1; i > SideSwipeRawList.size() - 15; i--) {
                                int num1 = SideSwipeRawList.get(i);
                                int numBefore1 = SideSwipeRawList.get(i - 1);

                                if (numBefore1 > num1) LeftScore++;
                                else RightScore++;
                            }
                        } else {
                            for (int i = SideSwipeRawList.size() - 1; i > 0; i--) {
                                int num1 = SideSwipeRawList.get(i);
                                int numBefore1 = SideSwipeRawList.get(i - 1);

                                if (numBefore1 > num1) LeftScore++;
                                else RightScore++;
                            }
                        }
                        if (RightScore > LeftScore) {
//                        onBackPressed();
                        }
                        hideNupdateSideSwipeThing();
                        SideSwipeRawList.clear();
                    }
                    ;
                }

                return false;
            }
        });
    }


    class MySwipebackGestureListener extends GestureDetector.SimpleOnGestureListener {
        int origX = 0;
        int origY = 0;


        @Override
        public boolean onDown(MotionEvent event) {
            Log.d("TAG", "onDown: ");
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            Log.d("TAG", "onDown: event.getRawX(): " + event.getRawX() + " event.getRawY()" + event.getRawY());
            CoordinatorLayout.LayoutParams lParams = (CoordinatorLayout.LayoutParams) swipeBackView.getLayoutParams();
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
            if ((X - x_delta) < maxSideSwipeLength) {

            } else if ((X - x_delta) > maxSideSwipeLength) {

            } else {
            }
            showNupdateSideSwipeThing(X - x_delta);

            Log.d("TAG", "the e2.getAction()= " + e2.getAction() + " and the MotionEvent.ACTION_CANCEL= " + MotionEvent.ACTION_CANCEL);
            SideSwipeRawList.add(X);

            isSideScrolling = true;
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            int RightScore = 0;
            int LeftScore = 0;
            if (SideSwipeRawList.size() > 15) {
                for (int i = SideSwipeRawList.size() - 1; i > SideSwipeRawList.size() - 15; i--) {
                    int num1 = SideSwipeRawList.get(i);
                    int numBefore1 = SideSwipeRawList.get(i - 1);

                    if (numBefore1 > num1) LeftScore++;
                    else RightScore++;
                }
            } else {
                for (int i = SideSwipeRawList.size() - 1; i > 0; i--) {
                    int num1 = SideSwipeRawList.get(i);
                    int numBefore1 = SideSwipeRawList.get(i - 1);

                    if (numBefore1 > num1) LeftScore++;
                    else RightScore++;
                }
            }
            if (RightScore > LeftScore) {
                onBackPressed();
            }
            SideSwipeRawList.clear();
            return false;

        }
    }

    private void showNupdateSideSwipeThing(int pos) {
        int trans = (int) ((pos - Utils.dpToPx(10)) * 0.9);
        RelativeLayout isGoingBackIndicator = findViewById(R.id.isGoingBackIndicator);
//        isGoingBackIndicator.setTranslationX(trans);

        View v = findViewById(R.id.swipeBackViewIndicator);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) v.getLayoutParams();
        params.height = trans;
        v.setLayoutParams(params);

        LockableScrollView scrollView = findViewById(R.id.ScrollView);
        scrollView.setTranslationX((int)(trans*0.05));
    }

    private void hideNupdateSideSwipeThing() {
        int myDurat = 200;
        RelativeLayout isGoingBackIndicator = findViewById(R.id.isGoingBackIndicator);
//        isGoingBackIndicator.animate().setDuration(mAnimationTime).translationX(Utils.dpToPx(-40)).start();

        final View v = findViewById(R.id.swipeBackViewIndicator);
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

        final LockableScrollView scrollView = findViewById(R.id.ScrollView);
        scrollView.animate().setDuration(myDurat).setInterpolator(new LinearOutSlowInInterpolator()).translationX(0)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        scrollView.setTranslationX(0);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }




    private void expandSettings(){
        mScrollView.setScrollingEnabled(false);
        isSettingsCardExpanded = true;
        TweaksBlackBack.setVisibility(View.VISIBLE);
        TweaksLayout.setVisibility(View.VISIBLE);
        findViewById(R.id.TweaksContainer).setVisibility(View.VISIBLE);

        final float alph = 0.8f;
        TweaksBlackBack.animate().alpha(alph).setDuration(mAnimationTime).setInterpolator(new LinearInterpolator())
                .setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                TweaksBlackBack.setAlpha(alph);
                TweaksBlackBack.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();

        final float trans = 0;
        TweaksLayout.animate().translationY(trans).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        TweaksLayout.setTranslationY(trans);
                        TweaksLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();


        final View settingsLine = findViewById(R.id.settingsLine);
        final float xtrans = 0;
        settingsLine.animate().translationX(xtrans).setDuration(2000).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        settingsLine.setTranslationX(xtrans);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        MyTouchBackGestureListener = new GestureDetector(mContext,new MyTouchBackGestureListener());
        setExpandedSettingsClickListeners();
        findViewById(R.id.TweaksLayout).setVisibility(View.VISIBLE);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setExpandedSettingsClickListeners(){
        final LinearLayout notificationsLayout = findViewById(R.id.notificationsLayout);
        final LinearLayout personalInfoLayout = findViewById(R.id.personalInfoLayout);
        final LinearLayout TargetLayout = findViewById(R.id.TargetLayout);
        final LinearLayout ChangeCPVLayout = findViewById(R.id.ChangeCPVLayout);

        notificationsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNotfBtn.performClick();
                kindaCollapseSettings();
            }
        });

        personalInfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUserDataDialog();
                kindaCollapseSettings();
            }
        });

        TargetLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                targetedBtn.performClick();
                kindaCollapseSettings();
            }
        });

        ChangeCPVLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCPVBtn.performClick();
                kindaCollapseSettings();
            }
        });

        findViewById(R.id.phoneIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationsLayout.performClick();
            }
        });

        findViewById(R.id.userInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                personalInfoLayout.performClick();
            }
        });

        findViewById(R.id.locationIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TargetLayout.performClick();
            }
        });

        findViewById(R.id.changeCPVIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeCPVLayout.performClick();
            }
        });

        TweaksBlackBack.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                Log.d(TAG,"Raw Y: "+event.getRawY());
                if (MyTouchBackGestureListener.onTouchEvent(event)) {
                    return true;
                }
                return false;
            }
        });

    }

    class MyTouchBackGestureListener extends GestureDetector.SimpleOnGestureListener {
        int origX = 0;
        int origY = 0;


        @Override
        public boolean onDown(MotionEvent event) {
//            Log.d("TAG", "onDown: ");
            if(isShowing_chooseAmountLayout){
                if (event.getRawY() < 450) {
                    onBackPressed();
                }
            }else {
                if (event.getRawY() < 730) {
                    onBackPressed();
                }
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
//            Log.i("TAG", "onSingleTapConfirmed: ");

            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
//            Log.i("TAG", "onLongPress: ");
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
//            Log.i("TAG", "onDoubleTap: ");
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;

        }
    }

    private void kindaCollapseSettings(){
        final float alph = 0.8f;
//        TweaksBlackBack.setAlpha(alph);
        TweaksBlackBack.animate().alpha(alph).setDuration(mAnimationTime).setInterpolator(new LinearInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        TweaksBlackBack.setAlpha(alph);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        final float trans = Utils.dpToPx(230);
        final float alph2 = 0.6f;
        TweaksLayout.animate().translationY(trans).alpha(alph2).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        TweaksLayout.setTranslationY(trans);
                        TweaksLayout.setAlpha(alph2);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }

    private void unKindaCollapseSettings(){
        final float alph = 0.8f;
        TweaksBlackBack.animate().alpha(alph).setDuration(mAnimationTime).setInterpolator(new LinearInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        TweaksBlackBack.setAlpha(alph);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        final float trans = 0;
        final float alph2 = 1f;
        TweaksLayout.animate().translationY(trans).alpha(alph2).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        TweaksLayout.setTranslationY(trans);
                        TweaksLayout.setAlpha(alph2);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }

    private void collapseSettingsCard(){
        isSettingsCardExpanded = false;
        mScrollView.setScrollingEnabled(true);

        final float alph = 0f;
        TweaksBlackBack.animate().alpha(alph).setDuration(mAnimationTime)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        TweaksBlackBack.setAlpha(alph);
                        TweaksBlackBack.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        final float trans = Utils.dpToPx(250);
        TweaksLayout.animate().translationY(trans).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        TweaksLayout.setTranslationY(trans);
                        TweaksLayout.setVisibility(View.GONE);
                        findViewById(R.id.TweaksContainer).setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

        final View settingsLine = findViewById(R.id.settingsLine);
        final float xtrans = -Utils.dpToPx(220);
        settingsLine.animate().translationX(xtrans).setDuration(2000).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        settingsLine.setTranslationX(xtrans);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();

//        findViewById(R.id.TweaksLayout).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
//        if(hasFocus && isSettingsCardExpanded) {
//            unKindaCollapseSettings();
//        }
    }



    private int y_promptDialog;
    private boolean isUpSwipingSwiping = false;
    private GestureDetector gt;
    private void addTouchListenerForSwipeUpToGoBack(){
        gt = new GestureDetector(this ,new MySwipebackPromptGestureListener());
        View swipeDownView = findViewById(R.id.swipeDownView);
        swipeDownView.setVisibility(View.VISIBLE);
        swipeDownView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                passTouchEvents(event);
                if (gt.onTouchEvent(event)) {
                    return true;
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (isUpSwipingSwiping) {
                        isSideScrolling = false;
                        restoreOnBottomsheetDialog();
                    }
                }
                return false;
            }
        });
    }

    private void removeTouchListenerForSwipeUpToGoBack(){
        final View swipeDownView = findViewById(R.id.swipeDownView);
        swipeDownView.setVisibility(View.GONE);

        final RelativeLayout TweaksContainer = findViewById(R.id.TweaksContainer);
        TweaksContainer.setTranslationY(0);
        TweaksContainer.setAlpha(1f);
    }

    class MySwipebackPromptGestureListener extends GestureDetector.SimpleOnGestureListener {
        int origX = 0;
        int origY = 0;


        @Override
        public boolean onDown(MotionEvent event) {
//            Log.d(TAG, "onDown: ");
            final int X = (int) event.getRawX();
            final int Y = (int) event.getRawY();
            Log.e(TAG, "onDown: event.getRawX(): " + event.getRawX() + " event.getRawY()" + event.getRawY());
            View swipeDownView = findViewById(R.id.swipeDownView);
            CoordinatorLayout.LayoutParams lParams = (CoordinatorLayout.LayoutParams) swipeDownView.getLayoutParams();
            y_promptDialog = Y - lParams.topMargin;
            Log.e(TAG, "onDown: top margin: " + lParams.topMargin+" y_promptDialog: "+y_promptDialog);

            origX = lParams.leftMargin;
            origY = lParams.topMargin;


            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            final int Y = (int) e2.getRawY();
            final int X = (int) e2.getRawX();

            Log.d(TAG, "the scroll pos= " + ((double)(Y-y_promptDialog)));

            animateTouchOnBottomsheetDialog((double)(Y - y_promptDialog));
            isUpSwipingSwiping = true;

            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.d(TAG,"velocityY-"+velocityY);
            if (Math.abs(velocityY) > Math.abs(velocityX) && Math.abs(velocityY)>1400 && Math.abs(velocityY)<8000) {
                onBackPressed();
            }else{
                restoreOnBottomsheetDialog();
            }
            isUpSwipingSwiping = false;
            return false;

        }
    }

    private void animateTouchOnBottomsheetDialog(double pos){
        double newPos = (pos/10);
        final RelativeLayout TweaksContainer = findViewById(R.id.TweaksContainer);
        TweaksContainer.setTranslationY(((float)newPos));

        TweaksLayout.setTranslationY(Utils.dpToPx(230)+((float)newPos));

        final float alph = 0.6f;
        float alphPos = (((float)((pos/10)))/100f);
        final float TweaksContainerAlph = 1f+alphPos;
        Log.w(TAG,"Set Alpha for TweaksContainer: " + TweaksContainerAlph );
        TweaksContainer.setAlpha(TweaksContainerAlph);

        final float TweaksLayoutAlph = alph-alphPos;
        Log.w(TAG,"Set Alpha for TweaksLayout: " + TweaksLayoutAlph);
        TweaksLayout.setAlpha(TweaksLayoutAlph);
    }

    private void restoreOnBottomsheetDialog(){
        final float newPos = 0;
        final RelativeLayout TweaksContainer = findViewById(R.id.TweaksContainer);
        final float alph2 = 1f;
        TweaksContainer.animate().translationY(newPos).alpha(alph2).setInterpolator(new LinearOutSlowInInterpolator()).setDuration(mAnimationTime).start();

        final float trans = Utils.dpToPx(230);
        final float alph = 0.6f;
        TweaksLayout.animate().translationY(trans).alpha(alph).setDuration(mAnimationTime).setInterpolator(new LinearOutSlowInInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        TweaksLayout.setTranslationY(trans);
                        TweaksLayout.setAlpha(alph);

                        TweaksContainer.setTranslationY(newPos);
                        TweaksContainer.setAlpha(alph2);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }

    private void passTouchEvents(MotionEvent event){
        if(isShowing_changingCPVMainLayout)onTouch_changingCPVMainLayout(event);
        if(isShowing_chooseAmountLayout)onTouch_chooseAmountLayout(event);
        if(isShowing_mainLayout) onTouch_mainLayout(event);
        if(isShowing_mainLaout2) onTouch_mainLaout2(event);
        if(isShowing_genderLayout) onTouch_genderLayout(event);
        if(isShowing_ageLayout) onTouch_ageLayout(event);
        if(isShowing_locationLayout) onTouch_locationLayout(event);
        if(isShowing_concludeLayout) onTouch_concludeLayout(event);
    }

}
