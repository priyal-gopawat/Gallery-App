package com.streamliners.galleryapp;

import android.app.SearchManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemHelper {

    private OnCompleteListener listener;
    private String rectangularImageURL = "https://picsum.photos/%d/%d"
            , squareImageURL = "https://picsum.photos/%d";
    private Context context;
    private  Bitmap bitmap;
    private Set<Integer> colors;


    //Triggers -----------------------------------------------------------------------------

    //For  Rectangular image passing url
    void fetchData(int x, int y , Context context, OnCompleteListener listener){
        this.context = context;
        this.listener = listener;
        fetchImage(
                String.format(rectangularImageURL,x,y)
        );
    }

    //For  Square image passing url
    void fetchData(int x, Context context,OnCompleteListener listener){
        this.context = context;
        this.listener = listener;
        fetchImage(
                String.format(squareImageURL,x)
        );
    }

   //ImageFetcher -----------------------------------------------------------------------------

    /**
     * fetching image from url
     * @param url
     */
    void  fetchImage(String url){
        Glide.with(context)
                .asBitmap()
                .load(url)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        bitmap = resource;
                    extractPaletteFromBitmap();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        listener.onError("Image Load Failed!");
                    }
                });
    }

    //PaletteHelper -----------------------------------------------------------------------------

    /**
     * extract palette from bitmap first color then label
     */
    private void extractPaletteFromBitmap() {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette p){
            colors = getColorsFromPalette(p);

            labelImage();
            }
        });
        }

    /**
     * converting palette to set
     * @param p
     * @return
     */
    private Set<Integer> getColorsFromPalette(Palette p) {
        Set<Integer> colors = new HashSet<>();


        colors.add(p.getVibrantColor(0));
        colors.add(p.getLightVibrantColor(0));
        colors.add(p.getDarkVibrantColor(0));

        colors.add(p.getMutedColor(0));
        colors.add(p.getLightMutedColor(0));
        colors.add(p.getDarkMutedColor(0));

        colors.add(p.getVibrantColor(0));

        colors.remove(0);

        return colors;
    }

    //LabelHelper -----------------------------------------------------------------------------

    /**
     * fetching labels
     */
    private void labelImage() {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
        labeler.process(image)
                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    @Override
                    public void onSuccess(List<ImageLabel> labels) {
                       List<String> strings = new ArrayList<>();
                       for(ImageLabel label: labels){
                           strings.add(label.getText());
                       }
                       listener.onFetched(bitmap, colors , strings);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                     listener.onError(e.toString());
                    }
                });
    }

    //Listener -----------------------------------------------------------------------------
    interface OnCompleteListener{
        void onFetched(Bitmap image, Set<Integer> colors, List<String> labels);
        void onError(String error);
    }
}
