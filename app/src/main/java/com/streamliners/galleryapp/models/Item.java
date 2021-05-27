package com.streamliners.galleryapp.models;

import android.graphics.Bitmap;

public class Item {

    public String url;
    public int color;
    public String label;


    public Item(String url, int color, String label) {
        this.url = url;
        this.color = color;
        this.label = label;
    }
}
