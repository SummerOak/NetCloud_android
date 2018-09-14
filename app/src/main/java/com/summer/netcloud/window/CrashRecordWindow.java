package com.summer.netcloud.window;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.summer.crashsdk.CrashSDK;
import com.summer.netcloud.Constants;
import com.summer.netcloud.ContextMgr;
import com.summer.netcloud.R;
import com.summer.netcloud.message.Messege;
import com.summer.netcloud.message.MsgDispatcher;
import com.summer.netcloud.utils.JobScheduler;
import com.summer.netcloud.utils.Log;
import com.summer.netcloud.utils.ResTools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by summer on 25/06/2018.
 */

public class CrashRecordWindow extends AbsListContentWindow<String, TextView> implements View.OnClickListener{

    private static final String TAG = Constants.TAG + ".CrashRecordWindow";

    private TitleBar mTitleBar;

    public CrashRecordWindow(Context context) {
        super(context);

        setEmptyDescryption("No crash info.");
    }

    @Override
    protected View getTitleBar() {
        if(mTitleBar == null){
            mTitleBar = new TitleBar(getContext());
            mTitleBar.setTitle("Tombstones");
        }

        return mTitleBar;
    }

    @Override
    protected void preSwitchIn() {
        super.preSwitchIn();
        initData();
    }

    @Override
    protected int getItemId(String item) {
        return getData().indexOf(item);
    }

    @Override
    protected TextView createItemView(int position) {
        TextView v = new TextView(getContext());
        v.setTextColor(ResTools.getColor(R.color.text));
        v.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)ResTools.getDimen(R.dimen.textsize2));
        v.setGravity(Gravity.CENTER_VERTICAL);
        int hp = (int)ResTools.getDimen(R.dimen.hor_padding);
        int vp = (int)ResTools.getDimen(R.dimen.vtl_padding);
        v.setPadding(hp,vp,hp,vp);
        v.setOnClickListener(CrashRecordWindow.this);
        v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,  (int)ResTools.getDimen(R.dimen.item_height)));
        v.setBackgroundResource(R.drawable.list_item_bg);

        return v;
    }

    @Override
    protected void bindItem(String item, TextView view) {
        view.setText(item);
        view.setTag(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private int mTaskSeq = 0;
    private void initData(){
        showLoading();

        ++mTaskSeq;

        Log.d(TAG,"update " + mTaskSeq);

        JobScheduler.scheduleBackground(new JobScheduler.Job("load-crash-files") {
            final private int taskSeq = mTaskSeq;
            final List<String> ls = new ArrayList<>();

            @Override
            public void run() {

                String logDir = CrashSDK.getTombstonesDirectory();
                File fLogDir = new File(logDir);
                if(fLogDir.isDirectory()){
                    String[] logs = fLogDir.list();

                    if(logs != null){
                        for(int i=logs.length-1; i>=0;--i){
                            ls.add(logs[i]);
                        }
                    }
                }

                Log.d(TAG,"data: " + ls.size() + " seq(" + mTaskSeq + "," + taskSeq + ")" + " isShowing: " + isShowing());

                if(mTaskSeq == taskSeq){
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG,"11>>> data: ");
                            if(mTaskSeq == taskSeq){
                                Log.d(TAG,"1>>> data: " + getData().size() + " seq(" + mTaskSeq + "," + taskSeq + ")");
                                updateData(ls);
                            }

                        }
                    });
                }

            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v.getTag() instanceof String){
            String name = (String)v.getTag();
            String directory = CrashSDK.getTombstonesDirectory();
            if(directory == null || name == null){
                return;
            }

            String fullPath = directory;
            if(!directory.endsWith(File.separator)){
                fullPath += File.separator;
            }
            fullPath += name;

            Log.d(TAG,"show log: " + fullPath);

            CrashInfoWindow crashInfoWindow = new CrashInfoWindow(ContextMgr.getContext());
            crashInfoWindow.showCrash(fullPath);
            MsgDispatcher.get().dispatchSync(Messege.PUSH_WINDOW, crashInfoWindow);
        }
    }
}
