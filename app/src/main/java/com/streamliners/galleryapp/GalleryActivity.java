package com.streamliners.galleryapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.streamliners.galleryapp.databinding.ActivityGalleryBinding;
import com.streamliners.galleryapp.databinding.ChipColorBinding;
import com.streamliners.galleryapp.databinding.ChipLabelBinding;
import com.streamliners.galleryapp.databinding.DialogAddImageBinding;
import com.streamliners.galleryapp.databinding.ItemCardBinding;
import com.streamliners.galleryapp.models.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GalleryActivity extends AppCompatActivity {
    ActivityGalleryBinding b;
    SharedPreferences sharedPrefs;
    List<Item> itemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = ActivityGalleryBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        sharedPrefs = getPreferences(MODE_PRIVATE);
        getDataFromSharedPreference();

    }


    //Actions menu methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.addImage){
          showAddImageDialog();
            return  true;
        }
        return false;
    }

    /**
     * showing add image dialog
     */
    private void showAddImageDialog() {
        //set orientation portrait only
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        new AddImageDialog()
                .show(this, new AddImageDialog.OnCompleteListener() {
                    @Override
                    public void onImageAdded(Item item) {

                        inflateViewForItem(item);
                }

                    @Override
                    public void onError(String error) {
                    new MaterialAlertDialogBuilder(GalleryActivity.this)
                            .setTitle("Error")
                            .setMessage(error)
                            .show();
                    }
                });
    }

    /**
     * inflate and add view to layout
     * @param item
     */
    private void inflateViewForItem(Item item) {
        //make orientation default
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        //inflate layout
        ItemCardBinding binding = ItemCardBinding.inflate(getLayoutInflater());

        itemList.add(item);

        //bind data
        Glide.with(this)
                .asBitmap()
                .load(item.url)
                .into(binding.imageView);
        binding.title.setText(item.label);
        binding.title.setBackgroundColor(item.color);

        //Add it to the list
        b.list.addView(binding.getRoot());
    }

    /**
     * override on pause to save data in shared preference
     */
    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = sharedPrefs.edit();
        int numOfImg = itemList.size();
        editor.putInt(Constants.NUMOFIMG, numOfImg).apply();

        int counter = 0;
        for (Item item : itemList) {
            editor.putInt(Constants.COLOR + counter, item.color)
                    .putString(Constants.LABEL + counter, item.label)
                    .putString(Constants.IMAGE + counter, item.url)
                    .apply();
            counter++;
        }
        editor.commit();

    }

    /**
     * get data from shared preference
     */
    void getDataFromSharedPreference(){
        int itemCount = sharedPrefs.getInt(Constants.NUMOFIMG, 0);

        // Inflate all items from shared preferences
        for (int i = 0; i < itemCount; i++) {

            Item item = new Item(sharedPrefs.getString(Constants.IMAGE + i, "")
                    , sharedPrefs.getInt(Constants.COLOR + i, 0)
                    , sharedPrefs.getString(Constants.LABEL + i, ""));

            inflateViewForItem(item);
        }
    }

}
