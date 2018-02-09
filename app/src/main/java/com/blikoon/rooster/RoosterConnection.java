package com.blikoon.rooster;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;

import com.blikoon.rooster.db.DB_Handler;

/*import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ChatMessageListener;*/
import org.jivesoftware.smack.AbstractConnectionListener;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.ReconnectionManager;
/*import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;*/
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketExtensionFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.filter.StanzaIdFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.ChatStateListener;
import org.jivesoftware.smackx.chatstates.ChatStateManager;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.iqlast.LastActivityManager;
import org.jivesoftware.smackx.iqlast.packet.LastActivity;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.iqregister.packet.Registration;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
/*import org.jivesoftware.smackx.ping.android.ServerPingWithAlarmManager;*/
import org.jivesoftware.smackx.offline.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.vcardtemp.provider.VCardProvider;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Created by gakwaya on 4/28/2016.
 */
public class RoosterConnection extends AbstractConnectionListener {

    private static final String TAG = "RoosterConnection";

    private  final Context mApplicationContext;
    private  final String mUsername;
    private  final String mPassword;
    private  final String mServiceName;
    private XMPPTCPConnection mConnection;
    private BroadcastReceiver uiThreadMessageReceiver;//Receives messages from the ui thread.
    private BroadcastReceiver uiThreadGettingFriendList,uiThreadAddFriend,uiThreadImageReceiver,
                              uiThreadLoadVcardReceiver,uiThreadUpdateVcardReceiver,uiThreadUploadProfileImageReceiver,
                              uiThreadUserLastSeenReceiver;//Receives messages from the ui thread.
//    private  ChatMessageListener messageListener;
    DB_Handler dbHandler;
    private ChatStateListener chatStateListener;
    private Presence presence =null;

    ArrayList<Contact> friendList=new ArrayList<>();


    public static enum ConnectionState
    {
        CONNECTED ,AUTHENTICATED, CONNECTING ,DISCONNECTING ,DISCONNECTED;
    }

    public static enum LoggedInState
    {
        LOGGED_IN , LOGGED_OUT;
    }


    public RoosterConnection( Context context)
    {
        Log.d(TAG,"RoosterConnection Constructor called.");
        mApplicationContext = context.getApplicationContext();
        dbHandler=new DB_Handler(mApplicationContext);
        String jid = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_jid",null);
        mPassword = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_password",null);

        if( jid != null)
        {
            mUsername = jid.split("@")[0];
            mServiceName = /*"115.124.109.82";*/"192.168.0.29";//jid.split("@")[1];
        }else
        {
            mUsername ="";
            mServiceName="";
        }
    }


    public void connect() throws IOException,XMPPException,SmackException
    {
        Log.d(TAG, "Connecting to server " + mServiceName);
        XMPPTCPConnectionConfiguration.Builder builder=XMPPTCPConnectionConfiguration.builder();
       /* XMPPTCPConnectionConfiguration.XMPPTCPConnectionConfigurationBuilder builder=
                XMPPTCPConnectionConfiguration.builder();*/
        builder.setServiceName(mServiceName);
        builder.setUsernameAndPassword(mUsername, mPassword);
     //   builder.setRosterLoadedAtLogin(true);
    //    builder.setResource("localhost");
        builder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        builder.setSendPresence(false);

        //Set up the ui thread broadcast message receiver.
        setupUiThreadBroadCastMessageReceiver();

        //Set up the ui thread broadcast Image receiver.
        setupUiThreadBroadCastImageReceiver();

        //Set up the ui thread broadcast friend list receiver.
        setupUiThreadBroadCastFriendListGetting();

        //Set up the ui thread broadcast add friend receiver.
        setupUiThreadBroadCastAddFriend();

        //Set up the ui thread broadcast for load user vcard
        setupUiThreadBroadCastLoadVcard();

        // set up the ui thread broadcast for update user vcard
        setupUiThreadBroadCastUpdateVcard();

        // set up the ui thread broadcast for upload user profile image
        setupUiThreadBroadCastUploadUserProfileImage();

        // set up the ui thread broadcast for getting user's last seen
        setupUiThreadBroadCastLastSeen();

        ProviderManager.addIQProvider("offline", "http://jabber.org/protocol/offline",
                new OfflineMessageRequest.Provider());

        DeliveryReceiptManager.setDefaultAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.always);
        ProviderManager.addExtensionProvider(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE, new DeliveryReceipt.Provider());
        ProviderManager.addExtensionProvider(DeliveryReceiptRequest.ELEMENT, new DeliveryReceiptRequest().getNamespace(), new DeliveryReceiptRequest.Provider());

        mConnection = new XMPPTCPConnection(builder.build());
        mConnection.addConnectionListener(this);
        mConnection.setPacketReplyTimeout(40000);
        mConnection.connect();


         /*file receiver delegate setup firsttime*/
        fileReciever(mConnection);

        /*get message delivery status delegate*/
        DeliveryReceiptManager.getInstanceFor(mConnection).addReceiptReceivedListener(new ReceiptReceivedListener() {
            @Override
            public void onReceiptReceived(String fromJid, String toJid, String receiptId, Stanza receipt) {
                Log.d("Delivery Receipt","from-"+fromJid+",To-"+toJid+",receiptId-"+receiptId+",receipt-"+receipt.toString());
                dbHandler.updateMessageDeliveryStatus(toJid.split("@")[0], fromJid.split("@")[0],receiptId,1);
                Intent intent1 = new Intent(RoosterConnectionService.CHAT_HISTORY_LIST_REFRESH);
                intent1.setPackage(mApplicationContext.getPackageName());

                mApplicationContext.sendBroadcast(intent1);
            }

        });


