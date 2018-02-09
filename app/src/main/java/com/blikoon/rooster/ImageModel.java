package com.blikoon.rooster;

import java.io.Serializable;

/**
 * Created by Administrator on 11/25/2016.
 */
public class ImageModel implements Serializable{

    String uri;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    String imageUrl;

    public ImageModel(String uri) {
        this.uri = uri;
    }
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
