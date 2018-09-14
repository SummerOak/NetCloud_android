package com.summer.netcloud.window;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.summer.netcloud.ContextMgr;
import com.summer.netcloud.R;
import com.summer.netcloud.utils.ResTools;

/**
 * Created by summer on 12/06/2018.
 */

public abstract class AbsWindow{

    private LinearLayout mView;
    private STATE mState = STATE.INIT;

    private enum STATE{
        INIT,
        SHOWING,
        BACKGROUND,
    }

    public AbsWindow(Context context) {

        mView = new LinearLayout(context);
        mView.setOrientation(LinearLayout.VERTICAL);
        mView.setBackgroundColor(ResTools.getColor(R.color.background));
        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        View titleBar = getTitleBar();
        if(titleBar != null){
            mView.addView(titleBar, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        }

        View content = getContentView();
        if(content != null){
            mView.addView(content,FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
        }

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
        return mView;
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
