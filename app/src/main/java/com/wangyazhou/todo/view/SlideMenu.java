package com.wangyazhou.todo.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Scroller;

import com.wangyazhou.todo.R;
import com.wangyazhou.todo.adapter.MenuListAdapter;

public class SlideMenu extends LinearLayout {
    public static interface TransparentFrameOnTouchCallback {
        public boolean perform();
    }

    protected Context context;
    protected LayoutInflater inflater;
    protected static final int LAYOUT_ID = R.layout.slide_menu_content;

    protected LinearLayout transparentFrame;
    protected LinearLayout menuContentFrame;
    protected ListView menuList;

    protected TransparentFrameOnTouchCallback transparentFrameOnTouchCallback;

    protected final static int MENU_WIDTH = 500;
    protected final static int FORCE_SLIDE_DURATION = 500;
    protected final static int MIN_SNAP_VELOCITY = 1;

    protected int screenWidth;

    protected float currentSlideX = 0;
    protected int minSlideXLimitation = -MENU_WIDTH;
    protected int maxSlideXLimitation = 0;

    protected static final int TOUCH_STATE_SLIDING = 1;
    protected static final int TOUCH_STATE_IDLE = 0;
    protected int mTouchState = TOUCH_STATE_IDLE;

    protected static final int TOUCH_STATE_LEFT = 1;
    protected static final int TOUCH_STATE_OTHER = 0;
    protected static final int TOUCH_STATE_RIGHT = -1;
    protected int mMoveDirection = TOUCH_STATE_OTHER;

    protected Scroller scroller;
    protected VelocityTracker mVelocityTracker;
    protected int minSlideThreshold;
    protected boolean enableSliding = false;

    protected MenuListAdapter menuListAdapter;

