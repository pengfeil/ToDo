package com.wangyazhou.todo.dataAccessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;

import com.wangyazhou.todo.util.ImageUtil;

public class ExternalFileIOHelper {
    private static String generateFileName(String sourcePath) {
	return "Todo" + (new Date()).getTime() + sourcePath.hashCode();
    }

    public static String writeImage(Context context, String sourceImagePath) {
	String targetName = generateFileName(sourceImagePath);
	try {
	    FileOutputStream out = context.openFileOutput(targetName, 0);
	    FileInputStream in = new FileInputStream(sourceImagePath);
	    byte[] buffer = new byte[1024 * 8];
	    int readLength = -1;
	    while ((readLength = in.read(buffer)) > 0) {
		out.write(buffer, 0, readLength);
	    }
	    in.close();
	    out.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
	return context.getFilesDir() + File.separator + targetName;
    }

    public static String writeImage(Context context, Bitmap bp) {
	if (bp == null) {
	    return null;
	}
	String targetName = generateFileName("");
	try {
	    FileOutputStream out = context.openFileOutput(targetName, 0);
	    byte[] buffer = ImageUtil.Bitmap2Bytes(bp);
	    out.write(buffer, 0, buffer.length);
	    out.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
	return context.getFilesDir() + File.separator + targetName;
    }

    public static boolean deleteImage(Context context, String path) {
	if (path == null) {
	    return true;
	}
	File file = new File(path);
	return file.delete();
    }
}
