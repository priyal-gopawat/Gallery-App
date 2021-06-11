package com.streamliners.galleryapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.streamliners.galleryapp.databinding.ItemCardBinding;
import com.streamliners.galleryapp.models.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.CardViewHolder> {

    private Context context;
    public List<Item> cardItem, visibleCardItem;
    ItemCardBinding b;
    public int index;
    public Bitmap mBitmap;
    public List<CardViewHolder> holders = new ArrayList<>();
    public int mode=0;

    /**
     * constructor for item adapter
     * @param context
     * @param cardItem
     */
    public ItemAdapter(Context context, List<Item> cardItem) {
        this.context = context;
        this.cardItem = cardItem;
        visibleCardItem = new ArrayList<>(cardItem);
    }


    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCardBinding b = ItemCardBinding.inflate(LayoutInflater.from(context), parent, false);

        return new CardViewHolder(b);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {

        Glide.with(context)
                .asBitmap()
                .load(visibleCardItem.get(position).url)
                .into(holder.b.imageView);


        holder.b.title.setText(visibleCardItem.get(position).label);
        holder.b.title.setBackgroundColor(visibleCardItem.get(position).color);

        holders.add(holder);
    }

    @Override
    public int getItemCount() {
        return visibleCardItem.size();
    }


    /**
     * Adding search functionality
     * @param query
     */
    public void filter(String query) {

        if (query.trim().isEmpty()) {
            visibleCardItem = new ArrayList<>(cardItem);
            notifyDataSetChanged();
            return;
        }
            query = query.toLowerCase();
            visibleCardItem.clear();

            for (Item card : cardItem) {
                if (card.label.toLowerCase().contains(query.toLowerCase())) {
                    visibleCardItem.add(card);
                }
            }

        notifyDataSetChanged();
    }

    /**
     * sort alphabetically
     */
    public void sortAlpha(){
        Collections.sort(visibleCardItem, new Comparator<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                return o1.label.compareTo(o2.label);
            }
        });
        notifyDataSetChanged();
    }


    class CardViewHolder extends RecyclerView.ViewHolder {
        ItemCardBinding b;

        public CardViewHolder(ItemCardBinding b) {
            super(b.getRoot());
            this.b = b;

           listenerSetter();
        }

        /**
         * setUp listener according to mode
         */
         public void listenerSetter(){
             if (mode==0){
                 registerForContextMenu(b);

             } else{
                 b.imageView.setOnCreateContextMenuListener(null);
             }
        }

        /**
         * register binding for context menu
         * @param binding
         */
        public void registerForContextMenu(ItemCardBinding binding) {
            binding.imageView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

                    menu.setHeaderTitle("Choose your option");
                    MenuInflater inflater = ((GalleryActivity) context).getMenuInflater();
                    inflater.inflate(R.menu.send_menu, menu);
                    mBitmap = getBitmapFromView(binding.getRoot());

                    index = visibleCardItem.indexOf(visibleCardItem.get(getAdapterPosition()));

                }
            });

        }

        /**
         * get bitmap from view
         * @param view
         * @return
         */
        public  Bitmap getBitmapFromView(View view) {
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

}
