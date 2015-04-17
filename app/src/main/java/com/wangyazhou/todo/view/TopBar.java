package com.wangyazhou.todo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wangyazhou.todo.R;

public class TopBar extends LinearLayout{
    protected LayoutInflater inflater;
    protected static final int LAYOUT_ID = R.layout.top_bar;

    protected Button leftButton, rightButton;
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
        this.addView(view, LayoutParams.MATCH_PARENT, dm.heightPixels / 8);
        leftButton = (Button) this.findViewById(R.id.top_bar_leftBtn);
        rightButton = (Button) this.findViewById(R.id.top_bar_rightBtn);
        titleTextView = (TextView) this.findViewById(R.id.top_bar_title);
    }
    public void setButtonsDisplay(boolean showLeft, boolean showRight, CharSequence leftText, CharSequence rightText){
        setButtonDisplay(leftButton, showLeft, leftText);
        setButtonDisplay(rightButton, showRight, rightText);
    }

    private void setButtonDisplay(Button button, boolean show, CharSequence text){
        if(show){
            button.setVisibility(View.VISIBLE);
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

