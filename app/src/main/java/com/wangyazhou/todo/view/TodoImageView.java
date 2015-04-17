package com.wangyazhou.todo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

public class TodoImageView extends ImageView {
    private OnMeasureListener onMeasureListener;
    private Paint mPaint;

    public void setOnMeasureListener(OnMeasureListener onMeasureListener) {
        this.onMeasureListener = onMeasureListener;
    }

    public TodoImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public TodoImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initPaint();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(0, 0, this.getWidth() - 1, 0, mPaint);
        canvas.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (onMeasureListener != null) {
            onMeasureListener.onMeasureSize(getMeasuredWidth(),
                    getMeasuredHeight());
        }
    }

    public interface OnMeasureListener {
        public void onMeasureSize(int width, int height);
    }

}