package com.wangyazhou.todo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wangyazhou.todo.R;

import java.util.zip.Inflater;

public class TopBar extends LinearLayout{
    protected LayoutInflater inflater;
    protected static final int LAYOUT_ID = R.layout.top_bar;

    protected Button leftButton;
    protected TextView titleTextView;
    public TopBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflater = LayoutInflater.from(context);
        init();
    }

    protected void init(){
        View view = inflater.inflate(LAYOUT_ID, null);
        this.addView(view, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        leftButton = (Button) this.findViewById(R.id.top_bar_leftBtn);
        titleTextView = (TextView) this.findViewById(R.id.top_bar_title);
    }

    public void setTitleText(CharSequence text){
        titleTextView.setText(text);
    }

    public void setTitleText(int resId){
        titleTextView.setText(resId);
    }

    public void setLeftButtonOnClickListener(OnClickListener listener){
        leftButton.setOnClickListener(listener);
    }
}