        chatStateListener=new ChatStateListener() {

            @Override
            public void stateChanged(Chat chat, ChatState state) {
                Intent intent11 = new Intent(RoosterConnectionService.CHAT_STATUS);
                intent11.setPackage(mApplicationContext.getPackageName());

                if (ChatState.composing.equals(state)) {
                    Log.d("Chat State",chat.getParticipant() + " is typing..");
                    intent11.putExtra(RoosterConnectionService.CHAT_STATUS,"Typing");
                    intent11.putExtra("from",chat.getParticipant().split("@")[0]);
                } else if (ChatState.gone.equals(state)) {
                    Log.d("Chat State",chat.getParticipant() + " Offline");
                    intent11.putExtra(RoosterConnectionService.CHAT_STATUS,"Offline");
                    intent11.putExtra("from",chat.getParticipant().split("@")[0]);
                }else if (ChatState.active.equals(state)) {
                    Log.d("Chat State",chat.getParticipant() + " online.");
                    intent11.putExtra(RoosterConnectionService.CHAT_STATUS,"Online");
                    intent11.putExtra("from",chat.getParticipant().split("@")[0]);
                    intent11.putExtra("messageReadStatus",true);
                    dbHandler.updateMessageDeliveryStatus(mUsername.split("@")[0],chat.getParticipant().split("@")[0],2);
                }
                else if (ChatState.paused.equals(state)) {
                    Log.d("Chat State",chat.getParticipant() + "pause");
                    intent11.putExtra(RoosterConnectionService.CHAT_STATUS,"Online");
                    intent11.putExtra("from",chat.getParticipant().split("@")[0]);
                    intent11.putExtra("messageReadStatus",false);
                }
                else if (ChatState.inactive.equals(state)) {
                    Log.d("Chat State",chat.getParticipant() + "inactive");
                    intent11.putExtra(RoosterConnectionService.CHAT_STATUS,"Inactive");
                    intent11.putExtra("from",chat.getParticipant().split("@")[0]);
                }
                else {
                    Log.d("Chat State",chat.getParticipant() + ": " + state.name());
                }

                mApplicationContext.sendBroadcast(intent11);
            }

            @Override
            public void processMessage(Chat chat, Message message) {
                Log.d(TAG, "message.getBody() :" + message.getBody());
                Log.d(TAG, "message.getFrom() :" + message.getFrom());

                String from = message.getFrom();
                String to=message.getTo().split("@")[0];
                String contactJid = "";
                if (from.contains("/")) {
                    contactJid = from.split("/")[0];
                    Log.d(TAG, "The real jid is :" + contactJid);

               /*FOR RECEIVING FILE */
                //    fileReciever(mConnection);
                } else {
                    contactJid = from;
                }

              //Bundle up the intent and send the broadcast.
                Intent intent = new Intent(RoosterConnectionService.NEW_MESSAGE);
                intent.setPackage(mApplicationContext.getPackageName());
                intent.putExtra(RoosterConnectionService.BUNDLE_FROM_JID,contactJid);
                intent.putExtra(RoosterConnectionService.BUNDLE_TO_JID,to);
                intent.putExtra("messageId",message.getStanzaId());

                intent.putExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY,message.getBody());
                mApplicationContext.sendBroadcast(intent);
                Log.d(TAG,"Received message from :"+contactJid+" broadcast sent.");
                ///ADDED
                if (message.getBody()!=null && !message.getBody().equalsIgnoreCase("")) {
                    dbHandler.addChatHistory(to, contactJid.split("@")[0], message.getBody(), "", "1", null,"false","",0);
                    Intent intent1=new Intent(RoosterConnectionService.BUDDYLIST_UPDATE);
                    mApplicationContext.sendBroadcast(intent1);
                }
            }
        };



        //The snippet below is necessary for the message listener to be attached to our connection.
        ChatManager.getInstanceFor(mConnection).addChatListener(new ChatManagerListener() {
            @Override
            public void chatCreated(Chat chat, boolean createdLocally) {

                //If the line below is missing ,processMessage won't be triggered and you won't receive messages.
                chat.addMessageListener(chatStateListener/*messageListener*/);
                ChatStateManager.getInstance(mConnection);


            }
        });


  //      ServerPingWithAlarmManager.getInstanceFor(mConnection).isEnabled();
        ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(mConnection);
        reconnectionManager.setEnabledPerDefault(true);
        reconnectionManager.enableAutomaticReconnection();

