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

    private Set<Task> mTasks = new HashSet<>();
    private Set<Task> mTaskHolder = new HashSet<>();
    private Task mCurTask;
    private static Starter sInstance = null;


    public Starter(){

        mTasks.clear();
        mTaskHolder.clear();
        mCurTask = null;

        ShowMainPage showMainPage = new ShowMainPage();
        ShowSplash showSplash = new ShowSplash();
        InitCore initCore = new InitCore();
        PermissionAcquire permissionAcquire = new PermissionAcquire();


        add(showSplash).depends(permissionAcquire)
        .add(showMainPage).depends(showSplash,initCore)
        .add(initCore).depends(permissionAcquire);

        mTaskHolder.addAll(mTasks);
    }

    public static void startup(){
        if(sInstance == null){
            sInstance = new Starter();
        }

        sInstance.check();
    }

    private void check(){

        if(!mTasks.isEmpty()){
            Set<Task> ready = new HashSet<>();
            for(Task task : mTasks){
                if(task.prevs.isEmpty()){
                    ready.add(task);
                }
            }

            if(!ready.isEmpty()){
                mTasks.removeAll(ready);

                for(Task task :ready){
                    Log.d(TAG, "execute: " + task.getClass().getName());
                    task.action();
                }
            }
        }else{
            sInstance = null;
            mTaskHolder.clear();
            MsgDispatcher.get().dispatch(Messege.START_UP_FINISHED);
        }
    }

    private void onFinish(Task task){
        if(!task.nexts.isEmpty()){
            for(Task s: task.nexts){
                s.prevs.remove(task);
            }
        }


        check();
    }


    public static abstract class Task {

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

        private Set<Task> nexts = new HashSet<>();
        private Set<Task> prevs = new HashSet<>();

        private void depends(Task task){
            if(!prevs.contains(task)){
                prevs.add(task);
            }
        }

        private void nexts(Task task){
            if(!nexts.contains(task)){
                nexts.add(task);
            }
        }
    }


    public Starter add(Task task){
        if(!mTasks.contains(task)){
            mTasks.add(task);
            task.starter = this;
        }

        mCurTask = task;

        return this;
    }

    public Starter depends(Task... tasks){
        if(mCurTask == null){
            throw new IllegalStateException("tasks can not depends on empty tasks!");
        }

        if(tasks != null && tasks.length > 0){
            for(Task task : tasks){
                mCurTask.depends(task);
                task.nexts(mCurTask);

                if(!mTasks.contains(task)){
                    mTasks.add(task);
                    task.starter = this;
                }
            }
        }

        return this;
    }

}
