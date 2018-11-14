package com.bry.adcafe.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.adapters.MessageItem;
import com.bry.adcafe.models.Message;
import com.bry.adcafe.ui.MainActivity;
import com.bry.adcafe.ui.Splash;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MessagesService extends IntentService {
    private Context mContext;
    private ChildEventListener mChil;
    DatabaseReference mRef;
    private String uid;
    private final String TAG = "MessageService";

    private NotificationManager notificationManager;
    private PendingIntent pendingIntent;
    private static int NOTIFICATION_ID = 1888;
    Notification notification;


    public MessagesService(String name) {
        super(name);
    }

    public MessagesService() {
        // Used to name the worker thread, important only for debugging.
        super("test-service");

    }

    @Override
    public void onCreate(){
        super.onCreate();
        // If a Context object is needed, call getApplicationContext() here.
        mContext = getApplicationContext();
        try{
            uid= FirebaseAuth.getInstance().getCurrentUser().getUid();
            if(mRef!=null) mRef.removeEventListener(mChil);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // This describes what will happen when service is triggered
        if(uid!=null) {
            mRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_MESSAGES)
                    .child(uid).child(Constants.MESSAGES_LIST);

            mChil = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Log.w(TAG, "Added message detected");
                    Log.e(TAG, dataSnapshot.getValue().toString());
                    try {
                        Message item = dataSnapshot.getValue(Message.class);
                        if (item.getSenderId().equals(uid)) {
                            item.setIsUsersMessage(true);
                        } else {
                            item.setIsUsersMessage(false);
                        }
                        if (!item.IsUsersMessage()) {
                            if (item.getMessageType().equals(Constants.IMAGE_MESSAGE)) {
                                setMessageImageInStorageThenAddMessageToSavedMessageList(item);
                            } else {
                                Log.w(TAG, "Detected sent message by admin. Adding message to saved message list");
                                addMessageToSavedMessagesList(item);
                            }
                        } else {
                            if (!checkIfMessageIsContained(item)) {
//                            addMessageToSavedMessagesList(item);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
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
            Log.w(TAG, "adding child event listener for detecting new messages");
            mRef.addChildEventListener(mChil);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.w(TAG,"removing child event listener for detecting new messages");
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

    private void addMessageToSavedMessagesList(Message message){
        List<Message> ourMessages = loadSavedMessages();
        ourMessages.add(message);

        Gson gson = new Gson();
        String hashMapString = gson.toJson(ourMessages);

        SharedPreferences prefs = mContext.getSharedPreferences(Constants.FIREBASE_MESSAGES, MODE_PRIVATE);
        prefs.edit().clear().putString(Constants.FIREBASE_MESSAGES, hashMapString).apply();
        notifyListenersOfNewMessages();
    }

    private void setMessageImageInStorageThenAddMessageToSavedMessageList(Message message){
//        message.setImageBitmap(decodeFromFirebaseBase64(message.getImage()));
//        message.setMessageUri(saveImageToDevice(message.getImageBitmap()));
        addMessageToSavedMessagesList(message);
    }

    public String saveImageToDevice(Bitmap imageToSave){
        String fileName = randomInt()+".jpg";
        File direct = new File(Environment.getExternalStorageDirectory() + "/AdCafePins/Media/Sent");

        if (!direct.exists()) {
            File wallpaperDirectory = new File("/sdcard/AdCafePins/Media/Sent/");
            wallpaperDirectory.mkdirs();
        }

        File file = new File(new File("/sdcard/AdCafePins/Media/Sent/"), fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    private String randomInt(){
        Random rand = new Random();
        int max = 1000000000;
        int min = 1;
        int n = rand.nextInt(max) + min;
        return Integer.toString(n);
    }

    private static Bitmap decodeFromFirebaseBase64(String image) {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        Bitmap bitm = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
        return getResizedBitmap(bitm,1200);
//        return bitm;
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

    private void notifyListenersOfNewMessages(){
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(Constants.NEW_MESSAGE_NOTIFIER_INTENT));
        showNotificationForNewMessage();
    }

    private boolean checkIfMessageIsContained(Message message){
        for(Message msg:loadSavedMessages()){
            if(message.getPushId().equals(msg.getPushId()))return true;
        }
        return false;
    }


    private void showNotificationForNewMessage(){
        if(!Variables.isDashboardActivityOnline){
            try{
                String message = "You've got a reply from the dev team.";
                Context context = mContext;
                notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                Intent mIntent = new Intent(context, Splash.class);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                Bundle bundle = new Bundle();
                bundle.putString("Test","test");
                mIntent.putExtras(bundle);
                pendingIntent = PendingIntent.getActivity(context,0,mIntent,PendingIntent.FLAG_UPDATE_CURRENT);

                Resources res = context.getResources();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                notification = new NotificationCompat.Builder(context)
//                    .setContentIntent(pendingIntent)
                        .setSmallIcon(R.drawable.ic_stat_notification2)
                        .setTicker("ticker value")
                        .setColor(mContext.getResources().getColor(R.color.colorPrimaryDark))
                        .setAutoCancel(true)
                        .setPriority(8)
                        .setContentTitle("AdCafé.")
                        .setContentText(message).build();
                notification.flags|= Notification.FLAG_AUTO_CANCEL|Notification.FLAG_SHOW_LIGHTS;
                notification.defaults|=Notification.DEFAULT_VIBRATE;
//        notification.defaults|=Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE;
                notification.ledARGB = 0xFFFFA500;
                notification.ledOnMS = 800;
                notification.ledOffMS = 1000;
                notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(NOTIFICATION_ID,notification);
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }


}
