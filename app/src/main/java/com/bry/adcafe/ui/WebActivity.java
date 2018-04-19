package com.bry.adcafe.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import com.bry.adcafe.R;

public class WebActivity extends AppCompatActivity {
    private WebView myWebView;
    private static final String TAG = WebActivity.class.getSimpleName();

//    ipayPayment myIpayPaymet = new ipayPayment();
////    String key = ipayPayment.hashKey;
////    String data = ipayPayment.dataString;
//
//    String generate_hmac = null;
//    public String getGenerate_hmac() {
//        try {
//            generate_hmac  = myIpayPaymet.encode(key,data);
//        }catch (Exception e){
//            e.printStackTrace();
//        }
//        return generate_hmac;
//    }



//    public WebActivity() throws Exception {
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);


//        String myGeneratedHash = getGenerate_hmac();
//        try {
//            myGeneratedHash = myIpayPaymet.encode(key,data);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        Log.d(TAG, "Generated Hmac is:"+myGeneratedHash);
//        myWebView =  findViewById(R.id.myWebView);
//        WebSettings myWebSetttings = myWebView.getSettings();
//        myWebSetttings.setJavaScriptEnabled(true);
//        myWebView.postUrl("https://payments.ipayafrica.com/v3/ke",myGeneratedHash.getBytes());
//        myWebView.setWebViewClient(new WebViewClient());
    }

    public void onBackPressed(){
        if (myWebView.canGoBack()){
            myWebView.goBack();
        }
        else {
            finish();
        }
    }
}
