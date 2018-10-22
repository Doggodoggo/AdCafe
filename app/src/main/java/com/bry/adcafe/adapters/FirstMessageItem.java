package com.bry.adcafe.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.models.Message;
import com.mindorks.placeholderview.PlaceHolderView;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

@NonReusable
@Layout(R.layout.message_item)
public class FirstMessageItem {
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
    private String message = "Hi! how can we help?";



    public FirstMessageItem(Context context,PlaceHolderView placeHolderView){
        this.mContext = context;
        this.mPlaceHolderView = placeHolderView;
    }

    @Resolve
    public void onResolved(){
       mSentMessageCard.setVisibility(android.view.View.GONE);
        mReceivedMessageTextView.setText(message);
    }


}
