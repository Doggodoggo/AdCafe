package com.bry.adcafe.adapters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.models.User;
import com.bry.adcafe.services.TimeManager;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by bryon on 23/03/2018.
 */

@NonReusable
@Layout(R.layout.older_ads_item)
public class OlderAdsItem {
    @View(R.id.EmailText) private TextView mEmailView;
    @View(R.id.TargetedNumber) private TextView mTargetedNumberView;
    @View(R.id.usersReachedSoFar) private TextView mUsersReachedSoFarView;
    @View(R.id.AmountToReimburse) private TextView mAmountToReimburseView;
    @View(R.id.hasBeenReimbursed) private TextView mHasBeenReimbursedView;
    @View(R.id.dateUploaded) private TextView mDateUploadedView;
    @View(R.id.reimburseOldBtn) private Button mReimburseButton;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private Advert mAdvert;
    private DatabaseReference dbRef;
    private boolean isClickable = false;


    public OlderAdsItem(Context Context, PlaceHolderView PlaceHolderView, Advert Advert){
        this.mContext = Context;
        this.mPlaceHolderView = PlaceHolderView;
        this.mAdvert = Advert;
    }

    @Resolve
    public void onResolved(){
        mEmailView.setText(String.format("Uploaded by : %s", mAdvert.getUserEmail()));
        if(mAdvert.getUserEmail().equals(Constants.ADMIN_ACC))mEmailView.setText("Uploaded by : me@myGeemail.com");
        mTargetedNumberView.setText(String.format("No. of users targeted : %d", mAdvert.getNumberOfUsersToReach()));
        mDateUploadedView.setText(String.format("Uploaded on %s", getDateFromDays(mAdvert.getDateInDays())));
        if(!mAdvert.isFlagged()){
            mUsersReachedSoFarView.setText("Users reached : "+mAdvert.getNumberOfTimesSeen());
        }else{
            mUsersReachedSoFarView.setText("Taken Down. No users reached.");
        }

        int numberOfUsersWhoDidntSeeAd = mAdvert.getNumberOfUsersToReach()- mAdvert.getNumberOfTimesSeen();
        double ammountToBeRepaid = numberOfUsersWhoDidntSeeAd*
                (mAdvert.getAmountToPayPerTargetedView()+Constants.MPESA_CHARGES);

        double vat = (mAdvert.getNumberOfUsersToReach()*Variables.getUserCpvFromTotalPayPerUser(
                mAdvert.getAmountToPayPerTargetedView())) *Constants.VAT_CONSTANT;

        double incentiveAmm = 0;
        if(mAdvert.didAdvertiserAddIncentive()){
            incentiveAmm = (mAdvert.getWebClickIncentive()* (mAdvert.getNumberOfUsersToReach()-mAdvert.getWebClickNumber()) );
        }

        double totalReimbursalPlusPayout = ammountToBeRepaid+mAdvert.getPayoutReimbursalAmount()+vat+incentiveAmm;
        String number = Double.toString(round(totalReimbursalPlusPayout));

        mAmountToReimburseView.setText(String.format("Reimbursing amount: %s Ksh", number));

        try{
            if(totalReimbursalPlusPayout==0){
                mHasBeenReimbursedView.setText("Status: All Users Reached.");
                mAmountToReimburseView.setText("Reimbursing amount:  0Ksh");
            }else{
                if (mAdvert.isHasBeenReimbursed()) {
                    mHasBeenReimbursedView.setText("Status: Reimbursed.");
                    mAmountToReimburseView.setText("Reimbursing amount:  0Ksh");
                } else {
                    mHasBeenReimbursedView.setText("Status: NOT Reimbursed.");
                    mAmountToReimburseView.setText("Reimbursing amount: " + number + "Ksh.");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        if(!mAdvert.isHasBeenReimbursed() && isCardForYesterdayAds() && totalReimbursalPlusPayout !=0 ){
//            mReimburseButton.setVisibility(android.view.View.VISIBLE);
            isClickable = true;
            addListenerForPayoutSessions();
        }else{
            isClickable = false;
            mReimburseButton.setBackgroundColor(mContext.getResources().getColor(R.color.accent));
            mReimburseButton.setText("Reimbursed");
        }
        loadListeners();
    }


    private boolean isCardForYesterdayAds(){
        return mAdvert.getDateInDays()+1 < getDateInDays();
    }

    private long getDateInDays(){
        return TimeManager.getDateInDays();
    }


    @Click(R.id.reimburseOldBtn)
    private void onClick(){
        if (isClickable) {
            Variables.isOlderAd = true;
            Variables.adToBeReimbursed = mAdvert;
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent("START_ADVERTISER_PAYOUT"));
        }
    }


    private String getDateFromDays(long days){
        long currentTimeInMills = days*(1000*60*60*24);

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
            Log.d("My_ad_stat_item","Ad was pined this year...");
            yearName = "";
        }else if(year2 == year+1){
            Log.d("My_ad_stat_item","Ad was pined last year...");
            yearName =", "+Integer.toString(year);
        }else{
            yearName =", "+ Integer.toString(year);
        }

        return dayOfMonth+" "+monthName+yearName;
    }

    private String getDate(){
        return TimeManager.getDate();
    }

    private String getMonthName_Abbr(int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month);
        SimpleDateFormat month_date = new SimpleDateFormat("MMM");
        String month_name = month_date.format(cal.getTime());
        return month_name;
    }



