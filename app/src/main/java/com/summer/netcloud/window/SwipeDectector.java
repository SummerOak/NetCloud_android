package com.summer.netcloud.window;

import android.view.MotionEvent;

import com.summer.netcloud.utils.ScreenUtils;

/**
 * Created by summer on 09/09/2018.
 */

public class SwipeDectector {

    private IListener mListener;

    private float mX0 = 0;
    private float mY0 = 0;

    private int MOVE_DIS = ScreenUtils.dp2px(15);
    private boolean mSwiping = false;
    private boolean mIgnore = false;

    public SwipeDectector(IListener l){
        mListener = l;
    }


    public boolean onTouchEvent(MotionEvent event){
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:{
                mX0 = event.getRawX();
                mY0 = event.getRawY();
                mSwiping = false;
                mIgnore = false;
                return false;
            }

            case MotionEvent.ACTION_MOVE:{
                if(!mIgnore){
                    float dx = event.getRawX() - mX0;
                    if(mSwiping){
                        mListener.onSwipe((int)dx);
                        return true;
                    }

                    float dy = event.getRawY() - mY0;
                    if(Math.abs(dx) > MOVE_DIS || Math.abs(dy) > MOVE_DIS){
                        if(Math.abs(dx) < Math.abs(dy)){
                            mIgnore = true;
                            return false;
                        }

                        if(mListener.onSwipe((int)dx)){
                            mSwiping = true;
                            return true;
                        }

                        mIgnore = true;
                    }
                }

                break;
            }

            case MotionEvent.ACTION_UP:{
                if(!mIgnore){
                    float dx = event.getRawX() - mX0;
                    if(mSwiping){
                        return mListener.onSwipeCancel((int)dx);
                    }
                }
                break;
            }
        }

        return false;
    }


    public interface IListener{
        boolean onSwipe(int dx);
        boolean onFling();
        boolean onSwipeCancel(int dx);
    }

}
