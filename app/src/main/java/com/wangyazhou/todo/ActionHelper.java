package com.wangyazhou.todo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.MediaStore;

public class ActionHelper {
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_IMAGE_SELECT = 2;
    public static void showPickImageDialog(final Activity context){
        new AlertDialog.Builder(context)
                .setTitle(R.string.pick_image_dialog_title)
                .setItems(R.array.pick_image_dialog_menu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    // Camera
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                } else if(which == 1){
                    // Picture lib
                    Intent pickPictureIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    if (pickPictureIntent.resolveActivity(context.getPackageManager()) != null){
                        context.startActivityForResult(pickPictureIntent, REQUEST_IMAGE_SELECT);
                    }
                }
                dialog.dismiss();
            }
        }).show();
    }
}
