package com.wangyazhou.todo.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

@SuppressLint("ClickableViewAccessibility")
public class UnderlineEditText extends EditText {
    private Paint mPaint;
    private boolean hasUnderLine = false;

    public UnderlineEditText(Context context, AttributeSet attrs) {
	super(context, attrs);
	mPaint = new Paint();

	mPaint.setStyle(Paint.Style.STROKE);
	mPaint.setColor(Color.BLACK);
    }

    @Override
    public void onDraw(Canvas canvas) {
	super.onDraw(canvas);
	if (hasUnderLine)
	    canvas.drawLine(0, this.getHeight() - 1, this.getWidth() - 1,
		    this.getHeight() - 1, mPaint);
    }

    public boolean isHasUnderLine() {
        return hasUnderLine;
    }

    public void setHasUnderLine(boolean hasUnderLine) {
        this.hasUnderLine = hasUnderLine;
        this.invalidate();
    }
    
    public int selection = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
	boolean returnVal = super.onTouchEvent(event);
	selection = this.getSelectionStart();
	return returnVal;
    }

    
}
