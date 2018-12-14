package com.bry.adcafe.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.models.Message;
import com.bry.adcafe.models.MyTime;
import com.bry.adcafe.services.TimeManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.function.UnaryOperator;

import static android.content.Context.MODE_PRIVATE;

@NonReusable
@Layout(R.layout.message_item)
public class MessageItem {
    private final String TAG = "MessageItem";
    @View(R.id.SentMessageCard) private CardView mSentMessageCard;
    @View(R.id.sentMessageTextView) private TextView mSentMessageTextView;
    @View(R.id.SentMessageImage) private ImageView mSentMessageImage;

    @View(R.id.ReceivedMessageCard) private CardView mReceivedMessageCard;
    @View(R.id.receivedMessageTextView) private TextView mReceivedMessageTextView;
    @View(R.id.ReceivedMessageImage) private ImageView mReceivedMessageImage;

    @View(R.id.notSentLayout) private LinearLayout mNotSentLayout;
    @View(R.id.sendingText) private TextView mSendingText;
    @View(R.id.mSentImage) private ImageView mSentImage;

    @View(R.id.progBar) private ProgressBar mProgBar;

    private Context mContext;
    private PlaceHolderView mPlaceHolderView;
    private Message mMessage;
    private boolean isToSend = false;



    public MessageItem(Context context,PlaceHolderView placeHolderView, Message message, boolean isToSend){
        this.mContext = context;
        this.mMessage = message;
        this.mPlaceHolderView = placeHolderView;
        this.isToSend = isToSend;
    }

    @Resolve
    public void onResolved(){
        if(mMessage.IsUsersMessage()&& !mMessage.HasBeenSent() && isToSend){
            if(mMessage.getMessageType().equals(Constants.IMAGE_MESSAGE)){
                sendImageMessageInFirebase();
            }else{
                sendMessageInFirebase();
            }
        }

        if(mMessage.IsUsersMessage()){
            //message was made by user
            setViewAsSentMessage();
        }else{
            //message was by admin
            setViewAsReceivedMessage();
        }

    }

    private void setViewAsReceivedMessage() {
        mSentMessageCard.setVisibility(android.view.View.GONE);
        if(mMessage.getMessageType().equals(Constants.IMAGE_MESSAGE)){
            setReceivedImageMessage();
        }else{
            mReceivedMessageTextView.setText(mMessage.getMessageString());
        }
    }


    private void setViewAsSentMessage() {
        mReceivedMessageCard.setVisibility(android.view.View.GONE);
        if(mMessage.getMessageType().equals(Constants.IMAGE_MESSAGE)){
            setSentImageMessage();
        }else{
            mSentMessageTextView.setText(mMessage.getMessageString());
        }
    }

    private void setReceivedImageMessage() {
        mReceivedMessageTextView.setVisibility(android.view.View.GONE);
        mReceivedMessageImage.setVisibility(android.view.View.VISIBLE);
        Bitmap bitmap = loadImageFromMemory(mMessage.getMessageUri());

        if(bitmap == null){
            loadImageFromFirebase();
        }else{
            Bitmap imageBitmap = getResizedBitmap(bitmap,1000);
            mReceivedMessageImage.setImageBitmap(imageBitmap);
        }
    }



    private void setSentImageMessage() {
        mSentMessageTextView.setVisibility(android.view.View.GONE);
        mSentMessageImage.setVisibility(android.view.View.VISIBLE);
        Bitmap bitmap = loadImageFromMemory(mMessage.getMessageUri());
        Bitmap imageBitmap = getResizedBitmap(bitmap,1000);
        mSentMessageImage.setImageBitmap(imageBitmap);
    }

