package com.blikoon.rooster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class ChatSplash extends AppCompatActivity {
    private static String TAG="ChatSplashActivity";
    private BroadcastReceiver mBroadcastReceiver;
    private Context mContext;
    boolean isLoggedIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_splash);
        mContext=this;
        isLoggedIn = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getBoolean("xmpp_logged_in",false);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
               /*IF USER IS AUTHENTICATED THE GO FORWARD WITHOUT LOGIN AGAIN*/
                Intent intent=null;
                if(RoosterConnectionService.getState().equals(RoosterConnection.ConnectionState.CONNECTED)){
                    Log.d(TAG,"state connected");
                    Intent i2 = new Intent(ChatSplash.this,ContactListActivity.class);
                    startActivity(i2);
                    finish();
                }
                else if(isLoggedIn){
                    intent=new Intent(mContext,RoosterConnectionService.class);
                    startService(intent);
                    Log.d(TAG,"state connected");
                }
                else{
                    intent=new Intent(mContext,LoginActivity.class);
                    startActivity(intent);
                }
            }
        },3000);

    }

    @Override
    protected void onResume() {
        super.onResume();

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                switch (action)
                {
                    case RoosterConnectionService.UI_AUTHENTICATED:
                        Log.d(TAG,"Got a broadcast to show the main app window");
                        //Show the main app window
                        Utils.dismissLoader();
                        Intent i2 = new Intent(mContext,ContactListActivity.class);
                        startActivity(i2);
                        finish();
                        break;
                }

            }
        };
        IntentFilter filter = new IntentFilter(RoosterConnectionService.UI_AUTHENTICATED);
        this.registerReceiver(mBroadcastReceiver, filter);


    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }
}
