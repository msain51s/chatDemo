package com.blikoon.rooster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class UpdateProfile extends AppCompatActivity implements View.OnClickListener{

    public static final String TAG="UpdateProfile";
    private EditText name,email,mobileNo;
    private Button updateBtn;
    private BroadcastReceiver mLoadProfileBroadcastReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Edit Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    /*UI INITIALISATION*/
        name= (EditText) findViewById(R.id.userNameTxt);
        email= (EditText) findViewById(R.id.userNameEmailTxt);
        mobileNo= (EditText) findViewById(R.id.userNameMobileNoTxt);
        updateBtn= (Button) findViewById(R.id.update_button);

        updateBtn.setOnClickListener(this);

        loadProfileData();
    }

  public void attemptUpdateInfo(){
      String nameValue=name.getText().toString();
      String emailValue=email.getText().toString();
      String mobileNoValue=mobileNo.getText().toString();
      
      View focusView = null;
      boolean cancel=false;
      
      if(TextUtils.isEmpty(nameValue)){
          name.setError("This field is required !!!");
          focusView=name;
          cancel=true;
      }
      
      if(cancel){
          focusView.requestFocus();
      }else {
      //    Utils.showLoader(UpdateProfile.this);
          updateProfile(nameValue,emailValue,mobileNoValue);
      }
  }

    private void updateProfile(String nameValue,String emailValue,String mobileNoValue) {
        Log.d(TAG, "saveCredentialsAndLogin() called.");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit()
                .putString("xmpp_name", nameValue)
                .putString("xmpp_email", emailValue)
                .commit();

        if (RoosterConnectionService.getState().equals(RoosterConnection.ConnectionState.CONNECTED)) {
            Log.d(TAG, "The client is connected to the server,Update User Info");
            //Send the message to the server

            Intent intent = new Intent(RoosterConnectionService.UPDATE_USER_PROFILE_DATA);
            intent.putExtra(RoosterConnectionService.VCARD_NAME,
                    nameValue);
            intent.putExtra(RoosterConnectionService.VCARD_EMAIL, emailValue);
            if (mobileNoValue != null)
                intent.putExtra(RoosterConnectionService.VCARD_MOBILE_NO, mobileNoValue);

            sendBroadcast(intent);

           Intent i=new Intent(UpdateProfile.this,ProfileActivity.class);
            i.putExtra("action","refresh");
            startActivity(i);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();  return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onResume() {
        super.onResume();
        mLoadProfileBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                switch (action)
                {
                    case RoosterConnectionService.GET_USER_PROFILE_DATA:
                        Log.d(TAG,"Got a broadcast to show the main app window");
                        //Show the main app window
                        String nickName=intent.getStringExtra(RoosterConnectionService.VCARD_NAME);
                        String emailAddress= intent.getStringExtra(RoosterConnectionService.VCARD_EMAIL);

                        if(nickName!=null)
                            name.setText(nickName);
                        if(emailAddress!=null)
                            email.setText(emailAddress);
                        //     userName.setText(userNameValue);
                        Utils.dismissLoader();

                        /*Log.d("userName",intent.getStringExtra(RoosterConnectionService.VCARD_USERNAME));
                        Log.d("NickName",intent.getStringExtra(RoosterConnectionService.VCARD_NAME));
                        Log.d("Email",intent.getStringExtra(RoosterConnectionService.VCARD_EMAIL));*/
                }

            }
        };
        IntentFilter filter = new IntentFilter(RoosterConnectionService.GET_USER_PROFILE_DATA);
        this.registerReceiver(mLoadProfileBroadcastReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mLoadProfileBroadcastReceiver);
    }

    public void loadProfileData(){
        if (RoosterConnectionService.getState().equals(RoosterConnection.ConnectionState.CONNECTED)) {
            Log.d(TAG, "The client is connected to the server,Update User Info");
            //Send the message to the server

            Intent intent = new Intent(RoosterConnectionService.LOAD_VCARD);
            sendBroadcast(intent);

        }
    }
        @Override
    public void onClick(View v) {
        if(v==updateBtn)
            attemptUpdateInfo();
    }
}
