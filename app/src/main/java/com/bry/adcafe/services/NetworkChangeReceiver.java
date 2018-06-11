package com.bry.adcafe.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.bry.adcafe.Constants;
import com.google.firebase.auth.FirebaseAuth;

import static android.content.Context.MODE_PRIVATE;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private Context mContext;

    @Override
    public void onReceive(final Context context, final Intent intent) {

        int status = NetworkUtil.getConnectivityStatusString(context);
        Log.e("NetworkChangeReceiver", "Network change has been detected");
        if (!"android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            if(status==NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){
                checkIfNeedToUpdateOnlinenessInFirebase();
            }else{
                checkIfNeedToUpdateOnlinenessInFirebase();
            }

        }
    }

    private void checkIfNeedToUpdateOnlinenessInFirebase() {
        if(!isAppOnline()){
            String uid = getLastUserId();
            if(!uid.equals("NULL")){
                Log.d("NetworkChangeReceiver","Boi");
            }
        }
    }

    private boolean isAppOnline(){
        SharedPreferences prefs2 = mContext.getSharedPreferences(Constants.ONLINE_NESS, MODE_PRIVATE);
        return prefs2.getBoolean(Constants.ONLINE_NESS, false);
    }

    private String getLastUserId(){
        SharedPreferences pref = mContext.getSharedPreferences(Constants.USER_ID,MODE_PRIVATE);
        return pref.getString(Constants.USER_ID,"NULL");
    }
}
