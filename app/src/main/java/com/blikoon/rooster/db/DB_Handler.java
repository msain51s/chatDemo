package com.blikoon.rooster.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.blikoon.rooster.AppController;
import com.blikoon.rooster.Contact;

import java.util.ArrayList;

/**
 * Created by Administrator on 4/18/2017.
 */

public class DB_Handler extends SQLiteOpenHelper {
    String mUserName;
    public DB_Handler(Context context) {
        super(context, DB_Data.DB_NAME, null, DB_Data.DB_VERSION);
        if(PreferenceManager.getDefaultSharedPreferences(AppController.mInstance)
                .getString("xmpp_jid",null)!=null)
        mUserName= PreferenceManager.getDefaultSharedPreferences(AppController.mInstance)
                .getString("xmpp_jid",null).split("@")[0];
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_Data.getCreateChatTable());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DB_Data.getDropTableChat());
        onCreate(db);
    }

    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

/*GET CHAT HISTORY*/

    public ArrayList<ChatMessage> getChatHistory(String sender,String receiver) {

        ArrayList<ChatMessage> list = new ArrayList<ChatMessage>();

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(DB_Data.getChatHistory_query(), new String[]{sender,receiver});

        ChatMessage obj = new ChatMessage();

        if (cursor.moveToFirst()) {
            do {

                obj = new ChatMessage(cursor.getInt(0),cursor.getString(2),
                                      cursor.getString(3),cursor.getLong(4),
                                      cursor.getString(5),cursor.getInt(6)
                        ,cursor.getString(7)
                        ,cursor.getString(8),cursor.getString(9),cursor.getInt(10));

                list.add(obj);
            } while (cursor.moveToNext());

            for (int i = list.size(); i > cursor.getCount(); i--) {
                list.remove(i);
            }
            cursor.close();
        }
        db.close();

        return list;

    }

    /*ADD CHAT HISTORY TO DB*/

    public void addChatHistory(String sender,String receiver,String message,String fileName,String status,String docsFileName,String readStatus,String messageDeliveryReceiptId,int messageDeliveryStatus) {

        SQLiteDatabase db=null;
        try {
            ContentValues values = new ContentValues();
            values.put(DB_Data.COLUMN_SENDER_NAME, sender);
            values.put(DB_Data.COLUMN_RECEIVER_NAME, receiver);
            values.put(DB_Data.COLUMN_TIME_STAMP, System.currentTimeMillis());
            values.put(DB_Data.COLUMN_MESSAGE, message);
            values.put(DB_Data.COLUMN_IMAGE_FILE_NAME, fileName);
            values.put(DB_Data.COLUMN_SENT_OR_RECEIVED_MESSAGE, status);
            values.put(DB_Data.COLUMN_DOCUMENT_FILE_NAME, docsFileName);
            values.put(DB_Data.COLUMN_MESSAGE_FLAG, readStatus);
            values.put(DB_Data.COLUMN_MESSAGE_DELIVERY_RECEIPT_ID, messageDeliveryReceiptId);
            values.put(DB_Data.COLUMN_MESSAGE_DELIVERY_STATUS, messageDeliveryStatus);
             db = this.getWritableDatabase();

            db.insert(DB_Data.TABLE_NAME, null, values);
        }catch (Exception e){
            e.printStackTrace();
        }
        /*finally {
            db.close();
        }*/

    }

