package com.bry.adcafe.ui;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.User;
import com.bry.adcafe.services.DatabaseManager;
import com.bry.adcafe.services.TimeManager;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{
    public static final String TAG = LoginActivity.class.getSimpleName();
    @Bind(R.id.emailEditText)  EditText mEmail;
    @Bind(R.id.passwordEditText) EditText mPassword;
    @Bind(R.id.LogInCard) CardView mLoginButton;
    @Bind(R.id.registerLink) TextView mRegisterLink;
    @Bind(R.id.LoginAvi) AVLoadingIndicatorView mAvi;
    @Bind(R.id.progressBarlogin) ProgressBar mProgressBarLogin;
    @Bind(R.id.settingUpMessageLogin) TextView mLoadingMessage;
    @Bind(R.id.LoginRelative) RelativeLayout mRelative;
    @Bind(R.id.noConnectionLayout) LinearLayout mNoConnectionLayout;
    @Bind(R.id.retry) Button mRetryButton;
    @Bind(R.id.failedLoadLayout) LinearLayout mFailedLoadLayout;
    @Bind(R.id.retryLoading) Button mRetryLoadingButton;


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private Context mContext;
    private String mKey = "";
    private boolean mIsLoggingIn = false;

    private boolean hasEverythingLoaded;
    private boolean isActivityVisible;
    private boolean didUserJustLogInManually = false;
    private boolean isShowingPromptForeula = false;

    Handler h = new Handler();
    Runnable r;
    private boolean isValidatePromptShowing = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        if(!Fabric.isInitialized()) Fabric.with(this, new Crashlytics());
        mAuth = FirebaseAuth.getInstance();
        mRegisterLink.setOnClickListener(this);
        mLoginButton.setOnClickListener(this);
        mContext = this.getApplicationContext();
        setBroadcastListeners();
        Variables.isLoginOnline = true;

        mPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        (actionId == EditorInfo.IME_ACTION_DONE) ||
                        (actionId == EditorInfo.IME_ACTION_NEXT) ||
                        (actionId == EditorInfo.IME_ACTION_GO)) {
                    try{
                        mLoginButton.performClick();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    Log.i(TAG,"Enter pressed");
                }
                return false;
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()!= null){
                    Log.d(TAG,"A user exists."+firebaseAuth.getCurrentUser().getUid());
                    if(isOnline(mContext)){
                        Log.d(TAG,"user is online, setting up everything normally");
                        mRelative.setVisibility(View.GONE);
                        mNoConnectionLayout.setVisibility(View.GONE);
                        mProgressBarLogin.setVisibility(View.VISIBLE);
                        mLoadingMessage.setVisibility(View.VISIBLE);
                        mIsLoggingIn = false;
                        startLoadingUserData();
                    }else{
                        setNoInternetView();
                    }
                }
            }
        };
    }

    private void setBroadcastListeners(){
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForFinishedLoadingData,new IntentFilter(Constants.LOADED_USER_DATA_SUCCESSFULLY));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForFailedToLoadData,new IntentFilter(Constants.FAILED_TO_LOAD_USER_DATA));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForSetUpTime,new IntentFilter(Constants.LOAD_TIME));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForFinishedSubscribingUserToAllAdsCategory
                ,new IntentFilter(Constants.SET_UP_USERS_SUBSCRIPTION_LIST));
    }

    private void removeBroadCastReceivers(){
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForFailedToLoadData);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForFinishedLoadingData);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForSetUpTime);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForFinishedSubscribingUserToAllAdsCategory);
    }

    private  void setNoInternetView(){
        Log.d(TAG,"There is no internet connection,showing no internet dialog");
        mRelative.setVisibility(View.GONE);
        mProgressBarLogin.setVisibility(View.GONE);
        mLoadingMessage.setVisibility(View.GONE);
        mNoConnectionLayout.setVisibility(View.VISIBLE);
        mRetryButton.setOnClickListener(this);
    }

    private void setFailedToLoadView(){
        Log.d(TAG,"Failed to load data,showing failed to load data dialog");
//        mRelative.setVisibility(View.GONE);
//        mAvi.setVisibility(View.GONE);
        mProgressBarLogin.setVisibility(View.GONE);
        mLoadingMessage.setVisibility(View.GONE);

        mFailedLoadLayout.setVisibility(View.VISIBLE);
        mRetryLoadingButton.setOnClickListener(this);
    }

    private void startMainActivity2(){
        if(hasEverythingLoaded && isActivityVisible && !isShowingPromptForeula){
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String uid = user.getUid();
            User.setUid(uid);
//            mAvi.setVisibility(View.GONE);
            mProgressBarLogin.setVisibility(View.GONE);
            mLoadingMessage.setVisibility(View.GONE);
            Variables.isStartFromLogin = true;
            Intent intent = new Intent (LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
        }
    }

    private void startMainActivity(){
        if(hasEverythingLoaded && isActivityVisible) checkIfTheFuckingEULAHasBeenUpdated();
    }

    private void startSelectCategory(){
        if(hasEverythingLoaded && isActivityVisible) {
            Variables.isStartFromLogin = true;
//            mAvi.setVisibility(View.GONE);
            mProgressBarLogin.setVisibility(View.GONE);
            mLoadingMessage.setVisibility(View.GONE);
            Intent intent = new Intent(LoginActivity.this, SelectCategory.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }


    private BroadcastReceiver mMessageReceiverForFinishedLoadingData = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Finished loading user data");
            if(Variables.Subscriptions.containsKey(Constants.CATEGORY_EVERYONE)){
                hasEverythingLoaded = true;
                if(isActivityVisible)checkIfTheFuckingEULAHasBeenUpdated();
            }else{
                if(Variables.Subscriptions.isEmpty()){
                    //If user has no subscriptions, app will take them to select subscription activity
                    hasEverythingLoaded = true;
                    if(isActivityVisible)checkIfTheFuckingEULAHasBeenUpdated();
                }else{
                    //if user has subscriptions, but not containing the 'everyone' category;
                    Log.e(TAG,"User isn't subbed to everyone category. Subscribing him/her now.");
                    DatabaseManager man = new DatabaseManager();
                    man.setContext(mContext);
                    man.subscribeUserToSpecificCategory(Constants.CATEGORY_EVERYONE);
                }
            }

        }
    };

    private BroadcastReceiver mMessageReceiverForFinishedSubscribingUserToAllAdsCategory = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Finished subscribing user to everything category. Restarting loading user data.");
            nowReallyStartLoadingUserData();
        }
    };

    private BroadcastReceiver mMessageReceiverForFailedToLoadData = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Failed to load User data");
            hasEverythingLoaded = false;
            setFailedToLoadView();
        }
    };

    private BroadcastReceiver mMessageReceiverForSetUpTime = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Finished setting up time.");
            reallyStartLoadingUserData();
        }
    };

    @Override
    protected void onStart(){
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onPause(){
        super.onPause();
        isActivityVisible = false;
    }

    @Override
    protected void onResume(){
        super.onResume();
        isActivityVisible = true;
        if(hasEverythingLoaded) checkIfTheFuckingEULAHasBeenUpdated();
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onDestroy(){
        Variables.isLoginOnline = false;
        removeBroadCastReceivers();
        super.onDestroy();
    }


    @Override
    public void onClick(View v){
        if(v == mRegisterLink && !mIsLoggingIn){
            Intent intent = new Intent(LoginActivity.this,CreateAccountActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        if(v == mLoginButton && !mIsLoggingIn){
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            loginUserWithPassword();

        }
        if(v == mRetryButton){
            if(isOnline(mContext)){
                mNoConnectionLayout.setVisibility(View.GONE);
                mRelative.setVisibility(View.GONE);
//                mAvi.setVisibility(View.VISIBLE);
                mProgressBarLogin.setVisibility(View.VISIBLE);
                mLoadingMessage.setVisibility(View.VISIBLE);
                startLoadingUserData();
            }else{
                Log.d(TAG,"No internet connection!!");
                Toast.makeText(mContext,"You don't have an internet connection.",Toast.LENGTH_SHORT).show();
            }
        }
        if(v== mRetryLoadingButton){
            mRelative.setVisibility(View.GONE);
            mFailedLoadLayout.setVisibility(View.GONE);
//            mAvi.setVisibility(View.VISIBLE);
            mProgressBarLogin.setVisibility(View.VISIBLE);
            mLoadingMessage.setVisibility(View.VISIBLE);
            Toast.makeText(mContext,"Retrying...",Toast.LENGTH_SHORT).show();
            startLoadingUserData();
        }
    }


    private void loginUserWithPassword() {
        final String email = mEmail.getText().toString().trim();
        final String password = mPassword.getText().toString().trim();
        if(email.equals("")){
            mEmail.setError("Please enter your email");
            return;
        }
        if(password.equals("")){
            mPassword.setError("Password cannot be blank");
            return;
        }
        if(!isOnline(mContext)){
            Snackbar.make(findViewById(R.id.loginCoordinatorLayout), R.string.LogInNoConnection,
                    Snackbar.LENGTH_LONG).show();
        }else{
            mProgressBarLogin.setVisibility(View.VISIBLE);
            mLoadingMessage.setVisibility(View.VISIBLE);
            mRelative.setVisibility(View.GONE);
            mIsLoggingIn = true;
            Log.d(TAG,"--Logging in user with username and password...");

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG,"signInWithEmail:onComplete"+task.isSuccessful());
                            if(!task.isSuccessful()){
                                Log.w(TAG,"SignInWithEmail",task.getException());
                                mRelative.setVisibility(View.VISIBLE);
                                mProgressBarLogin.setVisibility(View.GONE);
                                mLoadingMessage.setVisibility(View.GONE);
                                mIsLoggingIn = false;
                                showFailedLogin();
                            }else{
                                didUserJustLogInManually = true;
                                setUserPasswordInFireBase(password);
                                Variables.setPassword(password);
                                Variables.isGottenNewPasswordFromLogInOrSignUp = true;
                            }
                        }
                    });
            }
    }

    private void showFailedLogin(){
        final Dialog d = new Dialog(this);
        d.setTitle("Failed Login.");
        d.setContentView(R.layout.dialog97);
        Button b1 = d.findViewById(R.id.okBtn);
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }




    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }

    private void setUserPasswordInFireBase(String password){
        new DatabaseManager().setUsersNewPassword(Variables.encryptPassword(password));
    }


    private void startLoadingUserData(){
        if(!TimeManager.isTimerOnline()) TimeManager.setUpTimeManager(Constants.LOAD_TIME,mContext);
        else reallyStartLoadingUserData();
    }

    private void reallyStartLoadingUserData(){
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
                        nowReallyStartLoadingUserData();
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

    private void PerformShutdown() {
        if(!didUserJustLogInManually) {
            if (FirebaseAuth.getInstance() != null) {
                FirebaseAuth.getInstance().signOut();
            }
            Variables.resetAllValues();
            mRelative.setVisibility(View.VISIBLE);
            mProgressBarLogin.setVisibility(View.GONE);
            mLoadingMessage.setVisibility(View.GONE);
            mIsLoggingIn = false;
        }else setNewSessionKeyThenReallyStartLoadingUsersData();

    }

    private void setNewSessionKeyThenReallyStartLoadingUsersData() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String newSessionKey = generateRandomString();
        DatabaseReference FirstCheckref = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_CHILD_USERS)
                .child(uid).child(Constants.BOI_IS_DA_KEY);
        FirstCheckref.setValue(newSessionKey).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    setSessionKeyInSharedPrefs(newSessionKey);
                    nowReallyStartLoadingUserData();
                }
            }
        });
    }

    private void setSessionKeyInSharedPrefs(String newKey){
        SharedPreferences pref2 = getApplicationContext().getSharedPreferences(Constants.BOI_IS_DA_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor2 = pref2.edit();
        editor2.clear();
        editor2.putString(Constants.BOI_IS_DA_KEY, newKey);
        editor2.apply();
    }

    private String generateRandomString(){
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("R");
//        DatabaseReference myref = ref.push();
//        String key = myref.getKey();
//        String finalKey = "R"+key;

        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        String uuid2 = UUID.randomUUID().toString().replaceAll("-", "");
        String finalKey= uuid+uuid2;
        Log.i("Dashboard","generated randomString : "+finalKey);
        return finalKey;
    }

    public String getSessionKey(){
        SharedPreferences prefs2 = getSharedPreferences(Constants.BOI_IS_DA_KEY, MODE_PRIVATE);
        String sk = prefs2.getString(Constants.BOI_IS_DA_KEY, "NULL");
        Log.d(TAG, "Loading session key from shared prefs - " + sk);
        return sk;
    }

    private void nowReallyStartLoadingUserData(){
        Variables.Subscriptions.clear();
        DatabaseManager dbMan = new DatabaseManager();
        dbMan.setContext(mContext);
        dbMan.loadUserData(mContext);
    }




    private void checkIfTheFuckingEULAHasBeenUpdated(){
        DatabaseReference eulaRef = FirebaseDatabase.getInstance().getReference(Constants.EULA_REFERENCE);
        eulaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    int eulaVersion = dataSnapshot.getValue(Integer.class);
                    if(getEulaVersionInSharedPref()!= 0) {
                        if (eulaVersion!=getEulaVersionInSharedPref()){
                            showPromptForChangeInEula();
                            setEulaVersionInSharedPref(eulaVersion);
                        }else{
                            theNextThing();
                        }
                    }else{
                        setEulaVersionInSharedPref(eulaVersion);
                        theNextThing();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    theNextThing();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private int getEulaVersionInSharedPref(){
        SharedPreferences prefs2 = getSharedPreferences(Constants.EULA_REFERENCE, MODE_PRIVATE);
        int sk = prefs2.getInt(Constants.EULA_REFERENCE, 0);
        Log.d(TAG, "Loading eula version from shared prefs - " + sk);
        return sk;
    }

    private void setEulaVersionInSharedPref(int newKey){
        SharedPreferences pref2 = getApplicationContext().getSharedPreferences(Constants.EULA_REFERENCE, MODE_PRIVATE);
        SharedPreferences.Editor editor2 = pref2.edit();
        editor2.clear();
        editor2.putInt(Constants.EULA_REFERENCE, newKey);
        editor2.apply();
    }

    private void showPromptForChangeInEula(){
        isShowingPromptForeula = true;
        final Dialog d = new Dialog(this);
        d.setTitle("Change in Eula.");
        d.setContentView(R.layout.dialog_change_in_eula);

        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        Button okBtn = d.findViewById(R.id.okBtn);
        Button viewBtn = d.findViewById(R.id.viewBtn);
        TextView explanation = d.findViewById(R.id.explanation);

        String sourceString = "We've updated our terms of service. Please review them by clicking REVIEW. " +
                "By clicking CONTINUE, you agree to our updated End User License Agreement.";
        explanation.setText(sourceString);

        viewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Vibrator b = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    b.vibrate(30);
                    String url = Constants.EULA;
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(webIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isShowingPromptForeula = false;
                d.dismiss();
                theNextThing();
            }
        });
        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                isShowingPromptForeula = false;
            }
        });
        d.setCancelable(false);
        d.show();

    }

    private void theNextThing(){
        checkIfUserIsAuthentic();
    }



    private void checkIfUserIsAuthentic(){
        if(checkIfEmailIsVerified()){
            if(!isValidatePromptShowing)startNextActivity();
        }else{
            if(!isValidatePromptShowing)openNotVerifiedPrompt();
        }
    }

    private void openNotVerifiedPrompt() {
        if(!checkIfEmailIsVerified())sendVerificationEmail();
        final Dialog d = new Dialog(this);
        d.setTitle("Email Verification.");
        isValidatePromptShowing = true;
        d.setContentView(R.layout.dialog_reverify_email);
        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        Button b1 = d.findViewById(R.id.okBtn);
        final TextView hasVerifiedText = d.findViewById(R.id.hasVerifiedText);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationEmail();
            }
        });

        d.setCancelable(false);
        d.show();

        r = new Runnable() {
            @Override
            public void run() {
                if(checkIfEmailIsVerified()){
                    hasVerifiedText.setText("Email verified.");
                    h.removeCallbacks(r);
                    isValidatePromptShowing = false;
                    d.dismiss();

                    startNextActivity();
                }else{
                    hasVerifiedText.setText("Email not verified.");
                }
                if(Variables.isLoginOnline)h.postDelayed(r, 1000);
            }
        };
        h.postDelayed(r, 1000);

    }

    private void startNextActivity() {
        if(!Variables.Subscriptions.isEmpty())startMainActivity2();
        else if(Variables.Subscriptions.isEmpty()) startSelectCategory();
    }

    private boolean checkIfEmailIsVerified() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.reload();
        return user.isEmailVerified();
    }

    private void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(mContext,"Email sent.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}
