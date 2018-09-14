package com.summer.netcloud.startup;

import com.summer.netcloud.Constants;
import com.summer.netcloud.message.Messege;
import com.summer.netcloud.message.MsgDispatcher;
import com.summer.netcloud.utils.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by summer on 12/06/2018.
 */

public class Starter {
    private static final String TAG = Constants.TAG + ".Starter";

    private Set<Step> mSteps = new HashSet<>();
    private Step mCurStep;

    public Starter(){

        add(new ShowMainPage()).depends(new ShowSplash())
        .add(new InitCore());

    }

    public void startup(){
        check();
    }

    private void check(){

        if(!mSteps.isEmpty()){
            Set<Step> ready = new HashSet<>();
            for(Step step:mSteps){
                if(step.prevs.isEmpty()){
                    ready.add(step);
                }
            }

            if(!ready.isEmpty()){
                mSteps.removeAll(ready);
                for(Step step:ready){
                    step.action();
                }
            }
        }else{
            MsgDispatcher.get().dispatch(Messege.START_UP_FINISHED);
        }
    }

    private void onFinish(Step step){
        if(!step.nexts.isEmpty()){
            for(Step s:step.nexts){
                s.prevs.remove(step);
            }
        }


        check();
    }


    public static abstract class Step{

        private Starter starter;

        protected abstract int start();

        private void action(){

            try {
                int ret = start();
                if(ret == 0){
                    finish();
                }

            }catch (Throwable t){
                Log.e(TAG, "task execute exception: \n" + Log.getStackTraceString(t));
                t.printStackTrace();
            }

        }

        protected void finish(){
            starter.onFinish(this);
        }

        private Set<Step> nexts = new HashSet<>();
        private Set<Step> prevs = new HashSet<>();

        private void depends(Step step){
            if(!prevs.contains(step)){
                prevs.add(step);
            }
        }

        private void nexts(Step step){
            if(!nexts.contains(step)){
                nexts.add(step);
            }
        }
    }


    public Starter add(Step step){
        if(!mSteps.contains(step)){
            mSteps.add(step);
            step.starter = this;
        }

        mCurStep = step;

        return this;
    }

    public Starter depends(Step... steps){
        if(mCurStep == null){
            throw new IllegalStateException("steps can not depends on empty steps!");
        }

        if(steps != null && steps.length > 0){
            for(Step step:steps){
                mCurStep.depends(step);
                step.nexts(mCurStep);

                if(!mSteps.contains(step)){
                    mSteps.add(step);
                    step.starter = this;
                }
            }
        }

        return this;
    }

}
