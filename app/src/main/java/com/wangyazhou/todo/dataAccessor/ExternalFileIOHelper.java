package com.wangyazhou.todo.dataAccessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.wangyazhou.todo.util.ImageUtil;

public class ExternalFileIOHelper {
    private static String generateFileName(String sourcePath) {
	return "Todo" + (new Date()).getTime() + sourcePath.hashCode();
    }

    public static String writeImage(Context context, String sourceImagePath) {
	String targetName = generateFileName(sourceImagePath);
	String targetPath = context.getFilesDir() + File.separator + targetName;
	try {
	    FileOutputStream out = new FileOutputStream(targetPath);// context.openFileOutput(targetName,
								    // 0);
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
	return targetPath;
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

    /**
     * Only used by crash log record.
     * 
     * @param context
     * @param text
     * @param fileName
     * @throws FileNotFoundException
     */
    public static PrintWriter getCrashLogPrinter()
	    throws FileNotFoundException {
	File sdPath = Environment.getExternalStorageDirectory();
	if (sdPath.exists() && sdPath.canWrite()) {
	    String logFile = sdPath.getPath() + File.separator
		    + "TodoCrash.log." + (new Date().getTime());
	    Log.d("Todo", "Crash File " + logFile);
	    return new PrintWriter(logFile);
	} else {
	    return null;
	}
    }
}
