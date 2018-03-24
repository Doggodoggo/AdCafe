package com.bry.adcafe.adapters;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.bry.adcafe.R;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.services.TimeManager;
import com.mindorks.placeholderview.PlaceHolderView;
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

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private Advert mAdvert;

    public OlderAdsItem(Context Context, PlaceHolderView PlaceHolderView, Advert Advert){
        this.mContext = Context;
        this.mPlaceHolderView = PlaceHolderView;
        this.mAdvert = Advert;
    }

    @Resolve
    public void onResolved(){
        mEmailView.setText(String.format("Uploaded by : %s", mAdvert.getUserEmail()));
        mTargetedNumberView.setText(String.format("No. of users targeted : %d", mAdvert.getNumberOfUsersToReach()));
        mDateUploadedView.setText(String.format("Uploaded on %s", getDateFromDays(mAdvert.getDateInDays())));
        if(!mAdvert.isFlagged()){
            mUsersReachedSoFarView.setText("Users reached : "+mAdvert.getNumberOfTimesSeen());
        }else{
            mUsersReachedSoFarView.setText("Taken Down. No users reached.");
        }

        int numberOfUsersWhoDidntSeeAd = mAdvert.getNumberOfUsersToReach()- mAdvert.getNumberOfTimesSeen();
        int amountToBeRepaid = (int)(numberOfUsersWhoDidntSeeAd*mAdvert.getAmountToPayPerTargetedView());
        String number = Integer.toString(amountToBeRepaid);

        mAmountToReimburseView.setText(String.format("Reimbursing amount: %s Ksh", number));

        try{
            if(amountToBeRepaid==0){
                mHasBeenReimbursedView.setText("Status: All Users Reached.");
                mAmountToReimburseView.setText("Reimbursing amount:  0 Ksh");
            }else{
                if (mAdvert.isHasBeenReimbursed()) {
                    mHasBeenReimbursedView.setText("Status: Reimbursed.");
                    mAmountToReimburseView.setText("Reimbursing amount:  0 Ksh");
                } else {
                    mHasBeenReimbursedView.setText("Status: NOT Reimbursed.");
                    mAmountToReimburseView.setText("Reimbursing amount: " + number + " Ksh.");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private boolean isCardForYesterdayAds(){
        return mAdvert.getDateInDays()+1 < getDateInDays();
    }

    private long getDateInDays(){
        return TimeManager.getDateInDays();
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

}
