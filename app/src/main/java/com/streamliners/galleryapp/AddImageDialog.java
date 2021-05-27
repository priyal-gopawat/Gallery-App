package com.streamliners.galleryapp;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.streamliners.galleryapp.databinding.ChipColorBinding;
import com.streamliners.galleryapp.databinding.ChipLabelBinding;
import com.streamliners.galleryapp.databinding.DialogAddImageBinding;
import com.streamliners.galleryapp.models.Item;

import java.util.List;
import java.util.Set;


public class AddImageDialog implements ItemHelper.OnCompleteListener {
    private Context context;
    private OnCompleteListener listener;
    private DialogAddImageBinding b;
    private LayoutInflater inflater;
    private boolean isCustomLabel;
    private AlertDialog dialog;
    String redirectUrl;

    /**
     * showing dialogBox
     * @param context
     * @param listener
     */
    void show(Context context, OnCompleteListener listener) {
        this.context = context;
        this.listener = listener;

        //inflate Dialog's layout
        if (context instanceof GalleryActivity) {
            inflater = ((GalleryActivity) context).getLayoutInflater();
            b = DialogAddImageBinding.inflate(inflater);
        } else {
            dialog.dismiss();
            listener.onError("Cast Exception");
            return;
        }
        //create and show dialog
        dialog = new MaterialAlertDialogBuilder(context, R.style.CustomDialogTheme)
                .setView(b.getRoot())
                .show();

        //Handle events
        handleDimensionsInput();

        hideErrorsForET();
    }

    //Utility method
    private void hideErrorsForET() {
        b.width.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                b.width.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    //Step 1 : Input dimensions

    /**
     * dimensions input
     */
    private void handleDimensionsInput() {
        b.fetchImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get strings from ET
                String widthStr = b.width.getText().toString().trim(), heightStr = b.height.getText().toString().trim();

                //Guard code
                if (widthStr.isEmpty() && heightStr.isEmpty()) {
                    b.width.setError("Please enter at least one dimension!");
                    return;
                }

                //Update UI
                b.inputDimensionsRoot.setVisibility(View.GONE);
                b.progressIndicatorRoot.setVisibility(View.VISIBLE);

                //Hide keyboard
                hideKeyboard();

                //square image
                if (widthStr.isEmpty()) {
                    int height = Integer.parseInt(heightStr);
                    fetchRandomImage(height);
                } else if (heightStr.isEmpty()) {
                    int width = Integer.parseInt(widthStr);
                    fetchRandomImage(width);
                }

                //rectangular image
                else {
                    int height = Integer.parseInt(heightStr);
                    int width = Integer.parseInt(widthStr);
                    fetchRandomImage(width, height);
                }
            }
        });
    }

    /**
     * hide keyboard
     */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(b.widthInput.getWindowToken(), 0);
    }

    //Step 2 : Fetch random image
    //Rectangular image
    private void fetchRandomImage(int width, int height) {
        new ItemHelper()
                .fetchData(width, height, context, this);

    }

    //square image
    private void fetchRandomImage(int x) {
        new ItemHelper()
                .fetchData(x, context, this);
    }

    //Step 3 : show data

    /**
     * show and inflate data
     * @param url
     * @param colors
     * @param labels
     */
    private void showData(String url, Set<Integer> colors, List<String> labels) {
        this.redirectUrl = url;
        //loads image from glide cache
        Glide.with(context)
                .asBitmap()
                .load(redirectUrl)
                .into(b.imageView);
        inflateColorChips(colors);
        inflateLabelChips(labels);
        handleCustomLabelInput();
        handleAddImageEvent();

        b.progressIndicatorRoot.setVisibility(View.GONE);
        b.mainRoot.setVisibility(View.VISIBLE);
        b.customLabelInput.setVisibility(View.GONE);
    }

    /**
     * adding image to layout
     */
    private void handleAddImageEvent() {
        b.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int colorChipId = b.colorChips.getCheckedChipId(), labelChipId = b.labelChips.getCheckedChipId();

                //Guard code(if one of them is not checked = -1)
                if (colorChipId == -1 || labelChipId == -1) {
                    Toast.makeText(context, "Please choose color & label", Toast.LENGTH_SHORT).show();
                    return;
                }

                String label;
                //Get color and label
                if (isCustomLabel) {
                    label = b.customLabelEt.getText().toString().trim();
                    if (label.isEmpty()) {
                        Toast.makeText(context, "Please enter custom label", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                else {
                    label = ((Chip) b.labelChips.findViewById(labelChipId)).getText().toString();
                }
                int color = ((Chip) b.colorChips.findViewById(colorChipId))
                        .getChipBackgroundColor().getDefaultColor();

                //Send callback
                listener.onImageAdded(new Item(redirectUrl, color, label));
                dialog.dismiss();
            }
        });
    }

    /**
     * handle custom label
     */
    private void handleCustomLabelInput() {
        ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
        binding.getRoot().setText("Custom");
        b.labelChips.addView(binding.getRoot());

        binding.getRoot().setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // if isChecked is true then visible else gone
                b.customLabelInput.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                isCustomLabel = isChecked;
            }
        });
    }

    //Label chips
    /**
     * inflate label chips
     * @param labels
     */
    private void inflateLabelChips(List<String> labels) {
        for (String label : labels) {
            ChipLabelBinding binding = ChipLabelBinding.inflate(inflater);
            binding.getRoot().setText(label);
            b.labelChips.addView(binding.getRoot());
        }
    }

    //Color Chips

    /**
     * inflate color chips
     * @param colors
     */
    private void inflateColorChips(Set<Integer> colors) {
        for (int color : colors) {
            ChipColorBinding binding = ChipColorBinding.inflate(inflater);
            binding.getRoot().setChipBackgroundColor(ColorStateList.valueOf(color));
            b.colorChips.addView(binding.getRoot());
        }
    }

    //Item helper callbacks
    @Override
    public void onFetched(String url, Set<Integer> colors, List<String> labels) {

        showData(url, colors, labels);

    }

    @Override
    public void onError(String error) {
        listener.onError(error);
        dialog.dismiss();
    }

    interface OnCompleteListener {
        void onImageAdded(Item item);

        void onError(String error);
    }

}
