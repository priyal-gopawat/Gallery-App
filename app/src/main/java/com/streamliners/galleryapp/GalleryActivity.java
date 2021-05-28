package com.streamliners.galleryapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
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
    private static final int PICK_IMAGE = 0;
    ActivityGalleryBinding b;
    SharedPreferences sharedPrefs;
    List<Item> itemList = new ArrayList<>();
   ItemCardBinding binding;

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
        getMenuInflater().inflate(R.menu.gallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addImage) {
            showAddImageDialog();
            return true;
        } else if (item.getItemId() == R.id.addImageFromGallery) {
            showAddImageFromGallery();
            return true;
        }
        return false;
    }

    /**
     * intent to open gallery
     */
    private void showAddImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE) {
                Uri selectedImage = data.getData();
                String uri = selectedImage.toString();
                new AddImageDialog().fetchDataForGallery(uri, this, new AddImageDialog.OnCompleteListener() {
                    @Override
                    public void onImageAdded(Item item) {
                        inflateViewForItem(item);
                    }

                    @Override
                    public void onError(String error) {

                    }
                });
            }
        }

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
     *
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
        registerForContextMenu(binding);
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
    void getDataFromSharedPreference() {
        int itemCount = sharedPrefs.getInt(Constants.NUMOFIMG, 0);

        // Inflate all items from shared preferences
        for (int i = 0; i < itemCount; i++) {

            Item item = new Item(sharedPrefs.getString(Constants.IMAGE + i, "")
                    , sharedPrefs.getInt(Constants.COLOR + i, 0)
                    , sharedPrefs.getString(Constants.LABEL + i, ""));

            inflateViewForItem(item);
        }
    }

    public void registerForContextMenu(ItemCardBinding b) {
        b.imageView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                binding = b;
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.send_menu, menu);
            }
        });

    }


    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.shareImage) {
            shareImage();
        }
        return true;
    }

    private void shareImage() {
        Bitmap bitmap = getBitmapFromView(binding.getRoot());
        String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "palette", "share palette");
        Uri bitmapUri = Uri.parse(bitmapPath);
        //Intent to send image
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
        startActivity(Intent.createChooser(intent, "Share"));
    }

    public static Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }
}