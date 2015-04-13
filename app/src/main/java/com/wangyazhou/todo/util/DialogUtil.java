package com.wangyazhou.todo.util;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.Gravity;
import android.widget.Toast;

public class DialogUtil {
    public static void showSimpleToastText(Context context, String text) {
	Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
	toast.setGravity(Gravity.CENTER, 0, 0);
	toast.show();
    }

    public static void confirmDialog(Context context, String title, String msg,
	    final ActionCallback positiveAction) {
	AlertDialog.Builder builder = new Builder(context);
	builder.setMessage(msg);
	builder.setTitle(title);
	builder.setPositiveButton("Yes", new OnClickListener() {
	    @Override
	    public void onClick(DialogInterface dialog, int which) {
		positiveAction.perform();
		dialog.dismiss();
	    }
	});
	builder.setNegativeButton("No", new OnClickListener() {
	    @Override
	    public void onClick(DialogInterface dialog, int which) {
		dialog.dismiss();
	    }
	});
	builder.create().show();
    }

    public static interface ActionCallback {
	public void perform();
    }
}
