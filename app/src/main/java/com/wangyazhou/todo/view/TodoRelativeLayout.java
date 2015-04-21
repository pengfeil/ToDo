package com.wangyazhou.todo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;

public class TodoRelativeLayout extends RelativeLayout {
    private OnSizeChangedListener onSizeChangedListener;

    public TodoRelativeLayout(Context context, AttributeSet attrs) {
	super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
	Log.d("TodoKey", (h - oldh) + "");
	if(onSizeChangedListener != null){
	    onSizeChangedListener.onSizeChanged(w, h, oldw, oldh);
	}
	super.onSizeChanged(w, h, oldw, oldh);
    }
    

    public void setOnSizeChangedListener(OnSizeChangedListener onSizeChangedListener) {
        this.onSizeChangedListener = onSizeChangedListener;
    }


    public static interface OnSizeChangedListener {
	public void onSizeChanged(int w, int h, int oldw, int oldh);
    }
}
