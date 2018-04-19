package com.bry.adcafe.Payment.ipay;

import android.os.Handler;

import com.bry.adcafe.Constants;
import com.bry.adcafe.Variables;
import com.bry.adcafe.services.ipayservice;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ipayPayment {

    public  int live = 1;
    public  String oid = "";
    public  String inv = "";
    public  int amount = 0;
    public int payoutAmount = 0;
    public  String tel = "";
    public String payoutPhone = "";
    public  String eml = "";
    public  String vid = "ctl";
    public  String curr = "KES";
    public  String p1 = "";
    public  String p2 = "";
    public  String p3 = "";
    public  String p4 = "";
    public  String cbk = "";
    public  int cst = 1;
    public  int crl = 2;
    public  String hash = "Tech2548gtRV365";
    public  String cardno = Variables.cardNumber;
    public  String cvv = Variables.cvv;
    public  String month = Variables.expirationMonth;
    public  String year = Variables.expirationYear;
    public  String cust_address = Variables.postalCode;
    public  String cust_city = Variables.cardHolderState;
    public  String cust_country = "Kenya";
    public  String postcode = Variables.postalCode;
    public  String stateprov = Variables.cardHolderState;
    public  String fname = Variables.cardHolderFirstName;
    public  String lname = Variables.cardHolderLastName;
    public String payoutReference = "";


    private boolean isConfirmingPayments = false;
    private Handler h = new Handler();
    private Runnable r;
    private boolean isStoppingChecker = false;
    private final int delayMills = 3000;

    public  final String hashKey = "Tech2548gtRV365";
    public  final String dataString  = live+oid+inv+amount+tel+eml+vid+curr+p1+p2+p3+p4+cbk+cst;
    public  final String carddataString = vid+cardno+cvv+month+year+cust_address+cust_city+cust_country+postcode+stateprov+fname+lname;

    public void makeMpesaPayment(){
        final ipayservice ipayService = new ipayservice();
        ipayService.setMpesaValues(tel,eml,oid,amount);
        ipayService.makeipaympesapayment();
    }


    public void makeCardPayment(){
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.PAY_POOL);
        DatabaseReference pushRef = adRef.push();
        String mTransactionId = pushRef.getKey();
        String newTransactionId = ""+mTransactionId.substring(1);

        final ipayservice ipayservice = new ipayservice();
        ipayservice.setMpesaValues(tel,eml,newTransactionId,amount);
        ipayservice.setCardValues(amount,tel,eml,cardno,cvv,month,year,"00200",
               cust_city,cust_country,postcode,stateprov,fname,lname,newTransactionId);
        ipayservice.makeipayCardPayment();
    }
    public void makePayout(){
        DatabaseReference adRef = FirebaseDatabase.getInstance().getReference(Constants.PAY_POOL);
        DatabaseReference pushRef = adRef.push();
        String mTransactionId = pushRef.getKey();

        final ipayservice ipayService = new ipayservice();
        ipayService.setPayoutValues(mTransactionId,payoutPhone,payoutAmount);
        ipayService.makeIpayPayouts();
    }


    public void setMpesaValues(String phone,String email,String transactionid, int amount){
        this.tel = phone;
        this.eml = email;
        this.oid = transactionid;
        this.amount = amount;
    }


    public void setCardValues(int amount,String phone,String email,String cardNo, String cvv, String month,String year, String address,String city,String country,String
                              postcode,String stateProv,String firstName,String lastName,String transactionId){
        this.amount = amount;
        this.tel =  phone;
        this.eml= email;
        this.cardno = cardNo;
        this.cvv = cvv;
        this.month = month;
        this.year = year;
        this.cust_address = address;
        this.cust_city = city;
        this.cust_country = country;
        this.postcode = postcode;
        this.stateprov = stateProv;
        this.fname = firstName;
        this.lname = lastName;
        this.oid = transactionId;
        this.inv = transactionId;
    }
    public void setPayoutValues(String myReference, String myPhone, int myAmount){
        this.payoutAmount = myAmount;
        this.payoutPhone = myPhone;
        this.payoutReference = myReference;

    }



    public void stopRecursiveChecker(){
        isStoppingChecker = true;
        try{
            h.removeCallbacks(r);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

