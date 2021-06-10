package com.streamliners.galleryapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.streamliners.galleryapp.databinding.ActivityGalleryBinding;
import com.streamliners.galleryapp.databinding.ChipColorBinding;
import com.streamliners.galleryapp.databinding.ChipLabelBinding;
import com.streamliners.galleryapp.databinding.DialogAddImageBinding;
import com.streamliners.galleryapp.databinding.ItemCardBinding;
import com.streamliners.galleryapp.models.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class GalleryActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 0;
    ActivityGalleryBinding b;
    SharedPreferences sharedPrefs;
    List<Item> itemList = new ArrayList<>();
    ItemCardBinding binding;
    Context context = this;
    ItemAdapter adapter;
    ItemTouchHelper.SimpleCallback dragCallback;
    ItemTouchHelper itemTouchHelper2;
    int MODE_ENABLE =1;
    int MODE_DISABLE=0;
    int MODE = 0;
    static final Integer WRITE_EXST = 0x3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = ActivityGalleryBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        getSupportActionBar().setTitle((Html.fromHtml("<font color=\"#FFFFFF\">" + getString(R.string.app_name) + "</font>")));

        sharedPrefs = getPreferences(MODE_PRIVATE);
        getDataFromSharedPreference();

        floatingActionButton();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == 3 || requestCode == 4) {
                shareImage();
            }
        }
        else Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show();
    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(GalleryActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            ActivityCompat.requestPermissions(GalleryActivity.this, new String[]{permission}, requestCode);
        } else {
            shareImage();
        }
    }

    private void floatingActionButton() {
        b.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(MODE==MODE_ENABLE){
                MODE=MODE_DISABLE;
                adapter.mode = MODE_DISABLE;
                b.floatingActionButton.setImageResource(R.drawable.ic_vertical2);
                List<ItemAdapter.CardViewHolder> holders = adapter.holders;

                for(int i=0;i<holders.size();i++){

                    holders.get(i).listenerSetter();
                }

                itemTouchHelper2.attachToRecyclerView(null);
            }
            else {
                MODE=MODE_ENABLE;
                adapter.mode = MODE_ENABLE;
                b.floatingActionButton.setImageResource(R.drawable.ic_drag_drop);
                List<ItemAdapter.CardViewHolder> holders = adapter.holders;

                for(int i=0;i<holders.size();i++){

                    holders.get(i).listenerSetter();
                }
                itemTouchHelper2.attachToRecyclerView(b.list);
            }

            }
        });

    }

    private void setUpRecyclerView() {

        adapter = new ItemAdapter(this, itemList);


        b.list.setLayoutManager(new LinearLayoutManager(this));

        itemTouchHelperCallback();

        b.list.setAdapter(adapter);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

    }


    //Actions menu methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });

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
        } else if (item.getItemId() == R.id.sortAlpha) {
            adapter.sortAlpha();
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
                        itemList.add(item);
                        setUpRecyclerView();

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
                        itemList.add(item);
                        b.noItems.setVisibility(View.GONE);
                        //make orientation default
                        setUpRecyclerView();

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
        if (itemCount > 0) {
            b.noItems.setVisibility(View.GONE);
        }
        // Inflate all items from shared preferences
        for (int i = 0; i < itemCount; i++) {

            Item item = new Item(sharedPrefs.getString(Constants.IMAGE + i, "")
                    , sharedPrefs.getInt(Constants.COLOR + i, 0)
                    , sharedPrefs.getString(Constants.LABEL + i, ""));

            itemList.add(item);
        }
        setUpRecyclerView();
    }


    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.shareImage) {
            askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_EXST);
            return true;
        } else if (item.getItemId() == R.id.edit) {
            edit();
            return true;
        }
        return true;
    }

    private void edit() {
        Item item = itemList.get(adapter.index);
        new AddImageDialog().showEditImageDialog(item.url, this, new AddImageDialog.OnCompleteListener() {
            @Override
            public void onImageAdded(Item item) {
            adapter.visibleCardItem.set(adapter.index,item);
            itemList= adapter.visibleCardItem;
            adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {

            }
        });

    }

    private void shareImage() {
        Bitmap bitmap = adapter.mBitmap;
        String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "palette", "share palette");
        Uri bitmapUri = Uri.parse(bitmapPath);
        //Intent to send image
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/png");
        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
        startActivity(Intent.createChooser(intent, "Share"));
    }



    private void itemTouchHelperCallback() {

        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//                int position = viewHolder.getAdapterPosition();
//                itemList.remove(position);
//                adapter.notifyItemRemoved(position);

                adapter.visibleCardItem.remove(viewHolder.getAdapterPosition());
                itemList = adapter.visibleCardItem;
                //  adapter.cardItem.remove(viewHolder.getAdapterPosition());
                Toast.makeText(context, "Image Removed", Toast.LENGTH_SHORT).show();
                if (itemList.isEmpty())
                    b.noItems.setVisibility(View.VISIBLE);
                adapter.notifyDataSetChanged();

            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeCallback);
        itemTouchhelper.attachToRecyclerView(b.list);


         dragCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.DOWN | ItemTouchHelper.UP, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {

                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();

                Collections.swap(itemList, fromPosition, toPosition);
                // recyclerView.getAdapter().notifyItemMoved(fromPosition,toPosition);
                adapter.notifyItemMoved(fromPosition, toPosition);

                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }
        };

        itemTouchHelper2 = new ItemTouchHelper(dragCallback);
        if(MODE==MODE_ENABLE)
        itemTouchHelper2.attachToRecyclerView(b.list);
        else
            itemTouchHelper2.attachToRecyclerView(null);
    }




}