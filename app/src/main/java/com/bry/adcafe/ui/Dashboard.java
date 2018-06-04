package com.bry.adcafe.ui;

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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Vibrator;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.Payment.Lipisha.Payment;
import com.bry.adcafe.Payment.mpesaApi.Mpesaservice;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.fragments.ChangeCPVFragment;
import com.bry.adcafe.fragments.FeedbackFragment;
import com.bry.adcafe.fragments.FragmentUserPayoutBottomSheet;
import com.bry.adcafe.services.DatabaseManager;
import com.bry.adcafe.services.OnSwipeTouchListener;
import com.bry.adcafe.services.Payments;
import com.bry.adcafe.services.SliderPrefManager;
import com.bry.adcafe.services.TimeManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Calendar;
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
//    @Bind(R.id.LogoutBtn) public ImageButton mLogout;
    @Bind(R.id.payoutBtn) public ImageButton payoutBtn;
//    @Bind(R.id.shareAppBtn) public ImageButton shareAppBtn;

    public Context miniContext;
    private ProgressDialog mProgForPayments;
    private String joke;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Variables.isDashboardActivityOnline = true;
        mContext = this.getApplicationContext();
        miniContext = mContext;
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
    }

    @Override
    protected void onResume(){
        super.onResume();
        new DatabaseManager().loadUsersPassword();
        if (!TimeManager.isTimerOnline())TimeManager.setUpTimeManager("RESET_TIMER",mContext);
        setValues();
    }

    private void setListeners(){
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForShowingPrompt,
                new IntentFilter("SHOW_PROMPT"));

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForStartPayout,
                new IntentFilter("START_PAYOUT"));
    }

    private void removeListeners(){
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForShowingPrompt);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverForStartPayout);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiverForSuccessfulPayout);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForFailedPayout);
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

        findViewById(R.id.uploadedAdsStats).setOnClickListener(new View.OnClickListener() {
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
                FragmentManager fm = getFragmentManager();
                Log("DASHBOARD","Setting up fragment");
                FeedbackFragment reportDialogFragment = new FeedbackFragment();
                reportDialogFragment.setMenuVisibility(false);
                reportDialogFragment.show(fm, "Feedback.");
                reportDialogFragment.setfragContext(mContext);
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

//        mLogout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                promptUserIfSureToLogout();
//            }
//        });

        payoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Variables.getTotalReimbursementAmount()<1){
                    promptUserForUnableToPayout();
                }else{
                    if(isOnline(mContext)){
                        if (!TimeManager.isTimerOnline())TimeManager.setUpTimeManager("RESET_TIMER",mContext);
                        promptUserAboutPayout();
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
        super.onDestroy();
        removeListeners();
    }

    private void loadViews() {
        mTotalAdsSeenAllTime = (TextView) findViewById(R.id.AdsSeenAllTimeNumber);
        mTotalAdsSeenToday = (TextView) findViewById(R.id.AdsSeenTodayNumber);
        mInfoImageView = (ImageView) findViewById(R.id.helpIcon);
        mUploadAnAdIcon = (CardView) findViewById(R.id.uploadAnAdIcon);
        mAmmountNumber = (TextView) findViewById(R.id.ammountNumber);
        mUploadedAdsStats = (Button) findViewById(R.id.uploadedAdsStats);
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
        int totalAmounts = (int)(monthsTotals*Constants.CONSTANT_AMMOUNT_FOR_USER);
        mAmmountNumber.setText(Integer.toString(reimbursementTotals));

        if(Variables.doesUserWantNotifications)mDotForNotf.setVisibility(View.VISIBLE);
    }



    @Override
    public void onBackPressed(){
        if(!Variables.isMainActivityOnline){
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
            message = "Do you wish to put off daily morning alerts about new ads?";
        else message = "Do you wish to put back on daily morning notifications?";

        final Dialog d = new Dialog(this);
        d.setTitle("Notifications.");
        d.setContentView(R.layout.dialog8);
        Button b1 = (Button) d.findViewById(R.id.continueBtn);
        Button b2 = (Button) d.findViewById(R.id.cancelBtn);
        if(Variables.doesUserWantNotifications)b1.setText("PUT OFF.");
        else b1.setText("PUT ON.");
        TextView t = (TextView) d.findViewById(R.id.explanation);
        ImageButton imgBtn = (ImageButton) d.findViewById(R.id.pickTimeIcon);
        final TextView timeTxt = (TextView) d.findViewById(R.id.setTimeText);
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
        Button b1 = (Button) d.findViewById(R.id.continueBtn);
        Button b2 = (Button) d.findViewById(R.id.cancelBtn);
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

        if(Variables.getMonthAdTotals(mKey) ==0) {
            SharedPreferences prefs3 = getSharedPreferences("ReimbursementTotals", MODE_PRIVATE);
            reimbursementTotals = prefs3.getInt(Constants.REIMBURSEMENT_TOTALS, 0);

        }else reimbursementTotals = Variables.getTotalReimbursementAmount();

        String payoutPhoneNumber = Variables.phoneNo;
        int payoutAmount = (int)(reimbursementTotals);
        String PAYOUT_SUCCESSFUL = "PAYOUT_SUCCESSFUL";
        String PAYOUT_FAILED = "PAYOUT_FAILED";

        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.PAY_POOL);
        DatabaseReference pushRef = adRef.push();
        Variables.transactionID = "TRANS"+pushRef.getKey();

        String newPhoneNo = "254"+payoutPhoneNumber.substring(1);
        Log("Dashboard","new Phone no is: "+newPhoneNo);

        if(FirebaseAuth.getInstance().getCurrentUser().getEmail().equals("bryonyoni@gmail.com")) payoutAmount = 10;
        String StringPayoutAmount = Integer.toString(payoutAmount);

        Mpesaservice mps = new Mpesaservice("Xna3G2ahKqwsXmciMfmAtmxqv9GjShqx","xSkVFsFMUFJ2OEAA");
        mps.authenticateThenPayouts(StringPayoutAmount,newPhoneNo);


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
        }
    };

    private void showFailedPayoutsView() {
        final Dialog d = new Dialog(this);
        d.setTitle("Failed Payout.");
        d.setContentView(R.layout.dialog94);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Payout");
        builder.setMessage("You cant make a payout of 0Ksh.")
                .setCancelable(true)
                .setPositiveButton("Ok.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
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
    }

    private void showSuccessfulPayoutPrompt() {
        final Dialog d = new Dialog(this);
        d.setTitle("Successful Payout.");
        d.setContentView(R.layout.dialog95);
        Button b1 = d.findViewById(R.id.okBtn);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }

    private void setPayoutReceiptInFireBase(int amount) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.REIMBURSEMENT_HISTORY).child(Long.toString(-TimeManager.getDateInDays()));
        DatabaseReference pushRef = adRef.push();
        String pushId= pushRef.getKey();
        pushRef.child("Date").setValue(TimeManager.getDateInDays());
        pushRef.child("Time").setValue(TimeManager.getTime());
        pushRef.child("TransactionID").setValue(Variables.transactionID);
        pushRef.child("PhoneNo").setValue(Variables.phoneNo);
        pushRef.child("Amount").setValue(amount);
        pushRef.child("pushID").setValue(pushId);
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
            public void onResponse(Call call, Response response) throws IOException {
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
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(Constants.ADMIN_MONEY);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    long amm = dataSnapshot.getValue(long.class);
                    long newAmm = amm -= amount;
                    DatabaseReference mewRef = FirebaseDatabase.getInstance().getReference(Constants.ADMIN_MONEY);
                    mewRef.setValue(newAmm);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
