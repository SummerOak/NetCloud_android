package com.summer.netcloud.window;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

import java.util.Stack;

/**
 * Created by summer on 12/06/2018.
 */

public class WindowStack implements SwipeDectector.IListener{

    private StackView mRoot;
    private Stack<AbsWindow> mStack;

    private Animation mAnimIn;
    private TranslateAnimation mAnimOut;
    private SwipeDectector mSwipeDetector;

    public WindowStack(Context context){
        mStack = new Stack<>();
        mRoot = new StackView(context);
        mRoot.setBackgroundColor(Color.TRANSPARENT);

        mAnimIn = new TranslateAnimation(Animation.RELATIVE_TO_PARENT,1,Animation.RELATIVE_TO_PARENT,0,
                Animation.RELATIVE_TO_PARENT,0,Animation.RELATIVE_TO_PARENT,0);
        mAnimIn.setDuration(300);
        mAnimOut = new TranslateAnimation(Animation.RELATIVE_TO_PARENT,0,Animation.RELATIVE_TO_PARENT,1,
                Animation.RELATIVE_TO_PARENT,0,Animation.RELATIVE_TO_PARENT,0);
        mAnimOut.setDuration(300);

        mSwipeDetector = new SwipeDectector(this);
    }

    public View getView(){
        return mRoot;
    }

    public AbsWindow getTopWindow(){
        return mStack.peek();
    }

    public boolean onBackPressed(){
        if(mStack.empty()){
            return false;
        }

        if(mStack.peek().onBackPressed()){
            return true;
        }

        if(getSize() > 1){
            pop();
            return true;
        }
        return false;
    }

    public void push(AbsWindow window){
        push(window, true);
    }

    public void push(AbsWindow window, boolean anim){

        if(window == null || mStack.contains(window)){
            return;
        }

        window.preSwitchIn();
        addView(window.getView());
        if(!mStack.isEmpty()){
            AbsWindow top = mStack.peek();
            if(top != null){
                top.onPause();
            }
        }

        if(anim){
            window.getView().startAnimation(mAnimIn);
        }

        mStack.push(window);

        window.postSwitchIn();
    }

    public void pop(){
        pop(mAnimOut);
    }

    private void pop(Animation animation){
        if(mStack.empty()){
            return;
        }

        AbsWindow pop = mStack.pop();
        pop.preSwitchOut();
        pop.getView().startAnimation(animation);
        removeView(pop.getView());
        pop.postSwitchOut();

        if(!mStack.isEmpty()){
            AbsWindow top = mStack.peek();
            if(top != null){
                top.onResume();
            }
        }
    }

    public void onPause(){
        if(!mStack.empty()){
            mStack.peek().onPause();
        }

    }

    public void onResume(){
        if(!mStack.empty()){
            mStack.peek().onResume();
        }
    }

    public int getSize(){
        return mStack.size();
    }

    private void addView(View view){
        mRoot.addView(view, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
    }

    private void removeView(View view){
        mRoot.removeView(view);
    }

    @Override
    public boolean onSwipe(int dx) {
        if(mStack.size() <= 1){
            return false;
        }

        if(dx >= 0){
            AbsWindow top = mStack.peek();
            top.getView().animate().x(dx).setDuration(0).start();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                top.getView().setTranslationZ(20);
            }
        }

        return true;
    }

    @Override
    public boolean onFling() {
        if(mStack.size() <= 1){
            return false;
        }

        return false;
    }

    @Override
    public boolean onSwipeCancel(int dx) {
        AbsWindow top = mStack.peek();
        if(dx >= mRoot.getWidth()/3){
            Animation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT,top.getView().getX(),Animation.RELATIVE_TO_PARENT,1,
                    Animation.RELATIVE_TO_PARENT,0,Animation.RELATIVE_TO_PARENT,0);

            animation.setDuration((int)(((mRoot.getWidth()-top.getView().getX())/mRoot.getWidth())*300));
            pop();
        }else {
            long dur = (long)(((float)dx)/mRoot.getWidth()*400);
            if(dur < 0){
                dur = 0;
            }
            top.getView().animate().x(0).setDuration(dur).start();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                top.getView().setTranslationZ(0);
            }
        }
        return true;
    }

    private class StackView extends FrameLayout {

        public StackView(Context context) {
            super(context);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {

            if(!mSwipeDetector.onTouchEvent(ev)){
                return super.dispatchTouchEvent(ev);
            }

            return true;
        }

    }

}
