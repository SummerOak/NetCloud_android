package com.summer.netcloud.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class JobScheduler {
	private static final String TAG = "JobScheduler";
	private static ScheduledExecutorService sExecutor = null;
	private static boolean sInited = false;
	private static int sRunningTask;
	private static ObjectPool<WJob> sWJobPool;

	public static synchronized void init() {
		if(sInited) {
			return;
		}
		sExecutor = Executors.newScheduledThreadPool(5);
		sWJobPool = new ObjectPool<WJob>(new ObjectPool.IConstructor<WJob>() {
			
			@Override
			public WJob newInstance(Object... params) {
				return new WJob((Job)params[0]);
			}

			@Override
			public void initialize(WJob e, Object... params) {
				e.mJ = (Job)params[0];
			}
			
		}, 5);
		sInited = true;
	}
	
	public static synchronized boolean scheduleBackground(Job j) {
		if(!sInited) {
			return false;
		}
		sExecutor.execute(sWJobPool.obtain(j));
		incTask();
		return true;
	}
	
	public static void terminate() {
		if(sExecutor != null) {
			try {				
				sExecutor.shutdownNow();
			}catch(Throwable t) {
				ExceptionHandler.handleException(t);
			}
		}
	}
	
	private static synchronized void incTask() {
		sRunningTask++;
		Log.d(TAG, "incTask, " + sRunningTask);
	}
	
	private static synchronized void decTask() {
		sRunningTask--;
		Log.d(TAG, "decTask, " + sRunningTask);
	}
	
	public static WJob wrapJob(Job j) {
		return sWJobPool.obtain(j);
	}
	
	public static abstract class Job implements Runnable{
		private String mTag;
		public Job(String tag) {
			mTag = tag;
		}
	}
	
	public static class WJob implements Runnable{
		
		private Job mJ;
		public WJob(Job j) {
			mJ = j;
		}

		@Override
		public void run() {
			if(mJ != null) {				
				mJ.run();
			}
			
			decTask();
			
			sWJobPool.recycle(this);
		}
		
	}
	
}
