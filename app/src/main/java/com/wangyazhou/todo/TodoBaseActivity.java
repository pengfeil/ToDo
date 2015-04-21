package com.wangyazhou.todo;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;

import com.wangyazhou.todo.dataAccessor.ExternalFileIOHelper;

public class TodoBaseActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	requestWindowFeature(Window.FEATURE_NO_TITLE);
	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	Thread.currentThread().setUncaughtExceptionHandler(
		new UncaughtExceptionHandler() {
		    @Override
		    public void uncaughtException(Thread arg0, Throwable arg1) {
			try {
			    PrintWriter pw = ExternalFileIOHelper
				    	.getCrashLogPrinter();
			    arg1.printStackTrace(pw);
			    pw.close();
			} catch (FileNotFoundException e) {
			    e.printStackTrace();
			}
			arg1.printStackTrace();
			TodoBaseActivity.this.finish();
		    }
		});
    }
}