//        PingManager.setDefaultPingInterval(120);

    }

    private void setupUiThreadBroadCastMessageReceiver()
    {
        uiThreadMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                //Check if the Intents purpose is to send the message.
                String action = intent.getAction();
                if( action.equals(RoosterConnectionService.SEND_MESSAGE))
                {
                    Message message = new Message();
                    ChatStateExtension extension=null;
                    if(intent.getStringExtra("chatState").equalsIgnoreCase("composing")) {
                        extension = new ChatStateExtension(ChatState.composing);
                    }
                    else if(intent.getStringExtra("chatState").equalsIgnoreCase("active")) {
                        extension = new ChatStateExtension(ChatState.active);
                    }
                    else if(intent.getStringExtra("chatState").equalsIgnoreCase("pause")) {
                        extension = new ChatStateExtension(ChatState.paused);
                    }
                    message.addExtension(extension);
                    message.setTo(mUsername);
                    //Send the message.
                    if(intent.getStringExtra("filePath").equalsIgnoreCase("")) {
                        sendMessage(intent.getStringExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY),
                                intent.getStringExtra(RoosterConnectionService.BUNDLE_TO),message,intent.getStringExtra("chatState"));
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(RoosterConnectionService.SEND_MESSAGE);
        mApplicationContext.registerReceiver(uiThreadMessageReceiver,filter);

    }

    private void sendMessage ( String body ,String toJid,Message message,String chatState)
    {
        Log.d(TAG,"Sending message to :"+ toJid);
        Chat chat = ChatManager.getInstanceFor(mConnection)
                .createChat(toJid,chatStateListener/*messageListener*/);
        try
        {
            if(!body.equalsIgnoreCase("")) {
                message.setBody(body);
               String deliveryReceiptId= DeliveryReceiptRequest.addTo(message);
                String jid = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                        .getString("xmpp_jid", null);
                dbHandler.addChatHistory(jid, toJid.split("@")[0], message.getBody(), "", "0",null,"true",deliveryReceiptId,0);
            }
            else{
                if(chatState.equalsIgnoreCase("composing"))
                ChatStateManager.getInstance(mConnection).setCurrentState(ChatState.composing,chat);
                else if(chatState.equalsIgnoreCase("active"))
                    ChatStateManager.getInstance(mConnection).setCurrentState(ChatState.active,chat);
                else if(chatState.equalsIgnoreCase("pause"))
                    ChatStateManager.getInstance(mConnection).setCurrentState(ChatState.paused,chat);
                else if(chatState.equalsIgnoreCase("Inactive"))
                    ChatStateManager.getInstance(mConnection).setCurrentState(ChatState.inactive,chat);
            }

            chat.sendMessage(message/*body*/);

        }catch (SmackException.NotConnectedException /*| XMPPException*/ e)
        {
            e.printStackTrace();
        }


    }
/*SEND IMAGE FILE*/

    private void setupUiThreadBroadCastImageReceiver()
    {
        uiThreadImageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                //Check if the Intents purpose is to send the message.
                String action = intent.getAction();
                if( action.equals(RoosterConnectionService.SEND_IMAGE))
                {
                    /*FOR IMAGE SEND */
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                fileTransfer(intent.getStringExtra("filePath"),null,intent.getStringExtra(RoosterConnectionService.BUNDLE_TO));
                            }
                        });
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(RoosterConnectionService.SEND_IMAGE);
        mApplicationContext.registerReceiver(uiThreadImageReceiver,filter);

    }

    private void setupUiThreadBroadCastFriendListGetting()
    {

        uiThreadGettingFriendList = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Check if the Intents purpose is to send the message.
                String action = intent.getAction();
                if( action.equals(RoosterConnectionService.FRIEND_LIST_GET))
                {
                    getBuddyListAndSendToContactList();

                    }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(RoosterConnectionService.FRIEND_LIST_GET);
        mApplicationContext.registerReceiver(uiThreadGettingFriendList,filter);
    }
