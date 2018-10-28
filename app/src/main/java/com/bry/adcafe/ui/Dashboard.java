package com.bry.adcafe.ui;

import android.animation.Animator;
import android.app.Activity;
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
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
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

    @Bind(R.id.ScrollView) ScrollView mScrollView;
    @Bind(R.id.feedChatEditText) EditText mFeedChatEditText;
    @Bind(R.id.addImageBtn) ImageButton mAddImageBtn;
    @Bind(R.id.sendBtn) ImageButton mSendBtn;
    @Bind(R.id.chatsPlaceHolderView) PlaceHolderView mChatsPlaceHolderView;

    private int numberOfItemsAdded = 0;
    private Uri mFilepath;
    private String mPath;
    private Bitmap imageBitmap;

    private int numberOfNewMessages = 0;



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
                Intent intent = new Intent(Dashboard.this,TutorialUsers.class);
                startActivity(intent);
                Variables.isStartFromLogin = false;
                Variables.isInfo = true;
                finish();
            }
        });

        mUploadAnAdIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myPrefManager = new SliderPrefManager(getApplicationContext());
                if (myPrefManager.isFirstTimeLaunchForAdvertisers()){
                    Intent intent = new Intent(Dashboard.this,TutorialAdvertisers.class);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(Dashboard.this,SelectCategoryAdvertiser.class);
                    startActivity(intent);
                }
            }
        });

        findOutCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, AdStats.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.uploadedAdsStats).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(FirebaseAuth.getInstance().getCurrentUser().getEmail().equals("bryonyoni@gmail.com")){
                    Intent intent = new Intent(Dashboard.this, AdminConsole.class);
//                    startActivity(intent);
                }else{
//                    Log("Dashboard","NOT administrator.");
                }
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

                  showFeedChatView();
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
                Intent intent = new Intent(Dashboard.this, SubscriptionManager.class);
                startActivity(intent);
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
                if(Variables.getTotalReimbursementAmount()<1){
                    promptUserForUnableToPayout();
                }else{
                    if(isOnline(mContext)){
                        if (!TimeManager.isTimerOnline())TimeManager.setUpTimeManager("RESET_TIMER",mContext);
                        if(!isMakingPayout) promptUserAboutPayout();
                    }
                    else Toast.makeText(mContext,"You need internet connection first.",Toast.LENGTH_SHORT).show();
                }
            }
        });

//        shareAppBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                promptUserToShareApp();
//            }
//        });

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
        mTotalAdsSeenToday.setText(Integer.toString(todaysTotals));
        if(monthsTotals>9999){
            mTotalAdsSeenAllTime.setTextSize(35);
            mTotalAdsSeenToday.setTextSize(35);
        }
        mTotalAdsSeenAllTime.setText(Integer.toString(monthsTotals));
        mTotalAdsSeenAllTime.setText(format.format(monthsTotals));
        mAmmountNumber.setText(Integer.toString(reimbursementTotals));

        if(Variables.doesUserWantNotifications)mDotForNotf.setVisibility(View.VISIBLE);

        SharedPreferences pref = mContext.getSharedPreferences(Constants.CONSENT_TO_TARGET, MODE_PRIVATE);
        final boolean canUseData = pref.getBoolean(Constants.CONSENT_TO_TARGET,false);
        if(!canUseData) findViewById(R.id.dotForTargeted).setVisibility(View.VISIBLE);
    }



    @Override
    public void onBackPressed(){
        if(!isCardCollapsed){
            mCollapseFeedChatButton.performClick();
        }else if(!Variables.isMainActivityOnline){
            Intent intent = new Intent(Dashboard.this,MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else{
            super.onBackPressed();
        }

    }

    private void promptUserAboutNotifications2(){
        String message;
        if(Variables.doesUserWantNotifications)
            message = "Do you wish to put off daily morning alerts about new ads?";
        else message = "Do you wish to put back on daily morning notifications?";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Morning Notifications");
        builder.setMessage(message)
                .setCancelable(true)
                .setPositiveButton("Yes.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean newValue = !Variables.doesUserWantNotifications;
                        setUsersPreferedNotfStatus(newValue);
                    }
                })
                .setNegativeButton("No.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
    }

    private void promptUserAboutNotifications(){
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

    private void promptUserAboutChangingPrice(){
        FragmentManager fm = getFragmentManager();
        ChangeCPVFragment cpvFragment = new ChangeCPVFragment();
        cpvFragment.setMenuVisibility(false);
        cpvFragment.show(fm,"Change cpv fragment");
        cpvFragment.setContext(mContext);
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

        Toast.makeText(mContext,"Your preference has been set.",Toast.LENGTH_SHORT).show();
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

        Toast.makeText(mContext,"The time has been set.",Toast.LENGTH_SHORT).show();
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
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

    private void loadUserDataDialog(){
        FragmentManager fm = getFragmentManager();
        SetUsersPersonalInfo cpvFragment = new SetUsersPersonalInfo();
        cpvFragment.setMenuVisibility(false);
        cpvFragment.show(fm,"Edit User Data.");
        cpvFragment.setfragcontext(mContext);
        cpvFragment.setActivity(thisActivity);
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

    private void promptUserToStopTargeting(){
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
            mScrollView.setVisibility(View.GONE);
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
            addMessageToList(message);
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
                    addMessageToList(message);
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

        addMessageToList(message);
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




    private void addMessageToList(Message message){
        mChatsPlaceHolderView.addView(0,new MessageItem(mContext,mChatsPlaceHolderView,message));
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
        mScrollView.setVisibility(View.VISIBLE);
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


}
