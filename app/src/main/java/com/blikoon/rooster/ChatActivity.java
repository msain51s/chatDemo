package com.blikoon.rooster;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blikoon.rooster.db.ChatMessage;
import com.blikoon.rooster.db.ChatView;
import com.blikoon.rooster.db.DB_Handler;
import com.bumptech.glide.util.Util;
import com.example.drawingdemo.ImageEditingActivity;
import com.glidebitmappool.GlideBitmapFactory;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import co.devcenter.androiduilibrary.ChatViewEventListener;
import co.devcenter.androiduilibrary.SendButton;
import nl.changer.polypicker.ImagePickerActivity;
import siclo.com.ezphotopicker.api.EZPhotoPick;
import siclo.com.ezphotopicker.api.EZPhotoPickStorage;
import siclo.com.ezphotopicker.api.models.EZPhotoPickConfig;
import siclo.com.ezphotopicker.api.models.PhotoSource;


public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private static final String DEMO_PHOTO_PATH = "MyDemoPhotoDir";
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1, SELECT_DOCUMENT = 2;
    private String userChoosenTask;


    private String contactJid;
    private ChatView mChatView;
    private SendButton mSendButton;
    private BroadcastReceiver mBroadcastReceiver, mImageBroadcastReceiver,
            mChatStatusBroadcastReceiver, mFileBroadcastReceiver, mRefreshChatHistoryListBroadcastReceiver;

    TextView titleText, statusText;
    ImageView attachment, userImageView;
    String filePath, mUserName;
    String edited = "";
    DB_Handler dbHandler;

    /*FOR MULTIPLE IMAGE SELECTION WITH EDITOR*/
    private static final int INTENT_REQUEST_GET_N_IMAGES = 14;
    int sLimit = 8;
    int tLimit = 8;
    HashSet<Uri> mMedia = new HashSet<>();
    ArrayList<String> imageModelArrayList = new ArrayList<>();
    int size = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dbHandler = new DB_Handler(this);
        mUserName = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("xmpp_jid", null).split("@")[0];

        userImageView = (ImageView) findViewById(R.id.userImageView);
        attachment = (ImageView) findViewById(R.id.attachment);
        titleText = (TextView) findViewById(R.id.friendTitle);
        statusText = (TextView) findViewById(R.id.statusText);
        mChatView = (ChatView) findViewById(R.id.rooster_chat_view);
        mChatView.setEventListener(new ChatViewEventListener() {
            @Override
            public void userIsTyping() {
                //Here you know that the user is typing
                //    statusText.setText("Typing");
                if (RoosterConnectionService.getState().equals(RoosterConnection.ConnectionState.CONNECTED)) {
                    Log.d(TAG, "The client is connected to the server,Sendint Message");
                    //Send the message to the server

                    Intent intent = new Intent(RoosterConnectionService.SEND_MESSAGE);
                    intent.putExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY,
                            "");
                    intent.putExtra(RoosterConnectionService.BUNDLE_TO,/*"chandan@dell29"*/contactJid);
                    intent.putExtra("filePath", "");
                    intent.putExtra("chatState", "composing");

                    sendBroadcast(intent);

                }
            }

            @Override
            public void userHasStoppedTyping() {
                //Here you know that the user has stopped typing.
                statusText.setText("");

                if (RoosterConnectionService.getState().equals(RoosterConnection.ConnectionState.CONNECTED)) {
                    Log.d(TAG, "The client is connected to the server,Sendint Message");
                    //Send the message to the server

                    Intent intent = new Intent(RoosterConnectionService.SEND_MESSAGE);
                    intent.putExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY,
                            "");
                    intent.putExtra(RoosterConnectionService.BUNDLE_TO,/*"chandan@dell29"*/contactJid);
                    intent.putExtra("filePath", "");
                    intent.putExtra("chatState", "pause");

                    sendBroadcast(intent);

                }
            }
        });

        mSendButton = mChatView.getSendButton();
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Only send the message if the client is connected
                //to the server.

                if (RoosterConnectionService.getState().equals(RoosterConnection.ConnectionState.CONNECTED)) {
                    Log.d(TAG, "The client is connected to the server,Sending Message");
                    //Send the message to the server
                    if (mChatView.getTypedString() != null && !mChatView.getTypedString().trim().equalsIgnoreCase("")) {
                        Intent intent = new Intent(RoosterConnectionService.SEND_MESSAGE);
                        intent.putExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY,
                                mChatView.getTypedString());
                        intent.putExtra(RoosterConnectionService.BUNDLE_TO,/*"chandan@dell29"*/contactJid);
                        intent.putExtra("filePath", "");
                        intent.putExtra("chatState", "active");

                        sendBroadcast(intent);

                        //Update the chat view.
                        mChatView.sendMessage(mChatView.getTypedString(), "", null, 0, System.currentTimeMillis(),null);
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Client not connected to server ,Message not sent!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        final Intent intent = getIntent();
        contactJid = /*"chandan@dell29"*/intent.getStringExtra("EXTRA_CONTACT_JID");
        titleText.setText(Utils.capitalize(contactJid.split("@")[0]));

        if (AppController.friendList.get(intent.getIntExtra("POSITION", 0)).getImageData() != null) {
            userImageView.setImageBitmap(BitmapFactory.decodeByteArray(AppController.friendList.get(intent.getIntExtra("POSITION", 0)).getImageData(),
                    0, AppController.friendList.get(intent.getIntExtra("POSITION", 0)).getImageData().length));
        }

        userImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppController.friendList.get(intent.getIntExtra("POSITION", 0)).getImageData() != null) {
                    Utils.showFullImageDialog(ChatActivity.this, BitmapFactory.decodeByteArray(AppController.friendList.get(intent.getIntExtra("POSITION", 0)).getImageData(),
                            0, AppController.friendList.get(intent.getIntExtra("POSITION", 0)).getImageData().length));
                } else
                    Toast.makeText(ChatActivity.this, "Image Not Available", Toast.LENGTH_LONG).show();
            }
        });
 /* SEND ACTIVE STATUS WHEN USER OPEN CHAT SCREEN */
        sendActiveStatusFirstTime();

    /*LOAD CHAT HISTORY AT STARTING*/
        loadChatHistory();

    /*UNREAD MESSAGE SET AS READ*/
        dbHandler.updateReadMessageCountFlag(mUserName, contactJid.split("@")[0]);

  /*HIT REQUEST FOR USER LAST SEEN TIME*/
         getUserLastSeenTime(contactJid);

    }

    public void performAttachment(View view) {

        selectImage();
    }

    public void sendActiveStatusFirstTime() {
        if (RoosterConnectionService.getState().equals(RoosterConnection.ConnectionState.CONNECTED)) {
            Log.d(TAG, "The client is connected to the server,Sending Message");
            //Send the message to the server

            Intent intent = new Intent(RoosterConnectionService.SEND_MESSAGE);
            intent.putExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY,
                    "");
            intent.putExtra(RoosterConnectionService.BUNDLE_TO,/*"chandan@dell29"*/contactJid);
            intent.putExtra("filePath", "");
            intent.putExtra("chatState", "active");

            sendBroadcast(intent);

        } else {
            Toast.makeText(getApplicationContext(),
                    "Client not connected to server ,Message not sent!",
                    Toast.LENGTH_LONG).show();
        }
    }

