package com.blikoon.rooster;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;

/**
 * Created by gakwaya on 4/28/2016.
 */
public class RoosterConnectionService extends Service {
    private static final String TAG ="RoosterService";

    public static final String UI_AUTHENTICATED = "com.blikoon.rooster.uiauthenticated";
    public static final String SEND_MESSAGE = "sendMessage";
    public static final String SEND_IMAGE = "sendImage";
    public static final String CHAT_STATUS = "chatStatus";
    public static final String BUNDLE_MESSAGE_BODY = "b_body";
    public static final String BUNDLE_TO = "b_to";
    public static final String FRIEND_LIST_GET = "freindList_get";
    public static final String FRIEND_LIST_RECEIVE = "freindList_receive";
    public static final String FRIEND_LIST = "freindList";
    public static final String ADD_FRIEND = "addFriend";
    public static final String ADD_FRIEND_NAME = "name";
    public static final String ADD_FRIEND_MOBILE_NO = "mobileNo";

    public static final String NEW_MESSAGE = "com.blikoon.rooster.newmessage";
    public static final String NEW_IMAGE = "new_image";
    public static final String NEW_FILE = "new_file";
    public static final String IMAGE_BITMAP = "image_btimap";
    public static final String BUNDLE_FROM_JID = "b_from";
    public static final String BUNDLE_TO_JID = "b_to";
    public static final String DOMAIN_NAME = "@dell29";
    public static final String BUDDYLIST_UPDATE = "buddy_list_update";
    public static final String CHAT_HISTORY_LIST_REFRESH = "chat_history_list_refresh";
    public static final String CHAT_MESSAGE_READ_INDICATOR = "messageRead_event";

    public static final String GET_USER_PROFILE_DATA = "user_profile_data";
    public static final String LOAD_VCARD = "load_vcard";
    public static final String VCARD_USERNAME = "vcard_username";
    public static final String VCARD_NAME = "vcard_name";
    public static final String VCARD_EMAIL = "vcard_email";
    public static final String VCARD_PASSWORD = "vcard_password";
    public static final String VCARD_PROFILE_IMAGE = "vcard_profile_image";
    public static final String VCARD_MOBILE_NO = "vcard_mobileNo";
    public static final String UPDATE_USER_PROFILE_DATA = "update_user_profile_data";
    public static final String MESSAGE_PACKET_ID = "message_receipt_id";
    public static final String UPLOAD_USER_PROFILE_IMAGE = "upload_profile_image";
    public static final String GET_USER_LAST_SEEN = "get_user_last_seen";

    public static final String USER_JID = "user_jid";

    public static RoosterConnection.ConnectionState sConnectionState;
    public static RoosterConnection.LoggedInState sLoggedInState;
    private boolean mActive;//Stores whether or not the thread is active
    private Thread mThread;
    private Handler mTHandler;//We use this handler to post messages to
    //the background thread.
    private RoosterConnection mConnection;

    public RoosterConnectionService() {

    }
    public static RoosterConnection.ConnectionState getState()
    {
        if (sConnectionState == null)
        {
            return RoosterConnection.ConnectionState.DISCONNECTED;
        }
        return sConnectionState;
    }

    public static RoosterConnection.LoggedInState getLoggedInState()
    {
        if (sLoggedInState == null)
        {
            return RoosterConnection.LoggedInState.LOGGED_OUT;
        }
        return sLoggedInState;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate()");
    }

    private void initConnection()
    {
        Log.d(TAG,"initConnection()");
        if( mConnection == null)
        {
            mConnection = new RoosterConnection(this);
        }
        try
        {
            mConnection.connect();

        }catch (IOException |SmackException |XMPPException e)
        {
            Log.d(TAG,"Something went wrong while connecting ,make sure the credentials are right and try again");
            e.printStackTrace();
            //Stop the service all together.
            stopSelf();
        }

    }


    public void start()
    {
        Log.d(TAG," Service Start() function called.");
        if(!mActive)
        {
            mActive = true;
            if( mThread ==null || !mThread.isAlive())
            {
                mThread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Looper.prepare();
                        mTHandler = new Handler();
                        initConnection();
                        //THE CODE HERE RUNS IN A BACKGROUND THREAD.
                        Looper.loop();

                    }
                });
                mThread.start();
            }


        }

    }

    public void stop()
    {
        Log.d(TAG,"stop()");
        mActive = false;
        mThread=null;
        mTHandler.post(new Runnable() {
            @Override
            public void run() {
                if( mConnection != null)
                {
                    mConnection.disconnect();
                }
            }
        });

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand()");
        start();
        return Service.START_NOT_STICKY;
        //RETURNING START_STICKY CAUSES OUR CODE TO STICK AROUND WHEN THE APP ACTIVITY HAS DIED.
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy()");
        super.onDestroy();
        stop();
    }
}