    private Bitmap loadImageFromMemory(String imagePath){
        File path = new File(Environment.getExternalStorageDirectory(),"/AdCafePins/Media/Sent");
        try{
            return BitmapFactory.decodeFile(path.getPath()+"/"+imagePath);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
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

    private void sendImageMessageInFirebase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_MESSAGES)
                .child(uid).child(Constants.MESSAGES_LIST);
        DatabaseReference dbRef = mRef.push();
        mMessage.setPushId(dbRef.getKey());

        mSendingText.setVisibility(android.view.View.VISIBLE);
        if(mMessage.getImageBitmap()==null){
            loadImagesBitmapFirst();
        }
        if(mMessage.getImageBitmap()==null){
            Toast.makeText(mContext,"Something went wrong",Toast.LENGTH_SHORT).show();
            return;
        }
        mMessage.setMessageUri(saveImageToDevice(mMessage.getImageBitmap()));

        setImageDataInSeparatePartFirebase(mMessage.getImageBitmap(),mMessage.getPushId());

        mMessage.setImage(dbRef.getKey());
        mMessage.setImageBitmap(null);
        mMessage.setIsUsersMessage(true);
        mMessage.setTimeStamp(new MyTime(TimeManager.getCal()));

        dbRef.setValue(mMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                mMessage.setHasBeenSent(true);
                addMessageToSavedMessagesList(mMessage);
                mSendingText.setVisibility(android.view.View.GONE);
                if(mMessage.getMessageType().equals(Constants.TEXT_MESSAGE)) mSentImage.setVisibility(android.view.View.VISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(mContext,"Message not Sent.",Toast.LENGTH_SHORT).show();
                mNotSentLayout.setVisibility(android.view.View.VISIBLE);
                mSendingText.setVisibility(android.view.View.GONE);
            }
        });
    }

    private void setImageDataInSeparatePartFirebase(Bitmap img,String pushId) {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_IMAGE_MESSAGES)
                .child(pushId);
        String image = encodeBitmapForFirebaseStorage(img);
        mRef.setValue(image);
    }

    private void loadImageFromFirebase(){
        mProgBar.setVisibility(android.view.View.VISIBLE);
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_IMAGE_MESSAGES)
                .child(mMessage.getPushId());
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String image = dataSnapshot.getValue(String.class);
                    mMessage.setImageBitmap(decodeFromFirebaseBase64(image));
                    mMessage.setMessageUri(saveImageToDevice(mMessage.getImageBitmap()));
                    updateMessageUri(mMessage.getMessageUri());
                    mReceivedMessageImage.setImageBitmap(mMessage.getImageBitmap());
                    mProgBar.setVisibility(android.view.View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadImagesBitmapFirst(){
        Bitmap bitmap = loadImageFromMemory(mMessage.getMessageUri());
        Bitmap imageBitmap = getResizedBitmap(bitmap,1000);
        mMessage.setImageBitmap(imageBitmap);

    }

    private void sendMessageInFirebase(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_MESSAGES)
                .child(uid).child(Constants.MESSAGES_LIST);
        DatabaseReference dbRef = mRef.push();
        mMessage.setPushId(dbRef.getKey());
        mMessage.setIsUsersMessage(true);
        mMessage.setTimeStamp(new MyTime(TimeManager.getCal()));

        mSendingText.setVisibility(android.view.View.VISIBLE);
        dbRef.setValue(mMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                try{
                    mMessage.setHasBeenSent(true);
                    addMessageToSavedMessagesList(mMessage);
                    mSendingText.setVisibility(android.view.View.GONE);
                    mSentImage.setVisibility(android.view.View.VISIBLE);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                try{
                    Toast.makeText(mContext,"Message not Sent.",Toast.LENGTH_SHORT).show();
                    mNotSentLayout.setVisibility(android.view.View.VISIBLE);
                    mSendingText.setVisibility(android.view.View.GONE);
                }catch (Exception e2){
                    e.printStackTrace();
                }

            }
        });
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
        message.setImage("");
        ourMessages.add(message);

        Gson gson = new Gson();
        String hashMapString = gson.toJson(ourMessages);

        SharedPreferences prefs = mContext.getSharedPreferences(Constants.FIREBASE_MESSAGES, MODE_PRIVATE);
        prefs.edit().clear().putString(Constants.FIREBASE_MESSAGES, hashMapString).apply();
    }



    @Click(R.id.SentMessageCard)
    private void onClick(){
        if(mMessage.IsUsersMessage() && !mMessage.HasBeenSent()){
            updatePosition();
        }
    }

    private void updatePosition(){
        mPlaceHolderView.removeView(this);
        mPlaceHolderView.addView(0,new MessageItem(mContext,mPlaceHolderView,mMessage,false));
    }

    private String encodeBitmapForFirebaseStorage(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,80,baos);
        String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        return imageEncoded;
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


    private static Bitmap decodeFromFirebaseBase64(String image) {
        byte[] decodedByteArray = Base64.decode(image, Base64.DEFAULT);
        Bitmap bitm = BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
        return getResizedBitmap(bitm,1200);
//        return bitm;
    }

    private void updateMessageUri(String uri){
        List<Message> ourMessages = loadSavedMessages();
        int iterations = 0;
        for(Message msg:ourMessages){
            if(msg.getPushId().equals(mMessage.getPushId())){
                ourMessages.get(iterations).setMessageUri(uri);
                break;
            }
            iterations++;
        }

        Gson gson = new Gson();
        String hashMapString = gson.toJson(ourMessages);

        SharedPreferences prefs = mContext.getSharedPreferences(Constants.FIREBASE_MESSAGES, MODE_PRIVATE);
        prefs.edit().clear().putString(Constants.FIREBASE_MESSAGES, hashMapString).apply();
    }



}
