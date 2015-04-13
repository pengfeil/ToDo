package com.wangyazhou.todo.util;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.wangyazhou.todo.R;

@SuppressLint("HandlerLeak")
public class BackgroundUITask {
    private Context context;
    private ProgressDialog mProgressDialog;
    private boolean hasProgressDialog;
    private boolean isStopped = false;
    private TaskCallback okCallback, failCallback;
    private static final int OK = 1, FAIL = -1;

    public BackgroundUITask(Context context, boolean hasProgressDialog) {
	this.context = context;
	this.hasProgressDialog = hasProgressDialog;
	this.failCallback = new TaskCallback() {
	    @Override
	    public void perform() {
		DialogUtil.showSimpleToastText(BackgroundUITask.this.context,
			"Operation failed");
	    }
	};
    }

    public void stop() {
	isStopped = true;
    }

    public void setOkCallback(TaskCallback okCallback) {
	this.okCallback = okCallback;
    }

    public void setFailCallback(TaskCallback failCallback) {
	this.failCallback = failCallback;
    }

    private Handler mHandler = new Handler() {
	@Override
	public void handleMessage(Message msg) {
	    super.handleMessage(msg);
	    if (isStopped)
		return;
	    switch (msg.what) {
	    case OK:
		if (okCallback != null)
		    okCallback.perform();
		break;
	    case FAIL:
		if (failCallback != null)
		    failCallback.perform();
		break;
	    }
	    if (hasProgressDialog)
		mProgressDialog.dismiss();
	}
    };

    public void start(final Task task) {
	if (hasProgressDialog) {
	    mProgressDialog = ProgressDialog.show(
		    context,
		    null,
		    context.getResources().getString(
			    R.string.pick_image_dialog_loading));
	}
	new Thread(new Runnable() {
	    @Override
	    public void run() {
		int resultCode = task.perform() ? OK : FAIL;
		mHandler.sendEmptyMessage(resultCode);
	    }
	}).start();
    }

    public static interface TaskCallback {
	public void perform();
    }

    public static interface Task {
	public boolean perform();
    }
}