/*GET UNREAD CHAT MESSAGE COUNT*/

    public String getUnReadMessageCount(String sender,String receiver) {
        String qty = "";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(DB_Data.getUnReadMessage_query(),
                new String[]{"false",sender, receiver});

        ChatMessage data = new ChatMessage();
        if (cursor.moveToFirst()) {
            data.setID(cursor.getInt(0));
            Integer i=data.getID();
            qty=i.toString();
        }
        cursor.close();
        db.close();
        return qty;
    }

 /*UPDATE READ MESSAGE COUNT FLAG*/

    public void updateReadMessageCountFlag(String sender,String receiver) {
        ContentValues values = new ContentValues();
        values.put(DB_Data.COLUMN_MESSAGE_FLAG, "true");
        SQLiteDatabase db = this.getWritableDatabase();
        db.update(DB_Data.TABLE_NAME, values, DB_Data.COLUMN_MESSAGE_FLAG + " = ? AND "
                        +DB_Data.COLUMN_SENDER_NAME+"=?"+" AND "
                        +DB_Data.COLUMN_RECEIVER_NAME+"=?",
                new String[]{"false", sender, receiver});

        db.close();

    }

     /* DELIVERED/SEEN MESSAGE STATUS UPDATE*/

    public void updateMessageDeliveryStatus(String sender,String receiver,String messageReceiptId,int statusId) {
        ContentValues values = new ContentValues();
        values.put(DB_Data.COLUMN_MESSAGE_DELIVERY_STATUS, statusId);
        SQLiteDatabase db = this.getWritableDatabase();
       long i= db.update(DB_Data.TABLE_NAME, values, DB_Data.COLUMN_MESSAGE_DELIVERY_RECEIPT_ID + " = ? AND "
                        +DB_Data.COLUMN_SENDER_NAME+"=?"+" AND "
                        +DB_Data.COLUMN_RECEIVER_NAME+"=?",
                new String[]{messageReceiptId, sender, receiver});

        Log.d("row updated count",""+i);

   //     db.close();

    }

    /* DELIVERED/SEEN MESSAGE STATUS UPDATE*/

    public void updateMessageDeliveryID(String sender,String receiver,String messageReceiptId,String updatedMessageReceiptId,int statusId) {
        ContentValues values = new ContentValues();
        values.put(DB_Data.COLUMN_MESSAGE_DELIVERY_RECEIPT_ID, updatedMessageReceiptId);
        values.put(DB_Data.COLUMN_MESSAGE_DELIVERY_STATUS, statusId);
        SQLiteDatabase db = this.getWritableDatabase();
        long i= db.update(DB_Data.TABLE_NAME, values, DB_Data.COLUMN_MESSAGE_DELIVERY_RECEIPT_ID + " = ? AND "
                        +DB_Data.COLUMN_SENDER_NAME+"=?"+" AND "
                        +DB_Data.COLUMN_RECEIVER_NAME+"=?",
                new String[]{messageReceiptId, sender, receiver});

        Log.d("row updated count",""+i);

    //    db.close();

    }
    public void updateMessageDeliveryStatus(String sender,String receiver,int statusId) {
        ContentValues values = new ContentValues();
        values.put(DB_Data.COLUMN_MESSAGE_DELIVERY_STATUS, statusId);
        SQLiteDatabase db = this.getWritableDatabase();
        long i= db.update(DB_Data.TABLE_NAME, values, DB_Data.COLUMN_MESSAGE_DELIVERY_STATUS + " = ? AND "
                        +DB_Data.COLUMN_SENDER_NAME+"=?"+" AND "
                        +DB_Data.COLUMN_RECEIVER_NAME+"=?",
                new String[]{"1", sender, receiver});

        Log.d("row updated count",""+i);

    //    db.close();

    }

    /*GET DELIVERED CHAT MESSAGE RECEIPT IDS */

    public ArrayList<String> getDeliveredMessageReceiptIds(String sender,String receiver) {

        ArrayList<String> list = new ArrayList<String>();

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(DB_Data.getDeliverdChatMessage_query(), new String[]{sender,receiver,"1"});

        if (cursor.moveToFirst()) {
            do {

                list.add(cursor.getString(0));
            } while (cursor.moveToNext());

            for (int i = list.size(); i > cursor.getCount(); i--) {
                list.remove(i);
            }
            cursor.close();
        }
 //       db.close();

        return list;

    }

}
