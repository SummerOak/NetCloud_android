package com.summer.netcloud.window;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Outline;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.summer.netcloud.ContextMgr;
import com.summer.netcloud.R;
import com.summer.netcloud.utils.ResTools;

/**
 * Created by summer on 12/06/2018.
 */

public abstract class AbsWindow{

    private FrameLayout mWindowView;
    private LinearLayout mContentView;
    private STATE mState = STATE.INIT;

    private enum STATE{
        INIT,
        SHOWING,
        BACKGROUND,
    }

    public AbsWindow(Context context) {

        mWindowView = new FrameLayout(context);
        mWindowView.setBackgroundColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWindowView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 0);
                    }
                }
            });
        }


        mContentView = new LinearLayout(context);
        mContentView.setOrientation(LinearLayout.VERTICAL);
        mContentView.setBackgroundColor(ResTools.getColor(R.color.background));
        mContentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        View titleBar = getTitleBar();
        if(titleBar != null){
            mContentView.addView(titleBar, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        }

        View content = getContentView();
        if(content != null){
            mContentView.addView(content,FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
        }



        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mWindowView.addView(mContentView, lp);
    }

    public boolean isShowing(){
        return mState == STATE.SHOWING && ContextMgr.isForeground();
    }

    public boolean inStack(){
        return mState != STATE.INIT;
    }

    protected View getTitleBar(){
        return null;
    }

    protected View getContentView(){
        return null;
    }

    protected View getView(){
        return mWindowView;
    }

    protected Context getContext(){
        return getView().getContext();
    }

    protected void preSwitchIn(){
        mState = STATE.SHOWING;
    }

    protected void postSwitchIn(){
        mState = STATE.SHOWING;
    }

    protected void preSwitchOut(){
    }

    protected void postSwitchOut(){
        mState = STATE.INIT;
    }

    protected void onPause(){
        mState = STATE.BACKGROUND;
    }

    protected void onResume(){
        mState = STATE.SHOWING;
    }

    protected boolean onBackPressed(){
        return false;
    }

}
