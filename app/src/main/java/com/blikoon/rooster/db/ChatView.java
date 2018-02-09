package com.blikoon.rooster.db;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.blikoon.rooster.R;

import java.util.ArrayList;

import co.devcenter.androiduilibrary.ChatViewEventListener;
import co.devcenter.androiduilibrary.SendButton;

/**
 * Created by Administrator on 4/11/2017.
 */

public class ChatView extends LinearLayout {

    private ChatViewListAdapter chatViewListAdapter;
    private ListView chatListView;

    private EditText inputEditText;
    private SendButton sendButton;
    private ChatViewEventListener eventListener;
    private boolean previousFocusStatus = false;
    private View typeMessageView;
    private Context mContext;


    public ChatView(Context context){
        super(context);
        init(context);
        this.mContext=context;
    }

    public ChatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        this.mContext=context;
    }

    public ChatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
        this.mContext=context;
    }



    private void init(Context context){
        LayoutInflater inflater = LayoutInflater.from(context);
        View view=inflater.inflate(co.devcenter.androiduilibrary.R.layout.chat_view, this);
        view.setBackgroundColor(Color.parseColor("#f7eeef"));

        chatListView = (ListView)findViewById(co.devcenter.androiduilibrary.R.id.chat);
        inputEditText = (EditText)findViewById(co.devcenter.androiduilibrary.R.id.inputEditText);
        sendButton = (SendButton) findViewById(co.devcenter.androiduilibrary.R.id.sendButton);
        chatListView.setBackgroundColor(Color.parseColor("#f7efef"));
    //    chatListView.setBackground(context.getResources().getDrawable(R.drawable.bg));
        sendButton.setColorNormal(context.getResources().getColor(R.color.colorPrimaryDark));
        inputEditText.setHint("Enter Message");
        typeMessageView=inputEditText.getRootView();
        typeMessageView.setBackgroundColor(Color.parseColor("#ffffff"));

        chatViewListAdapter = new ChatViewListAdapter(context);
        chatListView.setAdapter(chatViewListAdapter);
    }


    public void setEventListener(final ChatViewEventListener eventListener){
        this.eventListener = eventListener;
        setUserTypingListener();
        setUserStoppedTypingListener();
    }

    public String getTypedString(){
        return  inputEditText.getText().toString();
    }

    public void sendMessage(String message, String from, Bitmap imageBitmap,int deliveryStatus,long timeStamp,String imageFileName){
        ChatMessage chatMessage = new ChatMessage(message, timeStamp/*System.currentTimeMillis()*/, ChatMessage.Status.SENT,from,imageBitmap,deliveryStatus,imageFileName);
        chatViewListAdapter.addMessage(chatMessage);
        inputEditText.getText().clear();//setText("");
        /*if (inputEditText.length() > 0) {
            TextKeyListener.clear(inputEditText.getText());
        }*/
    //    hideKeyBoard();
    }

    public void sendMessage(String message, String from, Bitmap imageBitmap,String documentFileName,int deliveryStatus,long timeStamp){
        ChatMessage chatMessage = new ChatMessage(message, timeStamp, ChatMessage.Status.SENT,from,imageBitmap,documentFileName,deliveryStatus);
        chatViewListAdapter.addMessage(chatMessage);
        inputEditText.getText().clear();//setText("");
       /* if (inputEditText.length() > 0) {
            TextKeyListener.clear(inputEditText.getText());
        }*/
     //   hideKeyBoard();
    }

    public void sendMessage(){
        ChatMessage chatMessage = new ChatMessage(inputEditText.getText().toString(), System.currentTimeMillis(), ChatMessage.Status.SENT,"");
        inputEditText.getText().clear();//setText("");
        /*if (inputEditText.length() > 0) {
            TextKeyListener.clear(inputEditText.getText());
        }*/
     //   hideKeyBoard();
        chatViewListAdapter.addMessage(chatMessage);
    }

    public void hideKeyBoard(){
        InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(inputEditText.getWindowToken(), 0);
    }
    public void receiveMessage(String message,String from){
        ChatMessage chatMessage = new ChatMessage(message, System.currentTimeMillis(), ChatMessage.Status.RECEIVED,from);
        chatViewListAdapter.addMessage(chatMessage);
    }

    public void receiveMessage(String message, String from, Bitmap imageBitmap,int deliveryStatus,long timeStamp,String imageFileName){
        ChatMessage chatMessage = new ChatMessage(message, timeStamp, ChatMessage.Status.RECEIVED,from,imageBitmap,0,imageFileName);
        chatViewListAdapter.addMessage(chatMessage);
    }

    public void receiveMessage(String message, String from, Bitmap imageBitmap,String docsFileName,long timeStamp){
        ChatMessage chatMessage = new ChatMessage(message, timeStamp, ChatMessage.Status.RECEIVED,from,imageBitmap,docsFileName,0);
        chatViewListAdapter.addMessage(chatMessage);
    }

    public void clearChatView(){
        chatViewListAdapter.clearChatView();
    }

    public void refreshChatView(ArrayList<ChatMessage> list){
        chatViewListAdapter.refreshChatView(list);
    }

    public SendButton getSendButton(){
        return sendButton;
    }

    private void setUserTypingListener(){
        inputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0){
                    eventListener.userIsTyping();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    private void setUserStoppedTypingListener(){
        inputEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    previousFocusStatus = true;
                } else if (!hasFocus) {
                    previousFocusStatus = false;
                } else if (previousFocusStatus && !hasFocus) {
                    eventListener.userHasStoppedTyping();
                }
            }
        });
    }
}

