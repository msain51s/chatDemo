package com.blikoon.rooster.db;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.format.DateFormat;

import com.blikoon.rooster.Utils;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.net.URI;
import java.util.Date;

/**
 * Created by Administrator on 4/13/2017.
 */

public class ChatMessage {
    private String message;
    private long timestamp;
    private Status status;

    public int getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(int deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    private int deliveryStatus;

    public String getMessageDeliveryReceiptId() {
        return messageDeliveryReceiptId;
    }

    public void setMessageDeliveryReceiptId(String messageDeliveryReceiptId) {
        this.messageDeliveryReceiptId = messageDeliveryReceiptId;
    }

    private String messageDeliveryReceiptId;

    public String getReadFlag() {
        return readFlag;
    }

    public void setReadFlag(String readFlag) {
        this.readFlag = readFlag;
    }

    private String readFlag;

    public String getDocumentFileName() {
        return documentFileName;
    }

    private String documentFileName;

    public String getImageFileName() {
        return imageFileName;
    }

    private String imageFileName;

    public int getStatusFlag() {
        return statusFlag;
    }

    public int statusFlag;

    public int getID() {
        return ID;
    }


    public void setID(int ID) {
        this.ID = ID;
    }

    private int ID;

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    private Bitmap imageBitmap;

    public String getImageUrl() {
        return imageUrl;
    }

    public Uri getImageURI() {
        return imageURI;
    }

    private String imageUrl;
    private Uri imageURI;

    public String getUsername() {
        return username;
    }

    private String username;

    public enum Status{
        SENT, RECEIVED
    }

    public ChatMessage(String message, long timestamp, Status status,String username){
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
        this.username=username;
    }

    public ChatMessage(){}

    public ChatMessage(String message, long timestamp, Status status,String username,Bitmap imgBitmap,int deliveryStatus,String imageFileName){
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
        this.username=username;
        this.imageBitmap=imgBitmap;
        this.deliveryStatus=deliveryStatus;
        this.imageFileName=imageFileName;
    }
/*FOR FILE SEND */
    public ChatMessage(String message, long timestamp, Status status,String username,Bitmap imgBitmap,String documentFileName,int deliveryStatus){
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
        this.username=username;
        this.imageBitmap=imgBitmap;
        this.documentFileName=documentFileName;
        this.deliveryStatus=deliveryStatus;

    }

    /*FOR DATABASE STORE PURPOSE*/
    public ChatMessage(int ID,String username,String message, long timestamp, String imageFile,int status,String documentFileName,String messageReadStatus,String messageReceiptId,int messageDeliveryStatus){
        this.message = message;
        this.timestamp = timestamp;
        this.statusFlag=status;
        if(status==0)
            this.status = Status.SENT;
        else
            this.status = Status.RECEIVED;
        this.username=username;
        this.imageFileName=imageFile;
        this.imageBitmap= Utils.loadSampledImage(imageFile);//BitmapFactory.decodeFile(imageFile);
        this.documentFileName=documentFileName;
        this.readFlag=messageReadStatus;
        this.messageDeliveryReceiptId=messageReceiptId;
        this.deliveryStatus=messageDeliveryStatus;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getFormattedTime(){

        long oneDayInMillis = 24 * 60 * 60 * 1000;
        long timeDifference = System.currentTimeMillis() - timestamp;
        Date startDate=new Date(timestamp);
        Date endDate=new Date(System.currentTimeMillis());

        if(startDate.compareTo(endDate)!=0) {
            return DateFormat.format("dd MMM yyyy - hh:mm a", timestamp).toString();
        }
        else if( timeDifference < oneDayInMillis ) {
            return "Today - "+DateFormat.format("hh:mm a", timestamp).toString();
        }else{
            return DateFormat.format("dd MMM yyyy - hh:mm a", timestamp).toString();
        }
    }
}