    private void loadListeners() {
        Query query = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CHILD_USERS)
                .child(User.getUid()).child(Constants.UPLOAD_HISTORY)
                .child(Long.toString(-(mAdvert.getDateInDays()+1))).child(mAdvert.getPushRefInAdminConsole());
        dbRef = query.getRef();
        dbRef.addChildEventListener(chil);

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForRemovingEventListeners
                ,new IntentFilter("REMOVE-LISTENERS"));
    }

    private BroadcastReceiver mMessageReceiverForRemovingEventListeners = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            dbRef.removeEventListener(chil);
            removeListenerForPayoutSessions();
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
        }
    };

    ChildEventListener chil = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d("MY_AD_STAT_ITEM","Listener from firebase has responded.Updating users reached so far");
            try{
                int newValue = dataSnapshot.getValue(int.class);
                Log.d("MY_AD_STAT_ITEM","New value gotten from firebase --"+newValue);
                mAdvert.setNumberOfTimesSeen(newValue);
                mUsersReachedSoFarView.setText("Users reached so far : "+newValue);
                int numberOfUsersWhoDidntSeeAd = mAdvert.getNumberOfUsersToReach()- newValue;
                int ammountToBeRepaid = numberOfUsersWhoDidntSeeAd*mAdvert.getAmountToPayPerTargetedView();
                double vat = (numberOfUsersWhoDidntSeeAd*(mAdvert.getAmountToPayPerTargetedView()+Constants.MPESA_CHARGES))
                        *Constants.VAT_CONSTANT;

                double incentiveAmm = 0;
                if(mAdvert.didAdvertiserAddIncentive()){
                    incentiveAmm = (mAdvert.getWebClickIncentive()* (mAdvert.getNumberOfUsersToReach()-mAdvert.getWebClickNumber()) );
                }

                String number = Integer.toString(ammountToBeRepaid+((int)vat)+((int)incentiveAmm));
                mAmountToReimburseView.setText("Reimbursing amount : "+number+" Ksh");
            }catch (Exception e){
                e.printStackTrace();
            }
            try{
                boolean newValue = dataSnapshot.getValue(boolean.class);
                Log.d("ADMIN_STAT_ITEM","New value gotten from firebase --"+newValue);
                mAdvert.setHasBeenReimbursed(newValue);
                try{
                    if(mAdvert.isHasBeenReimbursed()) {
                        mHasBeenReimbursedView.setText("Status: Reimbursed.");
                        mAmountToReimburseView.setText("Reimbursing amount:  0 Ksh");
                        if(isCardForYesterdayAds()){
                            hidePayoutButtons();
                            removeListenerForPayoutSessions();
                        }
                    }else{
                        mHasBeenReimbursedView.setText("Status: NOT Reimbursed.");
                        int numberOfUsersWhoDidntSeeAd = (mAdvert.getNumberOfUsersToReach()- mAdvert.getNumberOfTimesSeen());
                        int ammountToBeRepaid = (numberOfUsersWhoDidntSeeAd*mAdvert.getAmountToPayPerTargetedView());
                        double vat = (numberOfUsersWhoDidntSeeAd*(mAdvert.getAmountToPayPerTargetedView()+Constants.MPESA_CHARGES))
                                *Constants.VAT_CONSTANT;

                        double incentiveAmm = 0;
                        if(mAdvert.didAdvertiserAddIncentive()){
                            incentiveAmm = (mAdvert.getWebClickIncentive()* (mAdvert.getNumberOfUsersToReach()-mAdvert.getWebClickNumber()) );
                        }

                        String number = Integer.toString(ammountToBeRepaid+((int) vat)+ ((int) incentiveAmm));
                        mAmountToReimburseView.setText("Reimbursing amount : "+number+" Ksh");
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }catch (Exception e){
                e.printStackTrace();
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


    private BroadcastReceiver mMessageReceiverForHideBtnsBecauseOfPayoutSessionStart = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            hidePayoutButtons();
        }
    };

    private void addListenerForPayoutSessions(){
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForHideBtnsBecauseOfPayoutSessionStart,
                new IntentFilter(Constants.IS_MAKING_PAYOUT+"true"));
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiverForShowBtnsBecauseOfPayoutSessionStop,
                new IntentFilter(Constants.IS_MAKING_PAYOUT+"false"));
    }

    private void removeListenerForPayoutSessions(){
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForHideBtnsBecauseOfPayoutSessionStart);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMessageReceiverForShowBtnsBecauseOfPayoutSessionStop);
    }

    private BroadcastReceiver mMessageReceiverForShowBtnsBecauseOfPayoutSessionStop = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showPayoutButtons();
        }
    };

    private void hidePayoutButtons(){
        mReimburseButton.setBackgroundColor(mContext.getResources().getColor(R.color.accent));
        isClickable = false;
    }

    private void showPayoutButtons(){
        mReimburseButton.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimaryDark));
        isClickable = true;
    }

    public static double round(double value) {
        int places = 2;
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

}