/*LOAD CHAT HISTORY*/

    public void loadChatHistory() {
      runOnUiThread(new Runnable() {
          @Override
          public void run() {

        String jid = PreferenceManager.getDefaultSharedPreferences(ChatActivity.this)
                .getString("xmpp_jid", null);
        ArrayList<ChatMessage> list = dbHandler.getChatHistory(jid.split("@")[0], contactJid.split("@")[0]);
        String fileName = null;
        String fileArr[];
        Bitmap imageBitmap = null;
        mChatView.clearChatView();
        for (ChatMessage message : list) {

            if (message.getStatusFlag() == 0) {

                try {
                    if (/*fileName*/message.getImageFileName() != null) {
                   //     imageBitmap = Utils.loadSampledImage(message.getImageFileName());
                        mChatView.sendMessage(message.getMessage(), message.getUsername(), imageBitmap, message.getDeliveryStatus(), message.getTimestamp(),message.getImageFileName());
                    } else if (message.getDocumentFileName() != null) {
                        mChatView.sendMessage(message.getMessage(), message.getUsername(), null, message.getDocumentFileName(), message.getDeliveryStatus(), message.getTimestamp());
                    } else {
                        mChatView.sendMessage(message.getMessage(), message.getUsername(), message.getImageBitmap(), message.getDeliveryStatus(), message.getTimestamp(),message.getImageFileName());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                try {
                    if (message.getImageFileName() != null) {
               //         imageBitmap = Utils.loadSampledImage(message.getImageFileName());
                        mChatView.receiveMessage(message.getMessage(), message.getUsername(), imageBitmap, message.getDeliveryStatus(), message.getTimestamp(),message.getImageFileName());
                    } else if (message.getDocumentFileName() != null) {
                        mChatView.receiveMessage(message.getMessage(), message.getUsername(), message.getImageBitmap(), message.getDocumentFileName(), message.getTimestamp());
                    } else {
                        mChatView.receiveMessage(message.getMessage(), message.getUsername(), message.getImageBitmap(), message.getDeliveryStatus(), message.getTimestamp(),message.getImageFileName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
          }
      });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void sendLeaveConversationStatus() {
        if (RoosterConnectionService.getState().equals(RoosterConnection.ConnectionState.CONNECTED)) {
            Log.d(TAG, "The client is connected to the server,Sending Message");
            //Send the message to the server

            Intent intent = new Intent(RoosterConnectionService.SEND_MESSAGE);
            intent.putExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY,
                    "");
            intent.putExtra(RoosterConnectionService.BUNDLE_TO,/*"chandan@dell29"*/contactJid);
            intent.putExtra("filePath", "");
            intent.putExtra("chatState", "left");

            sendBroadcast(intent);

        } else {
            Toast.makeText(getApplicationContext(),
                    "Client not connected to server ,Message not sent!",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void sendMessageSeenIndication() {
        String jid = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("xmpp_jid", null);
        ArrayList<String> receiptIdsList = dbHandler.getDeliveredMessageReceiptIds(jid.split("@")[0], contactJid.split("@")[0]);

        for (String id : receiptIdsList) {
            dbHandler.updateMessageDeliveryStatus(jid.split("@")[0], contactJid.split("@")[0], id, 2);
        }

        ArrayList<ChatMessage> updatedList = dbHandler.getChatHistory(jid.split("@")[0], contactJid.split("@")[0]);
        mChatView.refreshChatView(updatedList);
    /*  Intent intent1 = new Intent(RoosterConnectionService.CHAT_HISTORY_LIST_REFRESH);
      sendBroadcast(intent1);*/
    }

    public void getUserLastSeenTime(String userJid) {

     Intent intent1 = new Intent(RoosterConnectionService.GET_USER_LAST_SEEN);
        intent1.putExtra(RoosterConnectionService.USER_JID,userJid);
      sendBroadcast(intent1);
    }

   /*  @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1){
            if (resultCode == RESULT_OK){
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null){
                    filePath=selectedImageUri.uri.getPath() ;//getRealPathFromURI(selectedImageUri);

                    if (RoosterConnectionService.getState().equals(RoosterConnection.ConnectionState.CONNECTED)) {
                        Log.d(TAG, "The client is connected to the server,Sending Image");
                        //Send the message to the server

                        Intent intent = new Intent(RoosterConnectionService.SEND_IMAGE);
                        intent.putExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY,
                                "");
                        intent.putExtra(RoosterConnectionService.BUNDLE_TO,*//**//*"chandan@dell29"*//**//*contactJid);
                        intent.putExtra("filePath",filePath);

                        sendBroadcast(intent);

                        //Update the chat view.
                        Bitmap bmp = BitmapFactory.decodeFile(filePath);
                        mChatView.sendMessage("",contactJid.split("@")[0],bmp);
                //        mChatView.sendMessage();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Client not connected to server ,Image not sent!",
                                Toast.LENGTH_LONG).show();
                    }
                }else{
                    //URI IS NULL
                }
            }
        }

       super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == EZPhotoPick.PHOTO_PICK_GALERY_REQUEST_CODE || requestCode == EZPhotoPick.PHOTO_PICK_CAMERA_REQUEST_CODE) {
            EZPhotoPickStorage eps=new EZPhotoPickStorage(this);
            String photoName = data.getStringExtra(EZPhotoPick.PICKED_PHOTO_NAME_KEY);
            filePath = eps.getAbsolutePathOfStoredPhoto(DEMO_PHOTO_PATH, photoName);

            try {
                Bitmap pickedPhoto = new EZPhotoPickStorage(this).loadStoredPhotoBitmap(DEMO_PHOTO_PATH, photoName,400);

                if (RoosterConnectionService.getState().equals(RoosterConnection.ConnectionState.CONNECTED)) {
                    Log.d(TAG, "The client is connected to the server,Sending Image");
                    //Send the message to the server

                    Intent intent = new Intent(RoosterConnectionService.SEND_IMAGE);
                    intent.putExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY,
                            "");
                    intent.putExtra(RoosterConnectionService.BUNDLE_TO,*//*"chandan@dell29"*//*contactJid);
                            intent.putExtra("filePath",filePath);

                    sendBroadcast(intent);

                    //Update the chat view.
            //        Bitmap bmp = BitmapFactory.decodeFile(filePath);
                    mChatView.sendMessage("",contactJid.split("@")[0],pickedPhoto);
                    //        mChatView.sendMessage();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Client not connected to server ,Image not sent!",
                            Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            //do something with the bitmap
        }

    }*/

    private String getRealPathFromURI(Uri contentURI) {
        String picturePath = "";
        try {
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(contentURI,
                    filePath, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePath[0]);
            picturePath = c.getString(columnIndex);
            c.close();
        } catch (Exception e) {

        }
        return picturePath;
        /*String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
        unregisterReceiver(mImageBroadcastReceiver);
        unregisterReceiver(mChatStatusBroadcastReceiver);
        unregisterReceiver(mFileBroadcastReceiver);
        unregisterReceiver(mRefreshChatHistoryListBroadcastReceiver);

    }

    @Override
    protected void onResume() {
        super.onResume();

  /*MESSAGE RECEIVE BROADCAST RECEIVER*/

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case RoosterConnectionService.NEW_MESSAGE:
                        String from = intent.getStringExtra(RoosterConnectionService.BUNDLE_FROM_JID);
                        String to = intent.getStringExtra(RoosterConnectionService.BUNDLE_TO_JID);
                        String body = intent.getStringExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY);
                        String messageId = intent.getStringExtra("messageId");

                        if (from.equals(contactJid) && body != null && !body.equalsIgnoreCase("")) {
                            statusText.setText("Online");
                            mChatView.receiveMessage(body, from.split("@")[0]);
                            //     dbHandler.addChatHistory(to,contactJid.split("@")[0],body,"","1",null);
                             /*UNREAD MESSAGE SET AS READ*/
                            dbHandler.updateReadMessageCountFlag(mUserName, contactJid.split("@")[0]);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    sendActiveStatusFirstTime();
                                }
                            }, 1000);

                        } else {
                            Log.d(TAG, "Got a message from jid :" + from);
                            if (body == null && messageId != null) {
                                dbHandler.updateMessageDeliveryStatus(to.split("@")[0], from.split("@")[0], messageId, 1);

                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent1 = new Intent(RoosterConnectionService.CHAT_HISTORY_LIST_REFRESH);
                                        sendBroadcast(intent1);
                                    }
                                }, 2000);

                            }
                        }

                        return;

                }

            }
        };

        IntentFilter filter = new IntentFilter(RoosterConnectionService.NEW_MESSAGE);
        registerReceiver(mBroadcastReceiver, filter);

/*IMAGE SEND BROADCAST RECEIVER*/

        mImageBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case RoosterConnectionService.NEW_IMAGE:
                        Bitmap bmp = null;
                        String to = intent.getStringExtra(RoosterConnectionService.BUNDLE_TO_JID);
                        String from1 = intent.getStringExtra(RoosterConnectionService.BUNDLE_FROM_JID);
                        String body1 = intent.getStringExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY);
                        String filename = intent.getStringExtra(RoosterConnectionService.IMAGE_BITMAP);
                        if (from1.split("@")[0].equals(contactJid.split("@")[0])) {
                            try {

                                bmp = Utils.loadSampledImage(filename);
                                //    Bitmap scaled = Bitmap.createScaledBitmap(bmp, 400, 400, true);
                                mChatView.receiveMessage(body1, from1.split("@")[0], bmp, 1, System.currentTimeMillis(), filename);       // default delivery status as of now
                                //        dbHandler.addChatHistory(to,contactJid.split("@")[0],"",filename,"1",null);
                            /*UNREAD MESSAGE SET AS READ*/
                                dbHandler.updateReadMessageCountFlag(mUserName, contactJid.split("@")[0]);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                }

            }
        };

        IntentFilter filter1 = new IntentFilter(RoosterConnectionService.NEW_IMAGE);
        registerReceiver(mImageBroadcastReceiver, filter1);

 /*FILE SEND BROADCAST RECEIVER*/

        mFileBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case RoosterConnectionService.NEW_FILE:
                        String to_file = intent.getStringExtra(RoosterConnectionService.BUNDLE_TO_JID);
                        String from1_file = intent.getStringExtra(RoosterConnectionService.BUNDLE_FROM_JID);
                        String body1_file = intent.getStringExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY);
                        String filename_file = intent.getStringExtra(RoosterConnectionService.IMAGE_BITMAP);
                        try {

                            mChatView.receiveMessage(body1_file, from1_file.split("@")[0], null, filename_file, System.currentTimeMillis());
                            //       dbHandler.addChatHistory(to_file,contactJid.split("@")[0],"",null,"1",filename_file);
                            /*UNREAD MESSAGE SET AS READ*/
                            dbHandler.updateReadMessageCountFlag(mUserName, contactJid.split("@")[0]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                }

            }
        };

        IntentFilter filter2 = new IntentFilter(RoosterConnectionService.NEW_FILE);
        registerReceiver(mFileBroadcastReceiver, filter2);

