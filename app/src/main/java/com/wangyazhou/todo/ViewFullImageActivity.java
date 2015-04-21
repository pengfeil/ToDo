package com.wangyazhou.todo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.wangyazhou.todo.dataAccessor.NativeImageLoader;
import com.wangyazhou.todo.dataAccessor.NativeImageLoader.NativeImageCallBack;
import com.wangyazhou.todo.util.BackgroundUITask;
import com.wangyazhou.todo.util.BackgroundUITask.Task;
import com.wangyazhou.todo.util.BackgroundUITask.TaskCallback;
import com.wangyazhou.todo.util.ImageUtil;

public class ViewFullImageActivity extends TodoBaseActivity {
    public static final String INTENT_KEY_THUMBNAIL = "Thumbnail";
    public static final String INTENT_KEY_FULL_IMAGE = "FullImagePath";
    private Button closeBtn, modifyBtn, deleteBtn;
    private ImageView imageView;
    private byte[] thumbnail;
    private String fullImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_full_image);
        initializeWidgets();
        setupImageView();
    }

    private void setupImageView() {
        Intent sourceIntent = this.getIntent();
        thumbnail = sourceIntent.getExtras().getByteArray(INTENT_KEY_THUMBNAIL);
        fullImagePath = sourceIntent.getExtras().getString(
                INTENT_KEY_FULL_IMAGE);
        if (thumbnail == null) {
            finish();
            return;
        }
        imageView.setImageBitmap(ImageUtil.bytes2Bitmap(thumbnail));
        loadFullImage(fullImagePath);
    }

    private void loadFullImage(String path) {
        WindowManager manager = (WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        NativeImageLoader.getInstance().loadNativeImage(path,
                new Point(dm.widthPixels, dm.heightPixels),
                new NativeImageCallBack() {
                    @Override
                    public void onImageLoader(Bitmap bitmap, String path) {
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                });
    }

    private Intent modifiedImageData;

    private void initializeWidgets() {
        closeBtn = (Button) this.findViewById(R.id.view_full_image_closeBtn);
        modifyBtn = (Button) this.findViewById(R.id.view_full_image_modifyBtn);
        deleteBtn = (Button) this.findViewById(R.id.view_full_image_deleteBtn);
        imageView = (ImageView) this
                .findViewById(R.id.view_full_image_imageView);
        closeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (modifiedImageData != null) {
                    setResult(ActionHelper.RESULT_MODIFY_IMAGE,
                            modifiedImageData);
                } else {
                    setResult(-100, null); //no result
                }
                finish();
            }
        });
        modifyBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent pickImageIntent = new Intent(ViewFullImageActivity.this,
                        ImagePickerActivity.class);
                ViewFullImageActivity.this.startActivityForResult(
                        pickImageIntent, ActionHelper.REQUEST_IMAGE_PICKER);
            }
        });
        deleteBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                setResult(ActionHelper.RESULT_DELETE_IMAGE);
                finish();
            }
        });
    }

    private BackgroundUITask uiTask;
    private Bitmap newThumbnail;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("Todo", this.getClass() + "onActivityResult " + resultCode);
        modifiedImageData = data;
        if (requestCode == ActionHelper.REQUEST_IMAGE_PICKER
                && resultCode == RESULT_OK) {
            final String imagePath = data
                    .getStringExtra(ImagePickerActivity.DATA_KEY);
            uiTask = new BackgroundUITask(this, true);
            uiTask.setOkCallback(new TaskCallback() {
                @Override
                public void perform() {
                    imageView.setImageBitmap(newThumbnail);
                    loadFullImage(imagePath);
                }
            });
            uiTask.start(new Task() {
                @Override
                public boolean perform() {
                    newThumbnail = NativeImageLoader.getInstance()
                            .decodeThumbBitmapForFile(imagePath,
                                    ImageUtil.THUMBNAIL_SIZE,
                                    ImageUtil.THUMBNAIL_SIZE);
                    return true;
                }
            });
        }
    }
}
