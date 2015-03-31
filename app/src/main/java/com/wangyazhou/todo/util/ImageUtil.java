package com.wangyazhou.todo.util;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

public class ImageUtil {
    public static byte[] Bitmap2Bytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}
