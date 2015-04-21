package com.wangyazhou.todo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.wangyazhou.todo.adapter.ImageGridAdapter;
import com.wangyazhou.todo.dataAccessor.Image;
import com.wangyazhou.todo.util.BackgroundUITask;
import com.wangyazhou.todo.util.BackgroundUITask.Task;
import com.wangyazhou.todo.util.BackgroundUITask.TaskCallback;
import com.wangyazhou.todo.util.DialogUtil;
import com.wangyazhou.todo.view.TopBar;

public class ImagePickerActivity extends TodoBaseActivity {
    public static final String DATA_KEY = "imageUrl";

    private TopBar topbar = null;
    private GridView gridView = null;
    private ImageGridAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);
        initializeWidgets();
    }

    protected void initializeWidgets() {
        topbar = (TopBar) this.findViewById(R.id.image_picker_top_bar);
        topbar.setButtonsDisplay(true, true,
                getResources().getString(R.string.activity_menu_left_2),
                getResources().getString(R.string.activity_menu_left_3));
        topbar.setTitleText(null);
        topbar.setLeftButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePickerActivity.this.setResult(RESULT_CANCELED);
                ImagePickerActivity.this.finish();
            }
        });
        topbar.setRightButtonOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Image selectedImage = (Image) adapter.getItem(adapter
                        .getSelectedItemPosition());
                Intent result = new Intent();
                result.putExtra(DATA_KEY, selectedImage.getPath());
                ImagePickerActivity.this.setResult(RESULT_OK, result);
                ImagePickerActivity.this.finish();
            }
        });
        topbar.setEnableRightButton(false); // disable it before any image is
        // picked.
        gridView = (GridView) this.findViewById(R.id.image_picker_grid);
        adapter = new ImageGridAdapter(ImagePickerActivity.this, list, gridView);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String tempImageDirName = "todo-temp-camera-images";
                String tempImageDir = Environment.getExternalStorageDirectory()
                        .getPath() + File.separator + tempImageDirName;
                if (position == 0) {
                    if (!Environment.getExternalStorageState().equals(
                            Environment.MEDIA_MOUNTED)) {
                        DialogUtil
                                .showSimpleToastText(ImagePickerActivity.this,
                                        "No external storage detected, take picture from camera is disabled.");
                        return;
                    }
                    // for camera
                    Intent takePictureIntent = new Intent(
                            MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent
                            .resolveActivity(ImagePickerActivity.this
                                    .getPackageManager()) != null) {
                        File dir = new File(tempImageDir);
                        if (!dir.exists()) {
                            if (!dir.mkdir()) {
                                DialogUtil
                                        .showSimpleToastText(
                                                ImagePickerActivity.this,
                                                "Can't write to external storage, take picture from camera is disabled.");
                                return;
                            }
                        }
                        imageFromCameraPath = tempImageDir + File.separator
                        + String.valueOf(System.currentTimeMillis());
                        Log.d("Todo", "Make camera out put image to "
                        + imageFromCameraPath);
                        Uri mUri = Uri.fromFile(new
                        File(imageFromCameraPath));
                        takePictureIntent.putExtra(
                        android.provider.MediaStore.EXTRA_OUTPUT, mUri);
                        takePictureIntent.putExtra("return-data", false);
                        ImagePickerActivity.this.startActivityForResult(
                                takePictureIntent,
                                ActionHelper.REQUEST_IMAGE_CAPTURE);
                    }
                } else {
                    View previousSelectedView = gridView
                            .findViewWithTag(adapter.getSelectedItemPosition());
                    if (previousSelectedView != null) {
                        ((ImageView) previousSelectedView)
                                .setImageResource(R.drawable.selectimageunselected);
                    }
                    adapter.setSelectedItemPosition(position);
                    ((ImageView) view.findViewWithTag(position))
                            .setImageResource(R.drawable.selectimageselected);
                    // Enable the ok button only when there is a image picked.
                    topbar.setEnableRightButton(true);
                }
            }
        });
        findImages();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        adapter.notifyDataSetChanged();
    }

    private BackgroundUITask uiTask;
    private List<Image> list = new ArrayList<>();

    private void findImages() {
        uiTask = new BackgroundUITask(this, true);
        uiTask.setOkCallback(new TaskCallback() {
            @Override
            public void perform() {
                adapter.notifyDataSetChanged();
            }
        });
        uiTask.start(new Task() {
            @Override
            public boolean perform() {
                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver mContentResolver = ImagePickerActivity.this
                        .getContentResolver();

                Cursor mCursor = mContentResolver.query(mImageUri, null,
                        MediaStore.Images.Media.MIME_TYPE + "=? or "
                                + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[]{"image/jpeg", "image/png"},
                        MediaStore.Images.Media.DATE_MODIFIED);

                if (mCursor == null) {
                    return false;
                }

                while (mCursor.moveToNext()) {
                    String path = mCursor.getString(mCursor
                            .getColumnIndex(MediaStore.Images.Media.DATA));
                    if (new File(path).exists())
                        list.add(new Image(path));
                }

                // Insert item for camera
                list.add(0, new Image(null));
                return true;
            }
        });

    }

    private String imageFromCameraPath;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ActionHelper.REQUEST_IMAGE_CAPTURE
                && resultCode == RESULT_OK) {
//            Read from external file
            if (imageFromCameraPath != null) {
            Intent result = new Intent();
            result.putExtra(DATA_KEY, imageFromCameraPath);
            ImagePickerActivity.this.setResult(RESULT_OK, result);
            imageFromCameraPath = null;
            ImagePickerActivity.this.finish();

//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            String path = ExternalFileIOHelper.writeImage(
//                    getApplicationContext(), imageBitmap);
//            Intent result = new Intent();
//            result.putExtra(DATA_KEY, path);
//            ImagePickerActivity.this.setResult(RESULT_OK, result);
//            ImagePickerActivity.this.finish();
            }
        } else if (data != null) {
            Log.d("Todo", getClass() + " onActivityResult " + data.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (uiTask != null) {
            uiTask.stop();
        }
    }
}