/*CHAT STATUS BROADCAST RECEIVER*/

        mChatStatusBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case RoosterConnectionService.CHAT_STATUS:
                        String chatStatusText = intent.getStringExtra(RoosterConnectionService.CHAT_STATUS);
                        String from = intent.getStringExtra("from");
                        boolean messageReadStatus = intent.getBooleanExtra("messageReadStatus", false);

                        try {
                            if (from != null && from.split("@")[0].equalsIgnoreCase(contactJid.split("@")[0])) {
                                //     if(!(statusText.getText().toString().equalsIgnoreCase("Online") && chatStatusText.equalsIgnoreCase("Online")))
                                statusText.setText(chatStatusText);
                                if (messageReadStatus) {
                                    sendMessageSeenIndication();
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                }

            }
        };

        IntentFilter filter11 = new IntentFilter(RoosterConnectionService.CHAT_STATUS);
        registerReceiver(mChatStatusBroadcastReceiver, filter11);





/*CHAT HISTORY LIST REFRESH BROADCAST RECEIVER WHEN MESSAGE DELIVERY RECEIPT GET*/

        mRefreshChatHistoryListBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case RoosterConnectionService.CHAT_HISTORY_LIST_REFRESH:
                        loadChatHistory();
                }

            }
        };

        IntentFilter filter111 = new IntentFilter(RoosterConnectionService.CHAT_HISTORY_LIST_REFRESH);
        registerReceiver(mRefreshChatHistoryListBroadcastReceiver, filter111);


    }


  /*  private void photoGalleryIntent(){
        Intent intent = new Intent();
        intent.setType("image*//*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"demoImage"), 1);
    }*/



 /*IMAGE CHOOSING AND HANDLING*/

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utils.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if (userChoosenTask.equals("Choose from Gallery"))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;
        }
    }

    private void selectImage() {
        final CharSequence[] items = { /*"Take Photo", "Choose from Gallery",*/"Picture", "Document",
                "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = Utils.checkPermission(ChatActivity.this);

                if (items[item].equals("Take Photo")) {
                    userChoosenTask = "Take Photo";
                    if (result)
                        cameraIntent();

                } else if (items[item].equals("Choose from Gallery")) {
                    userChoosenTask = "Choose from Gallery";
                    if (result)
                        galleryIntent();

                } else if (items[item].equals("Picture")) {
                    userChoosenTask = "Picture";
                    if (result)
                        if (sLimit > 0) {
                            getNImages(sLimit);

                        } else {
                            Toast.makeText(ChatActivity.this, "You can\'t add more than 8 image", Toast.LENGTH_SHORT).show();
                        }

                } else if (items[item].equals("Document")) {
                    userChoosenTask = "Document";
                    if (result)
                        documentIntent();

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        filePath = destination.getAbsolutePath();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(destination));
        intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
        intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION,
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void documentIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(intent, SELECT_DOCUMENT);
    }
   /* @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
            else if (requestCode == SELECT_DOCUMENT)
                onSelectFromDocumentResult(data);
        }
    }*/

    private void onCaptureImageResult(Intent data) {
       /* Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        Bitmap bm=null;
        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();

        filePath=destination.getAbsolutePath();*/
     /*try{
        Bitmap bm=Utils.loadSampledImage(filePath);
            if (RoosterConnectionService.getState().equals(RoosterConnection.ConnectionState.CONNECTED)) {
                Log.d(TAG, "The client is connected to the server,Sending Image");
                //Send the message to the server

                Intent intent = new Intent(RoosterConnectionService.SEND_IMAGE);
                intent.putExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY,
                        "");
                intent.putExtra(RoosterConnectionService.BUNDLE_TO,*//*"chandan@dell29"*//*contactJid);
                intent.putExtra("filePath",filePath);

                sendBroadcast(intent);

                //Update the chat view.
                //        Bitmap bmp = BitmapFactory.decodeFile(filePath);
                mChatView.sendMessage("",contactJid.split("@")[0],bm,0,System.currentTimeMillis());
                //        mChatView.sendMessage();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Client not connected to server ,Image not sent!",
                        Toast.LENGTH_LONG).show();
            }

        }  catch (Exception e) {
            e.printStackTrace();
        }*/

        try {
            System.gc();
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inSampleSize = 2;

            Bitmap newBitmap = BitmapFactory.decodeFile(
                    filePath, bounds);
            ExifInterface exif = new ExifInterface(
                    filePath);
            String orientString = exif
                    .getAttribute(ExifInterface.TAG_ORIENTATION);
            int orientation = orientString != null ? Integer
                    .parseInt(orientString)
                    : ExifInterface.ORIENTATION_NORMAL;

            int rotationAngle = 0;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90)
                rotationAngle = 90;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180)
                rotationAngle = 180;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270)
                rotationAngle = 270;

            Matrix mat = new Matrix();
            mat.postRotate(rotationAngle);

            newBitmap = Bitmap.createBitmap(newBitmap, 0, 0,
                    newBitmap.getWidth(), newBitmap.getHeight(),
                    mat, true);

            FileOutputStream out = null;
            out = new FileOutputStream(filePath);

            // write the compressed bitmap at the destination specified by
            // filename.
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);


            Intent intent = new Intent(ChatActivity.this, ImageEditingActivity.class);
            intent.putExtra("imageEdit", filePath);
            startActivity(intent);

            //       ivImage.setImageBitmap(thumbnail);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putString("file_uri", filePath);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        filePath = savedInstanceState.getString("file_uri");
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm = null;
        Uri selectedImageUri = null;
        if (data != null) {
            try {
                selectedImageUri = data.getData();
                filePath = getRealPathFromURI(selectedImageUri);

               /*
                bm=Utils.loadSampledImage(filePath);

                    if (RoosterConnectionService.getState().equals(RoosterConnection.ConnectionState.CONNECTED)) {
                        Log.d(TAG, "The client is connected to the server,Sending Image");
                        //Send the message to the server

                        Intent intent = new Intent(RoosterConnectionService.SEND_IMAGE);
                        intent.putExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY,
                                "");
                        intent.putExtra(RoosterConnectionService.BUNDLE_TO,*//*"chandan@dell29"*//*contactJid);
                                intent.putExtra("filePath",filePath);

                        sendBroadcast(intent);

                        //Update the chat view.
                        //        Bitmap bmp = BitmapFactory.decodeFile(filePath);
                        mChatView.sendMessage("",contactJid.split("@")[0],bm,0,System.currentTimeMillis());
                        //        mChatView.sendMessage();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Client not connected to server ,Image not sent!",
                                Toast.LENGTH_LONG).show();
                    }*/

                Intent intent = new Intent(ChatActivity.this, ImageEditingActivity.class);
                intent.putExtra("imageEdit", filePath);
                startActivity(intent);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //       ivImage.setImageBitmap(bm);
    }


    private void getNImages(int selectionLimit) {
        Intent intent = new Intent(this, ImagePickerActivity.class);
        nl.changer.polypicker.Config config = new nl.changer.polypicker.Config.Builder()
                .setTabBackgroundColor(R.color.white)    // set tab background color. Default white.
                .setTabSelectionIndicatorColor(R.color.blue)
                .setCameraButtonColor(R.color.mColor)
                .setSelectionLimit(selectionLimit)   // set photo selection limit. Default unlimited selection.
                .build();
        ImagePickerActivity.setConfig(config);
        startActivityForResult(intent, INTENT_REQUEST_GET_N_IMAGES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resuleCode, Intent intent) {
        super.onActivityResult(requestCode, resuleCode, intent);

        if (resuleCode == Activity.RESULT_OK) {
            if (requestCode == INTENT_REQUEST_GET_N_IMAGES) {
                Parcelable[] parcelableUris = intent.getParcelableArrayExtra(ImagePickerActivity.EXTRA_IMAGE_URIS);

                if (parcelableUris == null) {
                    return;
                }


                // Java doesn't allow array casting, this is a little hack
                Uri[] uris = new Uri[parcelableUris.length];
                System.arraycopy(parcelableUris, 0, uris, 0, parcelableUris.length);
                mMedia.clear();
                if (uris != null) {
                    for (Uri uri : uris) {
                        Log.i("MultipleImageSelection", " uri: " + uri);
                        mMedia.add(uri);
                    }

                    showMedia();
                }
            }
            else if (requestCode == SELECT_DOCUMENT)
                onSelectFromDocumentResult(intent);
        }
    }


    private void showMedia() {
        // Remove all views before
        // adding the new ones.
        Iterator<Uri> iterator = mMedia.iterator();
        imageModelArrayList.clear();
        while (iterator.hasNext()) {
            Uri uri = iterator.next();

            // showImage(uri);

            if (!uri.toString().contains("content://")) {
                // probably a relative uri
                uri = Uri.fromFile(new File(uri.toString()));
                Log.i(TAG, " uri: " + uri);
            }

            // Image subSampling
            File uFile = new File(uri.getPath());
            try {
                FileInputStream fileInputStream = new FileInputStream(uFile);
                size = fileInputStream.available();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (size > 2097152) {
                Utils.compressImage(uri);
            }

            //       ImageModel imageModel=new ImageModel(uri.toString());
            imageModelArrayList.add(uri.toString());

        }

        sLimit = tLimit - imageModelArrayList.size();
        //    addImageCountText.setText("" + imageModelArrayList.size() + "/8 Photos");
        Log.d("image count", "" + sLimit);

        Intent intent = new Intent(ChatActivity.this, ImageEditingActivity.class);
        intent.putExtra("imageList", imageModelArrayList);
        startActivity(intent);

        sLimit=8;
        tLimit=8;
        imageModelArrayList.clear();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        edited = intent.getStringExtra("edited");
        imageModelArrayList = (ArrayList<String>) intent.getSerializableExtra("editedImagesList");
        String posImg = intent.getStringExtra("eImgPosition");

        if (edited != null && edited.equalsIgnoreCase("true")) {
            for (final String imageEdit : imageModelArrayList) {
                final Bitmap bm = Utils.loadSampledImage(imageEdit.replace("file://", ""));

    /*HANDLER FOR SENDING MULTIPLE IMAGE*/
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (RoosterConnectionService.getState().equals(RoosterConnection.ConnectionState.CONNECTED)) {
                            Log.d(TAG, "The client is connected to the server,Sending Image");
                            //Send the message to the server

                            Intent intent1 = new Intent(RoosterConnectionService.SEND_IMAGE);
                            intent1.putExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY,
                                    "");
                            intent1.putExtra(RoosterConnectionService.BUNDLE_TO,/*"chandan@dell29"*/contactJid);
                            intent1.putExtra("filePath", imageEdit.replace("file://", ""));

                            sendBroadcast(intent1);

                            //Update the chat view.
                            //        Bitmap bmp = BitmapFactory.decodeFile(filePath);
                            mChatView.sendMessage("", contactJid.split("@")[0], bm, -1, System.currentTimeMillis(),imageEdit.replace("file://", ""));
                            //        mChatView.sendMessage();
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "Client not connected to server ,Image not sent!",
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                }, 2000);
            }
        }


    }


 // DOCUMENT
 private void onSelectFromDocumentResult(Intent data) {

     String selectedFilePath=null;
     if (data != null) {
         try {
             selectedFilePath=data.getData().getPath();

             if (RoosterConnectionService.getState().equals(RoosterConnection.ConnectionState.CONNECTED)) {
                 Log.d(TAG, "The client is connected to the server,Sending Image");
                 //Send the message to the server
                 final String finalSelectedFilePath = selectedFilePath;
                 new Handler().post(new Runnable() {
                  @Override
                  public void run() {

                 Intent intent = new Intent(RoosterConnectionService.SEND_IMAGE);
                 intent.putExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY,
                         "");
                 intent.putExtra(RoosterConnectionService.BUNDLE_TO,/*"chandan@dell29"*/contactJid);
                 intent.putExtra("filePath", finalSelectedFilePath);

                 sendBroadcast(intent);

                 //Update the chat view.
                 //        Bitmap bmp = BitmapFactory.decodeFile(filePath);
                mChatView.sendMessage("",contactJid.split("@")[0],null, finalSelectedFilePath,-1,System.currentTimeMillis());
                 //        mChatView.sendMessage();

                  }
              });
             } else {
                 Toast.makeText(getApplicationContext(),
                         "Client not connected to server ,Image not sent!",
                         Toast.LENGTH_LONG).show();
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }

     //       ivImage.setImageBitmap(bm);
 }

}
