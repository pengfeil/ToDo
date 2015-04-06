package com.wangyazhou.todo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.wangyazhou.todo.adapter.ImageGridAdapter;
import com.wangyazhou.todo.dataAccessor.Image;
import com.wangyazhou.todo.view.TopBar;

import java.util.ArrayList;
import java.util.List;

public class ImagePickerActivity extends Activity {
    public static final String DATA_KEY = "imageUrl";

    private TopBar topbar = null;
    private GridView gridView = null;
    private ImageGridAdapter adapter;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);
        initializeWidgets();
    }

    protected void initializeWidgets() {
        topbar = (TopBar) this.findViewById(R.id.image_picker_top_bar);
        topbar.setButtonsDisplay(true, true, getResources().getString(R.string.activity_menu_left_2),
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
                Image selectedImage = (Image)adapter.getItem(adapter.getSelectedItemPosition());
                Intent result = new Intent();
                result.putExtra(DATA_KEY, selectedImage.getPath());
                ImagePickerActivity.this.setResult(RESULT_OK, result);
                ImagePickerActivity.this.finish();
            }
        });
        topbar.setEnableRightButton(false); // disable it before any image is picked.
        gridView = (GridView) this.findViewById(R.id.image_picker_grid);
        adapter = new ImageGridAdapter(ImagePickerActivity.this, list, gridView);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                View previousSelectedView = gridView.findViewWithTag(adapter.getSelectedItemPosition());
                if(previousSelectedView != null) {
                    ((ImageView)previousSelectedView).setImageResource(android.R.drawable.btn_star_big_off);
                }
                adapter.setSelectedItemPosition(position);
                ((ImageView)view.findViewWithTag(position)).setImageResource(android.R.drawable.btn_star_big_on);
                // Enable the ok button only when there is a image picked.
                topbar.setEnableRightButton(true);
            }
        });
        findImages();
    }

    private static final int SCAN_OK = 1;
    private List<Image> list = new ArrayList<>();
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SCAN_OK:
                    adapter.notifyDataSetChanged();
                    mProgressDialog.dismiss();
                    break;
            }
        }

    };

    private void findImages() {
        mProgressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.pick_image_dialog_loading));

        new Thread(new Runnable() {

            @Override
            public void run() {
                Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentResolver mContentResolver = ImagePickerActivity.this.getContentResolver();

                Cursor mCursor = mContentResolver.query(mImageUri, null,
                        MediaStore.Images.Media.MIME_TYPE + "=? or "
                                + MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED);

                if (mCursor == null) {
                    return;
                }

                while (mCursor.moveToNext()) {
                    String path = mCursor.getString(mCursor
                            .getColumnIndex(MediaStore.Images.Media.DATA));
                    list.add(new Image(path));
                }

                mHandler.sendEmptyMessage(SCAN_OK);
                mCursor.close();
            }
        }).start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
