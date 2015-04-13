package com.wangyazhou.todo.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class ImageUtil {
    public static final int THUMBNAIL_SIZE = 120;
    public static byte[] Bitmap2Bytes(Bitmap bm) {
	if (bm == null)
	    return null;
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
	return baos.toByteArray();
    }

    public static Bitmap bytes2Bitmap(byte[] bytes) {
	if (bytes == null)
	    return null;
	return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