public void getBuddyListAndSendToContactList(){

           friendList=friendList();

           //Bundle up the intent and send the broadcast.
           Intent intent1 = new Intent(RoosterConnectionService.FRIEND_LIST_RECEIVE);
           intent1.setPackage(mApplicationContext.getPackageName());
    //       intent1.putExtra(RoosterConnectionService.FRIEND_LIST,friendList);
          AppController.friendList.clear();
           AppController.friendList=friendList;
           mApplicationContext.sendBroadcast(intent1);

}

    public ArrayList<Contact> friendList(){
        Roster roster = Roster.getInstanceFor(mConnection);
    //    roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
        ArrayList<Contact> friendList=new ArrayList<>();
        Collection<RosterEntry> entries = roster.getEntries();
        Presence presence;
        Contact contact;
        VCardManager vCardManager=VCardManager.getInstanceFor(mConnection);
        VCard vCard=null;
        byte[] imgArr=null;
        roster.addRosterListener(new RosterListener() {
            @Override
            public void entriesAdded(Collection<String> addresses) {
              Intent intent =new Intent(RoosterConnectionService.FRIEND_LIST_GET);
              mApplicationContext.sendBroadcast(intent);
                Log.d("Roster","Added");
            }

            @Override
            public void entriesUpdated(Collection<String> addresses) {
                Intent intent =new Intent(RoosterConnectionService.FRIEND_LIST_GET);
                mApplicationContext.sendBroadcast(intent);
                Log.d("Roster","Updated");
            }

            @Override
            public void entriesDeleted(Collection<String> addresses) {
                Intent intent =new Intent(RoosterConnectionService.FRIEND_LIST_GET);
                mApplicationContext.sendBroadcast(intent);
                Log.d("Roster","Deleted");
            }

            @Override
            public void presenceChanged(Presence presence) {
                   Log.d("presences",""+presence.isAvailable());
                   Log.d("presence id",presence.getFrom());
                Intent intent=new Intent(RoosterConnectionService.CHAT_STATUS);
                if(presence.isAvailable()) {
                    intent.putExtra(RoosterConnectionService.CHAT_STATUS, "Online");
                    intent.putExtra("from",presence.getFrom());
                    intent.putExtra("messageReadStatus",false);

                    AppController.resourceId=presence.getFrom();

                    if(RoosterConnectionService.sConnectionState == ConnectionState.DISCONNECTED)
                        RoosterConnectionService.sConnectionState = ConnectionState.CONNECTED;
                }
                else if(presence.isAway()) {
                    intent.putExtra(RoosterConnectionService.CHAT_STATUS, "Away");
                    intent.putExtra("from",presence.getFrom());
                }
                else {

                    intent.putExtra(RoosterConnectionService.CHAT_STATUS, "Last Seen at "+ DateFormat.format("hh:mm a", System.currentTimeMillis()).toString());
                    intent.putExtra("from",presence.getFrom());
                }

                mApplicationContext.sendBroadcast(intent);
            }
        });


        for(RosterEntry entry : entries) {
            presence = roster.getPresence(entry.getUser());

            System.out.println(entry.getUser());
            System.out.println(presence.getType().name());
            System.out.println(presence.getStatus());
            try {

               /* if(presence.getType().toString().equalsIgnoreCase("from")) {
                    Presence subscribed = new Presence(Presence.Type.subscribed);
                    subscribed.setTo(entry.getUser());
                    mConnection.sendStanza(subscribed);
                }*/

               /* if(!roster.isSubscribedToMyPresence(presence.getFrom())){
                     Presence response = new Presence(
                            Presence.Type.subscribed);
                    response.setTo(presence.getFrom());
                    mConnection.sendStanza(response);
                }*/

                vCard=vCardManager.loadVCard(entry.getUser());
                imgArr=vCard.getAvatar();
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
            } catch (XMPPException.XMPPErrorException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
            catch (Exception e){
                e.printStackTrace();
            }

            contact=new Contact();
            contact.setJid(entry.getUser());
            contact.setUserName(presence.getType().name());
            contact.setStatus(presence.getType().name());


           if(imgArr!=null)
            contact.setImageData(imgArr);

            friendList.add(contact);
        }
        return friendList;
    }

    private void setupUiThreadBroadCastAddFriend()
    {

        uiThreadAddFriend = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Check if the Intents purpose is to send the message.
                String action = intent.getAction();
                if( action.equals(RoosterConnectionService.ADD_FRIEND))
                {
                    addFriend(intent.getStringExtra(RoosterConnectionService.ADD_FRIEND_NAME),
                            intent.getStringExtra(RoosterConnectionService.ADD_FRIEND_MOBILE_NO));
                    //Bundle up the intent and send the broadcast.
                    Intent intent1 = new Intent(RoosterConnectionService.FRIEND_LIST_GET);
                    mApplicationContext.sendBroadcast(intent1);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(RoosterConnectionService.ADD_FRIEND);
        mApplicationContext.registerReceiver(uiThreadAddFriend,filter);
    }

    public void addFriend(String jid,String userName){
        Roster roster=Roster.getInstanceFor(mConnection);
        roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
        try {
            roster.createEntry(jid+RoosterConnectionService.DOMAIN_NAME, userName, null);
           /* Presence subscribed = new Presence(Presence.Type.subscribe);
            subscribed.setTo(jid+RoosterConnectionService.DOMAIN_NAME);
            mConnection.sendStanza(subscribed);*/

            Log.d("status",userName+" successfully added to your contact list");
        } catch (SmackException.NotLoggedInException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

        //   Toast.makeText(getApplicationContext(),userName+" successfully added to your contact list",Toast.LENGTH_LONG).show();

    }

    private void setupUiThreadBroadCastLoadVcard()
    {

        uiThreadLoadVcardReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Check if the Intents purpose is to send the message.
                String action = intent.getAction();
                if( action.equals(RoosterConnectionService.LOAD_VCARD))
                {
                    loadUserVCard();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(RoosterConnectionService.LOAD_VCARD);
        mApplicationContext.registerReceiver(uiThreadLoadVcardReceiver,filter);
    }
/*LOAD USER PROFILE VCARD*/
    public void loadUserVCard(){
        ProviderManager.addIQProvider("vCard", "vcard-temp", new VCardProvider());

        VCard vCard=null;
        try {
            VCardManager vCardManager=VCardManager.getInstanceFor(mConnection);
            vCard=vCardManager.loadVCard(mUsername+"@dell29");

            //Bundle up the intent and send the broadcast.
            Intent intent1 = new Intent(RoosterConnectionService.GET_USER_PROFILE_DATA);
            intent1.putExtra(RoosterConnectionService.VCARD_USERNAME,vCard.getFirstName());
            intent1.putExtra(RoosterConnectionService.VCARD_NAME,vCard.getNickName());
            intent1.putExtra(RoosterConnectionService.VCARD_EMAIL,vCard.getEmailHome());
            intent1.putExtra(RoosterConnectionService.VCARD_MOBILE_NO,vCard.getPhoneHome("Phone"));
       //     intent1.putExtra(RoosterConnectionService.VCARD_PASSWORD,vCard.get);
            intent1.putExtra(RoosterConnectionService.VCARD_PROFILE_IMAGE,vCard.getAvatar());
            mApplicationContext.sendBroadcast(intent1);
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

  /*UPDATE USER VCARD/ PROFILE DATA*/

    private void setupUiThreadBroadCastUpdateVcard()
    {

        uiThreadUpdateVcardReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Check if the Intents purpose is to send the message.
                String action = intent.getAction();
                if( action.equals(RoosterConnectionService.UPDATE_USER_PROFILE_DATA))
                {
                    updateUserVCard(intent.getStringExtra(RoosterConnectionService.VCARD_NAME),
                            intent.getStringExtra(RoosterConnectionService.VCARD_EMAIL),
                            intent.getStringExtra(RoosterConnectionService.VCARD_MOBILE_NO));
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(RoosterConnectionService.UPDATE_USER_PROFILE_DATA);
        mApplicationContext.registerReceiver(uiThreadUpdateVcardReceiver,filter);
    }

  public void updateUserVCard(String name,String email,String mobileNo){
      ProviderManager.addIQProvider("vCard", "vcard-temp", new VCardProvider());

      VCard vCard=null;
      try {
          VCardManager vCardManager=VCardManager.getInstanceFor(mConnection);
          vCard=vCardManager.loadVCard(mUsername+"@dell29");
          vCard.setNickName(name);
          if(!TextUtils.isEmpty(email))
            vCard.setEmailHome(email);
          if(!TextUtils.isEmpty(mobileNo))
            vCard.setPhoneHome("Phone",mobileNo);

          vCardManager.saveVCard(vCard);

          //Bundle up the intent and send the broadcast.
          Intent intent = new Intent(RoosterConnectionService.LOAD_VCARD);
          mApplicationContext.sendBroadcast(intent);

      } catch (SmackException.NoResponseException e) {
          e.printStackTrace();
      } catch (XMPPException.XMPPErrorException e) {
          e.printStackTrace();
      } catch (SmackException.NotConnectedException e) {
          e.printStackTrace();
      }
  }


    /*UPLOAD USER PROFILE IMAGE*/

    private void setupUiThreadBroadCastUploadUserProfileImage()
    {

        uiThreadUploadProfileImageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Check if the Intents purpose is to send the message.
                String action = intent.getAction();
                if( action.equals(RoosterConnectionService.UPLOAD_USER_PROFILE_IMAGE))
                {
                    uploadUserProfileImage(intent.getStringExtra(RoosterConnectionService.VCARD_PROFILE_IMAGE));
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(RoosterConnectionService.UPLOAD_USER_PROFILE_IMAGE);
        mApplicationContext.registerReceiver(uiThreadUploadProfileImageReceiver,filter);
    }


  public void uploadUserProfileImage(final String profileImagePath) {

      ProviderManager.addIQProvider("vCard", "vcard-temp", new VCardProvider());
      VCard vCard = null;
      try {
          VCardManager vCardManager = VCardManager.getInstanceFor(mConnection);
          vCard = vCardManager.loadVCard(mUsername + "@dell29");
          if (profileImagePath != null) {
              ByteArrayOutputStream baos = new ByteArrayOutputStream();
              BitmapFactory.decodeFile(profileImagePath).compress(Bitmap.CompressFormat.JPEG, 80, baos);
              //this will convert image to byte[]
              byte[] byteArrayImage = baos.toByteArray();
              vCard.setAvatar(byteArrayImage);
          }
          vCardManager.saveVCard(vCard);

          Utils.dismissLoader();

          //Bundle up the intent and send the broadcast.
          Intent intent = new Intent(RoosterConnectionService.LOAD_VCARD);
          mApplicationContext.sendBroadcast(intent);

      } catch (SmackException.NoResponseException e) {
          e.printStackTrace();
      } catch (XMPPException.XMPPErrorException e) {
          e.printStackTrace();
      } catch (SmackException.NotConnectedException e) {
          e.printStackTrace();
      }

  }


   /*GET USER LAST SEEN TIME*/

    private void setupUiThreadBroadCastLastSeen()
    {

        uiThreadUserLastSeenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Check if the Intents purpose is to send the message.
                String action = intent.getAction();
                if( action.equals(RoosterConnectionService.GET_USER_LAST_SEEN))
                {
                    getLastSeen(intent.getStringExtra(RoosterConnectionService.USER_JID));
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(RoosterConnectionService.GET_USER_LAST_SEEN);
        mApplicationContext.registerReceiver(uiThreadUserLastSeenReceiver,filter);
    }

    public void getLastSeen(String jid){

        try {
            LastActivity lastActivity=LastActivityManager.getInstanceFor(mConnection).getLastActivity(jid);
            Log.d("last seen",""+lastActivity.lastActivity);
            Intent intent=new Intent(RoosterConnectionService.CHAT_STATUS);
            intent.putExtra("from",jid.split("@")[0]);

            if(lastActivity.lastActivity>0){
                Date d = new Date(System.currentTimeMillis()-lastActivity.lastActivity*1000);
                intent.putExtra(RoosterConnectionService.CHAT_STATUS,"Last Seen at "+ Utils.getFormattedTime(d.getTime())/*DateFormat.format("hh:mm a", d).toString()*/);
            }else if(lastActivity.lastActivity==0){
                intent.putExtra(RoosterConnectionService.CHAT_STATUS,"Online");
            }
            mApplicationContext.sendBroadcast(intent);
        }  catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        }

    }

    public void disconnect()
    {
        Log.d(TAG,"Disconnecting from server "+ mServiceName);
        try
        {
            if (mConnection != null)
            {
                mConnection.disconnect();

            }

        }catch(Exception e){}/*catch (SmackException.NotConnectedException e)
        {
            RoosterConnectionService.sConnectionState=ConnectionState.DISCONNECTED;
            e.printStackTrace();

        }*/
        mConnection = null;
        // Unregister the message broadcast receiver.
        if( uiThreadMessageReceiver != null)
        {
            mApplicationContext.unregisterReceiver(uiThreadMessageReceiver);
            uiThreadMessageReceiver = null;
        }

    }


    @Override
    public void connected(XMPPConnection connection) {
        RoosterConnectionService.sConnectionState=ConnectionState.CONNECTED;
        Log.d(TAG,"Connected Successfully");
        boolean isLoggedIn = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getBoolean("xmpp_logged_in",false);
        if(isLoggedIn) {
            try {
                mConnection.login();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (XMPPException.XMPPErrorException e) {
                e.printStackTrace();
            } catch (XMPPException e) {
                e.printStackTrace();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
            } catch (SmackException e) {
                e.printStackTrace();
            }
        }
        else{
                registerUser();
            }

    }

    @Override
    public void authenticated(XMPPConnection connection, boolean resumed) {
        super.authenticated(connection, resumed);

        RoosterConnectionService.sConnectionState=ConnectionState.CONNECTED;
        Log.d(TAG,"Authenticated Successfully");
        showContactListActivityWhenAuthenticated();

    /*for user presence online*/

        try {
            presence = new Presence(Presence.Type.available);
            presence.setMode(Presence.Mode.available);
            mConnection.sendPacket(presence);


            StanzaFilter filter = new StanzaTypeFilter(Message.class);
            StanzaListener listener = new StanzaListener() {
                @Override
                public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                    Message message= (Message) packet;
                    System.out.println("offline Message : "+message.getBody());


                    Intent intent = new Intent(RoosterConnectionService.NEW_MESSAGE);
                    intent.setPackage(mApplicationContext.getPackageName());
                    intent.putExtra(RoosterConnectionService.BUNDLE_FROM_JID,message.getFrom());
                    intent.putExtra(RoosterConnectionService.BUNDLE_TO_JID,message.getTo());
                    intent.putExtra("messageId",message.getStanzaId());

                    intent.putExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY,message.getBody());
                    mApplicationContext.sendBroadcast(intent);

                    if (message.getBody()!=null && !message.getBody().equalsIgnoreCase("")) {
                        dbHandler.addChatHistory(message.getTo(), message.getFrom().split("@")[0], message.getBody(), "", "1", null,"false","",0);
                        Intent intent1=new Intent(RoosterConnectionService.BUDDYLIST_UPDATE);
                        mApplicationContext.sendBroadcast(intent1);
                    }
                }
            };
            mConnection.addSyncStanzaListener(listener,filter);

        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } /*catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        }*/
        catch (Exception e){
           e.printStackTrace();
        }
    }

    @Override
    public void connectionClosed() {
        RoosterConnectionService.sConnectionState=ConnectionState.DISCONNECTED;
        Log.d(TAG,"Connectionclosed()");
/*for user presence offline*/

       /* try {
            presence = new Presence(Presence.Type.unavailable);
            mConnection.sendPacket(presence);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        RoosterConnectionService.sConnectionState=ConnectionState.DISCONNECTED;
        Log.d(TAG,"ConnectionClosedOnError, error "+ e.toString());

    }

    @Override
    public void reconnectingIn(int seconds) {
        RoosterConnectionService.sConnectionState = ConnectionState.CONNECTING;
        Log.d(TAG,"ReconnectingIn() ");

    }

    @Override
    public void reconnectionSuccessful() {
        RoosterConnectionService.sConnectionState = ConnectionState.CONNECTED;
        Log.d(TAG,"ReconnectionSuccessful()");
/*for user presence online*/

        try {
            presence = new Presence(Presence.Type.available);
            mConnection.sendPacket(presence);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reconnectionFailed(Exception e) {
        RoosterConnectionService.sConnectionState = ConnectionState.DISCONNECTED;
        Log.d(TAG,"ReconnectionFailed()");
    }

    private void showContactListActivityWhenAuthenticated()
    {
        Intent i = new Intent(RoosterConnectionService.UI_AUTHENTICATED);
        i.setPackage(mApplicationContext.getPackageName());
        mApplicationContext.sendBroadcast(i);
        Log.d(TAG,"Sent the broadcast that we are authenticated");
    }

 /*CREATE USER */
   public void createUser(){
       VCard vcard = new VCard();

       ProviderManager.addIQProvider("vCard", "vcard-temp", new VCardProvider());

       String username = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
               .getString("xmpp_jid",null);
       String email = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
               .getString("xmpp_email",null);
       String nickName = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
               .getString("xmpp_name",null);
       String password = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
               .getString("xmpp_password",null);
       String imagePath = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
               .getString("xmpp_profileImageFilePath",null);

      Bitmap bitmap=Utils.loadSampledImage(imagePath);
           ByteArrayOutputStream stream = new ByteArrayOutputStream();
           bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
           byte[] image_path= stream.toByteArray();


       try {
           vcard.load(mConnection,username);

       vcard.setFirstName("" + username);
       vcard.setEmailHome("" + email);
       vcard.setNickName("" + nickName);
       vcard.setAvatar(image_path); //Image Path should be URL or Can be Byte Array etc.
       vcard.save(mConnection);

       } catch (SmackException.NoResponseException e) {
           e.printStackTrace();
       } catch (XMPPException.XMPPErrorException e) {
           e.printStackTrace();
       } catch (SmackException.NotConnectedException e) {
           e.printStackTrace();
       }
   }



    public String registerUser() {
        String username = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_jid",null);
        String email = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_email",null);
        String nickName = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_name",null);
        String password = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_password",null);
        String imagePath = PreferenceManager.getDefaultSharedPreferences(mApplicationContext)
                .getString("xmpp_profileImageFilePath",null);

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("username", username);
        attributes.put("name", nickName);
        attributes.put("email", "");
        attributes.put("password", password);


     if (mConnection == null)
            return "0";
        Registration reg = new Registration(attributes);
        reg.setType(IQ.Type.set);
        reg.setTo("dell29"); //  for live server we have to change service name according to server name
        // Note the createAccount registration, parameter is UserName, not Jid, is " @" the front part of the.

        // This addAttribute cannot be empty, otherwise an error. So do mark is Android mobile phone created！！！！！

        StanzaFilter filter =  new AndFilter(new StanzaIdFilter(
                reg.getStanzaId()), new StanzaTypeFilter(IQ.class));
        PacketCollector collector = mConnection.createPacketCollector(
                filter);

        try {
     //       mConnection.sendPacket(reg);
            mConnection.sendStanza(reg);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        IQ result = (IQ) collector.nextResult(10000);
        // Stop queuing Results stop request results (whether successful results)
        collector.cancel();
        if (result == null) {
            Log.e("regist", "No response from server.");
            return "0";
        } else if (result.getType() == IQ.Type.result) {
            Log.v("regist", "regist success.");

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mApplicationContext);
            prefs.edit()
                    .putBoolean("xmpp_logged_in", true).commit();

            Log.d("user created","Successfully");
            try {
                mConnection.login();
               VCardManager vCardManager=VCardManager.getInstanceFor(mConnection);
                VCard vCard=
                    vCardManager.loadVCard(username+"@dell29");

                    vCard.setTo(username);
                    vCard.setNickName(nickName);
                    vCard.setEmailHome(email);

                    vCardManager.saveVCard(vCard);

            } catch (XMPPException e) {
                e.printStackTrace();
            } catch (SmackException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "1";
        } else { // if (result.getType() == IQ.Type.ERROR)
            if (result.getError().toString().equalsIgnoreCase("conflict(409)")) {
                Log.e("regist", "IQ.Type.ERROR: "
                        + result.getError().toString());
                return "2";
            } else {
                Log.e("regist", "IQ.Type.ERROR: "
                        + result.getError().toString());
                return "3";
            }
        }
    }


  /*FILE RECEIVING CODE*/

    public void fileReciever(XMPPTCPConnection connection) {
        //**************************************

        FileTransferManager manager = FileTransferManager.getInstanceFor(connection);
        manager.addFileTransferListener(new FileTransferListener() {
     public void fileTransferRequest(final FileTransferRequest request) {
         new Thread(){
             @Override
            public void run() {
                                       IncomingFileTransfer transfer = request.accept();
                                       File mf = Environment.getExternalStorageDirectory();
                 File file=null;
                 if(transfer.getFileName().contains("png") || transfer.getFileName().contains("PNG")
                         || transfer.getFileName().contains("jpg")|| transfer.getFileName().contains("JPG") ||
                         transfer.getFileName().contains("jpeg") ||  transfer.getFileName().contains("JPEG") ||
                         transfer.getFileName().contains("gif"))
                     file = new File(mf.getAbsoluteFile()+"/DCIM/Camera/" + transfer.getFileName());
                 else
                     file = new File(mf.getAbsoluteFile()+"/" + transfer.getFileName());

                 Log.d("file status","file received with name "+transfer.getFileName() );
                 try{
                     transfer.recieveFile(file);
                     while(!transfer.isDone()) {
                         try{
                             Thread.sleep(1000L);
                         }catch (Exception e) {
                             Log.e("", e.getMessage());
                         }
                         if(transfer.getStatus().equals(FileTransfer.Status.error)) {
                             Log.e("ERROR!!! ", transfer.getError() + "");
                         }
                         if(transfer.getException() != null) {
                             transfer.getException().printStackTrace();
                         }
                     }

                     if(transfer.isDone()){
                         Intent intent1=null;
                         if(transfer.getFileName().contains("png") || transfer.getFileName().contains("PNG")
                                 || transfer.getFileName().contains("jpg")|| transfer.getFileName().contains("JPG") ||
                                 transfer.getFileName().contains("jpeg") ||  transfer.getFileName().contains("JPEG") ||
                                 transfer.getFileName().contains("gif")) {
                             intent1 = new Intent(RoosterConnectionService.NEW_IMAGE);
                             dbHandler.addChatHistory(mUsername,request.getRequestor().split("@")[0],"Image",mf.getAbsoluteFile()+"/DCIM/Camera/" + transfer.getFileName(),"1",null,"false","",1); // DUMMY DATA FOR DELIVERY STATUS UPDATE

                             Intent intent2=new Intent(RoosterConnectionService.BUDDYLIST_UPDATE);
                             mApplicationContext.sendBroadcast(intent2);
                         }
                         else {
                             intent1 = new Intent(RoosterConnectionService.NEW_FILE);
                             dbHandler.addChatHistory(mUsername,request.getRequestor().split("@")[0],"Document",null,"1",mf.getAbsoluteFile()+"/" + transfer.getFileName(),"false","",1);
                             Intent intent3=new Intent(RoosterConnectionService.BUDDYLIST_UPDATE);
                             mApplicationContext.sendBroadcast(intent3);
                         }

                         intent1.setPackage(mApplicationContext.getPackageName());
                         intent1.putExtra(RoosterConnectionService.BUNDLE_TO_JID,mUsername);
                         intent1.putExtra(RoosterConnectionService.BUNDLE_FROM_JID,request.getRequestor());
                         intent1.putExtra(RoosterConnectionService.BUNDLE_MESSAGE_BODY,"");
                         if(transfer.getFileName().contains("png") || transfer.getFileName().contains("PNG")
                                 || transfer.getFileName().contains("jpg")|| transfer.getFileName().contains("JPG") ||
                                 transfer.getFileName().contains("jpeg") ||  transfer.getFileName().contains("JPEG") ||
                                 transfer.getFileName().contains("gif"))
                             intent1.putExtra(RoosterConnectionService.IMAGE_BITMAP, mf.getAbsoluteFile()+"/DCIM/Camera/" + transfer.getFileName());
                         else
                             intent1.putExtra(RoosterConnectionService.IMAGE_BITMAP, mf.getAbsoluteFile()+"/" + transfer.getFileName());

                         Log.d("file receiver ID",transfer.getStreamID());
                         intent1.putExtra("ImagePath",transfer.getFileName());
                         mApplicationContext.sendBroadcast(intent1);

                         Message msg= new Message();
                         msg.setTo(request.getRequestor().split("/")[0]);
                         msg.setFrom(mUsername+"@dell29");
                         msg.setSubject("file_transfer_indicator_message");
                         msg.setStanzaId(/*transfer.getFileName()*/transfer.getStreamID());
                         Chat chat = ChatManager.getInstanceFor(mConnection)
                                 .createChat(request.getRequestor().split("/")[0]);
                         try {
                             chat.sendMessage(msg);
                         } catch (SmackException.NotConnectedException e) {
                             e.printStackTrace();
                         }

                     }
                 }catch (Exception e) {
                     Log.e("", e.getMessage());
                 }
                                  };
        }.start();
                     }
   });

    }


    /*FILE SEND CODE*/

    public synchronized void  fileTransfer(final String filenameWithPath, Bitmap thumbnail, final String userId) {

      new Thread(){
          @Override
          public void run() {
              try {
              boolean isIOSDevice=false,isIOSDevice1=false;
              Roster roster = Roster.getInstanceFor(mConnection);
              Presence presence=roster.getPresence(userId);
              String s_arr[]=presence.getFrom().split("/");
              if(s_arr.length>1){
                  if(!s_arr[1].equalsIgnoreCase("Smack"))
                      isIOSDevice=true;
              }


        FileTransferManager manager = FileTransferManager.getInstanceFor(mConnection);
        //    OutgoingFileTransfer transfer = manager.createOutgoingFileTransfer("usre2@myHost/Smack");
        OutgoingFileTransfer transfer = manager.createOutgoingFileTransfer(presence.getFrom()/*userId+"/localhost"*/ ); // "/Spark" is Resource Name
//        OutgoingFileTransfer transfer = manager.createOutgoingFileTransfer(userId + "/Directors-iMac");
        File file = new File(filenameWithPath);

            transfer.sendFile(file,"test file");

                  if(transfer.getFileName().contains("png") || transfer.getFileName().contains("PNG")
                          || transfer.getFileName().contains("jpg")|| transfer.getFileName().contains("JPG") ||
                          transfer.getFileName().contains("jpeg") ||  transfer.getFileName().contains("JPEG") ||
                          transfer.getFileName().contains("gif")) {

                      dbHandler.addChatHistory(mUsername, userId.split("@")[0], "Image", filenameWithPath, "0", null, "true", /*transfer.getFileName()*/filenameWithPath, -1);
                  }
                  else
                      dbHandler.addChatHistory(mUsername, userId.split("@")[0], "Document", null, "0",filenameWithPath,"true",/*transfer.getFileName()*/filenameWithPath,-1);

        while (!transfer.isDone()) {
            if (transfer.getStatus().equals(FileTransfer.Status.error)) {
                System.out.println("ERROR!!! " + transfer.getError());
            } else if (transfer.getStatus().equals(FileTransfer.Status.cancelled)
                    || transfer.getStatus().equals(FileTransfer.Status.refused)) {
                System.out.println("Cancelled!!! " + transfer.getError());
            }

             if(isIOSDevice){
                 dbHandler.updateMessageDeliveryID(mUsername.split("@")[0], userId.split("@")[0],filenameWithPath,transfer.getStreamID(),1);
                 isIOSDevice=false;
                 isIOSDevice1=true;
             }
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (transfer.getStatus().equals(FileTransfer.Status.refused) || transfer.getStatus().equals(FileTransfer.Status.error)
                || transfer.getStatus().equals(FileTransfer.Status.cancelled)) {
            System.out.println("refused cancelled error " + transfer.getError());
            System.out.println("transfer status "+transfer.getStatus());
        } else {

            System.out.println("Success");
            Log.d("fileName",transfer.getFileName());
           /* if(transfer.getFileName().contains("png") || transfer.getFileName().contains("PNG")
                    || transfer.getFileName().contains("jpg")|| transfer.getFileName().contains("JPG") ||
                    transfer.getFileName().contains("jpeg") ||  transfer.getFileName().contains("JPEG") ||
                    transfer.getFileName().contains("gif")) {

                dbHandler.addChatHistory(mUsername, userId.split("@")[0], "Image", filenameWithPath, "0", null, "true", *//*transfer.getFileName()*//*transfer.getStreamID(), 0);
            }
            else
                dbHandler.addChatHistory(mUsername, userId.split("@")[0], "Document", null, "0",filenameWithPath,"true",*//*transfer.getFileName()*//*transfer.getStreamID(),0);*/

     //       dbHandler.updateMessageDeliveryStatus(userId.split("@")[0], mUsername.split("@")[0],filenameWithPath,1);
            if(!isIOSDevice1)
               dbHandler.updateMessageDeliveryID(mUsername.split("@")[0], userId.split("@")[0],filenameWithPath,transfer.getStreamID(),1);
            Intent intent1 = new Intent(RoosterConnectionService.CHAT_HISTORY_LIST_REFRESH);
            intent1.setPackage(mApplicationContext.getPackageName());

            Log.d("file transfer ID",transfer.getStreamID());
        }
              } catch (SmackException e) {
                  e.printStackTrace();
              }
              catch (Exception e){
                  e.printStackTrace();

                  if(filenameWithPath.contains("png") || filenameWithPath.contains("PNG")
                          || filenameWithPath.contains("jpg")|| filenameWithPath.contains("JPG") ||
                          filenameWithPath.contains("jpeg") ||  filenameWithPath.contains("JPEG") ||
                          filenameWithPath.contains("gif")) {

                      dbHandler.addChatHistory(mUsername, userId.split("@")[0], "Image", filenameWithPath, "0", null, "true", /*transfer.getFileName()*/filenameWithPath, -1);
                  }
                  else
                      dbHandler.addChatHistory(mUsername, userId.split("@")[0], "Document", null, "0",filenameWithPath,"true",/*transfer.getFileName()*/filenameWithPath,-1);   // filenameWithPath used as id here only for image failed case
              }
          };

      }.start();
    }
}