    public SlideMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        inflater = LayoutInflater.from(context);
        init();
    }

    protected void init() {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;

        this.setOrientation(HORIZONTAL);
        this.setBackgroundResource(android.R.color.transparent);

        transparentFrame = new LinearLayout(context);
        transparentFrame.setLayoutParams(new LayoutParams(screenWidth,
                LayoutParams.MATCH_PARENT));
        transparentFrame.setOnTouchListener(new OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                Log.d("Todo", "transparentFrame touched " + arg1.getAction());
                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
                    if (enableSliding) {
                        forceSlideIn();
                        return true;
                    }
                    if (!enableSliding
                            && transparentFrameOnTouchCallback != null) {
                        return transparentFrameOnTouchCallback.perform();
                    }
                } else if (arg1.getAction() == MotionEvent.ACTION_MOVE) {
                    Log.d("Todo", "MOVE" + arg1.getX());
                }
                return enableSliding;
            }
        });

        menuContentFrame = new LinearLayout(context);
        LayoutParams params = new LayoutParams(MENU_WIDTH,
                LayoutParams.MATCH_PARENT);
        params.setMargins(-MENU_WIDTH, 0, 0, 0);
        menuContentFrame.setLayoutParams(params);

        View view = inflater.inflate(LAYOUT_ID, null);

        menuList = (ListView) view.findViewById(R.id.slide_menu_list);
        menuListAdapter = buildMenuListAdapter();
        menuList.setAdapter(menuListAdapter);
        menuContentFrame.addView(view, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));

        this.addView(menuContentFrame);
        this.addView(transparentFrame);

        scroller = new Scroller(context);
        minSlideThreshold = ViewConfiguration.get(context).getScaledTouchSlop();

        toggleTransparentFrame(false);
    }

    private MenuListAdapter buildMenuListAdapter() {
        String[] menus = getResources().getStringArray(R.array.top_bar_menu);
        List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
        for (String m : menus) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("text", m);
            int iconId = data.size() % 2 == 0 ? R.drawable.slidemenutodoicon : R.drawable.slidemenuarchiveicon;
            map.put("icon", iconId);
            map.put("number", 1);
            data.add(map);
        }
        return new MenuListAdapter(context, data, R.layout.slide_menu_item,
                new String[]{"text", "icon", "number"}, new int[]{
                R.id.slide_menu_item_text, R.id.slide_menu_item_icon,
                R.id.slide_menu_item_number}, new boolean[]{true,
                false});
    }

    protected void toggleTransparentFrame(boolean isShow) {
        transparentFrame.setBackgroundColor(getResources().getColor(
                android.R.color.darker_gray));
        transparentFrame.getBackground().setAlpha(isShow ? 100 : 0);
    }

    public static interface MenuSelectedAction {
        public void perform(int index);
    }

    public void setMenuSelectedAction(final MenuSelectedAction action) {
        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos,
                                    long id) {
                int position = (int) id;
                if (!menuListAdapter.isMenuSelected(position)) {
                    menuListAdapter.selectMenu(position);
                    action.perform(position);
                }
                forceSlideIn();
            }
        });
    }

    public void setTransparentFrameOnTouchCallback(
            TransparentFrameOnTouchCallback callback) {
        this.transparentFrameOnTouchCallback = callback;
    }

    // force slide all menu content out
    public void forceSlideOut() {
        scroller.startScroll(getScrollX(), 0, minSlideXLimitation
                - getScrollX(), 0, FORCE_SLIDE_DURATION);
        Log.d(this.getClass().getName(), "forceSlideOut from " + getScrollX()
                + " to " + maxSlideXLimitation);
        invalidate();
        enableSliding = true;
        toggleTransparentFrame(enableSliding);
        menuListAdapter.reloadNumbers();
    }

    // force slide all menu content in
    public void forceSlideIn() {
        scroller.startScroll(getScrollX(), 0, maxSlideXLimitation
                - getScrollX(), 0, FORCE_SLIDE_DURATION);
        Log.d(this.getClass().getName(), "forceSlideIn from " + getScrollX()
                + " to " + minSlideXLimitation);
        invalidate();
        enableSliding = false;
        toggleTransparentFrame(enableSliding);
    }

    protected boolean validateNextScroll(MotionEvent ev) {
        int scrollX = getScrollX();
        float deltaX = currentSlideX - ev.getX();
        float nextScrollX = scrollX + deltaX;
        return nextScrollX <= maxSlideXLimitation
                && nextScrollX >= minSlideXLimitation;
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE)
                && (mTouchState != TOUCH_STATE_IDLE)) {
            Log.d(this.getClass().getName(),
                    "onInterceptTouchEvent:Cache start move");
            return true;
        }

        final float x = ev.getX();

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                int deltaX = (int) Math.abs(currentSlideX - x);
                if (deltaX > minSlideThreshold && validateNextScroll(ev)) {
                    Log.d(this.getClass().getName(),
                            "onInterceptTouchEvent:ACTION_MOVE start move");
                    mTouchState = TOUCH_STATE_SLIDING;
                }
                break;

            case MotionEvent.ACTION_DOWN:
                currentSlideX = x;
                mTouchState = scroller.isFinished() ? TOUCH_STATE_IDLE
                        : TOUCH_STATE_SLIDING;
                Log.d(this.getClass().getName(),
                        "onInterceptTouchEvent:ACTION_DOWN: " + mTouchState);
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                Log.d(this.getClass().getName(),
                        "onInterceptTouchEvent:ACTION_UP move over");
                mTouchState = TOUCH_STATE_IDLE;
                break;
        }
        Log.d(this.getClass().getName(), "onInterceptTouchEvent:exit with  "
                + (enableSliding && mTouchState != TOUCH_STATE_IDLE));
        return enableSliding && mTouchState != TOUCH_STATE_IDLE;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean hasTookAction = false;
        super.onTouchEvent(ev);
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        float x = ev.getX();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (scroller != null) {
                    if (!scroller.isFinished()) {
                        Log.d(this.getClass().getName(),
                                "onTouchEvent:ACTION_DOWN scroller abort");
                        // scroller.abortAnimation();
                    }
                }
                currentSlideX = x;
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = currentSlideX - ev.getX();
                if (validateNextScroll(ev)) {
                    currentSlideX = ev.getX();
                    mMoveDirection = deltaX > 0 ? TOUCH_STATE_LEFT
                            : TOUCH_STATE_RIGHT;
                    scrollBy((int) deltaX, 0);
                    Log.d(this.getClass().getName(),
                            "onTouchEvent:ACTION_MOVE moved by " + deltaX);
                    hasTookAction = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                mVelocityTracker.computeCurrentVelocity(1000);
                int velocityX = Math.abs((int) mVelocityTracker.getXVelocity());
                Log.d(this.getClass().getName(),
                        "onTouchEvent:ACTION_UP with velocityX:" + velocityX
                                + " and mMoveDirection " + mMoveDirection);
                if (velocityX > MIN_SNAP_VELOCITY
                        && mMoveDirection != TOUCH_STATE_OTHER) {
                    if (mMoveDirection == TOUCH_STATE_LEFT) {
                        forceSlideIn();
                    } else if (mMoveDirection == TOUCH_STATE_RIGHT) {
                        forceSlideOut();
                    }
                    Log.d(this.getClass().getName(),
                            "onTouchEvent:ACTION_UP start force move"
                                    + " with velocityX:" + velocityX
                                    + " and mMoveDirection " + mMoveDirection);
                    hasTookAction = true;
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                mTouchState = TOUCH_STATE_IDLE;
                break;
            case MotionEvent.ACTION_CANCEL:
                mTouchState = TOUCH_STATE_IDLE;
                break;
        }
        Log.d(this.getClass().getName(), "onTouchEvent:exit with "
                + (enableSliding && hasTookAction));
        return enableSliding;
    }
}
