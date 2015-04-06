package com.wangyazhou.todo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;

import com.wangyazhou.todo.R;
import com.wangyazhou.todo.dataAccessor.Image;
import com.wangyazhou.todo.dataAccessor.NativeImageLoader;
import com.wangyazhou.todo.view.TodoImageView;

import java.util.List;

public class ImageGridAdapter extends BaseAdapter {
    protected List<Image> images;
    protected Context context;
    protected GridView gridView;

    protected static final int LAYOUT_ID = R.layout.image_grid_item;

    private LayoutInflater mInflater;
    private Point mPoint = new Point(0, 0); //Record size of images
    private int selectedItemPosition = -1;

    private NativeImageLoader nativeImageLoader = NativeImageLoader.getInstance();

    public ImageGridAdapter(Context context, List<Image> images, GridView gridView) {
        this.images = images;
        this.context = context;
        this.gridView = gridView;
    }

    public int getSelectedItemPosition() {
        return selectedItemPosition;
    }

    public void setSelectedItemPosition(int selectedItemPosition) {
        this.selectedItemPosition = selectedItemPosition;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int position) {
        return images.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        mInflater = LayoutInflater.from(context);
        if (convertView == null) {
            convertView = mInflater.inflate(LAYOUT_ID, null);
            holder = new ViewHolder();
            holder.image = (TodoImageView) convertView.findViewById(R.id.image_grid_item_image);
            holder.image.setOnMeasureListener(new TodoImageView.OnMeasureListener() {
                @Override
                public void onMeasureSize(int width, int height) {
                    mPoint = new Point(width, height);
                }
            });
            holder.picker = (ImageView) convertView.findViewById(R.id.image_grid_item_picker);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Image image = images.get(position);
        holder.image.setTag(image.getPath());
        Bitmap bitmap = nativeImageLoader.loadNativeImage(image.getPath(), mPoint,
                new NativeImageLoader.NativeImageCallBack() {
                    @Override
                    public void onImageLoader(Bitmap bitmap, String path) {
                        ImageView mImageView = (ImageView) gridView.findViewWithTag(path);
                        if (bitmap != null && mImageView != null) {
                            mImageView.setImageBitmap(bitmap);
                        }
                    }
                });
        if (bitmap != null) {
            holder.image.setImageBitmap(bitmap);
        }
        holder.picker.setTag(position);
        if (position != selectedItemPosition) {
            holder.picker.setImageResource(android.R.drawable.btn_star_big_off);
        } else {
            holder.picker.setImageResource(android.R.drawable.btn_star_big_on);
        }
        Log.d("Todo", "getView " + image.getPath());
        return convertView;
    }

    private class ViewHolder {
        TodoImageView image;
        ImageView picker;
    }
}
