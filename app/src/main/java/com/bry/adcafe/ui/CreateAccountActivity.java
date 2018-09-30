package com.bry.adcafe.ui;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Html;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CreateAccountActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = CreateAccountActivity.class.getSimpleName();
    private ProgressDialog mAuthProgressDialog;
    private Context mContext;
    private String mKey = "";

    @Bind(R.id.createUserButton)
    Button mCreateUserButton;
    @Bind(R.id.nameEditText)
    EditText mNameEditText;
    @Bind(R.id.emailEditText)
    EditText mEmailEditText;
    @Bind(R.id.passwordEditText)
    EditText mPasswordEditText;
    @Bind(R.id.confirmPasswordEditText)
    EditText mConfirmPasswordEditText;
    @Bind(R.id.loginTextView)
    TextView mLoginTextView;
    @Bind(R.id.signUpRelative)
    RelativeLayout mRelative;
    @Bind(R.id.SignUpAvi)
    AVLoadingIndicatorView mAvi;
    @Bind(R.id.progressBarSignUp)
    ProgressBar mProgressBarSignUp;
    @Bind(R.id.creatingAccountLoadingText)
    TextView mLoadingText;
    @Bind(R.id.ConfirmEmailLayout)
    LinearLayout mConfirmEmailLayout;
    @Bind(R.id.textLink)
    TextView mPrivPol;


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String mName;
    private DatabaseReference mRef1;
    private DatabaseReference mRef2;
    private int mClusterID;

    private List<String> easyPasswords = new ArrayList<>
            (Arrays.asList("123456789", "987654321","qwertyuio","asdfghjkl","zxcvbnm12","123456abc","123456qwe","987654qwe",
                    "987654asd",""));

    Handler h = new Handler();
    Runnable r;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        createAuthStateListener();
        mContext = this.getApplicationContext();

        mLoginTextView.setOnClickListener(this);
        mCreateUserButton.setOnClickListener(this);
        mConfirmPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        (actionId == EditorInfo.IME_ACTION_DONE) ||
                        (actionId == EditorInfo.IME_ACTION_NEXT) ||
                        (actionId == EditorInfo.IME_ACTION_GO)) {
                   try{
                       mCreateUserButton.performClick();
                   }catch (Exception e){
                       e.printStackTrace();
                   }
                    Log.i(TAG, "Enter pressed");
                }
                return false;
            }
        });

        mPasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b) findViewById(R.id.dontForgetStuff).setVisibility(View.VISIBLE);
                else findViewById(R.id.dontForgetStuff).setVisibility(View.GONE);
            }
        });

        String sourceString = "By clicking SIGN UP, you agree to our <b>End User License Agreement.</b>";
        mPrivPol.setText(Html.fromHtml(sourceString));
        mPrivPol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Vibrator b = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                b.vibrate(30);
                try {
                    String url = Constants.EULA;
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(webIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
//        TelephonyManager tMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
//            tMgr.getDeviceId();
//        }

    }

    @Override
    public void onClick(View v){
        if(v == mLoginTextView){
            Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        if(v == mCreateUserButton){
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            createNewUser();
        }
    }

    private void createNewUser() {
        final String name = mNameEditText.getText().toString().trim();
        final String email = mEmailEditText.getText().toString().trim();

        final String password = mPasswordEditText.getText().toString().trim();
        String confirmPassword = mConfirmPasswordEditText.getText().toString().trim();

        boolean validEmail = isValidEmail(email);
        boolean validPassword = isValidPassword(password,confirmPassword);
        boolean validName = isValidName(name);
        if(!validEmail || !validName || !validPassword)return;

        if(!isOnline(mContext)){
            Snackbar.make(findViewById(R.id.SignUpCoordinatorLayout), R.string.SignUpNoConnection,
                    Snackbar.LENGTH_LONG).show();
        }else{
//            mAvi.setVisibility(View.VISIBLE);
            mProgressBarSignUp.setVisibility(View.VISIBLE);
            mLoadingText.setVisibility(View.VISIBLE);
            mRelative.setVisibility(View.GONE);
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Log.d(TAG,"authentication successful");
                        createFirebaseUserProfile(task.getResult().getUser());
                        Variables.userName = name;
                        Variables.setPassword(password);
                    }else {
                        mRelative.setVisibility(View.VISIBLE);
//                        mAvi.setVisibility(View.GONE);
                        mProgressBarSignUp.setVisibility(View.GONE);
                        mLoadingText.setVisibility(View.GONE);
                        showFailedSignUp();
                    }
                }
            });
        }


    }

    private void showFailedSignUp(){
        final Dialog d = new Dialog(this);
        d.setTitle("Failed Sign Up.");
        d.setContentView(R.layout.dialog98);
        Button b1 = d.findViewById(R.id.okBtn);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }

    private void createFirebaseUserProfile(final FirebaseUser user) {
        UserProfileChangeRequest addProfileName = new UserProfileChangeRequest.Builder().setDisplayName(mName).build();

        user.updateProfile(addProfileName).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d(TAG,"Created new username");
                }
            }
        });
    }

    @Override
    public void onStart(){
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForFinishedCreatingUserSpace,
                new IntentFilter(Constants.CREATE_USER_SPACE_COMPLETE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverForSetUpTime,
                new IntentFilter(Constants.LOAD_TIME));

    }

    @Override
    public void onStop(){
        super.onStop();
        if(mAuthListener!=null) mAuth.removeAuthStateListener(mAuthListener);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForFinishedCreatingUserSpace);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForSetUpTime);

    }

    private BroadcastReceiver mMessageReceiverForFinishedCreatingUserSpace = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Finished creating user space");
            beforeOpeningTheNextActivity();
        }
    };

    private BroadcastReceiver mMessageReceiverForSetUpTime = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"Finished setting up time.");
            reallySetUpUserSpace();
        }
    };


    private void createAuthStateListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener(){

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth){
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null){
                    setUpUserSpace();
                    user.sendEmailVerification();
                }
            }
        };
    }




    private void startMainActivity(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user.getUid();
        User.setUid(uid);
        Variables.isStartFromLogin = true;
//        mAvi.setVisibility(View.GONE);
        mProgressBarSignUp.setVisibility(View.GONE);
        mLoadingText.setVisibility(View.GONE);
        Intent intent = new Intent(CreateAccountActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void startSelectCategory(){
        Variables.isStartFromLogin = true;
//        mAvi.setVisibility(View.GONE);
        mProgressBarSignUp.setVisibility(View.GONE);
        mLoadingText.setVisibility(View.GONE);
        Intent intent = new Intent(CreateAccountActivity.this, SelectCategory.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean isValidPassword(String password, String confirmPassword) {
        if(password.equals("")){
            mPasswordEditText.setError("We need a password.");
            return false;
        }else if (password.length() < 9) {
            mPasswordEditText.setError("Please create a password containing at least 6 characters");
            return false;
        } else if (!password.equals(confirmPassword)) {
            mPasswordEditText.setError("Passwords do not match");
            return false;
        }else if(easyPasswords.contains(password)){
            mPasswordEditText.setError("Please, put a strong password!");
            return false;
        }
        return true;
    }

    private boolean isValidEmail(String email) {
        if(email.equals("")){
            mEmailEditText.setError("We need your email.");
            return false;
        }

        boolean isGoodEmail = (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches());

        if(!email.contains("@")){
            mEmailEditText.setError("That's not an email address.");
            return false;
        }

        int counter = 0;
        for( int i=0; i<email.length(); i++ ) {
            if(email.charAt(i) == '.' ) {
                counter++;
            }
        }
        if(counter!=1 && counter!=2){
            mEmailEditText.setError("We need your actual email address.");
            return false;
        }

        int counter2 = 0;
        boolean continueIncrement = true;
        for( int i=0; i<email.length(); i++ ) {
            if(email.charAt(i) == '@' ) {
                continueIncrement = false;
            }
            if(continueIncrement)counter2++;
        }
        if(counter2<=3){
            mEmailEditText.setError("That's not a real email address");
            return false;
        }

        if(!isGoodEmail){
            mEmailEditText.setError("We need your actual email address please");
            return false;
        }
        return isGoodEmail;
    }

    private boolean isValidName(String name) {
        if (name.equals("")) {
            mNameEditText.setError("Please enter your name");
            return false;
        }
        if(name.length()>16){
            mNameEditText.setError("That name is too long");
            return false;
        }
        return true;
    }

    public boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }



    private void setUpUserSpace(){
        mRelative.setVisibility(View.GONE);
        if(!TimeManager.isTimerOnline()) TimeManager.setUpTimeManager(Constants.LOAD_TIME, mContext);
        else reallySetUpUserSpace();
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
                    nowReallySetUpUserSpace();
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

    private void reallySetUpUserSpace(){
        setNewSessionKeyThenReallyStartLoadingUsersData();
    }

    private void nowReallySetUpUserSpace(){
        DatabaseManager dbManager = new DatabaseManager();
        dbManager.createUserSpace(mContext);
    }




    private void beforeOpeningTheNextActivity(){
        verifyUser();
    }

    private void verifyUser(){
        if(!checkIfEmailIsVerified())sendVerificationEmail();
        final Dialog d = new Dialog(this);
        d.setTitle("Email.");
        d.setContentView(R.layout.dialog_verify_email);
        Button b1 = d.findViewById(R.id.okBtn);
        final TextView hasVerifiedText = d.findViewById(R.id.hasVerifiedText);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationEmail();
            }
        });
        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                Log.d(TAG,"On dismiss for dialog");
                h.removeCallbacks(r);
            }
        });
        d.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                Log.d(TAG,"On dismiss for dialog");
                h.removeCallbacks(r);
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
                    d.dismiss();
                    startNextActivity();
                }else{
                    hasVerifiedText.setText("Email not verified.");
                }
                h.postDelayed(r, 1000);
            }
        };
        h.postDelayed(r, 1000);
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

    private void startNextActivity(){
        startSelectCategory();
    }


}

