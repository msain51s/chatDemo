package com.blikoon.rooster;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by gakwaya on 4/16/2016.
 */
public class Contact implements Serializable{
    private String jid;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String userName;
    private String status;


    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    private byte[] imageData;


    public String getJid()
    {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }


}
