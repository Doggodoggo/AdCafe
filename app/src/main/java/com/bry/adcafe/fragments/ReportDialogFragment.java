package com.bry.adcafe.fragments;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bry.adcafe.Constants;
import com.bry.adcafe.R;
import com.bry.adcafe.Variables;
import com.bry.adcafe.models.Advert;
import com.bry.adcafe.models.User;
import com.bry.adcafe.services.TimeManager;
import com.bry.adcafe.ui.MainActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ReportDialogFragment extends DialogFragment {
    private Context mContext;
    private String mKey = "";

    public void setfragcontext(Context context){
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_report_dialog, container, false);
        Button cancelButton = (Button) rootView.findViewById(R.id.cancelButton);
        Button submitButton = (Button) rootView.findViewById(R.id.submitButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                RadioGroup surveyRadioGroup = (RadioGroup) rootView.findViewById(R.id.reportRadioGroup);
                final int selectedId = surveyRadioGroup.getCheckedRadioButtonId();
                final RadioButton selectedRadioButton = (RadioButton) rootView.findViewById((selectedId));

                Log.d("ReportDialog---",selectedRadioButton.getText().toString());
                Log.d("ReportDialog---","Ad being reported is : "+ Variables.getCurrentAdvert().getPushId());

                flagTheAd(selectedRadioButton.getText().toString());
            }
        });

        return rootView;
    }

    private void flagTheAd(String Message) {
        DatabaseReference mRef3 = FirebaseDatabase.getInstance().getReference(Constants.REPORTED_ADS)
                .child(getDate())
                .child(Variables.getCurrentAdvert().getPushRefInAdminConsole());
        DatabaseReference dbref = mRef3.push();
        dbref.setValue(Message);
        Toast.makeText(mContext,"Duly reported.",Toast.LENGTH_SHORT).show();
        dismiss();
    }



    @Override
    public void dismiss(){
        setBooleanForResumingTimer();
        super.dismiss();
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        setBooleanForResumingTimer();
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }

    private String getDate(){
        return TimeManager.getDate();
    }

    private void setBooleanForResumingTimer(){
        Log.d("ReportDialogFragment","Setting boolean for resuming timer.");
        if (!Variables.isAllClearToContinueCountDown)
            Variables.isAllClearToContinueCountDown = true;
    }


}
