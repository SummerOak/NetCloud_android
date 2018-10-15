package com.summer.netcloud.window;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.summer.netcloud.R;
import com.summer.netcloud.message.Messege;
import com.summer.netcloud.message.MsgDispatcher;
import com.summer.netcloud.utils.JobScheduler;
import com.summer.netcloud.utils.PackageUtils;
import com.summer.netcloud.utils.ResTools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by summer on 04/09/2018.
 */

public class AppsSelectWindow extends AbsListContentWindow<AppsSelectWindow.AppItem, AppsSelectWindow.ItemView> implements View.OnClickListener{
    private static final String TAG = "AppsSelectWindow";

    private List<Integer> mExcludes = new ArrayList<>();
    private List<AppItem> mDataList = new ArrayList<>();
    private IResultCallback mCallback;

    private TitleBar mTitleBar;
    private TextView mCancelAll;
    private TextView mDone;

    public AppsSelectWindow(Context context, List<Integer> excludes, IResultCallback callback) {
        super(context);
        mCallback = callback;
        if(excludes != null){
            mExcludes.addAll(excludes);
        }
    }

    @Override
    protected View getTitleBar() {
        if(mTitleBar == null){
            int textSize = (int)ResTools.getDimen(R.dimen.textsize1);
            mTitleBar = new TitleBar(getContext());
            mTitleBar.setTitle(ResTools.getString(R.string.select_apps));

            mDone = new TextView(getContext());
            mDone.setOnClickListener(this);
            mDone.setText(R.string.done);
            mDone.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            mDone.setTextColor(ResTools.getColor(R.color.background));
            mTitleBar.addRight(mDone);

            mCancelAll = new TextView(getContext());
            mCancelAll.setOnClickListener(this);
            mCancelAll.setText(R.string.cancel_all);
            mCancelAll.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            mCancelAll.setTextColor(ResTools.getColor(R.color.background));
            mTitleBar.addRight(mCancelAll);
        }
        return mTitleBar;
    }

    @Override
    protected int getItemId(AppItem item) {
        return mDataList.indexOf(item);
    }

    @Override
    protected void postSwitchIn() {
        super.postSwitchIn();
        initData();
    }

    @Override
    protected ItemView createItemView(int position) {
        return new ItemView(getContext());
    }

    @Override
    protected void bindItem(AppItem item, ItemView view) {
        view.bind(item);
        view.setTag(item);
        view.setOnClickListener(this);
    }

    private void initData(){
        showLoading();

        JobScheduler.scheduleBackground(new JobScheduler.Job("load-installed-apps") {
            @Override
            public void run() {

                final List<AppItem> data = new ArrayList<>();
                List<PackageUtils.AppInfo> appsInfo = PackageUtils.getAllInstallApps();
                if(appsInfo != null){
                    for(PackageUtils.AppInfo appInfo: appsInfo){
                        if(mExcludes.contains(appInfo.uid)){
                            continue;
                        }

                        AppItem item = new AppItem();
                        item.info = appInfo;
                        item.select = false;
                        data.add(item);
                    }
                }

                Collections.sort(data, new Comparator<AppItem>() {
                    @Override
                    public int compare(AppItem o1, AppItem o2) {
                        return o1.info.name.compareTo(o2.info.name);
                    }
                });

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        onDataLoaded(data);
                    }
                });
            }
        });

    }

    private void onDataLoaded(List<AppItem> data){
        mDataList.clear();;
        mDataList.addAll(data);
        updateData(mDataList);
    }

    @Override
    public void onClick(View v) {
        if(v == mCancelAll){
            for(AppItem item:mDataList){
                item.select = false;
            }
            update();
        }else if(v == mDone){

            if(mCallback != null){
                List<Integer> r = new ArrayList<>();
                for(AppItem item:mDataList){
                    if(item.select){
                        r.add(item.info.uid);
                    }
                }

                mCallback.onSelect(r);
            }

            MsgDispatcher.get().dispatchSync(Messege.POP_WINDOW);
        }else if(v.getTag() instanceof AppItem){
            AppItem item = (AppItem)v.getTag();
            item.select = !item.select;
            updateItem(item);
        }
    }

    public class AppItem{
        PackageUtils.AppInfo info;
        boolean select = false;
    }

    public class ItemView extends LinearLayout{

        ImageView mIcon;
        TextView mAppName;

        public ItemView(Context context) {
            super(context);
            setOrientation(HORIZONTAL);
            setGravity(Gravity.CENTER_VERTICAL);
            mIcon = new ImageView(context);
            int iconSize = (int) ResTools.getDimen(R.dimen.icon_size);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(iconSize, iconSize);
            lp.rightMargin = (int)ResTools.getDimen(R.dimen.hor_padding);
            mIcon.setLayoutParams(lp);
            addView(mIcon);

            int textsize = (int)ResTools.getDimen(R.dimen.textsize2);
            mAppName = new TextView(context);
            mAppName.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
            mAppName.setTextColor(ResTools.getColor(R.color.text));
            lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.weight = 1;
            addView(mAppName, lp);

            setMinimumHeight((int)ResTools.getDimen(R.dimen.item_height));

            int hp = (int)ResTools.getDimen(R.dimen.hor_padding);
            int vp = (int)ResTools.getDimen(R.dimen.vtl_padding);
            setPadding(hp,vp,hp,vp);
        }

        private void bind(AppItem item){
            mIcon.setImageDrawable(item.info.icon);
            mAppName.setText(item.info.name);
            if(item.select){
                setBackgroundResource(R.drawable.list_item_bg_selected);
            }else{
                setBackgroundResource(R.drawable.list_item_bg);
            }
        }
    }

    public interface IResultCallback{
        void onSelect(List<Integer> uids);
    }

}
