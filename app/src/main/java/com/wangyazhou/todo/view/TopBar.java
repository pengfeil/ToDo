package com.wangyazhou.todo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wangyazhou.todo.R;

public class TopBar extends LinearLayout{
    protected LayoutInflater inflater;
    protected static final int LAYOUT_ID = R.layout.top_bar;
    protected static final int BAR_HEIGHT_PX = 80;

    protected Button leftButton, rightButton;
    protected View leftButtonZone;
    protected TextView titleTextView;
    public TopBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflater = LayoutInflater.from(context);
        init();
    }

    protected void init(){
        View view = inflater.inflate(LAYOUT_ID, null);
        WindowManager manager = (WindowManager) this.getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        this.addView(view, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        leftButton = (Button) this.findViewById(R.id.top_bar_leftBtn);
        leftButtonZone = this.findViewById(R.id.top_bar_leftBtnZone);
        rightButton = (Button) this.findViewById(R.id.top_bar_rightBtn);
        titleTextView = (TextView) this.findViewById(R.id.top_bar_title);
    }
    
    public void setButtonsDisplay(boolean showLeft, boolean showRight, CharSequence leftText, CharSequence rightText){
        setButtonDisplay(leftButton, showLeft, leftText);
        setButtonDisplay(rightButton, showRight, rightText);
    }
    
    public void setLeftButtonBackground(int resId){
	setButtonBackground(leftButton, resId);
    }
    
    public void setRightButtonBackground(int resId){
	setButtonBackground(rightButton, resId);
    }
    
    private void setButtonBackground(Button btn, int resId){
	btn.setBackgroundResource(resId);
    }

    private void setButtonDisplay(Button button, boolean show, CharSequence text){
        if(show){
            button.setVisibility(View.VISIBLE);
            if(text != null){
        	ViewGroup.LayoutParams params = button.getLayoutParams();
        	params.height = LayoutParams.WRAP_CONTENT;
        	params.width = LayoutParams.WRAP_CONTENT;
        	button.setLayoutParams(params);
            }
            button.setText(text);
        } else {
            button.setVisibility(View.INVISIBLE);
        }
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
    
    public void setLeftButtonZoneOnClickListener(OnClickListener listener){
        leftButtonZone.setOnClickListener(listener);
        leftButton.setOnClickListener(listener);
    }

    public void setRightButtonOnClickListener(OnClickListener listener){
        rightButton.setOnClickListener(listener);
    }

    public void setEnableLeftButton(boolean enable){
        leftButton.setEnabled(enable);
    }

    public void setEnableRightButton(boolean enable){
        rightButton.setEnabled(enable);
    }
}

