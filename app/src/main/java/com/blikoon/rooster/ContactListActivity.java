package com.blikoon.rooster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blikoon.rooster.db.ChatMessage;
import com.blikoon.rooster.db.DB_Handler;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class ContactListActivity extends AppCompatActivity {

    private static final String TAG = "ContactListActivity";

    private RecyclerView contactsRecyclerView;
    private ContactAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private BroadcastReceiver mBroadcastReceiver,mListUpdateBroadcastReceiver;
    ImageView addFriendBtn;
    CircleImageView profileImageView;
    DB_Handler dbHandler;
    String mUserName;
    Typeface roboto_regular;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dbHandler=new DB_Handler(this);
        roboto_regular=Utils.getCustomFont(this,FontType.ROBOTO_REGULAR);
        mUserName=PreferenceManager.getDefaultSharedPreferences(this)
                .getString("xmpp_jid",null).split("@")[0];

        profileImageView= (CircleImageView) findViewById(R.id.profileInfo);
        addFriendBtn= (ImageView) findViewById(R.id.addFriend);
        contactsRecyclerView = (RecyclerView) findViewById(R.id.contact_list_recycler_view);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));

        String profImageString=PreferenceManager.getDefaultSharedPreferences(this)
        .getString("profileImage",null);

        if(profImageString!=null) {
            byte[] imgArray = Base64.decode(profImageString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(imgArray, 0, imgArray.length);
            profileImageView.setImageBitmap(null);
            profileImageView.setImageBitmap(bitmap);
        }

        getFriendListAndPopulate();

        addFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddFriendPrompt();
            }
        });

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ContactListActivity.this,ProfileActivity.class);
                startActivity(intent);
            }
        });

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle=intent.getExtras();
        if(bundle!=null){
            String action=bundle.getString("action");
            if(action!=null && action.equalsIgnoreCase("refresh")){
                String profImageString=PreferenceManager.getDefaultSharedPreferences(this)
                        .getString("profileImage",null);

                if(profImageString!=null) {
                    byte[] imgArray = Base64.decode(profImageString, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imgArray, 0, imgArray.length);
                    profileImageView.setImageBitmap(null);
                    profileImageView.setImageBitmap(bitmap);
                }
            }

        }
    }
