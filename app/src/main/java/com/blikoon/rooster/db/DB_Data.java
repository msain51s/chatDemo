package com.blikoon.rooster.db;

/**
 * Created by Administrator on 4/18/2017.
 */

public class DB_Data {
    public final static String DB_NAME="intelliChat";
    public final static int DB_VERSION = 4;
    public static final String TABLE_NAME = "chatHistory_table";

    /*TABLE COLUMN*/
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SENDER_NAME = "semder_name";
    public static final String COLUMN_RECEIVER_NAME = "receiver_name";
    public static final String COLUMN_MESSAGE = "chat_message";
    public static final String COLUMN_TIME_STAMP = "chat_message_timeStamp";
    public static final String COLUMN_IMAGE_FILE_NAME = "chatImageFileName";
    public static final String COLUMN_SENT_OR_RECEIVED_MESSAGE = "chat_sentOrReceivedMessage";
    public static final String COLUMN_DOCUMENT_FILE_NAME = "chatDocumentFileName";
    public static final String COLUMN_MESSAGE_FLAG = "messageFlag";
    public static final String COLUMN_MESSAGE_DELIVERY_RECEIPT_ID = "messageDeliveryReceiptId";
    public static final String COLUMN_MESSAGE_DELIVERY_STATUS = "messageDeliveryStatus";


    /*CREATE ORDER TABLE QUERY*/
    public static String CREATE_CHAT_TABLE = "CREATE TABLE " + TABLE_NAME
            + "(" + COLUMN_ID + " INTEGER PRIMARY KEY,"+ COLUMN_SENDER_NAME + " TEXT,"
            + COLUMN_RECEIVER_NAME + " TEXT," + COLUMN_MESSAGE + " TEXT,"
            + COLUMN_TIME_STAMP + " TEXT," + COLUMN_IMAGE_FILE_NAME + " INTEGER,"+
            COLUMN_SENT_OR_RECEIVED_MESSAGE+" TEXT,"+COLUMN_DOCUMENT_FILE_NAME+" TEXT,"+COLUMN_MESSAGE_FLAG+" TEXT,"+COLUMN_MESSAGE_DELIVERY_RECEIPT_ID+" TEXT,"+COLUMN_MESSAGE_DELIVERY_STATUS+" TEXT )";

    /*DROP TABLE QUERY*/
    public static String DROP_TABLE_ORDER="DROP TABLE IF EXISTS " + TABLE_NAME;

    public static String getCreateChatTable() {
        return CREATE_CHAT_TABLE;
    }

    public static String getDropTableChat() {
        return DROP_TABLE_ORDER;
    }

    public static String getChatHistory_query()
    {
        String orderListQuery = " select * from " + TABLE_NAME +" where "+COLUMN_SENDER_NAME+"=?"+" AND "+COLUMN_RECEIVER_NAME+"=?";
        return orderListQuery;
    }

    public static String getUnReadMessage_query()
    {
        String query = "Select count(_id) FROM " + TABLE_NAME+ " where "+COLUMN_MESSAGE_FLAG+"= ? AND "
                +COLUMN_SENDER_NAME+"=?"+" AND "+COLUMN_RECEIVER_NAME+"=?";
        return query;
    }

    public static String getDeliverdChatMessage_query()
    {
        String orderListQuery = " select "+COLUMN_MESSAGE_DELIVERY_RECEIPT_ID+" from " + TABLE_NAME +" where "+COLUMN_SENDER_NAME+"=?"+" AND "+COLUMN_RECEIVER_NAME+"=?"+" AND "+COLUMN_MESSAGE_DELIVERY_STATUS+"=?";
        return orderListQuery;
    }
}
