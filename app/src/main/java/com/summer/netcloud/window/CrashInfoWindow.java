package com.summer.netcloud.window;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.summer.netcloud.Constants;
import com.summer.netcloud.R;
import com.summer.netcloud.utils.FileUtils;
import com.summer.netcloud.utils.JobScheduler;
import com.summer.netcloud.utils.Log;
import com.summer.netcloud.utils.ResTools;

import java.io.File;

/**
 * Created by summer on 26/06/2018.
 */

public class CrashInfoWindow extends AbsWindow {

    private static final String TAG = Constants.TAG + ".CrashInfoWindow";

    private LinearLayout mContentView;
    private LinearLayout mTitleBar;
    private TextView mTitle;
    private ScrollView mScrollView;
    private TextView mCrashInfo;

    private String mCrashFile;

    public CrashInfoWindow(Context context) {
        super(context);
    }

    public void showCrash(String crashFilePath){
        mCrashFile = crashFilePath;

        if(mCrashFile != null){
            int i = mCrashFile.lastIndexOf(File.separator);
            if(i >= 0){
                mTitle.setText(mCrashFile.substring(i+1));
            }else{
                mTitle.setText(mCrashFile);
            }
        }else{
            mTitle.setText(mCrashFile);
        }

        update();
    }

    private void initView(Context context){

        mTitleBar = new LinearLayout(context);
        mTitleBar.setGravity(Gravity.CENTER_VERTICAL);
        mTitleBar.setMinimumHeight((int) ResTools.getDimen(R.dimen.title_bar_height));
        mTitleBar.setOrientation(LinearLayout.HORIZONTAL);
        mTitleBar.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        mTitleBar.setBackgroundResource(R.color.black);

        int textSize = (int)ResTools.getDimen(R.dimen.textsize1);
        mTitle = new TextView(context);
        mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        mTitle.setSingleLine();
        mTitle.setTextColor(ResTools.getColor(R.color.background));
        mTitle.setText("Tombstones");
        mTitle.setEllipsize(TextUtils.TruncateAt.START);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = (int)ResTools.getDimen(R.dimen.hor_padding);
        mTitleBar.addView(mTitle,lp);

        mScrollView = new ScrollView(context);
        mScrollView.setBackgroundColor(Color.TRANSPARENT);
        mCrashInfo = new TextView(context);
        mCrashInfo.setTextColor(ResTools.getColor(R.color.text));
        mCrashInfo.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)ResTools.getDimen(R.dimen.textsize3));
        mScrollView.addView(mCrashInfo, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mContentView = new LinearLayout(context);
        mContentView.setBackgroundColor(Color.TRANSPARENT);
        mContentView.setOrientation(LinearLayout.VERTICAL);
        mContentView.addView(mTitleBar);
        mContentView.addView(mScrollView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    protected View getContentView() {
        if(mContentView == null){
            initView(getContext());
        }

        return mContentView;
    }

    private int mTaskSeq = 0;
    private void update(){

        ++mTaskSeq;

        Log.d(TAG,"update " + mTaskSeq);

        JobScheduler.scheduleBackground(new JobScheduler.Job("load-crash-info") {
            final private int taskSeq = mTaskSeq;
            StringBuffer sb;

            @Override
            public void run() {

                sb = FileUtils.readSmallFileText(mCrashFile);
                if(sb != null && sb.length() > 0){
                    Log.d(TAG," seq(" + mTaskSeq + "," + taskSeq + ")" + " isShowing: " + isShowing());

                    if(isShowing() && mTaskSeq == taskSeq && getView().getHandler() != null){
                        getView().getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                if(isShowing() && mTaskSeq == taskSeq){
                                    mCrashInfo.setText(sb.toString());
                                }
                            }
                        });
                    }
                }
            }
        });
    }

}