public void getFriendListAndPopulate(){
    if (RoosterConnectionService.getState().equals(RoosterConnection.ConnectionState.CONNECTED)) {
        Log.d(TAG, "The client is connected to the server");
        //Send the message to the server
        Intent intent = new Intent(RoosterConnectionService.FRIEND_LIST_GET);
        intent.setPackage(this.getPackageName());
        sendBroadcast(intent);

    } else {
        Toast.makeText(getApplicationContext(),
                "Client not connected to server",
                Toast.LENGTH_LONG).show();
    }

}


    private class ContactHolder extends RecyclerView.ViewHolder
    {
        private TextView contactTextView,unreadMessageCountText,latestMessageText,messageTimeStampText;
        private ImageView profImage,statusIcon;
        private Contact mContact;
        public ContactHolder ( View itemView)
        {
            super(itemView);

            contactTextView = (TextView) itemView.findViewById(R.id.contact_jid);
            unreadMessageCountText= (TextView) itemView.findViewById(R.id.unread_message_count_text);
            latestMessageText= (TextView) itemView.findViewById(R.id.latest_message_text);
            profImage= (ImageView) itemView.findViewById(R.id.addedBuddy_icon_img);
            statusIcon= (ImageView) itemView.findViewById(R.id.lastMessageStatusIcon);
            messageTimeStampText= (TextView) itemView.findViewById(R.id.lastMessageTimeStamp);

            contactTextView.setTypeface(roboto_regular);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Inside here we start the chat activity
                    Intent intent = new Intent(ContactListActivity.this
                            ,ChatActivity.class);
                    intent.putExtra("EXTRA_CONTACT_JID",mContact.getJid()/*+"@115.124.109.82"*//*+"@dell29"*/);
                    intent.putExtra("POSITION",getAdapterPosition());
                    startActivity(intent);


                }
            });
        }


        public void bindContact( Contact contact)
        {

            mContact = contact;
            if (mContact == null)
            {
                Log.d(TAG,"Trying to work on a null Contact object ,returning.");
                return;
            }
            contactTextView.setText(mContact.getJid().split("@")[0].substring(0, 1).toUpperCase() + mContact.getJid().split("@")[0].substring(1));

            String unreadMessageCount=getUnreadMessageCount(mUserName,mContact.getJid().split("@")[0]);
            if(unreadMessageCount.equalsIgnoreCase("0")) {
                unreadMessageCountText.setVisibility(View.GONE);
            }else{
                unreadMessageCountText.setVisibility(View.VISIBLE);
                unreadMessageCountText.setText(unreadMessageCount);
            }
           /* if(mContact.getStatus()!=null) {
                if (mContact.getStatus().equalsIgnoreCase("available"))
                    gd.setColor(getResources().getColor(R.color.online_color));
                else
                    gd.setColor(getResources().getColor(R.color.offline_color));
            }
*/
           new Handler().post(new Runnable() {
            @Override
            public void run() {
            if(mContact.getImageData()!=null){

                Glide.with(ContactListActivity.this).load(mContact.getImageData()).asBitmap().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(profImage);
 //             profImage.setImageBitmap(BitmapFactory.decodeByteArray(mContact.getImageData(),0,mContact.getImageData().length));

               /* ImageLoader imageLoader=ImageLoader.getInstance();
                DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                        .cacheOnDisc(true).resetViewBeforeLoading(true)
                        .showImageForEmptyUri(ContactListActivity.this.getResources().getDrawable(R.drawable.place_holder))
                        .showImageOnFail(ContactListActivity.this.getResources().getDrawable(R.drawable.place_holder))
                        .showImageOnLoading(ContactListActivity.this.getResources().getDrawable(R.drawable.place_holder)).build();

                ByteArrayInputStream stream = new ByteArrayInputStream(mContact.getImageData());
                String imageId = "stream://" + stream.hashCode();
                imageLoader.displayImage(imageId,profImage,options);*/
            }

            String[] lastMessageInfo=getLatestMessage(mUserName,mContact.getJid().split("@")[0]).split("_");
            latestMessageText.setText(lastMessageInfo[0]);

            if(lastMessageInfo.length>2) {
                messageTimeStampText.setVisibility(View.VISIBLE);
                String messageTimeStamp = lastMessageInfo[2];
                String[] timeTextArr = messageTimeStamp.split("-");
                if (timeTextArr[0].trim().equalsIgnoreCase("Today"))
                    messageTimeStampText.setText(timeTextArr[1]);
                else
                    messageTimeStampText.setText(timeTextArr[0]);
            }
            else
                messageTimeStampText.setVisibility(View.GONE);

            if(lastMessageInfo.length>3){
                if(lastMessageInfo[2].equalsIgnoreCase("0"))
                    statusIcon.setVisibility(View.VISIBLE);
                else
                    statusIcon.setVisibility(View.GONE);
            }
    }
});
        }


    }

  public String getLatestMessage(String sender,String receiver){
      String result="";
      ArrayList<ChatMessage> chatList=dbHandler.getChatHistory(sender,receiver);
      if(chatList.size()>0){
          result=chatList.get(chatList.size()-1).getMessage()+ "_"+
                  chatList.get(chatList.size()-1).getDeliveryStatus()+"_"+
                  chatList.get(chatList.size()-1).getFormattedTime()+"_"+
                  chatList.get(chatList.size()-1).getStatus()+"_";
      }
      return result;
  }

 public String getUnreadMessageCount(String sender,String receiver){

     return dbHandler.getUnReadMessageCount(sender,receiver);
 }
    private class ContactAdapter extends RecyclerView.Adapter<ContactHolder>
    {
        private List<Contact> mContacts;

        public ContactAdapter( List<Contact> contactList)
        {
            mContacts = contactList;
        }

        @Override
        public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater
                    .inflate(R.layout.list_item_contact, parent,
                            false);
            return new ContactHolder(view);
        }

        @Override
        public void onBindViewHolder(ContactHolder holder, int position) {
            Contact contact = mContacts.get(position);
            holder.bindContact(contact);

        }

        @Override
        public int getItemCount() {
            return mContacts.size();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    /*FOR COMING BACK FROM ANY SCREEN LIST SHOULD BE UPDATED*/
       if(mAdapter!=null)
        mAdapter.notifyDataSetChanged();


        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action)
                {
                    case RoosterConnectionService.FRIEND_LIST_RECEIVE:
                 //       ArrayList<Contact> friendList = (ArrayList<Contact>) intent.getSerializableExtra(RoosterConnectionService.FRIEND_LIST);

                       if ( AppController.friendList!=null)
                        {
                            mAdapter = new ContactAdapter(AppController.friendList);
                            contactsRecyclerView.setAdapter(mAdapter);
                        }else
                        {
                            Log.d(TAG,"Friends not available");
                        }

                        return;
                }

            }
        };

        IntentFilter filter = new IntentFilter(RoosterConnectionService.FRIEND_LIST_RECEIVE);
        registerReceiver(mBroadcastReceiver,filter);

    /*LIST UPDATE BROADCAST RECEIVER WHEN NEW MESSAGE/IMAGE/DOCS COME*/

        mListUpdateBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action)
                {
                    case RoosterConnectionService.BUDDYLIST_UPDATE:
                        try {
                            mAdapter.notifyDataSetChanged();
                        }catch (Exception e){}
                        return;

                }

            }
        };

        IntentFilter filter1 = new IntentFilter(RoosterConnectionService.BUDDYLIST_UPDATE);
        registerReceiver(mListUpdateBroadcastReceiver,filter1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
        unregisterReceiver(mListUpdateBroadcastReceiver);
    }

  public void showAddFriendPrompt(){
      View promptView= LayoutInflater.from(this).inflate(R.layout.add_friend_prompt,null);
      AlertDialog.Builder builder=new AlertDialog.Builder(this);
      builder.setTitle("Add Friend");
      builder.setView(promptView);
      builder.setCancelable(true);

      final EditText name= (EditText) promptView.findViewById(R.id.name);
      final EditText mobileNo=(EditText) promptView.findViewById(R.id.mobileNo);
      Button add= (Button) promptView.findViewById(R.id.addFriendButton);

      final AlertDialog dialog=builder.create();
      dialog.show();

      add.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              String nameValue=name.getText().toString();
              String mobileNoValue=mobileNo.getText().toString();

              if(TextUtils.isEmpty(nameValue)){
                  name.requestFocus();
                  Toast.makeText(ContactListActivity.this,"Name is required!!!",Toast.LENGTH_LONG).show();
              }
              else if(TextUtils.isEmpty(mobileNoValue)){
                  mobileNo.requestFocus();
                  Toast.makeText(ContactListActivity.this,"Mobile No is required!!!",Toast.LENGTH_LONG).show();
              }else{

                  if (RoosterConnectionService.getState().equals(RoosterConnection.ConnectionState.CONNECTED)) {
                      Log.d(TAG, "The client is connected to the server");
                      //Send the message to the server
                      Intent intent = new Intent(RoosterConnectionService.ADD_FRIEND);
                      intent.putExtra(RoosterConnectionService.ADD_FRIEND_NAME,nameValue);
                      intent.putExtra(RoosterConnectionService.ADD_FRIEND_MOBILE_NO,mobileNoValue);
                      sendBroadcast(intent);
                      dialog.dismiss();

                  } else {
                      Toast.makeText(getApplicationContext(),
                              "Client not connected to server",
                              Toast.LENGTH_LONG).show();
                  }


              }
          }
      });


  }
  }
